package cse308.Thymeleaf.message;

import cse308.Thymeleaf.model.State;

public class StateComparisonMessage {
	private State[] states = new State[2];
	
	public StateComparisonMessage(){
		
	}
	
	public StateComparisonMessage(State state1, State state2){
		states[0] = state1;
		states[1] = state2;
	}
	
	public State[] getStates(){
		return states;
	}
	
	public void setStates(State[] states){
		this.states = states;
	}
	
}
