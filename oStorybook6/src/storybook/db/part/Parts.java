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
package storybook.db.part;

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
public class Parts extends AbsEntitys {

	private List<Part> parts = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Parts(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Part p : parts) {
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
			parts.add((Part) entity);
		} else {
			parts.set(getIdx(entity.getId()), (Part) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Part p : parts) {
			if (p.getId().equals(id)) {
				return parts.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Part get(Long id) {
		for (Part p : parts) {
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
		parts.add((Part) p);
	}

	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			parts.remove((Part) p);
		}
	}

	@Override
	public AbstractEntity getFirst() {
		if (parts.isEmpty()) {
			return null;
		}
		return parts.get(0);
	}

	@Override
	public List getList() {
		return parts;
	}

	/**
	 * sort by Id
	 */
	@Override
	public void sortById() {
		Collections.sort(parts, (Part r1, Part r2) -> r1.getId().compareTo(r2.getId()));
	}

	@Override
	public int getCount() {
		return parts.size();
	}

	/**
	 * find all chapters sorted by name
	 *
	 * @return
	 */
	public List findByName() {
		List<Part> ls = new ArrayList<>();
		for (Part p : parts) {
			ls.add(p);
		}
		Collections.sort(ls, (Part r1, Part r2)
				-> r1.getName().compareTo(r2.getName()));
		return ls;
	}

	/**
	 * find Chapter for the given name
	 *
	 * @param name
	 * @return
	 */
	public Part findName(String name) {
		for (Part p : parts) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * get Parts for the given super Part
	 *
	 * @param part
	 * @return
	 */
	public List<Part> getParts(Part part) {
		List<Part> ls = new ArrayList<>();
		for (Part p : parts) {
			if (p.hasSuperpart() && !ls.contains(p.getSuperpart())) {
				ls.add(p);
			}
		}
		return ls;
	}

	/**
	 * get all root Parts
	 *
	 * @return
	 */
	public List<Part> getRoots() {
		List<Part> ls = new ArrayList<>();
		for (Part p : parts) {
			if (!p.hasSuperpart() && !ls.contains(p.getSuperpart())) {
				ls.add(p);
			}
		}
		return ls;
	}

	/**
	 * get the last used Part number
	 *
	 * @return
	 */
	public int getLastNumber() {
		int n = 0;
		for (Part part : parts) {
			if ((part.getNumber() != null) && (part.getNumber() > n)) {
				n = part.getNumber();
			}
		}
		return n;
	}

	@Override
	public void setLinks() {
		for (Part p : parts) {
			if (p.getSuperpartId() != -1L) {
				p.setSuperpart(get(p.getSuperpartId()));
			}
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Part p : parts) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Part p : parts) {
			p.changeHtmlLinks(path);
		}
	}

	public void sortByNumber() {
		Collections.sort(parts, (Part r1, Part r2) -> r1.getNumber().compareTo(r2.getNumber()));
	}

}
