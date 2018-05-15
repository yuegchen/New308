package cse308.Thymeleaf;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class PolygonTest {
	public static void main(String [] args){
		GeometryFactory geometryFactory = new GeometryFactory();
		Coordinate[] coordinates = new Coordinate[5];
		coordinates[0] = new Coordinate(0,0);
		coordinates[1] = new Coordinate(0,1);
		coordinates[2] = new Coordinate(1,1);
		coordinates[3] = new Coordinate(1,0);
		coordinates[4] = new Coordinate(0,0);
		Polygon polygonFromCoordinates = geometryFactory.createPolygon(coordinates);
		System.out.println(polygonFromCoordinates.getArea());
		System.out.println(polygonFromCoordinates.getLength());
	}
}
