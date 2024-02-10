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
package storybook.db.status;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import storybook.ctrl.ActKey;
import storybook.db.abs.AbsColumn;
import storybook.db.abs.AbstractEntity;
import storybook.db.abs.AbsTable;
import storybook.db.book.Book;
import storybook.db.tag.Tag;
import storybook.ui.MainFrame;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class StatusTable extends AbsTable {

	public StatusTable(MainFrame mainFrame) {
		super(mainFrame, Book.TYPE.TAG);
	}

	@Override
	public void init() {
		this.withPart = false;
		allowMultiDelete = false;
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public List<AbstractEntity> getAllEntities() {
		//LOG.printInfos(TT+".getAllEntities()");
		List list = mainFrame.project.getList(getType());
		return (list);
	}

	@Override
	protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
		ActKey act = new ActKey(evt);
		if (act.isUpdate()
		   && (act.getType() == Book.TYPE.STATUS)) {
			fillTable();
		}
	}

	@Override
	protected void sendSetEntityToEdit(int row) {
		if (row == -1) {
			return;
		}
		Status tag = (Status) getEntityFromRow(row);
		if (tag != null) {
			mainFrame.showEditorAsDialog(tag);
		}
	}

	@Override
	protected void sendSetNewEntityToEdit(AbstractEntity entity) {
		mainFrame.showEditorAsDialog(entity);
	}

	@Override
	protected synchronized void sendDeleteEntity(int row) {
		Status status = (Status) getEntityFromRow(row);
		if (status != null) {
			ctrl.deleteEntity(status);
		}
	}

	@Override
	protected synchronized void sendDeleteEntities(List<AbstractEntity> entities) {
		ArrayList<Long> ids = new ArrayList<>();
		for (AbstractEntity entity : entities) {
			Status t = (Status) entity;
			ids.add(t.getId());
		}
		ctrl.deletemultiEntity(Book.TYPE.STATUS, ids);
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		return (Status) mainFrame.project.get(Book.TYPE.STATUS, id);
	}

	@Override
	public void updateRow(AbstractEntity entity) {
		// nothing
	}

	@Override
	public List<AbsColumn> getColumns(AbstractEntity entity) {
		List<AbsColumn> cols = super.getColumns(entity);

		this.getColumnsEnd(cols, entity);

		return (cols);
	}

	@Override
	public List<Object> getRow(AbstractEntity entity) {
		List<Object> row = super.getRow(entity);
		Tag e = (Tag) entity;
		row.add(e.getCategory());
		getRowEnd(row, entity);
		return (row);
	}

}
