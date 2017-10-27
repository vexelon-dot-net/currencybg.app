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

import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.ui.utils.UIUtils;
import net.vexelon.currencybg.app.ui.utils.CurrencyCodes;

import java.util.List;

public class ConvertSourceListAdapter extends ArrayAdapter<CurrencyData> {

	private List<CurrencyData> items;

	public ConvertSourceListAdapter(Context context, int textViewResId, List<CurrencyData> items) {
		super(context, textViewResId, items);
		this.items = items;
	}

	private View _getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(R.layout.convert_source_row_layout, parent, false);
		}

		CurrencyData row = items.get(position);

		UIUtils.setFlagIcon(v, R.id.convert_icon, row.getCode());
		UIUtils.setText(v, R.id.convert_code, row.getCode());
		UIUtils.setText(v, R.id.convert_name, CurrencyCodes.getCurrencyName(getContext().getResources(), row.getCode()));
		UIUtils.setText(v, R.id.convert_source, Sources.getName(getContext(), row.getSource()));

		return v;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return _getView(position, convertView, parent);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return _getView(position, convertView, parent);
	}

	public int getSelectedCurrencyPosition(String currencyCode) {
		for (int i = 0; i < items.size(); i++) {
			if (currencyCode.equals(items.get(i).getCode())) {
				return i;
			}
		}

		return -1;
	}

}
