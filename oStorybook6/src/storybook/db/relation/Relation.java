/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2015 FaVdB

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
package storybook.db.relation;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import storybook.db.DB.DATA;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.person.Person;
import storybook.db.scene.Scene;
import storybook.tools.ListUtil;
import storybook.tools.Period;
import storybook.tools.xml.XmlKey.XK;
import storybook.tools.xml.XmlUtil;
import storybook.ui.MainFrame;

public class Relation extends AbstractEntity {

	Scene startScene = null, endScene = null;
	private List<Person> persons = new ArrayList<>();
	private List<Long> persons_id = new ArrayList<>();
	private List<Item> items = new ArrayList<>();
	private List<Long> items_id = new ArrayList<>();
	private List<Location> locations = new ArrayList<>();
	private List<Long> locations_id = new ArrayList<>();
	private Long startScene_id = -1L, endScene_id = -1L;

	public Relation() {
		super(Book.TYPE.RELATION, "110");
	}

	public Relation(Person person1, Person person2, String description, Scene startScene, Scene endScene, String notes) {
		this();
		this.startScene = startScene;
		this.endScene = endScene;
		setDescription(description);
		setNotes(notes);
	}

	public boolean hasStartScene() {
		return this.startScene != null;
	}

	public Scene getStartScene() {
		return this.startScene;
	}

	public void setStartScene(Scene startScene) {
		this.startScene = startScene;
	}

	public Long getStartSceneId() {
		return this.startScene_id;
	}

	public void setStartSceneId(Long value) {
		this.startScene_id = value;
	}

	public boolean hasEndScene() {
		return endScene != null;
	}

	public Scene getEndScene() {
		return this.endScene;
	}

	public void setEndScene(Scene endScene) {
		this.endScene = endScene;
	}

	public Long getEndSceneId() {
		return this.endScene_id;
	}

	public void setEndSceneId(Long value) {
		this.endScene_id = value;
	}

	public List<Long> getPersonsId() {
		return this.persons_id;
	}

	public void setPersonsId(List<Long> value) {
		this.persons_id.addAll(value);
	}

	public List<Person> getPersons() {
		return this.persons;
	}

	public String getPersonList() {
		List<String> lst = new ArrayList<>();
		for (Person p : persons) {
			lst.add(p.getName());
		}
		return (ListUtil.join(lst));
	}

	public void setPersons(List<Person> persons) {
		this.persons = persons;
	}

	public int numberOfPersons() {
		return (persons.size());
	}

	public boolean hasItems() {
		if (items == null) {
			return (false);
		}
		return (!items.isEmpty());
	}

	public List<Item> getItems() {
		return items;
	}

