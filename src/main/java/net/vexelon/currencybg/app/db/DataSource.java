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

import java.io.Closeable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.Context;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.db.models.CurrencyLocales;

/**
 * Encapsulates the available read-write operations to and from an underlying
 * data source implementation.
 */
public interface DataSource extends Closeable {

	/**
	 * Establishes connection to data source.
	 *
	 * @param context
	 * @throws DataSourceException
	 *             If an SQL error is thrown.
	 */
	void connect(Context context) throws DataSourceException;

	/**
	 * Adds exchange rates data
	 *
	 * @param rates
	 * @throws DataSourceException
	 */
	public void addRates(List<CurrencyData> rates) throws DataSourceException;

	/**
	 *
	 *
	 * @return
	 * @throws DataSourceException
	 */
	public List<CurrencyData> getLastRates() throws  DataSourceException;

	/**
	 * Returns all currencies for the specific source
	 *
	 * @param source
	 * @return
	 * @throws DataSourceException
     */
	public List<CurrencyData> getAllCurrencies(Integer source) throws DataSourceException;

	/**
	 * Returns all currencies for the specific code
	 *
	 * @param code
	 * @return
	 * @throws DataSourceException
     */
	public List<CurrencyData> getAllRates(String code) throws  DataSourceException;

	/**
	 *
	 * @throws DataSourceException
	 */
	public void deleteRates() throws DataSourceException;

	/**
	 * Fetches a list of dates for which exchange rates were downloaded and
	 * available in the underlying data source.
	 *
	 * @param locale
	 * @return A {@link List} of {@link Date} objects or an empty {@link List},
	 *         if no dates are available.
	 * @throws DataSourceException
	 */
//	List<Date> getAvailableRatesDates(CurrencyLocales locale) throws DataSourceException;

	/**
	 * Fetches all exchange rates, for all dates, from the underlying data
	 * source.
	 *
	 * @param locale
	 * @return {@link List} or {@code null}, if no rates are
	 *         available.
	 * @throws DataSourceException
	 */
//	List<CurrencyData> getRates(CurrencyLocales locale) throws DataSourceException;

	/**
	 * Fetches exchange rates for a given date.
	 *
	 * @param locale
	 * @param date
	 * @return {@link List} or {@code null}, if no rates are
	 *         available for the given date.
	 * @throws DataSourceException
	 */
//	List<CurrencyData> getRates(CurrencyLocales locale, Date date) throws DataSourceException;


//	List<CurrencyData> getFixedRates(CurrencyLocales locale, Date date) throws DataSourceException;

	/**
	 * Adds exchange rates data for given download {@link Date}.
	 *
	 * @param rates
	 *            A {@link Map} of language and {@link CurrencyData} list
	 *            values.
	 * @throws DataSourceException
	 */
//	void addRates(Map<CurrencyLocales, List<CurrencyData>> rates) throws DataSourceException;

	/**
	 * Fetches the latest exchange rates from the underlying data source.
	 *
	 * @param locale
	 * @throws DataSourceException
	 */
//	List<CurrencyData> getLastRates(CurrencyLocales locale) throws DataSourceException;

	/**
	 * Fetches the last exchange fixed rates from the underlying data source.
	 *
	 * @param locale
	 * @return
	 * @throws DataSourceException
	 */
//	List<CurrencyData> getLastFixedRates(CurrencyLocales locale) throws DataSourceException;
}
