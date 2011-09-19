package org.wheelmap.android.manager;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wheelmap.android.R;
import org.wheelmap.android.model.Support;
import org.wheelmap.android.model.Support.CategoriesContent;
import org.wheelmap.android.model.Support.LastUpdateContent;
import org.wheelmap.android.model.Support.NodeTypesContent;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.utils.DetachableResultReceiver;
import org.wheelmap.android.utils.DetachableResultReceiver.Receiver;

import wheelmap.org.WheelchairState;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;

public class SupportManager implements DetachableResultReceiver.Receiver {
	private static final String TAG = "support";
	private static SupportManager INSTANCE;

	private Context mContext;
	private Map<Integer, NodeType> mNodeTypeLookup;
	private Map<Integer, Category> mCategoryLookup;

	private DetachableResultReceiver mReceiver;
	private DetachableResultReceiver mStatusSender;

	private NodeType mDefaultNodeType;
	private Category mDefaultCategory;

	private final static long MILLISECS_PER_DAY = 1000 * 60 * 60 * 24;
	// TODO: put in a proper update INTERVAL
	private final static long DATE_INTERVAL_FOR_UPDATE_IN_DAYS = 1;

	public final static int CREATION_RUNNING = 0x20;
	public final static int CREATION_FINISHED = 0x21;
	public final static int CREATION_ERROR = 0x22;

	public static class NodeType {
		public NodeType(int id, String identifier, String localizedName,
				int categoryId) {
			this.id = id;
			this.identifier = identifier;
			this.localizedName = localizedName;
			this.categoryId = categoryId;
		}

		public Drawable iconDrawable;
		public Map<WheelchairState, Drawable> stateDrawables;
		public int id;
		public String identifier;
		public String localizedName;
		public int categoryId;
	}

	public static class Category {
		public Category(int id, String identifier, String localizedName) {
			this.id = id;
			this.identifier = identifier;
			this.localizedName = localizedName;
		}

		public int id;
		public String identifier;
		public String localizedName;
	}

	private SupportManager(Context ctx) {
		mContext = ctx;
		mCategoryLookup = new HashMap<Integer, Category>();
		mNodeTypeLookup = new HashMap<Integer, NodeType>();
		mStatusSender = new DetachableResultReceiver(new Handler());

		mDefaultCategory = new Category(0, "unknown",
				mContext.getString(R.string.category_unknown));
		mDefaultNodeType = new NodeType(0, "unknown",
				mContext.getString(R.string.nodetype_unknown), 0);
		mDefaultNodeType.stateDrawables = createDefaultDrawables(); 
	}

	public static SupportManager get() {
		return INSTANCE;
	}

	public static SupportManager initOnce(Context ctx) {
		if (INSTANCE == null) {
			INSTANCE = new SupportManager(ctx);
			INSTANCE.init();
		}
		return INSTANCE;
	}

	public void init() {
		Log.d(TAG, "SupportManager:init");

		if (!checkIfUpdateDurationPassed()) {
			initLookup();
			return;
		}

		mStatusSender.send(CREATION_RUNNING, Bundle.EMPTY);

		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);

		Intent categoriesIntent = new Intent(Intent.ACTION_SYNC, null,
				mContext, SyncService.class);
		categoriesIntent.putExtra(SyncService.EXTRA_WHAT,
				SyncService.WHAT_RETRIEVE_CATEGORIES);
		categoriesIntent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
		mContext.startService(categoriesIntent);

		Intent nodeTypesIntent = new Intent(Intent.ACTION_SYNC, null, mContext,
				SyncService.class);
		nodeTypesIntent.putExtra(SyncService.EXTRA_WHAT,
				SyncService.WHAT_RETRIEVE_NODETYPES);
		nodeTypesIntent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
		mContext.startService(nodeTypesIntent);

