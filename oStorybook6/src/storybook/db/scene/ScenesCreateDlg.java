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
package storybook.db.scene;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import storybook.db.EntityUtil;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.dialog.AbsDialog;
import storybook.tools.StringUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;

/**
 *
 * @author favdb
 */
public class ScenesCreateDlg extends AbsDialog {

	private JTextField tfQuantity;
	private JComboBox<Object> chapterCombo;
	private JTextField tfFormat;
	private JCheckBox rbRoman;

	public ScenesCreateDlg(MainFrame parent) {
		super(parent);
		mainFrame = parent;
		initUi();
		this.setLocationRelativeTo(mainFrame);
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(I18N.getMsg("scenes.generate"));
		add(new JLabel(I18N.getMsg("scenes.generate.text")), MIG.SPLIT2);
		tfQuantity = new JTextField();
		tfQuantity.setColumns(2);
		add(tfQuantity, MIG.WRAP);
		add(new JLabel(I18N.getMsg("scenes.generate.format")), MIG.WRAP);
		tfFormat = new JTextField(I18N.getMsg("scene") + " %d");
		add(tfFormat, MIG.get(MIG.GROWX, MIG.WRAP));
		rbRoman = new JCheckBox(I18N.getMsg("scenes.generate.roman"));
		add(rbRoman, MIG.get(MIG.RIGHT, MIG.WRAP));

		add(new JLabel(I18N.getMsg("chapter")), MIG.SPLIT2);
		chapterCombo = new JComboBox<>();
		Chapter chapter = new Chapter();
		EntityUtil.cbFill(mainFrame, chapterCombo, Book.TYPE.CHAPTER, chapter, false, false);
		add(chapterCombo, MIG.WRAP);
		add(getCancelButton(), MIG.get(MIG.SPLIT2, MIG.RIGHT));
		add(getOkButton(), MIG.RIGHT);
		pack();
		setLocationRelativeTo(mainFrame);
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int quant = 0;
				try {
					quant = Integer.parseInt(tfQuantity.getText());
				} catch (NumberFormatException evt) {
					// ignore
				}
				if (quant < 1 || quant > 20) {
					showError(I18N.getMsg("scenes.generate.number.error"));
					return;
				}
				if (!tfFormat.getText().contains("%d")) {
					showError(I18N.getMsg("scenes.generate.format.error"));
					return;
				}
				Chapter part = (Chapter) chapterCombo.getSelectedItem();
				int lastScene = mainFrame.project.scenes.getLastNumber();
				for (int i = 0; i < quant; ++i) {
					int number = i + lastScene + 1;
					Scene ch = new Scene();
					ch.setSceneno(number);
					ch.setChapter(part);
					if (rbRoman.isSelected()) {
						String x = StringUtil.intToRoman((int) ch.getSceneno());
						String f = tfFormat.getText().replace("%d", "%s");
						ch.setTitle(String.format(f, x));
					} else {
						String f = tfFormat.getText().replace("%d", "%02d");
						ch.setTitle(String.format(f, number));
					}
					mainFrame.getBookController().newEntity(ch);
				}
				canceled = false;
				dispose();
			}

		};
	}

	private void showError(String msg) {
		JOptionPane.showMessageDialog(this, msg, I18N.getMsg("error"), JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