	public String getItemList() {
		StringBuilder list = new StringBuilder();
		for (Item item : items) {
			list.append(item.getName()).append(",");
		}
		return (list.toString());
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public int numberOfItems() {
		return (items.size());
	}

	public List<Long> getItemsId() {
		return this.items_id;
	}

	public void setItemsId(List<Long> value) {
		this.items_id.addAll(value);
	}

	public boolean hasLocations() {
		if (locations == null) {
			return (false);
		}
		return (!locations.isEmpty());
	}

	public List<Location> getLocations() {
		return locations;
	}

	public String getLocationList() {
		StringBuilder list = new StringBuilder();
		for (Location loc : locations) {
			list.append(loc.getName()).append(",");
		}
		return (list.toString());
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	public int numberOfLocations() {
		return (locations.size());
	}

	public List<Long> getLocationsId() {
		return this.locations_id;
	}

	public void setLocationsId(List<Long> value) {
		this.locations_id.addAll(value);
	}

	public boolean hasPeriod() {
		return (this.getStartScene() != null && this.getEndScene() != null);
	}

	public Period getPeriod() {
		if (hasPeriod()) {
			return new Period(getStartScene().getScenets(), getEndScene().getScenets());
		}
		if (hasStartScene()) {
			return new Period(getStartScene().getScenets(), getStartScene().getScenets());
		}
		return null;
	}

	@Override
	public String toCsv(String quoteStart, String quoteEnd, String separator) {
		StringBuilder b = new StringBuilder();
		b.append(quoteStart).append(getClean(this)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getName())).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(startScene)).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(endScene)).append(quoteEnd).append(separator);
		b.append(quoteStart);
		for (Person p : getPersons()) {
			b.append(p.getId().toString()).append("/");
		}
		b.append(quoteEnd).append(separator);
		b.append(quoteStart);
		for (Item p : getItems()) {
			b.append(p.getId().toString()).append("/");
		}
		b.append(quoteEnd).append(separator);
		b.append(quoteStart);
		for (Location p : getLocations()) {
			b.append(p.getId().toString()).append("/");
		}
		b.append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getDescription())).append(quoteEnd).append(separator);
		b.append(quoteStart).append(getClean(getNotes())).append(quoteEnd).append("\n");
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
		b.append(getInfo(detailed, DATA.SCENE_START, getStartScene()));
		b.append(getInfo(detailed, DATA.SCENE_END, getEndScene()));
		b.append(getInfo(detailed, DATA.PERSONS, EntityUtil.getNames(persons)));
		b.append(getInfo(detailed, DATA.LOCATIONS, EntityUtil.getNames(locations)));
		b.append(getInfo(detailed, DATA.ITEMS, EntityUtil.getNames(items)));
		b.append(toDetailFooter(detailed));
		return (b.toString());
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		b.append(toXmlBeg());
		if (startScene != null) {
			b.append(XmlUtil.setAttribute(0, XK.START, startScene));
		}
		if (endScene != null) {
			b.append(XmlUtil.setAttribute(0, XK.END, endScene));
		}
		b.append(XmlUtil.setAttribute(8, XK.PERSONS, EntityUtil.getIds(persons)));
		b.append(XmlUtil.setAttribute(8, XK.LOCATIONS, EntityUtil.getIds(locations)));
		b.append(XmlUtil.setAttribute(8, XK.ITEMS, EntityUtil.getIds(items)));
		b.append(">\n");
		b.append(toXmlEnd());
		return b.toString();
	}

	public static Relation fromXml(Node node) {
		Relation p = new Relation();
		fromXmlBeg(node, p);
		p.setStartSceneId(XmlUtil.getLong(node, XK.START));
		p.setEndSceneId(XmlUtil.getLong(node, XK.END));
		p.setItemsId(XmlUtil.getLongList(node, XK.ITEMS));
		p.setLocationsId(XmlUtil.getLongList(node, XK.LOCATIONS));
		p.setPersonsId(XmlUtil.getLongList(node, XK.PERSONS));
		fromXmlEnd(node, p);
		return p;
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		return (this.toXml().equals(((Relation) obj).toXml()));
	}

	@Override
	public int hashCode() {
		return hashPlus(super.hashCode(),
				startScene,
				endScene,
				persons,
				items,
				locations);
	}

	public static Relation find(List<Relation> list, String str) {
		for (Relation elem : list) {
			if (elem.getName().equals(str)) {
				return (elem);
			}
		}
		return (null);
	}

	public static Relation find(List<Relation> list, Long id) {
		for (Relation elem : list) {
			if (elem.id.equals(id)) {
				return (elem);
			}
		}
		return (null);
	}

	public static List<String> getDefColumns() {
		List<String> list = AbstractEntity.getDefColumns(Book.TYPE.RELATION);
		return (list);
	}

	public static List<String> getTable() {
		List<String> ls = new ArrayList<>();
		String tableName = "relationship";
		AbstractEntity.getTable(tableName, ls);
		ls.add(tableName + ",person1_id,Integer,0");
		ls.add(tableName + ",person2_id,Integer,0");
		ls.add(tableName + ",start_scene_id,Integer,0");
		ls.add(tableName + ",end_scene_id,Integer,0");
		ls.add(tableName + ",persons,Table.Person,0");
		ls.add(tableName + ",locations,Table.Location,0");
		ls.add(tableName + ",items,Table.Item,0");
		return (ls);
	}

	@Override
	public AbstractEntity copyTo(MainFrame m) {
		Relation ne = new Relation();
		doCopyTo(m, ne);
		ne.setEndScene(getEndScene());
		for (Item n : getItems()) {
			ne.items.add(n);
		}
		for (Location n : getLocations()) {
			ne.locations.add(n);
		}
		for (Person n : getPersons()) {
			ne.persons.add(n);
		}
		ne.setStartScene(getStartScene());
		return ne;
	}

}
