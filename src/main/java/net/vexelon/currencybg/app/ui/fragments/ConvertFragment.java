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
import java.math.MathContext;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.melnykov.fab.FloatingActionButton;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.db.DataSource;
import net.vexelon.currencybg.app.db.DataSourceException;
import net.vexelon.currencybg.app.db.SQLiteDataSource;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.db.models.CurrencyLocales;
import net.vexelon.currencybg.app.ui.components.ConvertSourceListAdapter;
import net.vexelon.currencybg.app.ui.components.ConvertTargetListAdapter;
import net.vexelon.currencybg.app.utils.IOUtils;

public class ConvertFragment extends AbstractFragment {

	private Spinner spinnerSourceCurrency;
	private EditText etSourceValue;
	private ListView lvTargetCurrencies;
	private List<CurrencyData> currenciesList = Lists.newArrayList();
	private Map<String, CurrencyData> currenciesMap = Maps.newHashMap();

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
			 * Back from Currencies fragment view, so we reload all
			 * currencies. The user might have updated them.
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
		// setup source currencies
		spinnerSourceCurrency = (Spinner) view.findViewById(R.id.source_currency);
		spinnerSourceCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (updateTargetCurrenciesCalculations()) {
					// save if value is valid
					CurrencyData sourceCurrency = (CurrencyData) spinnerSourceCurrency.getSelectedItem();
					new AppSettings(getActivity()).setLastConvertCurrencySel(sourceCurrency.getCode());
				}

			}

			public void onNothingSelected(android.widget.AdapterView<?> parent) {
			};
		});
		// setup source value
		etSourceValue = (EditText) view.findViewById(R.id.text_source_value);
		etSourceValue.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				etSourceValue.setSelection(etSourceValue.getText().length());
			}
		});
		etSourceValue.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (updateTargetCurrenciesCalculations()) {
					// save if value is valid
					new AppSettings(getActivity()).setLastConvertValue(etSourceValue.getText().toString());
				}
			}
		});
		// setup target currencies list
		lvTargetCurrencies = (ListView) view.findViewById(R.id.list_target_currencies);
		lvTargetCurrencies.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				ConvertTargetListAdapter adapter = (ConvertTargetListAdapter) lvTargetCurrencies.getAdapter();
				CurrencyData removed = adapter.remove(position);
				adapter.notifyDataSetChanged();
				if (removed != null) {
					new AppSettings(getActivity()).removeConvertCurrency(removed.getCode());
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
		FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_convert);
		fab.attachToListView(lvTargetCurrencies);
		fab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddCurrencyMenu().show();
			}
		});
	}

	private void refreshUIData() {
		final AppSettings appSettings = new AppSettings(getActivity());
		currenciesList = getCurrenciesList();
		if (!currenciesList.isEmpty()) {
			currenciesMap = getCurreniesMap(currenciesList);
			ConvertSourceListAdapter adapter = new ConvertSourceListAdapter(getActivity(),
					android.R.layout.simple_spinner_item, currenciesList);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerSourceCurrency.setAdapter(adapter);
			spinnerSourceCurrency
					.setSelection(adapter.getSelectedCurrencyPosition(appSettings.getLastConvertCurrencySel()));
		}
		etSourceValue.setText(appSettings.getLastConvertValue());
		updateTargetCurrenciesListView();
	}

	/**
	 * Fetches stored target currencies and adds them to the convert list
	 */
	private void updateTargetCurrenciesListView() {
		final AppSettings appSettings = new AppSettings(getActivity());
		List<CurrencyData> targetCurrencyList = Lists.newArrayList();
		for (String currencyCode : appSettings.getConvertCurrencies()) {
			if (currenciesMap.containsKey(currencyCode)) {
				targetCurrencyList.add(currenciesMap.get(currencyCode));
			}
		}
		ConvertTargetListAdapter adapter = new ConvertTargetListAdapter(getActivity(),
				R.layout.convert_target_row_layout, targetCurrencyList, true, appSettings.getCurrenciesPrecision());
		lvTargetCurrencies.setAdapter(adapter);
	}

	/**
	 * Does convert calculations and displays results
	 */
	private boolean updateTargetCurrenciesCalculations() {
		ConvertTargetListAdapter adapter = (ConvertTargetListAdapter) lvTargetCurrencies.getAdapter();
		CurrencyData sourceCurrency = (CurrencyData) spinnerSourceCurrency.getSelectedItem();
		if (sourceCurrency != null) {
			MathContext mathContext = new MathContext(Defs.SCALE_CALCULATIONS);
			try {
				BigDecimal value = new BigDecimal(etSourceValue.getText().toString(), mathContext);
				adapter.updateValues(sourceCurrency, value);
				adapter.notifyDataSetChanged();
				return true;
			} catch (Exception e) {
				Log.w(Defs.LOG_TAG, "Could not parse source currency value! " + e.getMessage());
			}
		}
		return false;
	}

	/**
	 * Displays a currencies selection dialog
	 * 
	 * @return
	 */
	private MaterialDialog showAddCurrencyMenu() {
		final Context context = getActivity();
		ConvertTargetListAdapter adapter = new ConvertTargetListAdapter(context, R.layout.convert_target_row_layout,
				currenciesList, false, new AppSettings(context).getCurrenciesPrecision());
		return new MaterialDialog.Builder(context).title(R.string.action_addcurrency).cancelable(true)
				.adapter(adapter, new MaterialDialog.ListCallback() {
					@Override
					public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
						CurrencyData currencyData = currenciesList.get(which);
						if (currencyData != null) {
							// TODO: check if already added
							new AppSettings(context).addConvertCurrency(currencyData.getCode());
							showSnackbar(context.getString(R.string.action_currency_added, currencyData.getCode()));
							updateTargetCurrenciesListView();
							updateTargetCurrenciesCalculations();
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
	private List<CurrencyData> getCurrenciesList() {
		DataSource source = null;
		List<CurrencyData> currenciesList = Lists.newArrayList();
		try {
			source = new SQLiteDataSource();
			source.connect(getActivity());
			//TODO - temporary commented
//			currenciesList = source.getLastRates(getSelectedCurrenciesLocale());
//			currenciesList.addAll(source.getLastFixedRates(getSelectedCurrenciesLocale()));
		} catch (DataSourceException e) {
			showSnackbar(R.string.error_db_load_rates, Defs.TOAST_ERR_TIME);
			Log.e(Defs.LOG_TAG, "Could not load currencies from database!", e);
		} finally {
			IOUtils.closeQuitely(source);
		}
		addBGNToCurrencyList(currenciesList);
		// sort by name
		Collections.sort(currenciesList, new Comparator<CurrencyData>() {
			@Override
			public int compare(CurrencyData lhs, CurrencyData rhs) {
				return lhs.getName().compareToIgnoreCase(rhs.getName());
			}
		});
		return currenciesList;
	}

	/**
	 * Adds fictional BGN currency to the convert list
	 * 
	 * @param currencyList
	 */
	private void addBGNToCurrencyList(List<CurrencyData> currencyList) {
		CurrencyData currency = new CurrencyData();
		if (getSelectedCurrenciesLocale() == CurrencyLocales.BG) {
			currency.setName("Български лев");
		} else {
			currency.setName("Bulgarian Lev");
		}
		currency.setGold(1);
		currency.setCode("BGN");
		currency.setRatio(1);
		currency.setReverseRate("1");
		currency.setRate("1");
		currency.setCurrDate(new Date());
		currency.setfStar(0);
		currencyList.add(currency);
	}

}
