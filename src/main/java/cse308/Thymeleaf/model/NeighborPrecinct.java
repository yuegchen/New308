package cse308.Thymeleaf.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "NEIGHBOR_PRECINCT")
public class NeighborPrecinct {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private		int			id;
	
	private		int			nid;
	private		Precinct	precinct;
	

	public NeighborPrecinct(int nid, Precinct precinct) {
		this.nid = nid;
		this.precinct = precinct;
	}
	
	public NeighborPrecinct(){
		
	}
	
	public int getId(){
		return nid;
	}
	
	public void setId(int nid){
		this.nid = nid;
	}
	
    @ManyToOne
    @JoinColumn(name = "PRECINCT_PID")
    public Precinct getComparingPrecicnt() {
        return precinct;
    }
    
    public void setComparingPrecinct(Precinct precinct){
    	this.precinct = precinct;
    }
}