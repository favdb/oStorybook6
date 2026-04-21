/*
 * Copyright (C) 2022 favdb
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
package storybook.db.endnote;

import api.mig.swing.MigLayout;
import api.shef.ShefEditor;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.scene.Scene;
import storybook.dialog.AbsDialog;
import storybook.tools.swing.FontUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;

/**
 *
 * @author favdb
 */
public class EndnotesListDlg extends AbsDialog implements ListSelectionListener {

	private static final String TT = "ListEndnotesDlg";

	public static void showDlg(MainFrame mainFrame, int type, Scene scene) {
		//LOG.trace(TT + ".showDlg(mainframe, type=" + type + ", scene=" + AbstractEntity.trace(scene));
		EndnotesListDlg dlg = new EndnotesListDlg(mainFrame, type, scene);
		dlg.setVisible(true);
	}

	private JList lsNumber;
	private ShefEditor htEndnote;
	private Endnote current = null;
	private final int endnoteType;
	private final Scene scene;
	private List<Endnote> endnotes;

	public EndnotesListDlg(MainFrame mainFrame, int type, Scene scene) {
		super(mainFrame);
		this.scene = scene;
		this.endnoteType = type;
		initAll();
	}

	@Override
	public void init() {
		//LOG.trace(TT + ".init()");
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		//LOG.trace(TT + ".initUi()");
		super.initUi();
		// layout
		setTitle(I18N.getMsg("endnotes"));
		setIconImage(IconUtil.getIconImageSmall(ICONS.K.CHAR_ENDNOTE));
		setLayout(new MigLayout(MIG.get(MIG.WRAP, MIG.FILL), "[10%][]"));
		// list box for choosing the endnote number
		JScrollPane scroll = new JScrollPane(initLsNumber());
		int sz = FontUtil.getWidth() * 4;
		scroll.setMinimumSize(new Dimension(sz, sz));
		add(scroll, MIG.GROW);

		// Shef editor for the selected endnote
		htEndnote = new ShefEditor(mainFrame.project.getPath(), "none, nothing", "");
		htEndnote.setMinimumSize(new Dimension(800, 600));
		add(htEndnote, MIG.GROW);

		// buttons for save, cancel and quit
		JPanel pb = new JPanel(new MigLayout());
		pb.add(getCloseButton());
		add(pb, MIG.get(MIG.SPAN, MIG.RIGHT));
		pack();
		setLocationRelativeTo(parent);
		setModal(true);
		lsNumber.addListSelectionListener(this);
		lsNumber.setSelectedIndex(0);
	}

	@SuppressWarnings("unchecked")
	private JList initLsNumber() {
		endnotes = mainFrame.project.endnotes.find(endnoteType, scene);
		DefaultListModel model = new DefaultListModel();
		for (Endnote en : endnotes) {
			model.addElement(en.getNumber());
		}
		lsNumber = new JList<>();
		lsNumber.setName("lsEndnote");
		lsNumber.setModel(model);
		return lsNumber;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//LOG.trace(TT + ".actionPerformed(e=" + e.toString() + ")");
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				if (current != null && !current.getNotes().equals(htEndnote.getText())) {
					current.setNotes(htEndnote.getText());
					mainFrame.getBookModel().ENTITY_Update(current);
				}
				canceled = false;
				dispose();
			}

		};
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (current != null && !current.getNotes().equals(htEndnote.getText())) {
			current.setNotes(htEndnote.getText());
			mainFrame.getBookModel().ENTITY_Update(current);
		}
		int n = (Integer) lsNumber.getSelectedValue();
		Endnote en = mainFrame.project.endnotes.find(endnoteType, scene, n);
		if (en != null) {
			current = en;
			htEndnote.setText(en.getNotes());
		}
	}

}
