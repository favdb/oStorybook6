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
package storybook.dialog;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class ChooseFolderDlg extends AbsDialog {

	protected File folder;
	private JTextField tfDir;
	private JButton btChooseDir;
	private JLabel lbWarning;
	private final String msg;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public ChooseFolderDlg(MainFrame m, String msg) {
		super(m);
		this.msg = msg;
		init();
	}

	@Override
	public void init() {
		setLayout(new MigLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(I18N.getMsg("welcome.new.project"));
		setModal(true);
		//name
		JLabel jLabel1 = new JLabel("<html>" + I18N.getMsg(msg) + "</html>");
		add(jLabel1, MIG.WRAP);
		//folder
		JLabel jLabel2 = new JLabel(I18N.getMsg("folder"));
		add(jLabel2, "split 3");
		tfDir = new javax.swing.JTextField();
		tfDir.setColumns(32);
		tfDir.setEditable(false);
		add(tfDir);
		btChooseDir = new JButton();
		btChooseDir.setMargin(new Insets(0, 0, 0, 0));
		btChooseDir.setIcon(IconUtil.getIconSmall(ICONS.K.F_OPEN));
		btChooseDir.setToolTipText(I18N.getMsg("folder.choose"));
		btChooseDir.addActionListener((java.awt.event.ActionEvent evt) -> {
			btChooseDir();
		});
		add(btChooseDir, MIG.WRAP);
		lbWarning = new JLabel();
		lbWarning.setForeground(java.awt.Color.red);
		//ok cancel
		add(getCancelButton(), MIG.get(MIG.SPLIT2, MIG.RIGHT));
		add(getOkButton(), MIG.RIGHT);

		this.pack();
		setLocationRelativeTo(mainFrame);
		setModal(true);
	}

	private void btChooseDir() {
		final JFileChooser fc = new JFileChooser(tfDir.getText());
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int ret = fc.showOpenDialog(this);
		if (ret != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File dir = fc.getSelectedFile();
		tfDir.setText(dir.getAbsolutePath());
	}

	@Override
	public AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applySettings();
			}
		};
	}

	private void applySettings() {
		File dir = new File(tfDir.getText());
		if (!dir.isDirectory() || !dir.canWrite() || !dir.canExecute()) {
			lbWarning.setText(I18N.getMsg("file.new.not.writable"));
			return;
		}
		folder = new File(tfDir.getText());
		if (folder.exists()) {
			this.dispose();
		}
	}

	public File getFolder() {
		return folder;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
