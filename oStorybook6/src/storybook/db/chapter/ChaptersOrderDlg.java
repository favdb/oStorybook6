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
package storybook.db.chapter;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import resources.icons.ICONS;
import resources.icons.IconButton;
import storybook.db.book.Book;
import storybook.db.part.Part;
import storybook.dialog.AbsDialog;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;
import static storybook.ui.panel.AbstractPanel.*;

/**
 * dialog for changing the Chapters order
 *
 * @author favdb
 */
public class ChaptersOrderDlg extends AbsDialog {

	private static final String TT = "ChaptesOrderDlg.";

	public static void show(MainFrame mainFrame) {
		ChaptersOrderDlg dlg = new ChaptersOrderDlg(mainFrame);
		dlg.setVisible(true);
	}

	private JList lstChapters;
	private Part curPart = null;
	private JComboBox cbParts;
	private IconButton btUp, btDown;
	private boolean modified;

	public ChaptersOrderDlg(MainFrame m) {
		super(m);
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		setLayout(new MigLayout(/*"ins 5 5 5 5"*/"", "[][]"));
		setTitle(I18N.getMsg("chapters.order"));
		lstChapters = new JList();
		lstChapters.setModel(new DefaultListModel());
		if (!mainFrame.project.parts.getList().isEmpty()) {
			initParts();
			cbParts.setSelectedIndex(0);
		}
		loadList(-1);
		JScrollPane scroller = new JScrollPane();
		scroller.setPreferredSize(new Dimension(310, 320));
		scroller.setViewportView(lstChapters);
		add(scroller);
		JPanel p = new JPanel(new MigLayout(MIG.WRAP, "[]"));
		p.add(btUp = new IconButton("btUp", ICONS.K.AR_UP, "move.up", e -> moveList(-1)));
		p.add(btDown = new IconButton("btDown", ICONS.K.AR_DOWN, "move.down", e -> moveList(1)));
		p.add(new IconButton("btRenumber", ICONS.K.LIST_ORDERED, "chapters.renumber", e -> renumber()));
		p.add(Ui.initButton("btOk", "ok", ICONS.K.OK, "", e -> {
			if (modified) {
				apply();
			}
			dispose();
		}));
		p.add(Ui.initButton("btCancel", "cancel", ICONS.K.CANCEL, "", e -> dispose()));
		add(p, MIG.TOP);
		pack();
		setModal(true);
		setLocationRelativeTo(mainFrame);
		btUp.setEnabled(false);
		btDown.setEnabled(false);
	}

	private void initParts() {
		//LOG.trace(TT + "initParts()");
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.WRAP), "[][]"));
		curPart = (Part) book.project.parts.getFirst();
		cbParts = Ui.initComboBox("cbParts", "", (List) mainFrame.project.getList(Book.TYPE.PART),
				curPart, !EMPTY, !ALL, e -> changePart());
		p.add(new JLabel(I18N.getColonMsg("part")));
		p.add(cbParts);
		add(p, MIG.SPAN);
	}

	public void changePart() {
		curPart = (Part) cbParts.getSelectedItem();
		loadList(-1);
	}

	@SuppressWarnings("unchecked")
	private void loadList(int first) {
		if (modified) {
			apply();
		}
		DefaultListModel listModel = (DefaultListModel) lstChapters.getModel();
		listModel.removeAllElements();
		List<Chapter> chapters;
		if (cbParts != null) {
			chapters = (List) mainFrame.project.chapters.find(curPart);
		} else {
			chapters = (List) mainFrame.project.chapters.getList();
		}
		for (Chapter chapter : chapters) {
			listModel.addElement(chapter);
		}
		if (first != -1) {
			lstChapters.setSelectedIndex(first);
		}
		lstChapters.addListSelectionListener(e -> changeChapter());
		modified = false;
	}

	private void changeChapter() {
		btUp.setEnabled(lstChapters.getSelectedIndex() > 0);
		btDown.setEnabled(lstChapters.getSelectedIndex() < lstChapters.getModel().getSize() - 1);
	}

	/**
	 * apply the order changes
	 */
	private void apply() {
		if (!modified) {
			return;
		}
		DefaultListModel listModel = (DefaultListModel) lstChapters.getModel();
		for (int i = 0; i < listModel.size(); i++) {
			Chapter chapter = (Chapter) listModel.getElementAt(i);
			chapter.setChapterno(i + 1);
		}
		mainFrame.setUpdated();
		mainFrame.refresh();
		modified = false;
	}

	@SuppressWarnings("unchecked")
	private void moveList(int sens) {
		int index = lstChapters.getSelectedIndex();
		if (index < 0) {
			return;
		}
		DefaultListModel listModel = (DefaultListModel) lstChapters.getModel();
		int newIndex = index + sens;
		if (newIndex < 0 || newIndex >= listModel.size()) {
			return;
		}
		Object chapter = listModel.remove(index);
		listModel.add(newIndex, chapter);
		lstChapters.setSelectedIndex(newIndex);
		lstChapters.ensureIndexIsVisible(newIndex);
		modified = true;
	}

	private void renumber() {
		int n = 1;
		DefaultListModel listModel = (DefaultListModel) lstChapters.getModel();
		int first = lstChapters.getSelectedIndex();
		for (int i = 0; i < listModel.size(); i++) {
			Chapter chapter = (Chapter) listModel.getElementAt(i);
			chapter.setChapterno(n++);
		}
		loadList(first);
		mainFrame.refresh();
		mainFrame.setUpdated();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
