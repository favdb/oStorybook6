/*
 * Copyright (C) 2020 favdb
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
package storybook.db;

import javax.swing.Icon;
import javax.swing.JLabel;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;

/**
 *
 * @author favdb
 */
public class EntityCbItem {

	private Book.TYPE type;
	private Long id;
	private String name;
	private Icon icon;

	public EntityCbItem(AbstractEntity entity) {
		this(entity.getId(), Book.getTYPE(entity), entity.getName(), entity.getIcon());
	}

	public EntityCbItem(Long id, Book.TYPE type, String name, Icon icon) {
		this.id = id;
		this.type = type;
		this.name = name;
		this.icon = icon;
	}

	public void setType(Book.TYPE type) {
		this.type = type;
	}

	public Book.TYPE getType() {
		return type;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public Icon getIcon() {
		return icon;
	}

	@Override
	public String toString() {
		return name;
	}

	public JLabel toLabel() {
		JLabel lb = new JLabel(name);
		if (icon != null) {
			lb.setIcon(icon);
		}
		return lb;
	}

}
