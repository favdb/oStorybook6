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
package storybook.db.gender;

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
public class Genders extends AbsEntitys {

	private final List<Gender> genders = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Genders(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Gender p : genders) {
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
			genders.add((Gender) entity);
		} else {
			genders.set(getIdx(entity.getId()), (Gender) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Gender p : genders) {
			if (p.getId().equals(id)) {
				return genders.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Gender get(Long id) {
		for (Gender p : genders) {
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
		genders.add((Gender) p);
	}

	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			genders.remove((Gender) p);
		}
	}

	@Override
	public AbstractEntity getFirst() {
		if (genders.isEmpty()) {
			return null;
		}
		return genders.get(0);
	}

	@Override
	public List getList() {
		return genders;
	}

	@Override
	public int getCount() {
		return genders.size();
	}

	public List findByName() {
		List<Gender> ls = new ArrayList<>();
		for (Gender p : genders) {
			ls.add(p);
		}
		Collections.sort(ls, (Gender r1, Gender r2)
		   -> r1.getName().compareTo(r2.getName()));
		return ls;
	}

	public Gender findName(String name) {
		for (Gender p : genders) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	@Override
	public void setLinks() {
		//empty no links
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Gender p : genders) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Gender p : genders) {
			p.changeHtmlLinks(path);
		}
	}

}
