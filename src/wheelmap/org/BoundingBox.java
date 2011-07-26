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
