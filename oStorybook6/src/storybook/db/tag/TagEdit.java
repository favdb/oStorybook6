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
package storybook.db.tag;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.JComboBox;
import storybook.db.abs.AbstractEntity;
import storybook.edit.AbstractEditor;
import storybook.edit.Editor;
import storybook.tools.swing.SwingUtil;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class TagEdit extends AbstractEditor {

	private JComboBox cbCategory;
	//private ImageChooserPanel tfIcon;
	//private CheckBoxPanel lsPhoto;

	public TagEdit(Editor m, AbstractEntity e) {
		super(m, e, "110");
		initAll();
	}

	@Override
	public void initUpper() {
		Tag tag = (Tag) entity;
		cbCategory = Ui.initCategory(pUpper, mainFrame.project.tags.findCategories(), tag.getCategory(), "010");
		SwingUtil.setCBsize(cbCategory);
	}

	@Override
	public boolean verifier() {
		resetError();
		return (msgError.isEmpty());
	}

	@Override
	public void apply() {
		Tag tag = (Tag) entity;
		tag.setCategory((String) cbCategory.getSelectedItem());
		//tag.setIcone(tfIcon.getIconFile());
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
