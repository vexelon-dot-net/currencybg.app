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

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import android.app.Service;
import android.content.Context;
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
import net.vexelon.currencybg.app.common.CurrencyLocales;
import net.vexelon.currencybg.app.remote.Source;
import net.vexelon.currencybg.app.remote.SourceException;
import net.vexelon.currencybg.app.utils.DateTimeUtils;
import net.vexelon.currencybg.app.utils.IOUtils;

public class RateService extends Service {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		// Toast.makeText(this, "MyAlarmService.onCreate()",
		// Toast.LENGTH_LONG).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		// Toast.makeText(this, "MyAlarmService.onBind()",
		// Toast.LENGTH_LONG).show();
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Toast.makeText(this, "MyAlarmService.onDestroy()",
		// Toast.LENGTH_LONG).show();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		// Toast.makeText(this, "MyAlarmService.onStart()",
		// Toast.LENGTH_LONG).show();
		boolean isFixedCurrenciesOK = isFixedCurrenciesToYear();
		if (!isCurrenciesToDate() || !isFixedCurrenciesOK) {
			new DownloadWebpageTask().execute(!isFixedCurrenciesOK);
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// Toast.makeText(this, "MyAlarmService.onUnbind()",
		// Toast.LENGTH_LONG).show();
		return super.onUnbind(intent);
	}

	/**
	 * Checks whether have for sysdate currencies.
	 * 
	 * @return: true-Have, false-Haven't
	 */
	private boolean isCurrenciesToDate() {
		Context ctx = RateService.this;
		DataSource source = null;
		List<CurrencyData> listCurrency = null;
		try {
			source = new SQLiteDataSource();
			source.connect(ctx);
			// TODO - temporary commented
			// listCurrency = source.getRates(getSelectedCurrenciesLocale(),
			// Calendar.getInstance().getTime());
			return listCurrency.size() > 0;
		} catch (DataSourceException e) {
			Log.e(Defs.LOG_TAG, "Could not load currencies from database!", e);
		} finally {
			IOUtils.closeQuitely(source);
		}
		return false;
	}

	/**
	 * Checks whether has currencies for current year.
	 *
	 * @return: true-Have, false-Haven't
	 */

	private boolean isFixedCurrenciesToYear() {
		Date currentYear = DateTimeUtils.getCurrentYear();
		Context ctx = RateService.this;
		DataSource source = null;
		List<CurrencyData> listFixedCurrency = null;
		try {
			source = new SQLiteDataSource();
			source.connect(ctx);
			// TODO - temporary commented
			// listFixedCurrency =
			// source.getFixedRates(getSelectedCurrenciesLocale(), currentYear);
			return listFixedCurrency.size() > 0;
		} catch (DataSourceException e) {
			Log.e(Defs.LOG_TAG, "Could not load fixed currencies from database!", e);
		} finally {
			IOUtils.closeQuitely(source);
		}
		return false;
	}

	private class DownloadWebpageTask extends AsyncTask<Object, Void, Map<CurrencyLocales, List<CurrencyData>>> {

		@Override
		protected Map<CurrencyLocales, List<CurrencyData>> doInBackground(Object... param) {
			Map<CurrencyLocales, List<CurrencyData>> rates = Maps.newHashMap();
			boolean isFixed = (Boolean) param[0];
			// for(Object object : param){
			// isFixed = (Boolean)object;
			// }
//			try {
//				Log.v(Defs.LOG_TAG, "Loading rates from remote source...");
				// Source source = new BNBSource();
				// rates = source.downloadRates(isFixed);
			// } catch (SourceException e) {
			// Log.e(Defs.LOG_TAG, "Could not load rates from remote!", e);
			// }
			return rates;
		}

		/**
		 * Add currencies in DB, if not existing
		 * 
		 * @param result
		 */
		@Override
		protected void onPostExecute(Map<CurrencyLocales, List<CurrencyData>> result) {
			Context ctx = RateService.this;
			DataSource source = null;
			try {
				source = new SQLiteDataSource();
				source.connect(ctx);
				// TODO - temporary commented
				// source.addRates(result);
			} catch (DataSourceException e) {
				Log.e(Defs.LOG_TAG, "Could not save currencies to database!", e);
			} finally {
				IOUtils.closeQuitely(source);
			}

		}
	}

	protected CurrencyLocales getSelectedCurrenciesLocale() {
		Context ctx = RateService.this;
		return new AppSettings(ctx).getCurrenciesLanguage();
	}
}
