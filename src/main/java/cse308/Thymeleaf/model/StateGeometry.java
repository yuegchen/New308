package cse308.Thymeleaf.model;

import java.io.IOException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.geotools.geojson.geom.GeometryJSON;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

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
		double precisionLevel = 10000;
		boolean isTopoExceptioned;
		do{
			try{
				if(Type.valueOf(precinctPolygon.getGeometryType().toUpperCase()) == Type.MULTIPOLYGON &&
						Type.valueOf(statePolygon.getGeometryType().toUpperCase()) == Type.MULTIPOLYGON ){
					for(int i = 0; i < statePolygon.getNumGeometries(); i++){
						for(int j = 0; j < precinctPolygon.getNumGeometries(); j++)
							if(GeometryPrecisionReducer.reduce(((Polygon)statePolygon.getGeometryN(i)).getExteriorRing(), 
									new PrecisionModel(precisionLevel)).intersects(
											GeometryPrecisionReducer.reduce(((Polygon)precinctPolygon.getGeometryN(j)).getExteriorRing(), 
													new PrecisionModel(precisionLevel)))){
								return true;
						}
					}
					return false;
				}else if(Type.valueOf(precinctPolygon.getGeometryType().toUpperCase()) == Type.MULTIPOLYGON){
					for(int j = 0; j < precinctPolygon.getNumGeometries(); j++)
						if(GeometryPrecisionReducer.reduce(((Polygon)statePolygon).getExteriorRing(), new PrecisionModel(precisionLevel))
								.intersects(GeometryPrecisionReducer.reduce(
								((Polygon)precinctPolygon.getGeometryN(j)).getExteriorRing(), new PrecisionModel(precisionLevel))))
							return true;
					return false;
				}else if(Type.valueOf(statePolygon.getGeometryType().toUpperCase()) == Type.MULTIPOLYGON){
					for(int i = 0; i < statePolygon.getNumGeometries(); i++){
						if(GeometryPrecisionReducer.reduce(((Polygon)statePolygon.getGeometryN(i)).getExteriorRing(),
								new PrecisionModel(precisionLevel)).intersects(
										GeometryPrecisionReducer.reduce(((Polygon)precinctPolygon).getExteriorRing(), 
												new PrecisionModel(precisionLevel))))
							return true;
					}
					return false;
				}
				return GeometryPrecisionReducer.reduce(((Polygon)statePolygon).getExteriorRing(), new PrecisionModel(precisionLevel))
						.intersects(GeometryPrecisionReducer.reduce(((Polygon)precinctPolygon).getExteriorRing(),
								new PrecisionModel(precisionLevel)));
			}catch(TopologyException e){			
				isTopoExceptioned = true;
				precisionLevel /= 10;
			}
		}while(isTopoExceptioned);
		return false;
	}
}
