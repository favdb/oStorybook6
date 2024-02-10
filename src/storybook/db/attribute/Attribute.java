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
package storybook.db.attribute;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.w3c.dom.Node;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.DB.DATA;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.tools.xml.XmlKey;
import storybook.tools.xml.XmlKey.XK;
import storybook.tools.xml.XmlUtil;

public class Attribute extends AbstractEntity {

	private static final String TT = "Attribute";

	private String key = "";
	private String value = "";

	public Attribute() {
		super(Book.TYPE.ATTRIBUTE, "110");
	}

	public Attribute(ResultSet rs) {
		super(Book.TYPE.ATTRIBUTE, "110", rs);
		try {
			this.key = rs.getString("key");
			this.value = rs.getString("value");
		} catch (SQLException ex) {
			//empty
		}
	}

	public Attribute(String key, String value) {
		this();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public Icon getIcon() {
		return (IconUtil.getIconSmall(ICONS.K.UNKNOWN));
	}

	@Override
	public String toCsv(String quoteStart, String quoteEnd, String separator) {
		StringBuilder b = new StringBuilder();
		b.append(quoteStart).append(getClean(this)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(key)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(value)).append(quoteEnd).append("\n");
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

	public static Attribute fromXml(Node node) {
		Attribute p = new Attribute();
		fromXmlBeg(node, p);
		p.setKey(XmlUtil.getString(node, XmlKey.XK.KEY));
		p.setValue(XmlUtil.getString(node, XmlKey.XK.VALUE));
		fromXmlEnd(node, p);
		return p;
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		b.append(toXmlBeg());
		b.append(XmlUtil.setAttribute(0, XK.KEY, key));
		b.append(XmlUtil.setAttribute(0, XK.VALUE, value));
		b.append(toXmlFooter());
		return b.toString();
	}

	@Override
	public String toDetail(Integer detailed) {
		StringBuilder b = new StringBuilder();
		b.append(toDetailHeader(detailed));
		b.append(getInfo(2, DATA.ATTRIBUTE_KEY, key));
		b.append(getInfo(2, DATA.ATTRIBUTE_VALUE, value));
		b.append(toDetailFooter(detailed));
		return (b.toString());
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		Attribute test = (Attribute) obj;
		boolean ret = equalsStringNullValue(key, test.getKey());
		ret = ret && equalsStringNullValue(value, test.getValue());
		return ret;
	}

	@Override
	public int hashCode() {
		return hashPlus(super.hashCode(), key, value);
	}

	public static Attribute find(List<Attribute> list, String str) {
		for (Attribute elem : list) {
			if (elem.getName().equals(str)) {
				return (elem);
			}
		}
		return (null);
	}

	public static Attribute find(List<Attribute> list, Long id) {
		for (Attribute elem : list) {
			if (elem.getId().equals(id)) {
				return (elem);
			}
		}
		return (null);
	}

	public static List<String> getTable() {
		List<String> ls = new ArrayList<>();
		String tableName = DATA.ATTRIBUTE.n();
		AbstractEntity.getTable(tableName, ls);
		ls.add(tableName + ",key,String,256");
		ls.add(tableName + ",value,String,2048");
		return (ls);
	}

}
