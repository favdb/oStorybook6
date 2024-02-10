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
package storybook.db.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import storybook.project.Project;

/**
 *
 * @author favdb
 */
public class Events extends AbsEntitys {

	private final List<Event> events = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Events(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Event p : events) {
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
			events.add((Event) entity);
		} else {
			events.set(getIdx(entity.getId()), (Event) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Event p : events) {
			if (p.getId().equals(id)) {
				return events.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Event get(Long id) {
		for (Event p : events) {
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
		events.add((Event) p);
	}

	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			events.remove((Event) p);
		}
	}

	@Override
	public AbstractEntity getFirst() {
		if (events.isEmpty()) {
			return null;
		}
		return events.get(0);
	}

	@Override
	public List getList() {
		return events;
	}

	@Override
	public int getCount() {
		return events.size();
	}

	public List findByName() {
		List<Event> ls = new ArrayList<>();
		for (Event p : events) {
			ls.add(p);
		}
		Collections.sort(ls, (Event r1, Event r2)
		   -> r1.getName().compareTo(r2.getName()));
		return ls;
	}

	public Event findName(String name) {
		for (Event p : events) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	public List<String> findCategories() {
		List<String> ls = new ArrayList<>();
		for (Event p : events) {
			if (!ls.contains(p.getCategory())) {
				ls.add(p.getCategory());
			}
		}
		Collections.sort(ls);
		return ls;
	}

	public List findByCategory(String category) {
		List<Event> ls = new ArrayList<>();
		for (Event p : events) {
			if (p.getCategory().equals(category)) {
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
		for (Event p : events) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Event p : events) {
			p.changeHtmlLinks(path);
		}
	}

}
