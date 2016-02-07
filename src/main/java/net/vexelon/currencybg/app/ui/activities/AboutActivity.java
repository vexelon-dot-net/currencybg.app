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
package net.vexelon.currencybg.app.ui.activities;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import net.vexelon.currencybg.app.R;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_about);
		init(findViewById(android.R.id.content));
	}

	private void init(View view) {
		ImageView icLogo = (ImageView) view.findViewById(R.id.about_logo);
		icLogo.setImageResource(R.drawable.about_icon);

		PackageInfo pinfo = null;
		try {
			pinfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_GIDS);
		} catch (Exception e) {
			// Log.e(TAG, e.getMessage());
		}

		StringBuilder sb = new StringBuilder();
		sb.append(getResString(R.string.app_name));
		sb.append("\n");
		sb.append(getResString(R.string.about_tagline));
		sb.append("\n");
		if (pinfo != null) {
			sb.append(getResString(R.string.about_version));
			sb.append(pinfo.versionName);
			sb.append("\n");
		}
		setText(R.id.about_row1, sb.toString());
		Linkify.addLinks((TextView) view.findViewById(R.id.about_row1), Linkify.ALL);

		sb = new StringBuilder();
		sb.append(getResString(R.string.about_author));
		sb.append("\n");
		sb.append("https://github.com/vexelon-dot-net/currencybg.app");
		sb.append("\n");
		sb.append(getResString(R.string.about_logo_author));
		sb.append("\n");
		sb.append("http://stremena.com");
		sb.append("\n");
		sb.append("\n");
		sb.append(getResString(R.string.about_bnb_info));
		sb.append("\n");
		sb.append("http://www.bnb.bg");
		sb.append("\n");
		sb.append("\n");
		sb.append(getResString(R.string.about_bnb_info_2));
		sb.append("\n");
		sb.append("\n");
		sb.append(getResString(R.string.about_flag_icons_info));
		sb.append("\n");
		sb.append("Copyright (c) 2013 Aha-Soft. http://www.aha-soft.com/free-icons/free-yellow-button-icons/");
		sb.append("\n");
		sb.append("Copyright (CC BY-ND 3.0) Visual Pharm. http://icons8.com");
		sb.append("\n");
		setText(R.id.about_row2, sb.toString());
		Linkify.addLinks((TextView) view.findViewById(R.id.about_row2), Linkify.ALL);
	}

	void setText(int id, String text) {
		TextView tx = (TextView) findViewById(id);
		if (tx != null) {
			tx.setText(text);
		}
	}

	String getResString(int resId) {
		return getString(resId);
	}

}
