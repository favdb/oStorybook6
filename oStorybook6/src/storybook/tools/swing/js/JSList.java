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
package storybook.tools.swing.js;

import api.mig.swing.MigLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import static javax.swing.ListSelectionModel.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import resources.icons.ICONS;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.dialog.EntitySelectDlg;
import storybook.renderer.EntityLCR;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.panels.AbstractPanel;

/**
 * JPanel for showing and updating links (Persons, Items, Locations, Tgas, Plots)
 *
 * @author favdb
 */
public class JSList extends AbstractPanel implements ListSelectionListener {

	private static final String TT = "JSList.";

	private Book.TYPE type;
	private final List<?> entities;
	private JList list;
	private JButton btAdd;
	private JButton btRemove;
	private DefaultListModel model;

	/**
	 * list panel for entities
	 *
	 * @param mainFrame
	 * @param type type of entity
	 * @param entities array list of entities
	 */
	public JSList(MainFrame mainFrame,
			Book.TYPE type, List<?> entities) {
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
	@SuppressWarnings("unchecked")
	public void initUi() {
		this.setLayout(new MigLayout(MIG.FILL, "[][]"));
		model = new DefaultListModel();
		for (Object obj : entities) {
			model.addElement(obj);
		}
		list = new JList(model);
		list.setBorder(BorderFactory.createTitledBorder(type.toString() + ":"));
		list.setName("ls_" + type.toString());
		list.setSelectionMode(SINGLE_SELECTION);
		list.setMaximumSize(SwingUtil.getScreenSize());
		list.addListSelectionListener(this);
		list.setCellRenderer(new EntityLCR());
		JScrollPane scroll = new JScrollPane(list);
		SwingUtil.setMaxPreferredSize(scroll);
		add(scroll, MIG.GROW);
		JPanel pb = new JPanel(new MigLayout(MIG.WRAP, "[]"));
		btAdd = initButton("btAdd", "", ICONS.K.PLUS, "", e -> actionAdd());
		pb.add(btAdd);
		btRemove = initButton("btDelete", "", ICONS.K.MINUS, "", e -> actionRemove());
		pb.add(btRemove);
		add(pb, MIG.GROWY);
	}

	/**
	 * select one or mode entities to add
	 */
	@SuppressWarnings("unchecked")
	private void actionAdd() {
		EntitySelectDlg dlg = new EntitySelectDlg(mainFrame, type, getList());
		dlg.setVisible(true);
		if (!dlg.isCanceled()) {
			for (Object obj : dlg.getList()) {
				model.addElement(obj);
			}
			list.revalidate();
		}
	}

	/**
	 * remove one entity from the list
	 */
	private void actionRemove() {
		int i = list.getSelectedIndex();
		list.remove(i);
	}

	public List<AbstractEntity> getList() {
		List<AbstractEntity> ls = new ArrayList<>();
		for (int i = 0; i < model.getSize(); i++) {
			ls.add((AbstractEntity) list.getModel().getElementAt(i));
		}
		return ls;
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

	/**
	 * list selection listener
	 *
	 * @param e
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (btRemove != null) {
			btRemove.setEnabled(list.getSelectedIndex() != -1 && list.getModel().getSize() > 0);
		}
	}

}
