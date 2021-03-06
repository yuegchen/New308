package cse308.Thymeleaf.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.geotools.geojson.geom.GeometryJSON;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

@Entity
@Table
public class State {
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id
	private 	int 		stateId;
	private 	String 		stateName;

	@Transient
	private 	List<District> 		distList = new ArrayList<District>(); 
	
	public 		static		final		int		MAX_STATE_ID_INITIAL = 999999999;
	
	public State( int sid, String stateName) {
		this.stateId		= 	sid;
		this.stateName 		= 	stateName;
	}
	//Test Use
	public State(int stateId){
		this.stateId = stateId;
		initDistList();
	}
	
	public State() {
		super();
	}
	
	public String getStateName() {
		return stateName;
	}

	public void setStateName(String s) {
		this.stateName= s;
	}

	public void initiNeighborPrecincts() throws IOException{
		EntityManagerFactory	emf				=	Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager			em				=	emf.createEntityManager();
		List<?>					precinctIds		=	(List<?>) em.createQuery("SELECT p.pid FROM Precinct p WHERE p.sid = :sid").setParameter("sid", stateId).getResultList();
		GeometryJSON			geometryJson	=	new GeometryJSON();
		List<PrecinctGeometry>	precinctGeometries=	new ArrayList<PrecinctGeometry>();
		List<Precinct>			precincts		=	new ArrayList<Precinct>();
		
		Geometry				statePolygon	=	StateGeometry.getStateGeometry(em.find(StateGeometry.class, stateId), geometryJson);
		System.err.println("StateId: " + stateId);
		for(int i = 0; i < precinctIds.size(); i++)
		{
			precinctGeometries.add(em.find(PrecinctGeometry.class, (int)precinctIds.get(i))); 
			precincts.add(em.find(Precinct.class, (int)precinctIds.get(i)));
			System.out.println("Precinct Ids: " + (int)precinctIds.get(i));
		}
		for(int i = 0; i < precinctIds.size(); i++){
			em.getTransaction().begin();
			for(int neighborI = 0; neighborI < precinctIds.size(); neighborI++){
				if(i != neighborI){
					if(PrecinctGeometry.isNeighborPrecincts(PrecinctGeometry.getPrecinctGeometries(precinctGeometries.get(neighborI), geometryJson), 
							PrecinctGeometry.getPrecinctGeometries(precinctGeometries.get(i), geometryJson))){
						em.persist(new NeighborPrecinct((int)precinctIds.get(neighborI), precincts.get(i)));
					}
				}
			}
			if(StateGeometry.intersects(statePolygon, PrecinctGeometry.getPrecinctGeometries(precinctGeometries.get(i), geometryJson))){
				em.persist(new NeighborPrecinct(MAX_STATE_ID_INITIAL-stateId, precincts.get(i)));
			}
			em.getTransaction().commit();
			System.out.println("first precinct reached");
		}
		em.close();
	}
	
	public void initiBorderingPrecincts() throws IOException {
		EntityManagerFactory	emf				=	Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager			em				=	emf.createEntityManager();
		List<?>				districtBorderPrecinctIds		=	(List<?>) em.createQuery(
				"SELECT p1.pid, p2.pid FROM Precinct p1, Precinct p2, NeighborPrecinct np WHERE (p1.pid = np.precinct.pid) AND (p2.pid = np.nid"
				+	" AND p1.cd != p2.cd) AND (p1.sid = :stateId)").setParameter("stateId", stateId).getResultList();
		List<?> 			stateBorderPrecinctIds			=	(List<?>) em.createQuery(
				"SELECT p.pid FROM Precinct p, NeighborPrecinct np WHERE (p.pid = np.precinct.pid) AND (np.nid = :stateId)").
				setParameter("stateId", MAX_STATE_ID_INITIAL-stateId).getResultList();
	 
		List<Integer>	storedPrecinctIdList	=	new ArrayList<Integer>();
		handleDistrictBorderingPrecincts(districtBorderPrecinctIds, em, storedPrecinctIdList);
		handleStateBorderingPrecincts(stateBorderPrecinctIds, em, storedPrecinctIdList);
		em.close();
	}
	
