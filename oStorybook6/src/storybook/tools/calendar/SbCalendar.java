/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

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
package storybook.tools.calendar;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import storybook.App;
import storybook.db.SbDate;
import storybook.tools.ListUtil;

/**
 *
 * @author favdb
 */
public class SbCalendar {

	public enum KEY {
		CALENDAR_DAYS("CalendarDays", ""),//list of DAY (name,abbr)
		CALENDAR_HOURS("CalendarHours", "24:60:60"),//hours configuration (HH:MM:SS)
		CALENDAR_MONTHS("CalendarMonths", ""),//list of MONTH (name,abbr,nd days)
		CALENDAR_STARTDAY("CalendarStartDay", "5"),//index of starting day
		CALENDAR_USE("UseCalendar", "0"),//use this specific calendar
		CALENDAR_YEARDAYS("CalendarYearDays", "365");//nb of days in one year
		private final String text, val;

		private KEY(String text, String val) {
			this.text = text;
			this.val = val;
		}

		@Override
		public String toString() {
			return text;
		}

		public int getInteger() {
			return (Integer.parseInt(val));
		}

		public boolean getBoolean() {
			return (val.equals("true") || val.equals("1"));
		}

		public String getString() {
			return (val);
		}
	}

	public boolean use = false;
	public int yeardays = 365;
	public int hours = 24;
	public int minutes = 60;
	public int seconds = 60;
	public int startday = 5;
	public List<MONTH> months;
	public List<DAY> days;
	public static boolean LongName = true;
	public int first_day;

	public SbCalendar() {
		setYearDays(365);
		init();
		setStartDay(5);
		setHours(24);
		setMinutes(60);
		setSeconds(60);
	}

	public void setCalendar(String g) {
		String[] y = g.split(",");
		hours = Integer.parseInt(y[0]);
		minutes = Integer.parseInt(y[1]);
		seconds = Integer.parseInt(y[2]);
		startday = Integer.parseInt(y[3]);
	}

	private void init() {
		App app = App.getInstance();
		DateFormatSymbols dfsFR = new DateFormatSymbols();
		try {
			dfsFR = new DateFormatSymbols(app.getLocale());
		} catch (Exception ex) {
		}
		String[] joursLongs = dfsFR.getWeekdays();
		String[] joursCourts = dfsFR.getShortWeekdays();
		days = new ArrayList<>();
		for (int i = 1; i < joursLongs.length; i++) {
			days.add(new DAY(joursLongs[i] + "," + joursCourts[i]));
		}
		String[] moisCourts = dfsFR.getShortMonths();
		String[] moisLongs = dfsFR.getMonths();
		int[] jours = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
		months = new ArrayList<>();
		for (int i = 0; i < moisLongs.length - 1; i++) {
			String s = String.format("%s,%s,%d", moisLongs[i], moisCourts[i], jours[i]);
			months.add(new MONTH(s));
		}
	}

	public boolean getUse() {
		return use;
	}

	public void setUse(boolean use) {
		this.use = use;
	}

	public void setYearDays(int n) {
		yeardays = n;
	}

	public int getYearDays() {
		return yeardays;
	}

	public void setDays(String ms) {
		//App.trace("SbCalendar.setDays(ms="+ms+")");
		if (ms.isEmpty()) {
			return;
		}
		days = new ArrayList<>();
		String m[] = ms.split(",");
		for (String m1 : m) {
			DAY lm = new DAY(m1);
			days.add(lm);
		}
	}

	public void setStartDay(int d) {
		if (d > days.size()) {
			return;
		}
		startday = d;
	}

	public void setMonths(String ms) {
		//App.trace("SbCalendar.Months(ms="+ms+")");
		if (ms.isEmpty()) {
			return;
		}
		months = new ArrayList<>();
		String m[] = ms.split(";");
		for (String m1 : m) {
			MONTH lm = new MONTH(m1);
			months.add(lm);
		}
	}

	public List<MONTH> getMonths() {
		return (months);
	}

	public int getMonthDays(int i) {
		return (months.get(i).days);
	}

	public String getMonthName(int idx) {
		if (idx < 1 || idx >= months.size()) {
			return ("month error=" + idx);
		}
		return (months.get(idx - 1).name);
	}

