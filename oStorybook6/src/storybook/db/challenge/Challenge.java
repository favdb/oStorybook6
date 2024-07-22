/*
 * Copyright (C) 2024 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.db.challenge;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import storybook.project.Project;
import storybook.tools.DateUtil;
import storybook.tools.ListUtil;
import storybook.tools.StringUtil;
import storybook.tools.xml.XmlKey.XK;
import storybook.tools.xml.XmlUtil;

/**
 * Challenge, inspired by Philippe <br>
 * in https://www.jeunesecrivains.com/t63749-outils-logiciels-tableau-de-bord-d-avancement-nanowrimo
 *
 * @author favdb
 */
public class Challenge {

	private static final String TT = "Challenge.";
	public static final int NBVALUES = 60;//maximum number of days

	/**
	 * datas of the challenge:
	 * <ul>
	 * <li>beginDate : begining challenge date</li>
	 * <li>days : number of days for the challenge</li>
	 * <li>words : number of word</li>s
	 * <li>beginWords : begining number of words</li>
	 * <li>values : list of values</li>
	 * </ul>
	 *
	 */
	private Integer days = 0, words = 0, beginWords = 0;
	private final Integer[] values = new Integer[NBVALUES];
	private Timestamp beginDate;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Challenge() {
		init();
	}

	/**
	 * reset the initial date
	 */
	public void beginDateReset() {
		beginDate = null;
	}

	/**
	 * set the inital date
	 */
	public void beginDateSet() {
		beginDate = new Timestamp(DateUtil.getZeroTimeDate().getTime());
	}

	/**
	 * get number of days from initDate to today
	 *
	 * @return -1 if initDate is null
	 */
	public Timestamp beginDateGet() {
		return beginDate;
	}

	public Integer startDateLeft() {
		if (beginDate == null) {
			return 0;
		}
		Long range = ChronoUnit.DAYS.between(beginDate.toInstant(), Instant.now());
		return range.intValue();
	}

	/**
	 * check if initial date exists and number of days if number of days is not zero
	 *
	 * @return
	 */
	public boolean beginDateHas() {
		return (beginDate != null && days > 0);
	}

	/**
	 * set the number of days
	 *
	 * @param days
	 */
	public void nbDaysSet(int days) {
		this.days = days;
	}

	/**
	 * get the days number
	 *
	 * @return
	 */
	public Integer nbDaysGet() {
		return this.days;
	}

	/**
	 * set the initial number of words
	 *
	 * @param nb
	 */
	public void beginWordsSet(int nb) {
		this.beginWords = nb;
	}

	/**
	 * get the initial number of words
	 *
	 * @return
	 */
	public Integer beginWordsGet() {
		return this.beginWords;
	}

	/**
	 * set the last number of words
	 *
	 * @param nb
	 */
	public void lastWordsSet(int nb) {
		if (isActive()) {
			int d = DateUtil.daysFrom(beginDate);
			if (d > -1 && d < days) {
				values[d] = nb;
			}
		}
	}

	/**
	 * set the objective number of words
	 *
	 * @param len
	 */
	public void nbWordsSet(Integer len) {
		words = len;
	}

	/**
	 * get the objective number of words
	 *
	 * @return
	 */
	public Integer nbWordsGet() {
		if (words == null) {
			return 0;
		}
		return words;
	}

	/**
	 * get the number of words of the given day
	 *
	 * @param day
	 * @return
	 */
	public Integer wordsFor(int day) {
		if (day < values.length) {
			if (day == 0) {
				return values[day] - beginWords;
			}
			return Math.max(0, values[day] - values[day - 1]);
		}
		return 0;
	}

	/**
	 * get the list of values
	 *
	 * @return
	 */
	public Integer[] valuesGet() {
		return values;
	}

	/**
	 * get the nth value
	 *
	 * @param n: number of the day to get
	 *
	 * @return
	 */
	public Integer valuesGet(int n) {
		return values[n];
	}

	/**
	 * summarize the values
	 *
	 * @return
	 */
	public Integer valuesSum() {
		int t = values[0];
		for (int i = 1; i < days; i++) {
			t += values[i];
		}
		return t - beginWords;
	}

