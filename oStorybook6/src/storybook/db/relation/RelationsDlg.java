/*
 * Copyright (C) 2020 favdb
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
package storybook.db.relation;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import resources.icons.ICONS;
import storybook.db.book.Book;
import storybook.edit.Editor;
import storybook.dialog.AbsDialog;
import storybook.tools.html.Html;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class RelationsDlg extends AbsDialog implements ListSelectionListener {

	private static final String BT_EXIT = "btExit";

	private final Editor editor;
	private JPanel listPanel, right;
	private JList listbox;
	public boolean modified = false;
	private JTextPane infoPane;
	private JScrollPane scrollInfo;

	public RelationsDlg(Editor editor) {
		super(editor.mainFrame);
		this.editor = editor;
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.WRAP, MIG.FILL, MIG.HIDEMODE3), "[20%][80%]", "[][grow]"));
		this.setTitle(I18N.getMsg("relations"));
		//liste des relations
		initList();
		//affichage/modification des informations
		right = new JPanel(new MigLayout(MIG.FILLX));
		right.setBorder(BorderFactory.createRaisedBevelBorder());
		initInfo(right);
		add(right, MIG.get(MIG.SPAN));
		// pour terminer
		add(Ui.initButton(BT_EXIT, "end", ICONS.K.EXIT, "", e -> dispose()),
		   MIG.get(MIG.SPAN, MIG.RIGHT));
		setPreferredSize(new Dimension(800, 600));
		pack();
		this.setLocationRelativeTo(editor);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//LOG.trace(TT+".actionPerformed(e=" + e.toString() + ")");
		if (e.getSource() instanceof JButton) {
			JButton bt = (JButton) e.getSource();
			switch (bt.getName()) {
				case "btAdd":
					if (mainFrame.showEditorAsDialog(new Relation(), bt)) {
						reloadList();
						modified = true;
					}
					break;
				case "btDelete":
					Relation r = (Relation) listbox.getSelectedValue();
					if (JOptionPane.showConfirmDialog(this,
					   I18N.getMsg("ask.delete"),
					   I18N.getMsg("delete"),
					   JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
						return;
					}
					mainFrame.getBookController().deleteEntity(r);
					reloadList();
					modified = true;
					break;
				default:
					break;

			}
		}
	}

	private void initList() {
		//LOG.trace(TT+".initList(panel)");
		listPanel = Ui.initListBox((JPanel) this.getContentPane(),
		   mainFrame, Book.TYPE.RELATION, "",
		   mainFrame.project.relations.find(editor.entity), null, e -> relationAdd());
		for (Object c : listPanel.getComponents()) {
			if (c instanceof JButton) {
				JButton bt = (JButton) c;
				bt.addActionListener(this);
			}
			listbox = getList();
			if (listbox != null) {
				listbox.addListSelectionListener(this);
			}
		}
		SwingUtil.setMaxPreferredSize(listPanel);
		add(listPanel, MIG.GROWY);
	}

	private void initInfo(JPanel panel) {
		//LOG.trace(TT+".initInfo(panel)");
		if (listbox == null) {
			return;
		}
		infoPane = new JTextPane();
		infoPane.setEditable(false);
		infoPane.setOpaque(false);
		infoPane.setContentType(Html.TYPE);
		SwingUtil.setMaxPreferredSize(infoPane);
		scrollInfo = new JScrollPane(infoPane);
		scrollInfo.setMinimumSize(new Dimension(320, 240));
		scrollInfo.setMaximumSize(SwingUtil.getScreenSize());
		panel.add(scrollInfo, MIG.GROW);
	}

	private JList getList() {
		//LOG.trace("RelationsDlg.getList()");
		JList ls = null;
		for (Object c : listPanel.getComponents()) {
			if (c instanceof JScrollPane) {
				Component obj = ((JScrollPane) c).getViewport().getView();
				if (obj instanceof JList) {
					ls = (JList) obj;
				}
				break;
			}
		}
		return (ls);
	}

	@SuppressWarnings("unchecked")
	private void reloadList() {
		listbox.removeListSelectionListener(this);
		DefaultListModel model = (DefaultListModel) listbox.getModel();
		model.removeAllElements();
		for (Relation r : mainFrame.project.relations.find(editor.entity)) {
			model.addElement(r);
		}
		listbox.setSelectedIndex(-1);
		listbox.addListSelectionListener(this);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (listbox.getSelectedIndex() == -1) {
			infoPane.setText("");
			return;
		}
		infoPane.setText(((Relation) listbox.getSelectedValue()).toDetail(0));
		infoPane.setCaretPosition(0);
	}

	private void relationAdd() {
		if (mainFrame.showEditorAsDialog(new Relation())) {
			reloadList();
			modified = true;
		}
	}

	public static boolean show(Editor editor) {
		RelationsDlg dlg = new RelationsDlg(editor);
		dlg.setVisible(true);
		return dlg.modified;
	}

}
