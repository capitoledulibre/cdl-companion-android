package org.toulibre.capitoledulibre.utils;

import android.app.Activity;
import android.content.Intent;

/**
 * NFC helper methods compatible with all API levels.
 *
 * @author Christophe Beyls
 */
public class NfcUtils {

    /**
     * Implement this interface to create application-specific data to be shared through Android Beam.
     */
    public interface CreateNfcAppDataCallback {
        /**
         * @return The app data, or null if no data is currently available for sharing.
         */
        byte[] createNfcAppData();
    }

    /**
     * Call this method in an Activity, between onCreate() and onDestroy(), to make its content sharable using Android Beam if available. MIME type of the data
     * to share will be "application/" followed by the app's package name. Declare it in your Manifest's intent filters as the data type with an action of
     * android.nfc.action.NDEF_DISCOVERED to handle the NFC Intents on the receiver side.
     *
     * @param activity
     * @param callback
     * @return true if NFC is available and the content was made available, false if not.
     */
    public static boolean setAppDataPushMessageCallbackIfAvailable(Activity activity, final CreateNfcAppDataCallback callback) {
        return NfcSenderUtils.setAppDataPushMessageCallbackIfAvailable(activity, callback);
    }

    /**
     * Determines if the intent contains NFC NDEF application-specific data to be extracted.
     *
     * @param intent
     * @return
     */
    public static boolean hasAppData(Intent intent) {
        return NfcReceiverUtils.hasAppData(intent);
    }

    /**
     * Extracts application-specific data sent through NFC from an intent. You must first ensure that the intent contains NFC data by calling hasAppData().
     *
     * @param intent
     * @return The extracted data
     */
    public static byte[] extractAppData(Intent intent) {
        return NfcReceiverUtils.extractAppData(intent);
    }
}
