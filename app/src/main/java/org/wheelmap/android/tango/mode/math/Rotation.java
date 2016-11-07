package org.wheelmap.android.tango.mode.math;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * @author julien Gaffuri
 *	https://sourceforge.net/p/opencarto/code/HEAD/tree/trunk/server/src/main/java/org/opencarto/algo/base/Rotation.java
 */
class Rotation {

	public static Coordinate get(Coordinate c, Coordinate center, double angle){
		double cos = Math.cos(angle), sin = Math.sin(angle);
		double x = c.x, y = c.y;
		double xc = center.x, yc = center.y;
		return new Coordinate(xc+cos*(x-xc)-sin*(y-yc), yc+sin*(x-xc)+cos*(y-yc));
	}

	public static Coordinate[] get(Coordinate[] coord, Coordinate center, double angle){
		Coordinate[] coord_= new Coordinate[coord.length];
		double cos = Math.cos(angle), sin = Math.sin(angle);
		double xc = center.x, yc = center.y;
		Coordinate ci;
		double x, y;
		for(int i=0; i<coord.length; i++) {
			ci = coord[i];
			x = ci.x;
			y = ci.y;
			coord_[i] = new Coordinate(xc+cos*(x-xc)-sin*(y-yc), yc+sin*(x-xc)+cos*(y-yc));
		}
		return coord_;
	}


	public static Point get(Point geom, Coordinate center, double angle, GeometryFactory gf) {
		return gf.createPoint( get(geom.getCoordinate(), center, angle) );
	}

	public static LineString get(LineString ls, Coordinate center, double angle, GeometryFactory gf) {
		return gf.createLineString(get(ls.getCoordinates(), center, angle));
	}

	public static LinearRing get(LinearRing lr, Coordinate center, double angle, GeometryFactory gf) {
		return gf.createLinearRing(get(lr.getCoordinates(), center, angle));
	}

	public static Polygon get(Polygon geom, Coordinate c, double angle, GeometryFactory gf) {
		LinearRing lr = get((LinearRing)geom.getExteriorRing(), c, angle, gf);
		LinearRing[] lr_ = new LinearRing[geom.getNumInteriorRing()];
		for(int j=0; j<geom.getNumInteriorRing(); j++) lr_[j] = get((LinearRing)geom.getInteriorRingN(j), c, angle, gf);
		return gf.createPolygon(lr, lr_);
	}

	public static GeometryCollection get(GeometryCollection geomCol, Coordinate center, double angle, GeometryFactory gf) {
		Geometry[] gs = new Geometry[geomCol.getNumGeometries()];
		for(int i=0; i< geomCol.getNumGeometries(); i++) gs[i] = get(geomCol.getGeometryN(i), center, angle, gf);
		return gf.createGeometryCollection(gs);
	}


	public static Geometry get(Geometry geom, Coordinate center, double angle, GeometryFactory gf) {
		if(geom instanceof Point) return get((Point)geom, center, angle, gf);
		else if(geom instanceof Polygon) return get((Polygon)geom, center, angle, gf);
		else if(geom instanceof LineString) return get((LineString)geom, center, angle, gf);
		else if(geom instanceof LinearRing) return get((LinearRing)geom, center, angle, gf);
		return null;
	}
}