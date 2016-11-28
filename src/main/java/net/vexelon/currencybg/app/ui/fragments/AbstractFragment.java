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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.common.CurrencyListRow;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.common.CurrencyLocales;
import net.vexelon.currencybg.app.ui.UiCodes;
import net.vexelon.currencybg.app.ui.events.Notifications;
import net.vexelon.currencybg.app.ui.events.NotificationsListener;

import org.jsoup.helper.StringUtil;

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

	/**
	 * 
	 * @return MaterialDialog
	 */
	protected MaterialDialog newCalculatorMenu(final CalculatorListener listener) {
		final AppSettings appSettings = new AppSettings(getActivity());
		return new MaterialDialog.Builder(getActivity()).customView(R.layout.calculator_layout, false)
				.positiveText(R.string.text_ok).negativeText(R.string.text_cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						// TODO
						TextView display = (TextView) dialog.getCustomView().findViewById(R.id.calc_display);
						Log.d(Defs.LOG_TAG, "CALC: " + display.getText());

						// notify
						listener.onValue(new BigDecimal(display.getText().toString()));
					}
				}).build();
	}

	protected CurrencyLocales getSelectedCurrenciesLocale() {
		return new AppSettings(getActivity()).getCurrenciesLanguage();
	}

	protected int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
	}

	protected Map<String, CurrencyData> getCurreniesMap(List<CurrencyData> currenciesList) {
		Map<String, CurrencyData> currenciesMap = Maps.newHashMap();
		for (CurrencyData currencyData : currenciesList) {
			currenciesMap.put(currencyData.getCode(), currencyData);
		}
		return currenciesMap;
	}

	/**
	 * Removes currencies that should not be shown to users
	 * 
	 * @param currencies
	 * @return
	 */
	protected List<CurrencyData> getVisibleCurrencies(List<CurrencyData> currencies) {
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
	 * @param currencies
	 * @return
	 */
	protected List<CurrencyListRow> toCurrencyRows(List<CurrencyData> currencies) {
		Map<String, CurrencyListRow> map = Maps.newHashMap();
		final Activity activity = getActivity();

		for (CurrencyData c : currencies) {
			CurrencyListRow row = map.get(c.getCode());
			if (row == null) {
				row = new CurrencyListRow(c.getCode(), UiCodes.getCurrencyName(activity.getResources(), c.getCode()));
				map.put(c.getCode(), row);
			}
			row.addColumn(Sources.valueOf(c.getSource()), c);
		}

		return Lists.newArrayList(map.values());
	}

	protected void showSnackbar(String text, int duration, boolean isError) {
		Snackbar snackbar = Snackbar.make(rootView, text, duration);
		View v = snackbar.getView();
		v.setBackgroundColor(
				ContextCompat.getColor(getContext(), isError ? R.color.colorAccent : R.color.colorPrimary));
		snackbar.show();
	}

	protected void showSnackbar(String text, boolean isError) {
		showSnackbar(text, Snackbar.LENGTH_SHORT, isError);
	}

	protected void showSnackbar(String text) {
		showSnackbar(text, Snackbar.LENGTH_SHORT, false);
	}

	protected void showSnackbar(int resId, int duration, boolean isError) {
		Snackbar snackbar = Snackbar.make(rootView, resId, duration);
		View v = snackbar.getView();
		// TextView textView = (TextView)
		// v.findViewById(android.support.design.R.id.snackbar_text);
		// textView.setTextColor(getResources().getColor(R.color.colorAccent));
		v.setBackgroundColor(
				ContextCompat.getColor(getContext(), isError ? R.color.colorAccent : R.color.colorPrimary));
		snackbar.show();
	}

	protected void showSnackbar(int resId, boolean isError) {
		showSnackbar(resId, Snackbar.LENGTH_SHORT, isError);
	}

	protected void showSnackbar(int resId) {
		showSnackbar(resId, Snackbar.LENGTH_SHORT, false);
	}

	/**
	 * 
	 */
	public static interface CalculatorListener {

		void onValue(BigDecimal value);

	}
}
