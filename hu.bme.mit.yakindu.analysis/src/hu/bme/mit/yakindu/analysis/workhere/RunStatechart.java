package hu.bme.mit.yakindu.analysis.workhere;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import hu.bme.mit.yakindu.analysis.RuntimeService;
import hu.bme.mit.yakindu.analysis.TimerService;
import hu.bme.mit.yakindu.analysis.example.ExampleStatemachine;
import hu.bme.mit.yakindu.analysis.example.IExampleStatemachine;

public class RunStatechart {
	
	public static void main(String[] args) throws IOException {
		ExampleStatemachine s = new ExampleStatemachine();
		s.setTimer(new TimerService());
		RuntimeService.getInstance().registerStatemachine(s, 200);
		
		s.init();
		s.enter();
		s.runCycle();
		
		print(s);
		
		while(true) {
		    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		    boolean exit = false;
		    String input = br.readLine();
		    switch(input) {
				case "finish":
					s.raiseFinish();
					break;
				case "purple":
					s.raisePurple();
					break;
				case "yellow":
					s.raiseYellow();
					break;
		    case "exit":
					exit = true;
					s.exit();
					break;
			}
		    s.runCycle();
		    print(s);
		    if (exit) break;
		}
		System.exit(0);
	}

	public static void print(IExampleStatemachine s) {
		System.out.println("P = " + s.getSCInterface().getPurpleTime());
		System.out.println("Y = " + s.getSCInterface().getYellowTime());
	}
}
