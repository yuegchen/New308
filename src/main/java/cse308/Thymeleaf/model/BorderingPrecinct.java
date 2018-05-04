package cse308.Thymeleaf.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "BORDERING_PRECINCT")
public class BorderingPrecinct {
	
	@Id
	private int pid;
	
	private District district;
	public BorderingPrecinct(Precinct precinct, District district){
		pid = precinct.getPid();
		this.district = district;
	}
	
	public BorderingPrecinct(){
		
	}
	
    @ManyToOne
    @JoinColumn(name = "DISTRICT_CD")
    public District getDistrict() {
        return district;
    }
	
}
