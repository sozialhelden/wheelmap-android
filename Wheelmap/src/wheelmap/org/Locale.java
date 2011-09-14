package wheelmap.org;

public class Locale {
	public final static Locale DEFAULT_LOCALE = new Locale("de");
	
	private String value;
	
	public Locale( String value ) {
		this.value = value;
	}

	public String asRequestParameter() {
		return value;
	}
}
