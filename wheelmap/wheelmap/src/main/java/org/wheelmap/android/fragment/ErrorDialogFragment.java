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
package org.wheelmap.android.fragment;

import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncServiceException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class ErrorDialogFragment extends SherlockDialogFragment implements DialogInterface.OnClickListener {
	private final static String ARGUMENT_EXCEPTION = "org.wheelmap.android.ARGUMENT_EXCEPTION";
	public static final String TAG = "error_dialog";
	static boolean isShowing;
	
	
	public final static ErrorDialogFragment newInstance( SyncServiceException e ) {
		if ( isShowing )
			return null;
		
		isShowing = true;
		ErrorDialogFragment dialog = new ErrorDialogFragment();
		Bundle b = new Bundle();
		
		b.putParcelable( ARGUMENT_EXCEPTION, e );
		dialog.setArguments( b );
		return dialog;
	}
	
	public ErrorDialogFragment() {
		
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {	
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_Wheelmap_Dialog );
				
        SyncServiceException e = getArguments().getParcelable( ARGUMENT_EXCEPTION );

		if (e.getErrorCode() == SyncServiceException.ERROR_NETWORK_FAILURE)
			builder.setTitle(R.string.error_network_title);
		else
			builder.setTitle(R.string.error_occurred);
		builder.setIcon(R.drawable.ic_dialog_alert_holo_light );
		builder.setMessage(e.getRessourceString());
		builder.setNeutralButton(R.string.okay, this );
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		isShowing = false;	
	}
}
