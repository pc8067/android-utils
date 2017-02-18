package com.utilsframework.android.navdrawer;

import android.support.v4.app.Fragment;

public abstract class NoTabsFragmentFactory implements FragmentFactory {
    @Override
    public Fragment createFragmentBySelectedItem(int selectedItemId, int tabIndex, int navigationLevel) {
        return createFragmentBySelectedItem(selectedItemId);
    }

    @Override
    public void initTab(int currentSelectedItem, int tabIndex, int navigationLevel, TabsAdapter.Tab tab) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTabsCount(int selectedItemId, int navigationLevel) {
        return 1;
    }

    public abstract Fragment createFragmentBySelectedItem(int selectedItem);
}
