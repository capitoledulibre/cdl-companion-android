package org.toulibre.capitoledulibre;

import android.app.Application;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;

import org.toulibre.capitoledulibre.alarms.FosdemAlarmManager;
import org.toulibre.capitoledulibre.db.DatabaseManager;

import io.fabric.sdk.android.Fabric;

public class CdlApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Stetho.initializeWithDefaults(this);
        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this);
        }

        DatabaseManager.init(this);
        // Initialize settings
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        // Alarms (requires settings)
        FosdemAlarmManager.init(this);
    }
}
