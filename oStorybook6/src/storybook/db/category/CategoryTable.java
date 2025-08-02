/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.category;

import java.beans.PropertyChangeEvent;
import java.util.List;
import storybook.ctrl.ActKey;
import storybook.db.DB;
import storybook.db.abs.AbsColumn;
import static storybook.db.abs.AbsColumn.*;
import storybook.db.abs.AbsTable;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.ui.MainFrame;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class CategoryTable extends AbsTable {

	public CategoryTable(MainFrame mainFrame) {
		super(mainFrame, Book.TYPE.CATEGORY);
	}

	@Override
	public void init() {
		this.withPart = false;
	}

	@Override
	public void initUi() {
		super.initUi();
	}

	@Override
	protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
		ActKey act = new ActKey(evt);
		if (act.isUpdate() || act.isDelete()) {
			fillTable();
			return;
		}
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		Category category = (Category) mainFrame.project.get(Book.TYPE.CATEGORY, id);
		return category;
	}

	@Override
	public void updateRow(AbstractEntity entity) {
		//nothing
	}

	@Override
	public List<AbsColumn> getColumns(AbstractEntity entity) {
		List<AbsColumn> cols = super.getColumns(entity);

		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.SORT, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.CATEGORY_SUP));

		super.getColumnsEnd(cols, entity);

		return cols;
	}

	@Override
	public List<Object> getRow(AbstractEntity entity) {
		List<Object> cols = super.getRow(entity);
		Category e = (Category) entity;
		cols.add(e.getSort());
		cols.add(e.getSup());
		getRowEnd(cols, entity);
		return (cols);
	}

}
