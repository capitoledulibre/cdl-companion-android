package org.toulibre.capitoledulibre.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.toulibre.capitoledulibre.R;
import org.toulibre.capitoledulibre.utils.WebUtils;
import org.toulibre.capitoledulibre.utils.customtabs.CustomTabsHelperFragment;

import java.util.Locale;

public class MapFragment extends Fragment {

    private static final double DESTINATION_LATITUDE = 43.6020423;

    private static final double DESTINATION_LONGITUDE = 1.45222;

    private static final String NATIVE_URI = "google.navigation:q=%1$f,%2$f&mode=d";

    //private static final Uri WEB_URI_GOOGLE = Uri.parse("https://goo.gl/maps/atYtpUZCouy");
    private static final Uri WEB_URI = Uri.parse("https://www.openstreetmap.org/way/22634781");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CustomTabsHelperFragment.attach(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.directions:
                launchDirections();
                return true;
        }
        return false;
    }

    private void launchDirections() {
        Uri uri = Uri.parse(String.format(Locale.US, NATIVE_URI, DESTINATION_LATITUDE, DESTINATION_LONGITUDE));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            WebUtils.openWebLink(getActivity(), WEB_URI);
        }
    }
}
