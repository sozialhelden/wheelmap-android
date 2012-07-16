package org.wheelmap.android.fragment;

import org.wheelmap.android.model.UserCredentials;
import org.wheelmap.android.online.R;
import org.wheelmap.android.service.SyncService;
import org.wheelmap.android.service.SyncServiceException;
import org.wheelmap.android.utils.DetachableResultReceiver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.akquinet.android.androlog.Log;

public class LoginDialogFragment extends SherlockDialogFragment implements
		OnClickListener, DetachableResultReceiver.Receiver {
	public final static String TAG = LoginDialogFragment.class.getSimpleName();

	private EditText mEmailText;
	private EditText mPasswordText;
	private ProgressBar mProgressBar;

	private boolean mSyncing;
	private DetachableResultReceiver mReceiver;
	private OnLoginDialogListener mListener;

	public interface OnLoginDialogListener {
		public void onLoginSuccessful();

		public void onLoginCancelled();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnLoginDialogListener) {
			mListener = (OnLoginDialogListener) activity;
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.login_info);
		builder.setIcon(R.drawable.ic_menu_search_wm_holo_light);
		builder.setNeutralButton(R.string.login_submit, null);
		builder.setOnCancelListener(this);

		View view = getActivity().getLayoutInflater().inflate(
				R.layout.fragment_dialog_login, null);
		builder.setView(view);

		mEmailText = (EditText) view.findViewById(R.id.login_email);
		mPasswordText = (EditText) view.findViewById(R.id.login_password);

		load();
		Dialog d = builder.create();
		return d;
	}

	@Override
	public void onResume() {
		super.onResume();

		AlertDialog dialog = (AlertDialog) getDialog();
		Button button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
		button.setOnClickListener(this);

		mProgressBar = (ProgressBar) dialog.findViewById(R.id.progressbar);

	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		if (mListener != null)
			mListener.onLoginCancelled();
	}

	private void load() {
		UserCredentials userCredentials = new UserCredentials(getActivity());
		String login = userCredentials.getLogin();
		String password = userCredentials.getPassword();

		if (userCredentials.isLoggedIn()) {
			mEmailText.setText(login);
			mPasswordText.setText(password);
		}
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.d(TAG, "onReceiveResult in list resultCode = " + resultCode);
		switch (resultCode) {
		case SyncService.STATUS_RUNNING:
			mSyncing = true;
			updateRefreshStatus();
			break;
		case SyncService.STATUS_FINISHED:
			mSyncing = false;
			updateRefreshStatus();
			loginSuccessful();
			break;
		case SyncService.STATUS_ERROR:
			// Error happened down in SyncService, show as toast.
			mSyncing = false;
			updateRefreshStatus();
			final SyncServiceException e = resultData
					.getParcelable(SyncService.EXTRA_ERROR);
			showErrorDialog(e);
			break;
		default: // noop
		}

	}

	private void updateRefreshStatus() {
		if (mSyncing)
			mProgressBar.setVisibility(View.VISIBLE);
		else
			mProgressBar.setVisibility(View.INVISIBLE);
	}

	private void showErrorDialog(SyncServiceException e) {
		SherlockFragmentActivity activity = getSherlockActivity();
		FragmentManager fm = activity.getSupportFragmentManager();
		ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(e);
		if (errorDialog == null)
			return;

		errorDialog.show(fm, ErrorDialogFragment.TAG);
	}

	private void loginSuccessful() {
		dismiss();
		if (mListener != null)
			mListener.onLoginSuccessful();
	}

	@Override
	public void onClick(View v) {
		String email = mEmailText.getText().toString();
		String password = mPasswordText.getText().toString();
		login(email, password);
	}

	private void login(String email, String password) {
		Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
				SyncService.class);
		intent.putExtra(SyncService.EXTRA_WHAT,
				SyncService.WHAT_RETRIEVE_APIKEY);
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
		intent.putExtra(SyncService.EXTRA_EMAIL, email);
		intent.putExtra(SyncService.EXTRA_PASSWORD, password);

		getActivity().startService(intent);
	}

}
