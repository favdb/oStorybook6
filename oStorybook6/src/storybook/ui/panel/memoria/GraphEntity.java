/*
 * Copyright (C) 2020 favdb
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
package storybook.ui.panel.memoria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import resources.icons.IconUtil;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.person.Person;
import storybook.db.plot.Plot;
import storybook.db.relation.Relation;
import storybook.db.scene.Scene;
import storybook.db.scene.Scenes;
import storybook.db.strand.Strand;
import storybook.db.tag.Tag;

/**
 * create a graph
 *
 * @author favdb
 */
public class GraphEntity {

	private static final String TT = "GraphEntity";

	AbstractEntity vertex;
	MemoriaPanel memoria;
	boolean isOk = false;
	int defNormal = IconUtil.getDefSize() * 2, defSub = defNormal / 2, defMaster = defNormal * 2;

	/**
	 * create a graph for the given Entity
	 *
	 * @param m
	 * @param entity
	 */
	public GraphEntity(MemoriaPanel m, AbstractEntity entity) {
		this.memoria = m;
		this.vertex = null;
		create(entity);
	}

	public void initVertex(AbstractEntity entity) {
		//LOG.trace(TT + ".initVertex(" + AbstractEntity.trace(entity)
		// + ") to " + vertex.getObjType());
		vertex.setName(" ");
		memoria.graph.addVertex(vertex);
		memoria.labelMap.put(vertex, vertex.getName());
		memoria.iconMap.put(vertex, vertex.getIcon(10));
		if (entity != null) {
			memoria.graph.addEdge(memoria.graphIndex++, entity, vertex);
		}
		isOk = true;
	}

	public void initMaster(AbstractEntity entity) {
		//LOG.trace(TT + ".initMaster(" + AbstractEntity.trace(entity) + ")");
		memoria.graphIndex = 0L;
		if (!isInGraph(entity)) {
			memoria.graph.addVertex(entity);
			memoria.labelMap.put(entity, entity.getName());
			memoria.iconMap.put(entity, entity.getIcon(64));
		}
	}

	private AbstractEntity initSub(AbstractEntity master, Book.TYPE type) {
		AbstractEntity sub = EntityUtil.createNewEntity(type);
		sub.setName(" ");
		if (!isInGraph(sub)) {
			memoria.graph.addVertex(sub);
			memoria.labelMap.put(sub, " ");
			memoria.iconMap.put(sub, sub.getIcon(10));
			memoria.graph.addEdge(memoria.graphIndex++, master, sub);
		}
		return sub;
	}

	public void add(AbstractEntity master, AbstractEntity entity) {
		if (entity != null) {
			//LOG.trace(TT + ".add(master " + AbstractEntity.trace(master)
			// + ", entity " + AbstractEntity.trace(entity) + ")");
			//add the entity vertex only if not exists in the graph
			if (!isInGraph(entity)) {
				memoria.graph.addVertex(entity);
				memoria.labelMap.put(entity, entity.getName());
				int sz = defNormal;
				if (master != null && master.equals(vertex)) {
					sz = defMaster;
				}
				memoria.iconMap.put(entity, entity.getIcon(sz));
			}
			//add the edge beetwen the master and this entity
			if (master != null && isInGraph(master)) {
				memoria.graph.addEdge(memoria.graphIndex++, master, entity);
			}
		}
	}

	public boolean isInGraph(AbstractEntity entity) {
		if (entity == null) {
			return false;
		}
		Collection collection = memoria.graph.getVertices();
		for (Object obj : collection) {
			if (obj instanceof AbstractEntity) {
				try {
					AbstractEntity e = (AbstractEntity) obj;
					if (e.getObjType().equals(entity.getObjType())
					   && e.getId().equals(entity.getId())) {
						return true;
					}
				} catch (Exception ex) {

				}
			}
		}
		return false;
	}

	private void create(AbstractEntity entity) {
		switch (entity.getObjType()) {
			case ITEM:
				createItem(entity);
				break;
			case LOCATION:
				createLocation(entity);
				break;
			case PERSON:
				createPerson(entity);
				break;
			case PLOT:
				createPlot(entity);
				break;
			case RELATION:
				createRelationship(entity);
				break;
			case SCENE:
				createScene(entity);
				break;
			case STRAND:
				createStrand(entity);
				break;
			default:
				break;
		}
	}

	public void createItem(AbstractEntity entity) {
		if (entity != null) {
			initMaster(entity);
			List<Scene> scenes = memoria.mainFrame.project.scenes.findBy((Item) entity);
			addScenes(entity, scenes);
			addSceneItems(entity, scenes);
			addSceneLocations(entity, scenes);
			addScenePersons(entity, scenes);
			addScenePlots(entity, scenes);
			addSceneStrands(entity, scenes);
			addRelations((Item) entity);
		}
	}

