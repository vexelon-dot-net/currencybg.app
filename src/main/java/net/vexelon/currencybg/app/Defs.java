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

import java.util.Arrays;
import java.util.List;

public class Defs {

	public final static String LOG_TAG = "net.vexelon.currencybg";

	public final static int TOAST_INFO_TIME = 4000;
	public final static int TOAST_ERR_TIME = 3000;
	public final static int SCALE_SHOW_LONG = 5;
	public final static int SCALE_SHOW_SHORT = 2;
	public final static int SCALE_CALCULATIONS = 10;
	public final static String BGN_CODE = "BGN";
	public static final String LONG_DASH = "\u2014";
	public static final String COLOR_NAVY_BLUE = "#00BFFF";
	public static final String COLOR_DARK_ORANGE = "#FF7F50";
	public static final int MAX_SOURCES_TO_SHOW = 3;

	/**
	 * How often to wake-up the Android service
	 */
	public static final long SERVICE_RUN_INTERVAL = 6 * 3600 * 1000; // 6 hours

	/**
	 * Parameters which used into SQLite
	 */
	public static final String DATABASE_NAME = "currencies.db";
	public static final int DATABASE_VERSION = 3;

	public static final String TABLE_CURRENCY = "currencies";
	public static final String TABLE_CURRENCY_DATE = "currenciesdate";
	public static final String TABLE_FIXED_CURRENCY = "fixedcurrencies";

	// Name of columns in table TABLE_CURRENCY
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_CODE = "code";
	public static final String COLUMN_RATIO = "ratio";
	public static final String COLUMN_BUY = "buy";
	public static final String COLUMN_SELL = "sell";
	public static final String COLUMN_SOURCE = "source";
	public static final String COLUMN_CURR_DATE = "curr_date";

	public static final String DATEFORMAT_ISO_8601 = "yyyy-MM-dd'T'HH:mmZ";

	/**
	 * Codes of currencies we don't want to show
	 */
	public static final List<String> HIDDEN_CURRENCY_CODES = Arrays.asList(new String[] { "SBP" });
}
