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
package net.vexelon.currencybg.app.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.utils.DateTimeUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SQLiteDataSource implements DataSource {

	private static final String[] ALL_COLUMNS = { Defs.COLUMN_ID, Defs.COLUMN_CODE, Defs.COLUMN_RATIO, Defs.COLUMN_BUY,
			Defs.COLUMN_SELL, Defs.COLUMN_CURR_DATE, Defs.COLUMN_SOURCE };

	private static final String DATE_FORMAT = "yyyy-MM-dd";
	// private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mmZ";

	private SQLiteDatabase database;
	private CurrenciesSQLiteDB dbHelper;

	@Override
	public void connect(Context context) throws DataSourceException {
		try {
			dbHelper = new CurrenciesSQLiteDB(context);
			database = dbHelper.getWritableDatabase();
		} catch (SQLException e) {
			throw new DataSourceException("Could not open SQLite database!", e);
		}
	}

	@Override
	public void close() {
		if (dbHelper != null) {
			dbHelper.close();
		}
	}

	private void closeCursor(Cursor cursor) {
		if (cursor != null) {
			cursor.close();
		}
	}

	@Override
	public void addRates(List<CurrencyData> rates) throws DataSourceException {
		ContentValues values = new ContentValues();

		for (CurrencyData rate : rates) {
			values.put(Defs.COLUMN_CODE, rate.getCode());
			values.put(Defs.COLUMN_RATIO, rate.getRatio());
			values.put(Defs.COLUMN_BUY, rate.getBuy());
			values.put(Defs.COLUMN_SELL, rate.getSell());
			// values.put(Defs.COLUMN_CURR_DATE,
			// DateTimeUtils.parseDateToString(rate.getDate(),
			// Defs.DATEFORMAT_ISO8601));
			values.put(Defs.COLUMN_CURR_DATE, rate.getDate());
			values.put(Defs.COLUMN_SOURCE, rate.getSource());

			database.insert(Defs.TABLE_CURRENCY, null, values);

			values = new ContentValues();
		}
	}

	@Override
	public void deleteRates(int backDays) throws DataSourceException {
		try {
			// '2016-11-19T20:48:29.022+02:00'
			database.execSQL("DELETE FROM currencies WHERE strftime('%s', curr_date) < strftime('%s', '"
					+ DateTimeUtils.getOldDate(backDays) + "' )");
		} catch (SQLException e) {
			throw new DataSourceException("SQL statement error!", e);
		}
	}

	@Override
	public List<CurrencyData> getLastRates() throws DataSourceException {
		Map<String, CurrencyData> result = Maps.newHashMap();
		Cursor cursor = null;

		try {
			cursor = database.rawQuery("SELECT * FROM currencies ORDER BY strftime('%s', curr_date)  ASC; ", null);

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				CurrencyData currency = cursorToCurrency(cursor);
				result.put(currency.getCode() + currency.getSource(), currency);

				cursor.moveToNext();
			}
		} catch (Throwable t) {
			throw new DataSourceException("SQL error: Failed fetching last rates!", t);
		} finally {
			closeCursor(cursor);
		}

		return Lists.newArrayList(result.values());
	}

	@Override
	public List<CurrencyData> getAllCurrencies(Integer source) throws DataSourceException {
		Map<String, CurrencyData> result = Maps.newHashMap();
		Cursor cursor = null;

		try {
			cursor = database.rawQuery(
					"SELECT * FROM currencies WHERE " + Defs.COLUMN_SOURCE
							+ " = ? ORDER BY strftime('%s', curr_date)  ASC; ",
					new String[] { String.valueOf(source) });

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				CurrencyData currency = cursorToCurrency(cursor);
				result.put(currency.getCode(), currency);

				cursor.moveToNext();
			}
		} catch (Throwable t) {
			throw new DataSourceException("SQL error: Failed fetching all currencies for source " + source, t);
		} finally {
			closeCursor(cursor);
		}

		return Lists.newArrayList(result.values());
	}

	@Override
	public List<CurrencyData> getAllRates(String code) throws DataSourceException {
		List<CurrencyData> result = Lists.newArrayList();
		Cursor cursor = null;

		try {
			cursor = database.rawQuery("SELECT * FROM currencies WHERE " + Defs.COLUMN_CODE
					+ " = ? ORDER BY strftime('%s', curr_date)  ASC; ", new String[] { String.valueOf(code) });

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				CurrencyData currency = cursorToCurrency(cursor);
				result.add(currency);

				cursor.moveToNext();
			}
		} catch (Throwable t) {
			throw new DataSourceException("SQL error: Failed fetching all rates!", t);
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	private CurrencyData cursorToCurrency(Cursor cursor) {
		CurrencyData currency = new CurrencyData();

		currency.setCode(cursor.getString(cursor.getColumnIndex(Defs.COLUMN_CODE)));
		currency.setRatio(cursor.getInt(cursor.getColumnIndex(Defs.COLUMN_RATIO)));
		currency.setBuy(cursor.getString(cursor.getColumnIndex(Defs.COLUMN_BUY)));
		currency.setSell(cursor.getString(cursor.getColumnIndex(Defs.COLUMN_SELL)));
		currency.setDate(cursor.getString(cursor.getColumnIndex(Defs.COLUMN_CURR_DATE)));
		currency.setSource(cursor.getInt(cursor.getColumnIndex(Defs.COLUMN_SOURCE)));

		return currency;
	}

}