	void createLocation(AbstractEntity entity) {
		if (entity != null) {
			initMaster(entity);
			List<Scene> scenes = memoria.mainFrame.project.scenes.findBy((Location) entity);
			addScenes(entity, scenes);
			addSceneItems(entity, scenes);
			addSceneLocations(entity, scenes);
			addScenePersons(entity, scenes);
			addScenePlots(entity, scenes);
			addSceneStrands(entity, scenes);
			addRelations((Location) entity);
		}
	}

	void createPerson(AbstractEntity entity) {
		if (entity != null) {
			initMaster(entity);
			List<Scene> scenes = memoria.mainFrame.project.scenes.findBy((Person) entity);
			addScenes(entity, scenes);
			addSceneItems(entity, scenes);
			addSceneLocations(entity, scenes);
			addScenePersons(entity, scenes);
			addScenePlots(entity, scenes);
			addSceneStrands(entity, scenes);
			addRelations((Person) entity);
		}
	}

	void createPlot(AbstractEntity entity) {
		//LOG.trace(TT+".createPlot("+AbstractEntity.trace(entity)+")");
		if (entity != null) {
			initMaster(entity);
			List<Scene> scenes = memoria.mainFrame.project.scenes.findBy((Plot) entity);
			addScenes(entity, scenes);
			addSceneItems(entity, scenes);
			addSceneLocations(entity, scenes);
			addScenePersons(entity, scenes);
			addScenePlots(entity, scenes);
			addSceneStrands(entity, scenes);
		}
	}

	void createRelationship(AbstractEntity entity) {
		if (entity == null) {
			return;
		}
		initMaster((Relation) entity);
		List<Scene> scenes = memoria.mainFrame.project.scenes.findBy((Relation) entity);
		addScenes(entity, scenes);
		addItems(entity, ((Relation) entity).getItems());
		addLocations(entity, ((Relation) entity).getLocations());
		addPersons(entity, ((Relation) entity).getPersons());
	}

	public void createScene(AbstractEntity entity) {
		//LOG.trace(TT+".createScene("+AbstractEntity.trace(entity)+")");
		if (entity == null) {
			return;
		}
		initMaster(entity);
		Scene scene = (Scene) entity;
		List<Scene> scenes = new ArrayList<>();
		List<Scene> sx = Scenes.find(memoria.mainFrame);
		int idx = sx.indexOf(scene);
		if (idx > 0) {
			scenes.add(sx.get(idx - 1));
		}
		if (idx < sx.size() - 2) {
			scenes.add(sx.get(idx + 1));
		}
		addScenes(entity, scenes);
		addItems(entity, scene.getItems());
		addLocations(entity, scene.getLocations());
		addPersons(entity, scene.getPersons());
		addPlots(entity, scene.getPlots());
		List<Strand> strands = new ArrayList<>();
		if (scene.getStrand() != null) {
			strands.add(scene.getStrand());
		}
		for (Strand s : scene.getStrands()) {
			if (!strands.contains(s)) {
				strands.add(s);
			}
		}
		addStrands(entity, strands);
	}

	public void createStrand(AbstractEntity entity) {
		if (entity != null) {
			initMaster(entity);
			List<Scene> scenes = memoria.mainFrame.project.scenes.findBy((Strand) entity);
			addScenes(entity, scenes);
			addSceneItems(entity, scenes);
			addSceneLocations(entity, scenes);
			addScenePersons(entity, scenes);
			addScenePlots(entity, scenes);
		}
	}

	public void createTag(AbstractEntity entity) {
		if (entity != null) {
			initMaster(entity);
			List<Scene> scenes = memoria.mainFrame.project.scenes.findBy((Tag) entity);
			addScenes(entity, scenes);
		}
	}

	public void addScenes(AbstractEntity master, List<Scene> scenes) {
		//LOG.trace(TT + ".addScenes(scenes nb=" + scenes.size() + ", " + AbstractEntity.trace(master) + ")");
		if (!scenes.isEmpty()) {
			AbstractEntity sub = initSub(master, Book.TYPE.SCENE);
			for (Scene scene : scenes) {
				add(sub, scene);
			}
		}
	}

	public void addSceneItems(AbstractEntity master, List<Scene> scenes) {
		//LOG.trace(TT + ".addSceneItems(" + AbstractEntity.trace(master) + ", scenes nb=" + scenes.size() + ")");
		List<Item> list = new ArrayList<>();
		for (Scene scene : scenes) {
			for (Item p : scene.getItems()) {
				if (!list.contains(p)) {
					list.add(p);
				}
			}
		}
		if (master instanceof Item) {
			list.remove((Item) master);
		}
		addItems(master, list);
	}

