package org.toulibre.capitoledulibre.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;

import org.toulibre.capitoledulibre.BuildConfig;
import org.toulibre.capitoledulibre.activities.SettingsActivity;
import org.toulibre.capitoledulibre.db.DatabaseManager;
import org.toulibre.capitoledulibre.services.AlarmIntentService;

/**
 * This class monitors bookmarks and preferences changes to dispatch alarm update work to AlarmIntentService.
 *
 * @author Christophe Beyls
 *
 */
public class AlarmManager implements OnSharedPreferenceChangeListener {

	private static final String ACTION_CHANGE_ALARMS = BuildConfig.APPLICATION_ID + ".change_alarms";
	private static final String EXTRA_ENABLE = "enable";

	private static AlarmManager sInstance;

	public static synchronized AlarmManager getInstance(Context context) {
	    if (sInstance == null) {
	        sInstance = new AlarmManager(context);
        }
        return sInstance;
    }

	private LocalBroadcastManager mBroadcastManager;
	private boolean isEnabled;

	private final BroadcastReceiver scheduleRefreshedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// When the schedule DB is updated, update the alarms too
			startUpdateAlarms(context);
		}
	};

	private final BroadcastReceiver bookmarksReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// Dispatch the Bookmark broadcasts to the service
            if (intent != null && intent.getExtras() != null) {
                Intent serviceIntent = new Intent(intent.getAction()).putExtras(intent.getExtras());
                JobIntentService.enqueueWork(context, AlarmIntentService.class, AlarmIntentService.JOB_ID, serviceIntent);
            }
		}
	};

	private AlarmManager(Context context) {
		mBroadcastManager = LocalBroadcastManager.getInstance(context);
		mBroadcastManager.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getBooleanExtra(EXTRA_ENABLE, false)) {
					startDisableAlarms(context);
				} else {
					startUpdateAlarms(context);
				}
			}
		}, new IntentFilter(ACTION_CHANGE_ALARMS));
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		isEnabled = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_NOTIFICATIONS_ENABLED, false);
		if (isEnabled) {
			registerReceivers();
		}
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (SettingsActivity.KEY_PREF_NOTIFICATIONS_ENABLED.equals(key)) {
			isEnabled = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_NOTIFICATIONS_ENABLED, false);
			if (isEnabled) {
				registerReceivers();
				mBroadcastManager.sendBroadcast(new Intent(ACTION_CHANGE_ALARMS).putExtra(EXTRA_ENABLE, true));
			} else {
				unregisterReceivers();
				mBroadcastManager.sendBroadcast(new Intent(ACTION_CHANGE_ALARMS).putExtra(EXTRA_ENABLE, false));
			}
		} else if (SettingsActivity.KEY_PREF_NOTIFICATIONS_DELAY.equals(key)) {
			mBroadcastManager.sendBroadcast(new Intent(ACTION_CHANGE_ALARMS).putExtra(EXTRA_ENABLE, true));
		}
	}

	private void registerReceivers() {
		mBroadcastManager.registerReceiver(scheduleRefreshedReceiver, new IntentFilter(DatabaseManager.ACTION_SCHEDULE_REFRESHED));
		IntentFilter filter = new IntentFilter();
		filter.addAction(DatabaseManager.ACTION_ADD_BOOKMARK);
		filter.addAction(DatabaseManager.ACTION_REMOVE_BOOKMARKS);
		mBroadcastManager.registerReceiver(bookmarksReceiver, filter);
	}

	private void unregisterReceivers() {
		mBroadcastManager.unregisterReceiver(scheduleRefreshedReceiver);
		mBroadcastManager.unregisterReceiver(bookmarksReceiver);
	}

	private void startUpdateAlarms(Context context) {
		JobIntentService.enqueueWork(context, AlarmIntentService.class, AlarmIntentService.JOB_ID, new Intent(AlarmIntentService.ACTION_UPDATE_ALARMS));
	}

	private void startDisableAlarms(Context context) {
		JobIntentService.enqueueWork(context, AlarmIntentService.class, AlarmIntentService.JOB_ID, new Intent(AlarmIntentService.ACTION_DISABLE_ALARMS));
	}
}
