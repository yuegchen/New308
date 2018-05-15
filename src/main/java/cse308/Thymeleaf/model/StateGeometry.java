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
	private String type;
	
	public int getSid(){
		return sid;
	}
	
	public String getCoordinates(){
		return coordinates;
	}
	
	public String getType(){
		return type.toUpperCase(); 
	}
	
	public static Geometry getStateGeometry(StateGeometry stateGeometry, GeometryJSON geometryJson) throws IOException{
		Geometry geometry;
		if (Type.valueOf(stateGeometry.getType()).equals(Type.POLYGON)){
			geometry = geometryJson.readPolygon(new String("{\"type\":\"Polygon\", \"coordinates\":" + stateGeometry.getCoordinates() + "}"));
			
		}else{
			geometry = geometryJson.readMultiPolygon(new String("{\"type\":\"MultiPolygon\", \"coordinates\":" + stateGeometry.getCoordinates() + "}"));
		}
		return geometry;
	}
	
	
	public static boolean intersects(Geometry statePolygon, Geometry precinctPolygon){
		try{
			if(Type.valueOf(precinctPolygon.getGeometryType().toUpperCase()) == Type.MULTIPOLYGON &&
					Type.valueOf(statePolygon.getGeometryType().toUpperCase()) == Type.MULTIPOLYGON ){
				for(int i = 0; i < statePolygon.getNumGeometries(); i++){
					for(int j = 0; j < statePolygon.getNumGeometries(); j++)
						if(((Polygon)statePolygon.getGeometryN(i)).getExteriorRing().intersects(
								((Polygon)precinctPolygon.getGeometryN(j)).getExteriorRing())){
							return true;
					}
				}
				return false;
			}else if(Type.valueOf(precinctPolygon.getGeometryType().toUpperCase()) == Type.MULTIPOLYGON){
				for(int j = 0; j < precinctPolygon.getNumGeometries(); j++)
					if(((Polygon)statePolygon).getExteriorRing().intersects(
							((Polygon)precinctPolygon.getGeometryN(j)).getExteriorRing()))
						return true;
				return false;
			}else if(Type.valueOf(statePolygon.getGeometryType().toUpperCase()) == Type.MULTIPOLYGON){
				for(int i = 0; i < statePolygon.getNumGeometries(); i++){
					if(((Polygon)statePolygon.getGeometryN(i)).getExteriorRing().intersects(
							((Polygon)precinctPolygon).getExteriorRing()))
						return true;
				}
				return false;
			}
			return ((Polygon)statePolygon).getExteriorRing().intersects(((Polygon)precinctPolygon).getExteriorRing());
		}catch(Exception e){			
			return true;
		}
	}
}
