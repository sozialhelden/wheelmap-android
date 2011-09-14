package org.wheelmap.android.manager;

import java.util.HashMap;
import java.util.Map;

import org.wheelmap.android.model.Support.CategoriesContent;
import org.wheelmap.android.model.Support.NodeTypesContent;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.utils.DetachableResultReceiver;

import wheelmap.org.WheelchairState;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class SupportManager implements DetachableResultReceiver.Receiver {
	private static final String TAG = "support";
	private static SupportManager INSTANCE;

	private Context mContext;
	private Map<Integer, NodeType> mNodeTypeLookup;
	private Map<Integer, Category> mCategoryLookup;
	
	private DetachableResultReceiver mReceiver;

	public static class NodeType {
		public NodeType( String identifier, String localizedName, int categoryId ) {
			this.identifier = identifier;
			this.localizedName = localizedName;
			this.categoryId = categoryId;
		}
		public Map<WheelchairState, Drawable> mDrawables;
		public int id;
		public String identifier;
		public String localizedName;
		public int categoryId;
	}

	public static class Category {
		public Category( String identifier, String localizedName ) {
			this.identifier = identifier;
			this.localizedName = localizedName;
		}
		public String identifier;
		public String localizedName;
	}

	private SupportManager(Context ctx) {
		mContext = ctx;
		mCategoryLookup = new HashMap<Integer, Category>();
		mNodeTypeLookup = new HashMap<Integer, NodeType>();
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
		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);
		
		Intent categoriesIntent = new Intent(Intent.ACTION_SYNC, null, mContext,
				SyncService.class);
		categoriesIntent.putExtra(SyncService.EXTRA_WHAT,
				SyncService.WHAT_RETRIEVE_CATEGORIES);
		categoriesIntent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
		mContext.startService(categoriesIntent);
		
		Intent nodeTypesIntent = new Intent(Intent.ACTION_SYNC, null, mContext, SyncService.class);
		nodeTypesIntent.putExtra(SyncService.EXTRA_WHAT, SyncService.WHAT_RETRIEVE_NODETYPES );		
		nodeTypesIntent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
		mContext.startService( nodeTypesIntent );
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d(TAG, "SupportManager:onReceiveResult");
		if ( resultCode == SyncService.STATUS_FINISHED ) {
			int what = resultData.getInt( SyncService.EXTRA_WHAT );
			switch( what ) {
			case SyncService.WHAT_RETRIEVE_LOCALES:
				initLocales();
				break;
			case SyncService.WHAT_RETRIEVE_CATEGORIES:
				initCategories();
				break;
			case SyncService.WHAT_RETRIEVE_NODETYPES:
				initNodeTypes();
				break;
			default:
				// nothing to do
			}
		}
	}
	
	private void initLocales() {
		Log.d( TAG, "SupportManager:initLocales" );
	}
	
	private void initCategories() {
		Log.d( TAG, "SupportManager:initCategories" );
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = resolver.query( CategoriesContent.CONTENT_URI, CategoriesContent.PROJECTION, null, null, null);
		cursor.moveToFirst();
		
		while( !cursor.isAfterLast()) {
			int id = CategoriesContent.getCategoryId( cursor );
			String identifier = CategoriesContent.getIdentifier( cursor );
			String localizedName = CategoriesContent.getLocalizedName( cursor );
			mCategoryLookup.put( id, new Category( identifier, localizedName ));
			
			cursor.moveToNext();
		}
	}
	
	private void initNodeTypes() {
		Log.d( TAG, "SupportManager:initNodeTypes" );
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = resolver.query( NodeTypesContent.CONTENT_URI, NodeTypesContent.PROJECTION, null, null, null);
		cursor.moveToFirst();

		while( !cursor.isAfterLast()) {
			int id = NodeTypesContent.getNodeTypeId( cursor );
			String identifier = NodeTypesContent.getIdentifier( cursor );
			String localizedName = CategoriesContent.getLocalizedName( cursor );
			int categoryId = NodeTypesContent.getCategoryId( cursor );
			byte[] iconData = NodeTypesContent.getIconData( cursor );
			
			NodeType nodeType = new NodeType( identifier, localizedName, categoryId );
			nodeType.mDrawables = createDrawableLookup( iconData );
			mNodeTypeLookup.put( id, nodeType );			
			cursor.moveToNext();
		}
	}
	
	private Map<WheelchairState, Drawable> createDrawableLookup( byte[] iconData ) {
		Drawable iconDrawable = null;
		if ( iconData != null) {
			Bitmap bitmap = BitmapFactory.decodeByteArray( iconData, 0, iconData.length);
			iconDrawable = new BitmapDrawable( bitmap );
		}
		
		return null;
	}
	
	public Category lookupCategory( int id ) {
		return mCategoryLookup.get( id );
	}
	
	public NodeType lookupNodeType( int id ) {
		return mNodeTypeLookup.get( id );
	}

}
