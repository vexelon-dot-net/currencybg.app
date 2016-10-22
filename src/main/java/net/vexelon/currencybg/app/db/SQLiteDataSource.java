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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.db.models.CurrencyLocales;
import net.vexelon.currencybg.app.utils.DateTimeUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.google.common.collect.Lists;

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

	private Date parseStringToDate(String date, String format) throws ParseException {
		return new SimpleDateFormat(format).parse(date);
	}

	private String parseDateToString(Date date, String dateFormat) {
		return new SimpleDateFormat(dateFormat).format(date);
	}

	@Override
	public void addRates(List<CurrencyData> rates) throws DataSourceException {

		ContentValues values = new ContentValues();

		for (CurrencyData rate : rates) {

			values.put(Defs.COLUMN_CODE, rate.getCode());
			values.put(Defs.COLUMN_RATIO, rate.getRatio());
			values.put(Defs.COLUMN_BUY, rate.getBuy());
			values.put(Defs.COLUMN_SELL, rate.getSell());
			values.put(Defs.COLUMN_CURR_DATE,
					DateTimeUtils.parseDateToString(rate.getDate(), Defs.DATEFORMAT_ISO_8601));
			values.put(Defs.COLUMN_SOURCE, rate.getSource());

			database.insert(Defs.TABLE_CURRENCY, null, values);

			values = new ContentValues();
		}

	}

	@Override
	public void deleteRates() throws DataSourceException {
		database.delete(Defs.TABLE_CURRENCY, null, null);
	}

	@Override
	public List<CurrencyData> getLastRates()/* getAllCurrencies */ throws DataSourceException {
		List<CurrencyData> resultCurrency = Lists.newArrayList();

		// String whereClause = Defs.COLUMN_CURR_DATE + " = ? AND " +
		// Defs.COLUMN_LOCALE + " = ? ";
		// String[] whereArgs = new String[] { parseDateToString(dateOfCurrency,
		// DATE_FORMAT), locale.toString() };

		Cursor cursor = database.query(Defs.TABLE_CURRENCY, ALL_COLUMNS, null/* whereClause */, null/* whereArgs */,
				null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			CurrencyData comment = cursorToCurrency(cursor);
			resultCurrency.add(comment);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();

		System.out.println("DB METHOD: " + resultCurrency.get(1).getCode() + " " + resultCurrency.get(1).getBuy() + " "
				+ resultCurrency.get(1).getSell() + " " + resultCurrency.get(1).getSource());

		return resultCurrency;
	}

	@Override
	public List<CurrencyData> getAllCurrencies(Integer source) throws DataSourceException {
		List<CurrencyData> resultCurrencies = Lists.newArrayList();

		String whereClause = Defs.COLUMN_SOURCE + " = ? ";
		String[] whereArgs = new String[] { source.toString() };

		Cursor cursor = database.query(Defs.TABLE_CURRENCY, ALL_COLUMNS, whereClause, whereArgs, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			CurrencyData comment = cursorToCurrency(cursor);
			resultCurrencies.add(comment);
			cursor.moveToNext();
		}

		cursor.close();
		return resultCurrencies;
	}

	@Override
	public List<CurrencyData> getAllRates(String code) throws DataSourceException {
		List<CurrencyData> resultCurrencies = Lists.newArrayList();

		String whereClause = Defs.COLUMN_CODE + " = ? ";
		String[] whereArgs = new String[] { code };

		Cursor cursor = database.query(Defs.TABLE_CURRENCY, ALL_COLUMNS, whereClause, whereArgs, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			CurrencyData comment = cursorToCurrency(cursor);
			resultCurrencies.add(comment);
			cursor.moveToNext();
		}

		cursor.close();
		return resultCurrencies;
	}

	private CurrencyData cursorToCurrency(Cursor cursor) {
		CurrencyData currency = new CurrencyData();

		currency.setCode(cursor.getString(cursor.getColumnIndex(Defs.COLUMN_CODE)));
		currency.setRatio(cursor.getInt(cursor.getColumnIndex(Defs.COLUMN_RATIO)));
		currency.setBuy(cursor.getString(cursor.getColumnIndex(Defs.COLUMN_BUY)));
		currency.setSell(cursor.getString(cursor.getColumnIndex(Defs.COLUMN_SELL)));
		currency.setDate(
				DateTimeUtils.parseStringToDate(cursor.getString(cursor.getColumnIndex(Defs.COLUMN_CURR_DATE))));
		currency.setSource(cursor.getInt(cursor.getColumnIndex(Defs.COLUMN_SOURCE)));
		return currency;
	}

}
