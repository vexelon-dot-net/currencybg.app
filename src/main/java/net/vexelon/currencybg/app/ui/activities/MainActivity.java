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

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
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
import net.vexelon.currencybg.app.services.RateService;
import net.vexelon.currencybg.app.ui.events.Notifications;
import net.vexelon.currencybg.app.ui.events.NotificationsListener;
import net.vexelon.currencybg.app.ui.fragments.AbstractFragment;
import net.vexelon.currencybg.app.ui.fragments.ConvertFragment;
import net.vexelon.currencybg.app.ui.fragments.CurrenciesFragment;

public class MainActivity extends AppCompatActivity implements NotificationsListener {

	private DrawerLayout drawerLayout;
	private NavigationView drawerNav;
	private ActionBarDrawerToggle drawerToggle;
	private Toolbar toolbar;
	private Menu menu;
	private PendingIntent pendingIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerNav = (NavigationView) findViewById(R.id.navView);
		setupDrawerContent(drawerNav);
		drawerToggle = setupDrawerToggle();

		// load default values
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// Start Service
		startService();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch(item.getItemId()) {
			case android.R.id.home:
				drawerLayout.openDrawer(GravityCompat.START);
				break;
			case R.id.action_settings:
				Intent intent = new Intent(this, PrefsActivity.class);
				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private ActionBarDrawerToggle setupDrawerToggle() {
		return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close);
	}

	private void setupDrawerContent(NavigationView navigationView) {
		navigationView.setNavigationItemSelectedListener(
				new NavigationView.OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(MenuItem menuItem) {
						selectDrawerItem(menuItem, getClassFromMenu(menuItem));
						return true;
					}
				});
	}

	private Class getClassFromMenu(MenuItem menuItem) {
		switch(menuItem.getItemId()) {
			case R.id.nav_convert:
				return ConvertFragment.class;
			case R.id.nav_currencies:
			default:
				return CurrenciesFragment.class;
		}
	}

	/**
	 * Create a new fragment and specify the planet to show based on position.
	 *
	 * @param menuItem
	 */
	public <T extends AbstractFragment> void selectDrawerItem(MenuItem menuItem, Class<T> clazz) {
		AbstractFragment fragment = null;
		try {
			fragment = clazz.newInstance();
			fragment.addListener(MainActivity.this);
//			Bundle args = new Bundle();
//			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//			fragment.setArguments(args);

			// Insert the fragment by replacing any existing fragment
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

			// Highlight the selected item, update the title, and close the drawer
			menuItem.setChecked(true);
			setTitle(menuItem.getTitle());
			drawerLayout.closeDrawers();
		} catch (Exception e) {
			Log.e(Defs.LOG_TAG, "Unknown drawer section!", e);
		}
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
	 * Starts background currencies update service
	 *
	 */
	public void startService() {
		Intent myIntent = new Intent(MainActivity.this, RateService.class);
		pendingIntent = PendingIntent.getService(MainActivity.this, 0, myIntent, 0);
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, 30);
		long initialStartTimeout = calendar.getTimeInMillis();
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, initialStartTimeout, Defs.NOTIFY_INTERVAL,
				pendingIntent);
	}

	public void cancelService() {
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
	}

}
