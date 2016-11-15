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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import com.google.common.collect.Lists;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.vexelon.currencybg.app.AppSettings;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.R;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.ui.UIFlags;
import net.vexelon.currencybg.app.ui.UIUtils;
import net.vexelon.currencybg.app.ui.UiCodes;
import net.vexelon.currencybg.app.utils.NumberUtils;

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
		ImageView icon = (ImageView) v.findViewById(R.id.icon);
		int imageId = UIFlags.getResourceFromCode(row.getCode());
		if (imageId != -1) {
			icon.setImageResource(imageId);
		}

		UIUtils.setText(v, R.id.name, UiCodes.getCurrencyName(getContext().getResources(), row.getCode()));
		UIUtils.setText(v, R.id.code, row.getCode());

		BigDecimal value = values.get(position);
		if (value == null) {
			value = BigDecimal.ZERO;
		}

		switch (precisionMode) {
		case AppSettings.PRECISION_ADVANCED:
			String rate = NumberUtils.scaleCurrency(value, Defs.SCALE_SHOW_LONG);
			UIUtils.setText(v, R.id.rate, rate);
			break;
		case AppSettings.PRECISION_SIMPLE:
		default:
			UIUtils.setText(v, R.id.rate, NumberUtils.scaleCurrency(value, row.getCode()));
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

	public void updateValues(CurrencyData sourceCurrency, BigDecimal value) {
		MathContext mathContext = new MathContext(Defs.SCALE_CALCULATIONS, RoundingMode.HALF_EVEN);
		// convert source currency to BGN value
		BigDecimal valueBGN;
		try {
			// BigDecimal rate = new BigDecimal(sourceCurrency.getRate(),
			// mathContext);
			BigDecimal rate = new BigDecimal(sourceCurrency.getBuy(), mathContext);
			BigDecimal ratio = new BigDecimal(sourceCurrency.getRatio(), mathContext);
			valueBGN = value.multiply(rate.divide(ratio, mathContext), mathContext);
		} catch (Exception e) {
			Log.e(Defs.LOG_TAG, "Failed to convert source currency to BGN!", e);
			return;
		}
		// convert each destination currency from BGN
		for (int i = 0; i < items.size(); i++) {
			CurrencyData currency = items.get(i);
			BigDecimal result = BigDecimal.ZERO;

			try {
				BigDecimal reverseRate;
				if ("0".equals(currency.getBuy())) {
					BigDecimal ratio0 = new BigDecimal(currency.getRatio());
					reverseRate = ratio0.divide(new BigDecimal(currency.getSell(), mathContext), mathContext);
				} else {
					reverseRate = new BigDecimal(currency.getBuy(), mathContext);
				}
				// if ("0".equals(currency.getReverseRate())) {
				// BigDecimal ratio0 = new BigDecimal(currency.getRatio());
				// reverseRate = ratio0.divide(new
				// BigDecimal(currency.getRate(), mathContext), mathContext);
				// } else {
				// reverseRate = new BigDecimal(currency.getReverseRate(),
				// mathContext);
				// }
				BigDecimal ratio = new BigDecimal(currency.getRatio(), mathContext);
				result = valueBGN.multiply(reverseRate, mathContext);
				// result = reverseRate.multiply(ratio,
				// mathContext).multiply(valueBGN, mathContext);
			} catch (Exception e) {
				Log.e(Defs.LOG_TAG, "Failed to calculate currency " + currency.getCode() + "!", e);
			}

			values.set(i, result);
		}
	}

}
