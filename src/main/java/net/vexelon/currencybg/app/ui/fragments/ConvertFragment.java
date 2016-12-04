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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;
import com.melnykov.fab.FloatingActionButton;

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

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.db.DataSource;
import net.vexelon.currencybg.app.db.DataSourceException;
import net.vexelon.currencybg.app.db.SQLiteDataSource;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.ui.components.CalculatorWidget;
import net.vexelon.currencybg.app.ui.components.ConvertSourceListAdapter;
import net.vexelon.currencybg.app.ui.components.ConvertTargetListAdapter;
import net.vexelon.currencybg.app.utils.IOUtils;
import net.vexelon.currencybg.app.utils.NumberUtils;

import org.joda.time.LocalDate;

public class ConvertFragment extends AbstractFragment {

	private Spinner spinnerSourceCurrency;
	private TextView tvSourceValue;
	private ListView lvTargetCurrencies;

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
		refreshUIData();
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
			refreshUIData();
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
		tvSourceValue = (TextView) view.findViewById(R.id.text_source_value2);
		tvSourceValue.setOnClickListener(new OnClickListener() {
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
		lvTargetCurrencies = (ListView) view.findViewById(R.id.list_target_currencies);
		lvTargetCurrencies.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				ConvertTargetListAdapter adapter = (ConvertTargetListAdapter) lvTargetCurrencies.getAdapter();

				CurrencyData removed = adapter.remove(position);
				if (removed != null) {
					adapter.notifyDataSetChanged();

					appSettings.removeConvertCurrency(removed);
					showSnackbar(getActivity().getString(R.string.action_currency_removed, removed.getCode()));
				}

				return false;
			}
		});
		lvTargetCurrencies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showSnackbar(getActivity().getString(R.string.hint_currency_remove));
			}
		});

		// add button
		FloatingActionButton action = (FloatingActionButton) view.findViewById(R.id.fab_convert);
		action.attachToListView(lvTargetCurrencies);
		action.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddCurrencyMenu().show();
			}
		});
	}

	private void refreshUIData() {
		final AppSettings appSettings = new AppSettings(getActivity());

		currencies = getCurrencies();
		if (!currencies.isEmpty()) {
			ConvertSourceListAdapter adapter = new ConvertSourceListAdapter(getActivity(),
					android.R.layout.simple_spinner_item, currencies);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerSourceCurrency.setAdapter(adapter);
			spinnerSourceCurrency
					.setSelection(adapter.getSelectedCurrencyPosition(appSettings.getLastConvertCurrencySel()));
		}

		setSourceCurrencyValue(appSettings.getLastConvertValue(), appSettings.getLastConvertCurrencySel());
		updateTargetCurrenciesListView();
	}

	/**
	 * Fetches stored target currencies and adds them to the convert list
	 */
	private void updateTargetCurrenciesListView() {
		final AppSettings appSettings = new AppSettings(getActivity());

		List<CurrencyData> targetCurrencyList = Lists.newArrayList();

		// add all previously added target currencies (TODO: optimize this crap)
		List<CurrencyData> convertCurrencies = appSettings.getConvertCurrencies();
		for (CurrencyData c1 : currencies) {
			for (CurrencyData c2 : convertCurrencies) {
				if (c1.getCode().equals(c2.getCode()) && c1.getSource() == c2.getSource()) {
					targetCurrencyList.add(c1);
				}
			}
		}

		ConvertTargetListAdapter adapter = new ConvertTargetListAdapter(getActivity(),
				R.layout.convert_target_row_layout, targetCurrencyList, appSettings.getCurrenciesPrecision());
		lvTargetCurrencies.setAdapter(adapter);
	}

	/**
	 * Performs convert calculations and displays results
	 */
	private boolean updateTargetCurrenciesCalculations() {
		ConvertTargetListAdapter adapter = (ConvertTargetListAdapter) lvTargetCurrencies.getAdapter();
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
		tvSourceValue.setText(formatCurrency(value, currencyCode));
	}

	private BigDecimal getSourceCurrencyValue() {
		return NumberUtils.getCurrencyValue(tvSourceValue.getText().toString(),
				new AppSettings(getActivity()).getLastConvertCurrencySel());
	}

	/**
	 * Displays currencies selection dialog
	 *
	 * @return
	 */
	private MaterialDialog showAddCurrencyMenu() {
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

	/**
	 * Fetches a sorted list of last downloaded currencies from the database
	 *
	 * @return
	 */
	private List<CurrencyData> getCurrencies() {
		DataSource source = null;
		List<CurrencyData> currencies = Lists.newArrayList();
		try {
			source = new SQLiteDataSource();
			source.connect(getActivity());
			currencies = source.getLastRates();
		} catch (DataSourceException e) {
			showSnackbar(R.string.error_db_load, Defs.TOAST_ERR_TIME, true);
			Log.e(Defs.LOG_TAG, "Could not load currencies from database!", e);
		} finally {
			IOUtils.closeQuitely(source);
		}

		addBGNToCurrencyList(currencies);

		// sort by code
		Collections.sort(currencies, new Comparator<CurrencyData>() {
			@Override
			public int compare(CurrencyData lhs, CurrencyData rhs) {
				return lhs.getCode().compareToIgnoreCase(rhs.getCode());
			}
		});

		return currencies;
	}

	/**
	 * Adds fictional BGN currency to the convert list
	 *
	 * @param currencies
	 * @return modified {@code currencies} list.
	 */
	private List<CurrencyData> addBGNToCurrencyList(List<CurrencyData> currencies) {
		CurrencyData currency = new CurrencyData();
		currency.setCode("BGN");
		currency.setRatio(1);
		currency.setBuy("1");
		currency.setSell("1");
		currency.setSource(0);
		currency.setDate(LocalDate.now().toString());

		currencies.add(currency);
		return currencies;
	}

}
