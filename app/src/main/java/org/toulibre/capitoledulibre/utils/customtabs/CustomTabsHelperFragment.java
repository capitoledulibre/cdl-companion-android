package org.toulibre.capitoledulibre.utils.customtabs;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Headless fragment for custom tabs
 *
 * @author valentin
 */
public class CustomTabsHelperFragment extends Fragment {

    private static final String TAG = "CustomTabsHelperFragmen";

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    public static void attach(final FragmentActivity activity) {
        if (activity != null
                && activity.getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            sMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .add(new CustomTabsHelperFragment(), TAG)
                            .commit();
                }
            });
        }
    }

    public static CustomTabActivityHelper getCustomTabsActivityHelper(Activity activity) {
        if (activity != null && activity instanceof FragmentActivity) {
            CustomTabsHelperFragment fragment
                    = (CustomTabsHelperFragment) ((FragmentActivity) activity)
                    .getSupportFragmentManager().findFragmentByTag(TAG);
            if (fragment != null) {
                return fragment.mCustomTabActivityHelper;
            }
        }
        return null;
    }

    private CustomTabActivityHelper mCustomTabActivityHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCustomTabActivityHelper = new CustomTabActivityHelper();
    }

    @Override
    public void onStart() {
        super.onStart();
        mCustomTabActivityHelper.bindCustomTabsService(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        mCustomTabActivityHelper.unbindCustomTabsService(getActivity());
    }
}
