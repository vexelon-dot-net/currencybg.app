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
package net.vexelon.currencybg.app.ui;

import java.util.Map;

import com.google.common.collect.Maps;

import net.vexelon.currencybg.app.R;

public class UIFlags {

	private static final Map<String, Integer> flagsMap = Maps.newHashMap();

	static {
		flagsMap.put("au", R.drawable.au);
		flagsMap.put("bg", R.drawable.bg);
		flagsMap.put("br", R.drawable.br);
		flagsMap.put("ca", R.drawable.ca);
		flagsMap.put("ch", R.drawable.ch);
		flagsMap.put("ca", R.drawable.ca);
		flagsMap.put("cn", R.drawable.cn);
		flagsMap.put("cz", R.drawable.cz);
		flagsMap.put("dk", R.drawable.dk);
		flagsMap.put("ee", R.drawable.ee);
		flagsMap.put("gb", R.drawable.gb);
		flagsMap.put("hk", R.drawable.hk);
		flagsMap.put("hr", R.drawable.hr);
		flagsMap.put("hu", R.drawable.hu);
		flagsMap.put("id", R.drawable.id);
		flagsMap.put("in", R.drawable.in);
		flagsMap.put("is", R.drawable.is);
		flagsMap.put("jp", R.drawable.jp);
		flagsMap.put("kr", R.drawable.kr);
		flagsMap.put("lt", R.drawable.lt);
		flagsMap.put("lv", R.drawable.lv);
		flagsMap.put("mx", R.drawable.mx);
		flagsMap.put("my", R.drawable.my);
		flagsMap.put("no", R.drawable.no);
		flagsMap.put("nz", R.drawable.nz);
		flagsMap.put("ph", R.drawable.ph);
		flagsMap.put("pl", R.drawable.pl);
		flagsMap.put("ro", R.drawable.ro);
		flagsMap.put("ru", R.drawable.ru);
		flagsMap.put("se", R.drawable.se);
		flagsMap.put("sg", R.drawable.sg);
		flagsMap.put("th", R.drawable.th);
		flagsMap.put("tr", R.drawable.tr);
		flagsMap.put("us", R.drawable.us);
		flagsMap.put("xa", R.drawable.xa);
		flagsMap.put("za", R.drawable.za);
		flagsMap.put("eu", R.drawable.eu);
	}

	public static int getResourceFromCode(String code) {
		Integer resId = R.drawable.unknown;
		if (!code.isEmpty()) {
			String lower = code.substring(0, 2).toLowerCase();
			if (flagsMap.containsKey(lower)) {
				resId = flagsMap.get(lower);
			}
		}
		return resId;
	}
}
