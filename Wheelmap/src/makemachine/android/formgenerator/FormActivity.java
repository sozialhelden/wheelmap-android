package makemachine.android.formgenerator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.LinearLayout.LayoutParams;

/**
 * FormActivity allows you to create dynamic form layouts based upon a json schema file. 
 * This class should be sub-classed. 
 * 
 * @author Jeremy Brown
 */
public abstract class FormActivity extends Activity
{
	public static String SCHEMA_KEY_TYPE		= "type";
	public static String SCHEMA_KEY_BOOL 		= "boolean";
	public static String SCHEMA_KEY_INT  		= "integer";
	public static String SCHEMA_KEY_STRING 		= "string";
	public static String SCHEMA_KEY_PRIORITY	= "priority";
	public static String SCHEMA_KEY_TOGGLES		= "toggles";
	public static String SCHEMA_KEY_DEFAULT		= "default";
	public static String SCHEMA_KEY_MODIFIERS	= "modifiers";
	public static String SCHEMA_KEY_OPTIONS		= "options";
	public static String SCHEMA_KEY_META		= "meta";
	public static String SCHEMA_KEY_HINT		= "hint";
	
	public static final LayoutParams defaultLayoutParams = new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	
	// -- data
	protected Map<String, FormWidget> _map;
	protected ArrayList<FormWidget> _widgets;

	// -- widgets
	protected LinearLayout _container;
	protected LinearLayout _layout;
	protected ScrollView   _viewport;
	
	// -----------------------------------------------
	//
	// parse data and build view
	//
	// -----------------------------------------------
	
	/**
	 * parses a supplied schema of raw json data and creates widgets
	 * @param data - the raw json data as a String
	 */
	public void generateForm( String data )
	{
		_widgets = new ArrayList<FormWidget>();
		_map = new HashMap<String, FormWidget>();

		try
		{
			String name;
			FormWidget widget;
			JSONObject property;
			JSONObject schema = new JSONObject( data ); 
			JSONArray names = schema.names();
			
			for( int i= 0; i < names.length(); i++ ) 
			{
				name = names.getString( i );
				
				if( name.equals( SCHEMA_KEY_META )  ) continue;
				
				property = schema.getJSONObject( name );	
				
				boolean toggles  = hasToggles( property );
				String defaultValue   = getDefault( property );
				int priority = property.getInt( FormActivity.SCHEMA_KEY_PRIORITY );
				
				widget = getWidget( name, property );
				if( widget == null) continue;
				
				widget.setPriority( priority );
				widget.setValue( defaultValue );
				
				if( toggles ){
					widget.setToggles( processToggles( property ) );
					widget.setToggleHandler( new FormActivity.FormWidgetToggleHandler() );
				}
				
				if( property.has(FormActivity.SCHEMA_KEY_HINT)) widget.setHint( property.getString( FormActivity.SCHEMA_KEY_HINT ) );
				
				_widgets.add( widget );
				_map.put( name, widget );
			}
		} catch( JSONException e ) {
			Log.i( "MakeMachine", e.getMessage() );
		}
		
		// -- sort widgets on priority
		Collections.sort( _widgets, new PriorityComparison() );	
		
		// -- create the layout
        _container = new LinearLayout( this );
        _container.setOrientation( LinearLayout.VERTICAL );
        _container.setLayoutParams( FormActivity.defaultLayoutParams );
        
        _viewport  = new ScrollView( this );
        _viewport.setLayoutParams( FormActivity.defaultLayoutParams );
        
        _layout = new LinearLayout( this );
        _layout.setOrientation( LinearLayout.VERTICAL );
        _layout.setLayoutParams( FormActivity.defaultLayoutParams );
        
        initToggles();
        
        for( int i = 0; i < _widgets.size(); i++ ) {
        	_layout.addView( ( View ) _widgets.get(i).getView() );
        }
        
        _viewport.addView( _layout );
        _container.addView( _viewport );
        
		setContentView( _container );
	}
	
	// -----------------------------------------------
	//
	// populate and save
	//
	// -----------------------------------------------
	
	/**
	 * this method fills the form with existing data
	 * get the json string stored in the record we are editing
	 * create a json object ( if this fails then we know there is now existing record )
	 * create a list of property names from the json object
	 * loop through the map returned by the Form class that maps widgets to property names
	 * if the map contains the property name as a key that means there is a widget to populate w/ a value
	 */
	protected void populate( String jsonString )
	{
		try
		{	
			String prop;
			FormWidget widget;
			JSONObject data = new JSONObject( jsonString );
			JSONArray properties = data.names();
			
			for( int i = 0; i < properties.length(); i ++ )
			{
				prop = properties.getString( i );
				if( _map.containsKey(prop) )  {
					widget = _map.get( prop );
					widget.setValue( data.getString(prop) );
				}
			}
		} catch ( JSONException e ) {
			
		}
	}
	
	/**
	 * this method preps the data and saves it
	 * if there is a problem w/ creating the json string, the method fails
	 * loop through each widget and set a property on a json object to the value of the widget's getValue() method
	 */
	protected JSONObject save()
	{
		FormWidget widget;
		JSONObject data = new JSONObject();
		
		boolean success = true;
		
		try{
			for( int i = 0; i < _widgets.size(); i++ ) 
			{
	        	widget = _widgets.get(i);
	        	data.put( widget.getPropertyName(), widget.getValue() );
			}
		} catch( JSONException e )
		{
			success = false;
			Log.i( "MakeMachine", "Save error - " + e.getMessage() );
			return null;
		}
		
		if( success ) {
			Log.i( "MakeMachine", "Save success " + data.toString() );
			return data;
		}
		return null;
	}
	
