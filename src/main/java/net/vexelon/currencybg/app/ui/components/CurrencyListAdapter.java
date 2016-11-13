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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.common.CurrencyListRow;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.ui.UIFlags;
import net.vexelon.currencybg.app.ui.UIUtils;
import net.vexelon.currencybg.app.utils.NumberUtils;
import net.vexelon.currencybg.app.utils.StringUtils;

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
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(R.layout.currency_row_layout, parent, false);
		}

		// set texts
		CurrencyListRow row = items.get(position);
		if (row != null) {
			// country ID icon
			ImageView icon = (ImageView) v.findViewById(R.id.icon);
			int imageId = UIFlags.getResourceFromCode(row.getCode());
			if (imageId != -1) {
				icon.setImageResource(imageId);
			}

			setResText(v, R.id.name, row.getName());
			setResText(v, R.id.code, row.getCode());

			setResText(v, R.id.rate_src_1,
					sourcesFilter.contains(Sources.TAVEX) ? getColumnValue(row, Sources.TAVEX) : "", true);
			setResText(v, R.id.rate_src_2,
					sourcesFilter.contains(Sources.POLANA1) ? getColumnValue(row, Sources.POLANA1) : "", true);
			setResText(v, R.id.rate_src_3, sourcesFilter.contains(Sources.FIB) ? getColumnValue(row, Sources.FIB) : "",
					true);
		}
		return v;
	}

	private String getColumnValue(CurrencyListRow row, Sources source) {
		if (!row.getColumn(source).isPresent()) {
			return Defs.LONG_DASH;
		}

		CurrencyData currencyData = row.getColumn(source).get();

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
				String v = NumberUtils.scaleCurrency(rate, Defs.SCALE_SHOW_LONG);
				return UIUtils.toHtmlColor(v, color);
			// String first = v.substring(0, Math.min(v.length() - 3,
			// v.length()));
			// String second = v.substring(Math.min(v.length() - 3, v.length()),
			// v.length());
			// return UIUtils.toHtmlColor(first, color) + "<small>" + second +
			// "</small>";
			case AppSettings.PRECISION_SIMPLE:
			default:
				return UIUtils.toHtmlColor(NumberUtils.scaleCurrency(rate, Defs.SCALE_SHOW_SHORT), color);
			}
		}

		return Defs.LONG_DASH;
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

	private void setResText(View v, int id, CharSequence text, boolean isHtml) {
		TextView textView = (TextView) v.findViewById(id);
		textView.setText(isHtml ? Html.fromHtml(text.toString()) : text);
	}

	private void setResText(View v, int id, CharSequence text) {
		setResText(v, id, text, false);
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
}
