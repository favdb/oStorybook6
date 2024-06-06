/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2015 Martin Mustun, Pete Keller

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
package storybook.db.person;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.Icon;
import org.w3c.dom.Node;
import storybook.db.DB.DATA;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.attribute.Attribute;
import storybook.db.book.Book;
import storybook.db.category.Category;
import storybook.db.gender.Gender;
import storybook.tools.DateUtil;
import static storybook.tools.DateUtil.clearTime;
import storybook.tools.ListUtil;
import storybook.tools.swing.ColorUtil;
import storybook.tools.xml.XmlKey.XK;
import storybook.tools.xml.XmlUtil;
import storybook.ui.MainFrame;

/**
 * @hibernate.class table="PERSON"
 */
public class Person extends AbstractEntity {

	private String firstname = "";
	private String lastname = "";
	private String abbreviation = "";
	private Gender gender = null;
	private Long gender_id = -1L;
	private Category category = null;
	private Long category_id = -1L;
	private Date birthday = null;
	private Date dayofdeath = null;
	private Integer color = 0;
	private String occupation = "";
	private List<Category> categories = new ArrayList<>();
	private List<Long> categories_id = new ArrayList<>();
	private List<Attribute> attributes = new ArrayList<>();
	private List<Long> attributes_id = new ArrayList<>();

	public Person() {
		super(Book.TYPE.PERSON, "111");
	}

	public Person(Gender gender, String firstname, String lastname,
			String abbreviation, Date birthday, Date dayofdeath,
			String occupation, String description, Integer color, String notes,
			Category category, List<Attribute> attributes) {
		this();
		this.gender = gender;
		this.firstname = firstname;
		this.lastname = lastname;
		this.abbreviation = abbreviation;
		this.birthday = birthday;
		this.dayofdeath = dayofdeath;
		this.occupation = occupation;
		this.color = color;
		setNotes(notes);
		this.category = category;
		this.attributes = attributes;
		setAssistant("");
	}

	@Override
	public String getName() {
		if (super.getName().isEmpty()) {
			return (getFirstname() + " " + getLastname()).trim();
		}
		return super.getName();
	}

