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
package net.vexelon.currencybg.app.services;

import java.util.List;
import java.util.TimeZone;

import com.google.common.collect.Lists;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.db.DataSource;
import net.vexelon.currencybg.app.db.DataSourceException;
import net.vexelon.currencybg.app.db.SQLiteDataSource;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.remote.APISource;
import net.vexelon.currencybg.app.remote.SourceException;
import net.vexelon.currencybg.app.utils.DateTimeUtils;
import net.vexelon.currencybg.app.utils.IOUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class BackgroundService extends Service {

	@Override
	public void onCreate() {
		// do nothing
	}

	@Override
	public IBinder onBind(Intent intent) {
		// do nothing
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new DownloadTask().execute();

		// allow service to be killed under high load
		return START_NOT_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	private class DownloadTask extends AsyncTask<Void, Void, Void> {

		private DateTime lastUpdate;
		private boolean updateOK = false;

		@Override
		protected void onPreExecute() {
			lastUpdate = new AppSettings(BackgroundService.this).getLastUpdateDate();
		}

		@Override
		protected Void doInBackground(Void... params) {
			Log.v(Defs.LOG_TAG, "[Service] Downloading rates from remote source...");

			DataSource source = null;
			List<CurrencyData> currencies = Lists.newArrayList();

			try {
				String iso8601Time = lastUpdate.toString();
				Log.d(Defs.LOG_TAG, "[Service] Downloading all rates since " + iso8601Time + " onwards...");

				// format, e.g., "2016-11-09T01:00:06+03:00"
				currencies = new APISource().getAllCurrentRatesAfter(iso8601Time);

				source = new SQLiteDataSource();
				source.connect(BackgroundService.this);
				source.addRates(currencies);

				Log.d(Defs.LOG_TAG, "[Service] Cleaning up currency rates older than 3 days ...");
				source.deleteRates(Defs.SERVICE_DATABASE_CLEAN_INTERVAL);

				updateOK = true;
			} catch (SourceException e) {
				Log.e(Defs.LOG_TAG, "[Service] Error fetching currencies from remote!", e);
			} catch (DataSourceException e) {
				Log.e(Defs.LOG_TAG, "[Service] Could not save currencies to database!", e);
			} finally {
				IOUtils.closeQuitely(source);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			if (updateOK) {
				// bump last update
				lastUpdate = DateTime.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone(Defs.DATE_TIMEZONE_SOFIA)));
				Log.d(Defs.LOG_TAG, "[Service] Last rate download on " + lastUpdate.toString());
				new AppSettings(BackgroundService.this).setLastUpdateDate(lastUpdate);

				// notify main fragment
				Intent intent = new Intent(Defs.SERVICE_ACTION_NOTIFY_UPDATE);
				intent.putExtra("LAST_UPDATE", DateTimeUtils.toDateText(BackgroundService.this, lastUpdate.toDate()));
				sendBroadcast(intent);
			}
		}
	}
}
