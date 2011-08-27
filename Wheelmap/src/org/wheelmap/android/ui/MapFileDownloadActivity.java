package org.wheelmap.android.ui;

import java.io.File;

import org.wheelmap.android.R;
import org.wheelmap.android.manager.MapFileManager;
import org.wheelmap.android.model.MapFileDownloadCursorAdapter;
import org.wheelmap.android.model.MapFileInfo;
import org.wheelmap.android.model.MapFileInfoProvider;
import org.wheelmap.android.model.MapFileInfo.MapFileInfos;
import org.wheelmap.android.service.MapFileService;
import org.wheelmap.android.utils.DetachableResultReceiver;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MapFileDownloadActivity extends ListActivity implements
		DetachableResultReceiver.Receiver {

	private final static String TAG = "mapfileactivity";

	/** State held between configuration changes. */
	private State mState;
	private final static String EXTRA_CURSOR_PARENT_DIR = "org.wheelmap.android.ui.MapFile.PARENT_DIR";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mapfile_download);

		mState = (State) getLastNonConfigurationInstance();
		final boolean previousState = mState != null;

		if (previousState) {
			mState.mReceiver.setReceiver(this);
			updateRefreshStatus();
		} else {
			mState = new State();
			mState.mReceiver.setReceiver(this);
			mState.mManager = MapFileManager.get(getApplicationContext());
			mState.mManager.registerResultReceiver( mState.mReceiver );
		}

		String parentDir = null;
		Bundle b = getIntent().getExtras();
		if (b == null) {
			mState.mManager.updateDatabaseWithRemote();
			mState.mManager.updateDatabaseWithLocal();
			parentDir = mState.mManager.getRootDirectory();
			Log.d(TAG, "parentDir = *" + parentDir + "*");
		} else
			parentDir = b.getString(EXTRA_CURSOR_PARENT_DIR);

		Uri uri = MapFileInfos.CONTENT_URI_DIRSNFILES;
		WhereClauseWithParent whereClause = new WhereClauseWithParent(parentDir);

		Cursor cursor = managedQuery(uri, MapFileInfos.filePROJECTION,
				whereClause.clause, whereClause.values, null);
		startManagingCursor(cursor);

		MapFileDownloadCursorAdapter adapter = new MapFileDownloadCursorAdapter(
				this, cursor);
		setListAdapter(adapter);

	}

	@Override
	public void onPause() {
		super.onPause();
		
		if ( isFinishing()) {
			mState.mManager.unregisterResultReceiver( mState.mReceiver );
			if ( getIntent().getExtras() == null)
				mState.mManager.stop();
		}
	}

	public void onHomeClick(View v) {
		final Intent intent = new Intent(this, WheelmapHomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
	}

	public void onRefreshClick(View v) {
		mState.mManager.updateDatabaseWithRemote();
		mState.mManager.updateDatabaseWithLocal();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Cursor cursor = (Cursor) l.getItemAtPosition(position);

		Log.d(TAG, "mCursor " + MapFileInfo.getRemoteName(cursor));
		int type = MapFileInfo.getType(cursor);
		if (type == MapFileInfoProvider.DIRS) {
			String parentName = MapFileInfo.getRemoteParentName(cursor)
					+ File.separator + MapFileInfo.getRemoteName(cursor);
			Log.d(TAG, "starting activity with parentName = " + parentName);
			Intent i = new Intent(MapFileDownloadActivity.this,
					MapFileDownloadActivity.class);
			i.putExtra(EXTRA_CURSOR_PARENT_DIR, parentName);
			startActivity(i);
		}
	}

	private void updateRefreshStatus() {
		findViewById(R.id.btn_title_refresh).setVisibility(
				mState.mSyncing ? View.GONE : View.VISIBLE);
		findViewById(R.id.title_refresh_progress).setVisibility(
				mState.mSyncing ? View.VISIBLE : View.GONE);
	}

	/** {@inheritDoc} */
	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case MapFileService.STATUS_RUNNING: {
			mState.mSyncing = true;
			updateRefreshStatus();
			break;
		}
		case MapFileService.STATUS_FINISHED: {
			mState.mSyncing = false;
			updateRefreshStatus();
			break;
		}
		case MapFileService.STATUS_ERROR: {
			mState.mSyncing = false;
			updateRefreshStatus();
			String errorMessage = resultData
					.getString(MapFileService.STATUS_ERROR_MSG);
			Toast.makeText(MapFileDownloadActivity.this,
					"Error: " + errorMessage, Toast.LENGTH_LONG).show();
			break;
		}
		}
	}

	private static class WhereClauseWithParent {
		String clause;
		String values[];

		public WhereClauseWithParent(String parentName) {
			clause = "( " + MapFileInfos.REMOTE_PARENT_NAME + " = ? )";
			values = new String[] { parentName };
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		mState.mReceiver.clearReceiver();
		return mState;
	}

	/**
	 * State specific to {@link HomeActivity} that is held between configuration
	 * changes. Any strong {@link Activity} references <strong>must</strong> be
	 * cleared before {@link #onRetainNonConfigurationInstance()}, and this
	 * class should remain {@code static class}.
	 */
	private static class State {
		public DetachableResultReceiver mReceiver;
		public MapFileManager mManager;
		public boolean mSyncing = false;

		private State() {
			mReceiver = new DetachableResultReceiver(new Handler());
		}
	}
}
