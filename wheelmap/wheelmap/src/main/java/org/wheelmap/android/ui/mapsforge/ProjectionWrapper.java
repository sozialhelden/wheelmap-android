/*
 * #%L
 * Wheelmap - App
 * %%
 * Copyright (C) 2011 - 2012 Michal Harakal - Michael Kroez - Sozialhelden e.V.
 * %%
 * Wheelmap App based on the Wheelmap Service by Sozialhelden e.V.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
