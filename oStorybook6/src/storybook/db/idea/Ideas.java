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
package storybook.db.idea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.project.Project;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class Ideas extends AbsEntitys {

	private final List<Idea> ideas = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Ideas(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Idea p : ideas) {
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
			ideas.add((Idea) entity);
		} else {
			ideas.set(getIdx(entity.getId()), (Idea) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Idea p : ideas) {
			if (p.getId().equals(id)) {
				return ideas.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Idea get(Long id) {
		for (Idea p : ideas) {
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
		ideas.add((Idea) p);
	}

	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			ideas.remove((Idea) p);
		}
	}

	@Override
	public AbstractEntity getFirst() {
		if (ideas.isEmpty()) {
			return null;
		}
		return ideas.get(0);
	}

	@Override
	public List getList() {
		return ideas;
	}

	@Override
	public int getCount() {
		return ideas.size();
	}

	public List findByName() {
		List<Idea> ls = new ArrayList<>();
		for (Idea p : ideas) {
			ls.add(p);
		}
		Collections.sort(ls, (Idea r1, Idea r2)
		   -> r1.getName().compareTo(r2.getName()));
		return ls;
	}

	public Idea findName(String name) {
		for (Idea p : ideas) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * get the next number for Idea
	 *
	 * @param mainFrame
	 * @return
	 */
	public static String getNextNumber(MainFrame mainFrame) {
		List entities = mainFrame.project.getList(Book.TYPE.IDEA);
		Long id = 0L;
		for (Object entity : entities) {
			Idea idea = (Idea) entity;
			if (idea.getId() > id) {
				id = idea.getId();
			}
		}
		id++;
		return (id.toString());
	}

	public List<String> findCategories() {
		List<String> ls = new ArrayList<>();
		for (Idea p : ideas) {
			if (p.getCategory() != null
			   && !p.getCategory().isEmpty()
			   && !ls.contains(p.getCategory())) {
				ls.add(p.getCategory());
			}
		}
		return ls;
	}

	public List<Idea> findAllOrderByState() {
		List<Idea> ls = new ArrayList<>();
		for (Idea p : ideas) {
			ls.add(p);
		}
		Collections.sort(ls, (Idea r1, Idea r2)
		   -> r1.getStatus().compareTo(r2.getStatus()));
		return ls;
	}

	public List<Idea> findAllByStatus(Integer value) {
		List<Idea> ls = new ArrayList<>();
		for (Idea p : ideas) {
			if (Objects.equals(p.getStatus(), value)) {
				ls.add(p);
			}
		}
		return ls;
	}

	public List<Idea> findByUuid(String value) {
		List<Idea> ls = new ArrayList<>();
		for (Idea p : ideas) {
			if (Objects.equals(p.getUuid(), value)) {
				ls.add(p);
			}
		}
		return ls;
	}

	public List<Idea> findByCategory(String value) {
		List<Idea> ls = new ArrayList<>();
		for (Idea p : ideas) {
			if (Objects.equals(p.getCategory(), value)) {
				ls.add(p);
			}
		}
		return ls;
	}

	@Override
	public void setLinks() {
		//empty no links
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Idea p : ideas) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Idea p : ideas) {
			p.changeHtmlLinks(path);
		}
	}

}
