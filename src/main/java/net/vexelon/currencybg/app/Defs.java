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

public final class Defs {

	public final static String LOG_TAG = "net.vexelon.currencybg";

	public final static int TOAST_INFO_TIME = 4000;
	public final static int TOAST_ERR_TIME = 3000;
	public static final String LONG_DASH = "\u2014";
	public static final String COLOR_NAVY_BLUE = "#00BFFF";
	public static final String COLOR_DARK_ORANGE = "#FFB400";
	public static final String COLOR_DANGER_RED = "#e8392f";
	public static final String COLOR_OK_GREEN = "#2fe857";

	public final static int SCALE_SHOW_LONG = 4;
	public final static int SCALE_SHOW_SHORT = 2;
	public final static int SCALE_CALCULATIONS = 10;

	public final static String CURRENCY_CODE_BGN = "BGN";

	/**
	 * How often to wake-up the Android service
	 */
	public static final int SERVICE_FIRST_RUN_INTERVAL = 5 * 60; // seconds
	public static final int SERVICE_DATABASE_CLEAN_INTERVAL = 3; // days
	public static final String SERVICE_ACTION_NOTIFY_UPDATE = "_CBG_NOTIFY_UPDATE";

	/**
	 * Parameters which used into SQLite
	 */
	public static final String DATABASE_NAME = "currencies.db";
	public static final int DATABASE_VERSION = 4;

	public static final String TABLE_CURRENCY = "currencies";
	public static final String TABLE_CURRENCY_DATE = "currenciesdate";
	public static final String TABLE_FIXED_CURRENCY = "fixedcurrencies";
	public static final String TABLE_WALLET = "wallet";

	// Name of columns in table TABLE_CURRENCY
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_CODE = "code";
	public static final String COLUMN_RATIO = "ratio";
	public static final String COLUMN_BUY = "buy";
	public static final String COLUMN_SELL = "sell";
	public static final String COLUMN_SOURCE = "source";
	public static final String COLUMN_CURR_DATE = "curr_date";

	// Name of columns in table TABLE_WALLET
	public static final String COLUMN_WALLET_ID = "id";
	public static final String COLUMN_WALLET_CODE = "code";
	public static final String COLUMN_WALLET_AMOUNT = "amount";
	public static final String COLUMN_WALLET_PURCHASE_TIME = "purchaseTime";
	public static final String COLUMN_WALLET_PURCHASE_RATE = "purchaseRate";

	public static final String DATE_TIMEZONE_SOFIA = "Europe/Sofia";
	public static final String DATEFORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mmZ";
	public static final String DATEFORMAT_YYMMDD = "yy/MM/dd";
	public static final String DATEFORMAT_DDMMMMYY = "dd-MMMM-yy";

	/**
	 * Max amount of currency sources to show/select on the main fragment
	 */
	public static final int MAX_SOURCES_TO_SHOW = 3;

	/**
	 * Codes of currencies we don't want to show
	 */
	public static final List<String> HIDDEN_CURRENCY_CODES = Arrays
			.asList(new String[] { "SBP", "QAR", "GEL", "SAR", "MAD", "TWD", "VND" });
}