	public String getMonthAbbr(int idx) {
		if (idx < 1 || idx >= months.size()) {
			return ("month error=" + idx);
		}
		return (months.get(idx - 1).abbr);
	}

	public String getMonthName(DATETIME date) {
		return (getMonthName(date, false));
	}

	public String getMonthName(DATETIME date, boolean isLongName) {
		String d = date.getDate();
		String[] x = d.split("/");
		if (isLongName) {
			return (months.get(Integer.parseInt(x[1])).name);
		} else {
			return (months.get(Integer.parseInt(x[1])).abbr);
		}
	}

	public void setHours(int n) {
		if (n > 99) {
			return;
		}
		hours = n;
	}

	public void setHours(String s) {
		if (s != null && !s.isEmpty()) {
			String h[] = s.split(":");
			if (h.length > 0) {
				hours = Integer.parseInt(h[0]);
			}
			if (h.length > 1) {
				minutes = Integer.parseInt(h[1]);
			}
			if (h.length > 2) {
				seconds = Integer.parseInt(h[2]);
			}
		} else {
			hours = 24;
			minutes = 60;
			seconds = 60;
		}
	}

	public void setMinutes(int n) {
		if (n > 99) {
			return;
		}
		minutes = n;
	}

	public void setSeconds(int n) {
		if (n > 99) {
			return;
		}
		seconds = n;
	}

	public DATETIME setDateTime(String x) {
		String[] p = x.split(" ");
		String[] date = p[0].split("/");
		int day = Integer.parseInt(date[0]);
		int month = Integer.parseInt(date[1]);
		int year = Integer.parseInt(date[2]);
		DATETIME d = new DATETIME(year, month, day);
		if (!p[1].isEmpty()) {
			String[] time = p[1].split(":");
			int hh = Integer.parseInt(time[0]);
			int mm = Integer.parseInt(time[1]);
			int ss = 0;
			if (time.length > 2) {
				ss = Integer.parseInt(time[2]);
			}
			d.setTime(hh, mm, ss);
		}
		return (d);
	}

	public String getDayName(int idx) {
		if (idx < 1 || idx >= days.size()) {
			return ("day error=" + idx);
		}
		return (days.get(idx - 1).getName());
	}

	public String getDayAbbr(int idx) {
		if (idx < 1 || idx >= days.size()) {
			return ("day error=" + idx);
		}
		return (days.get(idx - 1).getAbbr());
	}

	public String getDayOfWeek(DATETIME date) {
		return (getDayOfWeek(date, false));
	}

	public String getDayOfWeek(DATETIME date, boolean isLongName) {
		if (date == null) {
			return ("");
		}
		long t = (date.Year * yeardays);
		for (int i = 0; i < date.Month; i++) {
			t += months.get(i).days;
		}
		t += date.Day;
		long x = (t + startday) % days.size();
		if (isLongName) {
			return (days.get((int) x).getName());
		} else {
			return (days.get((int) t).getAbbr());
		}
	}

