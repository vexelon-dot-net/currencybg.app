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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;
import com.melnykov.fab.FloatingActionButton;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.db.DataSource;
import net.vexelon.currencybg.app.db.DataSourceException;
import net.vexelon.currencybg.app.db.SQLiteDataSource;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.db.models.WalletEntry;
import net.vexelon.currencybg.app.ui.UIUtils;
import net.vexelon.currencybg.app.ui.components.ConvertSourceListAdapter;
import net.vexelon.currencybg.app.ui.components.WalletListAdapter;
import net.vexelon.currencybg.app.utils.DateTimeUtils;
import net.vexelon.currencybg.app.utils.IOUtils;
import net.vexelon.currencybg.app.utils.NumberUtils;

import org.joda.time.LocalDateTime;

import java.math.BigDecimal;
import java.util.List;

public class WalletFragment extends AbstractFragment
		implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

	private ListView walletListView;
	private TextView dateTimeView;
	private LocalDateTime dateTimeSelected;
	private String codeSelected = "";

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
		final Activity activity = getActivity();

		List<WalletEntry> entries = Lists.newArrayList();

		DataSource source = null;
		try {
			source = new SQLiteDataSource();
			source.connect(activity);
			entries.addAll(source.getWalletEntries());
		} catch (DataSourceException e) {
			Log.e(Defs.LOG_TAG, "Could not load wallet entries from database!", e);
			showSnackbar(R.string.error_db_load, Defs.TOAST_ERR_TIME, true);
		} finally {
			IOUtils.closeQuitely(source);
		}

		final AppSettings appSettings = new AppSettings(activity);

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
				.cancelable(true).customView(R.layout.walletentry_layout, true).autoDismiss(false)
				.positiveText(R.string.text_ok).onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						// validate and save new wallet entry
						EditText amountView = (EditText) dialog.getView().findViewById(R.id.wallet_entry_amount);
						EditText boughtAtView = (EditText) dialog.getView().findViewById(R.id.wallet_entry_bought_at);
						TextView boughtOnView = (TextView) dialog.getView().findViewById(R.id.wallet_entry_bought_on);

						String amount = amountView.getText().toString();
						String boughtAt = boughtAtView.getText().toString();
						String boughtOn = boughtOnView.getText().toString();

						// validation
						if (amount.isEmpty() || boughtAt.isEmpty() || boughtOn.isEmpty()) {
							showSnackbar(dialog.getView(), R.string.error_add_wallet_entry, Defs.TOAST_ERR_TIME, true);
							return;
						}

						WalletEntry entry = new WalletEntry();
						try {
							entry.setCode(codeSelected);
							entry.setAmount(new BigDecimal(amount).toPlainString());
							entry.setPurchaseRate(new BigDecimal(boughtAt).toPlainString());
							entry.setPurchaseTime(dateTimeSelected.toDate());
						} catch (NumberFormatException e) {
							Log.w(Defs.LOG_TAG, "Error reading amount of rate!", e);
							showSnackbar(dialog.getView(), R.string.error_add_wallet_entry, Defs.TOAST_ERR_TIME, true);
							return;
						}

						// all ok
						dialog.dismiss();

						DataSource source = null;
						try {
							source = new SQLiteDataSource();
							source.connect(dialog.getContext());

							Log.v(Defs.LOG_TAG, "Adding new wallet entry: " + entry.toString());
							source.addWalletEntry(entry);
						} catch (DataSourceException e) {
							Log.e(Defs.LOG_TAG, "Could not save wallet entry to database!", e);
							showSnackbar(R.string.error_db_save, Defs.TOAST_ERR_TIME, true);
						} finally {
							IOUtils.closeQuitely(source);
						}

						// reload currencies
						dialog.getView().post(new Runnable() {

							@Override
							public void run() {
								updateUI();
							}
						});
					}
				}).negativeText(R.string.text_cancel).onNegative(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
						materialDialog.dismiss();
					}
				}).build();

		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialogInterface) {
				final View v = dialog.getCustomView();

				// setup source currencies
				final Spinner spinner = (Spinner) v.findViewById(R.id.wallet_entry_currency);
				spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						CurrencyData currency = (CurrencyData) parent.getSelectedItem();
						codeSelected = currency.getCode();
						UIUtils.setText(v, R.id.wallet_entry_bought_at, currency.getBuy());
					}

					public void onNothingSelected(android.widget.AdapterView<?> parent) {
						// do nothing
					}
				});

				ConvertSourceListAdapter adapter = new ConvertSourceListAdapter(context,
						android.R.layout.simple_spinner_item, getVisibleCurrencies(getCurrencies(context, true)));
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(adapter);

				dateTimeSelected = LocalDateTime.now();
				dateTimeView = (TextView) v.findViewById(R.id.wallet_entry_bought_on);
				dateTimeView.setText(DateTimeUtils.toDateTimeText(v.getContext(), dateTimeSelected.toDate()));

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
		dateTimeSelected = dateTimeSelected.withYear(year).withMonthOfYear(monthOfYear).withDayOfMonth(dayOfMonth);
		// show time picker
		TimePickerDialog timePicker = TimePickerDialog.newInstance(WalletFragment.this, dateTimeSelected.getHourOfDay(),
				dateTimeSelected.getMinuteOfHour(), true);
		timePicker.setThemeDark(true);
		timePicker.show(getFragmentManager(), getResources().getText(R.string.text_pick_time).toString());
	}

	@Override
	public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
		dateTimeSelected.withHourOfDay(hourOfDay).withMinuteOfHour(minute).withSecondOfMinute(second);
		// set field
		dateTimeView.setText(DateTimeUtils.toDateTimeText(view.getActivity(), dateTimeSelected.toDate()));
	}
}