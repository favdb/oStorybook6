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
package storybook.db.gender;

import java.beans.PropertyChangeEvent;
import java.util.List;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.ctrl.ActKey;
import storybook.db.DB;
import storybook.db.abs.AbsColumn;
import static storybook.db.abs.AbsColumn.*;
import storybook.db.abs.AbsTable;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.ui.frames.main.MainFrame;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class GenderTable extends AbsTable {

	public GenderTable(MainFrame mainFrame) {
		super(mainFrame, Book.TYPE.GENDER);
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
		if (isInit(act)) {
			return;
		}
		if (Book.getTYPE(act.type) == Book.TYPE.GENDER
				&& (act.isNew() || act.isUpdate() || act.isDelete())) {
			fillTable();
		}
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		Gender gender = (Gender) mainFrame.project.get(Book.TYPE.GENDER, id);
		return gender;
	}

	@Override
	public void updateRow(AbstractEntity entity) {
		// nothing
	}

	@Override
	public List<AbsColumn> getColumns(AbstractEntity entity) {
		List<AbsColumn> cols = super.getColumns(entity);

		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.ICON_FILE, TCR_HIDE, TCR_ICON));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.GENDER_CHILDHOOD, NUMERIC, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.GENDER_ADOLESCENCE, NUMERIC, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.GENDER_ADULTHOOD, NUMERIC, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.GENDER_RETIREMENT, NUMERIC, AL_CENTER));

		this.getColumnsEnd(cols, entity);

		return (cols);
	}

	@Override
	public List<Object> getRow(AbstractEntity entity) {
		List<Object> cols = super.getRow(entity);
		Gender e = (Gender) entity;
		if (e.getIcone().isEmpty()) {
			cols.add(IconUtil.getIconSmall(ICONS.K.EMPTY));
		} else {
			cols.add(e.getIcon());
		}
		cols.add(e.getChildhood());
		cols.add(e.getAdolescence());
		cols.add(e.getAdulthood());
		cols.add(e.getRetirement());
		getRowEnd(cols, entity);
		return cols;
	}

}
