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

import java.math.BigDecimal;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.vexelon.currencybg.app.common.CurrencyLocales;
import net.vexelon.currencybg.app.common.Sources;

public class AppSettings {

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
			return Sets.newHashSet(Sources.TAVEX, Sources.POLANA1, Sources.FIB);
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
		String precision = generalPrefs.getString("pref_currencies_precision", "simple");
		return precision.equals("advanced") ? PRECISION_ADVANCED : PRECISION_SIMPLE;
	}

	/**
	 * @param currencyCodes
	 *            List of currency codes (case-sensitive)
	 */
	private void setConvertCurrencies(Set<String> currencyCodes) {
		generalPrefs.edit().putStringSet("pref_convert_currencycodes", currencyCodes).apply();
	}

	/**
	 * Gets saved target convert currencies
	 *
	 * @return
	 */
	public Set<String> getConvertCurrencies() {
		Set<String> emptySet = Sets.newHashSet();
		// Needs a new Set instance - http://stackoverflow.com/a/14034804
		return Sets.newHashSet(generalPrefs.getStringSet("pref_convert_currencycodes", emptySet));
	}

	public void addConvertCurrency(String currencyCode) {
		Set<String> convertCurrencies = getConvertCurrencies();
		convertCurrencies.add(currencyCode);
		setConvertCurrencies(convertCurrencies);
	}

	public void removeConvertCurrency(String currencyCode) {
		Set<String> convertCurrencies = getConvertCurrencies();
		convertCurrencies.remove(currencyCode);
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
		return generalPrefs.getString("pref_convert_value", "0.00");
	}

	/**
	 * Sets the last selected currency (code) to convert from
	 *
	 * @param currencyCode
	 */
	public void setLastConvertCurrencySel(String currencyCode) {
		generalPrefs.edit().putString("pref_convert_selcurrencycode", currencyCode).apply();
	}

	public String getLastConvertCurrencySel() {
		return generalPrefs.getString("pref_convert_selcurrencycode", "bg");
	}

}
