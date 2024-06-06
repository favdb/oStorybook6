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
package storybook.db.event;

import java.awt.Color;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.w3c.dom.Node;
import storybook.db.DB.DATA;
import storybook.db.abs.AbstractEntity;
import static storybook.db.abs.AbstractEntity.*;
import storybook.db.book.Book;
import storybook.tools.SbDuration;
import storybook.tools.TextUtil;
import storybook.tools.swing.ColorIcon;
import storybook.tools.swing.ColorUtil;
import storybook.tools.xml.XmlKey.XK;
import storybook.tools.xml.XmlUtil;
import storybook.ui.MainFrame;

public class Event extends AbstractEntity {

	public enum STATUS {//for TimeStep
		MINUTE,
		HOUR,
		DAY,
		MONTH,
		YEAR
	};

	private String title = "";
	private Timestamp eventTime;
	private String duration = "";
	private Integer timeStep = 0;
	private String category = "";
	private Integer color = 0;

	public Event() {
		super(Book.TYPE.EVENT, "010");
		eventTime = Timestamp.from(Instant.now());
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Event(ResultSet rs) {
		this();
		try {
			title = getName();
			eventTime = rs.getTimestamp("eventTime");
			duration = rs.getString("duration");
			timeStep = rs.getInt("timeStep");
			category = rs.getString("category");
			color = rs.getInt("color");
		} catch (SQLException ex) {
			//empty
		}
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Event(String title, String notes, Timestamp eventTime, Integer timeStep, String category) {
		this();
		setName(title);
		this.title = title;
		setNotes(notes);
		this.eventTime = eventTime;
		this.timeStep = timeStep;
		this.category = category;
	}

	@Override
	public String getName() {
		if (super.getName().isEmpty()) {
			setName(title);
		}
		return super.getName();
	}

	@Override
	public void setName(String str) {
		super.setName(str);
		title = str;
	}

	public String getTitle() {
		return this.title == null ? "" : this.title;
	}

	public String getTitle(boolean truncate) {
		return title == null ? "" : TextUtil.ellipsize(title, 30);
	}

	public void setTitle(String title) {
		this.title = title;
		setName(title);
	}

	@Override
	public boolean hasDate() {
		return hasEventTime();
	}

	public boolean hasEventTime() {
		return eventTime != null;
	}

	public void setEventTime(Timestamp ts) {
		eventTime = ts;
	}

	public Date getDate() {
		if (eventTime == null) {
			return null;
		}
		return new Date(eventTime.getTime());
	}

	public Timestamp getEventTime() {
		return (hasEventTime() ? eventTime : new Timestamp(0));
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getDuration() {
		return this.duration;
	}

	public String getDurationToText() {
		if (!getDuration().isEmpty()) {
			SbDuration du = new SbDuration(duration);
			return du.toText();
		}
		return "";
	}

	public void setTimeStep(Integer num) {
		this.timeStep = num;
	}

	public Integer getTimeStep() {
		return this.timeStep;
	}

	public String getTimestepToText() {
		String str = "";
		if (timeStep > 0) {
			SbDuration dur = new SbDuration();
			dur.setMinutes(timeStep);
			str = dur.toText();
		}
		return str;
	}

	public void setTimeStepState(EventStatus state) {
		this.timeStep = state.getNumber();
	}

	public EventStatus getTimeStepState() {
		EventStatusModel model = new EventStatusModel();
		return (EventStatus) model.findByNumber(this.timeStep);
	}

	public String getCategory() {
		if (category == null) {
			return ("");
		}
		return category;
	}

	public void setCategory(String category) {
		this.category = (category == null ? "" : category);
	}

	public Integer getColor() {
		return (color == null ? 0 : color);
	}

	public Color getJColor() {
		if (color == null) {
			return null;
		}
		return new Color(color);
	}

	public String getHTMLColor() {
		return "<span style=\""
				+ "background-color:" + ColorUtil.getHTML(getJColor())
				+ ";"
				+ "color:" + ColorUtil.getHTML(getJColor())
				+ ";\">"
				+ "&#x25ae;&#x25ae;"
				+ "</span> "
				+ ColorUtil.getHTML(getJColor());
	}

	public ColorIcon getColorIcon() {
		return new ColorIcon(getJColor(), new Dimension(10, 20));
	}

	public void setColor(Integer color) {
		this.color = color;
	}

	public void setJColor(Color color) {
		if (color == null) {
			this.color = null;
			return;
		}
		this.color = color.getRGB();
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		Event test = (Event) obj;
		boolean ret = true;
		ret = ret && equalsStringNullValue(getName(), test.getTitle());
		ret = ret && equalsStringNullValue(getNotes(), test.getNotes());
		ret = ret && equalsObjectNullValue(eventTime, test.getEventTime());
		ret = ret && equalsObjectNullValue(duration, test.getDuration());
		ret = ret && equalsIntegerNullValue(color, test.getColor());
		return ret;
	}

	@Override
	public int hashCode() {
		return hashPlus(super.hashCode(),
				category,
				color,
				duration,
				eventTime,
				timeStep);
	}

	@Override
	public int compareTo(AbstractEntity ch) {
		if (eventTime == null) {
			return getName().compareTo(ch.getName());
		} else {
			return eventTime.compareTo(((Event) ch).eventTime);
		}
	}

	public String getStepFormat() {
		switch (getTimeStep()) {
			case 0:
				return "yyyy-MM-dd HH:mm";
			case 1:
				return "yyyy-MM-dd HH";
			case 2:
				return "yyyy-MM-dd";
			case 3:
				return "yyyy-MM";
			default:
				return "yyyy";
		}
	}

	@Override
	public String toString() {
		if (isTransient()) {
			return "";
		}
		SimpleDateFormat format = new SimpleDateFormat(getStepFormat());
		String time = format.format(getEventTime());
		return time + " - " + getTitle() + (hasNotes() ? "*" : "");
	}

	@Override
	public String toCsv(String quoteStart, String quoteEnd, String separator) {
		StringBuilder b = new StringBuilder();
		b.append(quoteStart).append(getClean(this)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getName())).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(eventTime)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(duration)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(timeStep)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(category)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(color)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getNotes())).append(quoteEnd).append(separator);
		b.append("\n");
		return (b.toString());
	}

