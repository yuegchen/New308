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
	private		int			stateId;
	
	private		double		area;
	
//	private		District	district;
	private List<NeighborPrecinct> neighborPrecincts;
	

	public Precinct(int pid, String name, int cd, int area, int stateId) {
		this.pid = pid;
		this.name = name;
		this.cd = cd;
		this.area = area;
		this.stateId = stateId;
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
	
	public int getStateId(){
		return stateId;
	}
	
	public void setStateId(int stateId){
		this.stateId = stateId;
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
		double tempArea = 0;
		EntityManagerFactory	emf				=	Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager			em				=	emf.createEntityManager();
		GeometryJSON			geometryJson	=	new GeometryJSON();
		tempArea = PrecinctGeometry.getPrecinctGeometries(em.find(PrecinctGeometry.class, pid), geometryJson).getArea();
		em.getTransaction().begin();
		Query query = em
				.createQuery("UPDATE Precinct p SET p.area = :tempArea "
				+ "WHERE p.pid= :pid");
		query.setParameter("tempArea", tempArea);
		query.setParameter("pid", pid);
		query.executeUpdate();
		em.getTransaction().commit();
		em.close();
		
		return tempArea;
	}

//	@OneToMany(mappedBy = "precinct", cascade = CascadeType.ALL)
	public List<Precinct> getNeighborPrecinctList(){
		  EntityManagerFactory emf = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		  EntityManager em = emf.createEntityManager();
	      List <?> nPrecIdList = (List <?> ) em.createNativeQuery(
	    		  "SELECT np.NID FROM NEIGHBOR_PRECINCT np WHERE np.PRECINCT_PID = ?")
	          .setParameter(1, pid)
	          .getResultList();
	      
	      List <Precinct> nPrecList = new ArrayList<Precinct>();
	      for (int i = 0; i < nPrecIdList.size(); i++) {
	          Precinct precinct = em.find(Precinct.class, (int) nPrecIdList.get(i));
	          nPrecList.add(precinct);
	      }
	      return nPrecList;
		}
	
	public void setNeighborPrecinctList(List<NeighborPrecinct> neighborPrecincts){
		this.neighborPrecincts = neighborPrecincts;
	}

}