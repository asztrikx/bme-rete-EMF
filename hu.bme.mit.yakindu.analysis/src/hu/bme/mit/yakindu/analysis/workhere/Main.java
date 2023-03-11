package hu.bme.mit.yakindu.analysis.workhere;

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
		System.out.println("Generált print függvény:");
		System.out.println("public static void print(IExampleStatemachine s) {");
		for (VariableDefinition variable : variables) {
			String variableUpperCase = variable.getName().substring(0,1).toUpperCase() + variable.getName().substring(1);
			System.out.println("	System.out.println(\"W = \" + s.getSCInterface().get" + variableUpperCase + "());");
		}
		System.out.println("}");
		
		// Transforming the model into a graph representation
		String content = model2gml.transform(root);
		// and saving it
		manager.saveFile("model_output/graph.gml", content);
	}
}
