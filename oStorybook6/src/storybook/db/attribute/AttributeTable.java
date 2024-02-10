/*
 * Copyright (C) 2019 favdb
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

import java.beans.PropertyChangeEvent;
import java.util.List;
import storybook.db.DB;
import storybook.db.abs.AbsColumn;
import storybook.db.abs.AbstractEntity;
import storybook.db.abs.AbsTable;
import storybook.db.book.Book;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class AttributeTable extends AbsTable {

	public AttributeTable(MainFrame main) {
		super(main, Book.TYPE.ATTRIBUTE);
	}

	@Override
	public void init() {
		this.withPart = false;
	}

	@Override
	public void initUi() {
		super.initUi();
		this.toolbar.setVisible(false);
	}

	@Override
	protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
		//no specific change
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		Attribute entity = (Attribute) mainFrame.project.get(Book.TYPE.STRAND, id);
		return entity;
	}

	@Override
	public List<AbsColumn> getColumns(AbstractEntity entity) {
		List<AbsColumn> cols = super.getColumns(entity);

		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.ATTRIBUTE_KEY));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.ATTRIBUTE_VALUE));

		super.getColumnsEnd(cols, entity);

		return cols;
	}

	@Override
	public List<Object> getRow(AbstractEntity entity) {
		List<Object> cols = super.getRow(entity);
		Attribute e = (Attribute) entity;
		cols.add(e.getKey());
		cols.add(e.getValue());
		getRowEnd(cols, entity);
		return (cols);
	}

	@Override
	public void updateRow(AbstractEntity entity) {
		//nothing
	}

}
