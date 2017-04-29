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

import android.util.Log;

import net.vexelon.currencybg.app.Defs;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;
import java.util.Random;

public final class NumberUtils {

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
		BigDecimal result = BigDecimal.ZERO;
		if (value != null && !value.isEmpty()) {
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

	public static BigDecimal getCurrencyValue(String formattedCurrency, String code) {
		if (!StringUtils.isEmpty(code) && !StringUtils.isEmpty(formattedCurrency)) {
			try {
				Currency currency = Currency.getInstance(code);
				NumberFormat format = NumberFormat.getCurrencyInstance();
				format.setCurrency(currency);
				return new BigDecimal(format.parse(formattedCurrency).toString());
			} catch (IllegalArgumentException e) {
				Log.wtf(Defs.LOG_TAG, "Died at currency to bign - " + formattedCurrency, e);
			} catch (ParseException e) {
				Log.wtf(Defs.LOG_TAG, "Died at currency to bign parse - " + formattedCurrency, e);
			}
		}

		return BigDecimal.ZERO;
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

	/**
	 * Gets an applicable currency operations math context.
	 * 
	 * @return
	 */
	public static MathContext getCurrencyMathContext() {
		return new MathContext(Defs.SCALE_CALCULATIONS, RoundingMode.HALF_EVEN);
	}

	/**
	 * 
	 * @param amount
	 *            Amount of currency to buy.
	 * @param rate
	 *            Buying rate.
	 * @param ratio
	 *            Buying ratio for one amount unit.
	 * @return
	 */
	public static BigDecimal buyCurrency(BigDecimal amount, String rate, int ratio) {
		BigDecimal bought = BigDecimal.ZERO;

		try {
			MathContext mathContext = getCurrencyMathContext();

			BigDecimal buyRate = BigDecimal.ZERO;
			if (!rate.isEmpty()) {
				buyRate = new BigDecimal(rate, mathContext);
			}

			BigDecimal buyRatio = new BigDecimal(ratio, mathContext);
			bought = amount.multiply(buyRate.divide(buyRatio, mathContext), mathContext);

		} catch (Exception e) {
			Log.e(Defs.LOG_TAG, "Failed to buy currency!", e);
		}

		return bought;
	}

	/**
	 * @see #buyCurrency(BigDecimal, String, int)
	 */
	public static BigDecimal buyCurrency(String amount, String rate, int ratio) {
		return buyCurrency(new BigDecimal(amount, getCurrencyMathContext()), rate, ratio);
	}

	/**
	 * 
	 * @param amount
	 *            Amount of currency to sell.
	 * @param rate
	 *            Selling rate.
	 * @param ratio
	 *            Selling ratio for one amount unit.
	 * @return
	 */
	public static BigDecimal sellCurrency(BigDecimal amount, String rate, int ratio) {
		BigDecimal sold = BigDecimal.ZERO;

		try {
			MathContext mathContext = getCurrencyMathContext();

			BigDecimal sellRate = BigDecimal.ZERO;
			if (!rate.isEmpty()) {
				sellRate = new BigDecimal(rate, mathContext);
			}

			BigDecimal sellRatio = new BigDecimal(ratio, mathContext);
			sold = amount.divide(sellRate, mathContext).multiply(sellRatio, mathContext);

		} catch (Exception e) {
			Log.e(Defs.LOG_TAG, "Failed to sell currency!", e);
		}

		return sold;
	}

	/**
	 * @see #sellCurrency(BigDecimal, String, int)
	 */
	public static BigDecimal sellCurrency(String amount, String rate, int ratio) {
		return sellCurrency(new BigDecimal(amount, getCurrencyMathContext()), rate, ratio);
	}

	/**
	 * Calculates profit given purchase info and current sell info
	 * 
	 * @param amount
	 * @param purchaseRate
	 * @param purchaseRatio
	 * @param sellRate
	 * @param sellRatio
	 * @return
	 */
	public static BigDecimal getProfit(String amount, String purchaseRate, int purchaseRatio, String sellRate,
			int sellRatio) {
		BigDecimal bought = NumberUtils.buyCurrency(amount, purchaseRate, purchaseRatio);
		BigDecimal todaysRate = NumberUtils.buyCurrency(amount, sellRate, sellRatio);
		return todaysRate.subtract(bought);
	}
}
