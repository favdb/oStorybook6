/*
 * Copyright (C) 2015 favdb
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
package storybook.ui.panels.memo;

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import api.shef.ShefEditor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.ctrl.Ctrl;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.memo.Memo;
import storybook.tools.net.Net;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSToolBar;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.Ui;
import storybook.ui.panels.AbstractPanel;

/**
 *
 * @author favdb
 */
public class MemosPanel extends AbstractPanel
		implements ActionListener, FocusListener,
		ListSelectionListener, HyperlinkListener,
		MouseListener, ItemListener {

	private static final String TT = "MemosPanel.";
	private ShefEditor shef;
	private JList lsMemo = null;//liste des memos
	private JButton btNew;// bouton nouveau
	private JButton btDelete;// bouton supprimer
	private JButton btEdit;// bouton modifier
	private JScrollPane lsScroller;
	private Memo currentMemo;
	private int hashMemo;

	private enum ACT {
		BT_NEW, BT_DELETE, BT_EDIT, CK_DISPOSITION, CB_MEMO, LS_MEMO, NONE;

		public String n() {
			return name().toLowerCase();
		}
	}

	private ACT getACT(String s) {
		for (ACT a : ACT.values()) {
			if (a.n().equals(s)) {
				return a;
			}
		}
		return ACT.NONE;
	}

	public MemosPanel(MainFrame mainFrame) {
		super(mainFrame);
	}

	@Override
	public void init() {
		//LOG.trace(TT+"init()");
		this.withPart = false;
		this.resetListener();
	}

	@Override
	public void initUi() {
		//LOG.trace(TT+"initUi()");
		setLayout(new MigLayout(MIG.get(MIG.WRAP, MIG.FILL, MIG.HIDEMODE3), "[20%][80%]", "[][grow][]"));
		lsMemo = new JList<>();
		lsMemo.setName(ACT.LS_MEMO.n());
		lsMemo.addFocusListener(this);
		lsScroller = new JScrollPane(lsMemo);
		SwingUtil.setMaxPreferredSize(lsScroller);
		int ln = IconUtil.getDefSize();
		lsScroller.setMinimumSize(new Dimension(ln * 4, ln));
		add(lsScroller, MIG.GROWX);
		//memo as a Shef
		shef = new ShefEditor(mainFrame.project.getPath(), "disallow", "");
		shef.wysEditorGet().showHideTB();
		shef.wysEditorGet().getWysEditor().addFocusListener(this);
		add(shef, MIG.get(MIG.SPAN, MIG.GROW));

		add(initFooter(), MIG.get(MIG.GROWX, MIG.SPAN));
		refreshListMemo();
		setListener();
	}

	private JToolBar initFooter() {
		JSToolBar footing = new JSToolBar(true);
		btNew = Ui.initButton(ACT.BT_NEW.n(), "", ICONS.K.NEW, "new", this);
		btNew.setEnabled(false);
		footing.add(btNew);
		btEdit = Ui.initButton(ACT.BT_EDIT.n(), "", ICONS.K.EDIT, "edit", this);
		btEdit.setEnabled(false);
		footing.add(btEdit);
		btDelete = Ui.initButton(ACT.BT_DELETE.n(), "", ICONS.K.DELETE, "delete", this);
		btDelete.setEnabled(false);
		footing.add(btDelete);
		return footing;
	}

	@Override
	public void focusGained(FocusEvent e) {
		//LOG.trace(TT+"focusGained(e)");
		btNew.setEnabled(true);
	}

	@Override
	public void focusLost(FocusEvent e) {
		//LOG.trace(TT+"focusLost(e)");
		checkTextChanged();
		btNew.setEnabled(false);
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//LOG.trace(TT + "modelPropertyChange(evt=" + evt.toString() + ")");
		String propName = evt.getPropertyName();
		if ("SHOWINFO".equalsIgnoreCase(propName)) {
			return;
		}
		Object newValue = evt.getNewValue();
		switch (Ctrl.getPROPS(propName)) {
			case REFRESH: {
				View newView = (View) newValue;
				View view = (View) getParent().getParent();
				if (view == newView) {
					refresh();
				}
				return;
			}
			case MEMO_SHOW: {
				if (newValue instanceof AbstractEntity) {
					Memo memo = (Memo) newValue;
					if (memo.isTransient()) {
						return;
					}
					setMemo(memo);
				}
				break;
			}
			case SHOWINFO:
				if (newValue instanceof Memo) {
					setMemo((Memo) newValue);
				}
			default:
				break;
		}
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent evt) {
		if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			Net.openUrl(evt);
		}
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		//LOG.trace(TT+"actionPerformed(evt=" + evt.toString() + ")");
		if (evt.getSource() instanceof JButton) {
			Memo memo = currentMemo;
			switch (getACT(((JButton) evt.getSource()).getName())) {
				case BT_EDIT:
					resetListener();
					if (!mainFrame.showEditorAsDialog(memo)) {
						refresh();
					}
					break;
				case BT_DELETE:
					resetListener();
					EntityUtil.delete(mainFrame, memo);
					break;
				case BT_NEW:
					resetListener();
					mainFrame.showEditorAsDialog(new Memo());
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		//LOG.trace(TT+"valueChanged(e=" + e.toString() + ")");
		checkTextChanged();
		setMemo((Memo) lsMemo.getSelectedValue());
	}

	private void setListener() {
		lsMemo.addMouseListener(this);
		lsMemo.addListSelectionListener(this);
	}

	private void resetListener() {
		if (lsMemo != null) {
			lsMemo.removeListSelectionListener(this);
			lsMemo.removeMouseListener(this);
		}
	}

	@SuppressWarnings({"unchecked", "unchecked", "unchecked", "unchecked"})
	private void refreshListMemo() {
		//LOG.trace(TT+"refreshListMemo()");
		//memorize selected Memo
		Memo entitySelected;
		entitySelected = (Memo) lsMemo.getSelectedValue();
		List<Memo> memos = (List) mainFrame.project.getList(Book.TYPE.MEMO);
		//reload list
		DefaultListModel model = new DefaultListModel();
		model.removeAllElements();
		for (Memo memo : memos) {
			model.addElement(memo);
		}
		lsMemo.setModel(model);
		lsMemo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.revalidate();
		//select prior selected Memo
		if (entitySelected != null) {
			setMemo(entitySelected);
		} else {
			if (currentMemo != null) {
				setMemo(currentMemo);
			}
		}
	}

	private void setMemo(Memo memo) {
		//LOG.trace(TT+"setMemo(memo=" + LOG.printInfos(memo) + ")");
		if (memo == null) {
			shef.setText("");
			return;
		}
		lsMemo.removeListSelectionListener(this);
		lsMemo.setSelectedValue(memo, true);
		lsMemo.addListSelectionListener(this);
		currentMemo = memo;
		shef.setText(currentMemo.getNotes());
		hashMemo = shef.wysEditorGet().getText().hashCode();
	}

	public JList getMemoList() {
		return lsMemo;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		//LOG.trace(TT+"mouseClicked(e)");
		if (e.getClickCount() == 2) {
			Memo memo = (Memo) lsMemo.getSelectedValue();
			mainFrame.showEditorAsDialog(memo);
			refreshListMemo();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//empty
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		//empty
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		//empty
	}

	@Override
	public void mouseExited(MouseEvent e) {
		//empty
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		//LOG.trace(TT+"itemStateChange(e=" + e.toString() + ")");
	}

	private void checkTextChanged() {
		//LOG.trace(TT+"checkTextChange()");
		if (currentMemo == null) {
			return;
		}
		if (hashMemo != shef.wysEditorGet().getText().hashCode()) {
			currentMemo.setNotes(shef.wysEditorGet().getText());
			mainFrame.getBookController().updateEntity(currentMemo);
			mainFrame.setUpdated();
			hashMemo = shef.wysEditorGet().getText().hashCode();
		}
	}
}
