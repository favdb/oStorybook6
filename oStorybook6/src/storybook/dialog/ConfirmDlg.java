/*
 * Copyright (C) 2022 favdb
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

import i18n.I18N;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import api.mig.swing.MigLayout;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;

/**
 *
 * @author favdb
 */
public class ConfirmDlg extends AbsDialog {

	private static final String TT = "ConfirmDlg";

	public static final int IGNORE = 2, OK = 1, CANCEL = 0;

	public static int show(MainFrame parent, String title, String message, boolean ignore) {
		//LOG.trace(TT + ".show(parent, title=" + title + ", message=" + message + ", " + (ignore ? "ignore" : ""));
		ConfirmDlg dlg = new ConfirmDlg(parent, title, message, ignore);
		dlg.setVisible(true);
		return dlg.getResult();
	}

	public static int show(MainFrame parent, String title, String message, boolean ignore, String okButton) {
		ConfirmDlg dlg = new ConfirmDlg(parent, title, message, ignore);
		dlg.setOkButton(okButton);
		dlg.setVisible(true);
		return dlg.getResult();
	}

	private final String message;
	private final boolean btIgnore;
	private int result;
	private JButton okButton;

	public ConfirmDlg(MainFrame parent, String title, String message, boolean ignore) {
		super(parent);
		this.message = message;
		this.btIgnore = ignore;
		this.setTitle(title);
		initAll();
	}

	@Override
	public void init() {
		//empty
	}

	@Override
	public void initUi() {
		this.setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP), "[][]"));
		this.add(new JLabel(IconUtil.getIconSmall(ICONS.K.INFO)));
		JLabel ta = new JLabel();
		ta.setText("<html>" + message.replaceAll("\\n", "<br>") + "</html>");
		ta.setMinimumSize(new Dimension(100, 100));
		add(ta, MIG.get(MIG.GROW, MIG.LEFT));
		if (btIgnore) {
			this.add(initButton("ignore"), MIG.get(MIG.SPAN, MIG.SPLIT + " 2", MIG.RIGHT));
		}
		okButton = initButton("ok");
		this.add(okButton, MIG.get(MIG.SPAN, MIG.SPLIT + " 2", MIG.RIGHT));
		this.add(initButton("cancel"), MIG.get(MIG.SPAN, MIG.RIGHT));
		this.pack();
		this.setLocationRelativeTo(this.getParent());
		this.setModal(true);
	}

	private JButton initButton(String text) {
		JButton bt = new JButton(I18N.getMsg(text));
		bt.setName(text);
		bt.addActionListener(this);
		return bt;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton bt = (JButton) e.getSource();
			switch (bt.getName()) {
				case "ignore":
					result = IGNORE;
					break;
				case "ok":
					result = OK;
					break;
				case "cancel":
					result = CANCEL;
					break;
				default:
					return;
			}
			this.dispose();
		}
	}

	private int getResult() {
		return result;
	}

	private void setOkButton(String text) {
		this.okButton.setText(text);
	}

}
