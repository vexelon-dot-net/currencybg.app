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
package net.vexelon.currencybg.app.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class StringUtils {

	public static String emptyIfNull(String value) {
		return value == null ? "" : value;
	}

	public static String emptyIfNull(JSONObject json, String key) {
		try {
			return json.getString(key);
		} catch (JSONException e) {
			return "";
		}
	}

	public static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	public static String stripHtml(String html, boolean stripWhiteSpace) {
		html = html.replaceAll("(<.[^>]*>)|(</.[^>]*>)", "");
		if (stripWhiteSpace)
			html = html.replaceAll("\\t|\\n|\\r", "");
		html = html.trim();
		return html;
	}

	public static String stripHtml(String html) {
		return stripHtml(html, true);
	}

}
