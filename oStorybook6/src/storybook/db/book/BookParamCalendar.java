/*
Copyright ???? by ????
Modifications ???? by ????

This file is part of oStorybook.

    oStorybook is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License.

    oStorybook is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with oStorybook.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.book;

import storybook.tools.calendar.SbCalendar;

/**
 *
 * @author FaVdB
 */
public class BookParamCalendar extends BookParamAbstract {

	//TODO calendar
	public final static boolean CALENDAR_DEFAULT_USE = false;
	private boolean calendarUse = false;
	private SbCalendar calendar;
	private boolean use = false;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public BookParamCalendar(BookParam param) {
		super(param, "calendar");
		if (param.book.project.rootNode != null) {
			node = getNodeElement("calendar");
		}
		init();
	}

	@Override
	protected void init() {
		if (node != null) {
			//todo
		}
	}

	@Override
	public String toXml() {
		//todo
		return "";
	}

	public void setCalendarUse(boolean x) {
		calendarUse = x;
	}

	public boolean getCalendarUse() {
		return (calendarUse);
	}

	public boolean isUseCalendar() {
		return (calendarUse);
	}

	public SbCalendar getCalendar() {
		return calendar;
	}

	public void setCalendar(SbCalendar sbc) {
		calendar = sbc;
		//todo
	}

	@Override
	public int hash() {
		return toString().hashCode();
	}

	@Override
	public void refresh() {
	}

	@Override
	public String toString() {
		return calendar.toString();
	}

	public void setUse(boolean value) {
		this.use = value;
	}

	public boolean getUse() {
		return this.use;
	}

	void setYeardays(int yeardays) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	void setHours(String listHours) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	void setDays(String listDays) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	void setMonths(String listMonths) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	void setStartDay(int startday) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

}
