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
package storybook.db.part;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import storybook.ctrl.ActKey;
import storybook.db.DB;
import storybook.db.DB.DATA;
import static storybook.db.DB.DATA.*;
import storybook.db.abs.AbsColumn;
import static storybook.db.abs.AbsColumn.*;
import storybook.db.abs.AbsTable;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.book.BookUtil;
import storybook.db.chapter.Chapter;
import storybook.ui.MainFrame;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class PartTable extends AbsTable {

	private static final String TT = "PartTable";

	public PartTable(MainFrame mainFrame) {
		super(mainFrame, Book.TYPE.PART);
		allowMultiDelete = false;
	}

	@Override
	public void init() {
		this.withPart = false;
	}

	@Override
	protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
		ActKey act = new ActKey(evt);
		if ((act.isNew() || act.isUpdate() || act.isDelete())
				&& (act.getType() == Book.TYPE.PART
				|| act.getType() == Book.TYPE.SCENE
				|| act.getType() == Book.TYPE.CHAPTER)) {
			fillTable();
		}
	}

	@Override
	protected void sendSetEntityToEdit(int row) {
		if (row == -1) {
			return;
		}
		Part part = (Part) getEntityFromRow(row);
		if (part != null) {
			mainFrame.showEditorAsDialog(part);
			mainFrame.getMainMenu().reloadToolbar();
		}
	}

	@Override
	protected void sendSetNewEntityToEdit(AbstractEntity entity) {
		mainFrame.showEditorAsDialog(entity);
		mainFrame.getMainMenu().reloadToolbar();
	}

	@Override
	protected synchronized void sendDeleteEntity(int row) {
		Part part = (Part) getEntityFromRow(row);
		if (part != null) {
			ctrl.deleteEntity(part);
			mainFrame.getMainMenu().reloadToolbar();
		}
	}

	@Override
	protected synchronized void sendDeleteEntities(List<AbstractEntity> entities) {
		ArrayList<Long> ids = new ArrayList<>();
		for (AbstractEntity entity : entities) {
			Part part = (Part) entity;
			ids.add(part.getId());
		}
		ctrl.deletemultiEntity(Book.TYPE.PART, ids);
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		return (Part) mainFrame.project.get(Book.TYPE.PART, id);
	}

	@Override
	public void updateRow(AbstractEntity entity) {
		// if part refresh for superpart
		if (entity.getObjType() == Book.TYPE.PART) {
			fillTable();
		}
	}

	@Override
	public List<AbsColumn> getColumns(AbstractEntity entity) {
		List<AbsColumn> cols = super.getColumns(entity);

		cols.add(new AbsColumn(mainFrame, cols, NUMBER, NUMERIC, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols, PART_SUP, TCR_ENTITY));
		cols.add(new AbsColumn(mainFrame, cols, OBJECTIVE_DATE, TCR_DAY, AL_CENTER, TCR_HIDE));
		cols.add(new AbsColumn(mainFrame, cols, OBJECTIVE_SIZE, NUMERIC_RENDERER, AL_CENTER, TCR_HIDE));
		cols.add(new AbsColumn(mainFrame, cols, OBJECTIVE_DONE, TCR_DAY, AL_CENTER, TCR_HIDE));
		cols.add(new AbsColumn(mainFrame, cols, CHAPTERS, NUMERIC_RENDERER, AL_CENTER, TCR_HIDE));
		cols.add(new AbsColumn(mainFrame, cols, WORDS, NUMERIC_RENDERER, AL_CENTER, TCR_HIDE));
		cols.add(new AbsColumn(mainFrame, cols, CHARACTERS, NUMERIC_RENDERER, AL_CENTER, TCR_HIDE));

		this.getColumnsEnd(cols, entity);

		return cols;
	}

	@Override
	public List<Object> getRow(AbstractEntity entity) {
		List<Object> cols = super.getRow(entity);
		Part e = (Part) entity;
		cols.add(e.getNumber());
		cols.add(e.getSuperpart());
		cols.add(e.getObjectiveTime());
		cols.add(e.getObjectiveChars() == 0 ? "" : e.getObjectiveChars());
		cols.add(e.getDoneTime());
		List<Chapter> chapters = mainFrame.project.chapters.find(e);
		cols.add(chapters.size());
		cols.add(BookUtil.getNbWords(mainFrame.project, chapters));
		cols.add(BookUtil.getNbChars(mainFrame.project, chapters));
		getRowEnd(cols, entity);
		return cols;
	}

	/**
	 * Table change event (simple change selected row)
	 *
	 * @param e
	 */
	@Override
	public void tableChanged(TableModelEvent e) {
		//LOG.printInfos(TT + ".tableChanged(e=" + e.toString() + ")");
		super.tableChanged(e);
		int row = e.getFirstRow();
		if (row < 0 || row != e.getLastRow()) {
			return;
		}
		AbstractEntity c = (AbstractEntity) this.getEntityFromRow(row);
		if (c != null) {
			mainFrame.getBookController().infoSetTo(c);
		}
		int col = e.getColumn();
		if (c instanceof Part && col != -1) {
			TableModel model = table.getModel();
			Part part = (Part) c;
			DATA data = DB.getData(model.getColumnName(col));
			Object value = model.getValueAt(row, col);
			switch (data) {
				case OBJECTIVE_SIZE:
					if (!(value instanceof Integer)) {
						return;
					}
					part.setObjectiveChars((Integer) value);
					break;
				default:
					return;
			}
			mainFrame.getBookController().updateEntity(part);
		}
	}

}
