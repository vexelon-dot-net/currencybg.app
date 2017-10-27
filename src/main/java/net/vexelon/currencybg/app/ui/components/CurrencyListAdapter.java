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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.common.CurrencyListRow;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.ui.utils.UIUtils;
import net.vexelon.currencybg.app.utils.NumberUtils;
import net.vexelon.currencybg.app.utils.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class CurrencyListAdapter extends ArrayAdapter<CurrencyListRow> {

	private final List<CurrencyListRow> itemsImmutable;
	private List<CurrencyListRow> items;
	private int precisionMode = AppSettings.PRECISION_SIMPLE;
	private int rateBy = AppSettings.RATE_BUY;
	private Set<Sources> sourcesFilter = Sets.newHashSet();
	// private CurrencyFilter filter = null;

	/**
	 * @param context
	 * @param textViewResId
	 * @param items
	 *            Currency rows
	 * @param precisionMode
	 * @param rateBy
	 *            Show buy or sell values
	 * @param sources
	 *            Sources to show currencies for
	 */
	public CurrencyListAdapter(Context context, int textViewResId, List<CurrencyListRow> items, int precisionMode,
			int rateBy, Set<Sources> sources) {
		super(context, textViewResId, items);

		this.itemsImmutable = Lists.newArrayList(items.iterator());
		this.items = items;
		this.precisionMode = precisionMode;
		this.rateBy = rateBy;
		this.sourcesFilter = sources;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		ViewHolder holder = null;
		final int[] ids = { R.id.rate_src_1, R.id.rate_src_2, R.id.rate_src_3 };

		View v = convertView;
		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(R.layout.currency_row_layout, parent, false);

			holder = new ViewHolder();
			holder.icon = v.findViewById(R.id.icon);
			holder.name = v.findViewById(R.id.name);
			holder.code = v.findViewById(R.id.code);
			holder.rates = new TextView[ids.length];
			for (int i = 0; i < ids.length; i++) {
				holder.rates[i] = v.findViewById(ids[i]);
			}

			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}

		CurrencyListRow row = items.get(position);

		UIUtils.setFlagIcon(holder.icon, row.getCode());
		holder.name.setText(row.getName());
		holder.code.setText(row.getCode());

		int i = 0;
		for (final Sources source : sourcesFilter) {
			holder.rates[i].setText(Html.fromHtml(getColumnValue(row, source)));
			/*
			 * Propagates tap event to parent
			 *
			 * @see http://stackoverflow.com/a/27595251
			 */
			v.findViewById(ids[i]).setOnClickListener((View view) -> {
				((ListView) parent).performItemClick(view, position, source.getID());
			});

			i += 1;
		}

		// clear unused columns
		for (int j = i; j < Defs.MAX_SOURCES_TO_SHOW; j++) {
			holder.rates[j].setText("");
		}

		return v;
	}

	public static String getColumnValue(CurrencyData currencyData, int rateBy, int precisionMode) {
		String value;
		String color;

		if (rateBy == AppSettings.RATE_SELL) {
			value = NumberUtils.cleanValue(currencyData.getSell());
			color = Defs.COLOR_DARK_ORANGE;
		} else {
			value = NumberUtils.cleanValue(currencyData.getBuy());
			color = Defs.COLOR_NAVY_BLUE;
		}

		if (!value.isEmpty()) {
			BigDecimal rate = NumberUtils.divCurrency(new BigDecimal(value), new BigDecimal(currencyData.getRatio()));

			switch (precisionMode) {
			case AppSettings.PRECISION_ADVANCED:
				String v = NumberUtils.getCurrencyFormat(rate, Defs.SCALE_SHOW_LONG, null);
				return UIUtils.toHtmlColor(v, color);
			// String first = v.substring(0, Math.min(v.length() - 3,
			// v.length()));
			// String second = v.substring(Math.min(v.length() - 3, v.length()),
			// v.length());
			// return UIUtils.toHtmlColor(first, color) + "<small>" + second +
			// "</small>";

			case AppSettings.PRECISION_SIMPLE:
			default:
				return UIUtils.toHtmlColor(NumberUtils.getCurrencyFormat(rate, Defs.SCALE_SHOW_SHORT, null), color);
			}
		}

		return Defs.LONG_DASH;
	}

	private String getColumnValue(CurrencyListRow row, Sources source) {
		if (!row.getColumn(source).isPresent()) {
			return Defs.LONG_DASH;
		}

		return getColumnValue(row.getColumn(source).get(), rateBy, precisionMode);
	}

	@Override
	public int getCount() {
		return items.size();
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

	public void setRateBy(final int rateBy) {
		this.rateBy = rateBy;
	}

	public void setSortBy(final int sortBy, final boolean sortByAscending) {
		Collections.sort(items, new Comparator<CurrencyListRow>() {
			@Override
			public int compare(CurrencyListRow lhs, CurrencyListRow rhs) {
				switch (sortBy) {
				case AppSettings.SORTBY_CODE:
					if (sortByAscending) {
						return lhs.getCode().compareToIgnoreCase(rhs.getCode());
					}
					return rhs.getCode().compareToIgnoreCase(lhs.getCode());

				case AppSettings.SORTBY_NAME:
				default:
					if (sortByAscending) {
						return StringUtils.emptyIfNull(lhs.getName())
								.compareToIgnoreCase(StringUtils.emptyIfNull(rhs.getName()));
					}
					return StringUtils.emptyIfNull(rhs.getName())
							.compareToIgnoreCase(StringUtils.emptyIfNull(lhs.getName()));
				}
			}
		});
	}

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

	/**
	 * ViewHolder pattern
	 */
	private static class ViewHolder {
		public ImageView icon;
		public TextView code;
		public TextView name;
		public TextView source;
		public TextView[] rates;
	}
}
