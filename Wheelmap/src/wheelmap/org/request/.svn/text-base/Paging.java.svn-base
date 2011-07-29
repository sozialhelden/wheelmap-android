package wheelmap.org.request;

public class Paging {
	public static final Paging DEFAULT_PAGING = new Paging(200, 1);
	public static final int MAX_NUMBER_OF_ITEMS_PER_PAGE=500;

	public final int numberOfItemsPerPage;
	public final int pageNumber;

	public Paging(int numberOfItemsPerPage) {
		this(numberOfItemsPerPage,1);
	}
	
	public Paging(int numberOfItemsPerPage, int pageNumber) {
		if (numberOfItemsPerPage>MAX_NUMBER_OF_ITEMS_PER_PAGE) {
			throw new IllegalArgumentException("numberOfItemsPerPage must be <="+MAX_NUMBER_OF_ITEMS_PER_PAGE);
		}
		this.numberOfItemsPerPage=numberOfItemsPerPage;
		this.pageNumber=pageNumber;
	}
	
	@Override
	public String toString() {
		return "Paging [numberOfItemsPerPage=" + numberOfItemsPerPage
				+ ", pageNumber=" + pageNumber + "]";
	}	
}
