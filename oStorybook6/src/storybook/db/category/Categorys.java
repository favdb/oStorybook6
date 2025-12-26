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
package storybook.db.category;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JCheckBox;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import static storybook.db.book.Book.TYPE.EVENT;
import static storybook.db.book.Book.TYPE.IDEA;
import static storybook.db.book.Book.TYPE.ITEM;
import static storybook.db.book.Book.TYPE.PLOT;
import static storybook.db.book.Book.TYPE.TAG;
import storybook.project.Project;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class Categorys extends AbsEntitys {

	private final List<Category> categorys = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Categorys(Project project) {
		super(project);
	}

	@Override
	public Long getLast() {
		Long n = 0L;
		for (Category p : categorys) {
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
			categorys.add((Category) entity);
		} else {
			categorys.set(getIdx(entity.getId()), (Category) entity);
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Category p : categorys) {
			if (p.getId().equals(id)) {
				return categorys.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Category get(Long id) {
		for (Category p : categorys) {
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
		categorys.add((Category) p);
	}

	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			categorys.remove((Category) p);
		}
	}

	/**
	 * sort scenes by scene number
	 */
	@Override
	public void sortById() {
		Collections.sort(categorys, (Category r1, Category r2) -> r1.getId().compareTo(r2.getId()));
	}

	@Override
	public AbstractEntity getFirst() {
		if (categorys.isEmpty()) {
			return null;
		}
		return categorys.get(0);
	}

	@Override
	public List getList() {
		return categorys;
	}

	@Override
	public int getCount() {
		return categorys.size();
	}

	/**
	 * find all chapters sorted by name
	 *
	 * @return
	 */
	public List findByName() {
		List<Category> ls = new ArrayList<>();
		for (Category p : categorys) {
			ls.add(p);
		}
		Collections.sort(ls, (Category r1, Category r2)
				-> r1.getName().compareTo(r2.getName()));
		return ls;
	}

	/**
	 * find Category for the given name
	 *
	 * @param name
	 * @return
	 */
	public Category findName(String name) {
		for (Category p : categorys) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * find all categories as a list of String for the given type
	 *
	 * @param mainFrame
	 * @param type
	 * @return
	 */
	public static List<String> find(MainFrame mainFrame, Book.TYPE type) {
		List<String> list = new ArrayList<>();
		switch (type) {
			case EVENT:
				return (mainFrame.project.events.findCategories());
			case IDEA:
				return (mainFrame.project.ideas.findCategories());
			case ITEM:
				return (mainFrame.project.items.findCategories());
			case PLOT:
				return (mainFrame.project.plots.findCategories());
			case TAG:
				return (mainFrame.project.tags.findCategories());
			default:
				break;
		}
		return list;
	}

	/**
	 * get a list of JCheckBox for Categories
	 *
	 * @param mainFrame
	 * @param comp
	 * @return
	 */
	public static List<JCheckBox> cbCategory(MainFrame mainFrame, ActionListener comp) {
		List<JCheckBox> list = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<Category> categories = mainFrame.project.categorys.getList();
		for (Category category : categories) {
			JCheckBox cb = new JCheckBox(category.getName());
			cb.putClientProperty("CbCategory", category);
			cb.setOpaque(false);
			cb.addActionListener(comp);
			cb.setSelected(true);
			list.add(cb);
		}
		return list;
	}

	@Override
	public void setLinks() {
		for (Category p : categorys) {
			if (p.getSupId() != -1L) {
				p.setSup(get(p.getId()));
			}
		}
	}

	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Category p : categorys) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	@Override
	public void changeHtmlLinks(String path) {
		for (Category p : categorys) {
			p.changeHtmlLinks(path);
		}
	}

}
