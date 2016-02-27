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

import java.util.List;
import java.util.Map;

import net.vexelon.currencybg.app.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class InfoListAdapter extends ArrayAdapter<Map<String, String>> {

	public static final String ROW_NAME = "_name";
	public static final String ROW_VALUE = "_value";
	public static final String ROW_URL = "_url";

	private List<Map<String, String>> items;

	public InfoListAdapter(Context context, int textViewResId, List<Map<String, String>> items) {
		super(context, textViewResId, items);
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(R.layout.info_row, parent, false);
		}

		Map<String, String> item = items.get(position);
		setResText(v, R.id.info_row1, item.get(ROW_NAME));
		setResText(v, R.id.info_row2, item.get(ROW_VALUE));
		if (item.containsKey(ROW_URL)) {
			// TODO
		}

		return v;
	}

	public String getUrl(int position) {
		return items.get(position).get(ROW_URL);
	}

	private void setResText(View v, int id, CharSequence text) {
		TextView tx = (TextView) v.findViewById(id);
		if (tx != null) {
			tx.setText(text);
		}
	}

}
