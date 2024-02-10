/*
 * Copyright (C) 2017 FaVdB
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
package storybook.db;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author FaVdB
 */
public class SbDate {
	//9.223.372.036.854.775.807

	private int year, month, day, hour, minute, second;

	public SbDate() {
		setDate(0, 0, 0);
		setTime(0, 0, 0);
	}

	public SbDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH);
		day = cal.get(Calendar.DAY_OF_MONTH);
		hour = cal.get(Calendar.HOUR);
		minute = cal.get(Calendar.MINUTE);
		second = cal.get(Calendar.SECOND);
	}

	public SbDate(int y, int m, int d) {
		setDate(y, m, d);
		setTime(0, 0, 0);
	}

	public SbDate(int y, int m, int d, int hh, int mm, int ss) {
		setDate(y, m, d);
		setTime(hh, mm, ss);
	}

	public SbDate(String d) {
		setDateTime(d);
	}

	public Long difMinutes(SbDate d) {
		Long l0 = this.getMinutes();
		Long l1 = d.getMinutes();
		return l1 - l0;
	}

	public void setDate(int y, int m, int d) {
		setYear(y);
		setMonth(m);
		setDay(d);
	}

	public void setDate(String str) {
		String x[] = str.split("-");
		if (x.length > 2) {
			year = Integer.parseInt(x[0]);
			month = Integer.parseInt(x[1]);
			day = Integer.parseInt(x[2]);
		}
	}

	public void setTime(int hh, int mm, int ss) {
		setHour(hh);
		setMinute(mm);
		setSecond(ss);
	}

	public void setTime(String str) {
		String x[] = str.split(":");
		hour = 0;
		minute = 0;
		second = 0;
		hour = Integer.parseInt(x[0]);
		if (x.length > 1) {
			minute = Integer.parseInt(x[1]);
		}
		if (x.length > 2) {
			second = Integer.parseInt(x[2]);
		}
	}

	public void setDateTime(String str) {
		if (str == null || str.isEmpty()) {
			return;
		}
		String x[] = str.split(" ");
		setDate(x[0]);
		setTime(x[1]);
	}

	public void setYear(int y) {
		year = y;
	}

	public void setMonth(int m) {
		if (m < 1) {
			month = 1;
		} else {
			month = m;
		}
	}

	public void setDay(int d) {
		if (d < 1) {
			day = 1;
		} else {
			day = d;
		}
	}

	public void setHour(int hh) {
		if (hh < 0) {
			hour = 0;
		} else {
			hour = hh;
		}
	}

	public void setMinute(int mm) {
		if (mm < 0) {
			minute = 0;
		} else {
			minute = mm;
		}
	}

	public void setSecond(int ss) {
		if (ss < 0) {
			second = 0;
		} else {
			second = ss;
		}
	}

	public String getDate() {
		return String.format("%04d-%02d-%02d", year, month, day);
	}

	public String getTime() {
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}

	public String getDateTimeToString() {
		return getDate() + " " + getTime();
	}

	public Date getDateTime() {
		Date d = new Date(this.toTimestamp().getTime());
		return d;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public int getDay() {
		return day;
	}

	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}

	public Long getMinutes() {
		return this.getDateTime().getTime() / 60000L;
	}

	public int getSecond() {
		return second;
	}

	public static SbDate getToDay() {
		Date today = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return (new SbDate(dateFormat.format(today)));
	}

	public Timestamp toTimestamp() {
		return Timestamp.valueOf(getDateTimeToString());
	}

}
