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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.Lists;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.common.CurrencyListRow;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.ui.UIFlags;
import net.vexelon.currencybg.app.utils.NumberUtils;
import net.vexelon.currencybg.app.utils.StringUtils;

public class CurrencyListAdapter extends ArrayAdapter<CurrencyListRow> {

	private final List<CurrencyListRow> itemsImmutable;
	private List<CurrencyListRow> items;
	private int precisionMode = AppSettings.PRECISION_SIMPLE;
	private int rateBy = AppSettings.RATE_BUY;
	// private CurrencyFilter filter = null;

	public CurrencyListAdapter(Context context, int textViewResId, List<CurrencyListRow> items, int precisionMode,
			int rateBy) {
		super(context, textViewResId, items);
		this.itemsImmutable = Lists.newArrayList(items.iterator());
		this.items = items;
		this.precisionMode = precisionMode;
		this.rateBy = rateBy;
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
			setResText(v, R.id.rate_src_1, getColumnValue(row, Sources.TAVEX));
			setResText(v, R.id.rate_src_2, getColumnValue(row, Sources.POLANA1));
			setResText(v, R.id.rate_src_3, getColumnValue(row, Sources.FIB));
		}
		return v;
	}

	private String getColumnValue(CurrencyListRow row, Sources source) {
		if (!row.getColumn(source).isPresent()) {
			return Defs.LONG_DASH;
		}

		String value;
		if (rateBy == AppSettings.RATE_SELL) {
			value = row.getColumn(source).get().getSell();
		} else {
			value = row.getColumn(source).get().getBuy();
		}

		// cleanup faulty characters
		value = value.replace(",", "");

		if (!value.isEmpty()) {
			switch (precisionMode) {
			case AppSettings.PRECISION_ADVANCED:
				return value.substring(0, Math.min(value.length(), Defs.SCALE_SHOW_LONG));
			// return NumberUtils.scaleCurrency(new BigDecimal(value),
			// Defs.SCALE_SHOW_LONG);
			// String rate = NumberUtils.scaleCurrency(rateDecimal,
			// Defs.SCALE_SHOW_LONG);
			// setResText(v, R.id.rate, rate.substring(0, rate.length() -
			// 3));
			// setResText(v, R.id.rate_decimals,
			// rate.substring(rate.length() - 3));
			case AppSettings.PRECISION_SIMPLE:
			default:
				return value.substring(0, Math.min(value.length(), 4));
			}
		}

		return Defs.LONG_DASH;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Filter getFilter() {
		// if (filter == null) {
		// filter = new CurrencyFilter();
		// }
		// return filter;
		return null;
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

	private void setResText(View v, int id, CharSequence text) {
		TextView textView = (TextView) v.findViewById(id);
		textView.setText(text);
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