	public Gender getGender() {
		return this.gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public Long getGenderId() {
		return this.gender_id;
	}

	public void setGenderId(Long value) {
		this.gender_id = value;
	}

	public String getFirstname() {
		return this.firstname == null ? "" : this.firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
		setName((getFirstname() + " " + getLastname()).trim());
	}

	public String getLastname() {
		return this.lastname == null ? "" : this.lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
		setName((getFirstname() + " " + getLastname()).trim());
	}

	@Override
	public String getFullName() {
		return (getFirstname() + " " + getLastname()).trim();
	}

	public String getFullNameAbbr() {
		return getFullName() + " [" + getAbbreviation() + "]";
	}

	public String getAbbreviation() {
		return this.abbreviation == null ? "" : this.abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public Date getBirthday() {
		return this.birthday;
	}

	public String getBirthdayToString() {
		return (birthday == null ? "" : birthday.toString());
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public Date getDayofdeath() {
		return this.dayofdeath;
	}

	public String getDayofdeathToString() {
		return (dayofdeath == null ? "" : birthday.toString());
	}

	public void setDayofdeath(Date dayofdeath) {
		this.dayofdeath = dayofdeath;
	}

	public String getOccupation() {
		return this.occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public Integer getColor() {
		return (this.color);
	}

	public Color getJColor() {
		return (color == 0 ? new Color(0) : new Color(color));
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

	public void setColor(Integer color) {
		this.color = color;
	}

	public void setJColor(Color color) {
		if (color == null) {
			this.color = 0;
			return;
		}
		this.color = color.getRGB();
	}

	public Category getCategory() {
		return this.category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Long getCategoryId() {
		return this.category_id;
	}

	public void setCategoryId(Long value) {
		this.category_id = value;
	}

	public List<Category> getCategories() {
		if (this.categories == null) {
			return (new ArrayList<>());
		}
		return this.categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	public List<Long> getCategoriesId() {
		if (this.categories_id == null) {
			return (new ArrayList<>());
		}
		return this.categories_id;
	}

	public void setCategoriesId(List<Long> values) {
		this.categories_id.addAll(values);
	}

	public void addCategories(Category c) {
		categories.add(c);
		ListUtil.setUnique(categories);
	}

	public void removeCategories(Category c) {
		categories.remove(c);
	}

	public List<Attribute> getAttributes() {
		if (attributes == null) {
			attributes = new ArrayList<>();
		}
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String getAbbr() {
		if (abbreviation == null) {
			return ("??");
		}
		return abbreviation;
	}

	@Override
	public Icon getIcon() {
		if (gender != null) {
			return gender.getIcon();
		}
		return super.getIcon();
	}

	@Override
	public Icon getIcon(int sz) {
		if (gender != null) {
			return gender.getIcon(sz);
		}
		return super.getIcon(sz);
	}

	@Override
	public boolean hasDate() {
		return dayofdeath != null || birthday != null;
	}

	public Boolean isDead(Date now) {
		if (getDayofdeath() == null || now == null) {
			return false;
		}
		return (now.after(getDayofdeath()));
	}

	public int calculateAge(Date now) {
		if (birthday == null) {
			return -1;
		}

		Calendar dateOfBirth = new GregorianCalendar();
		dateOfBirth.setTime(birthday);
		dateOfBirth = clearTime(dateOfBirth);

		if (isDead(now)) {
			Calendar death = new GregorianCalendar();
			death.setTime(getDayofdeath());
			death = clearTime(death);

			int age = death.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);

			Calendar dateOfBirth2 = new GregorianCalendar();
			dateOfBirth2.setTime(birthday);
			dateOfBirth2 = clearTime(dateOfBirth2);
			dateOfBirth2.add(Calendar.YEAR, age);

			if (death.before(dateOfBirth2)) {
				age--;
			}
			return age;
		}

		Calendar today = new GregorianCalendar();
		today.setTime(now);

		int age = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);

		dateOfBirth.add(Calendar.YEAR, age);

		if (today.before(dateOfBirth)) {
			age--;
		}
		return age;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String toDetail(Integer detailed) {
		StringBuilder b = new StringBuilder();
		b.append(super.toDetailHeader(detailed));
		b.append(getInfo(detailed, DATA.PERSON_FIRSTNAME, getFirstname()));
		b.append(getInfo(detailed, DATA.PERSON_LASTNAME, getLastname()));
		b.append(getInfo(detailed, DATA.ABBREVIATION, getAbbreviation()));
		b.append(getInfo(detailed, DATA.PERSON_GENDER, getGender()));
		if (detailed > 0) {
			if (birthday != null) {
				b.append(getInfo(detailed, DATA.PERSON_BIRTHDAY, getBirthday()));
			}
			if (dayofdeath != null) {
				b.append(getInfo(detailed, DATA.PERSON_DEATH, getDayofdeath()));
			}
			if (!occupation.isEmpty()) {
				b.append(getInfo(detailed, DATA.PERSON_OCCUPATION, getOccupation()));
			}
		}
		b.append(getInfo(detailed, DATA.PERSON_COLOR, getHTMLColor()));
		b.append(getInfo(detailed, DATA.PERSON_CATEGORY, getCategory()));
		if (categories != null && !getCategories().isEmpty()) {
			b.append(getInfo(detailed, DATA.PERSON_CATEGORIES, EntityUtil.getIds((List) getCategories())));
		}
		if (!getAttributes().isEmpty() || detailed > 1) {
			b.append(getInfo(detailed, DATA.PERSON_ATTRIBUTES, EntityUtil.getIds((List) getAttributes())));
		}
		b.append(super.toDetailFooter(detailed));
		return (b.toString());
	}

	@Override
	public String toString() {
		if (isTransient()) {
			return "";
		}
		return (getName() + (hasNotes() ? "*" : ""));
	}

	@Override
	public String toCsv(String quoteStart, String quoteEnd, String separator) {
		StringBuilder b = new StringBuilder();
		b.append(quoteStart).append(getClean(this)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getGender())).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(firstname)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(lastname)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(abbreviation)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(birthday)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(dayofdeath)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(occupation)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getJColor())).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getCategory())).append(quoteEnd).append(separator);
		b.append(quoteStart);
		for (Attribute attr : getAttributes()) {
			b.append(attr.getId().toString()).append("/");
		}
		b.append(quoteEnd).append(separator);
		b.append(quoteStart).append(getDescription()).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getNotes()).append(quoteEnd).append("\n");
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
	@SuppressWarnings("unchecked")
	public String toXml() {
		StringBuilder b = new StringBuilder(toXmlBeg());
		b.append(XmlUtil.setAttribute(8, XK.GENDER, getGender()));
		b.append(XmlUtil.setAttribute(0, XK.FIRSTNAME, getFirstname()));
		b.append(XmlUtil.setAttribute(0, XK.LASTNAME, getLastname()));
		b.append(XmlUtil.setAttribute(0, XK.ABBREVIATION, getAbbreviation()));
		b.append(XmlUtil.setAttribute(8, XK.BIRTHDAY, getBirthday()));
		b.append(XmlUtil.setAttribute(0, XK.DEATH, getDayofdeath()));
		b.append(XmlUtil.setAttribute(0, XK.OCCUPATION, getOccupation()));
		b.append(XmlUtil.setAttribute(8, XK.COLOR, getColor()));
		b.append(XmlUtil.setAttribute(0, XK.CATEGORY, getCategory()));
		b.append(XmlUtil.setAttribute(8, XK.CATEGORIES, EntityUtil.getIds((List) getCategories())));
		List<String> ls = new ArrayList<>();
		for (Attribute attr : getAttributes()) {
			ls.add("[" + attr.getKey() + "]" + attr.getValue());
		}
		if (!ls.isEmpty()) {
			b.append(XmlUtil.setAttribute(8, XK.ATTRIBUT, ListUtil.join(ls)));
		}
		b.append(">\n");
		b.append(toXmlEnd());
		return b.toString();
	}

	// NOTE les attributs et des catégories multiples sont ignorées
	public static Person fromXml(Node node) {
		Person p = new Person();
		fromXmlBeg(node, p);
		p.setFirstname(XmlUtil.getString(node, XK.FIRSTNAME));
		p.setLastname(XmlUtil.getString(node, XK.LASTNAME));
		p.setAbbreviation(XmlUtil.getString(node, XK.ABBREVIATION));
		p.setGenderId(XmlUtil.getLong(node, XK.GENDER));
		p.setCategoryId(XmlUtil.getLong(node, XK.CATEGORY));
		p.setBirthday(DateUtil.stdStringToDate(XmlUtil.getString(node, XK.BIRTHDAY)));
		p.setDayofdeath(DateUtil.stdStringToDate(XmlUtil.getString(node, XK.DEATH)));
		p.setOccupation(XmlUtil.getString(node, XK.OCCUPATION));
		if (!XmlUtil.getString(node, XK.COLOR).isEmpty()) {
			p.setColor(XmlUtil.getInteger(node, XK.COLOR));
		}
		String xx = XmlUtil.getString(node, XK.ATTRIBUT);
		if (!xx.isEmpty()) {
			List<Attribute> lsa = new ArrayList<>();
			String zz[] = xx.split(",");
			for (String zzx : zz) {
				String zxx[] = zzx.replace("[", "").split("]");
				if (zxx.length > 1 && !zxx[0].isEmpty() && !zxx[1].isEmpty()) {
					Attribute att = new Attribute();
					att.setKey(zxx[0]);
					att.setValue(zxx[1]);
					lsa.add(att);
				}
			}
			p.setAttributes(lsa);
		}
		fromXmlEnd(node, p);
		return p;
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		Person test = (Person) obj;
		boolean ret = true;
		ret = ret && equalsStringNullValue(getAbbreviation(), test.getAbbreviation());
		ret = ret && equalsStringNullValue(getFirstname(), test.getFirstname());
		ret = ret && equalsStringNullValue(getLastname(), test.getLastname());
		ret = ret && equalsObjectNullValue(getGender(), test.getGender());
		ret = ret && equalsDateNullValue(getBirthday(), test.getBirthday());
		ret = ret && equalsDateNullValue(getDayofdeath(), test.getDayofdeath());
		ret = ret && equalsIntegerNullValue(getColor(), test.getColor());
		ret = ret && equalsObjectNullValue(getCategory(), test.getCategory());
		ret = ret && equalsStringNullValue(getNotes(), test.getNotes());
		ret = ret && equalsListNullValue(getAttributes(), test.getAttributes());
		return ret;
	}

	@Override
	public int hashCode() {
		return hashPlus(super.hashCode(),
				abbreviation,
				firstname,
				lastname,
				gender,
				birthday,
				dayofdeath,
				color,
				category,
				categories,
				occupation,
				attributes);
	}

	@Override
	public int compareTo(AbstractEntity en) {
		Person o = (Person) en;
		if (category == null && o == null) {
			return 0;
		}
		if (category != null && o.getCategory() == null) {
			return -1;
		}
		if (o.getCategory() != null && category == null) {
			return -1;
		}
		int cmp = category.getSort().compareTo(o.getCategory().getSort());
		if (cmp == 0) {
			return getFullName().compareTo(o.getFullName());
		}
		return cmp;
	}

	public static Person find(List<Person> list, String str) {
		for (Person elem : list) {
			if (elem.getName().equals(str)) {
				return (elem);
			}
		}
		return (null);
	}

	public static Person find(List<Person> list, Long id) {
		for (Person elem : list) {
			if (elem.id.equals(id)) {
				return (elem);
			}
		}
		return (null);
	}

	public static List<String> getDefColumns() {
		List<String> list = AbstractEntity.getDefColumns(Book.TYPE.PERSON);
		list.add("firstname, 256");
		list.add("lastname, 256");
		list.add("abbreviation, 256");
		list.add("occupation, 256");
		return (list);
	}

	public static List<String> getTable() {
		List<String> ls = new ArrayList<>();
		String tableName = "person";
		AbstractEntity.getTable(tableName, ls);
		ls.add(tableName + ",gender_id,Integer,0");
		ls.add(tableName + ",firstname,String,256");
		ls.add(tableName + ",lastname,String,256");
		ls.add(tableName + ",abbreviation,String,256");
		ls.add(tableName + ",birthday,Date,0");
		ls.add(tableName + ",dayofdeath,Date,0");
		ls.add(tableName + ",occupation,String,256");
		ls.add(tableName + ",color,Integer,0");
		ls.add(tableName + ",category_id,Integer,0");
		ls.add(tableName + ",categories,Table.Category,0");
		ls.add(tableName + ",attributes,Table.Attribute,0");
		return (ls);
	}

	@Override
	public AbstractEntity copyTo(MainFrame m) {
		Person ne = new Person();
		doCopyTo(m, ne);
		for (Attribute n : getAttributes()) {
			ne.attributes.add(n);
		}
		ne.setBirthday(getBirthday());
		for (Category n : getCategories()) {
			ne.categories.add(n);
		}
		ne.setCategory(getCategory());
		ne.setColor(getColor());
		ne.setDayofdeath(getDayofdeath());
		ne.setFirstname(getFirstname());
		ne.setGender(getGender());
		ne.setLastname(getLastname());
		ne.setOccupation(getOccupation());
		return ne;
	}

}
