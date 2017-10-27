package net.vexelon.currencybg.app.ui.filters;

import com.google.common.collect.Lists;

import net.vexelon.currencybg.app.db.models.CurrencyData;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

public class CurrenciesFilter {

	// Codes of currencies we don't show at the moment
	private static final List<String> HIDDEN = Lists.newArrayList("SBP", "MAD");

	private static final List<String> CRYPTO = Lists.newArrayList("BTC", "BCH", "ETH", "DAS", "DOG", "LTC", "RIP",
			"ZEC");
	private static final List<String> TOP6 = Lists.newArrayList("USD", "EUR", "JPY", "GBP", "CHF", "CAD");
	private static final List<String> TOP8 = Lists.newArrayList("USD", "EUR", "JPY", "GBP", "CHF", "CAD", "AUD", "ZAR");

	/**
	 * @return {@code currencies} collection instance with entries not specified in
	 *         {@code filter} removed.
	 */
	public static Collection<CurrencyData> removeHidden(Collection<CurrencyData> currencies) {
		// return Collections2.filter(models, (@Nullable CurrencyData input) ->
		// !HIDDEN.contains(input.getCode()));
		Iterator<CurrencyData> iterator = currencies.iterator();
		while (iterator.hasNext()) {
			CurrencyData c = iterator.next();
			if (HIDDEN.contains(c.getCode())) {
				iterator.remove();
			}
		}
		return currencies;
	}

	/**
	 * @return {@code currencies} collection instance with the entries specified in
	 *         {@code filter} removed.
	 */
	public static Collection<CurrencyData> custom(@Nonnull Collection<CurrencyData> currencies,
			final Collection<String> filter) {
		// return Collections2.filter(models, (@Nullable CurrencyData input) ->
		// filter.contains(input.getCode()));
		Iterator<CurrencyData> iterator = currencies.iterator();
		while (iterator.hasNext()) {
			CurrencyData c = iterator.next();
			if (!filter.contains(c.getCode())) {
				iterator.remove();
			}
		}
		return currencies;
	}

	public static Collection<CurrencyData> crypto(@Nonnull Collection<CurrencyData> currencies) {
		return custom(currencies, CRYPTO);
	}

	public static Collection<CurrencyData> top6(@Nonnull Collection<CurrencyData> currencies) {
		return custom(currencies, TOP6);
	}

	public static Collection<CurrencyData> top8(@Nonnull Collection<CurrencyData> currencies) {
		return custom(currencies, TOP8);
	}
}
