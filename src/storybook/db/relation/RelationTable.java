/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2015 FaVdB

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
package storybook.db.relation;

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
public class RelationTable extends AbsTable {

	public RelationTable(MainFrame mainFrame) {
		super(mainFrame, Book.TYPE.RELATION);
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
		if (act.isUpdate()
		   && (act.getType() == Book.TYPE.RELATION
		   || act.getType() == Book.TYPE.ITEM
		   || act.getType() == Book.TYPE.LOCATION
		   || act.getType() == Book.TYPE.PERSON
		   || act.getType() == Book.TYPE.SCENE)) {
			fillTable();
		}
	}

	@Override
	protected void sendSetEntityToEdit(int row) {
		if (row == -1) {
			return;
		}
		Relation r = (Relation) getEntityFromRow(row);
		if (r != null) {
			mainFrame.showEditorAsDialog(r);
		}
	}

	@Override
	protected void sendSetNewEntityToEdit(AbstractEntity entity) {
		mainFrame.showEditorAsDialog(entity);
	}

	@Override
	protected synchronized void sendDeleteEntity(int row) {
		Relation r = (Relation) getEntityFromRow(row);
		if (r != null) {
			ctrl.deleteEntity(r);
		}
	}

	@Override
	protected synchronized void sendDeleteEntities(List<AbstractEntity> entities) {
		if (entities.isEmpty()) {
			return;
		}
		ArrayList<Long> ids = new ArrayList<>();
		for (AbstractEntity entity : entities) {
			Relation r = (Relation) entity;
			ids.add(r.getId());
		}
		ctrl.deletemultiEntity(entities.get(0).getObjType(), ids);
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		return (Relation) mainFrame.project.get(Book.TYPE.RELATION, id);
	}

	@Override
	public void updateRow(AbstractEntity entity) {
		// refresh for scene, person, item, tag, location
		switch (Book.getTYPE(entity)) {
			case ITEM:
			case LOCATION:
			case PERSON:
			case RELATION:
			case SCENE:
			case TAG:
				fillTable();
				break;
			default:
				break;
		}
	}

	@Override
	public List<AbsColumn> getColumns(AbstractEntity entity) {
		List<AbsColumn> cols = super.getColumns(entity);

		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.SCENE_START));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.SCENE_END));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.PERSONS, TCR_ENTITIES));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.LOCATIONS, TCR_ENTITIES));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.ITEMS, TCR_ENTITIES));

		this.getColumnsEnd(cols, entity);

		return (cols);
	}

	@Override
	public List<Object> getRow(AbstractEntity entity) {
		List<Object> cols = super.getRow(entity);
		Relation e = (Relation) entity;
		cols.add(e.getStartScene());
		cols.add(e.getEndScene());
		cols.add(e.getPersons());
		cols.add(e.getLocations());
		cols.add(e.getItems());
		getRowEnd(cols, entity);
		return (cols);
	}

}
