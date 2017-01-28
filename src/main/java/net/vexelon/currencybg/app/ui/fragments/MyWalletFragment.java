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

import com.google.common.collect.Lists;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.db.models.MyWalletEntry;
import net.vexelon.currencybg.app.ui.components.MyWalletListAdapter;
import net.vexelon.currencybg.app.utils.NumberUtils;

import org.joda.time.LocalDateTime;

import java.math.BigDecimal;
import java.util.List;

public class MyWalletFragment extends AbstractFragment {

	private ListView walletListView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.rootView = inflater.inflate(R.layout.fragment_wallet, container, false);
		init(rootView, inflater);
		return rootView;
	}

	private void init(View view, LayoutInflater inflater) {
		walletListView = (ListView) view.findViewById(R.id.list_wallet_entries);

		walletListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				MyWalletListAdapter adapter = (MyWalletListAdapter) walletListView.getAdapter();

				MyWalletEntry removed = adapter.remove(position);
				if (removed != null) {
					adapter.notifyDataSetChanged();

					// TODO remove
					showSnackbar(getActivity().getString(R.string.action_wallet_removed,
							NumberUtils.getCurrencyFormat(new BigDecimal(removed.getAmount()), removed.getCode())));
				}

				return false;
			}
		});
		walletListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showSnackbar(getActivity().getString(R.string.hint_currency_remove));
			}
		});

	}

	@Override
	public void onResume() {
		/*
		 * Back from Settings or another activity, so we reload all currencies.
		 */
		updateUI();
		super.onResume();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			/*
			 * Back from Currencies fragment view, so we reload all currencies.
			 * The user might have updated them.
			 */
			updateUI();
		}
	}

	private void updateUI() {
		final AppSettings appSettings = new AppSettings(getActivity());

		// TODO
		MyWalletEntry entry = new MyWalletEntry();
		entry.setCode("USD");
		entry.setAmount("2500");
		entry.setPurchaseRate("1.72");
		entry.setPurchaseTime(LocalDateTime.now().minusDays(1).toDate());

		List<MyWalletEntry> entries = Lists.newArrayList(entry);

		final Activity activity = getActivity();
		MyWalletListAdapter adapter = new MyWalletListAdapter(activity, android.R.layout.simple_spinner_item, entries,
				AppSettings.PRECISION_ADVANCED);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		walletListView.setAdapter(adapter);

	}
}