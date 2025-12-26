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
package storybook.db.person;

import i18n.I18N;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.JCheckBox;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import storybook.db.attribute.Attribute;
import storybook.db.category.Category;
import storybook.db.gender.Gender;
import storybook.project.Project;
import storybook.tools.TextUtil;
import storybook.tools.html.Html;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class Persons extends AbsEntitys {

	private static final String TT = "Persons.";

	private final List<Person> persons = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Persons(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Person p : persons) {
			if (n < p.getId()) {
				n = p.getId();
			}
		}
		return n;
	}

	@Override
	public void save(AbstractEntity entity) {
		if (entity.getId() < 0) {
			entity.setId(getLast() + 1);
			persons.add((Person) entity);
		} else {
			persons.set(getIdx(entity.getId()), (Person) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Person p : persons) {
			if (p.getId().equals(id)) {
				return persons.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Person get(Long id) {
		for (Person p : persons) {
			if (p.getId().equals(id)) {
				return p;
			}
		}
		return null;
	}

	@Override
	public void add(AbstractEntity p) {
		if (p.getId() == -1L) {
			p.setId(getLast() + 1L);
		}
		persons.add((Person) p);
	}

	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			persons.remove((Person) p);
		}
	}

	@Override
	public AbstractEntity getFirst() {
		if (persons.isEmpty()) {
			return null;
		}
		return persons.get(0);
	}

	@Override
	public List getList() {
		return persons;
	}

	/**
	 * sort by Id
	 */
	@Override
	public void sortById() {
		Collections.sort(persons, (Person r1, Person r2) -> r1.getId().compareTo(r2.getId()));
	}

	@Override
	public int getCount() {
		return persons.size();
	}

	/**
	 * find all chapters sorted by name
	 *
	 * @return
	 */
	public List findByName() {
		List<Person> ls = new ArrayList<>();
		for (Person p : persons) {
			ls.add(p);
		}
		Collections.sort(ls, (Person r1, Person r2)
				-> r1.getName().compareTo(r2.getName()));
		return ls;
	}

	/**
	 * find Chapter for the given name
	 *
	 * @param name
	 * @return
	 */
	public Person findName(String name) {
		for (Person p : persons) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	public List<String> findAllInList() {
		List<String> ls = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<Person> lsi = getList();
		for (Person p : lsi) {
			ls.add(p.getFullNameAbbr());
		}
		return ls;
	}

	@SuppressWarnings("unchecked")
	public List<Person> findByCategory(Category category) {
		List<Person> ls = new ArrayList<>();
		for (Person p : persons) {
			if (p.getCategory() != null && p.getCategory().equals(category)
					|| p.getCategories().contains(category)) {
				ls.add(p);
			}
		}
		return ls;
	}

	public List<Person> findByCategory() {
		@SuppressWarnings("unchecked")
		List<Person> ls = getList();
		Collections.sort(ls, (Person it1, Person it2)
				-> it1.getCategory().compareTo(it2.getCategory()));
		return ls;
	}

	@SuppressWarnings("unchecked")
	public List<Person> findByCategories(List<Category> categories) {
		//LOG.trace(TT + "findByCategories(categories=" + categories.toString() + ")");
		List<Person> ls = new ArrayList<>();
		for (Person p : persons) {
			if (categories.contains(p.getCategory())) {
				ls.add(p);
			}
		}
		return ls;
	}

	@SuppressWarnings("unchecked")
	public List<Person> findByAttribute(Attribute attribute) {
		List<Person> ls = new ArrayList<>();
		for (Person p : persons) {
			if (p.getAttributes().contains(attribute)) {
				ls.add(p);
			}
		}
		return ls;
	}

	@SuppressWarnings("unchecked")
	public List<Person> findByGender(Gender gender) {
		List<Person> ls = new ArrayList<>();
		for (Person p : persons) {
			if (p.getGender() != null && p.getGender().equals(gender)) {
				ls.add(p);
			}
		}
		return ls;
	}

	/**
	 * get a JCheckBox for Persons
	 *
	 * @param mainFrame
	 * @param cbl
	 * @param comp
	 * @return
	 */
	public static List<JCheckBox> cbPersons(MainFrame mainFrame,
			List<JCheckBox> cbl, ActionListener comp) {
		List<JCheckBox> list = new ArrayList<>();
		for (JCheckBox cb : cbl) {
			if (cb.isSelected()) {
				Category category = (Category) cb.getClientProperty("CbCategory");
				List<Person> lsp = mainFrame.project.persons.findByCategory(category);
				for (Person person : lsp) {
					JCheckBox cbPerson = new JCheckBox(person.getFullNameAbbr());
					cbPerson.setOpaque(false);
					cbPerson.putClientProperty("CbPerson", person);
					cbPerson.addActionListener(comp);
					list.add(cbPerson);
				}
			}
		}
		return list;
	}

	/**
	 * get the tool tip for the given Person
	 *
	 * @param buf
	 * @param person
	 * @param date
	 */
	public static void tooltip(StringBuilder buf, Person person) {
		if (person == null) {
			return;
		}
		if (person.getBirthday() != null) {
			buf.append(I18N.getColonMsg("person.age"));
			buf.append(" ");
			buf.append(person.calculateAge(new Date()));
			if (person.isDead(new Date())) {
				buf.append("+");
			}
			buf.append(Html.BR);
		}
		if (person.getGender() != null) {
			buf.append(I18N.getColonMsg("manage.persons.gender"));
			buf.append(" ");
			buf.append(person.getGender());
			buf.append(Html.BR);
		}
		if (person.getCategory() != null) {
			buf.append(I18N.getColonMsg("manage.persons.category"));
			buf.append(" ");
			buf.append(person.getCategory());
			buf.append(Html.BR);
		}
		if (person.getDescription() != null && !person.getDescription().isEmpty()) {
			buf.append(Html.intoP(TextUtil.ellipsize(person.getDescription()), "'margin-top:5px'"));
		}
	}

	@Override
	public void setLinks() {
		for (Person p : persons) {
			if (p.getGenderId() != -1L) {
				p.setGender(project.genders.get(p.getGenderId()));
			}
			if (p.getCategoryId() != -1L) {
				p.setCategory(project.categorys.get(p.getCategoryId()));
			}
			List<Category> lsCat = new ArrayList<>();
			if (!p.getCategoriesId().isEmpty()) {
				for (Long v : p.getCategoriesId()) {
					lsCat.add(project.categorys.get(v));
				}
			}
			p.setCategories(lsCat);
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Person p : persons) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Person p : persons) {
			p.changeHtmlLinks(path);
		}
	}

}
