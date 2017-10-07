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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.db.models.WalletEntry;
import net.vexelon.currencybg.app.utils.DateTimeUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

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
	public void close() throws IOException {
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
			cursor = database.rawQuery("SELECT * FROM currencies ORDER BY strftime('%s', curr_date) DESC; ", null);

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				CurrencyData currency = cursorToCurrency(cursor);

				String id = currency.getCode() + currency.getSource();
				if (!result.containsKey(id)) {
					result.put(id, currency);
				}

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
	public List<CurrencyData> getAllRates(String code, Integer source) throws DataSourceException {
		List<CurrencyData> result = Lists.newArrayList();
		Cursor cursor = null;

		try {
			cursor = database.rawQuery(
					"SELECT DISTINCT code, ratio, buy, sell, curr_date, source FROM currencies WHERE "
							+ Defs.COLUMN_CODE + " = ? AND " + Defs.COLUMN_SOURCE
							+ " = ? ORDER BY strftime('%s', curr_date) DESC; ",
					new String[] { code, Integer.toString(source) });

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				CurrencyData currency = cursorToCurrency(cursor);
				result.add(currency);

				cursor.moveToNext();
			}
		} catch (Throwable t) {
			throw new DataSourceException("SQL error: Failed fetching all source - " + source + " rates for - " + code,
					t);
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	@Override
	public List<CurrencyData> getAllRates(Integer source) throws DataSourceException {
		Map<String, CurrencyData> result = Maps.newHashMap();
		Cursor cursor = null;

		try {
			cursor = database.rawQuery(
					"SELECT * FROM currencies WHERE " + Defs.COLUMN_SOURCE
							+ " = ? ORDER BY strftime('%s', curr_date) DESC; ",
					new String[] { String.valueOf(source) });

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				CurrencyData currency = cursorToCurrency(cursor);

				if (!result.containsKey(currency.getCode())) {
					result.put(currency.getCode(), currency);
				}

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
					+ " = ? ORDER BY strftime('%s', curr_date) DESC; ", new String[] { code });

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				CurrencyData currency = cursorToCurrency(cursor);
				result.add(currency);

				cursor.moveToNext();
			}
		} catch (Throwable t) {
			throw new DataSourceException("SQL error: Failed fetching all rates for - " + code, t);
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

	@Override
	public Optional<DateTime> getLastRatesDownloadTime() throws DataSourceException {
		// final String COLUMN_DT = "dt";
		//
		// String query = "";
		// for (Sources source : Sources.values()) {
		// if (source.isEnabled()) {
		// if (!query.isEmpty()) {
		// query += " UNION ";
		// }
		//
		// query += "SELECT DISTINCT source, max(curr_date) as " + COLUMN_DT + " FROM
		// currencies WHERE SOURCE="
		// + source.getID();
		// }
		// }
		//
		// Set<DateTime> dates = Sets.newTreeSet(new Comparator<DateTime>() {
		// @Override
		// public int compare(DateTime t1, DateTime t2) {
		// return t2.compareTo(t1);
		// }
		// });
		// DateTime startOfToday =
		// DateTime.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone(Defs.DATE_TIMEZONE_SOFIA)))
		// .withTime(0, 0, 0, 0);

		DateTime result = null;
		Cursor cursor = null;

		try {
			cursor = database
					.rawQuery("SELECT curr_date FROM currencies ORDER BY strftime('%s', curr_date) DESC LIMIT 1", null);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				String rawDate = cursor.getString(cursor.getColumnIndex(Defs.COLUMN_CURR_DATE));
				result = DateTime.parse(rawDate);
			}

			// cursor = database.rawQuery(query, null);
			//
			// while (cursor.moveToNext()) {
			// String source = cursor.getString(cursor.getColumnIndex(Defs.COLUMN_SOURCE));
			// if (source != null) {
			// String rawDate = cursor.getString(cursor.getColumnIndex(COLUMN_DT));
			// DateTime dateTime = DateTime.parse(rawDate);
			//
			// if (dateTime.isAfter(startOfToday)) {
			// dates.add(dateTime);
			// }
			// }
			// }
			//
			// result = Iterables.getLast(dates, startOfToday);
			//
			// for (DateTime dt : dates) {
			// Log.d(Defs.LOG_TAG, "SQL date = " + dt);
			// }
			// Log.d(Defs.LOG_TAG, "SQL selected = " + result);

		} catch (Throwable t) {
			throw new DataSourceException("SQL error: Failed fetching latest currency date!", t);
		} finally {
			closeCursor(cursor);
		}

		return Optional.fromNullable(result);
	}

	@Override
	public void addWalletEntry(WalletEntry walletEntry) throws DataSourceException {
		ContentValues values = new ContentValues();

		if (walletEntry != null) {
			values.put(Defs.COLUMN_WALLET_CODE, walletEntry.getCode());
			values.put(Defs.COLUMN_WALLET_AMOUNT, walletEntry.getAmount());
			values.put(Defs.COLUMN_WALLET_PURCHASE_TIME,
					DateTimeUtils.parseDateToString(walletEntry.getPurchaseTime(), Defs.DATEFORMAT_ISO8601));
			values.put(Defs.COLUMN_WALLET_PURCHASE_RATE, walletEntry.getPurchaseRate());

			database.insert(Defs.TABLE_WALLET, null, values);
		}
	}

	@Override
	public void deleteWalletEntry(int id) throws DataSourceException {
		try {
			database.execSQL("DELETE FROM wallet WHERE id = " + id + ";");
		} catch (SQLException e) {
			throw new DataSourceException("SQL statement error!", e);
		}
	}

	@Override
	public void updateWalletEntry(int id, WalletEntry walletEntry) throws DataSourceException {
		ContentValues newValues = new ContentValues();

		if (walletEntry.getCode() != null) {
			newValues.put(Defs.COLUMN_WALLET_CODE, walletEntry.getCode());
		}
		if (walletEntry.getAmount() != null) {
			newValues.put(Defs.COLUMN_WALLET_AMOUNT, walletEntry.getAmount());
		}
		if (walletEntry.getPurchaseTime() != null) {
			newValues.put(Defs.COLUMN_WALLET_PURCHASE_TIME,
					DateTimeUtils.parseDateToString(walletEntry.getPurchaseTime(), Defs.DATEFORMAT_ISO8601));
		}
		if (walletEntry.getPurchaseRate() != null) {
			newValues.put(Defs.COLUMN_WALLET_PURCHASE_RATE, walletEntry.getPurchaseRate());
		}

		database.update(Defs.TABLE_WALLET, newValues, "id=?", new String[] { Integer.toString(id) });
	}

	@Override
	public List<WalletEntry> getWalletEntries() throws DataSourceException {
		List<WalletEntry> result = Lists.newArrayList();
		Cursor cursor = null;

		try {
			cursor = database.rawQuery("SELECT * FROM wallet;", null);

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				WalletEntry walletEntry = cursorToWallet(cursor);
				result.add(walletEntry);

				cursor.moveToNext();
			}
		} catch (Throwable t) {
			throw new DataSourceException("SQL error: Failed fetching wallet entries", t);
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	private WalletEntry cursorToWallet(Cursor cursor) {
		WalletEntry walletEntry = new WalletEntry();

		walletEntry.setId(cursor.getInt(cursor.getColumnIndex(Defs.COLUMN_WALLET_ID)));
		walletEntry.setCode(cursor.getString(cursor.getColumnIndex(Defs.COLUMN_WALLET_CODE)));
		walletEntry.setAmount(cursor.getString(cursor.getColumnIndex(Defs.COLUMN_WALLET_AMOUNT)));
		walletEntry.setPurchaseTime(DateTimeUtils
				.parseStringToDate(cursor.getString(cursor.getColumnIndex(Defs.COLUMN_WALLET_PURCHASE_TIME))));
		walletEntry.setPurchaseRate(cursor.getString(cursor.getColumnIndex(Defs.COLUMN_WALLET_PURCHASE_RATE)));

		return walletEntry;
	}

}
