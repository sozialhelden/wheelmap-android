package org.wheelmap.android.ui;

import android.os.Bundle;
import makemachine.android.formgenerator.FormActivity;

public class LoginActivity extends FormActivity {
	private final static String TAG = "poidetail";
	

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		generateForm( FormActivity.parseFileToString( this, "schema_login.json" ) );
		
	}

}
