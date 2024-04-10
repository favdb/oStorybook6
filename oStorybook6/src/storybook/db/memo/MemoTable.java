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
package storybook.db.memo;

import java.beans.PropertyChangeEvent;
import java.util.List;
import storybook.db.abs.AbstractEntity;
import storybook.db.abs.AbsTable;
import storybook.db.book.Book;
import storybook.ui.MainFrame;

/**
 * this table is not used
 *
 * @author favdb
 *
 */
@SuppressWarnings("serial")
public class MemoTable extends AbsTable {

	/**
	 * this MemoTable is not used, see the MemosPanel
	 *
	 * @param mainFrame
	 */
	public MemoTable(MainFrame mainFrame) {
		super(mainFrame, Book.TYPE.MEMO);
	}

	@Override
	public void init() {
		//LOG.printInfos(TT + ".init()");
		this.withPart = false;
		allowMultiDelete = true;
	}

	@Override
	protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
		//no specific change
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		return (Memo) mainFrame.project.get(Book.TYPE.MEMO, id);
	}

	@Override
	public void updateRow(AbstractEntity entity) {
		// nothing
	}

	@Override
	public List<Object> getRow(AbstractEntity entity) {
		List<Object> cols = super.getRow(entity);

		getRowEnd(cols, entity);
		return (cols);
	}

}
