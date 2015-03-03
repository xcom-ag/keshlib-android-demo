package de.xcom.keshlib.keshlibandroiddemo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import de.xcom.kesh.keshlib.KeshServiceManager;
import de.xcom.kesh.keshlib.KeshServiceManagerConfiguration;

/**
 * Created by gholz on 22.10.14.
 */
public abstract class BaseActivity extends ActionBarActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!KeshServiceManager.isInitialized()) {
			// Initialize the kesh service manager
			Context appContext = getApplicationContext();
			String appVersion;
			try {
				PackageInfo packageInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
				appVersion = packageInfo.versionName + " " + packageInfo.versionCode;
			} catch (PackageManager.NameNotFoundException e) {
				appVersion = "0.0.0";
				// should never happen
				throw new RuntimeException("Could not get package name: " + e);
			}
			KeshServiceManagerConfiguration conf = new KeshServiceManagerConfiguration(appContext, appVersion, "myAppType");
			// Disable auto reconnect
			conf.setAutoReconnectEnabled(false);
			KeshServiceManager.initializeManager(conf);
			// Show full debug prints
			// KeshServiceManager.setLogLevel(Level.FINEST);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
