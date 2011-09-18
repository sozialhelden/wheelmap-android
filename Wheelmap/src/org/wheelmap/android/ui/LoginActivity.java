package org.wheelmap.android.ui;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.Bundle;
import makemachine.android.formgenerator.FormActivity;

public class LoginActivity extends FormActivity {
	private final static String TAG = "poidetail";


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

	private void load() throws JSONException {

		JSONObject jo = new JSONObject();

		// get user credentials form LoginManager
		String login="user@wheelmap.org";
		String password="";

		jo.put("login",login);
		jo.put("password",password);		
	}
}
