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
package net.vexelon.currencybg.app.remote;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

import net.vexelon.currencybg.app.db.models.CurrencyData;
import net.vexelon.currencybg.app.db.models.CurrencyDataNew;
import net.vexelon.currencybg.app.db.models.CurrencyLocales;
import net.vexelon.currencybg.app.utils.DateTimeUtils;
import net.vexelon.currencybg.app.utils.IOUtils;

public class BNBSource implements Source {

	// Addresses on BNB for get on XML file
	public final static String URL_BNB_FORMAT_BG = "http://www.bnb.bg/Statistics/StExternalSector/StExchangeRates/StERForeignCurrencies/?download=xml&lang=BG";
	public final static String URL_BNB_FORMAT_EN = "http://www.bnb.bg/Statistics/StExternalSector/StExchangeRates/StERForeignCurrencies/?download=xml&lang=EN";
	public final static String URL_BNB_INDEX = "http://www.bnb.bg/index.htm";
	public final static String URL_BNB_FIXED_RATES = "http://www.bnb.bg/Statistics/StExternalSector/StExchangeRates/StERFixed/index.htm?toLang=_";

	public final static String XML_TAG_ROWSET = "ROWSET";
	public final static String XML_TAG_ROW = "ROW";
	public final static String XML_TAG_GOLD = "GOLD";
	public final static String XML_TAG_NAME = "NAME_";
	public final static String XML_TAG_CODE = "CODE";
	public final static String XML_TAG_RATIO = "RATIO";
	public final static String XML_TAG_REVERSERATE = "REVERSERATE";
	public final static String XML_TAG_RATE = "RATE";
	public final static String XML_TAG_EXTRAINFO = "EXTRAINFO";
	public final static String XML_TAG_CURR_DATE = "CURR_DATE";
	public final static String XML_TAG_TITLE = "TITLE";
	public final static String XML_TAG_F_STAR = "F_STAR";

	private CurrencyData currencyData;
	private String text;

	public BNBSource() {
	}

