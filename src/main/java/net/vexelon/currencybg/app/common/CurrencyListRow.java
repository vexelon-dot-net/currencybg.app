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
package net.vexelon.currencybg.app.common;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import net.vexelon.currencybg.app.db.models.CurrencyData;

import java.util.Map;

public class CurrencyListRow {

	private String code;
	private String name;

	private Map<Sources, CurrencyData> columns = Maps.newHashMap();

	public CurrencyListRow(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public CurrencyListRow addColumn(Sources source, CurrencyData currencyData) {
		columns.put(source, currencyData);
		return this;
	}

	public Optional<CurrencyData> getColumn(Sources source) {
		return Optional.fromNullable(columns.get(source));
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
}
