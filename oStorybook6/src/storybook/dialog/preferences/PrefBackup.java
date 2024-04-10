/*
 * Copyright (C) 2020 favdb
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
 * You should have received a fileCopy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.dialog.preferences;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Pref;
import storybook.tools.file.IOUtil;
import storybook.tools.swing.js.JSLabel;
import storybook.ui.MIG;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public class PrefBackup extends AbstractPanel {

	private static final String BACKUP_PREF = "file.backup.pref.";
	private final PreferencesDlg caller;
	private Pref pref;
	private JCheckBox ckBackupOpen;
	private JCheckBox ckBackupClose;
	private JTextField tfBackupDir;
	private JCheckBox ckBackupIncrement;
	private JSLabel lbBackupDir;
	private JSLabel lbAuto;

	public PrefBackup(PreferencesDlg dlg) {
		super(dlg.getMainFrame());
		caller = dlg;
		initAll();
	}

	@Override
	public void init() {
		pref = App.preferences;
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.WRAP, "[][]"));
		lbBackupDir = new JSLabel(I18N.getMsg(BACKUP_PREF + "dir"));
		add(lbBackupDir, MIG.SPAN);
		tfBackupDir = new JTextField();
		tfBackupDir.setText(pref.getString(Pref.KEY.BACKUP_DIR, ""));
		add(tfBackupDir, MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.GROWX));
		JButton btBackupDir = new JButton();
		btBackupDir.setIcon(IconUtil.getIconSmall(ICONS.K.F_OPEN));
		btBackupDir.setMargin(new Insets(0, 0, 0, 0));
		btBackupDir.addActionListener((java.awt.event.ActionEvent evt) -> {
			File dir = IOUtil.directorySelect(null, "");
			if (dir == null) {
				return;
			}
			tfBackupDir.setText(dir.getAbsolutePath());
			tfBackupDir.setCaretPosition(0);
		});
		add(btBackupDir);
		ckBackupIncrement = new JCheckBox(I18N.getMsg(BACKUP_PREF + "increment"));
		ckBackupIncrement.setSelected(pref.getBoolean(Pref.KEY.BACKUP_INCREMENT, false));
		add(ckBackupIncrement, MIG.SPAN);
		lbAuto = new JSLabel(I18N.getColonMsg(BACKUP_PREF + "auto"));
		add(lbAuto);
		ckBackupOpen = new JCheckBox(I18N.getMsg(BACKUP_PREF + "auto.open"));
		ckBackupOpen.setSelected(pref.getBoolean(Pref.KEY.BACKUP_OPEN, false));
		add(ckBackupOpen);
		ckBackupClose = new JCheckBox(I18N.getMsg(BACKUP_PREF + "auto.close"));
		ckBackupClose.setSelected(pref.getBoolean(Pref.KEY.BACKUP_CLOSE, false));
		add(ckBackupClose, "skip 1");
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	public void apply() {
		pref.setString(Pref.KEY.BACKUP_DIR, tfBackupDir.getText());
		pref.setBoolean(Pref.KEY.BACKUP_OPEN, ckBackupOpen.isSelected());
		pref.setBoolean(Pref.KEY.BACKUP_CLOSE, ckBackupClose.isSelected());
		pref.setBoolean(Pref.KEY.BACKUP_INCREMENT, ckBackupIncrement.isSelected());
	}

	public void refreshUi() {
		lbBackupDir.setText(I18N.getMsg(BACKUP_PREF + "dir"));
		ckBackupIncrement.setText(I18N.getMsg(BACKUP_PREF + "increment"));
		lbAuto.setText(I18N.getColonMsg(BACKUP_PREF + "auto"));
		ckBackupOpen.setText(I18N.getMsg(BACKUP_PREF + "auto.open"));
		ckBackupClose.setText(I18N.getMsg(BACKUP_PREF + "auto.close"));
	}

}
