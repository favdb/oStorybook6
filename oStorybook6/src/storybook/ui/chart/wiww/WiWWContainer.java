/*
 * SbApp: Open Source software for novelists and authors.
 * Original idea 2008 - 2012 Martin Mustun
 * Copyrigth (C) Favdb
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
 */
package storybook.ui.chart.wiww;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import storybook.db.location.Location;
import storybook.db.person.Person;
import storybook.db.scene.Scene;
import storybook.tools.DateUtil;
import storybook.ui.MainFrame;

public class WiWWContainer {

	private static final String TT = "WiWWContainer.";

	private final MainFrame mainFrame;
	private final Location location;
	private final List<Person> inPersonList;
	private final List<Person> outPersonList;
	private final Date date;
	private boolean found;

	public WiWWContainer(MainFrame mainFrame, Date date, Location location, List<Person> persons) {
		this.mainFrame = mainFrame;
		this.location = location;
		this.inPersonList = persons;
		this.date = date;//DateUtil.getZeroTimeDate(date);
		this.outPersonList = new ArrayList<>();
		init();
	}

	private void init() {
		//LOG.trace(TT + "init() for date=" + date);
		for (Person person : inPersonList) {
			if (isPersonLocationDate(person, location, date)) {
				outPersonList.add(person);
			}
		}
		found = !outPersonList.isEmpty();
	}

	public List<Person> getCharacterList() {
		return outPersonList;
	}

	public boolean isFound() {
		return found;
	}

	@SuppressWarnings("unchecked")
	private boolean isPersonLocationDate(Person person, Location location, Date date) {
		//LOG.trace(TT + "countByPersonLocationDate(person=" + LOG.trace(person)
		//   + ", loc=" + LOG.trace(location) + ", date=" + date.toString() + ")");
		Date tsStart = DateUtil.addMilliseconds(date, -1);
		date = DateUtil.addDays(date, 1);
		date = DateUtil.addMilliseconds(date, -1);
		Date tsEnd = new Date(date.getTime());
		for (Scene s : (List<Scene>) mainFrame.project.scenes.getList()) {
			if (!s.getPersons().contains(person)
			   && !s.getLocations().contains(location)) {
				continue;
			}
			if (s.getScenets() == null) {
				continue;
			}
			if (DateUtil.between(s.getDate(), tsStart, tsEnd)) {
				return true;
			}
		}
		return false;
	}

}
