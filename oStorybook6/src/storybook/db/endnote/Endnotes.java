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
package storybook.db.endnote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import storybook.db.scene.Scene;
import storybook.project.Project;

/**
 *
 * @author favdb
 */
public class Endnotes extends AbsEntitys {

	private final List<Endnote> endnotes = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Endnotes(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Endnote p : endnotes) {
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
			endnotes.add((Endnote) entity);
		} else {
			endnotes.set(getIdx(entity.getId()), (Endnote) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Endnote p : endnotes) {
			if (p.getId().equals(id)) {
				return endnotes.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Endnote get(Long id) {
		for (Endnote p : endnotes) {
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
		endnotes.add((Endnote) p);
	}

	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			endnotes.remove((Endnote) p);
		}
	}

	@Override
	public AbstractEntity getFirst() {
		if (endnotes.isEmpty()) {
			return null;
		}
		return endnotes.get(0);
	}

	@Override
	public List getList() {
		return endnotes;
	}

	@Override
	public int getCount() {
		return endnotes.size();
	}

	/**
	 * find an Endnote of the given number in the given Scene
	 *
	 * @param type
	 * @param scene
	 * @param number
	 * @return
	 */
	public Endnote find(int type, Scene scene, int number) {
		for (Endnote p : endnotes) {
			if (p.getType() == type
			   && scene.equals(p.getScene())
			   && number == p.getNumber()) {
				return p;
			}
		}
		return null;
	}

	public List<Endnote> find(int ordinal, Scene scene) {
		List<Endnote> list = new ArrayList<>();
		for (Endnote p : endnotes) {
			if (p.getNumber() == ordinal && scene.equals(p.getScene())) {
				list.add(p);
			}
		}
		return list;
	}

	public List<Endnote> findBySort(int ordinal) {
		List<Endnote> list = new ArrayList<>();
		for (Endnote p : endnotes) {
			if (p.getType() == ordinal) {
				list.add(p);
			}
		}
		return list;
	}

	/**
	 * check if the given Ednote is in the text of the Scene
	 *
	 * @param endnote
	 * @return
	 */
	public boolean check(Endnote endnote) {
		@SuppressWarnings("unchecked")
		Scene scene = endnote.getScene();
		if (scene == null) {
			return false;
		}
		return endnote.isInText();
	}

	public List findByName() {
		List<Endnote> ls = new ArrayList<>();
		for (Endnote p : endnotes) {
			ls.add(p);
		}
		Collections.sort(ls, (Endnote r1, Endnote r2)
		   -> r1.getName().compareTo(r2.getName()));
		return ls;
	}

	public Endnote findName(String name) {
		for (Endnote entity : endnotes) {
			if (entity.getName().equals(name)) {
				return entity;
			}
		}
		return null;
	}

	public List<Endnote> findAll(Scene scene) {
		List<Endnote> ls = new ArrayList<>();
		for (Endnote p : endnotes) {
			if (p.hasScene() && p.getScene().equals(scene)) {
				ls.add(p);
			}
		}
		return ls;
	}

	public int getNextNumber() {
		return getMaxNumber() + 1;
	}

	public int getMaxNumber() {
		int r = 0;
		for (Endnote p : endnotes) {
			if (r == 0 || p.getNumber() > r) {
				r = p.getNumber();
			}
		}
		return r;
	}

	public boolean checkIfNumberExists(int number) {
		for (Endnote p : endnotes) {
			if (p.getNumber() == number) {
				return true;
			}
		}
		return false;
	}

	/**
	 * get the list of Scenes for the given type
	 *
	 * @param type
	 * @return
	 */
	public List<Scene> findScenes(int type) {
		List<Scene> list = new ArrayList<>();
		for (Endnote p : endnotes) {
			if (p.hasScene() && p.getType() == type && !list.contains(p.getScene())) {
				list.add(p.getScene());
			}
		}
		return list;
	}

	/**
	 * find all Endnotes of the given type in the given Scene
	 *
	 * @param type
	 * @param scene
	 *
	 * @return
	 */
	public List<Endnote> find(Endnote.TYPE type, Scene scene) {
		List<Endnote> list = new ArrayList<>();
		if (scene != null) {
			for (Endnote p : endnotes) {
				if (p.getType() == type.ordinal()
				   && scene.equals(p.getScene())) {
					list.add(p);
				}
			}
		}
		return list;
	}

	List<Scene> findScenes(Endnote.TYPE type) {
		List<Scene> list = new ArrayList<>();
		for (Endnote p : endnotes) {
			if (p.getType() == type.ordinal()
			   && p.hasScene()) {
				list.add(p.getScene());
			}
		}
		return list;
	}

	/**
	 * get the first Endnote of given type
	 *
	 * @param scene
	 * @param type
	 * @return
	 */
	public int findFirst(Scene scene, Endnote.TYPE type) {
		if (!endnotes.isEmpty()) {
			return ((Endnote) endnotes.get(0)).getNumber();
		}
		return endnotes.size() + 1;
	}

	/**
	 * find the Endnote by number for the given type
	 *
	 * @param n for the number to find
	 * @param type
	 * @return
	 */
	public Endnote findNumber(int n, int type) {
		if (endnotes == null || endnotes.isEmpty()) {
			return (null);
		}
		for (Endnote p : endnotes) {
			if (p.getType() == type && p.getNumber() == n) {
				return p;
			}
		}
		return null;
	}

	@Override
	public void setLinks() {
		for (Endnote p : endnotes) {
			if (p.getSceneId() != -1L) {
				p.setScene(project.scenes.get(p.getSceneId()));
			}
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Endnote p : endnotes) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Endnote p : endnotes) {
			p.changeHtmlLinks(path);
		}
	}

	public void sortByNumber() {
		Collections.sort(endnotes, (Endnote r1, Endnote r2) -> r1.getNumber().compareTo(r2.getNumber()));
	}

}
