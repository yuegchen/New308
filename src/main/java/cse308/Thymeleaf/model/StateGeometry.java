package cse308.Thymeleaf.model;

import java.io.IOException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.geotools.geojson.geom.GeometryJSON;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import cse308.Thymeleaf.model.Type;

@Entity
@Table(name = "STATE_GEOMETRY")
public class StateGeometry {
	
	@Id
	private int sid;
	
	private String coordinates;
	
	public int getSid(){
		return sid;
	}
	
	public String getCoordinates(){
		return coordinates;
	}
	
	public static Polygon getStateGeometry(StateGeometry stateGeometry, GeometryJSON geometryJson) throws IOException{
		return geometryJson.readPolygon(new String("{\"type\":\"Polygon\", \"coordinates\":" + stateGeometry.getCoordinates() + "}"));
	}
	
	
	public static boolean intersects(Polygon statePolygon, Geometry precinctPolygon){
		try{
			if(Type.valueOf(precinctPolygon.getGeometryType().toUpperCase()) == Type.MULTIPOLYGON){
				for(int i = 0; i < precinctPolygon.getNumGeometries(); i++){
					if(statePolygon.getExteriorRing().intersects(((Polygon)precinctPolygon.getGeometryN(i)).getExteriorRing())){
						return true;
					}
				}
				return false;
			}
			return statePolygon.getExteriorRing().intersects(((Polygon)precinctPolygon).getExteriorRing());
		}catch(Exception e){			
			return true;
		}
	}
}
