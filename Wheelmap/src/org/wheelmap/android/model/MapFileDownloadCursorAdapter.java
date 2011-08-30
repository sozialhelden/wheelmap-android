package org.wheelmap.android.model;

import org.wheelmap.android.R;
import org.wheelmap.android.manager.MapFileManager;
import org.wheelmap.android.service.MapFileService.BaseListener;
import org.wheelmap.android.service.MapFileService.RetrieveFileListener;
import org.wheelmap.android.service.MapFileService.Task;
import org.wheelmap.android.ui.MapFileDirItemView;
import org.wheelmap.android.ui.MapFileFileItemView;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class MapFileDownloadCursorAdapter extends CursorAdapter {
	private final static String TAG = "mapfilecursoradpater";
	private Context context;
	private final Handler mHandler = new Handler();

	private int VIEW_TYPE_COUNT = 2;

	public MapFileDownloadCursorAdapter(Context context, Cursor c) {
		super(context, c);
		this.context = context;
	}

	@Override
	public int getViewTypeCount() {
		return VIEW_TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		int type = MapFileInfo.getType((Cursor) getItem(position));
		return type - 1; // types in the provider start with 1;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String screenName = MapFileInfo.getScreenName(cursor);

		int entityType = MapFileInfo.getType(cursor);
		if (entityType == MapFileInfoProvider.DIRS) {
			MapFileDirItemView mfdiv = (MapFileDirItemView) view;
			mfdiv.setDirName(screenName);
		} else {
			MapFileFileItemView mffiv = (MapFileFileItemView) view;
			mffiv.setDownloadButtonListener(null);
			mffiv.setDeleteButtonListener(null);

			String parentName = MapFileInfo.getParentName(cursor);
			String name = MapFileInfo.getName(cursor);
			String remoteParentName = MapFileInfo.getRemoteParentName(cursor);
			String remoteName = MapFileInfo.getRemoteName(cursor);
			String remoteTimeStamp = MapFileInfo.getRemoteTimestamp(cursor);
			String localTimeStamp = MapFileInfo.getLocalTimestamp(cursor);

			mffiv.setFileName(screenName);
			int megaBytes = (int) (MapFileInfo.getRemoteSize(cursor) / (1024 * 1024));
			mffiv.setFileSize(megaBytes);

			int localAvailable = MapFileInfo.getLocalAvailable(cursor);
			boolean updateAvailable = ((localTimeStamp != null) && localTimeStamp
					.compareTo(remoteTimeStamp) < 0);
			String availableText;
			if ((localAvailable != 0) && updateAvailable)
				availableText = context
						.getString(R.string.download_update_available);
			else if (localAvailable == MapFileInfo.FILE_COMPLETE)
				availableText = context
						.getString(R.string.download_file_local_available);
			else if (localAvailable == MapFileInfo.FILE_INCOMPLETE)
				availableText = context
						.getString(R.string.download_file_local_incomplete);
			else
				availableText = "";
			mffiv.setLocalAvailable(availableText);

			RetrieveFileListener dListener = createDownloadListener(mffiv);
			Task task = MapFileManager.get(context.getApplicationContext())
					.findTask(remoteName, remoteParentName,
							Task.TYPE_RETRIEVE_FILE);
			Task unknownTask = MapFileManager.get(
					context.getApplicationContext()).findTask(remoteName,
					remoteParentName, Task.TYPE_UNKNOWN);
			if (task != null) {
				task.listener.setListener(dListener);
				mffiv.setProgressVisibility(View.VISIBLE);
				int progress = ((RetrieveFileListener) task.listener).getProgress();
				if (progress == 0)
					mffiv.setProgress(context.getResources().getString(
							R.string.pending));
				else
					mffiv.setProgress(progress + " %");
				mffiv.setDownloadEnabled(false);
			} else if (unknownTask != null
					&& unknownTask.type != Task.TYPE_RETRIEVE_FILE) {
				if (unknownTask.type == Task.TYPE_DELETE_FILE)
					unknownTask.listener.setListener(defaultBaseListener);
				mffiv.setProgressVisibility(View.VISIBLE);
				mffiv.setProgress(context.getResources().getString(
						R.string.pending));
			} else {
				mffiv.setProgressVisibility(View.INVISIBLE);
				mffiv.setProgress("");
				mffiv.setDownloadEnabled(true);
			}

			OnClickListener clickListener = createClickListener(name,
					parentName, remoteName, remoteParentName, dListener);
			mffiv.setDownloadButtonListener(clickListener);
			mffiv.setDownloadButtonVisibility(View.VISIBLE);
			mffiv.setDeleteButtonListener(clickListener);
			mffiv.setDeleteButtonVisibility(localAvailable != 0);

		}

	}

	@Override
	public View newView(Context ctx, Cursor cursor, ViewGroup parent) {
		int entityType = MapFileInfo.getType(cursor);
		if (entityType == MapFileInfoProvider.DIRS)
			return new MapFileDirItemView(ctx);
		else
			return new MapFileFileItemView(ctx);
	}

	public RetrieveFileListener createDownloadListener(
			final MapFileFileItemView fileItemView) {
		
		return new RetrieveFileListener() {
			
			@Override
			public void setListener(BaseListener listener) {				
			}
			
			@Override
			public void onRunning() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						fileItemView.setDownloadEnabled(false);
						fileItemView.setProgressVisibility(View.VISIBLE);
						notifyDataSetChanged();
					}
				});				
			}
			
			@Override
			public void onFinished() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						fileItemView.setDownloadEnabled(true);
						fileItemView.setProgressVisibility(View.INVISIBLE);
						notifyDataSetChanged();
					}
				});				
			}
			
			@Override
			public void onProgress(final int percentageProgress) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						fileItemView.setProgress(String
								.valueOf(percentageProgress) + "%");
						notifyDataSetChanged();
					}
				});				
			}
			
			@Override
			public int getProgress() {
				return 0;
			}
		};
	}

	public OnClickListener createClickListener(final String name,
			final String parentName, final String remoteName,
			final String remoteParentName, final RetrieveFileListener listener) {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = v.getId();
				switch (id) {
				case R.id.list_item_file_btn_download:
					MapFileManager.get(context.getApplicationContext())
							.retrieveFile(remoteParentName, remoteName,
									listener);
					break;
				case R.id.list_item_file_btn_delete:
					MapFileManager.get(context.getApplicationContext())
							.deleteFile(parentName, name);
					break;
				default:
					// noop
				}

				notifyDataSetChanged();
			}
		};
	}

	private BaseListener defaultBaseListener = new BaseListener() {
		@Override
		public void setListener(BaseListener listener) {
		}

		@Override
		public void onRunning() {
		}

		@Override
		public void onFinished() {
			mHandler.post(new Runnable() {
				public void run() {
					notifyDataSetChanged();
				}
			});
		}
	};

}
