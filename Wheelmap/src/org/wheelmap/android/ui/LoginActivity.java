package org.wheelmap.android.ui;

import org.wheelmap.android.model.UserCredentials;

import android.app.Activity;
import android.os.Bundle;

public class LoginActivity extends Activity {
	private final static String TAG = "poidetail";
	

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	
	//		load();
	}

	@Override
	protected void onStop(){
		super.onStop();

		saveChanges();     

	}

	private void saveChanges() {
		UserCredentials userCredentials = new UserCredentials(this);
		String login;
		String password;
	/*		login = jo.getString( "login" );
			password = jo.getString( "password" );
			*/
		//	userCredentials.login(login, password);
			
		
	}

	private void load(){
		UserCredentials userCredentials = new UserCredentials(this);

		// get user credentials form LoginManager
		String login = userCredentials.getLogin();
		String password = userCredentials.getPassword();
		boolean loggedin = userCredentials.isLoggedIn();

/*		jo.put("login",login);
		jo.put("password",password);	
		jo.put("logged", loggedin);
		*/
	}
}
