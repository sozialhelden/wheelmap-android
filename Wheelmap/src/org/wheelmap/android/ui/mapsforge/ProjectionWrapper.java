package org.wheelmap.android.ui.mapsforge;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.Projection;

import android.graphics.Point;

public class ProjectionWrapper implements Projection {
	private Projection mProjection;
	
	public ProjectionWrapper( Projection projection ) {
		mProjection = projection;
	}

	@Override
	public GeoPoint fromPixels(int x, int y) {
		int desiredX = x + 20;
		int desiredY = x + 40;
		return mProjection.fromPixels( desiredX, desiredY );
	}

	@Override
	public float metersToPixels(float meters) {
		return mProjection.metersToPixels( meters );
	}

	@Override
	public float metersToPixels(float meters, byte zoom) {
		return mProjection.metersToPixels( meters, zoom);
	}

	@Override
	public Point toPixels(GeoPoint in, Point out) {
		Point outPoint = new Point();
		Point point = mProjection.toPixels( in, outPoint );
		if ( point == null )
			return null;
		
		Point desiredPoint = new Point();
		desiredPoint.x = outPoint.x - 20;
		desiredPoint.y = outPoint.y - 40;
		
		if ( out != null ) {
			out.x = desiredPoint.x;
			out.y = desiredPoint.y;
		}
		
		return desiredPoint;
	}

	@Override
	public Point toPoint(GeoPoint in, Point out, byte zoom) {
		return mProjection.toPoint( in, out, zoom);
	}
}