	public List<CurrencyData> getRatesFromUrl(String ratesUrl) throws SourceException {
		List<CurrencyData> listCurrencyData = Lists.newArrayList();
		InputStream is = null;
		XmlPullParserFactory factory = null;
		XmlPullParser parser = null;
		URL url = null;
		int header = 0;
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		Date currencyDate = new Date();
		try {
			url = new URL(ratesUrl);
			URLConnection connection = url.openConnection();
			connection.setDoInput(true);
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				// read error and throw it to caller
				is = httpConn.getErrorStream();
				throw new SourceException(new String(ByteStreams.toByteArray(is), Charsets.UTF_8.name()));
			}
			is = httpConn.getInputStream();
			// getEuroValue();

			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			parser = factory.newPullParser();
			parser.setInput(is, Charsets.UTF_8.name());

			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {

				String tagname = parser.getName();
				switch (eventType) {
				case XmlPullParser.START_TAG:
					if (tagname.equalsIgnoreCase(XML_TAG_ROW)) {
						if (header == 0) {
							header = 1;
						} else {
							header = 2;
						}
						// create a new instance of CurrencyData
						if (header > 1) {
							currencyData = new CurrencyData();
							// defaults
							currencyData.setRate("0");
							currencyData.setReverseRate("0");
						}
					}
					break;

				case XmlPullParser.TEXT:
					if (header > 1) {
						text = parser.getText();
					}
					break;

				case XmlPullParser.END_TAG:
					if (header > 1) {
						if (tagname.equalsIgnoreCase(XML_TAG_ROW)) {
							// add employee object to list
							listCurrencyData.add(currencyData);
						} else if (tagname.equalsIgnoreCase(XML_TAG_GOLD)) {
							currencyData.setGold(Integer.parseInt(text));
						} else if (tagname.equalsIgnoreCase(XML_TAG_NAME)) {
							currencyData.setName(text);
							eventType = parser.next();// ?
						} else if (tagname.equalsIgnoreCase(XML_TAG_CODE)) {
							currencyData.setCode(text);
						} else if (tagname.equalsIgnoreCase(XML_TAG_RATIO)) {
							currencyData.setRatio(Integer.parseInt(text));
						} else if (tagname.equalsIgnoreCase(XML_TAG_REVERSERATE)) {
							currencyData.setReverseRate(text);
						} else if (tagname.equalsIgnoreCase(XML_TAG_RATE)) {
							currencyData.setRate(text);
						} else if (tagname.equalsIgnoreCase(XML_TAG_EXTRAINFO)) {
							currencyData.setExtraInfo(text);
						} else if (tagname.equalsIgnoreCase(XML_TAG_CURR_DATE)) {
							try {
								currencyDate = dateFormat.parse(text);
							} catch (ParseException e1) {
								e1.printStackTrace();
								// use default (today)
								currencyDate = new Date();
							}
							currencyData.setCurrDate(currencyDate);
						} else if (tagname.equalsIgnoreCase(XML_TAG_TITLE)) {
							currencyData.setTitle(text);
						} else if (tagname.equalsIgnoreCase(XML_TAG_F_STAR)) {
							currencyData.setfStar(Integer.parseInt(text));
						}
					}
					break;

				default:
					break;
				}
				eventType = parser.next();
			}

			// setFixedCurrency();

			// listCurrencyData.add(setEuroCurrency(localeName, currencyDate));

			return listCurrencyData;
		} catch (Exception e) {
			throw new SourceException("Failed loading currencies from BNB source!", e);
		} finally {
			IOUtils.closeQuitely(is);
		}
	}

	private List<CurrencyData> getFixedRates(String language) throws SourceException {
		List<CurrencyData> listFixedCurrencyData = Lists.newArrayList();
		CurrencyData fixedCurrencyData = new CurrencyData();

		Date currentYear = DateTimeUtils.getCurrentYear();
		String fixedRatesUrl = URL_BNB_FIXED_RATES + language;
		InputStream is = null;
		try {
			URL url = new URL(fixedRatesUrl);
			URLConnection connection = url.openConnection();
			connection.setDoInput(true);
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			httpConn.setRequestProperty("Cookie", "userLanguage=" + language);
			if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				// read error and throw it to caller
				is = httpConn.getErrorStream();
				throw new SourceException(new String(ByteStreams.toByteArray(is), Charsets.UTF_8.name()));
			}
			is = httpConn.getInputStream();
			Document doc = Jsoup.parse(is, Charsets.UTF_8.name(), fixedRatesUrl);

			// Element element =
			// doc.select("div#more_information > div.box > div.top > div > ul > li").first();
			Element div = doc.select("div#content_box.content > div.doc_entry > center > table > tbody").first();
			Elements divChildren = div.children();

			int lineNumber = 1;
			for (Element table : divChildren) {
				if (lineNumber > 1) {
					// System.out.println(table.tagName());
					Elements tableChildren = table.children();
					int elementNumber = 1;
					fixedCurrencyData.setGold(1);
					fixedCurrencyData.setfStar(0);
					fixedCurrencyData.setCurrDate(currentYear);
					fixedCurrencyData.setIsFixed(true);
					for (Element elem : tableChildren) {
						// System.out.println(elem.tagName());
						Element elemChild = elem.children().first();
						// System.out.print(elemChild.text());//
						// elemChild.text()
						switch (elementNumber) {
						case 1:
							fixedCurrencyData.setName(elemChild.text());
							break;
						case 2:
							fixedCurrencyData.setCode(elemChild.text());
							break;
						case 3:
							fixedCurrencyData.setRatio(Integer.parseInt(elemChild.text()));
							break;
						case 4:
							fixedCurrencyData.setRate(elemChild.text());
							break;
						case 5:
							fixedCurrencyData.setReverseRate(elemChild.text());
							break;
						}
						elementNumber++;
					}
					listFixedCurrencyData.add(fixedCurrencyData);
					fixedCurrencyData = new CurrencyData();
				}
				lineNumber++;
			}
			// Element euroValue = element.getElementsByTag("strong").first();
			// String euroValuReturn = euroValue.text();
			return listFixedCurrencyData;
		} catch (Exception e) {
			throw new SourceException("Failed loading currencies from BNB source!", e);
		} finally {
			IOUtils.closeQuitely(is);
		}
	}

	@Deprecated
	private CurrencyData setEuroCurrency(CurrencyLocales currencyName, Date currencyDate) throws SourceException {
		CurrencyData euroValue = new CurrencyData();
		String euro = getEuroValue();
		euroValue.setGold(1);
		if (currencyName == CurrencyLocales.BG) {
			euroValue.setName("Евро");
		} else {
			euroValue.setName("Euro");
		}
		euroValue.setCode("EUR");
		euroValue.setRatio(1);
		euroValue.setReverseRate("0.511292"); // TODO parse from webpage
		euroValue.setRate(euro.substring(0, 7));
		euroValue.setCurrDate(currencyDate);
		euroValue.setfStar(0);
		return euroValue;
	}

	private String getEuroValue() throws SourceException {
		InputStream is = null;
		URL url = null;
		try {
			url = new URL(URL_BNB_INDEX);
			URLConnection connection = url.openConnection();
			connection.setDoInput(true);
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				// read error and throw it to caller
				is = httpConn.getErrorStream();
				throw new SourceException(new String(ByteStreams.toByteArray(is), Charsets.UTF_8.name()));
			}
			is = httpConn.getInputStream();
			Document doc = Jsoup.parse(is, Charsets.UTF_8.name(), URL_BNB_INDEX);

			Element element = doc.select("div#more_information > div.box > div.top > div > ul > li").first();

			Element euroValue = element.getElementsByTag("strong").first();
			// String euroValuReturn = euroValue.text();
			return euroValue.text();

		} catch (Exception e) {
			throw new SourceException("Failed loading currencies from BNB source!", e);
		} finally {
			IOUtils.closeQuitely(is);
		}
	}

	@Override
	public Map<CurrencyLocales, List<CurrencyData>> downloadRates(boolean getFixedRates) throws SourceException {
		Map<CurrencyLocales, List<CurrencyData>> result = Maps.newHashMap();

		List<CurrencyData> ratesEN = getRatesFromUrl(URL_BNB_FORMAT_EN);
		if (getFixedRates) {
			ratesEN.addAll(getFixedRates(CurrencyLocales.EN.name()));
		}
		result.put(CurrencyLocales.EN, ratesEN);

		List<CurrencyData> ratesBG = getRatesFromUrl(URL_BNB_FORMAT_BG);
		if (getFixedRates) {
			ratesBG.addAll(getFixedRates(CurrencyLocales.BG.name()));
		}
		result.put(CurrencyLocales.BG, ratesBG);

		return result;
	}


	@Override
	public List<CurrencyDataNew> getAllRatesByDate(String initialTime) throws  SourceException{
		return null;
	}

	@Override
	public List<CurrencyDataNew> getAllRatesByDateSource(String initialTime, Integer sourceId) throws  SourceException{
		return null;
	}

	@Override
	public List<CurrencyDataNew> getAllCurrentRatesAfter(String initialTime) throws  SourceException{
		return null;
	}

	@Override
	public List<CurrencyDataNew> getAllCurrentRatesAfter(String initialTime, Integer sourceId) throws  SourceException{
		return null;
	}
}
