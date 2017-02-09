package net.vexelon.currencybg.app.remote;

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
	private static final int HTTP_CODE_MAINTENANCE = 503;

	private static final String API_JUNCTION = "/currencybg.server/api/currencies/";
	private static final String HEADER = "APIKey";

	private final String serverUrl;
	private final String token;
	private final Gson gson = new GsonBuilder().setDateFormat(Defs.DATEFORMAT_ISO8601).create();
	private final Type type = new TypeToken<List<CurrencyData>>() {
	}.getType();
	private final OkHttpClient client = new OkHttpClient();

	public APISource() {
		if ("prod".equals(AppAssets.getDeployType())) {
			// production server address
			this.serverUrl = AppAssets.getProdServer() + API_JUNCTION;
			this.token = AppAssets.getProdServerApiKey();
		} else {
			// test server address
			this.serverUrl = AppAssets.getTestServer() + API_JUNCTION;
			this.token = AppAssets.getTestServerApiKey();
		}
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

		String address = serverUrl + initialTime;

		// TODO - to be set Authentication information

		try {
			return toList(downloadRates(address));
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}
	}

	@Override
	public List<CurrencyData> getAllRatesByDateSource(String initialTime, Integer sourceId) throws SourceException {

		String address = serverUrl + initialTime + "/" + sourceId;

		// TODO - to be set Authentication information

		try {
			return toList(downloadRates(address));
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}
	}

	@Override
	public List<CurrencyData> getAllCurrentRatesAfter(String initialTime) throws SourceException {

		String address = serverUrl + "today/" + initialTime;

		// TODO - to be set Authentication information

		try {
			return toList(downloadRates(address));
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}
	}

	@Override
	public List<CurrencyData> getAllCurrentRatesAfter(String initialTime, Integer sourceId) throws SourceException {

		String address = serverUrl + "today/" + initialTime + "/" + sourceId;

		// TODO - to be set Authentication information

		try {
			return toList(downloadRates(address));
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}
	}

	public String downloadRates(String url) throws IOException, SourceException {
		try {
			Request request = new Request.Builder().url(url).header(HEADER, token).build();
			Response response = client.newCall(request).execute();
			if (response.code() == HTTP_CODE_MAINTENANCE) {
				throw new SourceException(true);
			}

			return response.body().string();
		} catch (RuntimeException e) {
			throw new IOException("HTTP error!", e);
		}
	}

}