	public void handleDistrictBorderingPrecincts(List<?> districtBorderPrecinctIds, EntityManager em, List<Integer> storedPrecinctIdList) 
			throws IOException{
		List<String>	storedBorderIdList		=	new ArrayList<String>();
		em.getTransaction().begin();
		for(int i = 0; i < districtBorderPrecinctIds.size(); i++){
			Precinct precinct = em.find(Precinct.class, (int)((Object[])districtBorderPrecinctIds.get(i))[0]);
			Precinct nPrecInNDistrict = em.find(Precinct.class, (int)((Object[])districtBorderPrecinctIds.get(i))[1]);
			District district = em.find(District.class, precinct.getCd());
			if(storedPrecinctIdList.indexOf(precinct.getPid()) == -1){
	            em.createNativeQuery("INSERT INTO BORDERING_PRECINCT (PID, DISTRICT_CD)"
	                    + " VALUES ( ?1, ?2)")
	                    .setParameter(1, precinct.getPid())
	                    .setParameter(2, district.getDId()).executeUpdate();
	            storedPrecinctIdList.add(precinct.getPid());
			}
			String storedBorderIds = Integer.toString(nPrecInNDistrict.getCd()) + " " + Integer.toString(district.getDId());
			if(storedBorderIdList.indexOf(storedBorderIds) == -1){
	            em.createNativeQuery("INSERT INTO NEIGHBOR_DISTRICT (NDISTRICTID, DISTRICT_CD, SID)"
	                    + " VALUES ( ?1, ?2, ?3)")
	                    .setParameter(1, nPrecInNDistrict.getCd())
	                    .setParameter(2, district.getDId())
	                    .setParameter(3, stateId).executeUpdate();
				storedBorderIdList.add(storedBorderIds);
			}
		}
		
		em.getTransaction().commit();
	}
	
	public void handleStateBorderingPrecincts(List<?> stateBorderPrecinctIds, EntityManager em, List<Integer> storedPrecinctIdList) 
			throws IOException{
		em.getTransaction().begin();
		for(int i = 0; i < stateBorderPrecinctIds.size(); i++){
			
			Precinct precinct = em.find(Precinct.class, (int)stateBorderPrecinctIds.get(i));
			District district = new District(precinct.getCd(), stateId);
			if(storedPrecinctIdList.indexOf(precinct.getPid()) == -1){
	            em.createNativeQuery("INSERT INTO BORDERING_PRECINCT (PID, DISTRICT_CD)"
	                    + " VALUES ( ?1, ?2)")
	                    .setParameter(1, precinct.getPid())
	                    .setParameter(2, district.getDId());
	            storedPrecinctIdList.add(precinct.getPid());
			}
		}
		em.getTransaction().commit();
	} 
	
	public int getStateId() {
		return stateId;
	}

	public void setStateId(int id) {
		this.stateId= id; 
	}
		
//	@OneToMany(mappedBy = "state", cascade = CascadeType.ALL)
//	public List<District> getDistrictList(){
//		System.out.println("Tried");
//		return districtList;
//	}

	 public List <District> initDistList() { 
		if(distList.size() == 0){
	        EntityManagerFactory emf = Persistence.createEntityManagerFactory("Eclipselink_JPA");
	        EntityManager em = emf.createEntityManager();
	        
	        List <?> distIdList = (List <?>) em.createNativeQuery(
	                "SELECT d.cd FROM DISTRICT d WHERE d.stateid = ?")
	            .setParameter(1, this.stateId)
	            .getResultList();
	        
	        
	        for (int i = 0; i < distIdList.size(); i++) {
	        	distList.add(new District((int) distIdList.get(i), stateId));
	        }
		}
        return distList;
    }
    
    public void setDistList(List<District> distList){
    	this.distList=distList;
    }
}