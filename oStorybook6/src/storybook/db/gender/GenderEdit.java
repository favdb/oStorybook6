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
package storybook.db.gender;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.JTextField;
import storybook.Const;
import storybook.db.abs.AbstractEntity;
import storybook.edit.AbstractEditor;
import storybook.edit.Editor;
import storybook.tools.StringUtil;
import static storybook.ui.Ui.*;
import storybook.dialog.chooser.ImageChooserPanel;

/**
 *
 * @author favdb
 */
public class GenderEdit extends AbstractEditor {

	private JTextField tfChild;
	private JTextField tfAdo;
	private JTextField tfAdult;
	private JTextField tfRetire;
	private ImageChooserPanel tfIcon;

	public GenderEdit(Editor m, AbstractEntity e) {
		super(m, e, "110");
		initAll();
	}

	@Override
	public void initUpper() {
		Gender gender = (Gender) entity;
		tfChild = initIntegerField(pUpper, "gender.childhood", 5, gender.getChildhood(), BNONE);
		tfAdo = initIntegerField(pUpper, "gender.adolescence", 5, gender.getAdolescence(), BNONE);
		tfAdult = initIntegerField(pUpper, "gender.adulthood", 5, gender.getAdulthood(), BNONE);
		tfRetire = initIntegerField(pUpper, "gender.retirement", 5, gender.getRetirement(), BNONE);
		tfIcon = initIconChooser(pUpper, "icon.file", gender.getIcone(), "");
		String picon = gender.getIconName();
		if (picon.equals("person")) {
			editor.setIcon("new_gender");
		} else {
			editor.setIcon(picon);
		}
	}

	@Override
	public boolean verifier() {
		resetError();
		int child = 0, ado = 0, adult = 0, retire = 0;
		if (!StringUtil.isNumeric(tfChild.getText())) {
			errorMsg(tfChild, Const.ERROR_NOTNUMERIC);
		} else {
			child = Integer.parseInt(tfChild.getText());
		}
		if (!StringUtil.isNumeric(tfAdo.getText())) {
			errorMsg(tfAdo, Const.ERROR_NOTNUMERIC);
		} else {
			ado = Integer.parseInt(tfAdo.getText());
		}
		if (!StringUtil.isNumeric(tfAdult.getText())) {
			errorMsg(tfAdult, Const.ERROR_NOTNUMERIC);
		} else {
			adult = Integer.parseInt(tfAdult.getText());
		}
		if (!StringUtil.isNumeric(tfRetire.getText())) {
			errorMsg(tfRetire, Const.ERROR_NOTNUMERIC);
		} else {
			retire = Integer.parseInt(tfRetire.getText());
		}
		if (child >= ado) {
			errorMsg(tfAdo, Const.ERROR_WRONG);
		}
		if (ado >= adult) {
			errorMsg(tfAdult, Const.ERROR_WRONG);
		}
		if (adult >= retire) {
			errorMsg(tfRetire, Const.ERROR_WRONG);
		}
		return (msgError.isEmpty());
	}

	@Override
	public void apply() {
		Gender gender = (Gender) entity;
		gender.setName(tfName.getText());
		gender.setChildhood(Integer.valueOf(tfChild.getText()));
		gender.setAdolescence(Integer.valueOf(tfAdo.getText()));
		gender.setAdulthood(Integer.valueOf(tfAdult.getText()));
		gender.setRetirement(Integer.valueOf(tfRetire.getText()));
		gender.setIcone(tfIcon.getIconFile());
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
