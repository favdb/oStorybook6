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

	private static final String TT = "Chapters.";

	private final List<Chapter> chapters = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Chapters(Project project) {
		super(project);
	}

	/**
	 * get the last Chapter ID
	 *
	 * @return
	 */
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

	/**
	 * save the given Chapter in the list of Chapter
	 *
	 * @param entity
	 */
	@Override
	public void save(AbstractEntity entity) {
		//LOG.trace(TT + "save(entity=" + LOG.trace(entity) + ")");
		if (entity.getId() < 0) {
			entity.setId(getLast() + 1);
			chapters.add((Chapter) entity);
		} else {
			chapters.set(getIdx(entity.getId()), (Chapter) entity);
		}
	}

	/**
	 * get the index of the given Chapter
	 *
	 * @param id
	 * @return
	 */
	@Override
	public int getIdx(Long id) {
		for (Chapter p : chapters) {
			if (p.getId().equals(id)) {
				return chapters.indexOf(p);
			}
		}
		return -1;
	}

	/**
	 * get the Chapter for the given ID
	 *
	 * @param id
	 * @return
	 */
	@Override
	public Chapter get(Long id) {
		for (Chapter p : chapters) {
			if (p.getId().equals(id)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * get the last Chapter
	 *
	 * @return
	 */
	public Chapter getLastChapter() {
		if (chapters == null) {
			return null;
		}
		return chapters.get(chapters.size() - 1);
	}

	/**
	 * add the given Chapter to the list of chapters
	 *
	 * @param p
	 */
	@Override
	public void add(AbstractEntity p) {
		if (p.getId() == -1L) {
			p.setId(getLast() + 1L);
		}
		chapters.add((Chapter) p);
	}

	/**
	 * remove the given Chapter from the list of chapters
	 *
	 * @param p
	 */
	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			chapters.remove((Chapter) p);
		}
	}

	/**
	 * get the first Chapter
	 *
	 * @return
	 */
	@Override
	public AbstractEntity getFirst() {
		if (chapters.isEmpty()) {
			return null;
		}
		return chapters.get(0);
	}

	/**
	 * get the list of chapters
	 *
	 * @return
	 */
	@Override
	public List getList() {
		return chapters;
	}

	/**
	 * get number of chapters
	 *
	 * @return
	 */
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
		if (part == null) {
			ls = chapters;
		} else {
			for (Chapter p : chapters) {
				if (p.hasPart() && p.getPart().equals(part)) {
					ls.add(p);
				} else if (part == null) {
					ls.add(p);
				}
			}
		}
		if (ls.size() > 1) {
			Collections.sort(ls, (Chapter r1, Chapter r2)
					-> r1.getChapterno().compareTo(r2.getChapterno()));
		}
		return ls;
	}

	/**
	 * find all unassigned Chapter, sorted by number
	 *
	 * @return
	 */
	public List<Chapter> findNoPart() {
		List<Chapter> ls = new ArrayList<>();
		for (Chapter p : chapters) {
			if (!p.hasPart()) {
				ls.add(p);
			}
		}
		if (ls.size() > 1) {
			Collections.sort(ls, (Chapter r1, Chapter r2)
					-> r1.getChapterno().compareTo(r2.getChapterno()));
		}
		return ls;
	}

	/**
	 * find first Chapter of the given Part
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

	/**
	 * set the link to Part of the chapters
	 */
	@Override
	public void setLinks() {
		for (Chapter p : chapters) {
			if (p.getPartId() != -1L) {
				p.setPart(project.parts.get(p.getPartId()));
			}
		}
	}

	/**
	 * get a Xml String of the chapters
	 *
	 * @return
	 */
	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Chapter p : chapters) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	/**
	 * get the next number for a Chapter
	 *
	 * @return
	 */
	public Integer getNextNumber() {
		int rc = 0;
		for (Chapter p : chapters) {
			if (p.getChapterno() > rc) {
				rc = p.getChapterno();
			}
		}
		return rc + 1;
	}

	/**
	 * insert the given chapter before the given target chapter
	 *
	 * @param chapter
	 * @param target
	 */
	public void insertBefore(Chapter chapter, Chapter target) {
		int num = target.getChapterno();
		chapter.setChapterno(num++);
		//renumber others
		List<Chapter> cc = find(target.getPart());
		if (!cc.isEmpty()) {
			Collections.sort(cc, (Chapter r1, Chapter r2)
					-> r1.getChapterno().compareTo(r2.getChapterno()));
			int idx = 0;
			for (int i = 0; i < cc.size(); i++) {
				if (cc.get(i).equals(target)) {
					idx = i;
				}
			}
			for (int i = idx; i < cc.size(); i++) {
				cc.get(i).setChapterno(num++);
			}
		}
	}

	/**
	 * insert the given chapter into the given part as last one of this part
	 *
	 * @param chapter
	 * @param part
	 */
	public void insertInto(Chapter chapter, Part part) {
		List<Chapter> cc = (part == null ? findNoPart() : find(part));
		int num = 0;
		if (!cc.isEmpty()) {
			num = cc.get(cc.size() - 1).getChapterno();
		}
		chapter.setPart((part == null || part.getId() == -1L ? null : part));
		chapter.setChapterno(num + 1);
	}

	/**
	 * change the HTML link of the chapters
	 *
	 * @param path
	 */
	@Override
	public void changeHtmlLinks(String path) {
		//empty
	}

	/**
	 * sort the chapters by chapter number
	 */
	public void sortByNumber() {
		Collections.sort(chapters, (Chapter r1, Chapter r2) -> r1.getChapterno().compareTo(r2.getChapterno()));
	}

}