		createCurrentTimeTag();
	}

	public void registerReceiver(Receiver receiver) {
		mStatusSender.setReceiver(receiver, true);
	}

	private boolean checkIfUpdateDurationPassed() {
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = resolver.query(LastUpdateContent.CONTENT_URI,
				LastUpdateContent.PROJECTION, null, null, null);
		cursor.moveToFirst();
		if (cursor.getCount() == 1) {
			Date date;
			try {
				date = Support.LastUpdateContent.parseDate(LastUpdateContent.getDate(cursor));
			} catch (ParseException e) {
				return true;
			}

			long now = System.currentTimeMillis();

			long days = (now - date.getTime()) / MILLISECS_PER_DAY;
			Log.d(TAG, "checkIfUpdateDurationPassed: days = " + days);

			if (days >= DATE_INTERVAL_FOR_UPDATE_IN_DAYS)
				return true;

			return false;
		}

		return true;
	}

	private void createCurrentTimeTag() {
		ContentValues values = new ContentValues();
		String date = Support.LastUpdateContent.formatDate(new Date());
		values.put(LastUpdateContent.DATE, date);
		String whereClause = "( " + LastUpdateContent._ID + " = ? )";
		String[] whereValues = new String[] { String.valueOf(1) };

		insertContentValues(LastUpdateContent.CONTENT_URI,
				LastUpdateContent.PROJECTION, whereClause, whereValues, values);
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d(TAG, "SupportManager:onReceiveResult");
		if (resultCode == SyncService.STATUS_FINISHED) {
			int what = resultData.getInt(SyncService.EXTRA_WHAT);
			switch (what) {
			case SyncService.WHAT_RETRIEVE_LOCALES:
				initLocales();
				break;
			case SyncService.WHAT_RETRIEVE_CATEGORIES:
				initCategories();
				break;
			case SyncService.WHAT_RETRIEVE_NODETYPES:
				try {
					initNodeTypes();
				} catch (IOException e) {
					Log.v( TAG, "InitNodeTypes Error: " + e.getLocalizedMessage());
				}
				mStatusSender.send(CREATION_FINISHED, Bundle.EMPTY);
				break;
			default:
				// nothing to do
			}
		} else if (resultCode == SyncService.STATUS_ERROR) {
			mStatusSender.send(CREATION_ERROR, resultData);
		}
	}

	private void initLookup() {
		initCategories();
		try {
			initNodeTypes();
		} catch (IOException e) {
			Log.v( TAG, "InitNodeTypes Error: " + e.getLocalizedMessage());
		}
	}

	private void initLocales() {
		Log.d(TAG, "SupportManager:initLocales");
	}

	private void initCategories() {
		Log.d(TAG, "SupportManager:initCategories");
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = resolver.query(CategoriesContent.CONTENT_URI,
				CategoriesContent.PROJECTION, null, null, null);
		cursor.moveToFirst();
		mCategoryLookup.clear();

		while (!cursor.isAfterLast()) {
			int id = CategoriesContent.getCategoryId(cursor);
			String identifier = CategoriesContent.getIdentifier(cursor);
			String localizedName = CategoriesContent.getLocalizedName(cursor);
			mCategoryLookup
					.put(id, new Category(id, identifier, localizedName));

			cursor.moveToNext();
		}
	}

	private void initNodeTypes() throws IOException {
		Log.d(TAG, "SupportManager:initNodeTypes");
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = resolver.query(NodeTypesContent.CONTENT_URI,
				NodeTypesContent.PROJECTION, null, null, null);
		cursor.moveToFirst();
		mNodeTypeLookup.clear();

		while (!cursor.isAfterLast()) {
			int id = NodeTypesContent.getNodeTypeId(cursor);
			String identifier = NodeTypesContent.getIdentifier(cursor);
			Log.d(TAG,  "Loading nodetype: identifier = " + identifier);
			String localizedName = CategoriesContent.getLocalizedName(cursor);
			int categoryId = NodeTypesContent.getCategoryId(cursor);
			String iconPath = NodeTypesContent.getIconURL( cursor );

			NodeType nodeType = new NodeType(id, identifier, localizedName,
					categoryId);
			nodeType.iconDrawable = createIconDrawable(iconPath);
			nodeType.stateDrawables = createDrawableLookup(iconPath);
			mNodeTypeLookup.put(id, nodeType);
			cursor.moveToNext();
		}
	}

	private Drawable createIconDrawable( String assetPath ) {
		Bitmap bitmap;
		Log.d( TAG, "SupportManager:createIconDrawable loading " + assetPath );
		try {
			bitmap = BitmapFactory.decodeStream( mContext.getAssets().open( "icons/" + assetPath));
			
		} catch (IOException e) {
			Log.e(TAG, "Error in initNodes:createIconDrawable:" + e);
			return null;
		}
		Bitmap scaledBitmap = Bitmap.createScaledBitmap( bitmap, 100, 100, true);
		return new BitmapDrawable( scaledBitmap );
		
	}
	
	private Map<WheelchairState, Drawable> createDefaultDrawables() {
		Map<WheelchairState, Drawable> lookupMap = new HashMap<WheelchairState, Drawable>();
		
		int idx;		
		for (idx = 0; idx < WheelchairState.values().length - 1; idx++) {
			String path = String.format( "marker/%s.png", WheelchairState.valueOf(idx).toString().toLowerCase());
			Drawable drawable = null;
			try {
				drawable = Drawable.createFromStream(mContext.getAssets().open( path ), null);
			} catch (IOException e) {
				Log.e(TAG,  "Error in createDefaultDrawables." + e);
			}
			drawable.setBounds( 0, 0, 64, 64 );
			lookupMap.put(WheelchairState.valueOf(idx), drawable);
		}
		
		return lookupMap;
	}

	private Map<WheelchairState, Drawable> createDrawableLookup( String assetPath ) {
		Map<WheelchairState, Drawable> lookupMap = new HashMap<WheelchairState, Drawable>();
		Log.d( TAG, "SupportManager:createDrawableLookup loading " + assetPath );

		int idx;		
		for (idx = 0; idx < WheelchairState.values().length - 1; idx++) {
			String path = String.format( "marker/%s/%s", WheelchairState.valueOf(idx).toString().toLowerCase(), assetPath );
			Drawable drawable = null;
			try {
				drawable = Drawable.createFromStream(mContext.getAssets().open( path ), null);
			} catch (IOException e) {
				Log.e(TAG, "Error in initNodes:createDrawableLookup Loading fallback:" + e);
				drawable = mDefaultNodeType.stateDrawables.get( WheelchairState.valueOf(idx));
			}
			drawable.setBounds( 0, 0, 64, 64 );
			lookupMap.put(WheelchairState.valueOf(idx), drawable);
		}

		return lookupMap;
	}

	public Category lookupCategory(int id) {
		if (mCategoryLookup.containsKey(id))
			return mCategoryLookup.get(id);
		else
			return mDefaultCategory;

	}

	public NodeType lookupNodeType(int id) {
		if (mNodeTypeLookup.containsKey(id))
			return mNodeTypeLookup.get(id);
		else
			return mDefaultNodeType;
	}

	public List<Category> getCategoryList() {
		Set<Integer> keys = mCategoryLookup.keySet();
		List<Category> list = new ArrayList<Category>();
		for (Integer key : keys) {
			list.add(mCategoryLookup.get(key));
		}
		return list;
	}

	public List<NodeType> getNodeTypeList() {
		Set<Integer> keys = mNodeTypeLookup.keySet();
		List<NodeType> list = new ArrayList<NodeType>();
		for (Integer key : keys) {
			list.add(mNodeTypeLookup.get(key));
		}

		return list;
	}

	public List<NodeType> getNodeTypeListByCategory(int categoryId) {
		Set<Integer> keys = mNodeTypeLookup.keySet();
		List<NodeType> list = new ArrayList<NodeType>();
		for (Integer key : keys) {
			NodeType nodeType = mNodeTypeLookup.get(key);

			if (nodeType.categoryId == categoryId) {
				list.add(nodeType);
			}
		}
		return list;
	}

	private void insertContentValues(Uri contentUri, String[] projection,
			String whereClause, String[] whereValues, ContentValues values) {
		ContentResolver resolver = mContext.getContentResolver();
		Cursor c = resolver.query(contentUri, projection, whereClause,
				whereValues, null);
		int cursorCount = c.getCount();
		if (cursorCount == 0)
			resolver.insert(contentUri, values);
		else if (cursorCount == 1)
			resolver.update(contentUri, values, whereClause, whereValues);
		else {
			// do nothing, as more than one file would be updated
		}
	}

}