	/**
	 * get the min value of the values
	 *
	 * @return
	 */
	public Integer valuesMin() {
		if (days == 0) {
			return 0;
		}
		int t = Integer.MAX_VALUE;
		for (int i = 0; i < days; i++) {
			if (values[i] < t) {
				t = values[i];
			}
		}
		return t;
	}

	/**
	 * get the max value of the list
	 *
	 * @return
	 */
	public Integer valuesMax() {
		if (days == 0) {
			return 0;
		}
		int t = 0;
		for (int i = 0; i < days; i++) {
			if (values[i] > t) {
				t = values[i];
			}
		}
		return t - beginWords;
	}

	/**
	 * get the average value of the values
	 *
	 * @return if list is empty then return -1
	 */
	public Integer valuesAverage() {
		if (valuesSum() > 0) {
			return valuesSum() / days;
		}
		return 0;
	}

	/**
	 * initialize the values
	 */
	public void valuesInit() {
		for (int n = 0; n < NBVALUES; n++) {
			values[n] = 0;
		}
	}

	/**
	 * load the values from a comma separated String
	 *
	 * @param str
	 */
	public void valuesLoad(String str) {
		valuesInit();
		String sx[] = str.split(",");
		int n = 0;
		for (String s : sx) {
			try {
				if (StringUtil.isNumeric(s.trim())) {
					values[n] = Integer.valueOf(s.trim());
				} else {
					values[n] = 0;
				}
			} catch (NumberFormatException ex) {
			}
			n++;
		}
	}

	/**
	 * get the values as a comma separated String
	 *
	 * @return
	 */
	public String valuesToString() {
		List<String> b = new ArrayList<>();
		for (Integer v : values) {
			b.add(v.toString());
		}
		return ListUtil.join(b, ",");
	}

	/**
	 * load challenge from a XML String
	 *
	 * @param project
	 * @param rootNode
	 */
	public void fromXml(Project project, Node rootNode) {
		NodeList nodes = ((Element) rootNode).getElementsByTagName("challenge");
		if (nodes.getLength() > 0) {
			Node node = nodes.item(0);
			days = XmlUtil.getInteger(node, XK.DAYS);
			words = XmlUtil.getInteger(node, XK.WORDS);
			if (days > 0) {
				beginDate = XmlUtil.getTimestamp(node, XK.INITDATE);
				beginWords = XmlUtil.getInteger(node, XK.INITWORDS);
				valuesLoad(XmlUtil.getString(node, XK.VALUES));
			}
		}
	}

	/**
	 * get the XML String of the challenge
	 *
	 * @return
	 */
	public String toXml() {
		StringBuilder s = new StringBuilder();
		s.append("<challenge ");
		s.append(XmlUtil.setAttribute(0, XK.DAYS, days));
		s.append(XmlUtil.setAttribute(0, XK.WORDS, words));
		if (days > 0) {
			s.append(XmlUtil.setAttribute(2, XK.INITDATE, beginDate, true));
			s.append(XmlUtil.setAttribute(0, XK.INITWORDS, beginWords));
			s.append(XmlUtil.setAttribute(2, XK.VALUES, valuesToString())).append("\n");
		}
		s.append(" />\n");
		return s.toString();
	}

	/**
	 * get the days left
	 *
	 * @return
	 */
	public Integer daysGetLeft() {
		try {
			Timestamp today = new Timestamp(System.currentTimeMillis());
			return DateUtil.daysBetween(today, dateEndGet());
		} catch (Exception ex) {
			return 0;
		}
	}

	public Timestamp dateEndGet() {
		return DateUtil.addNbDays(beginDate, days);
	}

	public int wordsAverageGet() {
		return (words == 0 || days == 0 ? 0 : words / this.days);
	}

	public boolean isActive() {
		return days != 0 && words != 0;
	}

	/**
	 * initialize all data
	 */
	public void init() {
		days = 0;
		words = 0;
		beginDate = null;
		beginWords = 0;
		valuesInit();
	}

}
