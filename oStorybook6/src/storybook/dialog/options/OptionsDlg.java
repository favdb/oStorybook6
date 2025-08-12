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
import javax.swing.JTabbedPane;
import resources.icons.IconUtil;
import storybook.dialog.AbsDialog;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.SbView;

/**
 *
 * @author favdb
 */
public class OptionsDlg extends AbsDialog {

	private final String sbView;
	private OptsBook optsBook;
	private OptsChrono optsChrono;
	private OptsManage optsManage;
	private OptsTimeline optsTimeline;
	private OptsTree optsTree;

	public OptionsDlg(MainFrame m) {
		super(m);
		sbView = null;
		init();
		initUi();
	}

	public OptionsDlg(MainFrame m, String v) {
		super(m);
		sbView = v;
		init();
		initUi();
	}

	public static void show(MainFrame m, String v) {
		OptionsDlg dlg = new OptionsDlg(m, v);
		dlg.setVisible(true);
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		optsBook = new OptsBook(mainFrame);
		optsChrono = new OptsChrono(mainFrame);
		optsManage = new OptsManage(mainFrame);
		optsTimeline = new OptsTimeline(mainFrame);
		optsTree = new OptsTree(mainFrame);
		//layout
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP1)));
		setTitle(I18N.getMsg("options"));
		setIconImage(IconUtil.getIconImage("icon"));
		if (sbView == null) {
			JTabbedPane tabbed = new JTabbedPane();
			tabbed.add(I18N.getMsg("view.book"), optsBook);
			tabbed.add(I18N.getMsg("view.chrono"), optsChrono);
			tabbed.add(I18N.getMsg("view.manage"), optsManage);
			tabbed.add(I18N.getMsg("view.tree"), optsTree);
			add(tabbed);
		} else {
			switch (SbView.getVIEW(sbView)) {
				case BOOK:
					setTitle(I18N.getMsg("view.book"));
					add(optsBook);
					break;
				case CHRONO:
					setTitle(I18N.getMsg("view.chrono"));
					add(optsChrono);
					break;
				case MANAGE:
					setTitle(I18N.getMsg("view.manage"));
					add(optsManage);
					break;
				case TIMELINE:
					setTitle(I18N.getMsg("view.timeline"));
					add(optsTimeline);
					break;
				case TREE:
					setTitle(I18N.getMsg("view.tree"));
					add(optsTree);
					break;
			}
		}
		add(getOkButton(), MIG.get(MIG.SPAN, MIG.SG, MIG.RIGHT));
		pack();
		setLocationRelativeTo(mainFrame);
		setModal(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
