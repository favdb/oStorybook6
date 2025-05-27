/*
 * Copyright (C) 2021 favdb
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
package storybook.tools;

import i18n.I18N;
import storybook.tools.html.Html;

/**
 *
 * @author favdb
 */
public class SbDuration {

	public static boolean isZero(String duration) {
		return duration.equals("00-00-00_00:00:00");
	}

	public int years = 0, months = 0, days = 0, hours = 0, minutes = 0, seconds = 0;
	private static Double FREQ = 2.25;

	public SbDuration() {
		// an empty duration
	}

	/**
	 * a duration from a formated String like yy-MM-dd_hh:mm:ss
	 *
	 * @param duration
	 */
	public SbDuration(String duration) {
		//LOG.trace("SbDuration(\"" + duration + "\")");
		String dx = duration.replace("[", "").replace("]", "");
		if (dx.contains("_")) {
			String p[] = dx.split("_");
			//first part is date
			if (p.length > 0 && p[0].contains("-")) {
				String d[] = p[0].split("\\-");
				if (!d[0].equals("00")) {
					years = Integer.parseInt(d[0]);
				}
				if (!d[1].equals("00")) {
					months = Integer.parseInt(d[1]);
				}
				if (!d[2].equals("00")) {
					days = Integer.parseInt(d[2]);
				}
			}
			//second part is time
			if (p.length == 2 && p[1].contains(":")) {
				String d[] = p[1].split("\\:");
				if (!d[0].equals("00")) {
					hours = Integer.parseInt(d[0]);
				}
				if (!d[1].equals("00")) {
					minutes = Integer.parseInt(d[1]);
				}
				if (!d[2].equals("00")) {
					seconds = Integer.parseInt(d[2]);
				}
			}
		}
	}

	/**
	 * get a formated String like yy-MM-dd_hh:mm:ss
	 *
	 * @return the formated String
	 */
	@Override
	public String toString() {
		return String.format("%02d-%02d-%02d_%02d:%02d:%02d",
				years, months, days, hours, minutes, seconds);
	}

	/**
	 * get a formated String like YYy mmM HHh MMm SSs with default inititales
	 *
	 * @return the formated String
	 */
	public String toText() {
		return toText(I18N.getMsg("duration.initiales"));
	}

	/**
	 * get a formated String like YYy mmM HHh MMm SSs
	 *
	 * @param format : of the date to get
	 *
	 * @return the formated String
	 */
	public String toText(String format) {
		if (format == null || format.isEmpty() || format.length() != 6) {
			return "";
		}
		if (years == 0 && months == 0 && days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
			return "";
		}
		String r = "";
		if (years > 0) {
			r += String.format("%d%c ", years, format.charAt(0));
		}
		if (months > 0) {
			r += String.format("%d%c ", months, format.charAt(1));
		}
		if (days > 0) {
			r += String.format("%d%c ", days, format.charAt(2));
		}
		if (hours > 0) {
			r += String.format("%d%c ", hours, format.charAt(3));
		}
		if (minutes > 0) {
			r += String.format("%d%c ", minutes, format.charAt(4));
		}
		if (seconds > 0) {
			r += String.format("%d%c", seconds, format.charAt(5));
		}
		return r.trim();
	}

	public static SbDuration getFromString(String string) {
		return new SbDuration(string);
	}

	/**
	 * get a SbDuration from a formated String like YYy mmM DDd HHh MMm SSs any part may be ommitted
	 * if text is null or empty return an all 0 initiales with the default one
	 *
	 * @param text : contains the formated String to convert to a SbDuration
	 * @return the SbDuration
	 */
	public static SbDuration getFromText(String text) {
		if (text.contains("_") && text.contains("-") && text.contains(":")) {
			//this is a preformated duration like 00-00-00_00:00:00
			return new SbDuration(text);
		}
		return getFromText(Html.htmlToText(text), I18N.getMsg("duration.initiales"));
	}

	/**
	 * get a SbDuration from a formated String like YYy mmM DDd HHh MMm SSs<br>
	 * any part may be ommitted<br>
	 * if text is null or empty return an all 0 SbDuration<br>
	 * if initiales is null or empty use english initiales
	 *
	 * @param text : contains the formated String to convert to a SbDuration
	 * @param initiales: contains the initiales used
	 *
	 * @return the SbDuration
	 */
	public static SbDuration getFromText(String text, String initiales) {
		//LOG.trace("SbDuration.getFromText(text="+text+", initiales=\""+initiales+"\"");
		SbDuration dur = new SbDuration();
		if (text == null || text.isEmpty()) {
			return dur;
		}
		String fmt = "yMdhms";//default format for english
		if (initiales != null && !initiales.startsWith("!") && initiales.length() == 6) {
			fmt = initiales;
		}
		String r0[] = text.split(" ");
		for (String r1 : r0) {
			if (r1 == null || r1.isEmpty()) {
				continue;
			}
			char c = r1.charAt(r1.length() - 1);
			int v = Integer.parseInt(r1.substring(0, r1.length() - 1));
			if (c == fmt.charAt(0)) {
				dur.years = v;
			} else if (c == fmt.charAt(1)) {
				dur.months = v;
			} else if (c == fmt.charAt(2)) {
				dur.days = v;
			} else if (c == fmt.charAt(3)) {
				dur.hours = v;
			} else if (c == fmt.charAt(4)) {
				dur.minutes = v;
			} else if (c == fmt.charAt(5)) {
				dur.seconds = v;
			}
		}
		return dur;
	}

