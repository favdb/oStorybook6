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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.ui.frames.main.MainFrame;

/**
 *
 * @author favdb
 */
public class ConfirmDeleteDlg extends AbsDialog {

	public static boolean show(MainFrame mainFrame, AbstractEntity current, Object object) {
		ConfirmDeleteDlg dlg = new ConfirmDeleteDlg(mainFrame, current);
		dlg.setVisible(true);
		return (dlg.canceled);
	}

	public static boolean show(MainFrame mainFrame, List<AbstractEntity> current, Object object) {
		ConfirmDeleteDlg dlg = new ConfirmDeleteDlg(mainFrame, current);
		dlg.setVisible(true);
		return (dlg.canceled);
	}

	private JTextPane tpEntity;
	private final List<AbstractEntity> entities;

	public ConfirmDeleteDlg(MainFrame parent, AbstractEntity entity) {
		super(parent);
		canceled = true;
		mainFrame = parent;
		this.entities = new ArrayList<>();
		entities.add(entity);
		initAll();
	}

	public ConfirmDeleteDlg(MainFrame parent, List<AbstractEntity> entities) {
		super(parent);
		canceled = true;
		mainFrame = parent;
		this.entities = entities;
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout("", "", ""));
		JLabel lb = new JLabel(I18N.getMsg("ask.delete.multi"));
		if (entities.size() < 2) {
			setTitle(I18N.getMsg("ask.delete"));
			lb.setText(I18N.getMsg("ask.delete"));
		} else {
			setTitle(I18N.getMsg("ask.delete.multi"));
		}
		JScrollPane scroller = new JScrollPane();
		tpEntity = new JTextPane();
		tpEntity.setEditable(false);
		tpEntity.setContentType("text/html");
		StringBuilder buf = new StringBuilder();
		for (AbstractEntity e : entities) {
			buf.append(EntityUtil.getDeleteInfo(mainFrame, e))
					.append("<hr style='margin:10px'>\n");
		}
		tpEntity.setText(buf.toString());
		tpEntity.setCaretPosition(0);
		scroller.setViewportView(tpEntity);
		scroller.setPreferredSize(new Dimension(480, 320));
		add(scroller, "wrap");
		add(getCancelButton(), "split 2, right");
		add(getOkButton(), "right");
		pack();
		setLocationRelativeTo(mainFrame);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
