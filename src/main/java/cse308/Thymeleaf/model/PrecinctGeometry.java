package cse308.Thymeleaf.model;

import java.io.IOException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.geotools.geojson.geom.GeometryJSON;

import com.vividsolutions.jts.geom.Geometry;

import cse308.Thymeleaf.model.Type;

@Entity
@Table(name = "precinct_geometry")
public class PrecinctGeometry {
	
	@Id
	private int pid;
	
	private String type;
	private String coordinates;
	
	public int getPid(){
		return pid;
	}
	
	public String getType(){
		return type.toUpperCase();
	}
	
	public String getCoordinates(){
		return coordinates;
	}
	
	public static Geometry getPrecinctGeometries(PrecinctGeometry precinctGeometry, GeometryJSON geometryJson) throws IOException{
		Geometry geometry;
		if (Type.valueOf(precinctGeometry.getType()).equals(Type.POLYGON)){
			geometry = geometryJson.readPolygon(new String("{\"type\":\"Polygon\", \"coordinates\":" + precinctGeometry.getCoordinates() + "}"));
			
		}else{
			geometry = geometryJson.readMultiPolygon(new String("{\"type\":\"MultiPolygon\", \"coordinates\":" + precinctGeometry.getCoordinates() + "}"));
		}
		return geometry;
	}
	
	public static boolean isNeighborPrecincts(Geometry geometry1, Geometry geometry2){
		try{
			return geometry1.intersects(geometry2);
		}catch(Exception e){
			return true;
		}
	}
}
