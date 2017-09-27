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
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
import net.vexelon.currencybg.app.remote.SourceException;
import net.vexelon.currencybg.app.ui.UIUtils;
import net.vexelon.currencybg.app.ui.components.CurrencyListAdapter;
import net.vexelon.currencybg.app.ui.components.CurrencySelectListAdapter;
import net.vexelon.currencybg.app.utils.DateTimeUtils;
import net.vexelon.currencybg.app.utils.IOUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class CurrenciesFragment extends AbstractFragment {

	private static boolean sortByAscending = true;

	private ListView currenciesView;
	private TextView lastUpdateView;
	private TextView currenciesRateView;
	private String lastUpdateLastValue;
	private CurrencyListAdapter currencyListAdapter;
	private List<CurrencyData> rates = Lists.newArrayList();
	private List<TextView> sourceViews = Lists.newArrayList();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.rootView = inflater.inflate(R.layout.fragment_main, container, false);
		init(rootView, inflater);
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		final Context context = getActivity();
		final AppSettings appSettings = new AppSettings(context);
		Resources resources = context.getResources();

		// check against latest known version
		if (appSettings.getLastReadNewsId() != resources.getInteger(R.integer.news_last)) {
			// reset voting counter on new version
			appSettings.setUserAppUses(0);

			showNewsAlert(context);
			appSettings.setLastReadNewsId(resources.getInteger(R.integer.news_last));
		}
	}

	@Override
	public void onResume() {
		new ReloadRatesTask(false).execute();
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
			new ReloadRatesTask(true).execute();

			lastUpdateLastValue = lastUpdateView.getText().toString();
			lastUpdateView.setText(R.string.last_update_updating_text);
			setRefreshActionButtonState(true);
			return true;

		case R.id.action_rate:
			if (currencyListAdapter != null) {
				newRateMenu().show();
			}
			return true;

		case R.id.action_sort:
			newSortMenu().show();
			return true;

		case R.id.action_sources:
			newSourcesMenu().show();
			return true;

		case R.id.action_share:
			newShareCurrenciesDialog().show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void init(View view, LayoutInflater inflater) {
		currenciesView = (ListView) view.findViewById(R.id.list_currencies);
		currenciesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Sources source = Sources.valueOf((int) id);
				if (source != null) {
					newDetailsDialog(currencyListAdapter.getItem(position), source).show();
				}
			}
		});

		lastUpdateView = (TextView) view.findViewById(R.id.text_last_update);

		sourceViews.add((TextView) view.findViewById(R.id.header_src_1));
		sourceViews.add((TextView) view.findViewById(R.id.header_src_2));
		sourceViews.add((TextView) view.findViewById(R.id.header_src_3));

		currenciesRateView = (TextView) view.findViewById(R.id.header_currencies_rate);
		currenciesRateView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currencyListAdapter != null) {
					newRateMenu().show();
				}
			}
		});

		view.findViewById(R.id.header_src_1).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				newSourcesMenu().show();
			}
		});
		view.findViewById(R.id.header_src_2).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				newSourcesMenu().show();
			}
		});
		view.findViewById(R.id.header_src_3).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				newSourcesMenu().show();
			}
		});
	}

	public void setLastUpdate(String lastUpdate) {
		lastUpdateView.setText(lastUpdate);
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

	private MaterialDialog newSourcesMenu() {
		final AppSettings appSettings = new AppSettings(getActivity());
		return new MaterialDialog.Builder(getActivity()).title(R.string.action_sources_title)
				.items(R.array.currency_sources_full)
				.itemsCallbackMultiChoice(toSourcesFilterIndices(appSettings.getCurrenciesFilter()),
						new MaterialDialog.ListCallbackMultiChoice() {
							@Override
							public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
								if (which.length == 0) {
									showSnackbar(R.string.error_sources_selection, Defs.TOAST_ERR_DURATION, true);
									return false;
								} else if (which.length > 3) {
									showSnackbar(R.string.error_sources_selection_max, Defs.TOAST_ERR_DURATION, true);
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
								showSnackbar(getResources().getString(R.string.action_sources_desc, selected));

								return true;
							}
						})
				.positiveText(R.string.text_ok).build();
	}

	/**
	 * Displays a comparison of currencies from a single row
	 *
	 * @param row
	 * @param source
	 * @return
	 */
	private MaterialDialog newDetailsDialog(final CurrencyListRow row, final Sources source) {
		final Context context = getActivity();
		final AppSettings appSettings = new AppSettings(context);

		final MaterialDialog dialog = new MaterialDialog.Builder(context)
				.title(getResources().getString(R.string.action_currency_details, row.getCode(),
						Sources.getFullName(context, source.getID())))
				.cancelable(true).customView(R.layout.dialog_details, true).build();

		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialogInterface) {
				View v = dialog.getCustomView();
				UIUtils.setText(v, R.id.details_header,
						getResources().getString(R.string.text_rates_details, row.getName()), true);

				if (row.getColumn(source).isPresent()) {
					DataSource dataSource = null;
					try {
						dataSource = new SQLiteDataSource();
						dataSource.connect(context);

						String buyText = UIUtils.toHtmlColor(getResources().getString(R.string.buy),
								Defs.COLOR_NAVY_BLUE);
						String sellText = UIUtils.toHtmlColor(getResources().getString(R.string.sell),
								Defs.COLOR_DARK_ORANGE);

						StringBuilder buffer = new StringBuilder();

						List<CurrencyData> rates = dataSource.getAllRates(row.getCode(), source.getID());
						for (CurrencyData next : rates) {
							buffer.append(DateTimeUtils.toDateTimeText(context,
									DateTimeUtils.parseStringToDate(next.getDate())));
							buffer.append("<br>");

							buffer.append("&emsp;").append(buyText).append(" - ");
							buffer.append(CurrencyListAdapter.getColumnValue(next, AppSettings.RATE_BUY,
									appSettings.getCurrenciesPrecision()));
							buffer.append("<br>");

							buffer.append("&emsp;").append(sellText).append(" - ");
							buffer.append(CurrencyListAdapter.getColumnValue(next, AppSettings.RATE_SELL,
									appSettings.getCurrenciesPrecision()));
							buffer.append("<br>");
						}

						UIUtils.setText(v, R.id.details_content, buffer.toString(), true);
					} catch (DataSourceException e) {
						Log.e(Defs.LOG_TAG, "Error fetching currencies from database!", e);
						showSnackbar(R.string.error_db_load, Defs.TOAST_ERR_DURATION, true);
					} finally {
						IOUtils.closeQuitely(dataSource);
					}
				} else {
					UIUtils.setText(v, R.id.details_content, Defs.LONG_DASH);
				}
			}
		});

		return dialog;
	}

	/**
	 * Displays share currencies dialog
	 *
	 * @return
	 */
	private MaterialDialog newShareCurrenciesDialog() {
		final Context context = getActivity();
		final CurrencySelectListAdapter adapter = new CurrencySelectListAdapter(context,
				android.R.layout.simple_spinner_item, rates);

		return new MaterialDialog.Builder(context).title(R.string.action_addcurrency).cancelable(true)
				.adapter(adapter, null).negativeText(R.string.text_cancel).positiveText(R.string.text_ok)
				.onPositive(new MaterialDialog.SingleButtonCallback() {

					@Override
					public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
						if (!adapter.getSelected().isEmpty()) {
							final AppSettings appSettings = new AppSettings(context);

							StringBuilder buffer = new StringBuilder();
							buffer.append(getString(R.string.text_code)).append(Defs.TAB_2)
									.append(getString(R.string.buy)).append(Defs.TAB_2).append(getString(R.string.sell))
									.append(Defs.TAB_2).append(getString(R.string.text_source)).append(Defs.TAB_2)
									.append(Defs.NEWLINE);

							for (CurrencyData currency : adapter.getSelected()) {
								buffer.append(currency.getCode()).append(Defs.TAB_2);
								buffer.append(currency.getBuy()).append(Defs.TAB_2);
								buffer.append(currency.getSell()).append(Defs.TAB_2);
								buffer.append(Sources.getName(context, currency.getSource())).append(Defs.NEWLINE);
							}

							// app url footer
							buffer.append(Defs.NEWLINE)
									.append(getString(R.string.action_share_footer, appSettings.getAppUrl()));

							Intent sendIntent = new Intent();
							sendIntent.setAction(Intent.ACTION_SEND);
							sendIntent.putExtra(Intent.EXTRA_TEXT, buffer.toString());
							sendIntent.setType("text/plain");
							startActivity(sendIntent);
						}
					}
				}).build();
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
		currenciesView.setAdapter(currencyListAdapter);

		updateCurrenciesRateTitle(activity, appSettings.getCurrenciesRateSelection());
		updateCurrenciesSourcesTitles(appSettings.getCurrenciesFilter());
		setCurrenciesSort(appSettings.getCurrenciesSortSelection());

		lastUpdateView.setText(DateTimeUtils.toDateText(activity, appSettings.getLastUpdateDate().toDate()));
	}

	/**
	 * Sorts currencies by given criteria
	 *
	 * @param sortBy
	 */
	private void setCurrenciesSort(final int sortBy) {
		if (currencyListAdapter != null) {
			currencyListAdapter.setSortBy(sortBy, sortByAscending);
			currencyListAdapter.notifyDataSetChanged();
		}
	}

	private void updateCurrenciesRateTitle(Activity activity, int rateBy) {
		switch (rateBy) {
		case AppSettings.RATE_SELL:
			currenciesRateView.setText(Html.fromHtml(
					UIUtils.toHtmlColor(activity.getString(R.string.sell).toUpperCase(), Defs.COLOR_DARK_ORANGE)));
			break;
		case AppSettings.RATE_BUY:
		default:
			currenciesRateView.setText(Html.fromHtml(
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
	private void setCurrenciesRate(Activity activity, int rateBy) {
		updateCurrenciesRateTitle(activity, rateBy);
		currencyListAdapter.setRateBy(rateBy);
		currencyListAdapter.notifyDataSetChanged();
	}

	/**
	 * Toggles header visibility
	 *
	 * @param sources
	 */
	private void updateCurrenciesSourcesTitles(Set<Sources> sources) {
		for (TextView v : sourceViews) {
			v.setVisibility(View.INVISIBLE);
		}

		int i = 0;
		for (Sources source : sources) {
			sourceViews.get(i).setVisibility(View.VISIBLE);
			sourceViews.get(i).setText(Sources.getName(getActivity(), source.getID()));
			i += 1;
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
	 * Reloads currencies from database or triggers remote server download, if
	 * specified.
	 * 
	 */
	private class ReloadRatesTask extends AsyncTask<Void, Void, List<CurrencyData>> {

		private Activity activity;
		private boolean useRemoteSource;
		private int msgId = -1; // no error

		public ReloadRatesTask(boolean useRemoteSource) {
			this.useRemoteSource = useRemoteSource;
			this.activity = CurrenciesFragment.this.getActivity();
		}

		@Override
		protected List<CurrencyData> doInBackground(Void... params) {
			Log.v(Defs.LOG_TAG, "Loading rates from database...");
			List<CurrencyData> result = Lists.newArrayList();

			if (!useRemoteSource) {
				DataSource source = null;
				try {
					source = new SQLiteDataSource();
					source.connect(activity);

					result.addAll(source.getLastRates());
				} catch (DataSourceException e) {
					Log.e(Defs.LOG_TAG, "Could not load currencies from database!", e);
					msgId = R.string.error_db_load;
				} finally {
					IOUtils.closeQuitely(source);
				}
			}

			return result;
		}

		@Override
		protected void onPostExecute(List<CurrencyData> result) {
			if (msgId == -1) {
				// cache loaded currencies
				rates = getSortedCurrencies(getVisibleCurrencies(Lists.newArrayList(result)));

				if (!result.isEmpty()) {
					Log.v(Defs.LOG_TAG, "Displaying rates from database...");
					updateCurrenciesListView(activity, result);
				} else {
					useRemoteSource = true;
				}
			} else {
				showSnackbar(msgId, Defs.TOAST_ERR_DURATION, true);
			}

			if (useRemoteSource) {
				setRefreshActionButtonState(true);
				new UpdateRatesTask().execute();
			}
		}

	}

	/**
	 * Downloads currencies from remote server
	 * 
	 */
	private class UpdateRatesTask extends AsyncTask<Void, Void, List<CurrencyData>> {

		private Activity activity;
		private DateTime lastUpdate;
		private boolean updateOK = false;
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
				source = new SQLiteDataSource();
				source.connect(activity);

				// latest currency update regardless of the source
				// format, e.g., "2016-11-09T01:00:06+02:00"
				DateTime from = source.getLastRatesDownloadTime().or(lastUpdate);

				/**
				 * Manual updates only fetch the currencies since the
				 * last/latest currency update date time found in the database
				 * (regardless of the source). This might introduce a bug should
				 * users try to manually update on the next day after the last
				 * fetch, because the server fetches currencies from a given
				 * date at that specific day only. It does not fetch currencies
				 * for the next day of the given date. To counter this we force
				 * an update from the beginning of the day (00:00), if the last
				 * update was one day ago (we count from midnight on and not a
				 * 24h period).
				 */
				DateTime today = DateTime.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone(Defs.DATE_TIMEZONE_SOFIA)));
				if (today.toLocalDate().isAfter(from.toLocalDate())) {
					from = today.withTime(0, 0, 0, 0);
				}

				String iso8601Time = from.toString();
				Log.d(Defs.LOG_TAG, "Downloading all rates since " + iso8601Time + " ...");

				currencies = new APISource(activity).getAllCurrentRatesAfter(iso8601Time);
				if (!currencies.isEmpty()) {
					source.addRates(currencies);
					// reload merged currencies
					currencies = source.getLastRates();
					updateOK = true;
				} else {
					msgId = R.string.error_no_entries;
				}
			} catch (SourceException e) {
				Log.e(Defs.LOG_TAG, "Error fetching currencies from remote! Code: " + e.getCode(), e);
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
				// cache loaded currencies
				rates = getSortedCurrencies(getVisibleCurrencies(Lists.newArrayList(result)));

				updateCurrenciesListView(activity, result);
				// bump last update
				lastUpdate = DateTime.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone(Defs.DATE_TIMEZONE_SOFIA)));
				Log.d(Defs.LOG_TAG, "Last rate download on " + lastUpdate.toString());
				new AppSettings(activity).setLastUpdateDate(lastUpdate);
				// visualise update date time
				lastUpdateLastValue = DateTimeUtils.toDateText(activity, lastUpdate.toDate());
			} else if (msgId == R.string.error_no_entries) {
				showSnackbar(msgId, Defs.TOAST_INFO_DURATION, false);
			} else {
				showSnackbar(msgId, Defs.TOAST_ERR_DURATION, true);
			}

			lastUpdateView.setText(lastUpdateLastValue);
		}

	}
}