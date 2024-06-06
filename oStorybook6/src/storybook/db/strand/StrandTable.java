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
package storybook.db.strand;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
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
public class StrandTable extends AbsTable {

	public StrandTable(MainFrame mainFrame) {
		super(mainFrame, Book.TYPE.STRAND);
	}

	@Override
	public void init() {
		allowMultiDelete = false;
	}

	@Override
	protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
		ActKey act = new ActKey(evt);
		if ((act.isNew() || act.isUpdate() || act.isDelete())
				&& (act.getType() == Book.TYPE.STRAND)) {
			fillTable();
		}
	}

	@Override
	protected void sendSetEntityToEdit(int row) {
		if (row == -1) {
			return;
		}
		Strand strand = (Strand) getEntityFromRow(row);
		if (strand != null) {
			mainFrame.showEditorAsDialog(strand);
		}
	}

	@Override
	protected void sendSetNewEntityToEdit(AbstractEntity entity) {
		if (entity != null) {
			mainFrame.showEditorAsDialog(entity);
		}
	}

	@Override
	protected synchronized void sendDeleteEntity(int row) {
		Strand strand = (Strand) getEntityFromRow(row);
		if (strand != null) {
			ctrl.deleteEntity(strand);
		}
	}

	@Override
	protected synchronized void sendDeleteEntities(List<AbstractEntity> entities) {
		ArrayList<Long> ids = new ArrayList<>();
		for (AbstractEntity entity : entities) {
			Strand strand = (Strand) entity;
			ids.add(strand.getId());
		}
		ctrl.deletemultiEntity(Book.TYPE.STRAND, ids);
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		return (Strand) mainFrame.project.get(Book.TYPE.STRAND, id);
	}

	@Override
	public void updateRow(AbstractEntity entity) {
		// nothing
	}

	@Override
	public List<AbsColumn> getColumns(AbstractEntity entity) {
		List<AbsColumn> cols = super.getColumns(entity);

		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.ABBREVIATION, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.STRAND_COLOR, TCR_COLOR, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.NUMBER, NUMERIC, AL_CENTER));

		this.getColumnsEnd(cols, entity);

		return (cols);
	}

	@Override
	public List<Object> getRow(AbstractEntity entity) {
		List<Object> cols = super.getRow(entity);
		Strand e = (Strand) entity;
		cols.add(e.getAbbreviation());
		cols.add(e.getJColor());
		cols.add(e.getSort());
		getRowEnd(cols, entity);
		return (cols);
	}

}