	/**
	 * Get the formated date
	 *
	 * @param str Y : year (4 digits minimum) M : month number (2 digits) MM : month abbr MMM :
	 * month name D : day number in the month (2 digits) DD : day abbr DDD : day name
	 * @param date
	 *
	 * @return a String
	 *
	 * example for a full date string : - with day name and month name : "DDD MMM Y" - with day abbr
	 * and month abbr : "DD MM Y" - short date with slash separator : "D/M/Y" - the default
	 * representation is : "Y-M-D"
	 */
	public String formatDate(String str, SbDate date) {
		if (str == null || str.isEmpty()) {
			return (formatDate("Y-M-D", date));
		}
		StringBuilder r = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			switch (str.charAt(i)) {
				case 'Y':
					r.append(String.format("%4d", date.getYear()));
					break;
				case 'M':
					if (str.charAt(i + 1) == 'M' && str.charAt(i + 2) == 'M') {
						r.append(getMonthName(date.getMonth()));
						i += 2;
					} else if (str.charAt(i + 1) == 'M') {
						r.append(getMonthAbbr(date.getMonth()));
						i++;
					} else {
						r.append(String.format("%2d", date.getMonth()));
					}
					break;
				case 'D':
					if (str.charAt(i + 1) == 'D' && str.charAt(i + 2) == 'D') {
						r.append(getDayName(date));
						i += 2;
					} else if (str.charAt(i + 1) == 'D') {
						r.append(getDayAbbr(date));
						i++;
					} else {
						r.append(String.format("%2d", date.getDay()));
					}
					break;
				default:
					r.append(str.charAt(i));
					break;
			}
		}
		return (r.toString());
	}

	/**
	 * Default representation of the SbDate
	 *
	 * @param date
	 * @return a String (format is Y-M-D)
	 */
	public String formatDate(SbDate date) {
		return formatDate("Y-M-D", date);
	}

	/**
	 * Get the day name of the SbDate
	 *
	 * @param date
	 * @return a String
	 */
	public String getDayName(SbDate date) {
		return getDayName((int) (toDays(date) % yeardays));
	}

	/**
	 * Get the day abbreviation of the SbDate
	 *
	 * @param date
	 * @return a String
	 */
	public String getDayAbbr(SbDate date) {
		return getDayAbbr((int) (toDays(date) % yeardays));
	}

	/**
	 * Return the formated representation of time
	 *
	 * @param str : represent hours, m represent minutes and s represent seconds
	 * @param date
	 * @return the formated string
	 */
	public String formatTime(String str, SbDate date) {
		String r = "";
		for (int i = 0; i < str.length(); i++) {
			switch (str.charAt(i)) {
				case 'h':
					r += String.format("%2d", date.getHour());
					break;
				case 'm':
					r += String.format("%2d", date.getMinute());
					break;
				case 's':
					r += String.format("%2d", date.getSecond());
					break;
				default:
					r += str.charAt(i);
					break;
			}
		}
		return (r);
	}

	/**
	 * Return the formated representation of time
	 *
	 * @param date
	 * @return
	 */
	public String formatTime(SbDate date) {
		return formatTime("h:m:s", date);
	}

	/**
	 * Convert the date in number of days
	 *
	 * @param date
	 * @return a long value
	 */
	public long toDays(SbDate date) {
		long l = date.getYear() * getYearDays();
		long md = 0L;
		for (int i = 0; i < months.size(); i++) {
			md += getMonthDays(i);
		}
		l += md + date.getDay();
		return l;
	}

	/**
	 * Convert the date and time in number of seconds
	 *
	 * @param date
	 * @return a long value
	 */
	public long toSeconds(SbDate date) {
		long l = (toDays(date) * hours) + date.getHour();
		l = (l * minutes) + date.getMinute();
		l = (l * seconds) + date.getSecond();
		return l;
	}

	/**
	 * Indicates whether this SbDate object is later than the given SbDate object.
	 *
	 * @param date1
	 * @param date2
	 * @return true if date is after
	 */
	public boolean after(SbDate date1, SbDate date2) {
		long beg = toSeconds(date1);
		long end = toSeconds(date2);
		return (beg > end);
	}

	/**
	 * Indicates whether this SbDate object is earlier than the given SbDate object.
	 *
	 * @param date1
	 * @param date2
	 * @return true if date is before
	 */
	public boolean before(SbDate date1, SbDate date2) {
		long beg = toSeconds(date1);
		long end = toSeconds(date2);
		return (beg < end);
	}

	/**
	 * Check if date equals the param date
	 *
	 * @param date1
	 * @param date2
	 * @return true if dates are equals
	 */
	public boolean equals(SbDate date1, SbDate date2) {
		long beg = toSeconds(date1);
		long end = toSeconds(date2);
		return (beg == end);
	}

	/**
	 * Compares this SbDate object to the given SbDate object.
	 *
	 * @param date1
	 * @param date2
	 *
	 * @return int value -1 if date is less than the given SbDate 0 if dates are equals 1 if date is
	 * greater than the given SbDate
	 */
	public int compareTo(SbDate date1, SbDate date2) {
		long beg = toSeconds(date1);
		long end = toSeconds(date2);
		if (beg == end) {
			return 0;
		} else if (beg < end) {
			return -1;
		}
		return 1;
	}

	/**
	 * Return number of days from/to the given SbDate object
	 *
	 * @param date1
	 * @param date2
	 *
	 * @return a long value (maybe negative)
	 */
	public long diffDate(SbDate date1, SbDate date2) {
		return (toDays(date2) - toDays(date1));
	}

	/**
	 * Return number of seconds from/to the given SbDate object
	 *
	 * @param date1
	 * @param date2
	 *
	 * @return a long value (maybe negative)
	 */
	public long diffTime(SbDate date1, SbDate date2) {
		return (toSeconds(date2) - toSeconds(date1));
	}

	/**
	 * Return the number of days from/to the given SbDate object
	 *
	 * @param date1
	 * @param date2
	 *
	 * @return an int value (maybe negative)
	 */
	public int diffDays(SbDate date1, SbDate date2) {
		return (int) (toDays(date2) - toDays(date1));
	}

	public String DateToString(DATETIME date) {
		return (date.toString());
	}

	public String DateToString(DATETIME date, boolean isLongName) {
		StringBuilder rc = new StringBuilder();
		String d = date.getDate();
		String[] x = d.split("/");
		rc.append(getDayOfWeek(date, isLongName));
		rc.append(" ");
		rc.append(getMonthName(date, isLongName));
		rc.append(" ");
		rc.append(x[2]).append(" ").append(date.getTime());
		return (rc.toString());
	}

	public long DateToLong(DATETIME date) {
		if (date == null) {
			return (-1L);
		}
		long t = (date.Year * yeardays);
		for (int i = 0; i < date.Month; i++) {
			t += months.get(i).days;
		}
		t += date.Day;
		t = ((t - 1) * hours) + date.Hour;
		t = (t * minutes) + date.Minute;
		t = (t * seconds) + date.Second;
		return (t);
	}

	public long dateDiff(DATETIME date1, DATETIME date2) {
		long d1 = DateToLong(date1);
		long d2 = DateToLong(date2);
		return (d2 - d1);
	}

	public String getListMonths() {
		List<String> list = new ArrayList<>();
		for (MONTH d : months) {
			list.add(d.name + "," + d.abbr + "," + d.days);
		}
		return (ListUtil.join(list, ";"));
	}

	public String getListDays() {
		List<String> list = new ArrayList<>();
		for (DAY d : days) {
			list.add(d.getName() + "," + d.getAbbr());
		}
		return (ListUtil.join(list, ";"));
	}

	public String getListHours() {
		return (String.format("%d:%d:%d", hours, minutes, seconds));
	}

	public void setStartday(int value) {
		startday = value;
	}

	public int getStartday() {
		return (startday);
	}

	public class DATETIME {

		int Year, Month, Day, Hour, Minute, Second;

		public DATETIME(int year, int month, int day, int hour, int minute, int seconde) {
			Year = year;
			Month = month;
			Day = day;
			Hour = hour;
			Minute = minute;
			Second = seconde;
		}

		public DATETIME(int year, int month, int day) {
			Year = year;
			Month = month;
			Day = day;
			Hour = 0;
			Minute = 0;
			Second = 0;
		}

		@Override
		public String toString() {
			String rc = String.format("'%2d/%2d/%d %2d:%2d:%2d", Day, Month, Year, Hour, Minute, Second);
			return (rc);
		}

		public void setDate(int year, int month, int day) {
			Year = year;
			Month = month;
			Day = day;
		}

		public String getDate() {
			return (Day + "/" + Month + "/" + Year);
		}

		public void setTime(int hour, int minute, int seconde) {
			Hour = hour;
			Minute = minute;
			Second = seconde;
		}

		public String getTime() {
			return (Hour + ":" + Minute + ":" + Second);
		}
	}

	public static class MONTH {

		public String name;
		public String abbr;
		public int days;

		public MONTH() {
		}

		private MONTH(String x) {
			String y[] = x.split(",");
			name = y[0];
			abbr = y[1];
			days = Integer.parseInt(y[2]);
		}
	}

	public static class DAY {

		private String name;
		private String abbr;

		public DAY(String x) {
			String y[] = x.split(",");
			name = y[0];
			abbr = y[1];
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAbbr() {
			return abbr;
		}

		public void setAbbr(String abbr) {
			this.abbr = abbr;
		}

	}

}
