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

import android.content.Context;

import net.vexelon.currencybg.app.R;

/**
 * Available currency sources
 * <p>
 * Should any values be updated here, make sure to also update the
 * {@code currency_sources_ids} array in {@code arrays.xml}.
 */
public enum Sources {

	/**
	 * @deprecated
	 */
	BNB(1),

	FIB(100),
	TAVEX(200),
	POLANA1(300),
	FACTORIN(400);

	private int id;

	Sources(int id) {
		this.id = id;
	}

	public int getID() {
		return id;
	}

	/**
	 *
	 * @param id
	 * @return {@link Sources} or {@code null}.
	 */
	public static Sources valueOf(int id) {
		for (Sources s : Sources.values()) {
			if (s.getID() == id) {
				return s;
			}
		}
		return null;
	}

	public static String getName(int id, Context context) {
		int[] sourceIds = context.getResources().getIntArray(R.array.currency_sources_ids);
		String[] sourceNames = context.getResources().getStringArray(R.array.currency_sources);

		for (int i = 0; i < sourceIds.length; i++) {
			if (sourceIds[i] == id) {
				return sourceNames[i];
			}
		}

		return "";
	}
}
