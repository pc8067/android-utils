package com.utilsframework.android.navigation;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import com.utils.framework.Predicate;
import com.utilsframework.android.R;
import com.utilsframework.android.fragments.Fragments;
import com.utilsframework.android.view.GuiUtilities;

import java.util.List;

/**
 * Created by CM on 12/26/2014.
 */
public abstract class NavigationFragmentDrawer {
    private final Activity activity;
    private DrawerLayout drawerLayout;
    private FragmentFactory fragmentFactory;
    private ViewGroup navigationLayout;
    private int currentSelectedItem;
    private int navigationLevel = 0;
    private ActionBarDrawerToggle drawerToggle;

    private void selectFragment(int viewId, int navigationLevel, int tabIndex, boolean createFragment) {
        if(viewId == currentSelectedItem && navigationLevel == this.navigationLevel){
            return;
        }

        int tabsCount = fragmentFactory.getTabsCount(viewId, navigationLevel);
        if (tabsCount <= 1) {
            if (viewId != currentSelectedItem) {
                Fragments.clearBackStack(activity);
                Fragment fragment = fragmentFactory.createFragmentBySelectedItem(viewId, 0, navigationLevel);
                Fragments.replaceFragment(activity, getContentId(), fragment);
            }
            activity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        } else {
            initTabs(tabsCount, viewId, navigationLevel, createFragment, tabIndex);
        }

        hide();

        this.currentSelectedItem = viewId;
        this.navigationLevel = navigationLevel;
    }

    private void initTabs(int tabsCount,
                          final int viewId,
                          final int navigationLevel,
                          boolean createFragment,
                          int selectedTabIndex) {
        ActionBar actionBar = activity.getActionBar();
        if(actionBar == null){
            throw new NullPointerException("actionBar == null");
        }

        actionBar.removeAllTabs();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        for (int i = 0; i < tabsCount; i++) {
            ActionBar.Tab tab = actionBar.newTab();
            fragmentFactory.initTab(viewId, i, navigationLevel, tab);

            final int tabIndex = i;
            ActionBar.TabListener listener = new ActionBar.TabListener() {
                @Override
                public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                    if (viewId != currentSelectedItem) {
                        Fragments.clearBackStack(activity);
                    }
                    Fragment fragment =
                            fragmentFactory.createFragmentBySelectedItem(viewId, tabIndex,
                                    navigationLevel);
                    ft.replace(getContentId(), fragment);
                }

                @Override
                public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

                }

                @Override
                public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

                }
            };
            tab.setTabListener(listener);
            actionBar.addTab(tab);

            if (tabIndex == selectedTabIndex) {
                actionBar.selectTab(tab);
                if (createFragment) {
                    Fragment fragment =
                            fragmentFactory.createFragmentBySelectedItem(viewId, tabIndex, navigationLevel);
                    Fragments.replaceFragment(activity, getContentId(), fragment);
                }
            }
        }
    }

    protected void onNavigationItemClick(View view) {

    }

    public NavigationFragmentDrawer(final Activity activity,
                                    FragmentFactory fragmentFactory,
                                    int currentSelectedItem) {
        this.fragmentFactory = fragmentFactory;
        this.currentSelectedItem = currentSelectedItem;
        this.activity = activity;

        drawerLayout = (DrawerLayout) activity.findViewById(getDrawerLayoutId());
        navigationLayout = (ViewGroup) activity.findViewById(getNavigationLayoutId());

        List<View> navigationViews = getNavigationViews();

        for(View navigationView : navigationViews){
            navigationView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNavigationItemClick(v);
                    selectFragment(v.getId(), 0, 0, true);
                }
            });
        }

        if(navigationLayout.findViewById(currentSelectedItem) == null){
            throw new IllegalArgumentException("Could not find view with " +
                    currentSelectedItem + " id");
        }

        initDrawableToggle();
        int tabsCount = fragmentFactory.getTabsCount(currentSelectedItem, navigationLevel);
        if (tabsCount > 1) {
            initTabs(tabsCount, currentSelectedItem, navigationLevel, true, 0);
        } else {
            Fragments.addFragment(activity, getContentId(),
                    fragmentFactory.createFragmentBySelectedItem(currentSelectedItem, 0, navigationLevel));
            activity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
        onNavigationItemClick(activity.findViewById(currentSelectedItem));
    }

    private void initDrawableToggle() {
        drawerToggle = new ActionBarDrawerToggle(
                activity,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                getToggleIconResourceId(),  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                ActionBar actionBar = activity.getActionBar();
                if (actionBar != null) {
                    String title = getActionBarTitle(currentSelectedItem);
                    actionBar.setTitle(title);
                }
            }

            public void onDrawerOpened(View drawerView) {
                ActionBar actionBar = activity.getActionBar();
                if (actionBar != null) {
                    String title = getActionBarTitleWhenNavigationIsShown(currentSelectedItem);
                    actionBar.setTitle(title);
                }
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
    }

    private List<View> getNavigationViews() {
        return GuiUtilities.getAllChildrenRecursive(navigationLayout,
                new Predicate<View>() {
                    @Override
                    public boolean check(View item) {
                        if (item.getId() == View.NO_ID) {
                            return false;
                        }

                        if (item instanceof ViewGroup) {
                            ViewGroup viewGroup = (ViewGroup) item;
                            return viewGroup.getChildCount() == 0;
                        }

                        return true;
                    }
                });
    }

    public void show() {
        drawerLayout.openDrawer(navigationLayout);
    }

    public void hide() {
        drawerLayout.closeDrawer(navigationLayout);
    }

    protected abstract int getDrawerLayoutId();
    protected abstract int getContentId();
    protected abstract int getNavigationLayoutId();

    protected String getActionBarTitle(int currentSelectedItem) {
        return GuiUtilities.getApplicationName(activity);
    }

    protected String getActionBarTitleWhenNavigationIsShown(int currentSelectedItem) {
        return GuiUtilities.getApplicationName(activity);
    }

    protected int getToggleIconResourceId() {
        return R.drawable.ic_drawer;
    }

    private int getCurrentTabIndex() {
        ActionBar actionBar = activity.getActionBar();
        ActionBar.Tab selectedTab = actionBar.getSelectedTab();
        if(selectedTab == null){
            return 0;
        }

        return selectedTab.getPosition();
    }

    public void replaceFragment(Fragment newFragment, final int navigationLevel) {
        final int lastNavigationLevel = this.navigationLevel;
        final int tabIndex = getCurrentTabIndex();
        Fragments.replaceFragmentAndAddToBackStack(activity, R.id.content, newFragment, new Fragments.OnBack() {
            @Override
            public void onBack() {
                selectFragment(currentSelectedItem, lastNavigationLevel, tabIndex, false);
            }
        });
        selectFragment(currentSelectedItem, navigationLevel, 0, false);
    }
}
