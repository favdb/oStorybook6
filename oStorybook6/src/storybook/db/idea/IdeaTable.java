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
package storybook.db.idea;

import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JToolBar;
import storybook.db.DB;
import storybook.db.abs.AbsColumn;
import static storybook.db.abs.AbsColumn.*;
import storybook.db.abs.AbsTable;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.category.Categorys;
import storybook.ui.frames.main.MainFrame;
import static storybook.ui.panels.AbstractPanel.ALL;
import static storybook.ui.panels.AbstractPanel.EMPTY;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class IdeaTable extends AbsTable {

	private JComboBox cbCategories;

	public IdeaTable(MainFrame mainFrame) {
		super(mainFrame, Book.TYPE.IDEA);
	}

	@Override
	public void init() {
		this.withPart = false;
	}

	@Override
	public JToolBar initToolbar() {
		super.initToolbar();
		this.addCbCategories(Categorys.find(mainFrame, getType()), null, !EMPTY, ALL);
		int nx = mainFrame.getBook().getParam().getParamFilters().getIdeasCat();
		if (nx != -1) {
			cbCategories.setSelectedIndex(nx);
		}
		cbCategories.addActionListener(this);
		return toolbar;
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public List<AbstractEntity> getAllEntities() {
		//LOG.printInfos(TT+".getAllEntities()");
		List list = mainFrame.project.getList(getType());
		if (cbCategories != null && cbCategories.getSelectedIndex() > 0) {
			list = mainFrame.project.ideas.findByCategory((String) cbCategories.getSelectedItem());
		}
		return (list);
	}

	@Override
	protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
		//LOG.traceEvent("IdeaTable.modelPropertyChangeLocal",evt);
		if (evt.getPropertyName().equals("IDEA_Update")) {
			reloadCategories();
		}
		//no specific change
	}

	@SuppressWarnings("unchecked")
	private void reloadCategories() {
		List<String> list = Categorys.find(mainFrame, getType());
		String toSel = (String) cbCategories.getSelectedItem();
		cbCategories.removeAllItems();
		if (list != null && !list.isEmpty()) {
			list.forEach((e) -> {
				cbCategories.addItem((String) e);
			});
		}
		cbCategories.setSelectedItem(toSel);
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		return (Idea) mainFrame.project.get(Book.TYPE.IDEA, id);
	}

	@Override
	public void updateRow(AbstractEntity entity) {
		// nothing
	}

	@Override
	public List<AbsColumn> getColumns(AbstractEntity entity) {
		List<AbsColumn> cols = super.getColumns(entity);

		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.STATUS, TCR_STATUS));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.CATEGORY));

		this.getColumnsEnd(cols, entity);

		return (cols);
	}

	@Override
	public List<Object> getRow(AbstractEntity entity) {
		List<Object> cols = super.getRow(entity);
		Idea e = (Idea) entity;
		cols.add(e.getStatus());
		cols.add(e.getCategory());
		getRowEnd(cols, entity);
		return cols;
	}

}
