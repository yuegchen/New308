package cse308.Thymeleaf.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Persistence;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.geotools.geojson.geom.GeometryJSON;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

import cse308.Thymeleaf.model.Type;

@Entity
@Table(name = "DISTRICT")
public class District {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    @Column(name = "CD")
    private int districtId;
    private int stateId;
    
    @Transient
    private List<Integer> movedIntoPrecinctList= new ArrayList <Integer>(); //store precincts with their id

    @Transient
    private List <Precinct> borderingPrecinctList = new ArrayList <Precinct>();;
    
    @Transient
    private List <Precinct> precinctList = new ArrayList <Precinct> (); //store precincts with their id


    public District(int districtId, int stateId, List<Integer> inprecinctList, List <Precinct> borderingPrecinctList) {
        this.districtId = districtId;
        this.stateId = stateId;
        this.movedIntoPrecinctList = inprecinctList;
        this.borderingPrecinctList = borderingPrecinctList;
    }

    public District(int districtId, int stateId) {
        this.districtId = districtId;
        this.stateId = stateId;
        this.precinctList = initPrecList();
        this.borderingPrecinctList = initBorderingPrecinctList();
    }

    public District() {
        super();
    }

    public List<Integer> getIntoPList() {
        return movedIntoPrecinctList;
    }

	public void setIntoPList(List<Integer> pList) {
		this.movedIntoPrecinctList=pList;
	}
	
	public List<District> getNeighborDistricts(){
	  EntityManagerFactory emf = Persistence.createEntityManagerFactory("Eclipselink_JPA");
	  EntityManager em = emf.createEntityManager();
      List <?> nDistIdList = (List <?> ) em.createNativeQuery(
    		  "SELECT nd.NDISTRICTID FROM NEIGHBOR_DISTRICT nd WHERE nd.DISTRICT_CD = ?1"
    		  + " AND nd.SID = ?2")
          .setParameter(1, districtId)
          .setParameter(2, stateId)
          .getResultList();
      
      List <District> nDistList = new ArrayList<District>();
      for (int i = 0; i < nDistIdList.size(); i++) {
          District district = new District((int)nDistIdList.get(i), stateId);
          nDistList.add(district);
      }
      return nDistList;
	}
	
//  @OneToMany(mappedBy = "district", cascade = CascadeType.ALL)
//  public List < BorderingPrecinct > getBorderingPrecinctList() {
//      return borderingPrecinctList;
//  }
  
  public List <Precinct> initBorderingPrecinctList() {
	  if(borderingPrecinctList.size() == 0){
		  EntityManagerFactory emf = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		  EntityManager em = emf.createEntityManager();
	      List <?> borderPrecIdList = (List <?>) em.createNativeQuery(
	              "SELECT bp.pid FROM BORDERING_PRECINCT bp, PRECINCT p WHERE bp.DISTRICT_CD = ?1"
	              + " AND p.SID = ?2 AND p.PID = bp.PID")
	          .setParameter(1, districtId)
	          .setParameter(2, stateId)
	          .getResultList();
	      
	      for (int i = 0; i < borderPrecIdList.size(); i++) {
	          Precinct precinct = em.find(Precinct.class, (int) borderPrecIdList.get(i));
	          borderingPrecinctList.add(precinct);
	      }
	  }
      return borderingPrecinctList;
  }
  
  public void setBorderingPrecinctList(List<Precinct> bPrecinctList){
  	this.borderingPrecinctList = bPrecinctList;
  }

	public int getDId() {
		return districtId;
	}

	public void setDId(int id) {
		this.districtId= id;
	}
    
	public List<Precinct> initPrecList(){
		if(precinctList.size() == 0){
	    	EntityManagerFactory emf = Persistence.createEntityManagerFactory("Eclipselink_JPA");
	    	EntityManager em = emf.createEntityManager();
	    	List<?> precIdList = (List<?>) em.createQuery(
					"SELECT p.pid FROM Precinct p WHERE p.cd = :cd AND p.sid = :sid")
					.setParameter("cd", districtId)
					.setParameter("sid", stateId)
					.getResultList();
	    	for(int i = 0; i < precIdList.size(); i++){
	    		Precinct precinct = em.find(Precinct.class, (int)precIdList.get(i));
	    		precinctList.add(precinct);
	    	}
		}
    	return precinctList;
    }
	public int getPop() {
		int pop = 0;
		for (Precinct p : initPrecList()) {
			pop += p.getPopulation();
		}

		return pop;
	}
	
	public double getDem(){
		double dem=0;
		double demPop=0;
		for (Precinct p : initPrecList()) {
			demPop+= p.getPopulation()*p.getDem();
		}
		dem=demPop/(double)getPop();
		return dem;
	}
	
	public double getRep(){
		double rep=0;
		double repPop=0;
		for (Precinct p : initPrecList()) {
			repPop+= p.getPopulation()*p.getRep();
		}
		rep=repPop/(double)getPop();
		return rep;
	}
        
    public void setPrecinctList(List<Precinct> precinctList){
    	this.precinctList = precinctList;
    }
	
