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

import org.wheelmap.android.model.Extra;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncServiceException;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.app.SherlockDialogFragment;

public class ErrorDialogFragment extends SherlockDialogFragment implements
		DialogInterface.OnClickListener {
	public static final String TAG = ErrorDialogFragment.class.getSimpleName();
	static boolean isShowing;
	private OnErrorDialogListener mListener;

	public interface OnErrorDialogListener {
		public void onErrorDialogClose();
	}

	public final static ErrorDialogFragment newInstance(SyncServiceException e) {
		if (isShowing)
			return null;

		isShowing = true;
		ErrorDialogFragment dialog = new ErrorDialogFragment();
		Bundle b = new Bundle();

		b.putParcelable(Extra.EXCEPTION, e);
		dialog.setArguments(b);
		return dialog;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnErrorDialogListener)
			mListener = (OnErrorDialogListener) activity;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		HoloAlertDialogBuilder builder = new HoloAlertDialogBuilder(
				getActivity());

		SyncServiceException e = getArguments().getParcelable(Extra.EXCEPTION);

		if (e.getErrorCode() == SyncServiceException.ERROR_NETWORK_FAILURE)
			builder.setTitle(R.string.error_network_title);
		else
			builder.setTitle(R.string.error_occurred);

		builder.setIcon(R.drawable.ic_dialog_alert_holo_light);
		builder.setMessage(e.getRessourceString());
		builder.setNeutralButton(R.string.okay, this);
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		isShowing = false;

		if (mListener != null)
			mListener.onErrorDialogClose();
	}
}
