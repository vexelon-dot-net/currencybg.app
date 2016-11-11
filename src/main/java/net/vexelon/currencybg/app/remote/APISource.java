package net.vexelon.currencybg.app.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.common.Sources;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.common.CurrencyLocales;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by Tsvetoslav on 27.8.2016 г..
 */
public class APISource implements Source {

	private static final String SERVER_ADDRESS = "http://currencybg-tsvetoslav.rhcloud.com/currencybg.server/api/currencies/";
	private static final String HEADER = "APIKey";
	private static final String TOKEN = "CurrencyBgUser";

	private OkHttpClient client = new OkHttpClient();
	private Gson gson = new GsonBuilder().setDateFormat(Defs.DATEFORMAT_ISO_8601).create();
	private Type type = new TypeToken<List<CurrencyData>>() {
	}.getType();

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

	@Override
	public List<CurrencyData> getAllRatesByDate(String initialTime) throws SourceException {

		String address = SERVER_ADDRESS + initialTime;

		String rates;
		try {
			rates = downloadRates(address);
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}

		// TODO - to be set Authentication information
		List<CurrencyData> currencies = gson.fromJson(rates, type);
		return currencies;
	}

	@Override
	public List<CurrencyData> getAllRatesByDateSource(String initialTime, Integer sourceId) throws SourceException {

		String address = SERVER_ADDRESS + initialTime + "/" + sourceId;

		String rates;
		try {
			rates = downloadRates(address);
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}

		// TODO - to be set Authentication information

		return gson.fromJson(rates, type);
	}

	@Override
	public List<CurrencyData> getAllCurrentRatesAfter(String initialTime) throws SourceException {

		String address = SERVER_ADDRESS + "today/" + initialTime;

		String rates;
		try {
			rates = downloadRates(address);
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}

		// TODO - to be set Authentication information

		return gson.fromJson(rates, type);
	}

	@Override
	public List<CurrencyData> getAllCurrentRatesAfter(String initialTime, Integer sourceId) throws SourceException {

		String address = SERVER_ADDRESS + "today/" + initialTime + "/" + sourceId;

		String rates;
		try {
			rates = downloadRates(address);
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}

		// TODO - to be set Authentication information

		return gson.fromJson(rates, type);
	}

	String downloadRates(String url) throws IOException {
		try {
			Request request = new Request.Builder().url(url).header(HEADER, TOKEN).build();
			Response response = client.newCall(request).execute();
			return response.body().string();
		} catch (RuntimeException e) {
			throw new IOException("API error!", e);
		}
	}

}
