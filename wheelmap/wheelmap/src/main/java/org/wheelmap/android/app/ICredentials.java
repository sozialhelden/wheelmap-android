package org.wheelmap.android.app;


/**
 * Interface for API credentials 
 */
public interface ICredentials {
	
	/**
	 * gets API_key for accessing REST API. If user logged in, returning his privat API_KEY,
	 * otherwisou anonymous API_KEY allowing only change the wheelchar state 
	 * 
	 * @return API_KEY for accessing REST API of wheelmap
	 */
	public String getApiKey();
	
	
	
	/**
	 * gets Name of user that is logged in
	 * 
	 * @return user's name as {@link String}
	 */
	public String getUserName();
	
	
	
	/**
	 * @return 	true if user was succesfully authentificated on wheelmap server 
	 */
	public boolean isLoggedIn();
	
	/**
	 * delete saved credentials for cuerrent user
	 */
	public void logout();
	
	/**
	 * login a new user and save new data into local storage. 	

	 * @param ApiKey
	 * @param email
	 */
	public void save(final String ApiKey, final String email);
}
