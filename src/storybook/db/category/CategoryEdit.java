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
package storybook.db.category;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import storybook.Const;
import storybook.db.DB.DATA;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.edit.AbstractEditor;
import storybook.edit.Editor;
import storybook.tools.StringUtil;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class CategoryEdit extends AbstractEditor {

	private JTextField tfSort;
	private JComboBox cbSup;

	public CategoryEdit(Editor m, AbstractEntity e) {
		super(m, e, "110");
		initAll();
	}

	@Override
	public void initUpper() {
		Category category = (Category) entity;
		tfSort = Ui.initIntegerField(pUpper, "sort", 5, category.getSort(), BMANDATORY);
		if (category.getSort() < 1) {
			tfSort.setText("+");
		}
		cbSup = Ui.initAutoCombo(pUpper, mainFrame, DATA.CATEGORY_SUP,
		   Book.TYPE.CATEGORY, category.getSup(), category, "001");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean verifier() {
		JTextField tf = new JTextField();
		resetError();
		tfSort.setBackground(tf.getBackground());
		if (tfSort.getText().equals("+")) {
			tfSort.setText(Category.getSortNext((List) mainFrame.project.getList(Book.TYPE.CATEGORY)).toString());
		}
		if (tfSort.getText().isEmpty()) {
			errorMsg(tfSort, Const.ERROR_MISSING);
		} else if (!StringUtil.isNumeric(tfSort.getText())) {
			errorMsg(tfSort, Const.ERROR_NOTNUMERIC);
		}
		return (msgError.isEmpty());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void apply() {
		Category category = (Category) entity;
		category.setName(tfName.getText());
		if (tfSort.getText().equals("+")) {
			category.setSort(Category.getSortNext((List) mainFrame.project.getList(Book.TYPE.CATEGORY)));
		} else {
			category.setSort(Integer.valueOf(tfSort.getText()));
		}
		if (cbSup.getSelectedIndex() > 0) {
			category.setSup((Category) cbSup.getSelectedItem());
		} else {
			category.setSup(null);
		}
		super.apply();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
