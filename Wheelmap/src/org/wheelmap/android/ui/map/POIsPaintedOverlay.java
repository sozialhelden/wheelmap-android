package org.wheelmap.android.ui.map;

import java.util.ArrayList;

import org.wheelmap.android.R;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.utils.MapUtils;

import wheelmap.org.WheelchairState;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class POIsPaintedOverlay extends Overlay {
	private ArrayList<POIMapItem> mPoisLocations;
	private Cursor mPois;
	private Context mContext;
	private Bitmap bUnknown;
	private Bitmap bYes;
	private Bitmap bNo;
	private Bitmap bLimited;



	public POIsPaintedOverlay(Context context, Cursor cursor) {
		super();
		mContext = context;
		mPois = cursor;

		bUnknown = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.marker_unknown);
		bYes = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.marker_yes);
		bNo = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.marker_no);
		bLimited = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.marker_limited);

		mPoisLocations = new ArrayList<POIMapItem>();
		refreshLocations();
		mPois.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				refreshLocations();
			}
		});
	}

	private void refreshLocations() {
		int stateColumn = mPois.getColumnIndex(Wheelmap.POIs.WHEELCHAIR); 
		int latColumn = mPois.getColumnIndex(Wheelmap.POIs.COORD_LAT);
		int lonColumn = mPois.getColumnIndex(Wheelmap.POIs.COORD_LON);
		int idColumn = mPois.getColumnIndex(Wheelmap.POIs._ID);

		if (mPois.moveToFirst())
			do { 
				Double lat = mPois.getDouble(latColumn);
				Double lng = mPois.getDouble(lonColumn);
				WheelchairState state = WheelchairState.valueOf(mPois.getInt(stateColumn));
				int poiId = mPois.getInt(idColumn); 

				POIMapItem geoPoint = new POIMapItem(new GeoPoint(lat.intValue(), lng.intValue()), state, poiId);
				mPoisLocations.add(geoPoint);
			} while(mPois.moveToNext());
	}



	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();

		// Create and setup your paint brush
		Paint paint = new Paint();

		paint.setAntiAlias(true);
		paint.setFakeBoldText(true);

		if (shadow == false) {
			for (POIMapItem poi : mPoisLocations) {

				Point myPoint = new Point();
				projection.toPixels(poi.getPoint(), myPoint);

				// color depending on Wheelchairstate
				// TODO combine Wheelchair state and category into one image
				switch (poi.getWheelchairState()) {
				case UNKNOWN: {
					canvas.drawBitmap(bUnknown, myPoint.x - bUnknown.getWidth(), myPoint.y - bUnknown.getHeight(), paint);
					break;
				}
				case YES:
					canvas.drawBitmap(bYes, myPoint.x - bYes.getWidth(), myPoint.y - bYes.getHeight(), paint);
					break;
				case LIMITED:
					canvas.drawBitmap(bLimited, myPoint.x - bLimited.getWidth(), myPoint.y - bLimited.getHeight(), paint);
					break;
				case NO:
					canvas.drawBitmap(bNo, myPoint.x - bNo.getWidth(), myPoint.y - bNo.getHeight(), paint);
					break;
				default:
					canvas.drawBitmap(bUnknown, myPoint.x - bUnknown.getWidth(), myPoint.y - bUnknown.getHeight(), paint);
					break;
				}
			}
		}
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		for (POIMapItem poi : mPoisLocations) {
			GeoPoint poiLocation = poi.getPoint(); 
			if (MapUtils.NearPonits(poiLocation, p, mapView.getLongitudeSpan(), mapView.getLatitudeSpan())) {
				
				
			

			
				
				Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI, Integer.toString(poi.getId()));

				// Then query for this specific record:
				Cursor cur = mContext.getContentResolver().query(poiUri, null, null, null, null);
				
				
				POIHelper poi_helper = new POIHelper();


				if (cur.moveToFirst()) {		
					Log.d("POI id",Integer.toBinaryString(poi.getId()) + poi_helper.getName(cur) + ' ' + poi_helper.getAddress(cur));

					Toast.makeText(mContext,
							poi_helper.getName(cur) + ' ' + poi_helper.getAddress(cur),
							Toast.LENGTH_SHORT).show();
				}
				cur.close();
				return true;
				
			}
		}
		return super.onTap(p, mapView);
	}
	
	

}
