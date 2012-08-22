package org.wheelmap.modules;

import org.wheelmap.request.IHttpUserAgent;

import com.google.inject.Inject;
import com.google.inject.Provider;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class HttpUserAgent implements IHttpUserAgent {

	private Application application;

	@Inject
	HttpUserAgent(Provider<Application> applicationProvider) {
		this.application= applicationProvider.get();
	}

	@Override
	public String getAppUserAgent() {
		PackageInfo pInfo;
		try {
			pInfo = application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			pInfo = null;
			e.printStackTrace();
		};
		final StringBuilder sb = new StringBuilder(application.getPackageName());
		if (pInfo != null)
		{
			sb.append("/");
			sb.append(pInfo.versionName);
		}
		return sb.toString();
	}
}
