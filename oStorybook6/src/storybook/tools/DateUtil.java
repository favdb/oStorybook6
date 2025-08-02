/*
 Storybook: Scene-based software for novelists and authors.
 Copyright (C) 2008-2009 Martin Mustun

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.tools;

import i18n.I18N;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import storybook.App;
import storybook.Pref;

public class DateUtil {

	private static final String TT = "DateUtil.";

	public static Date forYear(Date ds) {
		ds = DateUtil.setMonths(ds, 0);
		ds = DateUtil.setDays(ds, 0);
		ds = DateUtil.setHours(ds, 0);
		ds = DateUtil.setMinutes(ds, 0);
		ds = DateUtil.setSeconds(ds, 0);
		return ds;
	}

	public static Date forMonth(Date ds) {
		ds = DateUtil.setDays(ds, 0);
		ds = DateUtil.setHours(ds, 0);
		ds = DateUtil.setMinutes(ds, 0);
		ds = DateUtil.setSeconds(ds, 0);
		return ds;
	}

	public static Date forDay(Date ds) {
		ds = DateUtil.setHours(ds, 0);
		ds = DateUtil.setMinutes(ds, 0);
		ds = DateUtil.setSeconds(ds, 0);
		return ds;
	}

	public static Date forHour(Date ds) {
		ds = DateUtil.setMinutes(ds, 0);
		ds = DateUtil.setSeconds(ds, 0);
		return ds;
	}

	public static Date forMinute(Date ds) {
		ds = DateUtil.setSeconds(ds, 0);
		return ds;
	}

	public static int daysFrom(Timestamp initDate) {
		return ((Long) ChronoUnit.DAYS.between(initDate.toInstant(), Instant.now())).intValue();
	}

	private DateUtil() {
		// empty
	}

	/**
	 * transform list of dates to a String list separated by comma
	 *
	 * @param dates: list of dates
	 * @return : a string list comma separated
	 */
	public static String getNiceDates(List<Date> dates) {
		DateFormat formatter = I18N.getLongDateFormatter();
		List<String> dateList = new ArrayList<>();
		for (Date date : dates) {
			dateList.add(formatter.format(date));
		}
		return ListUtil.join(dateList, ", ");
	}

	/**
	 * get a String formatted date
	 *
	 * @param cal
	 * @return
	 */
	public static String calendarToString(Calendar cal) {
		if (cal == null) {
			return ("null");
		}
		SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'at' HH:mm:ss a zzz");
		return formatter.format(cal.getTime());
	}

	public static Timestamp addDateTimeToTS(Date date, Date time) {
		return new Timestamp(addDateTime(date, time).getTime());
	}

	public static Date addDateTime(Date date, Date time) {
		int h = 0, m = 0, s = 0;
		if (time != null) {
			Calendar calTime = Calendar.getInstance();
			calTime.setTime(time);
			h = calTime.get(Calendar.HOUR_OF_DAY);
			m = calTime.get(Calendar.MINUTE);
			s = calTime.get(Calendar.SECOND);
		}
		date = DateUtil.setHours(date, h);
		date = DateUtil.setMinutes(date, m);
		date = DateUtil.setSeconds(date, s);
		return date;
	}

	/**
	 * add a String duration expressed by YYa MMb ddc HHe MMf SSg
	 *
	 * a is the initial for years, y for english b is the initial for month, M for english c is the
	 * initial for days, d for english e is the initial for hours, h for english f is the initial
	 * for minutes, m for english g is the initial for seconds, s for english
	 *
	 * for english initals may be yMdhms pour le français les initiales sont aMjhms
	 *
	 * @param date: the date to add to
	 * @param toAdd: the String duration to add formatted as yy-MM-dd_HH:mm:ss
	 *
	 * @return
	 */
	public static Date addDateTime(Date date, String toAdd) {
		//App.trace("DateUtil.addDateTime(date=" + DateUtil.dateToString(date)
		//		+ ", toAdd=" + (toAdd == null ? "null" : toAdd) + ")");
		if (toAdd.length() < 1) {
			return date;
		}
		SbDuration dur = SbDuration.getFromText(toAdd);
		//App.trace("adding " + dur.toText() + " to the given date");
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		if (dur.years > 0) {
			c.add(Calendar.YEAR, dur.years);
		}
		if (dur.months > 0) {
			c.add(Calendar.MONTH, dur.months);
		}
		if (dur.days > 0) {
			c.add(Calendar.DATE, dur.days);
		}
		if (dur.hours > 0) {
			c.add(Calendar.HOUR_OF_DAY, dur.hours);
		}
		if (dur.minutes > 0) {
			c.add(Calendar.MINUTE, dur.minutes);
		}
		if (dur.seconds > 0) {
			c.add(Calendar.SECOND, dur.seconds);
		}
		//App.trace("resulting c is="+dateToStandard(c.getTime()));
		Date rd = c.getTime();
		//App.trace("resulting date is " + DateUtil.dateToString(rd));
		return rd;
	}

	public static Date addDateTime(Date date, SbDuration toAdd) {
		//App.trace("DateUtil.addDateTime(date=" + DateUtil.dateToString(date)
		//		+ ", toAdd=" + (toAdd == null ? "null" : toAdd.toString()) + ")");
		//App.trace("adding "+toAdd.toText()+" to the given date");
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		if (toAdd.years > 0) {
			c.add(Calendar.YEAR, toAdd.years);
		}
		if (toAdd.months > 0) {
			c.add(Calendar.MONTH, toAdd.months);
		}
		if (toAdd.days > 0) {
			c.add(Calendar.DATE, toAdd.days);
		}
		if (toAdd.hours > 0) {
			c.add(Calendar.HOUR_OF_DAY, toAdd.hours);
		}
		if (toAdd.minutes > 0) {
			c.add(Calendar.MINUTE, toAdd.minutes);
		}
		if (toAdd.seconds > 0) {
			c.add(Calendar.SECOND, toAdd.seconds);
		}
		//App.trace("resulting c is="+dateToStandard(c.getTime()));
		//App.trace("resulting date is " + DateUtil.dateToString(c.getTime()));
		return c.getTime();
	}

	public static Date getZeroTimeDate() {
		Calendar cal = Calendar.getInstance();
		clearTime(cal);
		return cal.getTime();
	}

	public static boolean isZeroTimeDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.HOUR_OF_DAY) == 0
				&& cal.get(Calendar.MINUTE) == 0
				&& cal.get(Calendar.SECOND) == 0
				&& cal.get(Calendar.MILLISECOND) == 0;
	}

	public static Date getZeroTimeDate(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		clearTime(cal);
		return cal.getTime();
	}

	public static int daysBetween(Date d1, Date d2) {
		//LOG.trace(TT + "daysBetween(d1=" + d1.toString() + ", d2=" + d2.toString() + ")");
		return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
	}

	public static void expandDates(List<Date> dates) {
		expandDates(dates, 1, 1);
	}

	public static void expandDates(List<Date> dates, int count) {
		expandDates(dates, count, count);
	}

	public static void expandDates(List<Date> dates, int countPast, int countFuture) {
		expandDatesToPast(dates, countPast);
		expandDatesToFuture(dates, countFuture);
	}

	public static void expandDatesToFuture(List<Date> dates) {
		expandDatesToFuture(dates, 1);
	}

	public static void expandDatesToFuture(List<Date> dates, int count) {
		dates.removeAll(Collections.singletonList(null));
		if (dates.isEmpty()) {
			return;
		}
		for (int i = 0; i < count; ++i) {
			Date lastDate = Collections.max(dates);
			if (lastDate == null) {
				return;
			}
			lastDate = new Date(DateUtil.addDays(lastDate, 1).getTime());
			dates.add(lastDate);
		}
	}

	public static void expandDatesToPast(List<Date> dates) {
		expandDatesToPast(dates, 1);
	}

	public static void expandDatesToPast(List<Date> dates, int count) {
		if (dates.isEmpty()) {
			return;
		}
		dates.removeAll(Collections.singletonList(null));
		for (int i = 0; i < count; ++i) {
			Date firstDate = Collections.min(dates);
			firstDate = new Date(DateUtil.addDays(firstDate, -1).getTime());
			dates.add(firstDate);
		}
	}

	/**
	 * get a Date from a formatted String as MM-dd-yyyy HH:mm:ss
	 *
	 * @param str: the String contained the formatted date
	 * @return : the corresponding date
	 */
	public static Date stringToDate(String str) {
		Date d = null;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy" + " HH:mm:ss");
			ParsePosition pos = new ParsePosition(0);
			d = formatter.parse(str, pos);
		} catch (RuntimeException e) {
		}
		return d;
	}

	/**
	 * get a Date from a formatted String as yyyy-MM-dd HH:mm:ss
	 *
	 * @param str: the String contained the formatted date
	 * @return : the corresponding date
	 */
	public static Date stdStringToDate(String str) {
		Date d = null;
		try {
			SimpleDateFormat formatter
					= new SimpleDateFormat(
							App.preferences.getString(Pref.KEY.DATEFORMAT)
							+ " HH:mm:ss");
			ParsePosition pos = new ParsePosition(0);
			d = formatter.parse(str, pos);
		} catch (RuntimeException e) {
		}
		return d;
	}

	/**
	 * get a TimeStamp from a formatted String as yyyy-MM-dd HH:mm:ss
	 *
	 * @param str: the String contained the formatted date
	 * @return : the corresponding date
	 */
	public static Timestamp stdStringToTimestamp(String str) {
		Timestamp d = null;
		try {
			Date dt = stdStringToDate(str);
			if (dt != null) {
				d = new Timestamp(dt.getTime());
			}
		} catch (RuntimeException e) {
		}
		return d;
	}

	/**
	 * simple format a Date to a String
	 *
	 * @param date: the date to format
	 * @param seconds: optional, if true time with seconds
	 *
	 * @return : the String formatted date
	 */
	public static String dateToString(Date date, boolean... seconds) {
		if (date == null) {
			return "null";
		}
		SimpleDateFormat formatter = new SimpleDateFormat(App.preferences.getString(Pref.KEY.DATEFORMAT) + " HH:mm");
		if (seconds != null && seconds.length > 0 && seconds[0]) {
			formatter = new SimpleDateFormat(App.preferences.getString(Pref.KEY.DATEFORMAT) + " HH:mm:ss");
		}
		return formatter.format(date);
	}

	/**
	 * only time, simple format a Date to a String
	 *
	 * @param date: the date to format
	 * @param seconds: optional, if true time with seconds
	 *
	 * @return : the String formatted time
	 */
	public static String timeToString(Date date, boolean... seconds) {
		if (date == null) {
			return "null";
		}
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		if (seconds != null && seconds.length > 0 && seconds[0]) {
			formatter = new SimpleDateFormat("HH:mm:ss");
		}
		return (formatter.format(date));
	}

	/**
	 * only date, simple format a Date to a String
	 *
	 * @param date: the date to format
	 * @return : the String formatted date
	 */
	public static String simpleDateToString(Date date) {
		if (date == null) {
			return "null";
		}
		SimpleDateFormat formatter = new SimpleDateFormat(App.preferences.getString(Pref.KEY.DATEFORMAT));
		return formatter.format(date);
	}

	/**
	 * full date and time, simple format a Date to a String
	 *
	 * @param date: the date to format
	 * @param seconds: optional, if true then with seconds
	 *
	 * @return : the String formatted date and time
	 */
	public static String simpleDateTimeToString(Date date, boolean... seconds) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat formatter = new SimpleDateFormat(App.preferences.getString(Pref.KEY.DATEFORMAT) + " HH:mm");
		if (seconds != null && seconds.length > 0 && seconds[0]) {
			formatter = new SimpleDateFormat(App.preferences.getString(Pref.KEY.DATEFORMAT) + " HH:mm:ss");
		}
		if (isZeroTimeDate(date)) {
			formatter = new SimpleDateFormat(App.preferences.getString(Pref.KEY.DATEFORMAT));
		}
		return (formatter.format(date));
	}

	/**
	 * getting a DateFormat
	 *
	 * @return
	 */
	public static DateFormat simpleDateToString() {
		SimpleDateFormat formatter = new SimpleDateFormat(App.preferences.getString(Pref.KEY.DATEFORMAT));
		return (formatter);
	}

	/**
	 * clear the time part of a Calendar
	 *
	 * @param cal the value of cal
	 * @return calendar
	 */
	public static Calendar clearTime(Calendar cal) {
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	/**
	 * standard format of a Date
	 *
	 * @param date
	 * @return : the formatted date and time
	 */
	public static String dateToStandard(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);

	}

	public static Date standardToDate(String date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dx = null;
		try {
			dx = formatter.parse(date);
		} catch (ParseException ex) {
		}
		return dx;

	}

	/**
	 * coded format of the Date of day
	 *
	 * @param date
	 * @return : the formatted date and time
	 */
	public static String dateToCode() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		return formatter.format(new Date());

	}

	/**
	 * convert a date to minutes as Long
	 *
	 * @param date: the date to convert
	 * @return : the number of minutes
	 */
	public static long toMinutes(Date date) {
		if (date == null) {
			return (0L);
		}
		long r;
		int year = getYear(date);
		int month = getMonth(date);
		int day = getDay(date);
		int HH = getHour(date);
		int mm = getMinute(date);
		r = year * (365 * 24 * 60);
		r += howManyDays(month) * (24 * 60);
		r += day * (24 * 60);
		r += HH * (60);
		r += mm;
		return r;
	}

	/**
	 * compute how many days for a given month
	 *
	 * @param month: the month to search
	 * @return : number of days
	 */
	public static int howManyDays(int month) {
		int r = 0;
		int[] dm = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
		for (int i = 0; i < month - 1; i++) {
			r += dm[i];
		}
		return (r);
	}

	/**
	 * get the years part of a date
	 *
	 * @param date: the date to scan
	 * @return : the year part
	 */
	public static int getYear(Date date) {
		String[] d = dateToStandard(date).split(" ");
		String[] dx = d[0].split("-");
		return (Integer.parseInt(dx[0]));
	}

	/**
	 * get the month part of a date
	 *
	 * @param date: the date to scan
	 * @return : the month number part
	 */
	public static int getMonth(Date date) {
		String[] d = dateToStandard(date).split(" ");
		String[] dx = d[0].split("-");
		return (Integer.parseInt(dx[1]));
	}

	/**
	 * get the days part of a date
	 *
	 * @param date: the date to scan
	 * @return : the days part
	 */
	public static int getDay(Date date) {
		String[] d = dateToStandard(date).split(" ");
		String[] dx = d[0].split("-");
		return (Integer.parseInt(dx[2]));
	}

	/**
	 * get the hours part of a date
	 *
	 * @param date: the date to scan
	 * @return : the hours part
	 */
	public static int getHour(Date date) {
		String[] d = dateToStandard(date).split(" ");
		String[] dx = d[1].split(":");
		return (Integer.parseInt(dx[0]));
	}

	/**
	 * get the minutes part of a date
	 *
	 * @param date: the date to scan
	 * @return : the minutes part
	 */
	public static int getMinute(Date date) {
		String[] d = dateToStandard(date).split(" ");
		String[] dx = d[1].split(":");
		return (Integer.parseInt(dx[1]));
	}

	/**
	 * get number of minutes from a date
	 *
	 * @param date
	 * @return
	 */
	public static Long getMinutes(Date date) {
		if (date == null) {
			return 0L;
		}
		return date.getTime() / 60000L;
	}

	/*
	From org.apache.commons.lang3
	 */
	/**
	 * Adds a number of years to a date returning a new object. The original {@code Date} is
	 * unchanged.
	 *
	 * @param date the date, not null
	 * @param amount the amount to add, may be negative
	 * @return the new {@code Date} with the amount added
	 * @throws IllegalArgumentException if the date is null
	 */
	public static Date addYears(Date date, int amount) {
		return add(date, Calendar.YEAR, amount);
	}

	/**
	 * Adds a number of months to a date returning a new object. The original {@code Date} is
	 * unchanged.
	 *
	 * @param date the date, not null
	 * @param amount the amount to add, may be negative
	 * @return the new {@code Date} with the amount added
	 * @throws IllegalArgumentException if the date is null
	 */
	public static Date addMonths(final Date date, final int amount) {
		return add(date, Calendar.MONTH, amount);
	}

	/**
	 * Adds a number of weeks to a date returning a new object. The original {@code Date} is
	 * unchanged.
	 *
	 * @param date the date, not null
	 * @param amount the amount to add, may be negative
	 * @return the new {@code Date} with the amount added
	 * @throws IllegalArgumentException if the date is null
	 */
	public static Date addWeeks(final Date date, final int amount) {
		return add(date, Calendar.WEEK_OF_YEAR, amount);
	}

	/**
	 * Adds a number of days to a date returning a new object. The original {@code Date} is
	 * unchanged.
	 *
	 * @param date the date, not null
	 * @param amount the amount to add, may be negative
	 * @return the new {@code Date} with the amount added
	 * @throws IllegalArgumentException if the date is null
	 */
	public static Date addDays(Date date, int amount) {
		return add(date, Calendar.DAY_OF_MONTH, amount);
	}

	public static Timestamp addNbDays(Timestamp date, Integer days) {
		return new Timestamp(addDays(new Date(date.getTime()), days).getTime());
	}

	/**
	 * Adds a number of hours to a date returning a new object. The original {@code Date} is
	 * unchanged.
	 *
	 * @param date the date, not null
	 * @param amount the amount to add, may be negative
	 * @return the new {@code Date} with the amount added
	 * @throws IllegalArgumentException if the date is null
	 */
	public static Date addHours(final Date date, final int amount) {
		return add(date, Calendar.HOUR_OF_DAY, amount);
	}

	/**
	 * Adds a number of minutes to a date returning a new object. The original {@code Date} is
	 * unchanged.
	 *
	 * @param date the date, not null
	 * @param amount the amount to add, may be negative
	 * @return the new {@code Date} with the amount added
	 * @throws IllegalArgumentException if the date is null
	 */
	public static Date addMinutes(final Date date, final int amount) {
		return add(date, Calendar.MINUTE, amount);
	}

	/**
	 * Adds a number of seconds to a date returning a new object. The original {@code Date} is
	 * unchanged.
	 *
	 * @param date the date, not null
	 * @param amount the amount to add, may be negative
	 * @return the new {@code Date} with the amount added
	 * @throws IllegalArgumentException if the date is null
	 */
	public static Date addSeconds(final Date date, final int amount) {
		return add(date, Calendar.SECOND, amount);
	}

	/**
	 * Adds a number of milliseconds to a date returning a new object. The original {@code Date} is
	 * unchanged.
	 *
	 * @param date the date, not null
	 * @param amount the amount to add, may be negative
	 * @return the new {@code Date} with the amount added
	 * @throws IllegalArgumentException if the date is null
	 */
	public static Date addMilliseconds(final Date date, final int amount) {
		return add(date, Calendar.MILLISECOND, amount);
	}

	/**
	 * Adds to a date returning a new object. The original {@code Date} is unchanged.
	 *
	 * @param date the date, not null
	 * @param calendarField the calendar field to add to
	 * @param amount the amount to add, may be negative
	 * @return the new {@code Date} with the amount added
	 * @throws IllegalArgumentException if the date is null
	 */
	private static Date add(Date date, int calendarField, int amount) {
		if (date == null) {
			throw new IllegalArgumentException("Date must not be null");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(calendarField, amount);
		return c.getTime();
	}

	/**
	 * add a duration to a date if duration is null return the date
	 *
	 * @param date: date from
	 * @param duration: SbDuration to add
	 * @return the computed Date
	 */
	public static Date add(Date date, SbDuration duration) {
		if (duration == null) {
			return date;
		}
		//LOG.trace("DateUtil.add(date=" + date.toString() + ", SbDuration=" + d.toText()+")");
		Calendar c = Calendar.getInstance();
		if (c == null) {
			LOG.err("DateUtil.add Calendar.getInstance return null");
			return date;
		}
		c.setTime(date);
		if (duration.years > 0) {
			c.add(Calendar.YEAR, duration.years);
		}
		if (duration.months > 0) {
			c.add(Calendar.MONTH, duration.months);
		}
		if (duration.days > 0) {
			c.add(Calendar.DAY_OF_MONTH, duration.days);
		}
		if (duration.hours > 0) {
			c.add(Calendar.HOUR_OF_DAY, duration.hours);
		}
		if (duration.minutes > 0) {
			c.add(Calendar.MINUTE, duration.minutes);
		}
		if (duration.seconds > 0) {
			c.add(Calendar.SECOND, duration.seconds);
		}
		return c.getTime();
	}

	/**
	 * add a duration String to a date if the duration String is null or empty return the date.
	 *
	 * @param date: date from
	 * @param duration: the SbDuration String to add
	 * @return the computed Date
	 */
	public static Date add(Date date, String duration) {
		//LOG.trace(TT + "add(date=" + date.toString() + ", duration=" + duration + ")");
		if (duration == null || duration.isEmpty()) {
			return date;
		}
		SbDuration d = new SbDuration(duration);
		return add(date, d);
	}

	/**
	 * Sets the years field to a date returning a new object. The original {@code Date} is
	 * unchanged.
	 *
	 * @param date the date, not null
	 * @param amount the amount to set
	 * @return a new {@code Date} set with the specified value
	 * @throws IllegalArgumentException if the date is null
	 */
	public static Date setYears(final Date date, final int amount) {
		return set(date, Calendar.YEAR, amount);
	}

	/**
	 * Sets the months field to a date returning a new object. The original {@code Date} is
	 * unchanged.
	 *
	 * @param date the date, not null
	 * @param amount the amount to set
	 * @return a new {@code Date} set with the specified value
	 * @throws IllegalArgumentException if the date is null
	 */
	public static Date setMonths(final Date date, final int amount) {
		return set(date, Calendar.MONTH, amount);
	}

	/**
	 * Sets the day of month field to a date returning a new object. The original {@code Date} is
	 * unchanged.
	 *
	 * @param date the date, not null
	 * @param amount the amount to set
	 * @return a new {@code Date} set with the specified value
	 * @throws IllegalArgumentException if the date is null
	 */
	public static Date setDays(final Date date, final int amount) {
		return set(date, Calendar.DAY_OF_MONTH, amount);
	}

	/**
	 * Sets the hours field to a date returning a new object. Hours range from 0-23. The original
	 * {@code Date} is unchanged.
	 *
	 * @param date the date, not null
	 * @param amount the amount to set
	 * @return a new {@code Date} set with the specified value
	 * @throws IllegalArgumentException if the date is null
	 */
	public static Date setHours(final Date date, final int amount) {
		return set(date, Calendar.HOUR_OF_DAY, amount);
	}

	/**
	 * Sets the minute field to a date returning a new object. The original {@code Date} is
	 * unchanged.
	 *
	 * @param date the date, not null
	 * @param amount the amount to set
	 * @return a new {@code Date} set with the specified value
	 * @throws IllegalArgumentException if the date is null
	 */
	public static Date setMinutes(final Date date, final int amount) {
		return set(date, Calendar.MINUTE, amount);
	}

	/**
	 * Sets the seconds field to a date returning a new object. The original {@code Date} is
	 * unchanged.
	 *
	 * @param date the date, not null
	 * @param amount the amount to set
	 * @return a new {@code Date} set with the specified value
	 * @throws IllegalArgumentException if the date is null
	 */
	public static Date setSeconds(final Date date, final int amount) {
		return set(date, Calendar.SECOND, amount);
	}

	/**
	 * Sets the milliseconds field to a date returning a new object. The original {@code Date} is
	 * unchanged.
	 *
	 * @param date the date, not null
	 * @param amount the amount to set
	 * @return a new {@code Date} set with the specified value
	 * @throws IllegalArgumentException if the date is null
	 */
	public static Date setMilliseconds(final Date date, final int amount) {
		return set(date, Calendar.MILLISECOND, amount);
	}

	/**
	 * Sets the specified field to a date returning a new object. This does not use a lenient
	 * calendar. The original {@code Date} is unchanged.
	 *
	 * @param date the date, not null
	 * @param calendarField the {@code Calendar} field to set the amount to
	 * @param amount the amount to set
	 * @return a new {@code Date} set with the specified value
	 * @throws IllegalArgumentException if the date is null
	 */
	private static Date set(final Date date, final int calendarField, final int amount) {
		final Calendar c = Calendar.getInstance();
		c.setLenient(false);
		if (date == null) {
			c.setTime(new Date());
		} else {
			c.setTime(date);
		}
		c.set(calendarField, amount);
		return c.getTime();
	}

	public static boolean isSameDay(Date dtb, Date dte) {
		return (daysBetween(dtb, dte) < 1);
	}

	public static Long difMinutes(Date end, Date begin) {
		return (getMinutes(end) - getMinutes(begin));
	}

	/**
	 * get today date at 00:00:00
	 *
	 * @return
	 */
	public static Date getToday() {
		Calendar today = Calendar.getInstance();
		today.clear(Calendar.HOUR);
		today.clear(Calendar.MINUTE);
		today.clear(Calendar.SECOND);
		return today.getTime();
	}

	public static boolean between(Date date, Date tsStart, Date tsEnd) {
		if (date == null || tsStart == null || tsEnd == null) {
			return false;
		}
		if (date.equals(tsStart) || date.equals(tsEnd)) {
			return true;
		}
		return date.after(tsStart) && date.before(tsEnd);
	}

}
