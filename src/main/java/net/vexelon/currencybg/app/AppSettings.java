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

import com.google.common.collect.Sets;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import net.vexelon.currencybg.app.db.models.CurrencyLocales;

public class AppSettings {

	public static final int SORTBY_NAME = 0;
	public static final int SORTBY_CODE = 1;

	public static final int FILTERBY_ALL = 0;
	public static final int FILTERBY_NONFIXED = 1;
	public static final int FILTERBY_FIXED = 2;

	public static final int PRECISION_SIMPLE = 0;
	public static final int PRECISION_ADVANCED = 1;

	private SharedPreferences generalPrefs = null;
	private Context context = null;

	public AppSettings(Context context) {
		this.context = context;
		this.generalPrefs = PreferenceManager.getDefaultSharedPreferences(context);
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

	/**
	 * 
	 * @param value
	 */
	public void setCurrenciesSortSelection(int value) {
		generalPrefs.edit().putInt("pref_currencies_sortby", value).apply();
	}

	/**
	 * Gets currencies filtering
	 * 
	 * @return
	 *         <ul>
	 *         <li>0 All
	 *         <li>1 Non-Fixed (Default)
	 *         <li>2 Fixed
	 */
	public int getCurrenciesFilterSelection() {
		return generalPrefs.getInt("pref_currencies_filterby", FILTERBY_NONFIXED);
	}

	/**
	 * 
	 * @param value
	 */
	public void setCurrenciesFilterSelection(int value) {
		generalPrefs.edit().putInt("pref_currencies_filterby", value).apply();
	}

	/**
	 * Gets currencies language selection
	 * 
	 * @return {@link CurrencyLocales}
	 */
	public CurrencyLocales getCurrenciesLanguage() {
		String value = getCurrenciesLanguageRaw();
		if ("en".equals(value)) {
			return CurrencyLocales.EN;
		} else if ("bg".equals(value)) {
			return CurrencyLocales.BG;
		}
		return CurrencyLocales.getAppLocale(context);
	}

	public String getCurrenciesLanguageRaw() {
		return generalPrefs.getString("pref_currencies_language", "default");
	}

	/**
	 *
	 * @return <ul>
	 *         <li>0 - PRECISION_SIMPLE</li>
	 *         <li>1 - PRECISION_ADVANCED</li>
	 *         </ul>
	 */
	public int getCurrenciesPrecision() {
		String precision = generalPrefs.getString("pref_currencies_precision", "simple");
		return precision.equals("advanced") ? PRECISION_ADVANCED : PRECISION_SIMPLE;
	}

	/**
	 * 
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
