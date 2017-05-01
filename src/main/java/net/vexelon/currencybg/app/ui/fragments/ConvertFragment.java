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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.melnykov.fab.FloatingActionButton;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.ui.components.CalculatorWidget;
import net.vexelon.currencybg.app.ui.components.ConvertSourceListAdapter;
import net.vexelon.currencybg.app.ui.components.ConvertTargetListAdapter;
import net.vexelon.currencybg.app.utils.NumberUtils;

import java.math.BigDecimal;
import java.util.List;

public class ConvertFragment extends AbstractFragment {

	private Spinner spinnerSourceCurrency;
	private TextView sourceValueView;
	private ListView targetCurrenciesView;

	private List<CurrencyData> currencies = Lists.newArrayList();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.rootView = inflater.inflate(R.layout.fragment_convert, container, false);
		init(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		/*
		 * Back from Settings or another activity, so we reload all currencies.
		 */
		updateUI(true);
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
			updateUI(true);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// add refresh currencies menu option
		inflater.inflate(R.menu.convert, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_clear:
			final AppSettings appSettings = new AppSettings(getActivity());
			appSettings.setLastConvertValue("0");
			appSettings.setLastConvertCurrencySel("BGN");
			appSettings.setConvertCurrencies(Sets.<String> newHashSet());
			updateUI(false);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void init(View view) {
		final AppSettings appSettings = new AppSettings(getActivity());

		// setup source currencies
		spinnerSourceCurrency = (Spinner) view.findViewById(R.id.source_currency);
		spinnerSourceCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (updateTargetCurrenciesCalculations()) {
					// save if value is valid
					CurrencyData sourceCurrency = (CurrencyData) spinnerSourceCurrency.getSelectedItem();
					appSettings.setLastConvertCurrencySel(sourceCurrency.getCode());
					setSourceCurrencyValue(appSettings.getLastConvertValue(), sourceCurrency.getCode());
				}

			}

			public void onNothingSelected(android.widget.AdapterView<?> parent) {
				// do nothing
			}
		});

		// source value
		sourceValueView = (TextView) view.findViewById(R.id.text_source_value2);
		sourceValueView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new CalculatorWidget(getActivity()).showCalculator(appSettings.getLastConvertValue(),
						new CalculatorWidget.Listener() {

							@Override
							public void onValue(BigDecimal value) {
								setSourceCurrencyValue(value.toPlainString(), appSettings.getLastConvertCurrencySel());

								if (updateTargetCurrenciesCalculations()) {
									// save if value is valid
									appSettings.setLastConvertValue(value.toPlainString());
								}

							}
						});
			}
		});

		// setup target currencies list
		targetCurrenciesView = (ListView) view.findViewById(R.id.list_target_currencies);
		targetCurrenciesView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				ConvertTargetListAdapter adapter = (ConvertTargetListAdapter) targetCurrenciesView.getAdapter();

				CurrencyData removed = adapter.remove(position);
				if (removed != null) {
					adapter.notifyDataSetChanged();

					appSettings.removeConvertCurrency(removed);
					showSnackbar(getActivity().getString(R.string.action_currency_removed, removed.getCode()));

					vibrate(Defs.VIBRATE_DEL_DURATION);
				}

				return false;
			}
		});
		targetCurrenciesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showSnackbar(getActivity().getString(R.string.hint_currency_remove));
			}
		});

		// add button
		FloatingActionButton action = (FloatingActionButton) view.findViewById(R.id.fab_convert);
		action.attachToListView(targetCurrenciesView);
		action.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				newAddTargetCurrencyDialog().show();
			}
		});
	}

	private void updateUI(boolean reload) {
		final Activity activity = getActivity();

		if (reload) {
			currencies.clear();
			// add dummy BGN for convert purposes
			currencies.add(getBGNCurrency());
			// load all currencies from database
			currencies.addAll(getVisibleCurrencies(getCurrencies(activity, true)));
		}

		ConvertSourceListAdapter adapter = new ConvertSourceListAdapter(activity, android.R.layout.simple_spinner_item,
				currencies);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSourceCurrency.setAdapter(adapter);

		final AppSettings appSettings = new AppSettings(activity);

		int position = adapter.getSelectedCurrencyPosition(appSettings.getLastConvertCurrencySel());
		if (position > 0) {
			spinnerSourceCurrency.setSelection(position);
		}

		setSourceCurrencyValue(appSettings.getLastConvertValue(), appSettings.getLastConvertCurrencySel());
		updateTargetCurrenciesListView();
	}

	/**
	 * Fetches stored target currencies and adds them to the convert list
	 */
	private void updateTargetCurrenciesListView() {
		final AppSettings appSettings = new AppSettings(getActivity());

		List<CurrencyData> targetCurrencies = Lists.newArrayList();

		// add all previously added target currencies (TODO: optimize this crap)
		List<CurrencyData> convertCurrencies = appSettings.getConvertCurrencies();
		for (CurrencyData c1 : currencies) {
			for (CurrencyData c2 : convertCurrencies) {
				if (c1.getCode().equals(c2.getCode()) && c1.getSource() == c2.getSource()) {
					targetCurrencies.add(c1);
				}
			}
		}

		ConvertTargetListAdapter adapter = new ConvertTargetListAdapter(getActivity(),
				R.layout.convert_target_row_layout, targetCurrencies, appSettings.getCurrenciesPrecision());
		targetCurrenciesView.setAdapter(adapter);
	}

	/**
	 * Performs convert calculations and displays results
	 */
	private boolean updateTargetCurrenciesCalculations() {
		ConvertTargetListAdapter adapter = (ConvertTargetListAdapter) targetCurrenciesView.getAdapter();
		CurrencyData toCurrency = (CurrencyData) spinnerSourceCurrency.getSelectedItem();

		if (adapter != null && toCurrency != null) {
			try {
				adapter.updateConvert(toCurrency, getSourceCurrencyValue());
				adapter.notifyDataSetChanged();
				return true;
			} catch (Exception e) {
				Log.w(Defs.LOG_TAG, "Could not parse source currency value! ", e);
			}
		}

		return false;
	}

	private void setSourceCurrencyValue(String value, String currencyCode) {
		sourceValueView.setText(formatCurrency(getActivity(), value, currencyCode));
	}

	private BigDecimal getSourceCurrencyValue() {
		return NumberUtils.getCurrencyValue(sourceValueView.getText().toString(),
				new AppSettings(getActivity()).getLastConvertCurrencySel());
	}

	/**
	 * Displays currencies selection dialog
	 *
	 * @return
	 */
	private MaterialDialog newAddTargetCurrencyDialog() {
		final Context context = getActivity();
		ConvertSourceListAdapter adapter = new ConvertSourceListAdapter(context, android.R.layout.simple_spinner_item,
				currencies);

		return new MaterialDialog.Builder(context).title(R.string.action_addcurrency).cancelable(true)
				.adapter(adapter, new MaterialDialog.ListCallback() {
					@Override
					public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
						CurrencyData currencyData = currencies.get(which);
						if (currencyData != null) {
							new AppSettings(context).addConvertCurrency(currencyData);

							// notify UI
							updateTargetCurrenciesListView();
							updateTargetCurrenciesCalculations();

							showSnackbar(context.getString(R.string.action_currency_added, currencyData.getCode()));
						}
						dialog.dismiss();
					}
				}).build();
	}

}
