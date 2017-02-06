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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;
import com.melnykov.fab.FloatingActionButton;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.db.models.WalletEntry;
import net.vexelon.currencybg.app.ui.components.WalletListAdapter;
import net.vexelon.currencybg.app.utils.NumberUtils;

import org.joda.time.LocalDateTime;

import java.math.BigDecimal;
import java.util.List;

public class WalletFragment extends AbstractFragment
		implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

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
				WalletListAdapter adapter = (WalletListAdapter) walletListView.getAdapter();

				WalletEntry removed = adapter.remove(position);
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

		// add button
		FloatingActionButton action = (FloatingActionButton) view.findViewById(R.id.fab_wallet_entry);
		action.attachToListView(walletListView);
		action.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				newAddWalletEntryDialog().show();
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
		WalletEntry entry = new WalletEntry();
		entry.setCode("USD");
		entry.setAmount("2500");
		entry.setPurchaseRate("1.72");
		entry.setPurchaseTime(LocalDateTime.now().minusDays(1).toDate());

		List<WalletEntry> entries = Lists.newArrayList(entry);

		final Activity activity = getActivity();
		WalletListAdapter adapter = new WalletListAdapter(activity, android.R.layout.simple_spinner_item, entries,
				AppSettings.PRECISION_ADVANCED);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		walletListView.setAdapter(adapter);

	}

	/**
	 * Displays add wallet entry dialog
	 *
	 * @return
	 */
	private MaterialDialog newAddWalletEntryDialog() {
		final Context context = getActivity();
		final MaterialDialog dialog = new MaterialDialog.Builder(context).title(R.string.action_addwalletentry)
				.cancelable(true).customView(R.layout.walletentry_layout, true).positiveText(R.string.text_ok)
				.negativeText(R.string.text_cancel).onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						// TODO

					}
				}).build();

		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialogInterface) {
				View v = dialog.getCustomView();
				v.findViewById(R.id.wallet_entry_bought_on).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						LocalDateTime dateTime = LocalDateTime.now();
						DatePickerDialog datePicker = DatePickerDialog.newInstance(WalletFragment.this,
								dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfYear());
						datePicker.setThemeDark(true);
						datePicker.show(getFragmentManager(),
								getResources().getText(R.string.text_pick_date).toString());
					}
				});
			}
		});

		return dialog;
	}

	@Override
	public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
		// TODO
		LocalDateTime dateTime = LocalDateTime.now();
		TimePickerDialog timePicker = TimePickerDialog.newInstance(WalletFragment.this, dateTime.getHourOfDay(),
				dateTime.getMinuteOfHour(), true);
		timePicker.setThemeDark(true);
		timePicker.show(getFragmentManager(), getResources().getText(R.string.text_pick_time).toString());
	}

	@Override
	public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
		// TODO
	}
}