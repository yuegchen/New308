package cse308.Thymeleaf.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Statistics {

	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id
	private	int 	id;
	private int 	minnesota;
	private int 	connecticut;
	private int 	massachusetts;

	public Statistics ( int id, int Minnesota, int Connecticut, int Massachusetts) {
		this.id 	=	id;
		this.minnesota 		= 	Minnesota;
		this.connecticut 		= 	Connecticut;
		this.massachusetts 		= 	Massachusetts;
	}	
	
	public Statistics() {
		super();
	}

	public int getMinnesota() {
		return minnesota;
	}

	public void setMinnesota(int minnesota) {
		this.minnesota = minnesota;
	}

	public int getConnecticut() {
		return connecticut;
	}

	public void setConnecticut(int connecticut) {
		this.connecticut = connecticut;
	}

	public int getMassachusetts() {
		return massachusetts;
	}

	public void setMassachusetts(int massachusetts) {
		this.massachusetts = massachusetts;
	}


}

