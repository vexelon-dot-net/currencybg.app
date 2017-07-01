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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.vexelon.currencybg.app.common.CurrencyLocales;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.models.CurrencyData;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public final class AppSettings {

	public static final int RATE_BUY = 0;
	public static final int RATE_SELL = 1;

	public static final int SORTBY_NAME = 0;
	public static final int SORTBY_CODE = 1;

	public static final int PRECISION_SIMPLE = 0;
	public static final int PRECISION_ADVANCED = 1;

	private SharedPreferences generalPrefs = null;
	private Context context = null;

	public AppSettings(Context context) {
		this.context = context;
		this.generalPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public String getAppUrl() {
		final String appPackageName = context.getPackageName();
		return "https://play.google.com/store/apps/details?id=" + appPackageName;
	}

	public String getAppLink() {
		final String appPackageName = context.getPackageName();
		return "market://details?id=" + appPackageName;
	}

	/**
	 * 
	 * @return Date and time of the last update in the Europe/Sofia time zone.
	 *         If this is not available, it returns the date time from 00:00:00
	 *         (beginning of the current day) in Europe/Sofia.
	 */
	public DateTime getLastUpdateDate() {
		String value = generalPrefs.getString("pref_currencies_lastupdate", "");
		if (value.isEmpty()) {
			return DateTime.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone(Defs.DATE_TIMEZONE_SOFIA))).withTime(0, 0,
					0, 0);
		}

		return DateTime.parse(value);
	}

	/**
	 * Saves the last update time as ISO8601 formatted text
	 * 
	 * @param dateTime
	 */
	public void setLastUpdateDate(DateTime dateTime) {
		generalPrefs.edit().putString("pref_currencies_lastupdate", dateTime.toString()).apply();
	}

	/**
	 * Gets what type of rate to show on currencies list fragment
	 *
	 * @return
	 */
	public int getCurrenciesRateSelection() {
		return generalPrefs.getInt("pref_currencies_rate", RATE_BUY);
	}

	public void setCurrenciesRateSelection(int value) {
		generalPrefs.edit().putInt("pref_currencies_rate", value).apply();
	}

	/**
	 * Gets currencies sorting
	 *
	 * @return
	 *         <ul>
	 *         <li>-1 None (Default)
	 *         <li>0 Name
	 *         <li>1 Code
	 */
	public int getCurrenciesSortSelection() {
		return generalPrefs.getInt("pref_currencies_sortby", -1);
	}

	public void setCurrenciesSortSelection(int value) {
		generalPrefs.edit().putInt("pref_currencies_sortby", value).apply();
	}

	/**
	 * Gets currencies filtering by sources
	 *
	 * @return Sources
	 */
	public Set<Sources> getCurrenciesFilter() {
		Set<String> values = Sets.newHashSet();

		values = generalPrefs.getStringSet("pref_currencies_filter_sources", values);
		if (values.isEmpty()) {
			// default
			return Sets.newHashSet(Sources.TAVEX, Sources.FACTORIN, Sources.POLANA1);
		}

		Set<Sources> result = Sets.newHashSet();
		for (String value : values) {
			result.add(Sources.valueOf(Integer.parseInt(value)));
		}
		return result;
	}

	public void setCurrenciesFilter(Set<Sources> sources) {
		Set<String> values = Sets.newHashSet();
		for (Sources source : sources) {
			values.add(Integer.toString(source.getID()));
		}
		generalPrefs.edit().putStringSet("pref_currencies_filter_sources", values).apply();
	}

	/**
	 * Gets currencies language selection
	 *
	 * @return {@link CurrencyLocales}
	 */
	@Deprecated
	public CurrencyLocales getCurrenciesLanguage() {
		String value = getCurrenciesLanguageRaw();
		if ("en".equals(value)) {
			return CurrencyLocales.EN;
		} else if ("bg".equals(value)) {
			return CurrencyLocales.BG;
		}
		return CurrencyLocales.getAppLocale(context);
	}

	@Deprecated
	public String getCurrenciesLanguageRaw() {
		return generalPrefs.getString("pref_currencies_language", "default");
	}

	/**
	 * @return
	 *         <ul>
	 *         <li>0 - PRECISION_SIMPLE</li>
	 *         <li>1 - PRECISION_ADVANCED</li>
	 *         </ul>
	 */
	public int getCurrenciesPrecision() {
		String precision = generalPrefs.getString("pref_currencies_precision", "advanced");
		return precision.equals("advanced") ? PRECISION_ADVANCED : PRECISION_SIMPLE;
	}

	/**
	 * 
	 * @param currencies
	 */
	public void setConvertCurrencies(Set<String> currencies) {
		generalPrefs.edit().putStringSet("pref_convert_currencies", currencies).apply();
	}

	/**
	 * Gets saved target convert currencies
	 *
	 * @return
	 */
	public Set<String> getConvertCurrenciesRaw() {
		Set<String> emptySet = Sets.newHashSet();
		// Needs a new Set instance - http://stackoverflow.com/a/14034804
		return Sets.newHashSet(generalPrefs.getStringSet("pref_convert_currencies", emptySet));
	}

	/**
	 * 
	 * @return A list of dummy {@link CurrencyData} objects that have only their
	 *         code and source id set.
	 */
	public List<CurrencyData> getConvertCurrencies() {
		List<CurrencyData> result = Lists.newArrayList();
		Set<String> convertCurrencies = getConvertCurrenciesRaw();

		Iterator<String> iterator = convertCurrencies.iterator();
		while (iterator.hasNext()) {
			String next = iterator.next();

			// CSV deserialization
			List<String> tokens = Splitter.on(",").splitToList(next);
			if (tokens.size() == 2) {
				CurrencyData c = new CurrencyData();
				c.setCode(tokens.get(0));
				c.setSource(Integer.parseInt(tokens.get(1)));
				result.add(c);
			}
		}

		return result;
	}

	public void addConvertCurrency(CurrencyData currency) {
		Set<String> convertCurrencies = getConvertCurrenciesRaw();
		// CSV serialization
		convertCurrencies.add(currency.getCode() + "," + Integer.toString(currency.getSource()));
		setConvertCurrencies(convertCurrencies);
	}

	/**
	 * Removes a previously stored target convert currency
	 * 
	 * @param currency
	 *            Currency to search by code and source id
	 */
	public void removeConvertCurrency(CurrencyData currency) {
		Set<String> convertCurrencies = getConvertCurrenciesRaw();

		Iterator<String> iterator = convertCurrencies.iterator();
		while (iterator.hasNext()) {
			String next = iterator.next();

			// CSV deserialization
			List<String> tokens = Splitter.on(",").splitToList(next);
			if (tokens.size() == 2) {
				if (currency.getCode().equals(tokens.get(0))
						&& currency.getSource() == Integer.parseInt(tokens.get(1))) {
					iterator.remove();
				}
			}
		}

		setConvertCurrencies(convertCurrencies);
	}

	/**
	 * Sets the last set decimal value for currency convertion
	 *
	 * @param value
	 *            A {@link BigDecimal} converted to {@link String}.
	 */
	public void setLastConvertValue(String value) {
		generalPrefs.edit().putString("pref_convert_value", value).apply();
	}

	public String getLastConvertValue() {
		return generalPrefs.getString("pref_convert_value", "0");
	}

	/**
	 * Sets the last selected currency (code) to convert from
	 *
	 * @param currencyCode
	 */
	public void setLastConvertCurrencySel(String currencyCode) {
		generalPrefs.edit().putString("pref_convert_selcurrencycode", currencyCode).apply();
	}

	/**
	 * 
	 * @return code of last selected source convert currency, or <i>BGN</i> by
	 *         default
	 */
	public String getLastConvertCurrencySel() {
		return generalPrefs.getString("pref_convert_selcurrencycode", "BGN");
	}

	/**
	 * 
	 * @return index of last read <i>What's New</i> message.
	 */
	public int getLastReadNewsId() {
		return generalPrefs.getInt("pref_last_read_whatsnew", 0);
	}

	public void setLastReadNewsId(int value) {
		generalPrefs.edit().putInt("pref_last_read_whatsnew", value).apply();
	}

	/**
	 * 
	 * @return {@code true}, if background service should download rates only
	 *         via WiFi.
	 */
	public boolean isWiFiOnlyDownloads() {
		return generalPrefs.getBoolean("pref_wifi_only_downloads", true);
	}

	public void setWiFiOnlyDownloads(boolean value) {
		generalPrefs.edit().putBoolean("pref_wifi_only_downloads", value).apply();
	}

	/**
	 * 
	 * @param value
	 *            Number of times the user has used the app so far.
	 */
	public void setUserAppUses(int value) {
		generalPrefs.edit().putInt("pref_user_times_used", value).apply();
	}

	public int getUserAppUses() {
		return generalPrefs.getInt("pref_user_times_used", 0);
	}

	/**
	 * Sets whether the user has already voted for this app via the invitation
	 * dialog.
	 * 
	 * @param value
	 */
	public void setUserVoted(boolean value) {
		generalPrefs.edit().putBoolean("pref_user_voted", value).apply();
	}

	public boolean isUserVoted() {
		return generalPrefs.getBoolean("pref_user_voted", false);
	}

}