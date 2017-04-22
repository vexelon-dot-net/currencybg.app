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
package net.vexelon.currencybg.app.ui.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.common.collect.Lists;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.ui.UIUtils;
import net.vexelon.currencybg.app.ui.UiCodes;
import net.vexelon.currencybg.app.utils.NumberUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class ConvertTargetListAdapter extends ArrayAdapter<CurrencyData> {

	private List<CurrencyData> items;
	private List<BigDecimal> values;
	// private boolean showValues = false;
	private int precisionMode = AppSettings.PRECISION_SIMPLE;

	public ConvertTargetListAdapter(Context context, int textViewResId, List<CurrencyData> items, int precisionMode) {
		super(context, textViewResId, items);
		this.items = items;
		this.values = Lists.newArrayList();
		for (int i = 0; i < items.size(); i++) {
			values.add(BigDecimal.ZERO);
		}
		this.precisionMode = precisionMode;
	}

	private View _getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(R.layout.convert_target_row_layout, parent, false);
		}

		CurrencyData row = items.get(position);

		UIUtils.setFlagIcon(v, R.id.target_icon, row.getCode());
		UIUtils.setText(v, R.id.target_name, UiCodes.getCurrencyName(getContext().getResources(), row.getCode()));
		UIUtils.setText(v, R.id.target_code, row.getCode());
		UIUtils.setText(v, R.id.target_source, Sources.getName(row.getSource(), getContext()));

		BigDecimal value = values.get(position);
		if (value == null) {
			value = BigDecimal.ZERO;
		}

		switch (precisionMode) {
		case AppSettings.PRECISION_ADVANCED:
			UIUtils.setText(v, R.id.target_rate,
					NumberUtils.getCurrencyFormat(value, Defs.SCALE_SHOW_LONG, row.getCode()));
			break;
		case AppSettings.PRECISION_SIMPLE:
		default:
			UIUtils.setText(v, R.id.target_rate, NumberUtils.getCurrencyFormat(value, row.getCode()));
			break;
		}

		return v;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return _getView(position, convertView, parent);
	}

	@Override
	public void remove(CurrencyData object) {
		items.remove(object);
	}

	public CurrencyData remove(int position) {
		return items.remove(position);
	}

	/**
	 * Calculates exchange values from {@code source} currency to all selected
	 * target currencies.
	 * 
	 * @param source
	 * @param amount
	 */
	public void updateConvert(CurrencyData source, BigDecimal amount) {
		MathContext mathContext = new MathContext(Defs.SCALE_CALCULATIONS, RoundingMode.HALF_EVEN);

		/*
		 * Converts source currency to BGN value (Buy Lev)
		 */
		BigDecimal amountOfBGN = NumberUtils.buyCurrency(amount, source.getBuy(), source.getRatio());

		/*
		 * Convert each destination currency from BGN (Sell Lev for target
		 * currency)
		 */
		for (int i = 0; i < items.size(); i++) {
			CurrencyData targetCurrency = items.get(i);

			BigDecimal result = NumberUtils.sellCurrency(amountOfBGN, targetCurrency.getSell(),
					targetCurrency.getRatio());

//			BigDecimal result = BigDecimal.ZERO;
//
//			try {
//				BigDecimal sell = BigDecimal.ZERO;
//				if (!targetCurrency.getSell().isEmpty()) {
//					sell = new BigDecimal(targetCurrency.getSell(), mathContext);
//				}
//
//				BigDecimal ratio = new BigDecimal(targetCurrency.getRatio(), mathContext);
//				result = amountOfBGN.divide(sell, mathContext).multiply(ratio, mathContext);
//
//			} catch (Exception e) {
//				Log.e(Defs.LOG_TAG, "Failed to convert currency " + targetCurrency.getCode() + "!", e);
//			}

			values.set(i, result);
		}
	}

}
