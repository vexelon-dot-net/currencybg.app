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

import java.util.Collection;

public final class StringUtils {

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

	public static String toCSV(Collection<String> values) {
		String result = "";
		for (String value : values) {
			if (!result.isEmpty()) {
				result += ", ";
			}
			result += value;
		}
		return result;
	}

	/**
	 * Puts ellipses in input strings that are longer than than maxCharacters.
	 * Shorter strings or null is returned unchanged.
	 * 
	 * @param input
	 *            the input string that may be subjected to shortening
	 * @param maxCharacters
	 *            the maximum characters that are acceptable for the
	 *            unshortended string. Must be at least 3, otherwise a string
	 *            with ellipses is too long already.
	 * @param charactersAfterEllipsis
	 *            the number of characters that should appear after the ellipsis
	 *            (0 or larger)
	 * @return the truncated string with trailing ellipses
	 * @link http://stackoverflow.com/questions/3288716/automatically-ellipsize-a-string-in-java
	 */
	public static String ellipsize(String input, int maxCharacters, int charactersAfterEllipsis) {
		if (maxCharacters < 3) {
			throw new IllegalArgumentException(
					"maxCharacters must be at least 3 because the ellipsis already take up 3 characters");
		}
		// if (maxCharacters - 3 > charactersAfterEllipsis) {
		// throw new IllegalArgumentException("charactersAfterEllipsis must be
		// less than maxCharacters");
		// }
		if (input == null || input.length() < maxCharacters) {
			return input;
		}
		return input.substring(0, maxCharacters - 3 - charactersAfterEllipsis) + "..."
				+ input.substring(input.length() - charactersAfterEllipsis);
	}

}
