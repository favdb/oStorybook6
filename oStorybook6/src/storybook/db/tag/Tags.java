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
package storybook.db.tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import storybook.project.Project;
import storybook.tools.TextUtil;

/**
 *
 * @author favdb
 */
public class Tags extends AbsEntitys {

	public final List<Tag> tags = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Tags(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Tag p : tags) {
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
			tags.add((Tag) entity);
		} else {
			tags.set(getIdx(entity.getId()), (Tag) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Tag p : tags) {
			if (p.getId().equals(id)) {
				return tags.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Tag get(Long id) {
		for (Tag p : tags) {
			if (p.getId().equals(id)) {
				return p;
			}
		}
		return null;
	}

	@Override
	public List getList() {
		return tags;
	}

	/**
	 * sort by Id
	 */
	@Override
	public void sortById() {
		Collections.sort(tags, (Tag r1, Tag r2) -> r1.getId().compareTo(r2.getId()));
	}

	/**
	 * get the tools tip for the given Item/Tag
	 *
	 * @param buf
	 * @param tag
	 */
	public static void tooltip(StringBuilder buf, Tag tag) {
		if (tag.getDescription() != null && !tag.getDescription().isEmpty()) {
			buf.append(TextUtil.ellipsize(tag.getDescription()));
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Tag p : tags) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public AbstractEntity getFirst() {
		if (tags.isEmpty()) {
			return null;
		}
		return tags.get(0);
	}

	@Override
	public void add(AbstractEntity p) {
		if (p.getId() == -1L) {
			p.setId(getLast() + 1L);
		}
		tags.add((Tag) p);
	}

	@Override
	public void delete(AbstractEntity entity) {
		int n = getIdx(entity.getId());
		if (n != -1) {
			tags.remove((Tag) entity);
		}
	}

	@Override
	public int getCount() {
		return tags.size();
	}

	@Override
	public void setLinks() {
		//empty
	}

	public Tag findName(String n) {
		for (Tag e : tags) {
			if (e.getName().equals(n)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * find all sorted by name
	 *
	 * @return
	 */
	public List findByName() {
		List<Tag> ls = new ArrayList<>();
		for (Tag p : tags) {
			ls.add(p);
		}
		Collections.sort(ls, (Tag r1, Tag r2)
				-> r1.getName().compareTo(r2.getName()));
		return ls;
	}

	public List<String> findCategories() {
		List<String> ls = new ArrayList<>();
		for (Tag p : tags) {
			if (p.getCategory() != null
					&& !p.getCategory().isEmpty()
					&& !ls.contains(p.getCategory())) {
				ls.add(p.getCategory());
			}
		}
		return ls;
	}

	public List<Tag> findCategory(String value) {
		List<Tag> ls = new ArrayList<>();
		for (Tag p : tags) {
			if (Objects.equals(p.getCategory(), value)) {
				ls.add(p);
			}
		}
		return ls;
	}

	public List<Tag> findByCategory() {
		List<Tag> ls = new ArrayList<>();
		for (Tag p : tags) {
			ls.add(p);
		}
		Collections.sort(ls, (Tag r1, Tag r2)
				-> r1.getCategory().compareTo(r2.getCategory()));
		return ls;
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Tag p : tags) {
			p.changeHtmlLinks(path);
		}
	}

}
