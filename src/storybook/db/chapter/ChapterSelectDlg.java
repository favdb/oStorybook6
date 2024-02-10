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
import java.awt.MouseInfo;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import resources.icons.ICONS;
import storybook.edit.Editor;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.panel.typist.TypistPanel;

/**
 * dialog for selecting a Chapter
 *
 * @author favdb
 */
public class ChapterSelectDlg extends JDialog {

	private JList lstChapters;
	private final Chapter chapter;
	private final MainFrame mainFrame;
	private boolean canceled = false;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public ChapterSelectDlg(TypistPanel tp, Chapter c) {
		super(tp.getMainFrame().getFullFrame());
		mainFrame = tp.getMainFrame();
		chapter = c;
		initiatlize();
	}

	@SuppressWarnings("unchecked")
	public void initiatlize() {
		setLayout(new MigLayout());
		setTitle(I18N.getMsg("chapter"));
		setModal(true);
		JScrollPane scroller = new JScrollPane();
		scroller.setPreferredSize(new Dimension(642, 480));
		lstChapters = new JList();
		lstChapters.setModel(new DefaultListModel());
		loadList(-1);
		lstChapters.setSelectedValue(chapter, true);
		scroller.setViewportView(lstChapters);
		add(scroller, MIG.SPLIT2);
		JButton btNew = SwingUtil.createButton("", ICONS.K.NEW_CHAPTER,
		   "chapter.new", evt -> newChapter());
		add(btNew, MIG.get(MIG.TOP, MIG.WRAP));
		JButton btCancel = SwingUtil.createButton("cancel", ICONS.K.CANCEL,
		   "cancel", evt -> {
			   canceled = true;
			   dispose();
		   });
		add(btCancel, MIG.get(MIG.SPLIT2, MIG.RIGHT));
		JButton btOK = SwingUtil.createButton("ok", ICONS.K.OK,
		   "ok", evt -> dispose());
		add(btOK);
		pack();
		setLocation(MouseInfo.getPointerInfo().getLocation());
	}

	@SuppressWarnings("unchecked")
	private void loadList(int first) {
		DefaultListModel listModel = (DefaultListModel) lstChapters.getModel();
		listModel.removeAllElements();
		List<Chapter> chapters = (List) mainFrame.project.chapters;
		for (Chapter c : chapters) {
			listModel.addElement(c);
		}
		if (first != -1) {
			lstChapters.setSelectedIndex(first);
		}
	}

	private void newChapter() {
		JDialog dlg = new JDialog(this, true);
		Editor editor = new Editor(mainFrame, new Chapter(), dlg);
		dlg.setTitle(I18N.getMsg("editor"));
		Dimension pos = SwingUtil.getDlgPosition(new Chapter());
		dlg.add(editor);
		Dimension size = SwingUtil.getDlgSize(new Chapter());
		if (size != null) {
			dlg.setSize(size.height, size.width);
		} else {
			dlg.setLocationRelativeTo(this);
		}
		if (pos != null) {
			dlg.setLocation(pos.height, pos.width);
		} else {
			dlg.setSize(this.getWidth() / 2, 680);
		}
		dlg.setVisible(true);
		SwingUtil.saveDlgPosition(dlg, new Chapter());
		loadList(-1);
		lstChapters.setSelectedValue(chapter, true);
	}

	public Chapter getSelectedChapter() {
		return ((Chapter) lstChapters.getSelectedValue());
	}

	public boolean isCanceled() {
		return (canceled);
	}

}
