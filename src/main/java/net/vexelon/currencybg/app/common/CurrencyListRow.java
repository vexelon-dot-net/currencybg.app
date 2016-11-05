package net.vexelon.currencybg.app.common;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.remote.Source;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
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
