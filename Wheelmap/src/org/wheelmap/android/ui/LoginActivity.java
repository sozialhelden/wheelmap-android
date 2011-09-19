package org.wheelmap.android.ui;

import makemachine.android.formgenerator.FormActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.wheelmap.android.model.UserCredentials;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class LoginActivity extends FormActivity {
	private final static String TAG = "poidetail";
	
	public static final int OPTION_LOGIN = 0;
	public static final int OPTION_LOGOUT = 1;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		generateForm( FormActivity.parseFileToString( this, "schema_login.json" ) );

		try {
			load();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	protected void onStop(){
		super.onStop();

		saveChanges();     

	}
	

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) 
	{
		menu.add( 0, OPTION_LOGIN, 0, "Save" );
		menu.add( 0, OPTION_LOGOUT, 0, "Cancel" );
		return true;
	}

	@Override
	public boolean onMenuItemSelected( int id, MenuItem item )
	{

		switch( item.getItemId() )
		{
		case OPTION_LOGIN:
			saveChanges();
			break;
		case OPTION_LOGOUT:
			UserCredentials userCredentials = new UserCredentials(this);
			userCredentials.logout();			
			finish();					
			break;
		}

		return super.onMenuItemSelected( id, item );
	}
	
	private void saveChanges() {
		JSONObject jo;
		jo = save();
		
		UserCredentials userCredentials = new UserCredentials(this);
		String login;
		String password;
		try {				
			login = jo.getString( "login" );
			password = jo.getString( "password" );
			userCredentials.login(login, password);
			
		} catch (JSONException e) {
			Log.v( TAG, "Error with makemachine" + e.getMessage());
		}
	}

	private void load() throws JSONException {

		JSONObject jo = new JSONObject();

		UserCredentials userCredentials = new UserCredentials(this);


		// get user credentials form LoginManager
		String login = userCredentials.getLogin();
		String password = userCredentials.getPassword();
		boolean loggedin = userCredentials.isLoggedIn();

		jo.put("login",login);
		jo.put("password",password);	
		jo.put("logged", loggedin);
	}
}
