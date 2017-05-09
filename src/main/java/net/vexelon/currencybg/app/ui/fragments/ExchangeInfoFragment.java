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
package net.vexelon.currencybg.app.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;

import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.ui.components.InfoListAdapter;
import net.vexelon.currencybg.app.utils.StringUtils;

import java.util.List;

public class ExchangeInfoFragment extends AbstractFragment {

	private static final String URL_VERSION = "intr://verinfo";
	private static final String URL_3RDPARTY_LIBS = "intr://3rd";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.rootView = inflater.inflate(R.layout.fragment_info, container, false);
		init(rootView);
		return rootView;
	}

	private void init(View view) {
		final Activity activity = getActivity();
		ListView lvInfo = (ListView) view.findViewById(R.id.list_info);

		final InfoListAdapter adapter = new InfoListAdapter(getActivity(), R.layout.info_row, getInfosList());
		lvInfo.setAdapter(adapter);

		lvInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String url = adapter.getUrl(position);

				// TODO

				if (URL_VERSION.equals(url)) {
					showNewsAlert(getActivity());
				} else if (URL_3RDPARTY_LIBS.equals(url)) {
					new MaterialDialog.Builder(getActivity()).customView(R.layout.fragment_thirdparty_libs, true)
							.positiveText(R.string.text_ok).build().show();
				}
			}
		});
	}

	private List<InfoListAdapter.InfoItem> getInfosList() {
		final Activity activity = getActivity();
		List<InfoListAdapter.InfoItem> infoList = Lists.newArrayList();

		for (Sources source : Sources.values()) {
			if (source.isEnabled()) {
				String secondRow = source.getFullName(activity) + " - " + StringUtils.stripUrl(source.getWebAddress
						(activity));
				infoList.add(newInfoRow(source.getName(activity), secondRow));
			}
		}

		return infoList;
	}

	private InfoListAdapter.InfoItem newInfoRow(String name, String value, String url) {
		return new InfoListAdapter.InfoItem(name, value, url);
	}

	private InfoListAdapter.InfoItem newInfoRow(String name, String value) {
		return newInfoRow(name, value, null);
	}
}
