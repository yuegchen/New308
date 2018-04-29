package cse308.Thymeleaf.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class District {

	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id

	private 	int 		districtId;
	private 	int 		originalDistrictId;
	private 	int[] 		movedIntoPrecinctList;//store precincts with their id
	private 	int[] 		borderPrecinctList;
	private 	District[] 	neighborDistricts;
	
	private 	int 		districtNameId;
	private 	int[] 		precinctList;//store precincts with their id

	public District( int did, int oDid, int[] inprecinctList, int[] borderPrecinctList) {
		this.districtId				= 	did;
		this.originalDistrictId		=	oDid;
		this.movedIntoPrecinctList	= 	inprecinctList;
		this.borderPrecinctList		=	borderPrecinctList;
	}

	public District( int did, int districtNameId, int[] precinctList) {
		this.districtId= did;
		this.districtNameId = districtNameId;
		this.precinctList= precinctList;
	}
	
	public District() {
		super();
	}

	public int[] getIntoPList() {
		return movedIntoPrecinctList;
	}

	public void setIntoPList(int[] pList) {
		this.movedIntoPrecinctList=pList;
	}
	
	public District[] getNeighborDistricts(){
		return neighborDistricts;
	}
	
	public void setNeighborDistricts(District[] neighborDistricts){
		this.neighborDistricts=neighborDistricts;
	}
	
	public int[] getBorderPrecinctList() {
		return borderPrecinctList;
	}

	public void setBorderPrecinctList(int[] BPList) {
		this.borderPrecinctList=BPList;
	}

	public int getDId() {
		return districtId;
	}

	public void setDId(int id) {
		this.districtId= id;
	}
	public int getODId() {
		return originalDistrictId;
	}

	public void setODId(int id) {
		this.originalDistrictId= id;
	}
	
	public int getDistrictNameId(){
		return districtNameId;
	}
	
	public int[] getPrecinctList(){
		return precinctList;
	}
	
}