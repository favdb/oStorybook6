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
package storybook.db.person;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import storybook.ctrl.ActKey;
import storybook.db.DB;
import storybook.db.abs.AbsColumn;
import static storybook.db.abs.AbsColumn.*;
import storybook.db.abs.AbstractEntity;
import storybook.db.abs.AbsTable;
import storybook.db.book.Book;
import storybook.db.category.Category;
import storybook.db.gender.Gender;
import storybook.ui.MainFrame;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class PersonTable extends AbsTable {

	public PersonTable(MainFrame mainFrame) {
		super(mainFrame, Book.TYPE.PERSON);
	}

	@Override
	public void init() {
		this.withPart = false;
	}

	@Override
	public void initUi() {
		super.initUi();
		//this.toolbar.setVisible(false);
	}

	@Override
	protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
		ActKey act = new ActKey(evt);
		if (act.isUpdate()
		   && (act.getType() == Book.TYPE.PERSON)) {
			fillTable();
		}
	}

	private void updateCategories(PropertyChangeEvent evt) {
		Category oldCategory = (Category) evt.getOldValue();
		Category newCategory = (Category) evt.getNewValue();
		for (int row = 0; row < tableModel.getRowCount(); ++row) {
			if (oldCategory.equals(newCategory)) {
				tableModel.setValueAt(newCategory, row, 1);
			}
		}
	}

	private void updateGenders(PropertyChangeEvent evt) {
		Gender oldGender = (Gender) evt.getOldValue();
		Gender newGender = (Gender) evt.getNewValue();
		for (int row = 0; row < tableModel.getRowCount(); ++row) {
			if (oldGender.equals(newGender)) {
				tableModel.setValueAt(newGender, row, 1);
			}
		}
	}

	@Override
	protected void sendSetEntityToEdit(int row) {
		if (row == -1) {
			return;
		}
		Person person = (Person) getEntityFromRow(row);
		if (person != null) {
			mainFrame.showEditorAsDialog(person);
		}
	}

	@Override
	protected void sendSetNewEntityToEdit(AbstractEntity entity) {
		mainFrame.showEditorAsDialog(entity);
	}

	@Override
	protected synchronized void sendDeleteEntity(int row) {
		Person person = (Person) getEntityFromRow(row);
		if (person != null) {
			ctrl.deleteEntity(person);
		}
	}

	@Override
	protected synchronized void sendDeleteEntities(List<AbstractEntity> entities) {
		ArrayList<Long> ids = new ArrayList<>();
		for (AbstractEntity entity : entities) {
			Person person = (Person) entity;
			ids.add(person.getId());
		}
		ctrl.deletemultiEntity(Book.TYPE.PERSON, ids);
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		if (id == -1L) {
			return null;
		}
		return (Person) mainFrame.project.get(Book.TYPE.PERSON, id);
	}

	@Override
	public void updateRow(AbstractEntity entity) {
		// refresh for gender, category, attribute
		switch (Book.getTYPE(entity)) {
			case ATTRIBUTE:
			case CATEGORY:
			case GENDER:
				fillTable();
				break;
		}
	}

	@Override
	public List<AbsColumn> getColumns(AbstractEntity entity) {
		List<AbsColumn> cols = super.getColumns(entity);

		cols.add(new AbsColumn(mainFrame, cols,
		   DB.DATA.PERSON_FIRSTNAME));
		cols.add(new AbsColumn(mainFrame, cols,
		   DB.DATA.PERSON_LASTNAME));
		cols.add(new AbsColumn(mainFrame, cols,
		   DB.DATA.ABBREVIATION, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols,
		   DB.DATA.PERSON_GENDER, TCR_GENDER, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols,
		   DB.DATA.CATEGORY, TCR_CATEGORY, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols,
		   DB.DATA.PERSON_BIRTHDAY, TCR_DATE));
		cols.add(new AbsColumn(mainFrame, cols,
		   DB.DATA.PERSON_DEATH, TCR_DATE, TCR_HIDE));
		cols.add(new AbsColumn(mainFrame, cols,
		   DB.DATA.PERSON_OCCUPATION, TCR_HIDE));
		cols.add(new AbsColumn(mainFrame, cols,
		   DB.DATA.PERSON_COLOR, TCR_COLOR, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols,
		   DB.DATA.ATTRIBUTES, TCR_HIDE, TCR_ENTITIES, AL_CENTER));

		this.getColumnsEnd(cols, entity);
		return cols;
	}

	@Override
	public List<Object> getRow(AbstractEntity entity) {
		List<Object> cols = super.getRow(entity);
		Person e = (Person) entity;
		cols.add(e.getFirstname());
		cols.add(e.getLastname());
		cols.add(e.getAbbreviation());
		cols.add(e.getGender());
		cols.add(e.getCategory());
		cols.add(e.getBirthday());
		cols.add(e.getDayofdeath());
		cols.add(e.getOccupation());
		cols.add(e.getJColor());
		cols.add(e.getAttributes());
		getRowEnd(cols, entity);
		return cols;
	}

}
