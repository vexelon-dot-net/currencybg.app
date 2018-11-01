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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.DataSourceException;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.ui.components.CalculatorWidget;
import net.vexelon.currencybg.app.ui.components.ConvertSourceListAdapter;
import net.vexelon.currencybg.app.ui.components.ConvertTargetListAdapter;
import net.vexelon.currencybg.app.ui.components.CurrencySelectListAdapter;
import net.vexelon.currencybg.app.ui.events.LoadListener;
import net.vexelon.currencybg.app.utils.NumberUtils;
import net.vexelon.currencybg.app.utils.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ConvertFragment extends AbstractFragment implements LoadListener<List<CurrencyData>> {

	private Spinner spinnerSourceCurrency;
	private TextView sourceValueView;
	private ListView targetCurrenciesView;
	private FloatingActionButton actionButton;

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
		new UpdateRatesTask(getActivity(), this, true).execute();
		super.onResume();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			/*
			 * Back from Currencies fragment view, so we reload all currencies. The user
			 * might have updated them.
			 */
			new UpdateRatesTask(getActivity(), this, true).execute();
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
			appSettings.setConvertCurrencies(Sets.newHashSet());

			new UpdateRatesTask(getActivity(), this, false).execute();

			// bring back select button
			actionButton.show(true);

			return true;

		case R.id.action_share:
			newShareCurrenciesDialog();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void init(View view) {
		final AppSettings appSettings = new AppSettings(getActivity());

		// setup source currencies
		spinnerSourceCurrency = view.findViewById(R.id.source_currency);
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
		sourceValueView = view.findViewById(R.id.text_source_value2);
		sourceValueView.setOnClickListener((View v) -> {
			new CalculatorWidget(getActivity()).showCalculator(appSettings.getLastConvertValue(),
					(BigDecimal value) -> {
						setSourceCurrencyValue(value.toPlainString(), appSettings.getLastConvertCurrencySel());

						if (updateTargetCurrenciesCalculations()) {
							// save if value is valid
							appSettings.setLastConvertValue(value.toPlainString());
						}
					});
		});

		// setup target currencies list
		targetCurrenciesView = view.findViewById(R.id.list_target_currencies);
		targetCurrenciesView.setOnItemLongClickListener((AdapterView<?> parent, View v, int position, long id) -> {
			ConvertTargetListAdapter adapter = (ConvertTargetListAdapter) targetCurrenciesView.getAdapter();

			CurrencyData removed = adapter.remove(position);
			if (removed != null) {
				adapter.notifyDataSetChanged();

				appSettings.removeConvertCurrency(removed);
				showSnackbar(getActivity().getString(R.string.action_currency_removed, removed.getCode()));

				vibrate(Defs.VIBRATE_DEL_DURATION);
			}

			return false;
		});
		targetCurrenciesView.setOnItemClickListener((AdapterView<?> parent, View v, int position, long id) -> {
			showSnackbar(getActivity().getString(R.string.hint_currency_remove));
		});

		actionButton = view.findViewById(R.id.fab_convert);
		// actionButton.attachToListView(targetCurrenciesView);
		actionButton.setOnClickListener((View v) -> {
			newAddTargetCurrencyDialog().show();
		});
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
		final CurrencySelectListAdapter adapter = new CurrencySelectListAdapter(context,
				android.R.layout.simple_spinner_item, currencies);

		return new MaterialDialog.Builder(context).title(R.string.action_addcurrency).cancelable(true)
				.adapter(adapter, null).negativeText(R.string.text_cancel).positiveText(R.string.text_ok)
				.onPositive((@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) -> {

					if (!adapter.getSelected().isEmpty()) {
						AppSettings appSettings = new AppSettings(context);
						StringBuilder buffer = new StringBuilder();

						for (CurrencyData currency : adapter.getSelected()) {
							appSettings.addConvertCurrency(currency);
							buffer.append(currency.getCode()).append(", ");
						}

						buffer.setLength(buffer.length() - 2);
						String added = StringUtils.ellipsize(buffer.toString(), 30, 7);

						// // notify UI
						updateTargetCurrenciesListView();
						updateTargetCurrenciesCalculations();

						showSnackbar(context.getString(R.string.action_currency_added, added));
					}
				}).build();
	}

	/**
	 * Displays share currencies dialog
	 *
	 * @return
	 */
	private void newShareCurrenciesDialog() {
		final Context context = getActivity();

		ConvertTargetListAdapter adapter = (ConvertTargetListAdapter) targetCurrenciesView.getAdapter();
		if (adapter != null && !adapter.getTargets().isEmpty()) {
			StringBuilder buffer = new StringBuilder();

			buffer.append(getString(R.string.text_share_convert, sourceValueView.getText())).append(Defs.NEWLINE)
					.append(Defs.NEWLINE);

			buffer.append(getString(R.string.text_code)).append(Defs.TAB_2).append(getString(R.string.text_converted))
					.append(Defs.TAB_2).append(getString(R.string.text_source)).append(Defs.TAB_2).append(Defs.NEWLINE);

			for (Map.Entry<CurrencyData, String> next : adapter.getTargets().entrySet()) {
				buffer.append(next.getKey().getCode()).append(Defs.TAB_2);
				buffer.append(next.getValue()).append(Defs.TAB_2);
				buffer.append(Sources.getName(context, next.getKey().getSource())).append(Defs.NEWLINE);
			}

			// app url footer
			buffer.append(Defs.NEWLINE)
					.append(getString(R.string.action_share_footer, new AppSettings(context).getAppUrl()));

			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_TEXT, buffer.toString());
			sendIntent.setType("text/plain");
			startActivity(sendIntent);
		} else {
			showSnackbar(R.string.error_convert_empty, Defs.TOAST_INFO_DURATION, false);
		}
	}

	@Override
	public void onLoadStart() {
		// do nothing
	}

	@Override
	public void onLoadSuccessful(Supplier<List<CurrencyData>> newData) {
		final Activity activity = getActivity();
		if (activity != null) {

			if (newData.get() != null) {
				currencies = newData.get();
			}

			ConvertSourceListAdapter adapter = new ConvertSourceListAdapter(activity,
					android.R.layout.simple_spinner_item, AbstractFragment.getVisibleCurrencies(activity, currencies));
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
	}

	@Override
	public void onLoadFailed(int msgId) {
		showSnackbar(msgId, Defs.TOAST_ERR_DURATION, true);
	}

	static class UpdateRatesTask extends AsyncTask<Void, Void, Void> {

		private Activity activity;
		private boolean reload;
		private LoadListener<List<CurrencyData>> listener;

		private List<CurrencyData> currencies;
		private int msgId = -1;;

		public UpdateRatesTask(Activity activity, @NonNull LoadListener<List<CurrencyData>> listener, boolean reload) {
			this.activity = activity;
			this.reload = reload;
			this.listener = listener;
		}

		@Override
		protected Void doInBackground(Void... params) {
			Log.v(Defs.LOG_TAG, "Reloading rates ...");

			if (reload) {
				try {
					currencies = AbstractFragment.getCurrencies(activity, true, true);
				} catch (DataSourceException e) {
					msgId = R.string.error_db_load;
					Log.e(Defs.LOG_TAG, "Could not load currencies from database!", e);
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			if (msgId != -1) {
				listener.onLoadFailed(msgId);
			} else {
				listener.onLoadSuccessful(() -> currencies);
			}
		}

	}

}
