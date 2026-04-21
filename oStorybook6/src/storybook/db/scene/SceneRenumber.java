/*
 * Copyright (C) 2018 FaVdB
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
import java.awt.event.KeyEvent;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import storybook.App;
import storybook.Pref;
import storybook.dialog.AbsDialog;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;

/**
 *
 * @author FaVdB
 */
public class SceneRenumber extends AbsDialog {

	private static final String TT = "RenumScene";

	private JRadioButton rbAuto;
	private JTextField tfInc;
	private JCheckBox ckChapter;
	private JLabel lbInc;
	private JRadioButton rbInc;

	public SceneRenumber(MainFrame m) {
		super(m);
		initAll();
	}

	public static boolean show(MainFrame m) {
		//LOG.trace(TT + ".show(mainFrame)");
		SceneRenumber dlg = new SceneRenumber(m);
		dlg.setVisible(true);
		if (!dlg.canceled) {
			dlg.saveChoice();
		}
		return !dlg.canceled;
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout());
		setTitle(I18N.getMsg("scenes.renumber"));
		add(new JLabel(I18N.getMsg("scenes.renumber")), "wrap");
		//selection for type of numerotation
		ButtonGroup group = new ButtonGroup();
		rbAuto = new JRadioButton(I18N.getMsg("scenes.renumber.auto"));
		rbAuto.addActionListener(e -> setRenum());
		group.add(rbAuto);
		add(rbAuto, MIG.WRAP);
		rbInc = new JRadioButton(I18N.getMsg("scenes.renumber.inc"));
		rbInc.addActionListener(e -> setRenum());
		group.add(rbInc);
		add(rbInc, MIG.get(MIG.SPAN, MIG.SPLIT + " 3"));
		lbInc = new JLabel(I18N.getMsg("scenes.renumber.inc_by"));
		add(lbInc);
		// if incremental numerotation
		tfInc = new JTextField();
		tfInc.setColumns(3);
		tfInc.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent evt) {
				char c = evt.getKeyChar();
				if (c >= '0' && c <= '9') {
				} else {
					int len = tfInc.getText().length();
					if (len >= 2) {
						evt.consume();
					}
				}
			}
		});
		add(tfInc);
		// numerotation by chapter, only for incremental type
		ckChapter = new JCheckBox(I18N.getMsg("scenes.renumber.bychapter"));
		ckChapter.setSelected(App.preferences.sceneIsRenumByChapter());
		add(ckChapter, MIG.WRAP);
		//set the initial values
		if (mainFrame.getPref().sceneIsRenumAuto()) {
			rbAuto.setSelected(true);
		} else {
			rbInc.setSelected(true);
		}
		tfInc.setText("" + App.preferences.sceneGetRenumInc());
		setRenum();
		//finalize the dialog
		add(getCancelButton(), "span,split,right");
		add(getOkButton(), "right");
		pack();
		setLocationRelativeTo(mainFrame);
		this.setModal(true);
	}

	private void setRenum() {
		tfInc.setEnabled(!rbAuto.isSelected());
		lbInc.setEnabled(!rbAuto.isSelected());
		ckChapter.setEnabled(!rbAuto.isSelected());
	}

	public void saveChoice() {
		//LOG.trace(TT + ".saveChoice()");
		String str = (rbAuto.isSelected() ? "1" : "0")
		   + ";" + tfInc.getText()
		   + ";" + (ckChapter.isSelected() ? "1" : "0");
		mainFrame.getPref().setString(Pref.KEY.SCENE_RENUM, str);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
