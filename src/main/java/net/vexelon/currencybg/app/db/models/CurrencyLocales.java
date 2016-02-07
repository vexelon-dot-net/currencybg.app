package net.vexelon.currencybg.app.db.models;

import java.util.Locale;

import android.content.Context;

public enum CurrencyLocales {
	BG, EN;

	public static CurrencyLocales getAppLocale(Context context) {
		Locale currentLocale = context.getResources().getConfiguration().locale;
		if (currentLocale.getLanguage().equalsIgnoreCase("bg")) {
			return BG;
		}
		// default
		return EN;
	}
}
