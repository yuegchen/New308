package cse308.Thymeleaf.model;

import java.io.IOException;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Persistence;
import javax.persistence.Table;

import org.geotools.geojson.geom.GeometryJSON;

@Entity
@Table(name="PRECINCT")

public class Precinct {
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id
	private 	int 		pid;
	private 	String 		name;
	private 	int 	cd;
	private		int		area;
	
//	private		District	district;
	private List<NeighborPrecinct> neighborPrecincts;
	

	public Precinct(int pid, String name, int cd, int area) {
		this.pid = pid;
		this.name = name;
		this.cd = cd;
		this.area = area;
	}
	
	public Precinct(){
		
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getCd() {
		return cd;
	}

	public void setCd(int cd) {
		this.cd = cd;
	}
	
//    @ManyToOne
//    @JoinColumn(name = "CD")
//	public District getDistrict(){
//    	
//		return district;
//	}
//	
	

	public double getArea() throws IOException{
		double area;
		EntityManagerFactory	emf				=	Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager			em				=	emf.createEntityManager();
		GeometryJSON			geometryJson	=	new GeometryJSON();
		area = PrecinctGeometry.getPrecinctGeometries(em.find(PrecinctGeometry.class, pid), geometryJson).getArea();
		em.close();
		return area;
	}

//	@OneToMany(mappedBy = "precinct", cascade = CascadeType.ALL)
	public List<NeighborPrecinct> getNeighborPrecinctList(){
		return neighborPrecincts;
	}
	
	public void setNeighborPrecinctList(List<NeighborPrecinct> neighborPrecincts){
		this.neighborPrecincts = neighborPrecincts;
	}

}