	public void addItems(AbstractEntity master, List<Item> items) {
		//LOG.trace(TT + ".addItems(" + AbstractEntity.trace(master) + ", items nb=" + items.size() + ")");
		if (!items.isEmpty()) {
			AbstractEntity sub = initSub(master, Book.TYPE.ITEM);
			for (Item item : items) {
				add(sub, item);
			}
		}
	}

	public void addSceneLocations(AbstractEntity master, List<Scene> scenes) {
		//LOG.trace(TT + ".addSceneLocations(" + AbstractEntity.trace(master) + ", scenes nb=" + scenes.size() + ")");
		List<Location> list = new ArrayList<>();
		for (Scene scene : scenes) {
			for (Location p : scene.getLocations()) {
				if (!list.contains(p)) {
					list.add(p);
				}
			}
		}
		if (master instanceof Location) {
			list.remove((Location) master);
		}
		addLocations(master, list);
	}

	public void addLocations(AbstractEntity master, List<Location> locations) {
		//LOG.trace(TT + ".addLocations(" + AbstractEntity.trace(master) + ", locations nb=" + locations.size() + ")");
		if (!locations.isEmpty()) {
			AbstractEntity sub = initSub(master, Book.TYPE.LOCATION);
			for (Location location : locations) {
				add(sub, location);
			}
		}
	}

	public void addScenePersons(AbstractEntity master, List<Scene> scenes) {
		//LOG.trace(TT + ".addScenePersons(" + AbstractEntity.trace(master) + ", scenes nb=" + scenes.size() + ")");
		List<Person> persons = new ArrayList<>();
		for (Scene scene : scenes) {
			for (Person p : scene.getPersons()) {
				if (!persons.contains(p)) {
					persons.add(p);
				}
			}
		}
		if (master instanceof Person) {
			persons.remove((Person) master);
		}
		addPersons(master, persons);
	}

	public void addPersons(AbstractEntity master, List<Person> persons) {
		//LOG.trace(TT + ".addPersons(" + AbstractEntity.trace(master) + ", persons nb=" + persons.size() + ")");
		if (!persons.isEmpty()) {
			AbstractEntity sub = initSub(master, Book.TYPE.PERSON);
			for (Person person : persons) {
				if (person != null) {
					add(sub, person);
				}
			}
		}
	}

	public void addScenePlots(AbstractEntity master, List<Scene> scenes) {
		//LOG.trace(TT + ".addScenePlots(" + AbstractEntity.trace(master) + ", scenes nb=" + scenes.size() + ")");
		List<Plot> list = new ArrayList<>();
		for (Scene scene : scenes) {
			for (Plot p : scene.getPlots()) {
				if (!list.contains(p)) {
					list.add(p);
				}
			}
		}
		if (master instanceof Plot) {
			list.remove((Plot) master);
		}
		addPlots(master, list);
	}

	public void addPlots(AbstractEntity master, List<Plot> plots) {
		//LOG.trace(TT + ".addPlots(" + AbstractEntity.trace(master) + ", plots nb=" + plots.size() + ")");
		if (!plots.isEmpty()) {
			AbstractEntity sub = initSub(master, Book.TYPE.PLOT);
			for (Plot plot : plots) {
				add(sub, plot);
			}
		}
	}

	public void addRelations(AbstractEntity entity) {
		//LOG.trace(TT + ".addRelations(" + AbstractEntity.trace(entity) + ")");
		List<Relation> relations = memoria.mainFrame.project.relations.find(entity);
		if (!relations.isEmpty()) {
			AbstractEntity sub = initSub(entity, Book.TYPE.RELATION);
			for (Relation relation : relations) {
				add(sub, relation);
			}
		}
	}

	public void addSceneStrands(AbstractEntity master, List<Scene> scenes) {
		//LOG.trace(TT + ".addSceneStrands(" + AbstractEntity.trace(master) + ", scenes nb=" + scenes.size() + ")");
		List<Strand> list = new ArrayList<>();
		for (Scene scene : scenes) {
			if (scene.getStrand() != null) {
				list.add(scene.getStrand());
			}
			for (Strand s : scene.getStrands()) {
				if (!list.contains(s)) {
					list.add(s);
				}
			}
		}
		if (master instanceof Strand) {
			list.remove((Strand) master);
		}
		addStrands(master, list);
	}

	public void addStrands(AbstractEntity master, List<Strand> strands) {
		//LOG.trace(TT + ".addStrands(" + AbstractEntity.trace(master) + ", strands nb=" + strands.size() + ")");
		if (!strands.isEmpty()) {
			AbstractEntity sub = initSub(master, Book.TYPE.STRAND);
			for (Strand strand : strands) {
				add(sub, strand);
			}
		}
	}

}
