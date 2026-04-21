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
package storybook.db.gender;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.w3c.dom.Node;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.DB.DATA;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.tools.StringUtil;
import storybook.tools.xml.XmlKey.XK;
import storybook.tools.xml.XmlUtil;
import storybook.ui.frames.main.MainFrame;

/**
 * Gender class
 */
public class Gender extends AbstractEntity {

	private enum K {
		GENDER,
		ICON_FILE,
		ID,
		NAME,
		CHILDHOOD,
		ADOLESCENCE,
		ADULTHOOD,
		RETIREMENT,
		ICONE,
		SORT;

		public String c() {
			return StringUtil.capitalize(name());
		}

		public String m() {
			return name().replace("_", ".").toLowerCase();
		}
	}

	private Integer childhood = 6;
	private Integer adolescence = 12;
	private Integer adulthood = 18;
	private Integer retirement = 65;
	private String icone = "";

	public Gender() {
		super(Book.TYPE.GENDER, "110");
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Gender(String name, Integer childhood, Integer adolescence,
			Integer adulthood, Integer retirement) {
		this();
		setName(name);
		setChildhood(childhood);
		setAdolescence(adolescence);
		setAdulthood(adulthood);
		setRetirement(retirement);
	}

	public Gender(String name) {
		this();
		setName(name);
	}

	public Integer getChildhood() {
		return this.childhood;
	}

	public void setChildhood(Integer childhood) {
		this.childhood = childhood;
	}

	public Integer getAdolescence() {
		return this.adolescence;
	}

	public void setAdolescence(Integer adolescence) {
		this.adolescence = adolescence;
	}

	public Integer getAdulthood() {
		return this.adulthood;
	}

	public void setAdulthood(Integer adulthood) {
		this.adulthood = adulthood;
	}

	public Integer getRetirement() {
		return this.retirement;
	}

	public void setRetirement(Integer retirement) {
		this.retirement = retirement;
	}

	public boolean isMale() {
		return id == 1;
	}

	public boolean isFemale() {
		return id == 2;
	}

	@Override
	public String getIconName() {
		if (isMale()) {
			return ("man");
		} else if (isFemale()) {
			return ("woman");
		} else if (getIcone() != null) {
			return (Book.TYPE.PERSON.toString());
		}
		return (Book.TYPE.PERSON.toString());
	}

	@Override
	public Icon getIcon() {
		return getIcon(IconUtil.getDefSize());
	}

	@Override
	public Icon getIcon(int sz) {
		if (getIcone() != null && !icone.isEmpty()) {
			File f = new File(icone);
			if (f.exists()) {
				return (IconUtil.resizeIcon(new ImageIcon(getIcone()), new Dimension(sz, sz)));
			}
		} else if (isMale()) {
			return IconUtil.getIconLarge(ICONS.K.MAN, sz);
		} else if (isFemale()) {
			return IconUtil.getIconLarge(ICONS.K.WOMAN, sz);
		}
		return IconUtil.getIconLarge(ICONS.K.UNKNOWN, sz);
	}

	public void setIcone(String str) {
		icone = str;
	}

	public String getIcone() {
		if (icone == null) {
			icone = "";
		}
		return icone;
	}

	@Override
	public String toDetail(Integer detailed) {
		StringBuilder b = new StringBuilder();
		b.append(toDetailHeader(detailed));
		b.append(getInfo(detailed, DATA.GENDER_CHILDHOOD, getChildhood()));
		b.append(getInfo(detailed, DATA.GENDER_ADOLESCENCE, getAdolescence()));
		b.append(getInfo(detailed, DATA.GENDER_ADULTHOOD, getAdulthood()));
		b.append(getInfo(detailed, DATA.GENDER_RETIREMENT, getRetirement()));
		b.append(getInfo(detailed, DATA.ICON_FILE, getIcone()));
		b.append(toDetailFooter(detailed));
		return b.toString();
	}

	@Override
	public String toCsv(String quoteStart, String quoteEnd, String separator) {
		StringBuilder b = new StringBuilder();
		b.append(quoteStart).append(getClean(this)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getName())).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(childhood)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(adolescence)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(adulthood)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(retirement)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(icone)).append(quoteStart).append("\n");
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
	public String toXml() {
		StringBuilder b = new StringBuilder(toXmlBeg());
		b.append(XmlUtil.setAttribute(0, XK.CHILDHOOD, getChildhood()));
		b.append(XmlUtil.setAttribute(0, XK.ADOLESCENCE, getAdolescence()));
		b.append(XmlUtil.setAttribute(0, XK.ADULTHOOD, getAdulthood()));
		b.append(XmlUtil.setAttribute(0, XK.RETIREMENT, getRetirement()));
		b.append(XmlUtil.setAttribute(0, XK.ICONE, getIcone()));
		b.append(">\n");
		b.append(toXmlEnd());
		return b.toString();
	}

