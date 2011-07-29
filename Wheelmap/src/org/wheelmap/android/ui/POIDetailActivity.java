package org.wheelmap.android.ui;

import android.os.Bundle;
import makemachine.android.formgenerator.FormActivity;

public class POIDetailActivity extends FormActivity {
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        generateForm( FormActivity.parseFileToString( this, "schema_poi.json" ) );
        save();
    }

}
