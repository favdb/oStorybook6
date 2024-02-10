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
package storybook.db.status;

import java.util.ArrayList;
import java.util.List;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import storybook.project.Project;

/**
 *
 * @author favdb
 */
public class Statuss extends AbsEntitys {

	private final List<Status> statuss = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Statuss(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Status p : statuss) {
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
			statuss.add((Status) entity);
		} else {
			statuss.set(getIdx(entity.getId()), (Status) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Status p : statuss) {
			if (p.getId().equals(id)) {
				return statuss.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Status get(Long id) {
		for (Status p : statuss) {
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
		statuss.add((Status) p);
	}

	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			statuss.remove((Status) p);
		}
	}

	@Override
	public AbstractEntity getFirst() {
		if (statuss.isEmpty()) {
			return null;
		}
		return statuss.get(0);
	}

	@Override
	public List getList() {
		return statuss;
	}

	@Override
	public int getCount() {
		return statuss.size();
	}

	@Override
	public void setLinks() {
		for (Status p : statuss) {
			if (p.getSupId() != -1L) {
				p.setSup(get(p.getSupId()));
			}
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Status p : statuss) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Status p : statuss) {
			p.changeHtmlLinks(path);
		}
	}

}
