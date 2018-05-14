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
@Table(name = "NEIGHBOR_DISTRICT")
public class NeighborDistrict {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private		int			id;
	private		int			nDistrictId;
	private		District	district;
	private		int			sid;
	
	public NeighborDistrict(int nDistrictId, District district, int sid) {
		this.nDistrictId = nDistrictId;
		this.district = district;
		this.sid = sid;
	}
	
	public NeighborDistrict(){
		
	}
	
	public int getNDistrictId(){
		return nDistrictId;
	}
	
	public void setNDistrictId(int nDistrictId){
		this.nDistrictId = nDistrictId;
	}
	
	public int getSid(){
		return sid;
	}
	
	public void setSid(int sid){
		this.sid = sid;
	}
	
    @ManyToOne
    @JoinColumn(name = "DISTRICT_DISTRICTID")
    public District getComparingDistrict() {
        return district;
    }
    
    public void setComparingDistrict(District district){
    	this.district = district;
    }
}