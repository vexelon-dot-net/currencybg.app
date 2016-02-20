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
package net.vexelon.currencybg.app;

public class Defs {

	public final static String LOG_TAG = "net.vexelon.currencybg";

	public final static int TOAST_INFO_TIME = 4000;
	public final static int TOAST_ERR_TIME = 3000;
	public final static int SCALE_SHOW_LONG = 5;
	public final static int SCALE_SHOW_SHORT = 2;
	public final static int SCALE_CALCULATIONS = 10;
	public final static String BGN_CODE = "BGN";

	// //////Tsvetoslav
	// Time for request to BNB
	public static final long NOTIFY_INTERVAL = 6 * 3600 * 1000; // 6 hours
	// Parameters which used into SQLite//
	// Name of DB
	public static final String DATABASE_NAME = "currencies.db";
	// Version of DB
	public static final int DATABASE_VERSION = 2;
	// table name
	public static final String TABLE_CURRENCY = "currencies";
	public static final String TABLE_CURRENCY_DATE = "currenciesdate";
	public static final String TABLE_FIXED_CURRENCY = "fixedcurrencies";
	// Name of columns in table TABLE_CURRENCY
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_GOLD = "gold";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_CODE = "code";
	public static final String COLUMN_RATIO = "ratio";
	public static final String COLUMN_REVERSERATE = "reverserate";
	public static final String COLUMN_RATE = "rate";
	public static final String COLUMN_EXTRAINFO = "extrainfo";
	public static final String COLUMN_CURR_DATE = "curr_date";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_F_STAR = "f_star";
	public static final String COLUMN_LOCALE = "locale";
}