	public static Gender fromXml(Node node) {
		Gender p = new Gender();
		fromXmlBeg(node, p);
		p.setChildhood(XmlUtil.getInteger(node, XK.CHILDHOOD));
		p.setAdolescence(XmlUtil.getInteger(node, XK.ADOLESCENCE));
		p.setAdulthood(XmlUtil.getInteger(node, XK.ADULTHOOD));
		p.setRetirement(XmlUtil.getInteger(node, XK.RETIREMENT));
		p.setIcone(XmlUtil.getString(node, XK.ICONE));
		fromXmlEnd(node, p);
		return (p);
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		if (!(obj instanceof Gender)) {
			return false;
		}
		Gender test = (Gender) obj;
		boolean ret = equalsStringNullValue(getName(), test.getName());
		ret = ret && equalsIntegerNullValue(childhood, test.getChildhood());
		ret = ret && equalsIntegerNullValue(adolescence, test.getAdolescence());
		ret = ret && equalsIntegerNullValue(adulthood, test.getAdulthood());
		ret = ret && equalsIntegerNullValue(retirement, test.getRetirement());
		return ret;
	}

	@Override
	public int hashCode() {
		return hashPlus(super.hashCode(),
				childhood,
				adolescence,
				adulthood,
				retirement,
				icone);
	}

	public static Gender find(List<Gender> list, String str) {
		for (Gender elem : list) {
			if (elem.getName().equals(str)) {
				return (elem);
			}
		}
		return (null);
	}

	public static Gender find(List<Gender> list, Long id) {
		for (Gender elem : list) {
			if (elem.id.equals(id)) {
				return (elem);
			}
		}
		return (null);
	}

	public static List<String> getDefColumns() {
		List<String> list = AbstractEntity.getDefColumns(Book.TYPE.GENDER);
		list.add("icone, 256");
		return (list);
	}

	public static List<String> getTable() {
		List<String> ls = new ArrayList<>();
		String tableName = K.GENDER.m();
		AbstractEntity.getTable(tableName, ls);
		String INTEGER = ",Integer,0";
		ls.add(tableName + "," + K.CHILDHOOD.c() + INTEGER);
		ls.add(tableName + "," + K.ADOLESCENCE.c() + INTEGER);
		ls.add(tableName + "," + K.ADULTHOOD.c() + INTEGER);
		ls.add(tableName + "," + K.RETIREMENT.c() + INTEGER);
		ls.add(tableName + "," + K.ICONE.c() + ",String,256");
		return (ls);
	}

	@Override
	public AbstractEntity copyTo(MainFrame m) {
		Gender ne = new Gender();
		doCopyTo(m, ne);
		ne.setAdolescence(getAdolescence());
		ne.setAdulthood(getAdulthood());
		ne.setChildhood(getChildhood());
		ne.setIcone(getIcone());
		ne.setRetirement(getRetirement());
		return ne;
	}

}
