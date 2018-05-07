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
	
	public NeighborDistrict(int nDistrictId, District district) {
		this.nDistrictId = nDistrictId;
		this.district = district;
	}
	
	public NeighborDistrict(){
		
	}
	
	public int getNDistrictId(){
		return nDistrictId;
	}
	
	public void setNDistrictId(int nDistrictId){
		this.nDistrictId = nDistrictId;
	}
	
    @ManyToOne
    @JoinColumn(name = "DISTRICT_DISTRICTID")
    public District getComparingDistrict() {
        return district;
    }
    
    public void setComparingPrecinct(District district){
    	this.district = district;
    }
}