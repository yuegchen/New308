package cse308.Thymeleaf.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class BorderingId implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3962748567872957921L;

	@Column(name = "PID", nullable = false)
	private int pid;
	
	@Column(name = "CD", nullable = false)
	private int cd;
	
	public BorderingId(){
		
	}
	
	public BorderingId(int pid, int cd){
		this.pid = pid;
		this.cd = cd;
	}
	
	public int getPrecinctId(){
		return pid;
	}
	
	public int getCongressionalDistrictId(){
		return cd;
	}
	
	@Override
	public boolean equals(Object object){
		if(this == object)
			return true;
		if(!(object instanceof BorderingId))
			return false;
		BorderingId newBorderingId = (BorderingId) object;
		return pid == newBorderingId.getPrecinctId() && cd == newBorderingId.getCongressionalDistrictId();
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(pid, cd);
	}
	
}
