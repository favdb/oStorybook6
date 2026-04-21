/*
 * Copyright (C) 2025 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.dialog;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import resources.icons.IconUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.renderer.EntityLCR;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;

/**
 * dialog to select one or more entities
 *
 * @author favdb
 */
public class EntitySelectDlg extends AbsDialog implements ListSelectionListener {

	private static final String TT = "EntitySelectDialog.";
	private final Book.TYPE type;
	private JList list;
	private DefaultListModel model;
	private JButton btOk;
	private List<AbstractEntity> entities;

	public EntitySelectDlg(MainFrame mainFrame, Book.TYPE type, List<AbstractEntity> entities) {
		super(mainFrame);
		this.type = type;
		this.entities = entities;
		initAll();
	}

	@Override
	public void init() {
		//empty
	}

	@Override
	public void initUi() {
		super.initUi();
		setLayout(new MigLayout(MIG.get(MIG.WRAP), "", "[grow][]"));
		setTitle(I18N.getMsg(type.toString()));
		setIconImage(IconUtil.getIconImage("icon"));
		setPreferredSize(new Dimension(800, 600));
		add(initEntities(), MIG.GROW);
		btOk = getOkButton();
		add(btOk, MIG.get(MIG.RIGHT, MIG.SPLIT2));
		btOk.setEnabled(false);
		add(getCancelButton());
		this.pack();
		this.setLocationRelativeTo(mainFrame);
	}

	/**
	 * get the list of selected entities
	 *
	 * @return
	 */
	public List<AbstractEntity> getList() {
		List<AbstractEntity> ls = new ArrayList<>();
		for (Object obj : list.getSelectedValuesList()) {
			ls.add((AbstractEntity) obj);
		}
		return ls;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (btOk != null) {
			btOk.setEnabled(true);
		}
	}

	@SuppressWarnings("unchecked")
	private JScrollPane initEntities() {
		JPanel p = new JPanel(new MigLayout());
		model = new DefaultListModel();
		for (Object obj : mainFrame.project.getList(type)) {
			if (entities != null && !entities.contains(obj)) {
				model.addElement(obj);
			} else {
				model.addElement(obj);
			}
		}
		list = new JList(model);
		//list.setBorder(BorderFactory.createTitledBorder(type.toString() + ":"));
		list.setName("ls_" + type.toString());
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setMaximumSize(SwingUtil.getScreenSize());
		list.addListSelectionListener(this);
		list.setCellRenderer(new EntityLCR());
		JScrollPane scroll = new JScrollPane(list);
		SwingUtil.setMaxPreferredSize(scroll);
		return scroll;
	}

}
