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

public class InfoListAdapter extends ArrayAdapter<InfoListAdapter.InfoItem> {

	private List<InfoItem> items;

	public InfoListAdapter(Context context, int textViewResId, List<InfoItem> items) {
		super(context, textViewResId, items);
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(R.layout.info_row, parent, false);
		}

		InfoItem item = items.get(position);
		setResText(v, R.id.info_row1, item.getName());
		setResText(v, R.id.info_row2, item.getValue());

		return v;
	}

	public String getUrl(int position) {
		return items.get(position).getUrl();
	}

	private void setResText(View v, int id, CharSequence text) {
		TextView tx = (TextView) v.findViewById(id);
		if (tx != null) {
			tx.setText(text);
		}
	}

	public static class InfoItem {
		private String name;
		private String value;
		private String url;

		public InfoItem(String name, String value, String url) {
			this.name = name;
			this.value = value;
			this.url = url;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public String getUrl() {
			return url;
		}
	}
}
