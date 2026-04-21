/*
 * Copyright (C) 2017 favdb
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
package storybook.dialog.options;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import storybook.App;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.SbView;
import storybook.ui.panels.AbstractOptions;

/**
 *
 * @author favdb
 */
public class OptsTree extends AbstractOptions implements CaretListener, ActionListener {

	private JCheckBox cbChar;
	private JTextField tfChar;

	public OptsTree(MainFrame m) {
		super(m);
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout("wrap,fill"));
		// tree text troncation
		add(new JLabel(I18N.getMsg("tree.trunc")));
		cbChar = new JCheckBox(I18N.getMsg("tree.trunc.char"));
		cbChar.addActionListener(this);
		add(cbChar, "split 3");
		tfChar = new JTextField();
		tfChar.setName("tfChar");
		tfChar.setColumns(3);
		tfChar.setText("8");
		add(tfChar);
		add(new JLabel(I18N.getMsg("characters")));
		if (App.preferences.treeviewGetTrunc()) {
			cbChar.setSelected(true);
			tfChar.setText("" + App.preferences.treeviewGetChar());
		}
		tfChar.addCaretListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		save();
	}

	public void save() {
		App.preferences.treeviewSetTrunc(cbChar.isSelected());
		if (cbChar.isSelected()) {
			int nbchar = Integer.parseInt(tfChar.getText());
			App.preferences.treeviewSetChar(nbchar);
		}
		mainFrame.getBookController().refresh(mainFrame.getView(SbView.VIEWNAME.TREE));
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		if (e.getSource() instanceof JTextField && !tfChar.getText().isEmpty()) {
			save();
		}
	}

}
