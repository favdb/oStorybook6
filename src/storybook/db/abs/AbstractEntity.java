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
package storybook.db.abs;

import i18n.I18N;
import java.awt.Color;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.swing.Icon;
import org.w3c.dom.Node;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.db.DB.DATA;
import storybook.db.EntityUtil;
import storybook.db.SbDate;
import storybook.db.book.Book;
import storybook.db.endnote.Endnote;
import storybook.tools.StringUtil;
import storybook.tools.TextUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.ColorUtil;
import storybook.tools.xml.XmlKey.XK;
import storybook.tools.xml.XmlUtil;
import static storybook.tools.xml.XmlUtil.*;

public abstract class AbstractEntity implements Serializable, Comparable<AbstractEntity> {

	public static final String L_ID = "id",
	   L_NAME = "name",
	   L_CREATION = "creation",
	   L_MAJ = "maj",
	   L_DESCRIPTION = "description",
	   L_NOTES = "notes",
	   L_ASSISTANT = "assistant",
	   L_EMPTY = "empty",
	   L_STRING = "String";

	private static Long transientIdCounter = 1L;
	protected Long id = -1L, transientId = -1L;
	private Book.TYPE objtype;
	private String common = "000";//first=description, second=notes, third=assistant
	public String name = "",
	   aspect = "",
	   creation = "",
	   maj = "",
	   desc = "",
	   notes = "",
	   assistant = "";

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public AbstractEntity(Book.TYPE objtype, String common) {
		super();
		transientId = transientIdCounter++;
		this.objtype = objtype;
		this.common = common;
		setCreation();
		setMaj();
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public AbstractEntity(Book.TYPE objtype, String common, Long id, String name) {
		this(objtype, common);
		setId(id);
		setName(name);
		setCreation();
		setMaj();
		desc = "";
		notes = "";
		transientId = transientIdCounter++;
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public AbstractEntity(Book.TYPE type, String common, ResultSet rs) {
		this(type, common);
		try {
			setId(rs.getLong("id"));
			setName(rs.getString("name"));
			setCreation(rs.getString("creation"));
			setMaj(rs.getString("maj"));
			if (common.charAt(0) == '1') {
				setDescription(rs.getString("description"));
			}
			if (common.charAt(1) == '1') {
				setNotes(rs.getString("notes"));
			}
			if (common.charAt(2) == '1') {
				setAssistant(rs.getString("assistant"));
			}
			setAspect(rs.getString("aspect"));
		} catch (SQLException ex) {
			//empty
		}
	}

	/**
	 * get a limited value
	 *
	 * @param v: the value to chack
	 * @param a: the minimum allowed value
	 * @param b: the maximum allowed value
	 * @return
	 */
	public static int getMinMax(int v, int a, int b) {
		if (v < a) {
			return a;
		}
		if (v > b) {
			return b;
		}
		return v;
	}

	public Book.TYPE getObjType() {
		return (objtype);
	}

	public void setObjType(Book.TYPE value) {
		objtype = value;
	}

	public String getCommon() {
		return common;
	}

	public void setCommon(String value) {
		common = value;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setCreation() {
		creation = (SbDate.getToDay()).getDateTimeToString();
	}

	public void setCreation(String c) {
		creation = c;
	}

	public String getCreation() {
		return (creation);
	}

	public void setMaj() {
		maj = (SbDate.getToDay()).getDateTimeToString();
	}

	public void setMaj(String c) {
		maj = c;
	}

	public String getMaj() {
		return (maj);
	}

	public String getName() {
		return (name == null ? "???" : name.trim());
	}

	public String getFullName() {
		return (name == null ? "???" : name.trim());
	}

	public void setName(String x) {
		name = x;
	}

	public String getAspect() {
		if (aspect == null) {
			return "";
		}
		return aspect;
	}

	public void setAspect(String x) {
		aspect = x;
	}

	public boolean isTransient() {
		return id.intValue() == -1L;
	}

	public Long getTransientId() {
		return transientId;
	}

	public boolean hasDescription() {
		return (!Html.htmlToText(this.desc).equals(""));
	}

	public String getDescription() {
		return (desc == null ? "" : desc);
	}

	public void setDescription(String x) {
		desc = x;
	}

	public String getNotes() {
		return (notes == null ? "" : notes);
	}

	public boolean hasNotes() {
		return !Html.htmlToText(this.notes).isEmpty();
	}

	public void setNotes(String x) {
		notes = x;
	}

	public String getAssistant() {
		return (assistant == null || Html.isEmpty(assistant) ? "" : assistant);
	}

	public void setAssistant(String str) {
		this.assistant = str;
	}

	public String get() {
		return getId().toString() + "|" + getObjType().toString() + "|" + getName();
	}

	@Override
	public int compareTo(AbstractEntity ch) {
		return getName().compareTo(ch.getName());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = hash * 31 + (id != null ? id.hashCode() : 0);
		if (isTransient()) {
			hash = hash * 31
			   + (transientId != null ? transientId.hashCode() : 0);
		}
		hash = hash * 31 + (creation != null ? creation.hashCode() : 0);
		hash = hash * 31 + (maj != null ? maj.hashCode() : 0);
		hash = hash * 31 + (name != null ? name.hashCode() : 0);
		hash = hash * 31 + (aspect != null ? aspect.hashCode() : 0);
		hash = hash * 31 + (desc != null ? desc.hashCode() : 0);
		hash = hash * 31 + (notes != null ? notes.hashCode() : 0);
		hash = hash * 31 + (assistant != null ? assistant.hashCode() : 0);
		return hash;
	}

	public static int hashPlus(int hash, Object... v) {
		if (v == null || v.length < 1) {
			return 0;
		}
		int r = hash;
		for (Object o : v) {
			r = r * 31 + (o != null ? o.hashCode() : 0);
		}
		return r;
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		Class<?> cl1 = this.getClass();
		Class<?> cl2 = obj.getClass();
		if (cl1 != cl2) {
			return false;
		}
		AbstractEntity test = (AbstractEntity) obj;
		// for test use getter here since hibernate may need to load it first
		if (isTransient()) {
			return transientId.equals(test.getTransientId());
		}
		return id.equals(test.getId());
	}

	public static boolean equalsObjectNullValue(Object o1, Object o2) {
		if (o1 != null && o2 == null) {
			return false;
		}
		if (o1 == null && o2 != null) {
			return false;
		}
		if (o1 != null) {
			if (o2 == null) {
				return false;
			}
			return o1.equals(o2);
		}
		return true;
	}

	public static boolean equalsStringNullValue(String s1, String s2) {
		if (s1 != null && s2 == null) {
			return false;
		}
		if (s1 == null && s2 != null) {
			return false;
		}
		if (s1 != null) {
			if (s2 == null) {
				return false;
			}
			String st1 = Html.htmlToText(s1);
			String st2 = Html.htmlToText(s2);
			return st1.equals(st2);
		}
		return true;
	}

	public static boolean equalsIntegerNullValue(Integer n1, Integer n2) {
		if (n1 != null && n2 == null) {
			return false;
		}
		if (n1 == null && n2 != null) {
			return false;
		}
		if (n1 != null) {
			if (n2 == null) {
				return false;
			}
			return n1.equals(n2);
		}
		return true;
	}

	public static boolean equalsLongNullValue(Long l1, Long l2) {
		if (l1 != null && l2 == null) {
			return false;
		}
		if (l1 == null && l2 != null) {
			return false;
		}
		if (l1 != null) {
			if (l2 == null) {
				return false;
			}
			return l1.equals(l2);
		}
		return true;
	}

	public static boolean equalsDateNullValue(Date d1, Date d2) {
		if (d1 != null && d2 == null) {
			return false;
		}
		if (d1 == null && d2 != null) {
			return false;
		}
		if (d1 != null) {
			if (d2 == null) {
				return false;
			}
			return (d1.compareTo(d2) == 0);
		}
		return true;
	}

	public static boolean equalsTimestampNullValue(Timestamp ts1, Timestamp ts2) {
		if (ts1 != null && ts2 == null) {
			return false;
		}
		if (ts1 == null && ts2 != null) {
			return false;
		}
		if (ts1 != null) {
			if (ts2 == null) {
				return false;
			}
			return (ts1.compareTo(ts2) == 0);
		}
		return true;
	}

	public static boolean equalsListNullValue(
	   List<? extends AbstractEntity> li1,
	   List<? extends AbstractEntity> li2) {
		if (li1 == null && li2 == null) {
			return true;
		}
		if (li1 == null || li2 == null) {
			return false;
		}
		if (li1.isEmpty() && li2.isEmpty()) {
			return true;
		}
		if (li1.size() != li2.size()) {
			return false;
		}
		List<Long> ids1 = new ArrayList<>();
		for (AbstractEntity e : li1) {
			ids1.add(e.getId());
		}
		List<Long> ids2 = new ArrayList<>();
		for (AbstractEntity e : li2) {
			ids2.add(e.getId());
		}
		ids1.removeAll(ids2);
		return ids1.isEmpty();
	}

	public static int getListHashCode(List<?> list) {
		int hash = 31;
		for (Object o : list) {
			AbstractEntity e = (AbstractEntity) o;
			hash = hash * 31 + (e.getId() != null ? e.getId().hashCode() : 0);
		}
		return hash;
	}

	public static boolean equalsBooleanNullValue(Boolean b1, Boolean b2) {
		if (b1 != null && b2 == null) {
			return false;
		}
		if (b1 == null && b2 != null) {
			return false;
		}
		if (b1 != null) {
			if (b2 == null) {
				return false;
			}
			return b1.equals(b2);
		}
		return true;
	}

	public String getAbbr() {
		return toString();
	}

	public Icon getIcon() {
		return (IconUtil.getIconSmall(ICONS.getIconKey("ent_" + objtype.toString())));
	}

	public Icon getIcon(int size) {
		return (IconUtil.getIconLarge(ICONS.getIconKey("ent_" + objtype.toString()), size));
	}

	public String getIconName() {
		return ("ent_" + objtype.toString());
	}

	public String toCsv(String quoteStart, String quoteEnd, String separator) {
		StringBuilder b = new StringBuilder(toCsvHeader(quoteStart, quoteEnd, separator));
		b.append(toCsvFooter(quoteStart, quoteEnd, separator));
		return (b.toString());
	}

	public String toCsvHeader(String quoteStart, String quoteEnd, String separator) {
		StringBuilder b = new StringBuilder();
		b.append(quoteStart).append(getId().toString()).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getName()).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getCreation())).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getMaj())).append(quoteEnd).append(separator);
		return (b.toString());
	}

	public String toCsvFooter(String quoteStart, String quoteEnd, String separator) {
		StringBuilder b = new StringBuilder();
		b.append(quoteStart).append(getClean(getDescription())).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getNotes())).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getAssistant())).append(quoteEnd).append(separator);
		b.append("\n");
		return (b.toString());
	}

	/**
	 * get info for an Entity
	 *
	 * @param det
	 * @param key
	 * @param entity
	 * @return
	 */
	public String getInfo(Integer det, DATA key, AbstractEntity entity) {
		if (det == 0 && entity == null) {
			return "";
		}
		return getInfo(key, getClean(entity));
	}

	public String getInfo(Integer det, String key, AbstractEntity entity) {
		if (det == 0 && entity == null) {
			return "";
		}
		return getInfo(key, getClean(entity));
	}

	/**
	 * detailed info parameter value: <br>
	 * 0 = simple, only essential informations, String are elipsized to 200 <br>
	 * 1 = detailed, all used informations, String are elipsized to 500 <br>
	 * 2 = more, all used informations, String are not elipsized<br>
	 * 3 = full, all informations, String are not elipsized
	 */
	/**
	 * get info for a String
	 *
	 * @param det
	 * @param key
	 * @param value
	 * @return
	 */
	public String getInfo(Integer det, DATA key, String value) {
		if ((det == null || det < 3) && (value.isEmpty() || value.equals(I18N.getMsg("empty")))) {
			return "";
		}
		if (det != null) {
			switch (det) {
				case 0:
					return getInfo(key, TextUtil.ellipsize(value, 200));
				case 1:
					return getInfo(key, TextUtil.ellipsize(value, 500));
			}
		}
		return getInfo(key, value);
	}

	/**
	 * get info for an Integer
	 *
	 * @param det
	 * @param key
	 * @param value
	 * @return
	 */
	public String getInfo(Integer det, DATA key, Integer value) {
		if (det < 3 && (value == null || value == 0)) {
			return "";
		}
		if (value == null) {
			return getInfo(det, key, "0");
		}
		return getInfo(det, key, value.toString());
	}

	/**
	 * get info for a Date
	 *
	 * @param det
	 * @param key
	 * @param value
	 * @return
	 */
	public String getInfo(Integer det, DATA key, Date value) {
		if (det < 3 && value == null) {
			return "";
		}
		if (value == null) {
			return getInfo(key, "empty");
		}
		return getInfo(det, key, value.toString());
	}

	/**
	 * get info for a Boolean
	 *
	 * @param det
	 * @param key
	 * @param value
	 * @return
	 */
	public String getInfo(Integer det, DATA key, boolean value) {
		if (det < 2 && value == false) {
			return "";
		}
		return getInfo(key, (value ? I18N.getMsg("yes") : I18N.getMsg("no")));
	}

	/**
	 * get info for a list of Entities
	 *
	 * @param det
	 * @param data
	 * @param entities
	 * @return
	 */
	public String getInfo(Integer det, DATA data, List entities) {
		if (det < 3 && entities.isEmpty()) {
			return "";
		}
		return getInfo(data, EntityUtil.getNames(entities));
	}

	/**
	 * get info for a String
	 *
	 * @param det
	 * @param key
	 * @param value
	 * @return
	 */
	public String getInfo(Integer det, String key, String value) {
		if (det < 3 && value.isEmpty()) {
			return "";
		}
		return getInfo(key, value);
	}

	public String getInfo(Integer det, String key, Integer value) {
		if (det < 3 && value == null) {
			return "";
		}
		return getInfo(key, value.toString());
	}

	/**
	 * get info generic for a DATA.K with a String value
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	private String getInfo(DATA key, String value) {
		return getInfo(key.i18n(), value);
	}

	/**
	 * get info generic for a key String with a String value
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	private String getInfo(String key, String value) {
		StringBuilder b = new StringBuilder();
		b.append(Html.TR_B);
		b.append(Html.intoTD(Html.intoB(I18N.getMsg(key)), " valign=\"top\""));
		b.append(Html.intoTD((value == null || value.isEmpty() ? I18N.getMsg(L_EMPTY) : value)));
		b.append(Html.TR_E);
		return (b.toString());
	}

	/**
	 * export Entity to HTML beautify
	 *
	 * @return
	 */
	public String toHtml() {
		return (toCsv("<td>", "</td>", "\n"));
	}

	/**
	 * export detailed informations
	 *
	 * @param detailed level of detail
	 *
	 * @return
	 */
	public String toDetail(Integer detailed) {
		StringBuilder b = new StringBuilder();
		b.append(toDetailHeader(detailed));
		b.append(toDetailFooter(detailed));
		return b.toString();
	}

	/**
	 * export header of detailed informations
	 *
	 * @param det level of detail
	 *
	 * @return
	 */
	public String toDetailHeader(Integer det) {
		StringBuilder b = new StringBuilder();
		b.append(Html.TABLE_B);
		b.append(Html.intoTR(Html.intoTD(Html.intoH(2, getName()), "colspan=\"2\"")));
		if (App.isDev()) {
			b.append(getInfo(DATA.ID, getClean(getId())));
		}
		if (det > 0) {
			b.append(getInfo(det, DATA.ASPECT, getClean(getAspect())));
			b.append(getInfo(DATA.DATE_CREATION, getClean(getCreation())));
			b.append(getInfo(DATA.DATE_MAJ, getClean(getMaj())));
		}
		return b.toString();
	}

	/**
	 * export footer of detailed informations
	 *
	 * @param detailed level of detail
	 *
	 * @return
	 */
	public String toDetailFooter(Integer detailed) {
		StringBuilder b = new StringBuilder();
		if (common.charAt(0) == '1') {
			b.append(getInfo(detailed, DATA.DESCRIPTION, getDescription()));
		}
		if (common.charAt(1) == '1') {
			b.append(getInfo(detailed, DATA.NOTES, getNotes()));
		}
		if (common.charAt(2) == '1') {
			b.append(getInfo(detailed, DATA.ASSISTANT, getAssistant()));
		}
		b.append(Html.TABLE_E);
		return b.toString();
	}

	/**
	 * get String text for a key
	 *
	 * @param key
	 * @param value
	 *
	 * @return
	 */
	public String toHtmlString(String key, String value) {
		if (value != null && !Html.htmlToText(value).isEmpty()) {
			return getInfo(key, value);
		}
		return "";
	}

	/**
	 * export Entity to Text beautify
	 *
	 * @return
	 */
	public String toText() {
		return "";
	}

	/**
	 * get the String value of the Entity
	 *
	 * @return
	 */
	@Override
	public String toString() {
		return (!(this instanceof Endnote) && hasNotes() ? "*" : "") + getName();
	}

	/**
	 * export Entity to XML beautify
	 *
	 * @return
	 */
	public String toXml() {
		StringBuilder s = new StringBuilder(toXmlBeg());
		if (common.equals("000")) {
			s.append(" />\n");
		} else {
			s.append(toXmlFooter());
		}
		return s.toString();
	}

	/**
	 * get the header of XML for the entity
	 *
	 * @return
	 */
	public String toXmlBeg() {
		StringBuilder s = new StringBuilder();
		s.append("<").append(objtype.toString()).append(" ");
		s.append(setAttribute(0, XK.ID, getId().toString()));
		if (creation.isEmpty()) {
			setCreation();
		}
		s.append(setAttribute(0, XK.CREATION, getCreation()));
		if (maj.isEmpty()) {
			setMaj();
		}
		s.append(setAttribute(0, XK.MAJ, getMaj()));
		s.append(setAttribute(8, XK.NAME, getName()));
		s.append(setAttribute(0, XK.ASPECT, getAspect()));
		return s.toString();
	}

	/**
	 * get the footer of XML for the entity
	 *
	 * @return
	 */
	public String toXmlFooter() {
		StringBuilder s = new StringBuilder();
		s.append(" >\n");
		s.append(toXmlEnd());
		return s.toString();
	}

	/**
	 * get the end of XML for the entity
	 *
	 * @return
	 */
	public String toXmlEnd() {
		StringBuilder s = new StringBuilder();
		if (!getClean(getDescription()).isEmpty()) {
			s.append(toXmlMeta(2, XK.DESCRIPTION, desc, false));
		}
		if (!getClean(getNotes()).isEmpty()) {
			s.append(toXmlMeta(2, XK.NOTES, notes, false));
		}
		if (!getClean(getAssistant()).isEmpty()) {
			s.append(toXmlMeta(2, XK.ASSISTANT, assistant, false));
		}
		s.append("    </").append(objtype.toString()).append(">\n");
		return s.toString();
	}

	public String toXmlMeta(int n, XK key, String value, boolean html) {
		return toXmlMeta(n, key.toString().toLowerCase(), value, html);
	}

	public String toXmlMeta(int n, String name, String value, boolean html) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < n; i++) {
			b.append("    ");
		}
		b.append("<").append(name.toLowerCase()).append(">");
		if ((value != null) && !value.isEmpty()) {
			if (html) {
				if (!value.replace("<p>", "").replace("</p>", "").replace("\n", "").trim().isEmpty()) {
					b.append("<![CDATA[").append(value).append("]]>");
				}
			} else {
				b.append(Html.htmlToText(value));
			}
		}
		b.append("</").append(name.toLowerCase()).append(">\n");
		return (b.toString());
	}

	public static void fromXmlBeg(Node node, AbstractEntity p) {
		p.setId(getLong(node, XK.ID));
		p.setCreation(getString(node, XK.CREATION));
		p.setMaj(getString(node, XK.MAJ));
		p.setName(getString(node, XK.NAME));
		p.setAspect(getString(node, XK.ASPECT));
	}

	public static void fromXmlEnd(Node node, AbstractEntity p) {
		if (p.getCommon().charAt(0) == '1') {
			p.setDescription(getText(node, XK.DESCRIPTION));
		}
		if (p.getCommon().charAt(1) == '1') {
			p.setNotes(getText(node, XK.NOTES));
		}
		if (p.getCommon().charAt(2) == '1') {
			p.setAssistant(getText(node, XK.ASSISTANT));
		}
	}

	public String xmlCommon() {
		String s = XmlUtil.setAttribute(0, XK.ID, getId().toString());
		if (creation == null || creation.isEmpty()) {
			setCreation();
		}
		s += XmlUtil.setAttribute(0, XK.CREATION, getCleanDate(creation));
		if (maj == null || maj.isEmpty()) {
			setMaj();
		}
		s += XmlUtil.setAttribute(0, XK.MAJ, getCleanDate(maj));
		s += XmlUtil.setAttribute(0, XK.NAME, getClean(name));
		s += XmlUtil.setAttribute(0, XK.ASPECT, getClean(aspect));
		return (s);
	}

	public String getClean(Boolean d) {
		return (d != null ? d.toString() : "");
	}

	public String getClean(Timestamp d) {
		return (d != null ? d.toString() : "");
	}

	public String getClean(Date date) {
		return (date != null ? date.toString() : "");
	}

	public String getClean(Color color) {
		return (ColorUtil.getHTML(color));
	}

	public String getClean(Integer d) {
		return (d != null ? d.toString() : "");
	}

	public String getClean(Long d) {
		return (d != null ? d.toString() : "");
	}

	public String getClean(String str) {
		if (str == null) {
			return "";
		}
		if (!str.isEmpty() && Html.htmlToText(str).isEmpty()) {
			return "";
		}
		return StringUtil.escapeHtml(str).trim();
	}

	public String getClean(SbDate d) {
		if (d == null) {
			return ("");
		}
		return (d.getDateTimeToString());
	}

	public String getClean(AbstractEntity entity) {
		return (entity != null ? entity.getName() : "");
	}

	public String getClean(UUID uuid) {
		return (uuid.toString());
	}

	public String getCleanDate(String date) {
		if (date == null) {
			return ("");
		}
		String x[] = date.split(" ");
		String r = date;
		if (x.length != 2) {
			r = SbDate.getToDay().getDateTimeToString();
		}
		return (r);
	}

	public static List<String> getDefColumns(Book.TYPE type) {
		List<String> list = new ArrayList<>();
		list.add(getDefData(L_NAME, 255));
		AbstractEntity entity = EntityUtil.createNewEntity(type);
		String common = entity.getCommon();
		if (common.charAt(0) == '1') {
			list.add(getDefData(L_DESCRIPTION, 32768));
		}
		if (common.charAt(1) == '1') {
			list.add(getDefData(L_NOTES, 32768));
		}
		if (common.charAt(2) == '1') {
			list.add(getDefData(L_ASSISTANT, 32768));
		}
		return (list);
	}

	public static String getDefData(String name, int len) {
		return name + "," + len;
	}

	public static void getTable(String tableName, List<String> ls) {
		ls.add(tableName + getTableData(L_NAME, L_STRING, 256));
		ls.add(tableName + getTableData(L_CREATION, L_STRING, 255));
		ls.add(tableName + getTableData(L_MAJ, L_STRING, 255));
		ls.add(tableName + getTableData(L_DESCRIPTION, L_STRING, 32768));
		ls.add(tableName + getTableData(L_NOTES, L_STRING, 32768));
		ls.add(tableName + getTableData(L_ASSISTANT, L_STRING, 32768));
	}

	public static String getTableData(String key, String type, int len) {
		return "," + key + "," + type + "," + len;
	}

	public boolean hasDate() {
		return false;
	}

	public void changeHtmlLinks(String path) {
		setDescription(Html.changeLinks(path, getDescription()));
		setNotes(Html.changeLinks(path, getNotes()));
	}

}
