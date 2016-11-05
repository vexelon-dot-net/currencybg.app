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

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.common.CurrencyListRow;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.DataSource;
import net.vexelon.currencybg.app.db.DataSourceException;
import net.vexelon.currencybg.app.db.SQLiteDataSource;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.remote.APISource;
import net.vexelon.currencybg.app.remote.Source;
import net.vexelon.currencybg.app.remote.SourceException;
import net.vexelon.currencybg.app.ui.components.CurrencyListAdapter;
import net.vexelon.currencybg.app.utils.DateTimeUtils;
import net.vexelon.currencybg.app.utils.IOUtils;

public class CurrenciesFragment extends AbstractFragment {

	private static boolean sortByAscending = true;

	private ListView lvCurrencies;
	private TextView tvLastUpdate;
	private String lastUpdateLastValue;
	private CurrencyListAdapter currencyListAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.rootView = inflater.inflate(R.layout.fragment_main, container, false);
		init(rootView, inflater);
		return rootView;
	}

	@Override
	public void onResume() {
		reloadRates(false);
		super.onResume();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// add refresh currencies menu option
		inflater.inflate(R.menu.currencies, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			reloadRates(true);
			lastUpdateLastValue = tvLastUpdate.getText().toString();
			tvLastUpdate.setText(R.string.last_update_updating_text);
			setRefreshActionButtonState(true);
			return true;
		case R.id.action_sort:
			newSortMenu().show();
			return true;
		case R.id.action_filter:
			newFilterMenu().show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void init(View view, LayoutInflater inflater) {
		lvCurrencies = (ListView) view.findViewById(R.id.list_currencies);
		View header = inflater.inflate(R.layout.currency_row_header_layout, null);
		lvCurrencies.addHeaderView(header);

		tvLastUpdate = (TextView) view.findViewById(R.id.text_last_update);
	}

	private MaterialDialog newSortMenu() {
		final AppSettings appSettings = new AppSettings(getActivity());
		return new MaterialDialog.Builder(getActivity()).title(R.string.action_sort_title)
				.items(R.array.action_sort_values).itemsCallbackSingleChoice(appSettings.getCurrenciesSortSelection(),
						new MaterialDialog.ListCallbackSingleChoice() {
							@Override
							public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
								sortByAscending = appSettings.getCurrenciesSortSelection() != which ? true
										: !sortByAscending;
								appSettings.setCurrenciesSortSelection(which);
								sortCurrenciesListView(which);
								// notify user
								switch (appSettings.getCurrenciesSortSelection()) {
								case AppSettings.SORTBY_CODE:
									showSnackbar(sortByAscending ? R.string.action_sort_code_asc
											: R.string.action_sort_code_desc);
									break;
								case AppSettings.SORTBY_NAME:
								default:
									showSnackbar(sortByAscending ? R.string.action_sort_name_asc
											: R.string.action_sort_name_desc);
									break;
								}
								return true;
							}
						})
				.build();
	}

	private MaterialDialog newFilterMenu() {
		final AppSettings appSettings = new AppSettings(getActivity());
		return new MaterialDialog.Builder(getActivity()).title(R.string.action_filter_title)
				.items(R.array.action_filter_values).itemsCallbackSingleChoice(
						appSettings.getCurrenciesFilterSelection(), new MaterialDialog.ListCallbackSingleChoice() {
							@Override
							public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
								appSettings.setCurrenciesFilterSelection(which);
								filterCurrenciesListView(which);
								// notify user
								switch (appSettings.getCurrenciesFilterSelection()) {
								case AppSettings.FILTERBY_ALL:
									showSnackbar(R.string.action_filter_all);
									break;
								case AppSettings.FILTERBY_NONFIXED:
									showSnackbar(R.string.action_filter_nonfixed);
									break;
								case AppSettings.FILTERBY_FIXED:
									showSnackbar(R.string.action_filter_fixed);
									break;
								}
								return true;
							}
						})
				.build();
	}

	/**
	 * Populates the list of currencies
	 *
	 * @param currencies
	 */
	private void updateCurrenciesListView(List<CurrencyData> currencies) {
		final Activity activity = getActivity();
		AppSettings appSettings = new AppSettings(activity);

		currencyListAdapter = new CurrencyListAdapter(activity, R.layout.currency_row_layout,
				toCurrencyRows(currencies), appSettings.getCurrenciesPrecision());
		lvCurrencies.setAdapter(currencyListAdapter);

		// sortCurrenciesListView(appSettings.getCurrenciesSortSelection());
		// filterCurrenciesListView(appSettings.getCurrenciesFilterSelection());

		// Date lastUpdateDate = currencies.iterator().next().getCurrDate();
		Date lastUpdateDate = currencies.iterator().next().getDate();
		tvLastUpdate.setText(DateTimeUtils.toDateText(activity, lastUpdateDate));
	}

	/**
	 * Sorts currencies by given criteria
	 *
	 * @param sortBy
	 */
	private void sortCurrenciesListView(final int sortBy) {
		// currencyListAdapter.sortBy(new
		// AppSettings(getActivity()).getCurrenciesSortSelection(),
		// sortByAscending);
		currencyListAdapter.notifyDataSetChanged();
	}

	/**
	 * Filter currencies by rate type
	 *
	 * @param filterBy
	 */
	private void filterCurrenciesListView(final int filterBy) {
		currencyListAdapter.getFilter().filter(Integer.toString(filterBy), new Filter.FilterListener() {
			@Override
			public void onFilterComplete(int count) {
				if (count > 0) {
					// adapter.sortBy(new
					// AppSettings(getActivity()).getCurrenciesSortSelection(),
					// sortByAscending);
					currencyListAdapter.notifyDataSetChanged();
				} else {
					currencyListAdapter.notifyDataSetInvalidated();
				}
			}
		});
	}

	/**
	 * Reloads currencies from a remote source.
	 *
	 * @param useRemoteSource
	 */
	public void reloadRates(boolean useRemoteSource) {
		if (!useRemoteSource) {
			DataSource source = null;
			try {
				source = new SQLiteDataSource();
				source.connect(getActivity());

				List<CurrencyData> ratesList = source.getLastRates();
				if (!ratesList.isEmpty()) {
					Log.v(Defs.LOG_TAG, "Displaying rates from database...");
					updateCurrenciesListView(ratesList);
				} else {
					useRemoteSource = true;
				}
			} catch (DataSourceException e) {
				Log.e(Defs.LOG_TAG, "Could not load currencies from database!", e);
				showSnackbar(R.string.error_db_load_rates, Defs.TOAST_ERR_TIME);
			} finally {
				IOUtils.closeQuitely(source);
			}
		}

		if (useRemoteSource) {
			setRefreshActionButtonState(true);
			new UpdateRatesTask().execute();
		}
	}

	private List<CurrencyListRow> toCurrencyRows(List<CurrencyData> currencies) {
		Map<String, CurrencyListRow> map = Maps.newHashMap();

		for (CurrencyData c : currencies) {
			CurrencyListRow row = map.get(c.getCode());
			if (row == null) {
				row = new CurrencyListRow(c.getCode());
				map.put(c.getCode(), row);
			}

			System.out.println("Sourece " + c.getSource() + " = " + c.getCode());

			row.addColumn(Sources.valueOf(c.getSource()), c);
		}

		return Lists.newArrayList(map.values());
	}

	private class UpdateRatesTask extends AsyncTask<Void, Void, List<CurrencyData>> {

		private Activity activity;
		private boolean updateOK = false;
		private boolean downloadFixed = false;

		public UpdateRatesTask() {
			activity = CurrenciesFragment.this.getActivity();
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected List<CurrencyData> doInBackground(Void... params) {
			Log.v(Defs.LOG_TAG, "Loading rates from remote source...");

			List<CurrencyData> currencies = Lists.newArrayList();
			Source source = new APISource();

			try {
				currencies = source.getAllCurrentRatesAfter("2016-10-19T01:00:06+03:00");
				updateOK = true;
			} catch (SourceException e) {
				Log.e(Defs.LOG_TAG, "Error fetching currencies from remote!", e);
			}

			return currencies;
		}

		@Override
		protected void onPostExecute(List<CurrencyData> result) {
			setRefreshActionButtonState(false);

			if (updateOK && !result.isEmpty()) {
				DataSource source = null;
				try {
					source = new SQLiteDataSource();
					source.connect(activity);
					source.addRates(result);
				} catch (DataSourceException e) {
					Log.e(Defs.LOG_TAG, "Could not save currencies to database!", e);
					showSnackbar(R.string.error_db_load_rates, Defs.TOAST_ERR_TIME);
				} finally {
					IOUtils.closeQuitely(source);
				}
				updateCurrenciesListView(result);
			} else {
				tvLastUpdate.setText(lastUpdateLastValue);
				showSnackbar(R.string.error_download_rates, Defs.TOAST_ERR_TIME);
			}
		}

	}
}