package org.toulibre.capitoledulibre;

import android.app.Application;
import android.preference.PreferenceManager;

import com.squareup.leakcanary.LeakCanary;

import org.toulibre.capitoledulibre.alarms.AlarmManager;
import org.toulibre.capitoledulibre.db.DatabaseManager;

public class CdlApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this);
        }

        DatabaseManager.init(this);
        // Initialize settings
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        // Alarms (requires settings)
        AlarmManager.getInstance(this);
    }
}
