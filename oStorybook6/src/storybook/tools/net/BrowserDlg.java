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
package storybook.tools.net;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import api.mig.swing.MigLayout;
import i18n.I18N;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.dialog.AbsDialog;

/**
 *
 * @author favdb
 */
public class BrowserDlg extends AbsDialog {

	private final String url;
	private final String titleDlg;

	public BrowserDlg(String u, String t) {
		url = u;
		titleDlg = t;
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.WRAP + " 9"));
		setTitle(titleDlg);
		JEditorPane jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);
		JScrollPane scroller = new JScrollPane();
		scroller.setPreferredSize(new java.awt.Dimension(640, 480));
		scroller.setViewportView(jEditorPane);
		add(scroller, MIG.WRAP);
		try {
			jEditorPane.setPage(url);
		} catch (IOException ex) {
			jEditorPane.setText(I18N.getMsg("error.internet.connection.failed", url) + "\n");
		}
		add(getCloseButton(), MIG.RIGHT);
		pack();
		setLocationRelativeTo(null);
		setModal(true);
	}

	public static void show(String u, String t) {
		SwingUtil.showDialog(new BrowserDlg(u, t), null, true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
