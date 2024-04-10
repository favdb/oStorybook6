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
package storybook.ideabox;

import i18n.I18N;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import api.mig.swing.MigLayout;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.db.abs.AbstractEntity;
import storybook.db.idea.Idea;
import storybook.tools.swing.js.JSLabel;
import storybook.ui.MIG;

/**
 *
 * @author favdb
 */
public class IdeaConfirmDelete extends JDialog {

	public static boolean show(JFrame mainFrame, Idea idea) {
		IdeaConfirmDelete dlg = new IdeaConfirmDelete(mainFrame, idea);
		dlg.setVisible(true);
		return dlg.canceled;
	}

	private JTextPane tpEntity;
	private boolean canceled = false;
	private final AbstractEntity idea;

	public IdeaConfirmDelete(JFrame parent, AbstractEntity entity) {
		super(parent);
		canceled = true;
		idea = entity;
		initAll();
	}

	private void initAll() {
		init();
		initUi();
	}

	public void init() {
		// empty
	}

	public void initUi() {
		this.setModal(true);
		setLayout(new MigLayout(MIG.WRAP));
		JSLabel lb = new JSLabel();
		setTitle(I18N.getMsg("ask.delete"));
		lb.setText(I18N.getMsg("ask.delete"));
		JScrollPane scroller = new JScrollPane();
		tpEntity = new JTextPane();
		tpEntity.setEditable(false);
		tpEntity.setContentType("text/html");
		tpEntity.setText(idea.toDetail(0));
		tpEntity.setCaretPosition(0);
		scroller.setViewportView(tpEntity);
		scroller.setPreferredSize(new Dimension(480, 320));
		add(scroller, MIG.WRAP);
		add(getCancelButton(), MIG.get(MIG.SPLIT2, MIG.RIGHT));
		add(getOkButton(), MIG.RIGHT);
		pack();
		setLocationRelativeTo(this.getParent());
	}

	private JButton getOkButton(String... txt) {
		JButton bt = new JButton();
		bt.setFont((App.getInstance().fonts.defGet()));
		bt.setText(I18N.getMsg("ok"));
		bt.setIcon(IconUtil.getIconSmall(ICONS.K.OK));
		bt.addActionListener(e -> {
			canceled = false;
			dispose();
		});
		return bt;
	}

	private JButton getCancelButton() {
		JButton bt = new JButton();
		bt.setFont((App.getInstance().fonts.defGet()));
		bt.setText(I18N.getMsg("cancel"));
		bt.setIcon(IconUtil.getIconSmall(ICONS.K.CANCEL));
		bt.addActionListener(e -> {
			canceled = true;
			dispose();
		});
		return bt;
	}

}
