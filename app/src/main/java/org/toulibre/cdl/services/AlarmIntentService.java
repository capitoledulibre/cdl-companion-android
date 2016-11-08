package org.toulibre.cdl.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.StyleSpan;

import org.toulibre.cdl.R;
import org.toulibre.cdl.activities.EventDetailsActivity;
import org.toulibre.cdl.activities.MainActivity;
import org.toulibre.cdl.db.DatabaseManager;
import org.toulibre.cdl.fragments.SettingsFragment;
import org.toulibre.cdl.model.Event;
import org.toulibre.cdl.receivers.AlarmReceiver;

/**
 * A service to schedule or unschedule alarms in the background, keeping the app responsive.
 *
 * @author Christophe Beyls
 */
public class AlarmIntentService extends IntentService {

	public static final String ACTION_UPDATE_ALARMS = "org.toulibre.cdl.action.UPDATE_ALARMS";
	public static final String EXTRA_WITH_WAKE_LOCK = "with_wake_lock";
	public static final String ACTION_DISABLE_ALARMS = "org.toulibre.cdl.action.DISABLE_ALARMS";

	private AlarmManager alarmManager;

	public AlarmIntentService() {
		super("AlarmIntentService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// Ask for the last unhandled intents to be redelivered if the service dies early.
		// This ensures we handle all events, in order.
		setIntentRedelivery(true);

		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	}

	private PendingIntent getAlarmPendingIntent(long eventId) {
		Intent intent = new Intent(this, AlarmReceiver.class)
				.setAction(AlarmReceiver.ACTION_NOTIFY_EVENT)
				.setData(Uri.parse(String.valueOf(eventId)));
		return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		switch (intent.getAction()) {

			case ACTION_UPDATE_ALARMS: {

				// Create/update all alarms
				long delay = getDelay();
				long now = System.currentTimeMillis();
				Cursor cursor = DatabaseManager.getInstance().getBookmarks(now);
				try {
					while (cursor.moveToNext()) {
						long eventId = DatabaseManager.toEventId(cursor);
						long notificationTime = DatabaseManager.toEventStartTimeMillis(cursor) - delay;
						PendingIntent pi = getAlarmPendingIntent(eventId);
						if (notificationTime < now) {
							// Cancel pending alarms that where scheduled between now and delay, if any
							alarmManager.cancel(pi);
						} else {
							setExactAlarm(alarmManager, AlarmManager.RTC_WAKEUP, notificationTime, pi);
						}
					}
				} finally {
					cursor.close();
				}

				// Release the wake lock setup by AlarmReceiver, if any
				if (intent.getBooleanExtra(EXTRA_WITH_WAKE_LOCK, false)) {
					AlarmReceiver.completeWakefulIntent(intent);
				}

				break;
			}
			case ACTION_DISABLE_ALARMS: {

				// Cancel alarms of every bookmark in the future
				Cursor cursor = DatabaseManager.getInstance().getBookmarks(System.currentTimeMillis());
				try {
					while (cursor.moveToNext()) {
						long eventId = DatabaseManager.toEventId(cursor);
						alarmManager.cancel(getAlarmPendingIntent(eventId));
					}
				} finally {
					cursor.close();
				}

				break;
			}
			case DatabaseManager.ACTION_ADD_BOOKMARK: {

				long delay = getDelay();
				long eventId = intent.getLongExtra(DatabaseManager.EXTRA_EVENT_ID, -1L);
				long startTime = intent.getLongExtra(DatabaseManager.EXTRA_EVENT_START_TIME, -1L);
				// Only schedule future events. If they start before the delay, the alarm will go off immediately
				if ((startTime == -1L) || (startTime < System.currentTimeMillis())) {
					break;
				}
				setExactAlarm(alarmManager, AlarmManager.RTC_WAKEUP, startTime - delay, getAlarmPendingIntent(eventId));

				break;
			}
			case DatabaseManager.ACTION_REMOVE_BOOKMARKS: {

				// Cancel matching alarms, might they exist or not
				long[] eventIds = intent.getLongArrayExtra(DatabaseManager.EXTRA_EVENT_IDS);
				for (long eventId : eventIds) {
					alarmManager.cancel(getAlarmPendingIntent(eventId));
				}

				break;
			}
			case AlarmReceiver.ACTION_NOTIFY_EVENT: {

				long eventId = Long.parseLong(intent.getDataString());
				Event event = DatabaseManager.getInstance().getEvent(eventId);
				if (event != null) {
					PendingIntent eventPendingIntent = TaskStackBuilder
							.create(this)
							.addNextIntent(new Intent(this, MainActivity.class))
							.addNextIntent(
									new Intent(this, EventDetailsActivity.class).setData(Uri.parse(String.valueOf(event
											.getId())))).getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

					int defaultFlags = Notification.DEFAULT_SOUND;
					SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
					if (sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFICATIONS_VIBRATE, false)) {
						defaultFlags |= Notification.DEFAULT_VIBRATE;
					}

					String personsSummary = event.getPersonsSummary();
					String trackName = event.getTrack().getName();
					String contentText;
					CharSequence bigText;
					if (TextUtils.isEmpty(personsSummary)) {
						contentText = trackName;
						bigText = event.getSubTitle();
					} else {
						contentText = String.format("%1$s - %2$s", trackName, personsSummary);
						String subTitle = event.getSubTitle();
						SpannableString spannableBigText;
						if (TextUtils.isEmpty(subTitle)) {
							spannableBigText = new SpannableString(personsSummary);
						} else {
							spannableBigText = new SpannableString(String.format("%1$s\n%2$s", subTitle, personsSummary));
						}
						// Set the persons summary in italic
						spannableBigText.setSpan(new StyleSpan(Typeface.ITALIC),
								spannableBigText.length() - personsSummary.length(), spannableBigText.length(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						bigText = spannableBigText;
					}

					int notificationColor = ContextCompat.getColor(this, R.color.color_primary);

					NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
							.setSmallIcon(R.drawable.notif_icon)
							.setColor(notificationColor)
							.setWhen(event.getStartTime().getTime())
							.setContentTitle(event.getTitle())
							.setContentText(contentText)
							.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText).setSummaryText(trackName))
							.setContentInfo(event.getRoomName())
							.setContentIntent(eventPendingIntent)
							.setAutoCancel(true)
							.setDefaults(defaultFlags)
							.setPriority(NotificationCompat.PRIORITY_HIGH)
							.setCategory(NotificationCompat.CATEGORY_EVENT);

					// Blink the LED with CDL color if enabled in the options
					if (sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFICATIONS_LED, false)) {
						notificationBuilder.setLights(notificationColor, 1000, 5000);
					}

					// Android Wear extensions
					NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();

					notificationBuilder.extend(wearableExtender);

					NotificationManagerCompat.from(this).notify((int) eventId, notificationBuilder.build());
				}

				AlarmReceiver.completeWakefulIntent(intent);
				break;
			}
		}
	}

	private static void setExactAlarm(AlarmManager manager, int type, long triggerAtMillis, PendingIntent operation) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			manager.setExact(type, triggerAtMillis, operation);
		} else {
			manager.set(type, triggerAtMillis, operation);
		}
	}

	private long getDelay() {
		String delayString = PreferenceManager.getDefaultSharedPreferences(this).getString(
				SettingsFragment.KEY_PREF_NOTIFICATIONS_DELAY, "0");
		// Convert from minutes to milliseconds
		return Long.parseLong(delayString) * DateUtils.MINUTE_IN_MILLIS;
	}
}
