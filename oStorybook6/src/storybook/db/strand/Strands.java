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
package storybook.db.strand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import storybook.project.Project;

/**
 * utilities for Strand
 *
 * @author favdb
 */
public class Strands extends AbsEntitys {

	private final List<Strand> strands = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Strands(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Strand p : strands) {
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
			strands.add((Strand) entity);
		} else {
			strands.set(getIdx(entity.getId()), (Strand) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Strand p : strands) {
			if (p.getId().equals(id)) {
				return strands.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Strand get(Long id) {
		for (Strand p : strands) {
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
		strands.add((Strand) p);
	}

	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			strands.remove((Strand) p);
		}
	}

	@Override
	public AbstractEntity getFirst() {
		if (strands.isEmpty()) {
			return null;
		}
		return strands.get(0);
	}

	@Override
	public List getList() {
		return strands;
	}

	public List getList(Strand st) {
		//LOG.trace("getList(st=" + LOG.trace(st) + ")");
		List<Strand> lst = new ArrayList<>();
		for (Strand xst : strands) {
			if (!xst.equals(st)) {
				lst.add(xst);
			}
		}
		return lst;
	}

	/**
	 * sort by Id
	 */
	@Override
	public void sortById() {
		Collections.sort(strands, (Strand r1, Strand r2) -> r1.getId().compareTo(r2.getId()));
	}

	@Override
	public int getCount() {
		return strands.size();
	}

	/**
	 * find all chapters sorted by name
	 *
	 * @return
	 */
	public List findByName() {
		List<Strand> ls = new ArrayList<>();
		for (Strand p : strands) {
			ls.add(p);
		}
		Collections.sort(ls, (Strand r1, Strand r2)
				-> r1.getName().compareTo(r2.getName()));
		return ls;
	}

	/**
	 * find Chapter for the given name
	 *
	 * @param name
	 * @return
	 */
	public Strand findName(String name) {
		for (Strand p : strands) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	public List findOrderBySort() {
		List<Strand> ls = new ArrayList<>();
		for (Strand p : strands) {
			ls.add(p);
		}
		Collections.sort(ls, (Strand r1, Strand r2)
				-> r1.getSort().compareTo(r2.getSort()));
		return ls;
	}

	/**
	 * get the tools tip for the given Strand (not used)
	 *
	 * @param buf
	 * @param strand
	 */
	public static void tooltip(StringBuilder buf, Strand strand) {
		//buf.append(strand.getName());
	}

	@Override
	public void setLinks() {
		//empty no links
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Strand p : strands) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Strand p : strands) {
			p.changeHtmlLinks(path);
		}
	}

}
