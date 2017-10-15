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

import com.google.common.collect.Maps;

import net.vexelon.currencybg.app.R;

import java.util.Map;

public final class UIFlags {

	private static final Map<String, Integer> flagsMap = Maps.newHashMap();

	static {
		flagsMap.put("ae", R.drawable.ae);
		flagsMap.put("al", R.drawable.al);
		flagsMap.put("ar", R.drawable.ar);
		flagsMap.put("au", R.drawable.au);
		flagsMap.put("ba", R.drawable.ba);
		flagsMap.put("bg", R.drawable.bg);
		flagsMap.put("br", R.drawable.br);
		flagsMap.put("bt", R.drawable.bt);
		flagsMap.put("by", R.drawable.by);
		flagsMap.put("ca", R.drawable.ca);
		flagsMap.put("ch", R.drawable.ch);
		flagsMap.put("ca", R.drawable.ca);
		flagsMap.put("cn", R.drawable.cn);
		flagsMap.put("cz", R.drawable.cz);
		flagsMap.put("do", R.drawable.dop);
		flagsMap.put("dk", R.drawable.dk);
		flagsMap.put("eg", R.drawable.eg);
		flagsMap.put("ee", R.drawable.ee);
		flagsMap.put("ge", R.drawable.ge);
		flagsMap.put("gb", R.drawable.gb);
		flagsMap.put("hk", R.drawable.hk);
		flagsMap.put("hr", R.drawable.hr);
		flagsMap.put("hu", R.drawable.hu);
		flagsMap.put("id", R.drawable.id);
		flagsMap.put("in", R.drawable.in);
		flagsMap.put("is", R.drawable.is);
		flagsMap.put("jo", R.drawable.jo);
		flagsMap.put("jp", R.drawable.jp);
		flagsMap.put("ke", R.drawable.ke);
		flagsMap.put("kr", R.drawable.kr);
		flagsMap.put("lt", R.drawable.lt);
		flagsMap.put("lv", R.drawable.lv);
		flagsMap.put("md", R.drawable.md);
		flagsMap.put("mk", R.drawable.mk);
		flagsMap.put("mu", R.drawable.mu);
		flagsMap.put("mx", R.drawable.mx);
		flagsMap.put("my", R.drawable.my);
		flagsMap.put("no", R.drawable.no);
		flagsMap.put("nz", R.drawable.nz);
		flagsMap.put("ph", R.drawable.ph);
		flagsMap.put("pl", R.drawable.pl);
		flagsMap.put("qa", R.drawable.qa);
		flagsMap.put("ro", R.drawable.ro);
		flagsMap.put("rs", R.drawable.rs);
		flagsMap.put("ru", R.drawable.ru);
		flagsMap.put("sa", R.drawable.sa);
		flagsMap.put("se", R.drawable.se);
		flagsMap.put("sg", R.drawable.sg);
		flagsMap.put("th", R.drawable.th);
		flagsMap.put("tr", R.drawable.tr);
		flagsMap.put("tw", R.drawable.tw);
		flagsMap.put("ua", R.drawable.ua);
		flagsMap.put("us", R.drawable.us);
		flagsMap.put("vn", R.drawable.vn);
		flagsMap.put("xa", R.drawable.xa);
		flagsMap.put("za", R.drawable.za);
		flagsMap.put("eu", R.drawable.eu);
		flagsMap.put("il", R.drawable.ils);
		// crypto currencies
		flagsMap.put("bch", R.drawable.bch);
		flagsMap.put("eth", R.drawable.eth);
		flagsMap.put("das", R.drawable.das);
		flagsMap.put("dog", R.drawable.dog);
		flagsMap.put("ltc", R.drawable.ltc);
		flagsMap.put("rip", R.drawable.rip);
		flagsMap.put("zec", R.drawable.zec);
	}

	public static int getResourceFromCode(String code) {
		Integer resId = null;

		if (!code.isEmpty()) {
			code = code.substring(0, Math.min(code.length(), 3)).toLowerCase();
			resId = flagsMap.get(code);
			if (resId == null) {
				resId = flagsMap.get(code.substring(0, 2));
			}

		}

		return resId == null ? R.drawable.unknown : resId;
	}
}
