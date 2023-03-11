package hu.bme.mit.yakindu.analysis.workhere;

import java.io.PrintWriter;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.junit.Test;
import org.yakindu.base.expressions.expressions.PrimitiveValueExpression;
import org.yakindu.base.types.Direction;
import org.yakindu.sct.model.sgraph.State;
import org.yakindu.sct.model.sgraph.Statechart;
import org.yakindu.sct.model.sgraph.Transition;
import org.yakindu.sct.model.stext.stext.EventDefinition;
import org.yakindu.sct.model.stext.stext.VariableDefinition;

import hu.bme.mit.model2gml.Model2GML;
import hu.bme.mit.yakindu.analysis.example.IExampleStatemachine;
import hu.bme.mit.yakindu.analysis.modelmanager.ModelManager;

public class Main {
	@Test
	public void test() {
		main(new String[0]);
	}
	
	public static void main(String[] args) {
		ModelManager manager = new ModelManager();
		Model2GML model2gml = new Model2GML();
		
		// Loading model
		EObject root = manager.loadModel("model_input/example.sct");
		
		// Reading model
		EList<State> trapStates = new BasicEList<State>();
		EList<String> allStateName = new BasicEList<String>();
		EList<State> unnamedStates = new BasicEList<State>();
		EList<VariableDefinition> variables = new BasicEList<VariableDefinition>();
		EList<EventDefinition> events = new BasicEList<EventDefinition>();
		Statechart s = (Statechart) root;
		TreeIterator<EObject> iterator = s.eAllContents();
		while (iterator.hasNext()) {
			EObject content = iterator.next();
			if(content instanceof State) {
				State state = (State) content;
				System.out.println(state.getName());
				EList<Transition> edges = state.getOutgoingTransitions();
				boolean other = false;
				for (Transition edge: edges) {
					System.out.println(edge.getSource().getName() + " -> " + edge.getTarget().getName());
					if (state != edge.getTarget()) {
						other = true;
					}
				}
				if (!other) {
					trapStates.add(state);
				}
				if(state.getName().equals(""))
					unnamedStates.add(state);
				allStateName.add(state.getName());
			}
			if (content instanceof VariableDefinition) {
				VariableDefinition variable = (VariableDefinition) content;
				System.out.println("Variable: " + variable.getName());
				
				variables.add(variable);
			}
			if (content instanceof EventDefinition) {
				EventDefinition event = (EventDefinition) content;
				if (event.getDirection() == Direction.IN) {
					System.out.println("IN event: " + event.getName());
					
					events.add(event);
				}
			}
		}
		System.out.println("csapdák:");
		for (State trap: trapStates) {
			System.out.println(trap.getName());
		}
		
		System.out.println("Nem volt neve, átnevezve:");
		int i = 0;
		for (State unnamed: unnamedStates) {
			while(allStateName.contains("Unnamed" + i))
				++i;
			unnamed.setName("Unnamed" + i);
			++i;
			System.out.println(unnamed.getName());
		}
		
		// Kódgenerálás
		System.out.println("Generált print függvény:");
		generateCode(variables, events, new PrintWriter(System.out, true));
		
		// Transforming the model into a graph representation
		String content = model2gml.transform(root);
		// and saving it
		manager.saveFile("model_output/graph.gml", content);
	}
	
	public static void generateCode(EList<VariableDefinition> variables, EList<EventDefinition> events, PrintWriter writer) {
		String pre = "package hu.bme.mit.yakindu.analysis.workhere;\r\n" + 
				"\r\n" + 
				"import java.io.BufferedReader;\r\n" + 
				"import java.io.IOException;\r\n" + 
				"import java.io.InputStreamReader;\r\n" + 
				"\r\n" + 
				"import hu.bme.mit.yakindu.analysis.RuntimeService;\r\n" + 
				"import hu.bme.mit.yakindu.analysis.TimerService;\r\n" + 
				"import hu.bme.mit.yakindu.analysis.example.ExampleStatemachine;\r\n" + 
				"import hu.bme.mit.yakindu.analysis.example.IExampleStatemachine;\r\n" + 
				"\r\n" + 
				"public class RunStatechart {\r\n" + 
				"	\r\n" + 
				"	public static void main(String[] args) throws IOException {\r\n" + 
				"		ExampleStatemachine s = new ExampleStatemachine();\r\n" + 
				"		s.setTimer(new TimerService());\r\n" + 
				"		RuntimeService.getInstance().registerStatemachine(s, 200);\r\n" + 
				"		\r\n" + 
				"		s.init();\r\n" + 
				"		s.enter();\r\n" + 
				"		s.runCycle();\r\n" + 
				"		\r\n" + 
				"		print(s);\r\n" + 
				"		\r\n" + 
				"		while(true) {\r\n" + 
				"		    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));\r\n" + 
				"		    boolean exit = false;\r\n" + 
				"		    String input = br.readLine();\r\n" + 
				"		    switch(input) {";

		String mid = "		    case \"exit\":\r\n" + 
				"					exit = true;\r\n" + 
				"					s.exit();\r\n" + 
				"					break;\r\n" + 
				"			}\r\n" + 
				"		    s.runCycle();\r\n" + 
				"		    print(s);\r\n" + 
				"		    if (exit) break;\r\n" + 
				"		}\r\n" + 
				"		System.exit(0);\r\n" + 
				"	}\r\n" + 
				"\r\n" + 
				"	public static void print(IExampleStatemachine s) {";

		String end = "	}\r\n" + 
				"}\r\n" + 
				"";
		

		writer.println(pre);
		for (EventDefinition event : events) {
			writer.println("				case \"" + event.getName() + "\":");
			writer.println("					s.raise" + toFirstLetterUpperCase(event.getName()) + "();");
			writer.println("					break;");
		}
		writer.println(mid);
		for (VariableDefinition variable : variables) {
			writer.println("		System.out.println(\"" + toFirstLetterUpperCase(variable.getName()).charAt(0) + " = \" + s.getSCInterface().get" + toFirstLetterUpperCase(variable.getName()) + "());");
		}
		writer.println(end);
	}
	
	public static String toFirstLetterUpperCase(String text) {
		return text.substring(0,1).toUpperCase() + text.substring(1);
	}
}
