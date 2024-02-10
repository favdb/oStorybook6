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
package storybook.db.memo;

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
public class Memos extends AbsEntitys {

	public final List<Memo> memos = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Memos(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Memo p : memos) {
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
			memos.add((Memo) entity);
		} else {
			memos.set(getIdx(entity.getId()), (Memo) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Memo p : memos) {
			if (p.getId().equals(id)) {
				return memos.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Memo get(Long id) {
		for (Memo p : memos) {
			if (p.getId().equals(id)) {
				return p;
			}
		}
		return null;
	}

	@Override
	public List getList() {
		return memos;
	}

	/**
	 * get the tools tip for the given Memo/Tag
	 *
	 * @param buf
	 * @param tag
	 */
	public static void tooltip(StringBuilder buf, Memo tag) {
		if (tag.getDescription() != null && !tag.getDescription().isEmpty()) {
			buf.append(TextUtil.ellipsize(tag.getDescription()));
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Memo p : memos) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public AbstractEntity getFirst() {
		if (memos.isEmpty()) {
			return null;
		}
		return memos.get(0);
	}

	@Override
	public void add(AbstractEntity p) {
		if (p.getId() == -1L) {
			p.setId(getLast() + 1L);
		}
		memos.add((Memo) p);
	}

	@Override
	public void delete(AbstractEntity entity) {
		int n = getIdx(entity.getId());
		if (n != -1) {
			memos.remove((Memo) entity);
		}
	}

	@Override
	public int getCount() {
		return memos.size();
	}

	@Override
	public void setLinks() {
		//empty
	}

	public Memo findName(String n) {
		for (Memo e : memos) {
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
		List<Memo> ls = new ArrayList<>();
		for (Memo p : memos) {
			ls.add(p);
		}
		Collections.sort(ls, (Memo r1, Memo r2)
		   -> r1.getName().compareTo(r2.getName()));
		return ls;
	}

	public List<String> findCategories() {
		List<String> ls = new ArrayList<>();
		for (Memo p : memos) {
			if (p.getCategory() != null
			   && !p.getCategory().isEmpty()
			   && !ls.contains(p.getCategory())) {
				ls.add(p.getCategory());
			}
		}
		return ls;
	}

	public List<Memo> findCategory(String value) {
		List<Memo> ls = new ArrayList<>();
		for (Memo p : memos) {
			if (Objects.equals(p.getCategory(), value)) {
				ls.add(p);
			}
		}
		return ls;
	}

	public List<Memo> findByCategory() {
		List<Memo> ls = new ArrayList<>();
		for (Memo p : memos) {
			ls.add(p);
		}
		Collections.sort(ls, (Memo r1, Memo r2)
		   -> r1.getCategory().compareTo(r2.getCategory()));
		return ls;
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Memo p : memos) {
			p.changeHtmlLinks(path);
		}
	}

}
