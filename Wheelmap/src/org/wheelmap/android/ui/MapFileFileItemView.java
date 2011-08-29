package org.wheelmap.android.ui;


import org.wheelmap.android.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MapFileFileItemView extends FrameLayout {
	
	private TextView mFileName;
	private TextView mFileSize;
	private TextView mFileLocalAvailable;
	private Button mFileDelete;
	private Button mFileDownload;
	private TextView mProgress;
			
	public MapFileFileItemView(Context context)  {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.map_list_item_file, this, true);
		
		mFileName = (TextView) findViewById( R.id.list_item_file_name );
		mFileSize = (TextView) findViewById( R.id.list_item_file_size );
		mFileLocalAvailable = (TextView) findViewById( R.id.list_item_file_local_available);
		mFileDelete = (Button) findViewById( R.id.list_item_file_btn_delete );
		mFileDownload = (Button) findViewById( R.id.list_item_file_btn_download );
		mProgress = (TextView) findViewById( R.id.list_item_file_progress );
	}	
	
	public void setFileName( String name ) {
		mFileName.setText( name );
	}
	
	public void setFileSize( int megabytes ) {
		mFileSize.setText( String.valueOf( megabytes ) + " MB");
	}
	
	public void setLocalAvailable( String message ) {
		mFileLocalAvailable.setText( message );
	}

	public void setDeleteButtonListener( OnClickListener listener ) {
		mFileDelete.setOnClickListener( listener );
	}
	
	public void setDeleteButtonVisibility( boolean visibility ) {
		int param = visibility ? View.VISIBLE : View.INVISIBLE;
		mFileDelete.setVisibility( param );
	}
	
	public void setDownloadButtonListener( OnClickListener listener ) {
		mFileDownload.setOnClickListener( listener );
	}
	
	public void setDownloadButtonVisibility( int visibility ) {
		mFileDownload.setVisibility( visibility );
	}
	
	public void setDownloadEnabled(boolean enabled) {
		mFileDownload.setEnabled( enabled );
	}
	
	public void setProgressVisibility( int visibility ) {
		mProgress.setVisibility( visibility );
	}
	
	public int getProgressVisibility() {
		return mProgress.getVisibility();
	}
	
	public void setProgress( String progress ) {
		mProgress.setText( progress );
	}
	
	
	
}
