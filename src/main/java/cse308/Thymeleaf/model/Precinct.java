package cse308.Thymeleaf.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.geotools.geojson.geom.GeometryJSON;

@Entity
@Table(name="PRECINCT")

public class Precinct {
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id
	private 	int 		pid;
	private 	String 		name;
	private 	int 		cd;
	private		int			sid;
	private		double		area;
	private 	int 		population;
	private 	double 		dem;
	private 	double 		rep;
	
	@Transient
	private List<Integer> nPrecIdList = new ArrayList<Integer>();
	@Transient
	private List<Precinct> neighborPrecincts = new ArrayList<Precinct> ();
	
	
	public Precinct(int pid, String name, int cd, double area, int sid, int population, 
			double dem, double rep) {
		this.pid = pid;
		this.name = name;
		this.cd = cd;
		this.area = area;
		this.sid = sid;
		this.population = population;
		this.dem = dem;
		this.rep = rep;
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
	
	public int getSid(){
		return sid;
	}
	
	public void setSid(int sid){
		this.sid = sid;
	}
	
	public double getDem() {
		return dem;
	}

	public void setDem(double dem) {
		this.dem = dem;
	}
	
	public double getRep() {
		return rep;
	}

	public void setRep(double rep) {
		this.rep = rep;
	}
	
	public int getPopulation() {
		return population;
	}

	public void setPopulation(int population) {
		this.population = population;
	}
//    @ManyToOne
//    @JoinColumn(name = "CD")
//	public District getDistrict(){
//    	
//		return district;
//	}
//	
	@Column(name="AREA")
	public void setArea(double area){
		this.area = area;
	}
	
	public double getArea(){
		return area;
	}

	
	public double initArea() throws IOException{
		System.out.println("sid " + sid);
		
		double tempArea = 0;
		EntityManagerFactory	emf				=	Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager			em				=	emf.createEntityManager();
		GeometryJSON			geometryJson	=	new GeometryJSON();
		tempArea = PrecinctGeometry.getPrecinctGeometries(em.find(PrecinctGeometry.class, pid), geometryJson).getArea();
		em.getTransaction().begin();
		Query query = em
				.createQuery("UPDATE Precinct p SET p.area = :tempArea "
				+ "WHERE p.pid= :pid AND p.sid = :sid");
		query.setParameter("tempArea", tempArea);
		query.setParameter("pid", pid);
		query.setParameter("sid", sid);
		query.executeUpdate();
		em.getTransaction().commit();
		em.close();
		
		return tempArea;
	} 

//	@OneToMany(mappedBy = "precinct", cascade = CascadeType.ALL)
	@SuppressWarnings("unchecked")
	public List<Precinct> getNeighborPrecinctList(){
		if(neighborPrecincts.size() == 0){
		  EntityManagerFactory emf = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		  EntityManager em = emf.createEntityManager();
	      nPrecIdList = (List<Integer> ) em.createNativeQuery(
	    		  "SELECT np.NID FROM NEIGHBOR_PRECINCT np WHERE np.PRECINCT_PID = ?")
	          .setParameter(1, pid)
	          .getResultList();
	      
	      for (int i = 0; i < nPrecIdList.size(); i++) {
	    	  if((int)nPrecIdList.get(i) < 999999900){
	    		  Precinct precinct = em.find(Precinct.class, (int) nPrecIdList.get(i));
	        	  neighborPrecincts.add(precinct);
	    	  }
	      }
		}
		return neighborPrecincts;
	}
	
	public boolean isNeighbor(int precinctId){
		if(neighborPrecincts.size() == 0){
			getNeighborPrecinctList();
		}
		return nPrecIdList.contains(precinctId);
	}
	
//	public void setNeighborPrecinctList(List<NeighborPrecinct> neighborPrecincts){
//		this.neighborPrecincts = neighborPrecincts;
//	}

}