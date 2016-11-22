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
package net.vexelon.currencybg.app.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.content.Context;

import net.vexelon.currencybg.app.Defs;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class DateTimeUtils {

	public static final long MS_PER_DAY = (24 * 60 * 60 * 1000);

	protected static final Calendar CALENDAR = Calendar.getInstance();
	protected static DateFormat DT_FORMAT = null;
	protected static DateFormat DATE_FORMAT = null;

	// Test Joda time methods
	public static void main(String[] args) {
//		SimpleDateFormat sdfBulgaria = new SimpleDateFormat(Defs.DATEFORMAT_ISO8601);
//		TimeZone tzInBulgaria = TimeZone.getTimeZone("Europe/Sofia");
//		sdfBulgaria.setTimeZone(tzInBulgaria);
//
//
//		SimpleDateFormat sdf = new SimpleDateFormat(Defs.DATEFORMAT_ISO8601);
//
//		Date date = new Date();
//		System.out.println(sdf.format(new Timestamp(date.getTime())));
//
//		DateTime dateTime = new DateTime(new Timestamp(date.getTime()));
//		DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(Defs.DATEFORMAT_ISO8601);
//		System.out.println(dateTimeFormatter.print(dateTime));


//		DateTimeZone timeZone = DateTimeZone.getDefault();
//		DateTime dateTime = new DateTime();
//		DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").withZone(DateTimeZone.forID("Europe/Sofia"));//America/New_York
//		System.out.println(dateTimeFormatter.print(dateTime.minusDays(3)).replace("+0200","+02:00"));

		System.out.print(getOldDate(3));



	}

	/**
	 *  Returns OldDate = CurrencyDate - X days. An example format - 2016-11-17T14:49:41+02:00
	 * @param minusDays
	 * @return
     */
	public static String getOldDate(int minusDays){
		DateTime dateTime = new DateTime();
		return dateTime.minusDays(minusDays).withZone(DateTimeZone.forID("Europe/Sofia")).toString();
	}



//	public static void main(String[] args) {
//		DateFormat dateFormat = new SimpleDateFormat(Defs.DATEFORMAT_ISO8601);
//		Date date = new Date();
//		System.out.println("Currect Time: " + dateFormat.format(date)); // 2014/08/06
//																		// 15:59:48
//		System.out.println();
//
//		SimpleDateFormat sdfAmerica = new SimpleDateFormat(Defs.DATEFORMAT_ISO8601);
//		TimeZone tzInAmerica = TimeZone.getTimeZone("America/New_York");
//		sdfAmerica.setTimeZone(tzInAmerica);
//
//		String sDateInAmerica = sdfAmerica.format(date);
//		System.out.println("America String: " + sDateInAmerica);
//		Date dateObject1 = parseStringToDate(sDateInAmerica);
//		System.out.println("Bulgaria Date: " + dateFormat.format(dateObject1));
//		System.out.println();
//
//		try {
//			Date dateInAmerica = sdfAmerica.parse(sDateInAmerica);
//			System.out.println("America Date: " + sDateInAmerica);
//			String stringOject = parseDateToString(dateInAmerica, Defs.DATEFORMAT_ISO8601);
//			System.out.println("Bulgaria String: " + stringOject);
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//
//	}

	/**
	 *
	 * @param dateTime
	 * @param dateFormater
	 * @return
	 */
	public static String parseDateToString(Date dateTime, String dateFormater) {
		DateTime jodaTime = new DateTime(dateTime);
		DateTimeFormatter jodaFormater = DateTimeFormat.forPattern(dateFormater);
		return jodaFormater.print(jodaTime);
	}

	/**
	 *
	 * @param dateTime
	 * @return
	 */
	public static Date parseStringToDate(String dateTime) {
		DateTimeFormatter parse = ISODateTimeFormat.dateTimeParser();
		DateTime dateTimeHere = parse.parseDateTime(dateTime);
		Date dateNew = dateTimeHere.toDate();
		return dateNew;
	}

	/**
	 * Датата се сетва, като се вземе текущата година и се добави 01.01.
	 * Използва се за фиксираните валути.
	 * 
	 * @return
	 */
	public static Date getCurrentYear() {
		int year = Calendar.getInstance().get(Calendar.YEAR);
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		String dateInString = "01.01." + year;
		Date currentYear = null;
		try {
			currentYear = sdf.parse(dateInString);
		} catch (ParseException e1) {
			e1.printStackTrace();
			// use default (today)
		}
		return currentYear;
	}

	private static void initDateFormat(Context context) {
		if (DT_FORMAT == null) {
			DT_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT,
					context.getResources().getConfiguration().locale);
		}
		if (DATE_FORMAT == null) {
			DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM,
					context.getResources().getConfiguration().locale);
		}
	}

	/**
	 * Millisecond representation of the given time
	 * 
	 * @return milliseconds representing the given time
	 */
	public static long calculateTime(int hours, int minutes, int seconds, int ms) {
		return (((((hours * 60) + minutes) * 60) + seconds) * 1000) + ms;
	}

	/**
	 * @see {@link #calculateTime(int, int, int, int)}
	 */
	public static long calculateTime(int hours, int minutes, int seconds) {
		return calculateTime(hours, minutes, seconds, 0);
	}

	public static long getTimeFromDate(Date date) {
		Date now = Calendar.getInstance().getTime();
		long timePortion = now.getTime() % MS_PER_DAY;

		return timePortion;
	}

	/**
	 * Checks if year, month, date of two <code>Date</code> objects match.
	 * 
	 * @param first
	 * @param second
	 * @return
	 */
	public static boolean equalDates(Date first, Date second) {
		Calendar calFirst = Calendar.getInstance();
		Calendar calSecond = Calendar.getInstance();

		calFirst.setTime(first);
		calSecond.setTime(second);

		return calFirst.get(Calendar.YEAR) == calSecond.get(Calendar.YEAR)
				&& calFirst.get(Calendar.MONTH) == calSecond.get(Calendar.MONTH)
				&& calFirst.get(Calendar.DAY_OF_MONTH) == calSecond.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Reset {@code date} object to start 00:00:00 time of the day.
	 * 
	 * @param date
	 * @return
	 */
	public static Date zeroTimePortionOfADate(Date date) {
		// Calendar cal =
		// GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC +0"));
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.AM_PM, Calendar.AM);
		return cal.getTime();
	}

	/**
	 * @return the first date in 1970 with millisSinceMidnight added to it
	 */
	public static Date getDateFromTime(long millisSinceMidnight) {

		Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC +0"));
		Date date = new Date();
		date.setTime(millisSinceMidnight);
		cal.setTime(date);

		return cal.getTime();
	}

	/**
	 * Get the last hour, minute, second of the date specified
	 * 
	 * @param date
	 * @return
	 */
	public static Date endOfDay(Date date) {
		Calendar calendar = CALENDAR;
		synchronized (calendar) {
			calendar.setTime(date);
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MILLISECOND, 999);
			calendar.set(Calendar.SECOND, 59);
			calendar.set(Calendar.MINUTE, 59);
			return calendar.getTime();
		}
	}

	/**
	 * 
	 * @return
	 * @see #endOfDay(Date)
	 */
	public static Date endOfToday() {
		return endOfDay(new Date());
	}

	/**
	 * Get the first hour, minute, second of the date specified
	 * 
	 * @param date
	 * @return
	 */
	public static Date startOfDay(Date date) {
		Calendar calendar = CALENDAR;
		synchronized (calendar) {
			calendar.setTime(date);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MINUTE, 0);
			return calendar.getTime();
		}
	}

	/**
	 * 
	 * @return
	 * @see #startOfDay(Date)
	 */
	public static Date startOfToday() {
		return startOfDay(new Date());
	}

	public static String toDateTimeText(Context context, Date date) {
		initDateFormat(context);
		return DT_FORMAT.format(date);
	}

	public static String toDateText(Context context, Date date) {
		initDateFormat(context);
		return DATE_FORMAT.format(date);
	}

}
