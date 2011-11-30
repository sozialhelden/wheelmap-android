package org.wheelmap.android.test;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriUtils;
import org.wheelmap.android.service.SyncServiceException;


import wheelmap.org.domain.apikey.AuthInfo;
import wheelmap.org.domain.apikey.User;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.ApiKeyRequestBuilder;
import wheelmap.org.request.RequestProcessor;
import android.test.AndroidTestCase;
import android.util.Log;

public class LoginTest extends AndroidTestCase {
	private final static String TAG = "logintest";	
	private String server = "staging.wheelmap.org";
	
	private int statusAuthFailed = 400;
	private int statusOSMFailed = 403;
	
	public void testLoginTest() throws URISyntaxException {
		
		ApiKeyRequestBuilder rb = new ApiKeyRequestBuilder(server, AcceptType.JSON );
		// rb.setCredentials( "olfila@gmx.net", "testtest" ); // correct
		rb.setCredentials( "olfila@gmx.ne", "testtest" ); // wrong
		rb.setCredentials( "rutton@web.de", "testtest" ); // no osm connection
		
		String request;
		try {
			request = UriUtils.encodeQuery(rb.buildRequestUri(),
					"utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new SyncServiceException( SyncServiceException.ERROR_INTERNAL_ERROR, e);
		}
		
		Log.d( TAG, "Request = " + request );

		RequestProcessor rp = new RequestProcessor();
		AuthInfo authInfo = null;
		try {
			authInfo = rp.post( new URI(request), null, AuthInfo.class );
		} catch ( HttpClientErrorException e ) {
			HttpStatus status = e.getStatusCode();
			if ( status.value() == statusAuthFailed )
				Log.d( TAG, "wrong email or password");
			else if (status.value() == statusOSMFailed )
				Log.d( TAG, "not osm connected" );
		}
				
		if ( authInfo != null )		
			Log.d( TAG, "User: olfila@gmx.net " + " api_key = " + authInfo.getUser().getApiKey());
		
	}
}
