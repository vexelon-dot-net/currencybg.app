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
	BNB(1, false),

	FIB(100),
	TAVEX(200),
	POLANA1(300),
	FACTORIN(400),
	UNICREDIT(500),
	SGEB(600),
	CRYPTO(700),
	CHANGEPARTNER(800),
	FOREXHOUSE(900);

	private int id;
	private boolean enabled;

	Sources(int id, boolean enabled) {
		this.id = id;
		this.enabled = enabled;
	}

	Sources(int id) {
		this(id, true);
	}

	public int getID() {
		return id;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getName(Context context) {
		return getName(context, id);
	}

	public String getFullName(Context context) {
		return getFullName(context, id);
	}

	public String getWebAddress(Context context) {
		return getWebAddress(context, id);
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

	private static String getResource(Context context, int id, int resId) {
		if (context != null) {
			int[] sourceIds = context.getResources().getIntArray(R.array.currency_sources_ids);
			String[] resources = context.getResources().getStringArray(resId);

			for (int i = 0; i < sourceIds.length; i++) {
				if (sourceIds[i] == id) {
					return resources[i];
				}
			}
		}

		return "";
	}

	public static String getName(Context context, int id) {
		return getResource(context, id, R.array.currency_sources);
	}

	public static String getFullName(Context context, int id) {
		return getResource(context, id, R.array.currency_sources_full);
	}

	public static String getWebAddress(Context context, int id) {
		return getResource(context, id, R.array.currency_sources_web);
	}
}
