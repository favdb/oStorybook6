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
package storybook.db.item;

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
public class Items extends AbsEntitys {

	public final List<Item> items = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Items(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Item p : items) {
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
			items.add((Item) entity);
		} else {
			items.set(getIdx(entity.getId()), (Item) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Item p : items) {
			if (p.getId().equals(id)) {
				return items.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Item get(Long id) {
		for (Item p : items) {
			if (p.getId().equals(id)) {
				return p;
			}
		}
		return null;
	}

	@Override
	public List getList() {
		return items;
	}

	/**
	 * sort by Id
	 */
	@Override
	public void sortById() {
		Collections.sort(items, (Item r1, Item r2) -> r1.getId().compareTo(r2.getId()));
	}

	/**
	 * get the tools tip for the given Item/Tag
	 *
	 * @param buf
	 * @param tag
	 */
	public static void tooltip(StringBuilder buf, Item tag) {
		if (tag.getDescription() != null && !tag.getDescription().isEmpty()) {
			buf.append(TextUtil.ellipsize(tag.getDescription()));
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Item p : items) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public AbstractEntity getFirst() {
		if (items.isEmpty()) {
			return null;
		}
		return items.get(0);
	}

	@Override
	public void add(AbstractEntity p) {
		if (p.getId() == -1L) {
			p.setId(getLast() + 1L);
		}
		items.add((Item) p);
	}

	@Override
	public void delete(AbstractEntity entity) {
		int n = getIdx(entity.getId());
		if (n != -1) {
			items.remove((Item) entity);
		}
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public void setLinks() {
		//empty
	}

	public Item findName(String n) {
		for (Item e : items) {
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
		List<Item> ls = new ArrayList<>();
		for (Item p : items) {
			ls.add(p);
		}
		Collections.sort(ls, (Item r1, Item r2)
				-> r1.getName().compareTo(r2.getName()));
		return ls;
	}

	public List<String> findCategories() {
		List<String> ls = new ArrayList<>();
		for (Item p : items) {
			if (p.getCategory() != null
					&& !p.getCategory().isEmpty()
					&& !ls.contains(p.getCategory())) {
				ls.add(p.getCategory());
			}
		}
		return ls;
	}

	public List<Item> findCategory(String value) {
		List<Item> ls = new ArrayList<>();
		for (Item p : items) {
			if (Objects.equals(p.getCategory(), value)) {
				ls.add(p);
			}
		}
		return ls;
	}

	public List<Item> findByCategory() {
		List<Item> ls = new ArrayList<>();
		for (Item p : items) {
			ls.add(p);
		}
		Collections.sort(ls, (Item r1, Item r2)
				-> r1.getCategory().compareTo(r2.getCategory()));
		return ls;
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Item p : items) {
			p.changeHtmlLinks(path);
		}
	}

}
