package net.vexelon.currencybg.app.ui.filters;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.vexelon.currencybg.app.db.models.CurrencyData;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CurrenciesFilter implements Filter<CurrencyData> {

	// Codes of currencies we don't show at the moment
	private static final List<String> HIDDEN = Lists.newArrayList("SBP", "MAD");

	private static final List<String> CRYPTO = Lists.newArrayList("BGN", "BTC", "BCH", "ETC", "ETH", "EOS", "DASH",
			"DOGE", "LTC", "XRP", "XLM", "XMR", "ZEC");
	private static final List<String> TOP6 = Lists.newArrayList("BGN", "USD", "EUR", "JPY", "GBP", "CHF", "CAD");
	private static final List<String> TOP8 = Lists.newArrayList("BGN", "USD", "EUR", "JPY", "GBP", "CHF", "CAD", "AUD",
			"ZAR");

	private Collection<CurrencyData> currencies;

	public CurrenciesFilter(Collection<CurrencyData> currencies) {
		this.currencies = currencies;
	}

	@Override
	public Collection<CurrencyData> get() {
		return currencies;
	}

	/**
	 * Removes all entries for which duplicate {@link CurrencyData#getCode()}
	 * exists.
	 */
	public CurrenciesFilter distinct() {
		Collection<CurrencyData> result = Lists.newArrayList();
		Set<String> codes = Sets.newHashSet();

		for (CurrencyData currency : currencies) {
			if (!codes.contains(currency.getCode())) {
				result.add(currency);
				codes.add(currency.getCode());
			}
		}

		currencies = result;

		return this;
	}

	/**
	 * Removes all entries specified in {@link CurrenciesFilter#HIDDEN}.
	 */
	public CurrenciesFilter removeHidden() {
		// return Collections2.filter(models, (@Nullable CurrencyData input) ->
		// !HIDDEN.contains(input.getCode()));

		Iterator<CurrencyData> iterator = currencies.iterator();
		while (iterator.hasNext()) {
			CurrencyData c = iterator.next();
			if (HIDDEN.contains(c.getCode())) {
				iterator.remove();
			}
		}

		return this;
	}

	/**
	 * Removes all entries not specified in {@code filter}.
	 */
	public CurrenciesFilter custom(final Collection<String> filter) {
		// return Collections2.filter(models, (@Nullable CurrencyData input) ->
		// filter.contains(input.getCode()));

		Iterator<CurrencyData> iterator = currencies.iterator();
		while (iterator.hasNext()) {
			CurrencyData c = iterator.next();
			if (!filter.contains(c.getCode())) {
				iterator.remove();
			}
		}

		return this;
	}

	public CurrenciesFilter crypto() {
		return custom(CRYPTO);
	}

	public CurrenciesFilter top6() {
		return custom(TOP6);
	}

	public CurrenciesFilter top8() {
		return custom(TOP8);
	}
}
