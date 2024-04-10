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
package storybook.db.tag;

import i18n.I18N;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.JToolBar;
import storybook.ctrl.ActKey;
import storybook.db.DB;
import storybook.db.abs.AbsColumn;
import storybook.db.abs.AbstractEntity;
import storybook.db.abs.AbsTable;
import storybook.db.book.Book;
import storybook.db.category.Categorys;
import storybook.ui.MainFrame;
import static storybook.ui.panel.AbstractPanel.ALL;
import static storybook.ui.panel.AbstractPanel.EMPTY;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class TagTable extends AbsTable {

	public TagTable(MainFrame mainFrame) {
		super(mainFrame, Book.TYPE.TAG);
	}

	@Override
	public void init() {
		this.withPart = false;
		allowMultiDelete = true;
	}

	@Override
	public JToolBar initToolbar() {
		super.initToolbar();
		super.addCbCategories(Categorys.find(mainFrame, getType()), null, !EMPTY, ALL);
		return toolbar;
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public List<AbstractEntity> getAllEntities() {
		//LOG.printInfos(TT+".getAllEntities()");
		List ls = mainFrame.project.getList(getType());
		if (cbCategories != null && cbCategories.getSelectedIndex() > 0) {
			ls = mainFrame.project.tags.findCategory((String) cbCategories.getSelectedItem());
		}
		return ls;
	}

	@Override
	protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
		ActKey act = new ActKey(evt);
		if (act.isUpdate()
		   && (act.getType() == Book.TYPE.TAG)) {
			fillTable();
		}
	}

	@Override
	public void updateRow(AbstractEntity entity) {
		// nothing
	}

	@Override
	public List<AbsColumn> getColumns(AbstractEntity entity) {
		List<AbsColumn> cols = super.getColumns(entity);

		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.TAG_CATEGORY));

		getColumnsEnd(cols, entity);

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

	@SuppressWarnings("unchecked")
	private void reloadCategories() {
		//LOG.printInfos(TT + ".reloadCategories()");
		int idx = cbCategories.getSelectedIndex();
		String n = (String) cbCategories.getSelectedItem();
		List<String> cats = mainFrame.project.tags.findCategories();
		cbCategories.removeAllItems();
		cbCategories.addItem(I18N.getMsg("all"));
		String nx = "";
		for (String c : cats) {
			cbCategories.addItem(c);
			if (c.equals(n)) {
				nx = c;
			}
		}
		cbCategories.setSelectedIndex(0);
		if (idx > 0 && !nx.isEmpty()) {
			cbCategories.setSelectedItem(n);
		}
	}

	@Override
	protected void deleteEntity(AbstractEntity entity) {
		//LOG.printInfos(TT + ".deleteEntity(" + AbstractEntity.printInfos(entity) + ")");
		reloadCategories();
		super.deleteEntity(entity);
	}

	@Override
	protected void newEntity(AbstractEntity entity) {
		//LOG.printInfos(TT + ".newEntity(" + AbstractEntity.printInfos(entity) + ")");
		reloadCategories();
		super.newEntity(entity);
	}

	@Override
	protected void updateEntity(AbstractEntity entity) {
		//LOG.printInfos(TT + ".updateEntity(" + AbstractEntity.printInfos(entity) + ")");
		reloadCategories();
		super.updateEntity(entity);
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		return (Tag) mainFrame.project.get(Book.TYPE.TAG, id);
	}

}
