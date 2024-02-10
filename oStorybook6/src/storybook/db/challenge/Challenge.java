/*
 * Copyright (C) 2023 favdb
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
package storybook.db.challenge;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import storybook.tools.DateUtil;
import storybook.tools.ListUtil;
import storybook.tools.StringUtil;
import storybook.tools.xml.XmlUtil;

/**
 *
 * @author favdb
 */
public class Challenge {

	Timestamp initDate, lastDate;
	Integer days = 0, initWords = 0, lastWords = 0, nbWords = 0;
	List<Integer> list = new ArrayList<>();

	public Challenge() {

	}

	public void initDateSet() {
		initDate = new Timestamp(DateUtil.getZeroTimeDate().getTime());
	}

	public Timestamp initDateGet() {
		return initDate;
	}

	public boolean initDateHas() {
		return (initDate != null && days > 0);
	}

	public void daysSet(int days) {
		this.days = days;
	}

	public Integer daysGet() {
		return this.days;
	}

	public void initWordsSet(int nbchars) {
		this.initWords = nbchars;
	}

	public Integer initWordsGet() {
		return this.initWords;
	}

	public void lastWordsSet(int nbchars) {
		this.lastWords = nbchars;
	}

	public Integer lastWordsGet() {
		return this.lastWords;
	}

	public void nbWordsSet(Integer len) {
		this.nbWords = len;
	}

	public Integer nbWordsGet() {
		return this.nbWords;
	}

	public void lastDateSet() {
		lastDate = new Timestamp(DateUtil.getZeroTimeDate().getTime());
	}

	public Timestamp lastDateGet() {
		return lastDate;
	}

	public List<Integer> listGet() {
		return list;
	}

	public void listLoad(String str) {
		list.clear();
		String sx[] = str.split(",");
		for (String s : sx) {
			if (StringUtil.isNumeric(s)) {
				list.add(Integer.valueOf(s));
			} else {
				list.add(0);
			}
		}
	}

	public String listSave() {
		List<String> b = new ArrayList<>();
		for (Integer p : list) {
			b.add(p.toString());
		}
		return ListUtil.join(b, ",");
	}

	public void fromXml() {

	}

	public String toXml() {
		StringBuilder s = new StringBuilder();
		s.append("<challenge ");
		s.append(XmlUtil.setAttribute(0, "initDate", initDate.toString()));
		s.append(XmlUtil.setAttribute(2, "days", days.toString()));
		s.append(XmlUtil.setAttribute(0, "nbWords", nbWords.toString()));
		s.append(XmlUtil.setAttribute(0, "initWords", initWords.toString()));
		s.append(XmlUtil.setAttribute(2, "lastDate", lastDate.toString()));
		s.append(XmlUtil.setAttribute(0, "lastWords", initDate.toString()));
		s.append(XmlUtil.setAttribute(2, "list", listSave()));
		s.append(" />");
		return s.toString();
	}

}
