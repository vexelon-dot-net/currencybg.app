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
import android.util.Pair;
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
import com.google.common.base.Supplier;
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
import net.vexelon.currencybg.app.ui.utils.UIUtils;
import net.vexelon.currencybg.app.ui.components.CurrencyListAdapter;
import net.vexelon.currencybg.app.ui.components.CurrencySelectListAdapter;
import net.vexelon.currencybg.app.ui.events.LoadListener;
import net.vexelon.currencybg.app.utils.DateTimeUtils;
import net.vexelon.currencybg.app.utils.IOUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class CurrenciesFragment extends AbstractFragment implements LoadListener<Pair<DateTime, List<CurrencyData>>> {

    private static boolean sortByAscending = true;

    private ListView currenciesView;
    private TextView lastUpdateView;
    private TextView filterView;
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
        reloadCurrencies(getActivity(), false, true);
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
        int itemId = item.getItemId();
        if (itemId == R.id.action_refresh) {
            reloadCurrencies(getActivity(), true, true);
            return true;
        } else if (itemId == R.id.action_rate) {
            if (currencyListAdapter != null) {
                newRateMenu().show();
            }
            return true;
        } else if (itemId == R.id.action_filter) {
            newFilterMenu().show();
            return true;
        } else if (itemId == R.id.action_sort) {
            newSortMenu().show();
            return true;
        } else if (itemId == R.id.action_sources) {
            newSourcesMenu().show();
            return true;
        } else if (itemId == R.id.action_share) {
            newShareCurrenciesDialog().show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void init(View view, LayoutInflater inflater) {
        currenciesView = view.findViewById(R.id.list_currencies);
        currenciesView.setOnItemClickListener((AdapterView<?> parent, View v, int position, long id) -> {
            Sources source = Sources.valueOf((int) id);
            if (source != null) {
                newDetailsDialog(currencyListAdapter.getItem(position), source).show();
            }
        });

        lastUpdateView = view.findViewById(R.id.text_last_update);
        filterView = view.findViewById(R.id.text_filter);
        filterView.setOnClickListener((View v) -> {
            if (currencyListAdapter != null) {
                newFilterMenu().show();
            }
        });

        sourceViews.add(view.findViewById(R.id.header_src_1));
        sourceViews.add(view.findViewById(R.id.header_src_2));
        sourceViews.add(view.findViewById(R.id.header_src_3));

        currenciesRateView = view.findViewById(R.id.header_currencies_rate);
        currenciesRateView.setOnClickListener((View v) -> {
            if (currencyListAdapter != null) {
                newRateMenu().show();
            }
        });

        view.findViewById(R.id.header_src_1).setOnClickListener((View v) -> {
            newSourcesMenu().show();
        });
        view.findViewById(R.id.header_src_2).setOnClickListener((View v) -> {
            newSourcesMenu().show();
        });
        view.findViewById(R.id.header_src_3).setOnClickListener((View v) -> {
            newSourcesMenu().show();
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
                        (MaterialDialog dialog, View view, int which, CharSequence text) -> {

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
                        })
                .build();
    }

    private MaterialDialog newFilterMenu() {
        final Activity activity = getActivity();
        final AppSettings appSettings = new AppSettings(activity);

        // custom must always be last
        final int CUSTOM_INDEX = 4; // getResources().getStringArray(R.array.action_filter_values).length;

        MaterialDialog newDialog = new MaterialDialog.Builder(getActivity()).title(R.string.action_filter_title)
                .items(R.array.action_filter_values).itemsCallbackSingleChoice(appSettings.getCurrenciesSortSelection(),
                        (MaterialDialog dialog, View view, int which, CharSequence text) -> {

                            switch (which) {
                                case AppSettings.CURRENCY_FILTER_NONE:
                                case AppSettings.CURRENCY_FILTER_CRYPTO:
                                case AppSettings.CURRENCY_FILTER_TOP6:
                                case AppSettings.CURRENCY_FILTER_TOP8:
                                    appSettings.setCurrenciesFilter(which);

                                    showSnackbar(getResources().getString(R.string.action_filter_desc, text));

                                    updateCurrenciesListView(activity, rates);
                                    return true;

                                case CUSTOM_INDEX: // AppSettings.CURRENCY_FILTER_CUSTOM
                                    getActivity().runOnUiThread(() -> {
                                        newCustomFilterDialog().show();
                                    });
                                    return true;
                            }

                            return false;
                        })
                .build();

        // default selection
        newDialog
                .setSelectedIndex(appSettings.getCurrenciesFilter() == AppSettings.CURRENCY_FILTER_CUSTOM ? CUSTOM_INDEX
                        : appSettings.getCurrenciesFilter());
        return newDialog;
    }

    private MaterialDialog newCustomFilterDialog() {
        final Activity activity = getActivity();
        final CurrencySelectListAdapter adapter = new CurrencySelectListAdapter(activity,
                android.R.layout.simple_spinner_item, getCurrenciesDistinct(rates, true), false);

        return new MaterialDialog.Builder(activity).title(R.string.action_addcurrency).cancelable(true)
                .adapter(adapter, null).negativeText(R.string.text_cancel).positiveText(R.string.text_ok)
                .onPositive((@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) -> {
                    List<CurrencyData> selected = adapter.getSelected();

                    if (!materialDialog.isCancelled() && !selected.isEmpty()) {
                        final AppSettings appSettings = new AppSettings(activity);

                        Set<String> codes = new HashSet<>(selected.size());
                        for (CurrencyData currency : selected) {
                            codes.add(currency.getCode());
                        }

                        appSettings.setCurrenciesFilterCustom(codes);
                        appSettings.setCurrenciesFilter(AppSettings.CURRENCY_FILTER_CUSTOM);

                        showSnackbar(getResources().getString(R.string.action_filter_desc,
                                getResources().getString(R.string.action_filter_custom)));

                        updateCurrenciesListView(activity, rates);
                    }
                }).build();
    }

    private MaterialDialog newSortMenu() {
        final AppSettings appSettings = new AppSettings(getActivity());

        return new MaterialDialog.Builder(getActivity()).title(R.string.action_sort_title)
                .items(R.array.action_sort_values).itemsCallbackSingleChoice(appSettings.getCurrenciesSortSelection(),
                        (MaterialDialog dialog, View view, int which, CharSequence text) -> {

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
                        })
                .build();
    }

    private MaterialDialog newSourcesMenu() {
        final AppSettings appSettings = new AppSettings(getActivity());
        return new MaterialDialog.Builder(getActivity()).title(R.string.action_sources_title)
                .items(R.array.currency_sources_full)
                .itemsCallbackMultiChoice(toSourcesFilterIndices(appSettings.getSourcesFilter()),
                        (MaterialDialog dialog, Integer[] which, CharSequence[] text) -> {

                            if (which.length == 0) {
                                showSnackbar(R.string.error_sources_selection, Defs.TOAST_ERR_DURATION, true);
                                return false;
                            } else if (which.length > 3) {
                                showSnackbar(R.string.error_sources_selection_max, Defs.TOAST_ERR_DURATION, true);
                                return false;
                            }

                            Set<Sources> sources = getSourcesFilterIndices(which);
                            appSettings.setSourcesFilter(sources);
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

        dialog.setOnShowListener((DialogInterface dialogInterface) -> {
            View v = dialog.getCustomView();
            UIUtils.setText(v, R.id.details_header,
                    getResources().getString(R.string.text_rates_details, row.getName()), true);

            if (row.getColumn(source).isPresent()) {
                try (DataSource dataSource = new SQLiteDataSource()) {
                    dataSource.connect(context);

                    String buyText = UIUtils.toHtmlColor(getResources().getString(R.string.buy), Defs.COLOR_NAVY_BLUE);
                    String sellText = UIUtils.toHtmlColor(getResources().getString(R.string.sell),
                            Defs.COLOR_DARK_ORANGE);

                    StringBuilder buffer = new StringBuilder();

                    List<CurrencyData> rates = dataSource.getAllRates(row.getCode(), source.getID());
                    for (CurrencyData next : rates) {
                        buffer.append(
                                DateTimeUtils.toDateTimeText(context, DateTimeUtils.parseStringToDate(next.getDate())));
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
                } catch (DataSourceException | IOException e) {
                    Log.e(Defs.LOG_TAG, "Error fetching currencies from database!", e);
                    showSnackbar(R.string.error_db_load, Defs.TOAST_ERR_DURATION, true);
                }
            } else {
                UIUtils.setText(v, R.id.details_content, Defs.LONG_DASH);
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
                .onPositive((@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) -> {

                    List<CurrencyData> selected = adapter.getSelected();

                    if (!materialDialog.isCancelled() && !selected.isEmpty()) {
                        final AppSettings appSettings = new AppSettings(context);

                        StringBuilder buffer = new StringBuilder();
                        buffer.append(getString(R.string.text_code)).append(Defs.TAB_2).append(getString(R.string.buy))
                                .append(Defs.TAB_2).append(getString(R.string.sell)).append(Defs.TAB_2)
                                .append(getString(R.string.text_source)).append(Defs.TAB_2).append(Defs.NEWLINE);

                        for (CurrencyData currency : selected) {
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
                }).build();
    }

    /**
     * Converts checkbox sources selection to a {@link Sources} set.
     *
     * @param indices {@code 0..n}
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
                toCurrencyRows(activity, getVisibleCurrencies(activity, currencies)),
                appSettings.getCurrenciesPrecision(), appSettings.getCurrenciesRateSelection(),
                appSettings.getSourcesFilter());
        currenciesView.setAdapter(currencyListAdapter);

        updateCurrenciesRateTitle(activity, appSettings.getCurrenciesRateSelection());
        updateCurrenciesSourcesTitles(appSettings.getSourcesFilter());
        setCurrenciesSort(appSettings.getCurrenciesSortSelection());

        lastUpdateView.setText(DateTimeUtils.toDateText(activity, appSettings.getLastUpdateDate().toDate()));

        // show filter, if selected
        if (appSettings.getCurrenciesFilter() == AppSettings.CURRENCY_FILTER_CUSTOM) {
            filterView.setText(getResources().getString(R.string.action_filter_custom));
        } else if (appSettings.getCurrenciesFilter() > 0) {
            filterView.setText(
                    getResources().getStringArray(R.array.action_filter_values)[appSettings.getCurrenciesFilter()]);
        } else {
            filterView.setText("");
        }
    }

    /**
     * Sorts currencies by given criteria
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

    private void reloadCurrencies(Activity activity, boolean useRemote, boolean showAnimation) {
        new ReloadRatesTask(activity, this, useRemote).execute();
        if (showAnimation) {
            lastUpdateLastValue = lastUpdateView.getText().toString();
            lastUpdateView
                    .setText(useRemote ? R.string.last_update_updating_text : R.string.last_update_reloading_text);
            setRefreshActionButtonState(true);
        }
    }

    /**
     * Shows buy/sell currencies rate info
     */
    private void setCurrenciesRate(Activity activity, int rateBy) {
        updateCurrenciesRateTitle(activity, rateBy);
        currencyListAdapter.setRateBy(rateBy);
        currencyListAdapter.notifyDataSetChanged();
    }

    /**
     * Toggles header visibility
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

    @Override
    public void onLoadStart() {
        setRefreshActionButtonState(true);
    }

    @Override
    public void onLoadSuccessful(@NonNull Supplier<Pair<DateTime, List<CurrencyData>>> newData) {
        final Activity activity = getActivity();
        if (activity != null) {
            setRefreshActionButtonState(false);

            // cache loaded currencies
            rates = newData.get().second;
            updateCurrenciesListView(getActivity(), rates);

            if (newData.get().first != null) {
                DateTime lastUpdate = newData.get().first;
                new AppSettings(activity).setLastUpdateDate(lastUpdate);

                // visualise update date time
                lastUpdateLastValue = DateTimeUtils.toDateText(activity, lastUpdate.toDate());
                lastUpdateView.setText(lastUpdateLastValue);
            }
        }
    }

    @Override
    public void onLoadFailed(int msgId) {
        setRefreshActionButtonState(false);
        lastUpdateView.setText(lastUpdateLastValue);

        if (msgId == R.string.error_no_entries) {
            showSnackbar(msgId, Defs.TOAST_INFO_DURATION, false);
        } else {
            showSnackbar(msgId, Defs.TOAST_ERR_DURATION, true);
        }
    }

    /**
     * Reloads currencies from database or triggers remote server download, if
     * specified.
     */
    private static class ReloadRatesTask extends AsyncTask<Void, Void, List<CurrencyData>> {

        private Activity activity;
        private LoadListener<Pair<DateTime, List<CurrencyData>>> listener;
        private boolean useRemoteSource;
        private int msgId = -1; // no error

        public ReloadRatesTask(Activity activity, LoadListener<Pair<DateTime, List<CurrencyData>>> listener,
                               boolean useRemoteSource) {
            this.activity = activity;
            this.listener = listener;
            this.useRemoteSource = useRemoteSource;
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
        protected void onPostExecute(final List<CurrencyData> result) {
            if (msgId != -1) {
                listener.onLoadFailed(msgId);
            } else {
                if (result.isEmpty() || useRemoteSource) {
                    new UpdateRatesTask(activity, listener).execute();
                } else {
                    Log.v(Defs.LOG_TAG, "Displaying rates from database...");
                    listener.onLoadSuccessful(() -> Pair.create(null, result));
                }
            }
        }
    }

    /**
     * Downloads currencies from remote server
     */
    private static class UpdateRatesTask extends AsyncTask<Void, Void, List<CurrencyData>> {

        private Activity activity;
        private LoadListener<Pair<DateTime, List<CurrencyData>>> listener;
        private DateTime lastUpdate;
        private boolean updateOK = false;
        private int msgId = R.string.error_download_rates;

        public UpdateRatesTask(Activity activity, LoadListener<Pair<DateTime, List<CurrencyData>>> listener) {
            this.activity = activity;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            listener.onLoadStart();
            lastUpdate = new AppSettings(activity).getLastUpdateDate();
        }

        @Override
        protected List<CurrencyData> doInBackground(Void... params) {
            Log.v(Defs.LOG_TAG, "Downloading rates from remote source...");

            List<CurrencyData> currencies = Lists.newArrayList();

            try (DataSource source = new SQLiteDataSource()) {
                source.connect(activity);

                // latest currency update regardless of the source
                // format, e.g., "2016-11-09T01:00:06+02:00"
                DateTime from = source.getLastRatesDownloadTime().or(lastUpdate);

                /**
                 * Manual updates only fetch the currencies since the last/latest currency
                 * update date time found in the database (regardless of the source). This might
                 * introduce a bug should users try to manually update on the next day after the
                 * last fetch, because the server fetches currencies from a given date at that
                 * specific day only. It does not fetch currencies for the next day of the given
                 * date. To counter this we force an update from the beginning of the day
                 * (00:00), if the last update was one day ago (we count from midnight on and
                 * not a 24h period).
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
            } catch (DataSourceException | IOException e) {
                Log.e(Defs.LOG_TAG, "Could not save currencies to database!", e);
                msgId = R.string.error_db_save;
            }

            return currencies;
        }

        @Override
        protected void onPostExecute(final List<CurrencyData> result) {
            if (!updateOK) {
                listener.onLoadFailed(msgId);
            } else {
                // OK
                lastUpdate = DateTime.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone(Defs.DATE_TIMEZONE_SOFIA)));
                Log.d(Defs.LOG_TAG, "Last rate download on " + lastUpdate.toString());

                listener.onLoadSuccessful(() -> Pair.create(lastUpdate, result));
            }
        }

    }
}