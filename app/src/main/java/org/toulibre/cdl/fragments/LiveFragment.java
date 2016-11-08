package org.toulibre.cdl.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.toulibre.cdl.R;
import org.toulibre.cdl.widgets.SlidingTabLayout;

public class LiveFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_live, container, false);

		ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
		pager.setAdapter(new LivePagerAdapter(getChildFragmentManager(), getResources()));
		SlidingTabLayout slidingTabs = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
		slidingTabs.setViewPager(pager);

		return view;
	}

	private static class LivePagerAdapter extends FragmentPagerAdapter {

		private final Resources resources;

		public LivePagerAdapter(FragmentManager fm, Resources resources) {
			super(fm);
			this.resources = resources;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					return new NextLiveListFragment();
				case 1:
					return new NowLiveListFragment();
			}
			return null;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return resources.getString(R.string.next);
				case 1:
					return resources.getString(R.string.now);
			}
			return null;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// Allow the non-primary fragments to start as soon as they are visible
			Fragment f = (Fragment) super.instantiateItem(container, position);
			f.setUserVisibleHint(true);
			return f;
		}
	}
}
