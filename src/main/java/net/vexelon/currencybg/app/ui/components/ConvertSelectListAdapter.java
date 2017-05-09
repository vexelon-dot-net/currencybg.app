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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.Lists;

import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.ui.UIUtils;
import net.vexelon.currencybg.app.ui.UiCodes;

import java.util.List;

public class ConvertSelectListAdapter extends ArrayAdapter<CurrencyData> {

	private List<CurrencyData> items;
	private List<CurrencyData> selected;

	public ConvertSelectListAdapter(Context context, int textViewResId, List<CurrencyData> items) {
		super(context, textViewResId, items);
		this.items = items;
		this.selected = Lists.newArrayList();
	}

	private View _getView(int position, final View convertView, ViewGroup parent) {
		ViewHolder holder = null;

		View v = convertView;
		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(R.layout.convert_select_row_layout, parent, false);

			holder = new ViewHolder();
			holder.checkBox = (CheckBox) v.findViewById(R.id.convert_checked);
			holder.checkBox.setChecked(false);
			holder.icon = (ImageView) v.findViewById(R.id.convert_icon);
			holder.code = (TextView) v.findViewById(R.id.convert_code);
			holder.name = (TextView) v.findViewById(R.id.convert_name);
			holder.source = (TextView) v.findViewById(R.id.convert_source);

			v.setTag(holder);

			/*
			 * Handles taps that occur on the row itself
			 */
			v.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ViewHolder holder = (ViewHolder) view.getTag();
					holder.checkBox.toggle();

					CurrencyData currencyData = (CurrencyData) holder.checkBox.getTag();

					if (holder.checkBox.isChecked()) {
						selected.add(currencyData);
					} else {
						selected.remove(currencyData);
					}

				}
			});

			/*
			 * Handles taps that occur only on the checkbox. This is a bit
			 * tricky, because we need to notify the view that something has
			 * changed, otherwise the checkboxes will not be flagged.
			 */
			holder.checkBox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					CheckBox checkBox = (CheckBox) view;
					CurrencyData currencyData = (CurrencyData) view.getTag();

					if (checkBox.isChecked()) {
						selected.add(currencyData);
					} else {
						selected.remove(currencyData);
					}

					notifyDataSetChanged();
				}
			});

			// holder.checkBox.setOnCheckedChangeListener(new
			// CompoundButton.OnCheckedChangeListener() {
			// @Override
			// public void onCheckedChanged(CompoundButton compoundButton,
			// boolean isChecked) {
			// CurrencyData cd = (CurrencyData) compoundButton.getTag();
			// Log.d(Defs.LOG_TAG, "CHECKED2: " + cd.getCode());
			// if (isChecked) {
			// selected.add(cd);
			// } else {
			// selected.remove(cd);
			// }
			// notifyDataSetChanged();
			// }
			// });
		} else {
			holder = (ViewHolder) v.getTag();
		}

		CurrencyData row = items.get(position);

		holder.checkBox.setChecked(selected.contains(row));
		holder.checkBox.setTag(row);

		UIUtils.setFlagIcon(holder.icon, row.getCode());
		holder.code.setText(row.getCode());
		holder.name.setText(UiCodes.getCurrencyName(getContext().getResources(), row.getCode()));
		holder.source.setText(Sources.getName(row.getSource(), getContext()));

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

	/**
	 * Get all checked currencies
	 * 
	 * @return
	 */
	public List<CurrencyData> getSelected() {
		return selected;
	}

	/**
	 * ViewHolder pattern
	 */
	private static class ViewHolder {
		public CheckBox checkBox;
		public ImageView icon;
		public TextView code;
		public TextView name;
		public TextView source;
	}
}
