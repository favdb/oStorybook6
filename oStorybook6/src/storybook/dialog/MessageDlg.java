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
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.ui.MIG;

/**
 *
 * @author favdb
 */
public class MessageDlg extends JDialog {

	private static final String TT = "InformationDlg";

	private final String msg;
	private final String title;
	private JButton btOk;
	private JButton btCancel;
	private boolean canceled = false;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public MessageDlg(JFrame frame, String msg, String title) {
		super(frame);
		this.msg = msg;
		this.title = title;
		initialize();
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public MessageDlg(JDialog dialog, String msg, String title) {
		super(dialog);
		this.msg = msg;
		this.title = title;
		initialize();
	}

	public void initialize() {
		//LOG.trace(TT + ".initialize()");
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP, MIG.HIDEMODE3), "[][]"));
		add(new JLabel(IconUtil.getIconSmall(ICONS.K.INFO)), MIG.TOP);
		JLabel ta = new JLabel();
		ta.setText("<html>" + msg.replaceAll("\\n", "<br>") + "</html>");
		ta.setMinimumSize(new Dimension(100, 100));
		add(ta, MIG.get(MIG.GROW, MIG.LEFT));
		btOk = new JButton(I18N.getMsg("ok"));
		btOk.setIcon(IconUtil.getIconSmall(ICONS.K.OK));
		btOk.addActionListener((java.awt.event.ActionEvent evt) -> {
			canceled = false;
			dispose();
		});
		btCancel = new JButton(I18N.getMsg("cancel"));
		btCancel.setIcon(IconUtil.getIconSmall(ICONS.K.CANCEL));
		btCancel.addActionListener((java.awt.event.ActionEvent evt) -> {
			canceled = true;
			dispose();
		});
		add(btOk, MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.RIGHT));
		add(btCancel, MIG.RIGHT);
		setTitle(I18N.getMsg(title));
		setIconImage(IconUtil.getIconImageSmall(ICONS.K.INFO));
		setMinimumSize(new Dimension(320, 100));
		pack();
		setLocationRelativeTo(this.getOwner());
		setModal(true);
	}

	public void setOkButton(String text) {
		btOk.setText(text);
	}

	public void hideCancel() {
		btCancel.setVisible(false);
	}

	public void setCancelButton(String text) {
		btCancel.setText(text);
	}

	public static boolean show(Container comp,
			String msg, String title, boolean... options) {
		MessageDlg dlg = new MessageDlg((JFrame) null, msg, title);
		Container p = comp;
		while (p != null
				&& !(p instanceof JFrame)
				&& !(p instanceof JDialog)) {
			if ((p instanceof JFrame) || (p instanceof JDialog)) {
				if (p instanceof JFrame) {
					dlg = new MessageDlg((JFrame) p, msg, title);
				} else {
					dlg = new MessageDlg((JDialog) p, msg, title);
				}
				break;
			}
			p = p.getParent();
		}
		if (options != null) {
			dlg.hideCancel();
		}
		dlg.setLocationRelativeTo(comp);
		dlg.setVisible(true);
		return !dlg.canceled;
	}

}
