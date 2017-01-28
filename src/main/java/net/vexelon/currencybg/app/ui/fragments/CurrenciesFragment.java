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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.DataSource;
import net.vexelon.currencybg.app.db.DataSourceException;
import net.vexelon.currencybg.app.db.SQLiteDataSource;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.remote.APISource;
import net.vexelon.currencybg.app.remote.SourceException;
import net.vexelon.currencybg.app.ui.UIUtils;
import net.vexelon.currencybg.app.ui.components.CurrencyListAdapter;
import net.vexelon.currencybg.app.utils.DateTimeUtils;
import net.vexelon.currencybg.app.utils.IOUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class CurrenciesFragment extends AbstractFragment {

	private static boolean sortByAscending = true;

	private ListView lvCurrencies;
	private TextView tvLastUpdate;
	private TextView tvCurrenciesRate;
	private String lastUpdateLastValue;
	private CurrencyListAdapter currencyListAdapter;
	private Map<Sources, TextView> tvSources = Maps.newHashMap();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.rootView = inflater.inflate(R.layout.fragment_main, container, false);
		init(rootView, inflater);
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		final AppSettings appSettings = new AppSettings(getActivity());
		Resources resources = getActivity().getResources();

		if (appSettings.getLastReadNewsId() != resources.getInteger(R.integer.news_last)) {
			final TextView tvMessage = new TextView(getActivity());
			tvMessage.setText(Html.fromHtml(resources.getString(R.string.news_messages)));
			tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
			tvMessage.setPadding(24, 24, 24, 12);

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.news_title)
					.setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).setCancelable(false).setView(tvMessage).show();

			appSettings.setLastReadNewsId(resources.getInteger(R.integer.news_last));
		}
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
		case R.id.action_rate:
			newRateMenu().show();
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
		tvLastUpdate = (TextView) view.findViewById(R.id.text_last_update);
		tvSources.put(Sources.TAVEX, (TextView) view.findViewById(R.id.header_src_1));
		tvSources.put(Sources.POLANA1, (TextView) view.findViewById(R.id.header_src_2));
		tvSources.put(Sources.FIB, (TextView) view.findViewById(R.id.header_src_3));

		tvCurrenciesRate = (TextView) view.findViewById(R.id.header_currencies_rate);
		tvCurrenciesRate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				newRateMenu().show();
			}
		});
	}

	public void setLastUpdate(String lastUpdate) {
		tvLastUpdate.setText(lastUpdate);
	}

	private MaterialDialog newRateMenu() {
		final Activity activity = getActivity();
		final AppSettings appSettings = new AppSettings(activity);

		return new MaterialDialog.Builder(getActivity()).title(R.string.action_rate_title)
				.items(R.array.action_rate_values).itemsCallbackSingleChoice(appSettings.getCurrenciesRateSelection(),
						new MaterialDialog.ListCallbackSingleChoice() {
							@Override
							public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
								appSettings.setCurrenciesRateSelection(which);
								setCurrenciesRate(activity, which);
								// notify user
								switch (appSettings.getCurrenciesRateSelection()) {
								case AppSettings.RATE_SELL:
									showSnackbar(R.string.action_rate_sell_desc);
									break;
								case AppSettings.RATE_BUY:
								default:
									showSnackbar(R.string.action_rate_buy_desc);
									break;
								}
								return true;
							}
						})
				.build();
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
								setCurrenciesSort(which);
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
				.items(R.array.currency_sources_full)
				.itemsCallbackMultiChoice(toSourcesFilterIndices(appSettings.getCurrenciesFilter()),
						new MaterialDialog.ListCallbackMultiChoice() {
							@Override
							public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
								if (which.length == 0) {
									showSnackbar(R.string.error_filter_selection, Defs.TOAST_ERR_TIME, true);
									return false;
								}

								Set<Sources> sources = getSourcesFilterIndices(which);
								appSettings.setCurrenciesFilter(sources);
								setCurrenciesSourcesFilter(sources);

								// notify user
								String selected = "";
								for (int i : which) {
									if (!selected.isEmpty()) {
										selected += ", ";
									}
									selected += getResources().getStringArray(R.array.currency_sources)[i];
								}
								showSnackbar(getResources().getString(R.string.action_filter_desc, selected));

								return true;
							}
						})
				.positiveText(R.string.text_ok).build();
	}

	/**
	 * Converts checkbox sources selection to a {@link Sources} set.
	 *
	 * @param indices
	 *            {@code 0..n}
	 * @return
	 */
	private Set<Sources> getSourcesFilterIndices(Integer[] indices) {
		Set<Sources> result = Sets.newHashSet();
		int[] sources = getResources().getIntArray(R.array.currency_sources_ids);

		for (int i : indices) {
			result.add(Sources.valueOf(sources[i]));
		}

		return result;
	}

	private Integer[] toSourcesFilterIndices(Set<Sources> sources) {
		int[] sourcesIdx = getResources().getIntArray(R.array.currency_sources_ids);
		Set<Integer> result = Sets.newHashSet();

		for (Sources source : sources) {
			for (int i = 0; i < sourcesIdx.length; i++) {
				if (sourcesIdx[i] == source.getID()) {
					result.add(i);
					break;
				}
			}
		}

		return result.toArray(new Integer[0]);
	}

	/**
	 * Populates the list of currencies
	 *
	 * @param activity
	 * @param currencies
	 */
	private void updateCurrenciesListView(final Activity activity, List<CurrencyData> currencies) {
		AppSettings appSettings = new AppSettings(activity);

		currencyListAdapter = new CurrencyListAdapter(activity, R.layout.currency_row_layout,
				toCurrencyRows(activity, getVisibleCurrencies(currencies)), appSettings.getCurrenciesPrecision(),
				appSettings.getCurrenciesRateSelection(), appSettings.getCurrenciesFilter());
		lvCurrencies.setAdapter(currencyListAdapter);

		updateCurrenciesRateTitle(activity, appSettings.getCurrenciesRateSelection());
		updateCurrenciesSourcesTitles(appSettings.getCurrenciesFilter());
		setCurrenciesSort(appSettings.getCurrenciesSortSelection());

		tvLastUpdate.setText(DateTimeUtils.toDateText(activity, appSettings.getLastUpdateDate().toDate()));
	}

	/**
	 * Sorts currencies by given criteria
	 *
	 * @param sortBy
	 */
	private void setCurrenciesSort(final int sortBy) {
		currencyListAdapter.setSortBy(sortBy, sortByAscending);
		currencyListAdapter.notifyDataSetChanged();
	}

	private void updateCurrenciesRateTitle(final Activity activity, final int rateBy) {
		switch (rateBy) {
		case AppSettings.RATE_SELL:
			tvCurrenciesRate.setText(Html.fromHtml(
					UIUtils.toHtmlColor(activity.getString(R.string.sell).toUpperCase(), Defs.COLOR_DARK_ORANGE)));
			break;
		case AppSettings.RATE_BUY:
		default:
			tvCurrenciesRate.setText(Html.fromHtml(
					UIUtils.toHtmlColor(activity.getString(R.string.buy).toUpperCase(), Defs.COLOR_NAVY_BLUE)));
			break;
		}
	}

	/**
	 * Shows buy/sell currencies rate info
	 *
	 * @param activity
	 * @param rateBy
	 */
	private void setCurrenciesRate(final Activity activity, final int rateBy) {
		updateCurrenciesRateTitle(activity, rateBy);
		currencyListAdapter.setRateBy(rateBy);
		currencyListAdapter.notifyDataSetChanged();
	}

	private void updateCurrenciesSourcesTitles(Set<Sources> sources) {
		// toggle header visibility
		for (TextView tv : tvSources.values()) {
			tv.setVisibility(View.INVISIBLE);
		}

		for (Sources source : sources) {
			tvSources.get(source).setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Filter currencies by given sources
	 *
	 * @param sources
	 */
	private void setCurrenciesSourcesFilter(Set<Sources> sources) {
		updateCurrenciesSourcesTitles(sources);
		if (currencyListAdapter != null) {
			currencyListAdapter.setFilterBy(sources);
			currencyListAdapter.notifyDataSetChanged();
		}
		// currencyListAdapter.getFilter().filter(Integer.toString(filterBy),
		// new Filter.FilterListener() {
		// @Override
		// public void onFilterComplete(int count) {
		// if (count > 0) {
		// // adapter.sortBy(new
		// // AppSettings(getActivity()).getCurrenciesSortSelection(),
		// // sortByAscending);
		// currencyListAdapter.notifyDataSetChanged();
		// } else {
		// currencyListAdapter.notifyDataSetInvalidated();
		// }
		// }
		// });
	}

	/**
	 * Reloads currencies from a remote source.
	 *
	 * @param useRemoteSource
	 */
	public void reloadRates(boolean useRemoteSource) {
		final Activity activity = getActivity();

		if (!useRemoteSource) {
			DataSource source = null;
			try {
				source = new SQLiteDataSource();
				source.connect(activity);

				List<CurrencyData> ratesList = source.getLastRates();

				if (!ratesList.isEmpty()) {
					Log.v(Defs.LOG_TAG, "Displaying rates from database...");
					updateCurrenciesListView(activity, ratesList);
				} else {
					useRemoteSource = true;
				}
			} catch (DataSourceException e) {
				Log.e(Defs.LOG_TAG, "Could not load currencies from database!", e);
				showSnackbar(R.string.error_db_load, Defs.TOAST_ERR_TIME, true);
			} finally {
				IOUtils.closeQuitely(source);
			}
		}

		if (useRemoteSource) {
			setRefreshActionButtonState(true);
			new UpdateRatesTask().execute();
		}
	}

	private class UpdateRatesTask extends AsyncTask<Void, Void, List<CurrencyData>> {

		private Activity activity;
		private DateTime lastUpdate;
		private boolean updateOK = false;
		private boolean downloadFixed = false;
		private int msgId = R.string.error_download_rates;

		public UpdateRatesTask() {
			activity = CurrenciesFragment.this.getActivity();
		}

		@Override
		protected void onPreExecute() {
			lastUpdate = new AppSettings(activity).getLastUpdateDate();
		}

		@Override
		protected List<CurrencyData> doInBackground(Void... params) {
			Log.v(Defs.LOG_TAG, "Downloading rates from remote source...");

			DataSource source = null;
			List<CurrencyData> currencies = Lists.newArrayList();

			try {
				String iso8601Time = lastUpdate.toString();

				Log.d(Defs.LOG_TAG, "Downloading all rates since " + iso8601Time + " onwards...");

				// format, e.g., "2016-11-09T01:00:06+03:00"
				currencies = new APISource().getAllCurrentRatesAfter(iso8601Time);
				if (!currencies.isEmpty()) {

					source = new SQLiteDataSource();
					source.connect(activity);
					source.addRates(currencies);

					// reload merged currencies
					currencies = source.getLastRates();

					updateOK = true;
				} else {
					msgId = R.string.error_no_entries;
				}
			} catch (SourceException e) {
				Log.e(Defs.LOG_TAG, "Error fetching currencies from remote!", e);
				if (e.isMaintenanceError()) {
					msgId = R.string.error_maintenance;
				}
			} catch (DataSourceException e) {
				Log.e(Defs.LOG_TAG, "Could not save currencies to database!", e);
				msgId = R.string.error_db_save;
			} finally {
				IOUtils.closeQuitely(source);
			}

			return currencies;
		}

		@Override
		protected void onPostExecute(List<CurrencyData> result) {
			setRefreshActionButtonState(false);

			if (updateOK) {
				updateCurrenciesListView(activity, result);

				// bump last update
				lastUpdate = DateTime.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone(Defs.DATE_TIMEZONE_SOFIA)));
				Log.d(Defs.LOG_TAG, "Last rate download on " + lastUpdate.toString());
				new AppSettings(activity).setLastUpdateDate(lastUpdate);

				lastUpdateLastValue = DateTimeUtils.toDateText(activity, lastUpdate.toDate());
			} else if (msgId == R.string.error_no_entries) {
				showSnackbar(msgId, Defs.TOAST_INFO_TIME, false);
			} else {
				showSnackbar(msgId, Defs.TOAST_ERR_TIME, true);
			}

			tvLastUpdate.setText(lastUpdateLastValue);
		}

	}
}