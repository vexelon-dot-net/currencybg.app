package net.vexelon.currencybg.app.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.db.models.CurrencyDataNew;
import net.vexelon.currencybg.app.db.models.CurrencyLocales;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by Tsvetoslav on 27.8.2016 г..
 */
public class APISource implements Source {

	OkHttpClient client = new OkHttpClient();
	Gson gson = new GsonBuilder().setDateFormat(Defs.DATEFORMAT_ISO_8601).create();
	Type type = new TypeToken<List<CurrencyDataNew>>() {}.getType();

	@Override
	public Map<CurrencyLocales, List<CurrencyData>> downloadRates(boolean getFixedRates) throws SourceException {

		return null;
	}

	@Override
	public List<CurrencyDataNew> getAllRatesByDate(String initialTime) throws SourceException {

		String address = Defs.SERVER_ADDRESS+initialTime;

		String rates;
		try {
			rates = downloadRates(address);
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}

		// TODO - to be set Authentication information
		List<CurrencyDataNew> currencies = gson.fromJson(rates, type);

		for (CurrencyDataNew currency : currencies) {
			System.out.println(currency.getCode());
		}

		return currencies;
	}

	@Override
	public List<CurrencyDataNew> getAllRatesByDateSource(String initialTime, Integer sourceId) throws SourceException {

		String address = Defs.SERVER_ADDRESS+initialTime+"/"+sourceId;

		String rates;
		try {
			rates = downloadRates(address);
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}

		// TODO - to be set Authentication information
		List<CurrencyDataNew> currencies = gson.fromJson(rates, type);

		for (CurrencyDataNew currency : currencies) {
			System.out.println(currency.getCode());
		}

		return currencies;
	}

	@Override
	public List<CurrencyDataNew> getAllCurrentRatesAfter(String initialTime) throws SourceException {

		String address = Defs.SERVER_ADDRESS+"today/"+initialTime;

		String rates;
		try {
			rates = downloadRates(address);
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}

		// TODO - to be set Authentication information
		List<CurrencyDataNew> currencies = gson.fromJson(rates, type);

		for (CurrencyDataNew currency : currencies) {
			System.out.println(currency.getCode());
		}

		return currencies;
	}

	@Override
	public List<CurrencyDataNew> getAllCurrentRatesAfter(String initialTime, Integer sourceId) throws SourceException {

		String address = Defs.SERVER_ADDRESS+"today/"+initialTime+"/"+sourceId;

		String rates;
		try {
			rates = downloadRates(address);
		} catch (IOException io) {
			throw new SourceException(
					"Failed in method getAllRatesByDate(String initialTime) to loading currencies from OpenShift!", io);
		}

		// TODO - to be set Authentication information
		List<CurrencyDataNew> currencies = gson.fromJson(rates, type);

		for (CurrencyDataNew currency : currencies) {
			System.out.println(currency.getCode());
		}

		return currencies;
	}

	String downloadRates(String url) throws IOException {
		Request request = new Request.Builder()
				.url(url)
				.header("APIKey", "CurrencyBgUser")
				.build();

		Response response = client.newCall(request).execute();
		return response.body().string();
	}



}
