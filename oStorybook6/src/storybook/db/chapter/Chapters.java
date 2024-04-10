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
package storybook.db.chapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.project.Project;
import storybook.tools.ListUtil;
import storybook.tools.comparator.ObjectComparator;
import storybook.ui.MainFrame;

/**
 * Chapter utilities
 *
 * @author favdb
 */
public class Chapters extends AbsEntitys {

	private final List<Chapter> chapters = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Chapters(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Chapter p : chapters) {
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
			chapters.add((Chapter) entity);
		} else {
			chapters.set(getIdx(entity.getId()), (Chapter) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Chapter p : chapters) {
			if (p.getId().equals(id)) {
				return chapters.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Chapter get(Long id) {
		for (Chapter p : chapters) {
			if (p.getId().equals(id)) {
				return p;
			}
		}
		return null;
	}

	public Chapter getLastChapter() {
		if (chapters == null) {
			return null;
		}
		return chapters.get(chapters.size() - 1);
	}

	@Override
	public void add(AbstractEntity p) {
		if (p.getId() == -1L) {
			p.setId(getLast() + 1L);
		}
		chapters.add((Chapter) p);
	}

	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			chapters.remove((Chapter) p);
		}
	}

	@Override
	public AbstractEntity getFirst() {
		if (chapters.isEmpty()) {
			return null;
		}
		return chapters.get(0);
	}

	@Override
	public List getList() {
		return chapters;
	}

	@Override
	public int getCount() {
		return chapters.size();
	}

	/**
	 * find all chapters sorted by name
	 *
	 * @return
	 */
	public List findByName() {
		List<Chapter> ls = new ArrayList<>();
		for (Chapter p : chapters) {
			ls.add(p);
		}
		Collections.sort(ls, (Chapter r1, Chapter r2)
		   -> r1.getName().compareTo(r2.getName()));
		return ls;
	}

	/**
	 * find Chapter for the given name
	 *
	 * @param name
	 * @return
	 */
	public Chapter findName(String name) {
		for (Chapter p : chapters) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * find the Chapters list of the given Part ordered by number
	 *
	 * @param part
	 * @return
	 */
	public List<Chapter> find(Part part) {
		List<Chapter> ls = new ArrayList<>();
		for (Chapter p : chapters) {
			if (p.hasPart() && p.getPart().equals(part)) {
				ls.add(p);
			} else if (part == null && !p.hasPart()) {
				ls.add(p);
			}
		}
		Collections.sort(ls, (Chapter r1, Chapter r2)
		   -> r1.getChapterno().compareTo(r2.getChapterno()));
		return ls;
	}

	/**
	 * find first Chapterof the given Part
	 *
	 * @param part
	 * @return
	 */
	public Chapter findFirst(Part part) {
		List<Chapter> ls = find(part);
		if (!ls.isEmpty()) {
			return ls.get(0);
		}
		return null;
	}

	/**
	 * find the Chapters list of the given Part ordered by number
	 *
	 * @param part
	 * @return
	 */
	public List<Chapter> findByNumber(Part part) {
		List<Chapter> list = new ArrayList<>();
		for (Chapter p : chapters) {
			if (part == null
			   || (!p.hasPart() && part.equals(p.getPart()))) {
				list.add(p);
			}
		}
		Collections.sort(list, (Chapter r1, Chapter r2)
		   -> r1.getChapterno().compareTo(r2.getChapterno()));
		return list;
	}

	/**
	 * find dates for the given Chapter
	 *
	 * @param mainFrame
	 * @param chapter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Date> findDates(MainFrame mainFrame, Chapter chapter) {
		List<Date> dates = new ArrayList<>();
		List<Scene> scenes = mainFrame.project.scenes.find(chapter);
		for (Scene s : scenes) {
			dates.add(s.getDate());
		}
		dates = ListUtil.setUnique(dates);
		Collections.sort(dates, new ObjectComparator());
		return dates;
	}

	/**
	 * get the last used Chapter number
	 *
	 * @return
	 */
	public int getLastNumber() {
		@SuppressWarnings("unchecked")
		int n = 0;
		for (Chapter chapter : chapters) {
			if ((chapter.getChapterno() != null) && (chapter.getChapterno() > n)) {
				n = chapter.getChapterno();
			}
		}
		return n;
	}

	/**
	 * get the last used Chapter number
	 *
	 * @param project
	 * @return
	 */
	public static int getLastNumber(Project project) {
		@SuppressWarnings("unchecked")
		List<Chapter> list = (List) project.chapters.getList();
		int n = 0;
		for (Chapter chapter : list) {
			if ((chapter.getChapterno() != null) && (chapter.getChapterno() > n)) {
				n = chapter.getChapterno();
			}
		}
		return n;
	}

	@Override
	public void setLinks() {
		for (Chapter p : chapters) {
			if (p.getPartId() != -1L) {
				p.setPart(project.parts.get(p.getPartId()));
			}
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Chapter p : chapters) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	public Integer getNextNumber() {
		int rc = 0;
		for (Chapter p : chapters) {
			if (p.getChapterno() > rc) {
				rc = p.getChapterno();
			}
		}
		return rc + 1;
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Chapter p : chapters) {
			p.changeHtmlLinks(path);
		}
	}

	public void sortByNumber() {
		Collections.sort(chapters, (Chapter r1, Chapter r2) -> r1.getChapterno().compareTo(r2.getChapterno()));
	}

}
