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
package storybook.db.plot;

import i18n.I18N;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import storybook.project.Project;
import storybook.tools.html.Html;

/**
 *
 * @author favdb
 */
public class Plots extends AbsEntitys {

	private final List<Plot> plots = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Plots(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Plot p : plots) {
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
			plots.add((Plot) entity);
		} else {
			plots.set(getIdx(entity.getId()), (Plot) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Plot p : plots) {
			if (p.getId().equals(id)) {
				return plots.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Plot get(Long id) {
		for (Plot p : plots) {
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
		plots.add((Plot) p);
	}

	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			plots.remove((Plot) p);
		}
	}

	@Override
	public AbstractEntity getFirst() {
		if (plots.isEmpty()) {
			return null;
		}
		return plots.get(0);
	}

	@Override
	public List getList() {
		return plots;
	}

	@Override
	public int getCount() {
		return plots.size();
	}

	public List findByName() {
		List<Plot> ls = new ArrayList<>();
		for (Plot p : plots) {
			ls.add(p);
		}
		Collections.sort(ls, (Plot r1, Plot r2)
		   -> r1.getName().compareTo(r2.getName()));
		return ls;
	}

	public Plot findName(String name) {
		for (Plot p : plots) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	public List findByCategory(String value) {
		List<Plot> ls = new ArrayList<>();
		for (Plot p : plots) {
			if (Objects.equals(p.getCategory(), value)) {
				ls.add(p);
			}
		}
		return ls;
	}

	public List<String> findCategories() {
		List<String> ls = new ArrayList<>();
		for (Plot p : plots) {
			if (p.getCategory() != null
			   && !p.getCategory().isEmpty()
			   && !ls.contains(p.getCategory())) {
				ls.add(p.getCategory());
			}
		}
		return ls;
	}

	public List<Plot> findCategory(String value) {
		List<Plot> ls = new ArrayList<>();
		for (Plot p : plots) {
			if (p.getCategory().equals(value)) {
				ls.add(p);
			}
		}
		return ls;
	}

	public List<String> findAllInList() {
		List<String> list = new ArrayList<>();
		for (Plot p : plots) {
			list.add(p.getName());
		}
		return list;
	}

	/**
	 * get the tool tip for the given Plot
	 *
	 * @param buf
	 * @param plot
	 */
	public static void tooltip(StringBuilder buf, Plot plot) {
		if (plot == null) {
			return;
		}
		if (plot.getCategory() != null && !plot.getCategory().isEmpty()) {
			buf.append(Html.intoB(I18N.getMsg("category"))).append(": ")
			   .append(plot.getCategory()).append(Html.BR);
		}
		if (plot.getDescription() != null && !plot.getDescription().isEmpty()) {
			buf.append(plot.getDescription());
		}
	}

	@Override
	public void setLinks() {
		//empty no links
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Plot p : plots) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Plot p : plots) {
			p.changeHtmlLinks(path);
		}
	}

}
