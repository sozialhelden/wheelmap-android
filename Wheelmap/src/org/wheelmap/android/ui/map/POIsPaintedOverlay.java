package org.wheelmap.android.ui.map;

import java.util.ArrayList;

import org.wheelmap.android.model.Wheelmap;

import wheelmap.org.WheelchairState;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class POIsPaintedOverlay extends Overlay {
	//private ArrayList<BitmapDrawable> mBitmaps = new ArrayList<BitmapDrawable>();
	private ArrayList<POIMapItem> mPoisLocations;
	private Cursor mPois;
	private int rad = 5;



	public POIsPaintedOverlay(Cursor cursor) {
		super();
		mPois = cursor;

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

		if (mPois.moveToFirst())
			do { 
				Double lat = mPois.getDouble(latColumn);
				Double lng = mPois.getDouble(lonColumn);
				WheelchairState state = WheelchairState.valueOf(mPois.getInt(stateColumn));

				POIMapItem geoPoint = new POIMapItem(new GeoPoint(lat.intValue(), lng.intValue()), state);
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

				RectF oval = new RectF(myPoint.x-rad, myPoint.y-rad, 
						myPoint.x+rad, myPoint.y+rad);

				// color depending on Wheelchairstate
				// TODO combine Wheelchair state and category into one image
				switch (poi.getWheelchairState()) {
				case UNKNOWN: {
					paint.setColor(0xcdc9c9);// Snow 3
					paint.setAlpha(250);
					break;
				}
				case YES:
					paint.setARGB(250, 0, 255, 0);
					break;
				case LIMITED:
					paint.setARGB(250, 255, 255, 0);
					break;
				case NO:
					paint.setARGB(250, 255, 0, 0);
					break;
				default:
					paint.setARGB(250, 255, 0, 0);
					break;
				}
				canvas.drawOval(oval, paint);
			}
		}
	}

}
