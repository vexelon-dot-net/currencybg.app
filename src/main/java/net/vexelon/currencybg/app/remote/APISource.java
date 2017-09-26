package net.vexelon.currencybg.app.remote;

import android.content.Context;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.vexelon.currencybg.app.AppAssets;
import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.utils.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Tsvetoslav on 27.8.2016 г..
 */
public class APISource implements Source {
	private static final String API_JUNCTION = "/api/currencies/";
	private static final String HEADER = "APIKey";

	private final AppAssets appAssets;
	private final Gson gson = new GsonBuilder().setDateFormat(Defs.DATEFORMAT_ISO8601).create();
	private final Type type = new TypeToken<List<CurrencyData>>() {
	}.getType();
	private final OkHttpClient client = new OkHttpClient();

	public APISource(Context context) {
		appAssets = new AppAssets(context);
	}

	private String getServerUrl() throws IOException {
		if ("prod".equals(appAssets.getDeployType())) {
			return appAssets.getProdServer() + API_JUNCTION;
		}

		return appAssets.getTestServer() + API_JUNCTION;
	}

	private String getToken() throws IOException {
		if ("prod".equals(appAssets.getDeployType())) {
			return appAssets.getProdServerApiKey();
		}

		return appAssets.getTestServerApiKey();
	}

	/**
	 * Debug method
	 * 
	 * @param currencies
	 */
	private List<CurrencyData> show(final List<CurrencyData> currencies) {
		for (CurrencyData currency : currencies) {
			System.out.println(currency.getCode() + " - " + Sources.valueOf(currency.getSource()));
		}
		return currencies;
	}

	private List<CurrencyData> toList(String json) throws SourceException {
		if (!StringUtils.isEmpty(json)) {
			return gson.fromJson(json, type);
		}

		// can't parse json, return an empty list to prevent NPEs
		return Lists.newArrayList();
	}

	@Override
	public List<CurrencyData> getAllRatesByDate(String initialTime) throws SourceException {
		// TODO - to be set Authentication information
		try {
			String address = getServerUrl() + initialTime;
			return toList(downloadRates(address));
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}
	}

	@Override
	public List<CurrencyData> getAllRatesByDateSource(String initialTime, Integer sourceId) throws SourceException {
		// TODO - to be set Authentication information
		try {
			String address = getServerUrl() + initialTime + "/" + sourceId;
			return toList(downloadRates(address));
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}
	}

	@Override
	public List<CurrencyData> getAllCurrentRatesAfter(String initialTime) throws SourceException {
		// TODO - to be set Authentication information
		try {
			String address = getServerUrl() + "today/" + initialTime;
			return toList(downloadRates(address));
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}
	}

	@Override
	public List<CurrencyData> getAllCurrentRatesAfter(String initialTime, Integer sourceId) throws SourceException {
		// TODO - to be set Authentication information
		try {
			String address = getServerUrl() + "today/" + initialTime + "/" + sourceId;
			return toList(downloadRates(address));
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}
	}

	public String downloadRates(String url) throws IOException, SourceException {
		try {
			Request request = new Request.Builder().url(url).header(HEADER, getToken()).build();
			Response response = client.newCall(request).execute();
			if (!response.isSuccessful()) {
				throw new SourceException(response.code());
			}

			return response.body().string();
		} catch (RuntimeException e) {
			throw new IOException("HTTP error!", e);
		}
	}

}
