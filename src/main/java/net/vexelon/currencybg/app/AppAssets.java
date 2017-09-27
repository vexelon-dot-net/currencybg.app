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
package net.vexelon.currencybg.app;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.util.Properties;

/**
 * Loads and provides access to assets found in <i>src/main/assets</i>.
 */
public final class AppAssets {

	private Context context;
	private Properties apiProps;

	public AppAssets(Context context) {
		this.context = context;
	}

	private Properties getProps() throws IOException {
		if (apiProps == null) {
			AssetManager assetManager = context.getAssets();
			apiProps = new Properties();
			apiProps.load(assetManager.open("api.properties"));
		}

		return apiProps;
	}

	public String getDeployType() throws IOException {
		return getProps().getProperty("deploy.type");
	}

	public String getTestServer() throws IOException {
		return getProps().getProperty("test.server");
	}

	public String getTestServerApiKey() throws IOException {
		return getProps().getProperty("test.key");
	}

	public String getProdServer() throws IOException {
		return getProps().getProperty("prod.server");
	}

	public String getProdServerApiKey() throws IOException {
		return getProps().getProperty("prod.key");
	}

}
