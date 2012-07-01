package org.wheelmap.android.activity;

import org.wheelmap.android.fragment.POIDetailFragment;
import org.wheelmap.android.fragment.POIDetailFragment.OnPOIDetailFragmentListener;
import org.wheelmap.android.model.POIHelper;
import org.wheelmap.android.model.Wheelmap;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.ui.POIDetailActivityEditable;
import org.wheelmap.android.ui.WheelchairStateActivity;
import org.wheelmap.android.ui.mapsforge.POIsMapsforgeActivity;

import roboguice.inject.ContentView;
import wheelmap.org.WheelchairState;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

@ContentView(R.layout.activity_fragment_list)
public class POIDetailActivity extends MapsforgeMapActivity implements
		OnPOIDetailFragmentListener {
	private final static String TAG = "poislist";
	private boolean isInForeground;

	// Definition of the one requestCode we use for receiving resuls.
	static final private int SELECT_WHEELCHAIRSTATE = 0;
	POIDetailFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		Intent intent = getIntent();
		// check if this intent is started via custom scheme link
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Uri uri = intent.getData();
			long wmID = -1l;
			try {
				wmID = Long.parseLong(uri.getLastPathSegment());
			} catch (NumberFormatException e) {
				// TODO: show a dialog with a meaningful message here
				finish();
			}
			Log.d(TAG, "onCreate: wmId = " + wmID);
			fragment = POIDetailFragment.newInstanceWithWMID(wmID);
		} else {
			long poiID = getIntent().getLongExtra(Wheelmap.POIs.EXTRAS_POI_ID, -1);
			Log.d(TAG, "onCreate: poiID = " + poiID);
			fragment = POIDetailFragment.newInstanceWithPOIID(poiID);
		}

		getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.fragment_detail_frame, fragment,
						POIDetailFragment.TAG).commit();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		isInForeground = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		isInForeground = false;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}
	
	public void onItemEdit(View v) {
		long poiID = fragment.getPoiId();
		
		// Launch overall conference schedule
		Intent i = new Intent(POIDetailActivity.this,
				POIDetailActivityEditable.class);
		i.putExtra(Wheelmap.POIs.EXTRAS_POI_ID, poiID);
		startActivity(i);
	}
	
	public void onItemShare(View v) {
		long poiID = fragment.getPoiId();

		Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI_POI_ID,
				String.valueOf(poiID));

		// Then query for this specific record:
		Cursor cur = getContentResolver().query(poiUri, null, null, null, null);
		if ( cur == null )
			return;

		if (cur.getCount() < 1) {
			cur.close();
			return;
		}

		cur.moveToFirst();
		String wmId = POIHelper.getWMId(cur);
		String name = POIHelper.getName(cur);
		String comment = POIHelper.getComment(cur);
		String address = POIHelper.getAddress(cur);
		String website = POIHelper.getWebsite(cur);
		cur.close();

		StringBuilder sb = new StringBuilder(name);

		if (comment.length() > 0) {
			sb.append(", ");
			sb.append(comment);
		}

		if (address.length() > 0) {
			sb.append(", ");
			sb.append(address);
		}

		if (website.length() > 0) {
			sb.append(", ");
			sb.append(website);
		}

		sb.append(", ");
		sb.append("http://wheelmap.org/nodes/" + String.valueOf(wmId));

		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent
				.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());
		startActivity(Intent.createChooser(sharingIntent, getResources()
				.getString(R.string.title_share_using)));
	}

	public void onItemExtern(View v) {
		long poiID = fragment.getPoiId();

		Uri poiUri = Uri.withAppendedPath(Wheelmap.POIs.CONTENT_URI_POI_ID,
				String.valueOf(poiID));

		// Then query for this specific record:
		Cursor cur = getContentResolver().query(poiUri, null, null, null, null);
		if ( cur == null )
			return;

		if (cur.getCount() < 1) {
			cur.close();
			return;
		}

		cur.moveToFirst();
		String name = POIHelper.getName(cur);
		double lat = POIHelper.getLatitude(cur);
		double lon = POIHelper.getLongitude(cur);
		String street = POIHelper.getStreet(cur);
		String houseNum = POIHelper.getHouseNumber(cur);
		String postCode = POIHelper.getPostcode( cur );
		String city = POIHelper.getCity( cur );		
		cur.close();

		Uri geoURI;
		if ( street.length() > 0 && ( postCode.length() > 0 || city.length() > 0 )) {
			String address = street + "+" + houseNum + "+" + postCode + "+" + city;
			geoURI = Uri.parse("geo:0,0?q=" + address.replace( " " , "+" ));
		} else {
			geoURI = Uri.parse("geo:" + String.valueOf(lat) + ","
					+ String.valueOf(lon) + "?z=17");
		}

		Log.d(TAG, "geoURI = " + geoURI.toString());

		Intent sharingIntent = new Intent(Intent.ACTION_VIEW);
		sharingIntent.setData(geoURI);
		startActivity(Intent.createChooser(sharingIntent, getResources()
				.getString(R.string.title_view_using)));
	}

	@Override
	public void onEditWheelchairState(Fragment fragment, WheelchairState wState) {
		// Sometimes, the poiId doesnt exists in the db, as the db got loaded
		// again
		// Actually it would be better to use the wmId in this activity, instead
		// of the poiId, as the wmId is persistent during reload
		// This is only a quick fix to take care of a npe here,
		// as mWheelchairState is null in this case.
		if (wState == null)
			return;

		// Start the activity whose result we want to retrieve. The
		// result will come back with request code GET_CODE.
		Intent intent = new Intent(POIDetailActivity.this,
				WheelchairStateActivity.class);
		intent.putExtra(Wheelmap.POIs.WHEELCHAIR, (long) wState.getId());
		startActivityForResult(intent, SELECT_WHEELCHAIRSTATE);

	}
	
	/**
	 * This method is called when the sending activity has finished, with the
	 * result it supplied.
	 * 
	 * @param requestCode
	 *            The original request code as given to startActivity().
	 * @param resultCode
	 *            From sending activity as per setResult().
	 * @param data
	 *            From sending activity as per setResult().
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// You can use the requestCode to select between multiple child
		// activities you may have started. Here there is only one thing
		// we launch.
		if (requestCode == SELECT_WHEELCHAIRSTATE) {
			// This is a standard resultCode that is sent back if the
			// activity doesn't supply an explicit result. It will also
			// be returned if the activity failed to launch.
			if (resultCode == RESULT_OK) {
				// newly selected wheelchair state as action data
				if (data != null) {
					long poiID = fragment.getPoiId();
					
					WheelchairState newState = WheelchairState.valueOf(Integer
							.parseInt(data.getAction()));
					Uri poiUri = Uri.withAppendedPath(
							Wheelmap.POIs.CONTENT_URI_POI_ID,
							String.valueOf(poiID));
					ContentValues values = new ContentValues();
					values.put(Wheelmap.POIs.WHEELCHAIR, newState.getId());
					values.put(Wheelmap.POIs.UPDATE_TAG,
							Wheelmap.UPDATE_WHEELCHAIR_STATE);
					this.getContentResolver().update(poiUri, values, "", null);

					final Intent intent = new Intent(Intent.ACTION_SYNC, null,
							POIDetailActivity.this, SyncService.class);
					intent.putExtra(SyncService.EXTRA_WHAT,
							SyncService.WHAT_UPDATE_SERVER);
					startService(intent);
				}
			}
		}
	}

	@Override
	public void onShowLargeMapAt(Fragment fragment, int lat, int lon) {
		Intent i = new Intent(POIDetailActivity.this,
				POIsMapsforgeActivity.class);
		i.putExtra(POIsMapsforgeActivity.EXTRA_CENTER_AT_LAT, lat);
		i.putExtra(POIsMapsforgeActivity.EXTRA_CENTER_AT_LON, lon);

		POIDetailActivity.this.startActivity(i);
	}
}
