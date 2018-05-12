package cse308.Thymeleaf.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Persistence;
import javax.persistence.Table;

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

    private List<Integer> movedIntoPrecinctList; //store precincts with their id

    private List <Precinct> borderingPrecinctList = new ArrayList <Precinct>();;
    
    private List <Precinct> precinctList = new ArrayList <Precinct> (); //store precincts with their id

    private int stateId = 27;

    public District(int districtId, List<Integer> inprecinctList, List <Precinct> borderingPrecinctList) {
        this.districtId = districtId;
        this.movedIntoPrecinctList = inprecinctList;
        this.borderingPrecinctList = borderingPrecinctList;
    }

    public District(int districtId) {
        this.districtId = districtId;
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
    		  "SELECT nd.NDISTRICTID FROM NEIGHBOR_DISTRICT nd WHERE nd.DISTRICT_CD = ?")
          .setParameter(1, districtId)
          .getResultList();
      
      List <District> nDistList = new ArrayList<District>();
      for (int i = 0; i < nDistIdList.size(); i++) {
          District precinct = em.find(District.class, (int) nDistIdList.get(i));
          nDistList.add(precinct);
      }
      return nDistList;
	}
	
//  @OneToMany(mappedBy = "district", cascade = CascadeType.ALL)
//  public List < BorderingPrecinct > getBorderingPrecinctList() {
//      return borderingPrecinctList;
//  }
  
  public List <Precinct> initBorderingPrecinctList() {
	  EntityManagerFactory emf = Persistence.createEntityManagerFactory("Eclipselink_JPA");
	  EntityManager em = emf.createEntityManager();
      List <?> borderPrecIdList = (List <?> ) em.createNativeQuery(
              "SELECT bp.pid FROM BORDERING_PRECINCT bp WHERE bp.DISTRICT_CD = ?")
          .setParameter(1, districtId)
          .getResultList();
      
      List <Precinct> borderPrecList = new ArrayList<Precinct>();
      for (int i = 0; i < borderPrecIdList.size(); i++) {
          Precinct precinct = em.find(Precinct.class, (int) borderPrecIdList.get(i));
          borderPrecList.add(precinct);
      }
      return borderPrecList;
  }
  
  public List<Precinct> getBorderingPrecinctList(){
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
    	EntityManagerFactory emf = Persistence.createEntityManagerFactory("Eclipselink_JPA");
    	EntityManager em = emf.createEntityManager();
    	List<?> precIdList = (List<?>) em.createQuery(
				"SELECT p.pid FROM Precinct p WHERE p.cd = :cd")
				.setParameter("cd", districtId)
				.getResultList();
    	List<Precinct> precList = new ArrayList<Precinct>();
    	for(int i = 0; i < precIdList.size(); i++){
    		Precinct precinct = em.find(Precinct.class, (int)precIdList.get(i));
    		precList.add(precinct);
    	}
    	return precList;
    }
    
    public List<Precinct> getPrecinctList(){
    	return precinctList;
    }
    
    public void setPrecinctList(List<Precinct> precinctList){
    	this.precinctList = precinctList;
    }
	
	public double getArea() throws IOException{
		double districtArea = 0.0;
//		System.out.println(getPrecinctList().size());
		
		for(Precinct precinct: initPrecList()){
//			System.err.println("tried__________________________");
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
		
		List<?>				districtBorderPrecinctIds		=	(List<?>) em.createQuery(
				"SELECT p1.pid, p2.pid FROM Precinct p1, Precinct p2, NeighborPrecinct np WHERE (p1.pid = np.precinct.pid) AND (p2.pid = " + 
				"np.nid AND p1.cd != p2.cd) AND (p1.cd = :cd)").setParameter("cd", districtId).getResultList();
		
		for(int i = 0; i < districtBorderPrecinctIds.size(); i++){
			PrecinctGeometry precBound = em.find(PrecinctGeometry.class, ((Integer)((Object[]) districtBorderPrecinctIds.get(i))[0]));
			PrecinctGeometry neighborPrecBound = em.find(PrecinctGeometry.class, ((Integer)((Object[]) districtBorderPrecinctIds.get(i))[1]));
			districtPerimeter += getDistBorderPrecBoundIntPerimter(precBound, neighborPrecBound, geometryJson, gpr);
		}
		
		List<?> 			stateBorderPrecinctIds			=	(List<?>) em.createQuery(
				"SELECT p.pid FROM Precinct p, NeighborPrecinct np WHERE (p.pid = np.precinct.pid) AND (np.nid = :stateId) AND (p.cd = :cd)")
				.setParameter("stateId", State.MAX_STATE_ID_INITIAL-stateId)
				.setParameter("cd", districtId)
				.getResultList();
		StateGeometry stateBorder = em.find(StateGeometry.class, stateId);
		for(int i = 0; i < stateBorderPrecinctIds.size(); i++){
			PrecinctGeometry precBound = em.find(PrecinctGeometry.class, ((Integer)((Object[]) districtBorderPrecinctIds.get(i))[0]));
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