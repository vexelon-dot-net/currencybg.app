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

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.common.CurrencyListRow;
import net.vexelon.currencybg.app.common.CurrencyLocales;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.DataSource;
import net.vexelon.currencybg.app.db.DataSourceException;
import net.vexelon.currencybg.app.db.SQLiteDataSource;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.ui.UiCodes;
import net.vexelon.currencybg.app.ui.events.Notifications;
import net.vexelon.currencybg.app.ui.events.NotificationsListener;
import net.vexelon.currencybg.app.utils.IOUtils;
import net.vexelon.currencybg.app.utils.NumberUtils;
import net.vexelon.currencybg.app.utils.StringUtils;

import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AbstractFragment extends Fragment {

	protected View rootView;
	protected Menu mMenu;
	protected List<NotificationsListener> listeners = new ArrayList<NotificationsListener>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		mMenu = menu;
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onStart() {
		super.onStart();

		final Context context = getActivity();
		final AppSettings appSettings = new AppSettings(context);
		if (appSettings.getUserAppUses() > Defs.VOTING_THRESHOLD && !appSettings.isUserVoted()) {
			showVotingInvitation(context).show();
		} else {
			appSettings.setUserAppUses(appSettings.getUserAppUses() + 1);
		}
	}

	/**
	 * Shows a dialog with "What's New" information
	 *
	 * @param context
	 * @return
	 */
	public AlertDialog showNewsAlert(final Context context) {
		Resources resources = context.getResources();

		final TextView messagesView = new TextView(context);
		messagesView.setText(Html.fromHtml(resources.getString(R.string.news_messages)));
		messagesView.setMovementMethod(LinkMovementMethod.getInstance());
		// messagesView.setMovementMethod(ScrollingMovementMethod.getInstance());
		messagesView.setPadding(24, 24, 24, 12);

		ScrollView scrollView = new ScrollView(context);
		scrollView.setFillViewport(true);
		scrollView.addView(messagesView);

		return new AlertDialog.Builder(context).setTitle(R.string.news_title)
				.setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setCancelable(false).setView(scrollView).show();
	}

	public AlertDialog showVotingInvitation(final Context context) {
		Resources resources = context.getResources();

		final TextView messagesView = new TextView(context);
		messagesView.setText(Html
				.fromHtml(resources.getString(R.string.vote_invite_content, resources.getString(R.string.app_name))));
		messagesView.setMovementMethod(LinkMovementMethod.getInstance());
		messagesView.setPadding(48, 24, 48, 24);

		ScrollView scrollView = new ScrollView(context);
		scrollView.setFillViewport(true);
		scrollView.addView(messagesView);

		return new AlertDialog.Builder(context)
				.setTitle(getString(R.string.vote_invite_title, getString(R.string.app_name)))
				.setNegativeButton(R.string.text_not_now, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						new AppSettings(context).setUserAppUses(0);
					}
				}).setPositiveButton(R.string.text_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final String appPackageName = getActivity().getPackageName();
						try {
							startActivity(
									new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
						} catch (android.content.ActivityNotFoundException e) {
							startActivity(new Intent(Intent.ACTION_VIEW,
									Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
						}

						final AppSettings appSettings = new AppSettings(context);
						appSettings.setUserAppUses(0);
						appSettings.setUserVoted(true);

						dialog.dismiss();
					}
				}).setCancelable(false).setView(scrollView).show();
	}

	// public void addListener(NotificationsListener listner) {
	// listeners.add(listner);
	// }
	//
	// public void removeListener(NotificationsListener listener) {
	// listeners.remove(listener);
	// }

	public void notifyListeners(Notifications notification) {
		for (NotificationsListener listener : listeners) {
			listener.onNotification(notification);
		}
	}

	protected void setRefreshActionButtonState(final boolean isRefreshing) {
		if (mMenu != null) {
			MenuItem menuItem = mMenu.findItem(R.id.action_refresh);
			if (menuItem != null) {
				if (isRefreshing) {
					menuItem.setActionView(isRefreshing ? R.layout.actionbar_indeterminate_progress : null);
				} else {
					menuItem.setActionView(null);
				}
			}
		}
	}

	protected CurrencyLocales getSelectedCurrenciesLocale(final Context context) {
		return new AppSettings(context).getCurrenciesLanguage();
	}

	/**
	 * 
	 * @param currencies
	 * @return
	 */
	protected static List<CurrencyData> getSortedCurrencies(List<CurrencyData> currencies) {
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
	 * Generates a fictional BGN currency with 1:1:1 rate
	 *
	 * @return CurrencyData
	 */
	private static CurrencyData getBGNCurrency() {
		CurrencyData currency = new CurrencyData();
		currency.setCode("BGN");
		currency.setRatio(1);
		currency.setBuy("1");
		currency.setSell("1");
		currency.setSource(0);
		currency.setDate(LocalDate.now().toString());
		return currency;
	}

	/**
	 * Fetches a sorted list of last downloaded currencies from the database
	 *
	 * @param context
	 * @param sorted
	 * @param addBGN
	 *            If {@code true}, adds a fictional <i>BGN</i> currency to the
	 *            result list.
	 * @return
	 * @throws DataSourceException
	 */
	public static List<CurrencyData> getCurrencies(final Context context, boolean sorted, boolean addBGN)
			throws DataSourceException {
		List<CurrencyData> currencies = Lists.newArrayList();

		DataSource source = null;
		try {
			source = new SQLiteDataSource();
			source.connect(context);
			currencies = source.getLastRates();
		} finally {
			IOUtils.closeQuitely(source);
		}

		if (addBGN) {
			currencies.add(getBGNCurrency());
		}

		if (sorted) {
			currencies = getSortedCurrencies(currencies);
		}

		return currencies;
	}

	/**
	 * @see #getCurrencies(Context, boolean, boolean)
	 */
	public static List<CurrencyData> getCurrencies(final Context context, boolean sorted) throws DataSourceException {
		return getCurrencies(context, sorted, false);
	}

	/**
	 * Fetches a mapping of currency codes and currencies for every source found
	 * 
	 * @param currencies
	 * @return
	 */
	public static Multimap<String, CurrencyData> getCurrenciesMapped(List<CurrencyData> currencies) {
		ImmutableListMultimap.Builder<String, CurrencyData> builder = ImmutableListMultimap.builder();
		for (CurrencyData currencyData : currencies) {
			builder.put(currencyData.getCode(), currencyData);
		}
		return builder.build();
	}

	/**
	 * Fetches a table-alike mapping of currency codes and currencies for every
	 * source found
	 * 
	 * @param currencies
	 * @return
	 */
	public static Table<String, Sources, CurrencyData> getCurrenciesTable(List<CurrencyData> currencies) {
		ImmutableTable.Builder<String, Sources, CurrencyData> builder = ImmutableTable.builder();
		for (CurrencyData currencyData : currencies) {
			builder.put(currencyData.getCode(), Sources.valueOf(currencyData.getSource()), currencyData);
		}
		return builder.build();
	}

	/**
	 * Removes currencies that should not be shown to users
	 *
	 * @param currencies
	 * @return
	 */
	public static List<CurrencyData> getVisibleCurrencies(List<CurrencyData> currencies) {
		Iterator<CurrencyData> iterator = currencies.iterator();
		while (iterator.hasNext()) {
			CurrencyData c = iterator.next();
			if (Defs.HIDDEN_CURRENCY_CODES.contains(c.getCode())) {
				iterator.remove();
			}
		}

		return currencies;
	}

	/**
	 * Converts list of currencies from various sources to a table with rows &
	 * columns
	 *
	 * @param context
	 * @param currencies
	 * @return
	 */
	protected List<CurrencyListRow> toCurrencyRows(final Context context, List<CurrencyData> currencies) {
		Map<String, CurrencyListRow> map = Maps.newHashMap();

		for (CurrencyData c : currencies) {
			CurrencyListRow row = map.get(c.getCode());
			if (row == null) {
				row = new CurrencyListRow(c.getCode(), UiCodes.getCurrencyName(context.getResources(), c.getCode()));
				map.put(c.getCode(), row);
			}
			row.addColumn(Sources.valueOf(c.getSource()), c);
		}

		return Lists.newArrayList(map.values());
	}

	protected String formatCurrency(final Context context, String value, String code) {
		if (!StringUtils.isEmpty(value)) {
			int precisionMode = new AppSettings(context).getCurrenciesPrecision();

			try {
				return formatCurrency(new BigDecimal(value), code, precisionMode);
			} catch (Exception e) {
				Log.e(Defs.LOG_TAG, "Currency format exception! ", e);
			}
		}

		return value;
	}

	protected String formatCurrency(BigDecimal amount, String code, int precisionMode) {
		switch (precisionMode) {
		case AppSettings.PRECISION_ADVANCED:
			return NumberUtils.getCurrencyFormat(amount, Defs.SCALE_SHOW_LONG, code);

		case AppSettings.PRECISION_SIMPLE:
		default:
			return NumberUtils.getCurrencyFormat(amount, code);
		}
	}

	protected void showSnackbar(String text, int duration, boolean isError) {
		try {
			Snackbar snackbar = Snackbar.make(rootView, text, duration);
			View v = snackbar.getView();
			v.setBackgroundColor(
					ContextCompat.getColor(getActivity(), isError ? R.color.colorAccent : R.color.colorPrimary));
			snackbar.show();
		} catch (RuntimeException e) {
			Log.wtf(Defs.LOG_TAG, "Failed displaying Snackbar! Probably wrong fragment binding.", e);
		}
	}

	protected void showSnackbar(String text, boolean isError) {
		showSnackbar(text, Snackbar.LENGTH_SHORT, isError);
	}

	protected void showSnackbar(String text) {
		showSnackbar(text, Snackbar.LENGTH_SHORT, false);
	}

	protected void showSnackbar(View view, int resId, int duration, boolean isError) {
		try {
			Snackbar snackbar = Snackbar.make(view, resId, duration);
			View v = snackbar.getView();
			// TextView textView = (TextView)
			// v.findViewById(android.support.design.R.id.snackbar_text);
			// textView.setTextColor(getResources().getColor(R.color.colorAccent));
			v.setBackgroundColor(
					ContextCompat.getColor(getActivity(), isError ? R.color.colorAccent : R.color.colorPrimary));
			snackbar.show();
		} catch (RuntimeException e) {
			Log.wtf(Defs.LOG_TAG, "Failed displaying Snackbar! Probably wrong fragment binding.", e);
		}
	}

	protected void showSnackbar(int resId, int duration, boolean isError) {
		showSnackbar(rootView, resId, duration, isError);
	}

	protected void showSnackbar(int resId, boolean isError) {
		showSnackbar(rootView, resId, Snackbar.LENGTH_SHORT, isError);
	}

	protected void showSnackbar(int resId) {
		showSnackbar(rootView, resId, Snackbar.LENGTH_SHORT, false);
	}

	protected int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
	}

	protected void vibrate(int duration) {
		Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		if (v != null) {
			v.vibrate(duration);
		}
	}
}
