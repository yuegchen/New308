package cse308.Thymeleaf.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
//import java.util.Date;
@Entity

@Table
public class Plan {

	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id
	private 	int 	planId;
	private 	String 	creationDate;
	private 	String 	stateName;
	private 	int 	stateId;
	private 	State 	state;
	private 	String 	email;

	public Plan( int pid, String d, String stateName, int stateId, State s, String email) {
		this.planId			= 	pid;
		this.creationDate 	= 	d;
		this.stateName 		= 	stateName;
		this.stateId 		= 	stateId;
		this.state			=	s;
		this.email			=	email;
	}
	
	public Plan( int pid, String d, String stateName, State state, String email) {
		this.planId= pid;
		this.creationDate = d;
		this.stateName = stateName;
		this.state = state;
		this.email=email;
	}

	public Plan() {
		super();
	}

	public int getPlanId() {
		return planId;
	}

	public void setUname(int pid) {
		this.planId=pid;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCD(String d) {
		this.creationDate = d;
	}

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String s) {
		this.stateName = s;
	}

	public int getStateId() {
		return stateId;
	}

	public void setStateId(int id) {
		this.stateId = id;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String e) {
		this.email = e;
	}
	
	public State getState(){
		return state;
	}	
}