package net.vexelon.currencybg.app.ui;

import android.app.Activity;
import android.content.res.Resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.vexelon.currencybg.app.R;

import java.util.List;
import java.util.Map;

/**
 * Operations based on currency codes
 * 
 */
public final class UiCodes {

	private static Map<String, String> mapping;

	/**
	 * Fetch currency name by code
	 *
	 * @param activity
	 * @param code
	 * @return
	 */
	public static String getCurrencyName(Resources res, String code) {
		if (mapping == null) {
			String[] codes = res.getStringArray(R.array.currency_codes);
			String[] names = res.getStringArray(R.array.currency_names);

			mapping = Maps.newHashMap();
			for (int i = 0; i < codes.length; i++) {
				mapping.put(codes[i], names[i]);
			}
		}

		return mapping.get(code);
	}
}
