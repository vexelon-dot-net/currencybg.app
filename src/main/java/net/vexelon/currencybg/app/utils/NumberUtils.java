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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Random;

import android.util.Log;
import net.vexelon.currencybg.app.Defs;

public class NumberUtils {

	private static Random _random = null;

	/**
	 * Get random integer value
	 * 
	 * @param max
	 * @return
	 */
	public static int getRandomInt(int max) {
		if (_random == null) {
			_random = new SecureRandom();
		}
		return _random.nextInt(max);
	}

	public static BigDecimal zeroIfNull(String value) {
		BigDecimal result = new BigDecimal("0.00");
		if (value != null) {
			try {
				result = new BigDecimal(value);
			} catch (NumberFormatException e) {
				Log.w(Defs.LOG_TAG, "Failed to get decimal value from " + value + "!", e);
			}
		}
		return result;
	}

	public static String scaleNumber(BigDecimal number, int n) {
		return number.setScale(n, RoundingMode.HALF_UP).toPlainString();
	}

	public static String roundNumber(BigDecimal number, int n) {
		return number.round(new MathContext(n, RoundingMode.HALF_UP)).toPlainString();
	}

	public static BigDecimal divCurrency(BigDecimal number, BigDecimal divisor) {
		return number.divide(divisor, RoundingMode.HALF_EVEN);
	}

	public static String getCurrencyFormat(BigDecimal number, int n, String code) {
		if (!StringUtils.isEmpty(code)) {
			try {
				Currency currency = Currency.getInstance(code);
				NumberFormat format = NumberFormat.getCurrencyInstance();
				format.setMaximumFractionDigits(n);
				format.setCurrency(currency);
				return format.format(number.doubleValue());
			} catch (IllegalArgumentException e) {
				// default
			}
		}

		return number.setScale(n, RoundingMode.HALF_EVEN).toPlainString();
	}

	public static String getCurrencyFormat(BigDecimal number, String code) {
		return getCurrencyFormat(number, Defs.SCALE_SHOW_SHORT, code);
	}

	/**
	 * Removes unwanted characters
	 * 
	 * @param value
	 * @return
	 */
	public static String cleanValue(String value) {
		return value.replace(",", "");
	}
}