	// -----------------------------------------------
	//
	// toggles
	//
	// -----------------------------------------------
	
	/**
	 * creates the map a map of values for visibility and references to the widgets the value affects
	 */
	protected HashMap<String, ArrayList<String>> processToggles( JSONObject property )
	{
		try{
			ArrayList<String> toggled;
			HashMap<String, ArrayList<String>> toggleMap = new HashMap<String, ArrayList<String>>();
			
			JSONObject toggleList = property.getJSONObject( FormActivity.SCHEMA_KEY_TOGGLES );
			JSONArray toggleNames = toggleList.names();
			
			for( int j = 0; j < toggleNames.length(); j++ )
			{
				String toggleName = toggleNames.getString(j);
				JSONArray toggleValues = toggleList.getJSONArray( toggleName );
				toggled = new ArrayList<String>();
				toggleMap.put( toggleName, toggled );
				for( int k = 0; k < toggleValues.length(); k++ ) {
					toggled.add( toggleValues.getString(k) );
				}
			}
			
			return toggleMap;
			
		} catch( JSONException e ){
			return null;
		}
	}
	
	/**
	 * returns a boolean indicating that the supplied json object contains a property for toggles
	 */
	protected boolean hasToggles( JSONObject obj ){
		try{
			obj.getJSONObject( FormActivity.SCHEMA_KEY_TOGGLES );
			return true;
		} catch ( JSONException e ){
			return false;
		}
	}
	
	/**
	 * initializes the visibility of widgets that are togglable 
	 */
	protected void initToggles()
	{
		int i;
		FormWidget widget;
		
		for( i = 0; i < _widgets.size(); i++ )  {
			widget = _widgets.get(i);
			updateToggles( widget );
		}
	}
	
	/**
	 * updates any widgets that need to be toggled on or off
	 * @param widget
	 */
	protected void updateToggles( FormWidget widget ) 
	{
		int i;
		String name;
		ArrayList<String> toggles;
		ArrayList<FormWidget> ignore = new ArrayList<FormWidget>();
		
		toggles = widget.getToggledOn();
		for( i = 0; i < toggles.size(); i++ ) 
		{
			name = toggles.get(i);
			if( _map.get(name) != null ) 
			{
				FormWidget toggle = _map.get(name);
				ignore.add( toggle );
				toggle.setVisibility( View.VISIBLE );
			}
		}
		
		toggles = widget.getToggledOff();
		for( i = 0; i < toggles.size(); i++ ) 
		{
			name = toggles.get(i);
			if( _map.get(name) != null ) 
			{
				FormWidget toggle = _map.get(name);
				if( ignore.contains(toggle) ) continue;
				toggle.setVisibility( View.GONE );
			}
		}
	}
	
	/**
	 * simple callbacks for widgets to use when their values have changed
	 */
	class FormWidgetToggleHandler
	{
		public void toggle( FormWidget widget ) {
			updateToggles( widget );
		}
	}
	
	// -----------------------------------------------
	//
	// utils
	//
	// -----------------------------------------------
	
	protected String getDefault( JSONObject obj ){
		try{
			return obj.getString( FormActivity.SCHEMA_KEY_DEFAULT );
		} catch ( JSONException e ){
			return null;
		}
	}
	
	/**
	 * helper class for sorting widgets based on priority
	 */
	class PriorityComparison implements Comparator<FormWidget>
	{
		public int compare( FormWidget item1, FormWidget item2 ) {
			return item1.getPriority() > item2.getPriority() ? 1 : -1;
		}
	}
	
	/**
	 * factory method for actually instantiating widgets
	 */
	protected FormWidget getWidget( String name, JSONObject property ) 
	{
		try
		{
			String type = property.getString( FormActivity.SCHEMA_KEY_TYPE );
			
			if( type.equals( FormActivity.SCHEMA_KEY_STRING ) ){
				return new FormEditText( this, name  );
			}
			
			if( type.equals( FormActivity.SCHEMA_KEY_BOOL ) ){
				return new FormCheckBox( this, name );
			}
			
			if( type.equals(  FormActivity.SCHEMA_KEY_INT ) )
			{	
				if( property.has( FormActivity.SCHEMA_KEY_OPTIONS ) ) 
				{
					JSONObject options = property.getJSONObject( FormActivity.SCHEMA_KEY_OPTIONS );
					return new FormSpinner(  this, name, options );
				}else{
					return new FormNumericEditText( this, name );
				}
			}
		} catch( JSONException e ) {
			return null;
		}
		return null;
	}
	
	public static String parseFileToString( Context context, String filename )
	{
		try
		{
			InputStream stream = context.getAssets().open( filename );
			int size = stream.available();
			
			byte[] bytes = new byte[size];
			stream.read(bytes);
			stream.close();
			
			return new String( bytes );
			
		} catch ( IOException e ) {
			Log.i("MakeMachine", "IOException: " + e.getMessage() );
		}
		return null;
	}
}


