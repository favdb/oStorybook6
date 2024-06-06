/*
 * Copyright (C) 2018 FaVdB
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
package storybook.db.plot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import storybook.db.DB.DATA;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.tools.ListUtil;
import storybook.tools.xml.XmlKey.XK;
import storybook.tools.xml.XmlUtil;
import storybook.ui.MainFrame;

/**
 *
 * @author FaVdB
 */
public class Plot extends AbstractEntity {

	private String category;

	public Plot() {
		super(Book.TYPE.PLOT, "111");
	}

	public Plot(ResultSet rs) {
		super(Book.TYPE.PLOT, "111", rs);
		try {
			category = rs.getString("category");
		} catch (SQLException ex) {
			//empty
		}
	}

	public Plot(String name, String cat, String desc, String not) {
		this();
		setName(name);
		category = cat;
		setDescription(desc);
		setNotes(not);
	}

	public void setCategory(String cat) {
		this.category = cat;
	}

	public String getCategory() {
		return (category);
	}

	@Override
	public String toString() {
		return (isTransient() ? "" : getName() + (hasNotes() ? "*" : ""));
	}

	@Override
	public String toCsv(String quoteStart, String quoteEnd, String separator) {
		StringBuilder b = new StringBuilder();
		b.append(quoteStart).append(getClean(this)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getName())).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getCategory())).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getDescription())).append(quoteStart).append("\n");
		b.append(quoteStart).append(getClean(getNotes())).append(quoteStart).append("\n");
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
		b.append(super.toDetailHeader(detailed));
		b.append(getInfo(detailed, DATA.CATEGORY, getCategory()));
		if (detailed == 0) {
			b.append(getInfo(2, L_DESCRIPTION, getDescription()));
		} else {
			b.append(super.toDetailFooter(detailed));
		}
		return b.toString();
	}

	public static Plot fromXml(Node node) {
		Plot p = new Plot();
		fromXmlBeg(node, p);
		p.setCategory(XmlUtil.getString(node, XK.CATEGORY));
		fromXmlEnd(node, p);
		return p;
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder(toXmlBeg());
		b.append(XmlUtil.setAttribute(0, XK.CATEGORY, getCategory()));
		b.append(">\n");
		b.append(toXmlEnd());
		return b.toString();
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		Plot test = new Plot();
		boolean ret = true;
		ret = ret && equalsStringNullValue(getName(), test.getName());
		ret = ret && equalsStringNullValue(category, test.getCategory());
		ret = ret && equalsStringNullValue(getDescription(), test.getDescription());
		ret = ret && equalsStringNullValue(getNotes(), test.getNotes());
		return ret;
	}

	@Override
	public int hashCode() {
		return hashPlus(super.hashCode(), category);
	}

	@Override
	public int compareTo(AbstractEntity en) {
		Plot o = (Plot) en;
		if (category == null && o == null) {
			return 0;
		}
		if (category != null && o.getCategory() == null) {
			return -1;
		}
		if (o.getCategory() != null && category == null) {
			return -1;
		}
		int cmp = category.compareTo(o.getCategory());
		if (cmp == 0) {
			return getName().compareTo(o.getName());
		}
		return cmp;
	}

	public static Plot find(List<Plot> list, String str) {
		for (Plot elem : list) {
			if (elem.getName().equals(str)) {
				return (elem);
			}
		}
		return (null);
	}

	public static Plot find(List<Plot> list, Long id) {
		for (Plot elem : list) {
			if (elem.id.equals(id)) {
				return (elem);
			}
		}
		return (null);
	}

	public static List<String> findCategories(MainFrame mainFrame) {
		@SuppressWarnings("unchecked")
		List<Plot> events = (List) mainFrame.project.getList(Book.TYPE.PLOT);
		List<String> list = new ArrayList<>();
		events.forEach((e) -> {
			list.add(e.getCategory());
		});
		ListUtil.setUnique(list);
		return (list);
	}

	public static List<String> getDefColumns() {
		List<String> list = AbstractEntity.getDefColumns(Book.TYPE.PLOT);
		list.add("category, 255");
		return (list);
	}

	public static List<String> getTable() {
		List<String> ls = new ArrayList<>();
		String tableName = "plot";
		AbstractEntity.getTable(tableName, ls);
		ls.add(tableName + ",category,String,255");
		return (ls);
	}

	@Override
	public AbstractEntity copyTo(MainFrame m) {
		Plot ne = new Plot();
		doCopyTo(m, ne);
		ne.setCategory(getCategory());
		return ne;
	}

}
