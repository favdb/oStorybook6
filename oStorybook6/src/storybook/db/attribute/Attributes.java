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
package storybook.db.attribute;

import java.util.ArrayList;
import java.util.List;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.project.Project;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class Attributes extends AbsEntitys {

	private final List<Attribute> attributes = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Attributes(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Attribute p : attributes) {
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
			attributes.add((Attribute) entity);
		} else {
			attributes.set(getIdx(entity.getId()), (Attribute) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Attribute p : attributes) {
			if (p.getId().equals(id)) {
				return attributes.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Attribute get(Long id) {
		for (Attribute p : attributes) {
			if (p.getId().equals(id)) {
				return p;
			}
		}
		return null;
	}

	@Override
	public AbstractEntity getFirst() {
		if (attributes.isEmpty()) {
			return null;
		}
		return attributes.get(0);
	}

	@Override
	public List getList() {
		return attributes;
	}

	@Override
	public int getCount() {
		return attributes.size();
	}

	/**
	 * find the Attribute of the given key name and value
	 *
	 * @param mainFrame
	 * @param key
	 * @param value
	 * @return
	 */
	public static Attribute find(MainFrame mainFrame, String key, String value) {
		@SuppressWarnings("unchecked")
		List<Attribute> attributes = (List) mainFrame.project.getList(Book.TYPE.ATTRIBUTE);
		for (Attribute attribute : attributes) {
			if (key.equals(attribute.getKey()) && value.equals(attribute.getValue())) {
				return (attribute);
			}
		}
		return (null);
	}

	List<String> findKeys() {
		List<String> keys = new ArrayList<>();
		for (Attribute p : attributes) {
			if (!keys.contains(p.getKey())) {
				keys.add(p.getKey());
			}
		}
		return keys;
	}

	public void deleteOrphans() {
		//todo delete orphans attribute
	}

	@Override
	public void add(AbstractEntity p) {
		if (p.getId() == -1L) {
			p.setId(getLast() + 1L);
		}
		attributes.add((Attribute) p);
	}

	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			attributes.remove((Attribute) p);
		}
	}

	@Override
	public void setLinks() {
		//empty
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Attribute p : attributes) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Attribute p : attributes) {
			p.changeHtmlLinks(path);
		}
	}

}
