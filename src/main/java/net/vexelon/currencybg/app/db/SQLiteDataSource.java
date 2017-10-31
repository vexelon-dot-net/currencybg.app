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
import android.util.ArrayMap;

import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.db.models.WalletEntry;
import net.vexelon.currencybg.app.utils.DateTimeUtils;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQLiteDataSource implements DataSource {

	private SQLiteDatabase database;
	private CurrenciesSQLiteDB dbHelper;

	private String getCurrenciesSelectCols() {
		return String.format("%s, %s, %s, %s, %s, %s", Defs.COLUMN_CODE, Defs.COLUMN_RATIO, Defs.COLUMN_BUY,
				Defs.COLUMN_SELL, Defs.COLUMN_CURR_DATE, Defs.COLUMN_SOURCE);
	}

	@Override
	public void connect(Context context) throws DataSourceException {
		try {
			dbHelper = new CurrenciesSQLiteDB(context);
			database = dbHelper.getWritableDatabase();
		} catch (SQLException e) {
			throw new DataSourceException("Cannot open database!", e);
		}
	}

	@Override
	public void close() throws IOException {
		if (dbHelper != null) {
			dbHelper.close();
		}
	}

	@Override
	public void addRates(List<CurrencyData> rates) throws DataSourceException {
		for (CurrencyData rate : rates) {
			ContentValues values = new ContentValues();

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
		}
	}

	@Override
	public void deleteRates(int backDays) throws DataSourceException {
		try {
			// '2016-11-19T20:48:29.022+02:00'
			database.execSQL("DELETE FROM currencies WHERE strftime('%s', curr_date) < strftime('%s', '"
					+ DateTimeUtils.getOldDate(backDays) + "' )");
		} catch (SQLException e) {
			throw new DataSourceException("SQL error: Failed deleting rates!", e);
		}
	}

	@Override
	public List<CurrencyData> getLastRates() throws DataSourceException {
		Table<Integer, String, CurrencyData> table = HashBasedTable.create();

		/**
		 * XXX This needs a further optimization! A 3 days SQLite database may contain
		 * up to 20K rows, so fetching and wrapping all of them into objects takes a lot
		 * of time. The current, very basic, solution is to limit the rows fetched to
		 * 5K. This is not a good solution and may lead to some rates, those that were
		 * not recently updated, to not being shown in the CurrenciesFragment. / Why 5K?
		 * There would be an average of 50K rows collected in 3 days in the remote
		 * database, or about 4,2K per day.
		 */

		try (Cursor cursor = database.rawQuery("SELECT " + getCurrenciesSelectCols()
				+ " FROM currencies ORDER BY strftime('%s', curr_date) DESC LIMIT 5000", null)) {

			Map<String, Integer> cache = newCache();

			while (cursor.moveToNext()) {
				String code = cursor.getString(getIndex(cursor, Defs.COLUMN_CODE, cache));
				int source = cursor.getInt(getIndex(cursor, Defs.COLUMN_SOURCE, cache));

				if (!table.contains(source, code)) {
					table.put(source, code, cursorToCurrency(cursor, code, source, cache));
				}
			}
		} catch (Throwable t) {
			throw new DataSourceException("SQL error: Failed fetching last rates!", t);
		}

		return new ArrayList<>(table.values());
	}

	@Override
	public List<CurrencyData> getAllRates(String code, Integer source) throws DataSourceException {
		List<CurrencyData> result = Lists.newArrayList();

		try (Cursor cursor = database.rawQuery(
				"SELECT DISTINCT " + getCurrenciesSelectCols() + " FROM currencies WHERE " + Defs.COLUMN_CODE
						+ " = ? AND " + Defs.COLUMN_SOURCE + " = ? ORDER BY strftime('%s', curr_date) DESC; ",
				new String[] { code, Integer.toString(source) });) {

			Map<String, Integer> cache = newCache();

			while (cursor.moveToNext()) {
				result.add(cursorToCurrency(cursor, cache));
			}
		} catch (Throwable t) {
			throw new DataSourceException("SQL error: Failed fetching all source - " + source + " rates for - " + code,
					t);
		}

		return result;
	}

	@Override
	public List<CurrencyData> getAllRates(Integer source) throws DataSourceException {
		Map<String, CurrencyData> result = Maps.newHashMap();

		try (Cursor cursor = database
				.rawQuery(
						"SELECT " + getCurrenciesSelectCols() + " FROM currencies WHERE " + Defs.COLUMN_SOURCE
								+ " = ? ORDER BY strftime('%s', curr_date) DESC",
						new String[] { String.valueOf(source) })) {

			Map<String, Integer> cache = newCache();

			while (cursor.moveToNext()) {
				CurrencyData currency = cursorToCurrency(cursor, cache);

				if (!result.containsKey(currency.getCode())) {
					result.put(currency.getCode(), currency);
				}
			}
		} catch (Throwable t) {
			throw new DataSourceException("SQL error: Failed fetching all currencies for source " + source, t);
		}

		return Lists.newArrayList(result.values());
	}

	@Override
	public List<CurrencyData> getAllRates(String code) throws DataSourceException {
		List<CurrencyData> result = Lists.newArrayList();

		try (Cursor cursor = database.rawQuery("SELECT " + getCurrenciesSelectCols() + " FROM currencies WHERE "
				+ Defs.COLUMN_CODE + " = ? ORDER BY strftime('%s', curr_date) DESC", new String[] { code })) {

			Map<String, Integer> cache = newCache();

			while (cursor.moveToNext()) {
				result.add(cursorToCurrency(cursor, cache));
			}
		} catch (Throwable t) {
			throw new DataSourceException("SQL error: Failed fetching all rates for - " + code, t);
		}

		return result;
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

		try (Cursor cursor = database
				.rawQuery("SELECT curr_date FROM currencies ORDER BY strftime('%s', curr_date) DESC LIMIT 1", null)) {

			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
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

		try (Cursor cursor = database.rawQuery("SELECT * FROM wallet", null)) {
			Map<String, Integer> cache = newCache();

			while (cursor.moveToNext()) {
				result.add(cursorToWallet(cursor, cache));
			}
		} catch (Throwable t) {
			throw new DataSourceException("SQL error: Failed fetching wallet entries", t);
		}

		return result;
	}

	private Map<String, Integer> newCache() {
		return new ArrayMap<>();
	}

	private int getIndex(Cursor cursor, String column, Map<String, Integer> cache) {
		Integer idx = cache.get(column);

		if (idx == null) {
			idx = cursor.getColumnIndex(column);
			cache.put(column, idx);
		}

		return idx;
	}

	private WalletEntry cursorToWallet(Cursor cursor, Map<String, Integer> cache) {
		WalletEntry walletEntry = new WalletEntry();

		walletEntry.setId(cursor.getInt(getIndex(cursor, Defs.COLUMN_WALLET_ID, cache)));
		walletEntry.setCode(cursor.getString(getIndex(cursor, Defs.COLUMN_WALLET_CODE, cache)));
		walletEntry.setAmount(cursor.getString(getIndex(cursor, Defs.COLUMN_WALLET_AMOUNT, cache)));
		walletEntry.setPurchaseTime(DateTimeUtils
				.parseStringToDate(cursor.getString(getIndex(cursor, Defs.COLUMN_WALLET_PURCHASE_TIME, cache))));
		walletEntry.setPurchaseRate(cursor.getString(getIndex(cursor, Defs.COLUMN_WALLET_PURCHASE_RATE, cache)));

		return walletEntry;
	}

	private CurrencyData cursorToCurrency(Cursor cursor, String code, int source, Map<String, Integer> cache) {
		CurrencyData currency = new CurrencyData();

		currency.setCode(code);
		currency.setRatio(cursor.getInt(getIndex(cursor, Defs.COLUMN_RATIO, cache)));
		currency.setBuy(cursor.getString(getIndex(cursor, Defs.COLUMN_BUY, cache)));
		currency.setSell(cursor.getString(getIndex(cursor, Defs.COLUMN_SELL, cache)));
		currency.setDate(cursor.getString(getIndex(cursor, Defs.COLUMN_CURR_DATE, cache)));
		currency.setSource(source);

		return currency;
	}

	private CurrencyData cursorToCurrency(Cursor cursor, Map<String, Integer> cache) {
		return cursorToCurrency(cursor, cursor.getString(getIndex(cursor, Defs.COLUMN_CODE, cache)),
				cursor.getInt(getIndex(cursor, Defs.COLUMN_SOURCE, cache)), cache);
	}

}
