package org.toulibre.cdl;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import org.toulibre.cdl.alarms.FosdemAlarmManager;
import org.toulibre.cdl.db.DatabaseManager;

import android.app.Application;
import android.preference.PreferenceManager;

public class CdlApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());

		DatabaseManager.init(this);
		// Initialize settings
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
		// Alarms (requires settings)
		FosdemAlarmManager.init(this);
	}
}