	/**
	 * check if a String has correct duration format like YYy mmM DDd HHh MMm SSs
	 *
	 * @param text: the String to check
	 * @param initiales : for year, month, day, hour, minute and second
	 *
	 * @return true if correct
	 */
	public static String check(String text, String initiales) {
		String rc = "";
		if (text == null || text.isEmpty()) {
			return "";//text is empty so there is no error
		}
		if (initiales.startsWith("!")) {
			return "missing durations initiales, unable to check";
		}
		String r0[] = text.split(" ");
		for (String r1 : r0) {
			if (r1 == null || r1.isEmpty()) {
				continue;
			}
			char c = r1.charAt(r1.length() - 1);
			if (!initiales.contains(c + "")) {
				rc = I18N.getMsg("verifier.wrong.duration.structure");
				break;
			}
		}
		return rc;
	}

	/**
	 * get a SbDuration from words by 2.25 word by second source :
	 * https://www.cairn.info/revue-francaise-de-linguistique-appliquee-2015-2-page-63.htm
	 *
	 * @param words
	 *
	 * @return
	 */
	public static SbDuration getFromWords(String words) {
		Double l = (TextUtil.countWords(words) / FREQ);
		//transformation du nombre de secondes en durées aMjhms
		int li = l.intValue();
		int annees = 0, months = 0;
		int jours = li / 86400;
		int heures = (li % 86400) / 3600;
		int minutes = (li % 3600) / 60;
		int secondes = (li % 60);
		SbDuration d = new SbDuration();
		d.years = annees;
		d.months = 0;
		d.days = jours;
		d.hours = heures;
		d.minutes = minutes;
		d.seconds = secondes;
		return d;
	}

	/**
	 * get a String duration from words by 2.25 words by second source :
	 * https://www.cairn.info/revue-francaise-de-linguistique-appliquee-2015-2-page-63.htm
	 *
	 * @param words
	 *
	 * @return
	 */
	public static String computeFromWords(String words) {
		//LOG.trace("SbDuration.computeFromWords(words len="+words.length()+")");
		Double l = (TextUtil.countWords(words) / FREQ);
		//transformation du nombre de secondes en durées aMjhms
		int li = l.intValue();
		int annees = 0, months = 0, jours = 0;
		int heures = li / 3600;
		int minutes = (li - (heures * 3600)) / 60;
		int secondes = li % 60;
		String str = String.format("%02d-%02d-%02d_%02d:%02d:%02d",
				annees, months, jours, heures, minutes, secondes);
		return str;
	}

	/**
	 * add a duration formatted like YYy mmM DDd HHh MMm SSs
	 *
	 * @param d the duration to add
	 */
	public void add(SbDuration d) {
		//LOG.trace("SbDuration.add("+d.toString()+")");
		this.years += d.years;
		this.months += d.months;
		this.days += d.days;
		this.hours += d.hours;
		this.minutes += d.minutes;
		this.seconds += d.seconds;
	}

	/**
	 * convert duration to minutes
	 *
	 * @return
	 */
	public int toMinutes() {
		int s = toSeconds();
		return s / 60;
	}

	/**
	 * convert duretion to seconds
	 *
	 * @return
	 */
	public int toSeconds() {
		int s = this.seconds;
		s += minutesToSeconds(minutes);
		s += hoursToSeconds(hours);
		s += daysToSeconds(days);
		s += monthsToSeconds(months);
		s += yearsToSeconds(years);
		return s;
	}

	/**
	 * convert only the minutes in seconds
	 *
	 * @param m
	 * @return
	 */
	private static int minutesToSeconds(int m) {
		return (m * 60);
	}

	/**
	 * convert the hour in seconds
	 *
	 * @param h
	 * @return
	 */
	private static int hoursToSeconds(int h) {
		return (minutesToSeconds(60)) * h;
	}

	/**
	 * convert the day in seconds
	 *
	 * @param h
	 * @return
	 */
	private static int daysToSeconds(int d) {
		return (hoursToSeconds(24)) * d;
	}

	/**
	 * convert the month in seconds
	 *
	 * @param h
	 * @return
	 */
	private static int monthsToSeconds(int d) {
		return (daysToSeconds(30)) * d;
	}

	/**
	 * convert the year in seconds
	 *
	 * @param h
	 * @return
	 */
	private static int yearsToSeconds(int d) {
		return (daysToSeconds(365)) * d;
	}

	/**
	 * get I18N format for tooltips text
	 *
	 * @param initiales
	 * @return
	 */
	public static String getFormat(String initiales) {
		StringBuilder list = new StringBuilder();
		for (char c : initiales.toCharArray()) {
			list.append(String.format("99%c ", c));
		}
		return list.toString();
	}

	/**
	 * set the duration from a number of minutes
	 *
	 * @param value
	 */
	public void setMinutes(Integer value) {
		minutes = value;
		if (value > 60) {
			hours = value / 60;
			minutes = value % 60;
		}
		if (hours > 24) {
			days = hours / 24;
			hours = hours % 24;
		}
		if (days > 30) {
			months = days / 30;
			days = days % 30;
		}
		if (months > 12) {
			years = months / 12;
			months = months % 12;
		}
	}

}
