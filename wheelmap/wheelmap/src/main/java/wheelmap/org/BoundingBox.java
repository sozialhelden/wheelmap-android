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
package wheelmap.org;

//import org.apache.commons.lang.Validate;

public class BoundingBox {
	private final Wgs84GeoCoordinates westSouth;
	private final Wgs84GeoCoordinates eastNorth;

	public BoundingBox(final Wgs84GeoCoordinates westSouth, final Wgs84GeoCoordinates eastNorth) {
		//Validate.notNull(westSouth,"");
		//Validate.notNull(eastNorth, "eastNorth");
		this.westSouth = westSouth;
		this.eastNorth = eastNorth;
	}
	
	public  Wgs84GeoCoordinates getWestSouth()
	{
		return westSouth;
	}
	
	public  Wgs84GeoCoordinates getEastNorth()
	{
		return eastNorth;
	}
	
	public String asRequestParameter() {
		return westSouth.asRequestParameter() + "," + eastNorth.asRequestParameter();
	}	
	
	@Override
	public String toString() {
		return "BoundingBox [westSouth=" + westSouth + ", eastNorth="
				+ eastNorth + "]";
	}
	
	public static class Wgs84GeoCoordinates {

		public final double longitude;
		public final double latitude;
		
		public Wgs84GeoCoordinates (double longitude,double latitude) {
			this.longitude = longitude;
			this.latitude = latitude;
		}

		public String asRequestParameter() {
			return longitude + "," + latitude;
		}
		
		@Override
		public String toString() {
			return "Wgs84GeoCoordinates [longitude=" + longitude
			+ ", latitude=" + latitude + "]";
		}
	}
}
