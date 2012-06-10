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
package org.wheelmap.android.model;

import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.manager.SupportManager;
import org.wheelmap.android.manager.SupportManager.NodeType;
import org.wheelmap.android.ui.POIsListItemView;
import org.wheelmap.android.utils.GeocoordinatesMath;
import org.wheelmap.android.utils.GeocoordinatesMath.DistanceUnit;

import wheelmap.org.WheelchairState;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class POIsListCursorAdapter extends CursorAdapter {
	private final static String TAG = "poislist";
	private DistanceFormatter mDistanceFormatter;
		
	
	
	public POIsListCursorAdapter(Context context, Cursor cursor, boolean autorequery) {
		super( context, cursor, autorequery );
		if ( GeocoordinatesMath.DISTANCE_UNIT == DistanceUnit.KILOMETRES )
			mDistanceFormatter = new DistanceFormatterMetric();
		else
			mDistanceFormatter = new DistanceFormatterAnglo();
	}

	@Override
	public long getItemId(int position) {		
		return position;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		POIsListItemView  pliv = (POIsListItemView ) view;
		SupportManager manager = WheelmapApp.getSupportManager();
		if ( manager == null ) {
			Log.d( TAG, "SupportManager is null - how can that be?");
			return;
		}
		
		String name = POIHelper.getName(cursor);
		WheelchairState state = POIHelper.getWheelchair(cursor);
		int index = cursor.getColumnIndex( POIsCursorWrapper.LOCATION_COLUMN_NAME );
		double distance = cursor.getDouble( index );
		int categoryId = POIHelper.getCategoryId( cursor );
		int nodeTypeId = POIHelper.getNodeTypeId( cursor );
		NodeType nodeType = manager.lookupNodeType(nodeTypeId);

		if ( name.length() > 0 )
			pliv.setName( name );
		else {
			pliv.setName( nodeType.localizedName );
		}
		String category = manager.lookupCategory( categoryId ).localizedName;
		pliv.setCategory( category );
		pliv.setNodeType( nodeType.localizedName );

		pliv.setDistance( mDistanceFormatter.format( distance ));
		Drawable marker = manager.lookupWheelDrawable(state.getId());
		pliv.setIcon( marker );
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return new POIsListItemView(context);
	}
	
	private interface DistanceFormatter {
		String format( double distance );
	}
	
	private class DistanceFormatterMetric implements DistanceFormatter {
		@Override
		public String format(double distance) {
			if ( distance < 1.0 )
				return String.format( "%2.0f0m", distance * 100.0 );
			else
				return String.format( "%.1fkm", distance );
		}
	}
	
	private class DistanceFormatterAnglo implements DistanceFormatter {
		@Override
		public String format(double distance) {
			if ( distance < 1.0 )
				return String.format( "%0.2fmi", distance );
			else
				return String.format( "%.1fmi", distance );
		}	
	}
	
}