	@Override
	public String toHtml() {
		return (toCsv("<td>", "</td>", "\n"));
	}

	@Override
	public String toText() {
		return (toCsv("", "", "\t"));
	}

	@Override
	public String toDetail(Integer detailed) {
		StringBuilder b = new StringBuilder();
		b.append(toDetailHeader(detailed));
		b.append(getInfo(detailed, DATA.DATE, getEventTime()));
		if (!getDuration().isEmpty() || detailed > 1) {
			b.append(getInfo(detailed, DATA.DURATION, getDurationToText()));
		}
		if (getTimeStep() != 0 || detailed > 1) {
			b.append(getInfo(detailed, DATA.EVENT_TIMESTEP, getTimestepToText()));
		}
		if (!getCategory().isEmpty() || detailed > 1) {
			b.append(getInfo(detailed, DATA.CATEGORY, getCategory()));
		}
		b.append(getInfo(detailed, DATA.COLOR, getHTMLColor()));
		b.append(toDetailFooter(detailed));
		return (b.toString());
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder(toXmlBeg());
		b.append(XmlUtil.setAttribute(0, XK.TIME, getEventTime()));
		b.append(XmlUtil.setAttribute(0, XK.DURATION, getDuration()));
		b.append(XmlUtil.setAttribute(0, XK.STEP, getTimeStep()));
		b.append(XmlUtil.setAttribute(0, XK.CATEGORY, this.getCategory()));
		b.append(XmlUtil.setAttribute(0, XK.COLOR, getColor()));
		b.append(">\n");
		b.append(toXmlEnd());
		return XmlUtil.beautify(b.toString());
	}

	public static Event fromXml(Node node) {
		Event p = new Event();
		fromXmlBeg(node, p);
		p.setEventTime(XmlUtil.getTimestamp(node, XK.TIME));
		p.setDuration(XmlUtil.getString(node, XK.DURATION));
		p.setTimeStep(XmlUtil.getInteger(node, XK.STEP));
		p.setCategory(XmlUtil.getString(node, XK.CATEGORY));
		p.setColor(XmlUtil.getInteger(node, XK.COLOR));
		fromXmlEnd(node, p);
		return p;
	}

	public static List<String> getDefColumns() {
		List<String> list = AbstractEntity.getDefColumns(Book.TYPE.EVENT);
		list.add("title, 256");
		list.add("category, 255");
		return (list);
	}

	public static List<String> getTable() {
		List<String> ls = new ArrayList<>();
		String tableName = "timeevent";
		AbstractEntity.getTable(tableName, ls);
		ls.add(tableName + ",title,String,256");
		ls.add(tableName + ",event_ts,Time,0");
		ls.add(tableName + ",duration,String,255");
		ls.add(tableName + ",time_step,Integer,0");
		ls.add(tableName + ",category,String,255");
		ls.add(tableName + ",color,Integer,0");
		return (ls);
	}

	@Override
	public AbstractEntity copyTo(MainFrame m) {
		Event ne = new Event();
		doCopyTo(m, ne);
		ne.setCategory(getCategory());
		ne.setColor(getColor());
		ne.setDuration(getDuration());
		ne.setEventTime(getEventTime());
		ne.setTimeStep(getTimeStep());
		//ne.setTitle(getTitle());
		return ne;
	}

}