	public double getArea() throws IOException{
		double districtArea = 0.0;
		
		for(Precinct precinct: initPrecList()){
			districtArea += precinct.getArea();
		}
		return districtArea;
	}
	
	public double getPerimeter() throws IOException{
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager em = emf.createEntityManager();
		double districtPerimeter = 0.0;
		GeometryJSON			geometryJson	=	new GeometryJSON();
		GeometryPrecisionReducer gpr			=	new GeometryPrecisionReducer(new PrecisionModel(10));
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for(Precinct precinct: borderingPrecinctList){
			List<Precinct> neighborPrecincts = precinct.getNeighborPrecinctList();
			for(Precinct nPrecinct: neighborPrecincts){
				map.put(nPrecinct.getPid(), precinct.getPid());
			}
		}
//		List<?>				districtBorderPrecinctIds		=	(List<?>) em.createQuery(
//				"SELECT p1.pid, p2.pid FROM Precinct p1, Precinct p2, NeighborPrecinct np WHERE (p1.pid = np.precinct.pid) AND (p2.pid = " + 
//				"np.nid AND p1.cd != p2.cd) AND (p1.cd = :cd) AND (p1.sid = :sid)")
//				.setParameter("cd", districtId)
//				.setParameter("sid", stateId)
//				.getResultList();
		
//		List<?> 			stateBorderPrecinctIds			=	(List<?>) em.createQuery(
//				"SELECT p.pid FROM Precinct p, NeighborPrecinct np WHERE (p.pid = np.precinct.pid) AND (np.nid = :stateId) AND (p.cd = :cd)")
//				.setParameter("stateId", State.MAX_STATE_ID_INITIAL-stateId)
//				.setParameter("cd", districtId)
//				.getResultList();
		
		for(Map.Entry<Integer, Integer> districtBorderPrecinctIds: map.entrySet()){
			PrecinctGeometry precBound = em.find(PrecinctGeometry.class, districtBorderPrecinctIds.getValue());
			PrecinctGeometry neighborPrecBound = em.find(PrecinctGeometry.class, districtBorderPrecinctIds.getKey());
			districtPerimeter += getDistBorderPrecBoundIntPerimter(precBound, neighborPrecBound, geometryJson, gpr);
		}
		StateGeometry stateBorder = em.find(StateGeometry.class, stateId);
		for(int i = 0; i < borderingPrecinctList.size(); i++){
			PrecinctGeometry precBound = em.find(PrecinctGeometry.class, borderingPrecinctList.get(i).getPid());
			districtPerimeter += getStateBorderPrecBoundIntPerimeter(precBound, stateBorder, geometryJson, gpr);
		}
		em.close();
		return districtPerimeter;
	}
	
	public double getDistBorderPrecBoundIntPerimter(PrecinctGeometry precBound, PrecinctGeometry distBorder, GeometryJSON geometryJson,
			GeometryPrecisionReducer gpr) throws IOException{
		Geometry intersection = gpr.reduce(PrecinctGeometry.getPrecinctGeometries(precBound, geometryJson)).intersection(
				gpr.reduce(PrecinctGeometry.getPrecinctGeometries(distBorder, geometryJson)));
		return getIntersectionPerimeter(intersection);
	}
	
	public double getStateBorderPrecBoundIntPerimeter(PrecinctGeometry precBound, StateGeometry stateBorder, GeometryJSON geometryJson,
			GeometryPrecisionReducer gpr) throws IOException{
		Polygon	stateGeometry = StateGeometry.getStateGeometry(stateBorder, geometryJson);
		double perimeter = 0;
		if(Type.valueOf(precBound.getType()).equals(Type.MULTIPOLYGON)){
			Geometry precGeometry = (MultiPolygon) PrecinctGeometry.getPrecinctGeometries(precBound, geometryJson);
			for(int i = 0; i < precGeometry.getNumGeometries(); i++){
				perimeter += getIntersectionPerimeter(((gpr.reduce(((Polygon)precGeometry.getGeometryN(i)).getExteriorRing())).intersection(
						gpr.reduce(stateGeometry.getExteriorRing()))));
			}
		}else{
			perimeter = getIntersectionPerimeter((gpr.reduce(((Polygon) PrecinctGeometry.getPrecinctGeometries(precBound, geometryJson)).getExteriorRing())
					.intersection(gpr.reduce(stateGeometry.getExteriorRing()))));
		}
		return perimeter;
		
	}
	
	public double getIntersectionPerimeter(Geometry intersection){
		if(Type.valueOf(intersection.getGeometryType().toUpperCase()).equals(Type.POINT) ||
				Type.valueOf(intersection.getGeometryType().toUpperCase()).equals(Type.MULTIPOINT) )
			return 0;
		return (Type.valueOf(intersection.getGeometryType().toUpperCase()).equals(Type.LINESTRING) ||
				Type.valueOf(intersection.getGeometryType().toUpperCase()).equals(Type.MULTILINESTRING)) ? intersection.getLength() :
			intersection.getLength()/2;
	}
	
}