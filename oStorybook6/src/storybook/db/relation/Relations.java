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
package storybook.db.relation;

import i18n.I18N;
import java.util.ArrayList;
import java.util.List;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import static storybook.db.book.Book.TYPE.ITEM;
import static storybook.db.book.Book.TYPE.LOCATION;
import static storybook.db.book.Book.TYPE.PERSON;
import static storybook.db.book.Book.TYPE.SCENE;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.person.Person;
import storybook.db.scene.Scene;
import storybook.project.Project;
import storybook.tools.html.Html;

/**
 * utilities for Relation
 *
 * @author favdb
 */
public class Relations extends AbsEntitys {

	private final List<Relation> relations = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Relations(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Relation p : relations) {
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
			relations.add((Relation) entity);
		} else {
			relations.set(getIdx(entity.getId()), (Relation) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Relation p : relations) {
			if (p.getId().equals(id)) {
				return relations.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Relation get(Long id) {
		for (Relation p : relations) {
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
		relations.add((Relation) p);
	}

	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			relations.remove((Relation) p);
		}
	}

	@Override
	public AbstractEntity getFirst() {
		if (relations.isEmpty()) {
			return null;
		}
		return relations.get(0);
	}

	@Override
	public List getList() {
		return relations;
	}

	@Override
	public int getCount() {
		return relations.size();
	}

	@SuppressWarnings("unchecked")
	public List<Relation> findByScene(Scene scene) {
		List<Relation> ls = new ArrayList<>();
		for (Relation rel : relations) {
			if ((rel.getStartScene() != null && rel.getStartScene().equals(scene))
			   || (rel.getEndScene() != null && rel.getEndScene().equals(scene))) {
				ls.add(rel);
			}
		}
		return ls;
	}

	@SuppressWarnings("unchecked")
	public List<Relation> findByItems(Item item) {
		List<Relation> ls = new ArrayList<>();
		for (Relation rel : relations) {
			if (rel.getItems().contains(item)) {
				ls.add(rel);
			}
		}
		return ls;
	}

	@SuppressWarnings("unchecked")
	public List<Relation> findByLocations(Location location) {
		List<Relation> ls = new ArrayList<>();
		for (Relation rel : relations) {
			if (rel.getLocations().contains(location)) {
				ls.add(rel);
			}
		}
		return ls;
	}

	@SuppressWarnings("unchecked")
	public List<Relation> findByPersons(Person person) {
		List<Relation> ls = new ArrayList<>();
		for (Relation rel : relations) {
			if ((rel.getPersons() != null && rel.getPersons().contains(person))) {
				ls.add(rel);
			}
		}
		return ls;
	}

	/**
	 * find Relation linked with the given Entity
	 *
	 * @param entity
	 * @return
	 */
	public List<Relation> find(AbstractEntity entity) {
		List<Relation> ls = new ArrayList<>();
		switch (entity.getObjType()) {
			case ITEM:
				ls = findByItems((Item) entity);
				break;
			case LOCATION:
				ls = findByLocations((Location) entity);
				break;
			case PERSON:
				ls = findByPersons((Person) entity);
				break;
			case SCENE:
				ls = findByScene((Scene) entity);
				break;
			default:
				break;
		}
		return ls;
	}

	/**
	 * get the tool tip for the given Relation
	 *
	 * @param buf
	 * @param relation
	 */
	public static void tooltip(StringBuilder buf, Relation relation) {
		List<Person> persons = relation.getPersons();
		if (persons != null && !persons.isEmpty()) {
			buf.append(Html.P_B);
			buf.append(Html.intoB(Html.intoI(I18N.getColonMsg("persons")))).append(" ");
			for (Person p : persons) {
				buf.append(p.getAbbr()).append(" ");
			}
			buf.append(Html.P_E);
		}
		List<Item> items = relation.getItems();
		if (items != null && !items.isEmpty()) {
			buf.append(Html.P_B);
			buf.append(Html.intoB(Html.intoI(I18N.getColonMsg("items")))).append(" ");
			for (Item p : items) {
				buf.append(p.getName()).append(" ");
			}
			buf.append(Html.P_E);
		}
		List<Location> locations = relation.getLocations();
		if (locations != null && !locations.isEmpty()) {
			buf.append(Html.P_B);
			buf.append(Html.intoB(Html.intoI(I18N.getColonMsg("locations")))).append(" ");
			for (Location p : locations) {
				buf.append(p.getName()).append(" ");
			}
			buf.append(Html.P_E);
		}
	}

	@Override
	public void setLinks() {
		for (Relation p : relations) {
			if (p.getStartSceneId() != -1L) {
				p.setStartScene(project.scenes.get(p.getStartSceneId()));
			}
			if (p.getEndSceneId() != -1L) {
				p.setEndScene(project.scenes.get(p.getEndSceneId()));
			}
			if (!p.getPersonsId().isEmpty()) {
				for (Long v : p.getPersonsId()) {
					p.getPersons().add(project.persons.get(v));
				}
			}
			if (!p.getItemsId().isEmpty()) {
				for (Long v : p.getItemsId()) {
					p.getItems().add((Item) project.items.get(v));
				}
			}
			if (!p.getLocationsId().isEmpty()) {
				for (Long v : p.getItemsId()) {
					p.getLocations().add(project.locations.get(v));
				}
			}
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Relation p : relations) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Relation p : relations) {
			p.changeHtmlLinks(path);
		}
	}

}
