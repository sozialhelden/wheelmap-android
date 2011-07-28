package wheelmap.org.request;

public enum AcceptType {
	XML, JSON;

	public String asRequestParameter() {
		return this.name().toLowerCase();
	}
}
