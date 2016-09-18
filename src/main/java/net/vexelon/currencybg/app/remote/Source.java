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
package net.vexelon.currencybg.app.remote;

import com.squareup.okhttp.OkHttpClient;

import java.util.List;
import java.util.Map;

import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.db.models.CurrencyDataNew;
import net.vexelon.currencybg.app.db.models.CurrencyLocales;

public interface Source {

	/**
	 * Fetches exchange rates from the underlying source and serves back a ready
	 * to consume {@link CurrencyData} language mapped list.
	 *
	 * @return {@link Map} of languages for each of which a list of
	 *         {@link CurrencyData} is available.
	 * @throws SourceException
	 */
	@Deprecated
	Map<CurrencyLocales, List<CurrencyData>> downloadRates(boolean getFixedRates) throws SourceException;

	/**
	 * Fetches a list of currencies by date
	 *
	 * @param initialTime
	 * @return
	 * @throws SourceException
	 */
	List<CurrencyData> getAllRatesByDate(String initialTime) throws SourceException;

	/**
	 * Fetches a list of currencies by sourceId and date
	 *
	 * @param initialTime
	 * @param sourceId
	 * @return
	 * @throws SourceException
	 */
	List<CurrencyData> getAllRatesByDateSource(String initialTime, Integer sourceId) throws SourceException;

	/**
	 * Fetches a list of currencies for the current date which are after DateTime
	 *
	 * @param timeFrom
	 * @return
	 * @throws SourceException
	 */
	List<CurrencyData> getAllCurrentRatesAfter(String timeFrom) throws  SourceException;

	/**
	 * Fetches a list of currencies for the current date which are after timeFrom by sourceId
	 *
	 * @param timeFrom
	 * @param sourceId
	 * @return
	 * @throws SourceException
	 */
	List<CurrencyData> getAllCurrentRatesAfter(String timeFrom, Integer sourceId) throws  SourceException;
}
