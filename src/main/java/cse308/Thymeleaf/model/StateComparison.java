package cse308.Thymeleaf.model;

public class StateComparison {
	private int stateId1;
	private int stateId2;
	
	public StateComparison(){
		
	}
	
	public StateComparison(int stateId1, int stateId2){
		this.stateId1 = stateId1;
		this.stateId2 = stateId2;
	}
	
	public int getStateId1(){
		return stateId1;
	}
	
	public void setStateId1(int stateId1){
		this.stateId1 = stateId1;
	}
	
	public int getStateId2(){
		return stateId2;
	}
	
	public void setStateId2(int stateId2){
		this.stateId2 = stateId2;
	}
}
