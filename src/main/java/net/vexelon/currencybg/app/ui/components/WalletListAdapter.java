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
package net.vexelon.currencybg.app.ui.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.db.models.WalletEntry;
import net.vexelon.currencybg.app.ui.UIUtils;
import net.vexelon.currencybg.app.utils.NumberUtils;

import org.joda.time.LocalDateTime;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class WalletListAdapter extends ArrayAdapter<WalletEntry> {

	private final List<WalletEntry> itemsImmutable;
	private List<WalletEntry> items;
	private int precisionMode = AppSettings.PRECISION_SIMPLE;
	private Set<Sources> sourcesFilter = Sets.newHashSet();
	private Multimap<String, CurrencyData> currencies;

	/**
	 * @param context
	 * @param textViewResId
	 * @param items
	 *            Wallet entries
	 * @param currencies
	 *            Currencies
	 * @param precisionMode
	 */
	public WalletListAdapter(Context context, int textViewResId, List<WalletEntry> items,
			Multimap<String, CurrencyData> currencies, int precisionMode) {
		super(context, textViewResId, items);
		this.itemsImmutable = Lists.newArrayList(items.iterator());
		this.items = items;
		this.currencies = currencies;
		this.precisionMode = precisionMode;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(R.layout.wallet_row_layout, parent, false);
		}

		WalletEntry entry = items.get(position);

		UIUtils.setFlagIcon(v, R.id.wallet_row_icon, entry.getCode());
		UIUtils.setText(v, R.id.wallet_row_code, entry.getCode());
		UIUtils.setText(v, R.id.wallet_row_amount, entry.getAmount());
		UIUtils.setText(v, R.id.wallet_row_bought_at, entry.getPurchaseRate());
		UIUtils.setText(v, R.id.wallet_row_bought_on,
				LocalDateTime.fromDateFields(entry.getPurchaseTime()).toString(Defs.DATEFORMAT_DDMMMMYY));
		UIUtils.setText(v, R.id.wallet_row_current_value, getProfit(entry), true);

		// propagte icon and code view taps
		v.findViewById(R.id.wallet_row_icon).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((ListView) parent).performItemClick(v, position, R.id.wallet_row_icon);
			}
		});
		v.findViewById(R.id.wallet_row_code).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((ListView) parent).performItemClick(v, position, R.id.wallet_row_code);
			}
		});

		return v;
	}

	/**
	 * Calculates profit for given wallet {@code entry} based on the latest
	 * known currency rates and sources.
	 *
	 * @param entry
	 * @return
	 */
	private String getProfit(WalletEntry entry) {
		BigDecimal bestRate = BigDecimal.ZERO;
		BigDecimal amount = new BigDecimal(entry.getAmount(), NumberUtils.getCurrencyMathContext());

		Collection<CurrencyData> currencyDatas = currencies.get(entry.getCode());
		for (CurrencyData currencyData : currencyDatas) {
			// simulate BGN buying
			BigDecimal thisRate = NumberUtils.buyCurrency(amount, currencyData.getBuy(), currencyData.getRatio());
			if (thisRate.compareTo(bestRate) > 0) {
				bestRate = thisRate;
			}
		}

		BigDecimal result = bestRate.subtract(NumberUtils.buyCurrency(entry.getAmount(), entry.getPurchaseRate(), 1));
		String formatted;

		switch (precisionMode) {
		case AppSettings.PRECISION_ADVANCED:
			formatted = NumberUtils.getCurrencyFormat(result, Defs.SCALE_SHOW_LONG, Defs.CURRENCY_CODE_BGN);
			break;

		case AppSettings.PRECISION_SIMPLE:
		default:
			formatted = NumberUtils.getCurrencyFormat(result, Defs.CURRENCY_CODE_BGN);
			break;
		}

		return UIUtils.toHtmlColor(formatted,
				result.compareTo(BigDecimal.ZERO) > 0 ? Defs.COLOR_OK_GREEN : Defs.COLOR_DANGER_RED);
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public void remove(WalletEntry object) {
		items.remove(object);
	}

	public WalletEntry remove(int position) {
		return items.remove(position);
	}

	public void setCurrencies(Multimap<String, CurrencyData> currencies) {
		this.currencies = currencies;
	}

	// @Override
	// public Filter getFilter() {
	// if (filter == null) {
	// filter = new CurrencyFilter();
	// }
	// return filter;
	// return null;
	// }

	public void setFilterBy(Set<Sources> sources) {
		this.sourcesFilter = sources;
	}

	// public void setSortBy(final int sortBy, final boolean sortByAscending) {
	// Collections.sort(items, new Comparator<CurrencyListRow>() {
	// @Override
	// public int compare(CurrencyListRow lhs, CurrencyListRow rhs) {
	// switch (sortBy) {
	// case AppSettings.SORTBY_CODE:
	// if (sortByAscending) {
	// return lhs.getCode().compareToIgnoreCase(rhs.getCode());
	// }
	// return rhs.getCode().compareToIgnoreCase(lhs.getCode());
	//
	// case AppSettings.SORTBY_NAME:
	// default:
	// if (sortByAscending) {
	// return StringUtils.emptyIfNull(lhs.getName())
	// .compareToIgnoreCase(StringUtils.emptyIfNull(rhs.getName()));
	// }
	// return StringUtils.emptyIfNull(rhs.getName())
	// .compareToIgnoreCase(StringUtils.emptyIfNull(lhs.getName()));
	// }
	// }
	// });
	// }

	// private class CurrencyFilter extends Filter {
	//
	// @Override
	// protected FilterResults performFiltering(CharSequence constraint) {
	// FilterResults results = new FilterResults();
	// List<CurrencyData> currenciesFiltered = Lists.newArrayList();
	// int filterBy = Integer.parseInt(constraint.toString());
	//
	// switch (filterBy) {
	// case AppSettings.FILTERBY_ALL:
	// currenciesFiltered.addAll(itemsImmutable);
	// break;
	// case AppSettings.FILTERBY_NONFIXED:
	// for (CurrencyData currency : itemsImmutable) {
	// // TODO
	// if (/* !currency.isFixed() */true) {
	// currenciesFiltered.add(currency);
	// }
	// }
	// break;
	// case AppSettings.FILTERBY_FIXED:
	// for (CurrencyData currency : itemsImmutable) {
	// // TODO
	// if (/* currency.isFixed() */true) {
	// currenciesFiltered.add(currency);
	// }
	// }
	// break;
	// }
	//
	// results.values = currenciesFiltered;
	// results.count = currenciesFiltered.size();
	// return results;
	// }
	//
	// @SuppressWarnings("unchecked")
	// @Override
	// protected void publishResults(CharSequence constraint, FilterResults
	// results) {
	// if (results.count > 0) {
	// items = (List<CurrencyData>) results.values;
	// }
	// }
	// }
}
