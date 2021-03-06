package com.utilsframework.android.navdrawer;

import android.support.v4.app.Fragment;

/**
 * Created by CM on 2/20/2015.
 */
public class NavigationDrawerFragment extends Fragment {
    public NavigationActivity getNavigationActivity() {
        return (NavigationActivity) getActivity();
    }

    public void replaceFragment(Fragment newFragment, int navigationLevel) {
        getNavigationActivity().replaceFragment(newFragment, navigationLevel);
    }

    public void updateActionBarTitle() {
        getNavigationActivity().updateActionBarTitle();
    }
}
