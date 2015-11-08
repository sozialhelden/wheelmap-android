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
import org.wheelmap.android.service.RestServiceException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ErrorDialogFragment extends DialogFragment implements
        DialogInterface.OnClickListener {

    public static final String TAG = ErrorDialogFragment.class.getSimpleName();

    static boolean isShowing;

    private OnErrorDialogListener mListener;

    private int mId;

    public interface OnErrorDialogListener {

        void onErrorDialogClose(int id);
    }

    public static ErrorDialogFragment newInstance(String title,
            String message, int id) {
        if (isShowing) {
            return null;
        }

        isShowing = true;

        ErrorDialogFragment dialog = new ErrorDialogFragment();
        Bundle b = new Bundle();
        b.putString(Extra.ALERT_TITLE, title);
        b.putString(Extra.ALERT_MESSAGE, message);
        b.putInt(Extra.ID, id);
        dialog.setArguments(b);
        return dialog;
    }

    public static ErrorDialogFragment newInstance(RestServiceException e,
            int id) {
        if (isShowing) {
            return null;
        }

        isShowing = true;
        ErrorDialogFragment dialog = new ErrorDialogFragment();
        Bundle b = new Bundle();
        b.putParcelable(Extra.EXCEPTION, e);
        b.putInt(Extra.ID, id);
        dialog.setArguments(b);
        return dialog;
    }

    public void setOnErrorDialogListener(OnErrorDialogListener listener) {
        mListener = listener;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mListener == null && activity instanceof OnErrorDialogListener) {
            mListener = (OnErrorDialogListener) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isShowing = false;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mId = getArguments().getInt(Extra.ID);
        String title;
        String msg;
        if (getArguments().containsKey(Extra.ALERT_TITLE)) {
            title = getArguments().getString(Extra.ALERT_TITLE);
            msg = getArguments().getString(Extra.ALERT_MESSAGE);
        } else {
            RestServiceException e = getArguments().getParcelable(
                    Extra.EXCEPTION);
            if (e.isNetworkError()) {
                title = getString(R.string.error_network_title);
            } else {
                title = getString(R.string.error_authorization_error);
            }
            msg = title;
        }
        if(title != null){
            builder.setTitle(title);
        }
        builder.setMessage(msg);
        builder.setIcon(R.drawable.ic_dialog_alert_wheelmap);
        builder.setNeutralButton(R.string.btn_okay, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mListener != null) {
            mListener.onErrorDialogClose(mId);
        }
    }
}
