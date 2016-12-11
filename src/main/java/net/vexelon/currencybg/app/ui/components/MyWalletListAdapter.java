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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.models.MyWalletEntry;
import net.vexelon.currencybg.app.ui.UIUtils;

import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.Set;

public class MyWalletListAdapter extends ArrayAdapter<MyWalletEntry> {

	private final List<MyWalletEntry> itemsImmutable;
	private List<MyWalletEntry> items;
	private int precisionMode = AppSettings.PRECISION_SIMPLE;
	private Set<Sources> sourcesFilter = Sets.newHashSet();
	// private CurrencyFilter filter = null;

	/**
	 * @param context
	 * @param textViewResId
	 * @param items
	 *            Currency rows
	 * @param precisionMode
	 */
	public MyWalletListAdapter(Context context, int textViewResId, List<MyWalletEntry> items, int precisionMode) {
		super(context, textViewResId, items);
		this.itemsImmutable = Lists.newArrayList(items.iterator());
		this.items = items;
		this.precisionMode = precisionMode;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(R.layout.wallet_row_layout, parent, false);
		}

		MyWalletEntry row = items.get(position);

		Log.d(Defs.LOG_TAG, "*** " + row.toString());
		Log.d(Defs.LOG_TAG, "*** " + LocalDateTime.fromDateFields(row.getPurchaseTime()).toString("yyyy-MM-dd"));

		// TODO
		UIUtils.setFlagIcon(v, R.id.wallet_row_icon, row.getCode());
		UIUtils.setText(v, R.id.wallet_row_code, row.getCode());
		UIUtils.setText(v, R.id.wallet_row_amount, row.getAmount());
		UIUtils.setText(v, R.id.wallet_row_bought_on,
				LocalDateTime.fromDateFields(row.getPurchaseTime()).toString("yy/MM/dd"));
		UIUtils.setText(v, R.id.wallet_row_bought_at, "2.56");
		UIUtils.setText(v, R.id.wallet_row_current_value, "10000");

		return v;
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
