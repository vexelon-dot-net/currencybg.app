/*
 * CurrencyBG App
 * Copyright (C) 2016 Vexelon.NET Services
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.vexelon.currencybg.app.ui.activities;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.services.BackgroundUpdateService;
import net.vexelon.currencybg.app.ui.events.Notifications;
import net.vexelon.currencybg.app.ui.events.NotificationsListener;
import net.vexelon.currencybg.app.ui.fragments.AbstractFragment;
import net.vexelon.currencybg.app.ui.fragments.ConvertFragment;
import net.vexelon.currencybg.app.ui.fragments.CurrenciesFragment;
import net.vexelon.currencybg.app.ui.fragments.ExchangeInfoFragment;
import net.vexelon.currencybg.app.ui.fragments.InfoFragment;
import net.vexelon.currencybg.app.ui.fragments.PrefsFragment;
import net.vexelon.currencybg.app.ui.fragments.WalletFragment;

public class MainActivity extends AppCompatActivity implements NotificationsListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private PendingIntent pendingIntent;
    private BroadcastReceiver receiver;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView drawerNav = (NavigationView) findViewById(R.id.navView);
        setupDrawerContent(drawerNav);
        drawerToggle = setupDrawerToggle();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        startService();
        startReceivers();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();

        if (savedInstanceState == null) {
            try {
                currentFragment = showFragment(CurrenciesFragment.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        cancelReceivers();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        final Context context = this;
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.nav_settings) {
                    // special case -> settings
                    PreferenceFragment fragment = new PrefsFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit();
                    menuItem.setChecked(true);
                    setTitle(menuItem.getTitle());
                    drawerLayout.closeDrawers();
                } else {
                    selectDrawerItem(menuItem, getClassFromMenu(menuItem));
                }
                return true;
            }
        });
    }

    private Class getClassFromMenu(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.nav_convert) {
            return ConvertFragment.class;
        } else if (itemId == R.id.nav_wallet) {
            return WalletFragment.class;
        } else if (itemId == R.id.nav_exchanges_info) {
            return ExchangeInfoFragment.class;
        } else if (itemId == R.id.nav_info) {
            return InfoFragment.class;
        } else if (itemId == R.id.nav_settings) {
            return PrefsFragment.class;
        }
        return CurrenciesFragment.class;
    }

    /**
     * Create a new fragment and specify the planet to show based on position.
     *
     * @param menuItem
     * @param clazz
     */
    private <T extends AbstractFragment> void selectDrawerItem(MenuItem menuItem, Class<T> clazz) {
        try {
            currentFragment = showFragment(clazz);
            /*
             * Highlight the selected item, update the title, and close the
             * drawer
             */
            menuItem.setChecked(true);
            setTitle(menuItem.getTitle());
            drawerLayout.closeDrawers();
        } catch (Exception e) {
            Log.e(Defs.LOG_TAG, "Unknown drawer section!", e);
        }
    }

    private <T extends AbstractFragment> AbstractFragment showFragment(Class<T> clazz) throws Exception {
        AbstractFragment fragment = clazz.newInstance();
        // fragment.addListener(MainActivity.this);
        // Bundle args = new Bundle();
        // args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        // fragment.setArguments(args);
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit();
        return fragment;
    }

    @Override
    public void onNotification(Notifications event) {
        switch (event) {
            case UPDATE_RATES_DONE:
                // setRefreshActionButtonState(false);
                break;
        }
    }

    /**
     * Starts background service
     */
    public void startService() {
        Intent myIntent = new Intent(MainActivity.this, BackgroundUpdateService.class);
        pendingIntent = PendingIntent.getService(MainActivity.this, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        /*
         * First run timeout
         */
        long startTimeout = System.currentTimeMillis() + Defs.SERVICE_FIRST_RUN_INTERVAL;

        /**
         * Align service interval to 1/2 a day for devices with API < 19
         */
        long period = Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ? AlarmManager.INTERVAL_HALF_DAY
                : Defs.SERVICE_PERIODIC_INTERVAL;

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC, startTimeout, period, pendingIntent);
    }

    public void cancelService() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    /**
     * Registers receivers for service sent intents
     */
    public void startReceivers() {
        receiver = new Receiver();
        registerReceiver(receiver, new IntentFilter(Defs.SERVICE_ACTION_NOTIFY_UPDATE));
    }

    public void cancelReceivers() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    /**
     * Processes actions sent by the background service
     */
    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Defs.SERVICE_ACTION_NOTIFY_UPDATE.equals(intent.getAction())) {
                /**
                 * Set last update time
                 */
                if (currentFragment instanceof CurrenciesFragment) {
                    ((CurrenciesFragment) currentFragment).setLastUpdate(intent.getStringExtra("LAST_UPDATE"));
                }
            }
        }
    }

}