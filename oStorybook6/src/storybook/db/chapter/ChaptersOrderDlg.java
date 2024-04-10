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
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import resources.icons.ICONS;
import resources.icons.IconButton;
import storybook.dialog.AbsDialog;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

/**
 * dialog for changing the Chapters order
 *
 * @author favdb
 */
public class ChaptersOrderDlg extends AbsDialog {

	public static void show(MainFrame mainFrame) {
		ChaptersOrderDlg dlg = new ChaptersOrderDlg(mainFrame);
		dlg.setVisible(true);
	}

	private JList lstChapters;

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
		setLayout(new MigLayout("ins 5 5 5 5", "[][]"));
		setTitle(I18N.getMsg("chapters.order"));
		lstChapters = new JList();
		lstChapters.setModel(new DefaultListModel());
		loadList(-1);
		JScrollPane scroller = new JScrollPane();
		scroller.setPreferredSize(new Dimension(310, 320));
		scroller.setViewportView(lstChapters);
		add(scroller);
		JPanel p = new JPanel(new MigLayout());
		p.add(new IconButton("btUp", ICONS.K.AR_UP, "move.up", e -> moveList(-1)), MIG.WRAP);
		p.add(new IconButton("btDown", ICONS.K.AR_DOWN, "move.down", e -> moveList(1)), MIG.WRAP);
		p.add(new IconButton("btRenumber", ICONS.K.LIST_ORDERED, "chapters.renumber", e -> renumber()), MIG.WRAP);
		p.add(new IconButton("btExit", ICONS.K.EXIT, "end", e -> dispose()));
		add(p, MIG.TOP);
		pack();
		setModal(true);
		setLocationRelativeTo(mainFrame);
	}

	@SuppressWarnings("unchecked")
	private void loadList(int first) {
		DefaultListModel listModel = (DefaultListModel) lstChapters.getModel();
		listModel.removeAllElements();
		List<Chapter> chapters = (List) mainFrame.project.chapters.getList();
		for (Chapter chapter : chapters) {
			listModel.addElement(chapter);
		}
		if (first != -1) {
			lstChapters.setSelectedIndex(first);
		}
	}

	private void moveList(int sens) {
		int index = lstChapters.getSelectedIndex();
		if (index < 0) {
			return;
		}
		DefaultListModel listModel = (DefaultListModel) lstChapters.getModel();
		if ((sens == -1) && (index < 1)) {
			return;
		}
		Chapter oChapter = (Chapter) listModel.getElementAt(index + sens);
		Integer oChapterNo = oChapter.getChapterno();
		oChapter.setChapterno(99999);
		mainFrame.getBookController().updateEntity(oChapter);
		Chapter nChapter = (Chapter) listModel.getElementAt(index);
		Integer nChapterNo = nChapter.getChapterno();
		nChapter.setChapterno(oChapterNo);
		mainFrame.getBookController().updateEntity(nChapter);
		oChapter.setChapterno(nChapterNo);
		mainFrame.getBookController().updateEntity(oChapter);
		loadList(index + sens);
		mainFrame.setUpdated();
	}

	private void renumber() {
		int n = 1;
		DefaultListModel listModel = (DefaultListModel) lstChapters.getModel();
		int first = lstChapters.getSelectedIndex();
		for (int i = 0; i < listModel.size(); i++) {
			Chapter chapter = (Chapter) listModel.getElementAt(i);
			chapter.setChapterno(n++);
			mainFrame.getBookController().updateEntity(chapter);
		}
		loadList(first);
		mainFrame.setUpdated();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
