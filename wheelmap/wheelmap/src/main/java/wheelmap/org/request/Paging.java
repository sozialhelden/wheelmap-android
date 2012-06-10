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
package wheelmap.org.request;

public class Paging {
	public static final Paging DEFAULT_PAGING = new Paging(200, 1);
	public static final int MAX_NUMBER_OF_ITEMS_PER_PAGE=500;

	public final int numberOfItemsPerPage;
	public int pageNumber;

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
	
	public void setPage( int pageNumber ) {
		if ( pageNumber < 1 )
			throw new IllegalArgumentException( "pageNumber must be greater than zero" );
		this.pageNumber = pageNumber;
	}
	
	@Override
	public String toString() {
		return "Paging [numberOfItemsPerPage=" + numberOfItemsPerPage
				+ ", pageNumber=" + pageNumber + "]";
	}	
}
