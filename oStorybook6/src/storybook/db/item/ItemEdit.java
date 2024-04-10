/*
 * Copyright (C) 2019 favdb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.item;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.edit.AbstractEditor;
import storybook.edit.Editor;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSCheckList;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class ItemEdit extends AbstractEditor {

	private JComboBox cbCategory;
	private JSCheckList lRelations;

	public ItemEdit(Editor m, AbstractEntity e) {
		super(m, e, "111");
		initAll();
	}

	@Override
	public void initUpper() {
		Item item = (Item) entity;
		cbCategory = Ui.initCategory(pUpper, mainFrame.project.items.findCategories(), item.getCategory(), "010");
		SwingUtil.setCBsize(cbCategory);
		lRelations = Ui.initCkList(pUpper, mainFrame, Book.TYPE.RELATION,
		   mainFrame.project.relations.find(entity), tab1, BNONE);
	}

	@Override
	public boolean verifier() {
		resetError();
		return (msgError.isEmpty());
	}

	@Override
	public void apply() {
		Item item = (Item) entity;
		item.setCategory((String) cbCategory.getSelectedItem());
		super.apply();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton bt = (JButton) e.getSource();
			switch (bt.getName()) {
				default:
					break;

			}
		}
	}

}
