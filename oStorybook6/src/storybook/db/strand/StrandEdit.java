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
package storybook.db.strand;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import resources.icons.ICONS;
import storybook.Const;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.edit.AbstractEditor;
import storybook.edit.Editor;
import storybook.tools.StringUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;
import storybook.dialog.chooser.ColorPicker;

/**
 *
 * @author favdb
 */
public class StrandEdit extends AbstractEditor {

	private JTextField tfAbbr;
	private JTextField tfSort;
	private ColorPicker cbColor;
	private JButton btColor;

	public StrandEdit(Editor m, AbstractEntity e) {
		super(m, e, "111");
		initAll();
	}

	@Override
	public void initUpper() {
		Strand strand = (Strand) entity;
		tfAbbr = Ui.initStringField(pUpper, "abbreviation", 6, strand.getAbbreviation(), BMANDATORY);
		tfSort = Ui.initStringField(pUpper, "sort", 4, strand.getSort().toString(), BMANDATORY);
		if (strand.getSort() < 1) {
			tfSort.setText("+");
		}
		JPanel p = new JPanel(new MigLayout(MIG.INS0));
		cbColor = new ColorPicker(strand.getJColor());
		p.add(cbColor);
		btColor = Ui.initButton("btColor", " ", ICONS.K.COLOR, "color.other", e -> changeColor());
		btColor.setHorizontalAlignment(SwingConstants.LEFT);
		if (cbColor.getSelectedIndex() < 1) {
			btColor.setBackground(strand.getJColor());
		}
		p.add(btColor);
		Ui.addField(pUpper, "color", "", p, null, BMANDATORY);
	}

	@Override
	public boolean verifier() {
		resetError();
		if (tfAbbr.getText().isEmpty()) {
			errorMsg(tfAbbr, Const.ERROR_MISSING);
		}
		@SuppressWarnings("unchecked")
		List<Strand> strands = (List) mainFrame.project.getList(Book.TYPE.STRAND);
		if (tfSort.getText().equals("+")) {
			tfSort.setText(Strand.getNextNumber(strands).toString());
		}
		if (tfSort.getText().isEmpty()) {
			errorMsg(tfSort, Const.ERROR_MISSING);
		} else if (!StringUtil.isNumeric(tfSort.getText())) {
			errorMsg(tfSort, Const.ERROR_NOTNUMERIC);
		} else {
			int n = Integer.parseInt(tfSort.getText());
			@SuppressWarnings("unchecked")
			Strand c = Strand.findSort(strands, n);
			if (c != null && !Objects.equals(c.getId(), entity.getId())) {
				errorMsg(tfSort, Const.ERROR_EXISTS);
			}
		}
		return (msgError.isEmpty());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void apply() {
		Strand pov = (Strand) entity;
		pov.setName(tfName.getText());
		pov.setAbbreviation(tfAbbr.getText());
		if (tfSort.getText().equals("+")) {
			pov.setSort(Strand.getNextNumber((List) mainFrame.project.getList(Book.TYPE.STRAND)));
		} else {
			pov.setSort(Integer.valueOf(tfSort.getText()));
		}
		if (cbColor.getSelectedIndex() == -1) {
			pov.setJColor(btColor.getBackground());
		} else {
			pov.setJColor((Color) cbColor.getSelectedItem());
		}
		super.apply();
	}

	private void changeColor() {
		Color color = JColorChooser.showDialog(this, I18N.getMsg("color.choose"), btColor.getBackground());
		btColor.setBackground(color);
		cbColor.setSelectedIndex(-1);
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
