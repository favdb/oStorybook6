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
package storybook.db.part;

import api.mig.swing.MigLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Objects;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import storybook.Const;
import storybook.db.DB.DATA;
import storybook.db.Objective;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.book.BookUtil;
import storybook.edit.AbstractEditor;
import storybook.edit.Editor;
import storybook.tools.StringUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class PartEdit extends AbstractEditor {

	private JTextField tfNumber;
	private JComboBox cbSup;
	private Objective objective;

	public PartEdit(Editor m, AbstractEntity e) {
		super(m, e, "111");
		initAll();
	}

	@Override
	public void initUpper() {
		Part part = (Part) entity;
		mainFrame.lastPartSet(part);
		JPanel panel0 = new JPanel(
		   new MigLayout(MIG.get(MIG.INS1, MIG.WRAP, MIG.HIDEMODE3), "[][]"));
		JPanel panel1 = new JPanel(
		   new MigLayout(MIG.get(MIG.INS1, MIG.WRAP, MIG.HIDEMODE3), "[][grow]"));
		tfNumber = Ui.initIntegerField(panel0, "number", 5, part.getNumber(), BMANDATORY);
		if (part.getNumber() < 1) {
			tfNumber.setText("+");
		}
		cbSup = Ui.initAutoCombo(panel0, mainFrame, DATA.PART_SUP, Book.TYPE.PART,
		   part.getSuperpart(), part, "001");
		objective = new Objective(entity,
		   BookUtil.getNbChars(mainFrame.project, mainFrame.project.chapters.find(part)));
		panel1.add(objective.getPanel(this), "span");
		pUpper.add(panel0, MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.TOP));
		pUpper.add(panel1);
	}

	@Override
	public boolean verifier() {
		resetError();
		@SuppressWarnings("unchecked")
		List<Part> parts = (List) mainFrame.project.getList(Book.TYPE.PART);
		Part part = (Part) entity;
		if (tfNumber.getText().equals("+")) {
			tfNumber.setText(Part.getNextNumber(parts).toString());
		}
		if (tfNumber.getText().isEmpty()) {
			errorMsg(tfNumber, Const.ERROR_MISSING);
		} else if (!StringUtil.isNumeric(tfNumber.getText())) {
			errorMsg(tfNumber, Const.ERROR_NOTNUMERIC);
		} else {
			int n = Integer.parseInt(tfNumber.getText());
			Part b = Part.findNumber(parts, n);
			if (b != null && (b.getObjType().equals(part.getObjType()) && !Objects.equals(b.getId(), part.getId()))) {
				errorMsg(tfNumber, Const.ERROR_EXISTS);
				tfNumber.setBackground(Color.red);
			}
		}
		objective.check(this);
		return (msgError.isEmpty());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void apply() {
		Part part = (Part) entity;
		if (tfNumber.getText().equals("+")) {
			part.setNumber(Part.getNextNumber((List) mainFrame.project.getList(Book.TYPE.PART)));
		} else {
			part.setNumber(Integer.valueOf(tfNumber.getText()));
		}
		if (cbSup.getSelectedIndex() > 0) {
			part.setSuperpart((Part) cbSup.getSelectedItem());
		} else {
			part.setSuperpart(null);
		}
		objective.apply(entity);
		mainFrame.lastPartSet(part);
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
