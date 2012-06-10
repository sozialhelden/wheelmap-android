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
package org.wheelmap.android.utils;

import org.mapsforge.android.maps.GeoPoint;


public class MapUtils {
		
	public static boolean NearPonits(GeoPoint p1, GeoPoint p2, int lngSpan, int latSpan) {
		int lonDistance = Math.abs(p1.getLongitudeE6() - p2.getLongitudeE6());
		int latDistance = Math.abs(p1.getLatitudeE6() - p2.getLatitudeE6());
		double latPer = (double)latDistance / latSpan;
		double lonPer = (double)lonDistance / lngSpan;
		
		
		// distance between points is less 10% of screen span 	
		return (latPer < 0.1) && (lonPer < 0.1);
	}
		
}
