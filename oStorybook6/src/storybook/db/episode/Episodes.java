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
package storybook.db.episode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import storybook.db.strand.Strand;
import storybook.project.Project;

/**
 *
 * @author favdb
 */
public class Episodes extends AbsEntitys {

	private List<Episode> episodes = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Episodes(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Episode p : episodes) {
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
			episodes.add((Episode) entity);
		} else {
			episodes.set(getIdx(entity.getId()), (Episode) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Episode p : episodes) {
			if (p.getId().equals(id)) {
				return episodes.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Episode get(Long id) {
		for (Episode p : episodes) {
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
		episodes.add((Episode) p);
	}

	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			episodes.remove((Episode) p);
		}
	}

	@Override
	public AbstractEntity getFirst() {
		if (episodes.isEmpty()) {
			return null;
		}
		return episodes.get(0);
	}

	@Override
	public List getList() {
		return episodes;
	}

	@Override
	public int getCount() {
		return episodes.size();
	}

	public List findByName() {
		List<Episode> ls = new ArrayList<>();
		for (Episode p : episodes) {
			ls.add(p);
		}
		Collections.sort(ls, (Episode r1, Episode r2)
		   -> r1.getName().compareTo(r2.getName()));
		return ls;
	}

	public Episode findName(String name) {
		for (Episode p : episodes) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	public List<Episode> find(Strand strand) {
		List<Episode> ls = new ArrayList<>();
		for (Episode p : episodes) {
			if (p.getStrand().equals(strand)) {
				ls.add(p);
			}
		}
		return ls;
	}

	@Override
	public void setLinks() {
		for (Episode p : episodes) {
			if (p.getStrandId() != -1L) {
				p.setStrand(project.strands.get(p.getStrandId()));
			}
			if (p.getChapterId() != -1L) {
				p.setChapter(project.chapters.get(p.getChapterId()));
			}
			if (p.getSceneId() != -1L) {
				p.setScene(project.scenes.get(p.getSceneId()));
			}
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Episode p : episodes) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Episode p : episodes) {
			p.changeHtmlLinks(path);
		}
	}

}
