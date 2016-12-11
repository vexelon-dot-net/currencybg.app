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
import java.util.Collections;
import java.util.Comparator;
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
import android.content.Context;
import android.content.DialogInterface;
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
import net.vexelon.currencybg.app.db.DataSource;
import net.vexelon.currencybg.app.db.DataSourceException;
import net.vexelon.currencybg.app.db.SQLiteDataSource;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.common.CurrencyLocales;
import net.vexelon.currencybg.app.ui.UIUtils;
import net.vexelon.currencybg.app.ui.UiCodes;
import net.vexelon.currencybg.app.ui.components.CalculatorWidget;
import net.vexelon.currencybg.app.ui.events.Notifications;
import net.vexelon.currencybg.app.ui.events.NotificationsListener;
import net.vexelon.currencybg.app.utils.IOUtils;
import net.vexelon.currencybg.app.utils.NumberUtils;
import net.vexelon.currencybg.app.utils.StringUtils;

import org.joda.time.LocalDate;
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

	protected CurrencyLocales getSelectedCurrenciesLocale(final Context context) {
		return new AppSettings(context).getCurrenciesLanguage();
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
	 */
	protected List<CurrencyData> getCurrencies(final Context context, boolean sorted, boolean addBGN) {
		List<CurrencyData> currencies = Lists.newArrayList();

		DataSource source = null;
		try {
			source = new SQLiteDataSource();
			source.connect(context);
			currencies = source.getLastRates();
		} catch (DataSourceException e) {
			showSnackbar(R.string.error_db_load, Defs.TOAST_ERR_TIME, true);
			Log.e(Defs.LOG_TAG, "Could not load currencies from database!", e);
		} finally {
			IOUtils.closeQuitely(source);
		}

		if (addBGN) {
			currencies.add(getBGNCurrency());
		}

		if (sorted) {
			// sort by code
			Collections.sort(currencies, new Comparator<CurrencyData>() {
				@Override
				public int compare(CurrencyData lhs, CurrencyData rhs) {
					return lhs.getCode().compareToIgnoreCase(rhs.getCode());
				}
			});
		}

		return currencies;
	}

	/**
	 * @see #getCurrencies(Context, boolean, boolean)
	 */
	protected List<CurrencyData> getCurrencies(final Context context, boolean sorted) {
		return getCurrencies(context, sorted, false);
	}

	/**
	 * Generates a fictional BGN currency with 1:1:1 rate
	 *
	 * @return CurrencyData
	 */
	private CurrencyData getBGNCurrency() {
		CurrencyData currency = new CurrencyData();
		currency.setCode("BGN");
		currency.setRatio(1);
		currency.setBuy("1");
		currency.setSell("1");
		currency.setSource(0);
		currency.setDate(LocalDate.now().toString());
		return currency;
	}

	protected Map<String, CurrencyData> getMappedCurrencies(List<CurrencyData> currencies) {
		Map<String, CurrencyData> result = Maps.newHashMap();
		for (CurrencyData currencyData : currencies) {
			result.put(currencyData.getCode(), currencyData);
		}
		return result;
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
				switch (precisionMode) {
				case AppSettings.PRECISION_ADVANCED:
					return NumberUtils.getCurrencyFormat(new BigDecimal(value), Defs.SCALE_SHOW_LONG, code);
				case AppSettings.PRECISION_SIMPLE:
				default:
					return NumberUtils.getCurrencyFormat(new BigDecimal(value), code);
				}
			} catch (Exception e) {
				Log.e(Defs.LOG_TAG, "Currency format exception! ", e);
			}
		}

		return value;
	}

	protected void showSnackbar(String text, int duration, boolean isError) {
		try {
			Snackbar snackbar = Snackbar.make(rootView, text, duration);
			View v = snackbar.getView();
			v.setBackgroundColor(
					ContextCompat.getColor(getContext(), isError ? R.color.colorAccent : R.color.colorPrimary));
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

	protected void showSnackbar(int resId, int duration, boolean isError) {
		try {
			Snackbar snackbar = Snackbar.make(rootView, resId, duration);
			View v = snackbar.getView();
			// TextView textView = (TextView)
			// v.findViewById(android.support.design.R.id.snackbar_text);
			// textView.setTextColor(getResources().getColor(R.color.colorAccent));
			v.setBackgroundColor(
					ContextCompat.getColor(getContext(), isError ? R.color.colorAccent : R.color.colorPrimary));
			snackbar.show();
		} catch (RuntimeException e) {
			Log.wtf(Defs.LOG_TAG, "Failed displaying Snackbar! Probably wrong fragment binding.", e);
		}
	}

	protected void showSnackbar(int resId, boolean isError) {
		showSnackbar(resId, Snackbar.LENGTH_SHORT, isError);
	}

	protected void showSnackbar(int resId) {
		showSnackbar(resId, Snackbar.LENGTH_SHORT, false);
	}

	protected int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
	}

}
