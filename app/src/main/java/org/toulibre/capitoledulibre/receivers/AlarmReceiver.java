package org.toulibre.capitoledulibre.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.JobIntentService;

import org.toulibre.capitoledulibre.BuildConfig;
import org.toulibre.capitoledulibre.alarms.AlarmManager;
import org.toulibre.capitoledulibre.services.AlarmIntentService;

/**
 * Entry point for system-generated events: boot complete and alarms.
 *
 * @author Christophe Beyls
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_NOTIFY_EVENT = BuildConfig.APPLICATION_ID + ".action.NOTIFY_EVENT";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ACTION_NOTIFY_EVENT.equals(action)) {
            // Forward the intent to the AlarmIntentService for background processing of the notification
            Intent serviceIntent = new Intent(ACTION_NOTIFY_EVENT)
                    .setData(intent.getData())
                    .putExtra(AlarmIntentService.EXTRA_WITH_WAKE_LOCK, true);
            JobIntentService.enqueueWork(context, AlarmIntentService.class, AlarmIntentService.JOB_ID, serviceIntent);

        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            String serviceAction = AlarmManager.getInstance(context).isEnabled()
                    ? AlarmIntentService.ACTION_UPDATE_ALARMS : AlarmIntentService.ACTION_DISABLE_ALARMS;
            Intent serviceIntent = new Intent(serviceAction)
                    .putExtra(AlarmIntentService.EXTRA_WITH_WAKE_LOCK, true);
            JobIntentService.enqueueWork(context, AlarmIntentService.class, AlarmIntentService.JOB_ID, serviceIntent);
        }
    }

}
