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
package storybook.ui.panels.typist;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.scene.Scene;
import storybook.tools.html.Html;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.frames.scriber.ScriberPanel;

/**
 * info panel for a Scene
 *
 * @author favdb
 */
public class TypistInfo extends JPanel {

	private static final String TT = "TypistInfo";

	private Scene scene;
	private boolean modified;
	private JPanel pStrands, pPersons, pLocations, pItems, pPlots;
	private JEditorPane edNotes, edDescription;
	private TypistPanel typist = null;
	private ScriberPanel scriber = null;
	private JScrollPane scrollerNotes, scrollerDescription;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public TypistInfo(TypistPanel m, Scene s) {
		typist = m;
		scene = s;
		init();
		if (scene != null) {
			refreshInfo(false);
		}
		modified = false;
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public TypistInfo(ScriberPanel m, Scene s) {
		scriber = m;
		scene = s;
		init();
		if (scene != null) {
			refreshInfo(false);
		}
		modified = false;
	}

	/**
	 * initalize the links panel
	 *
	 * @param label
	 * @return
	 */
	private JPanel initLinksPanel(String label) {
		JButton btEdit = initButton(label);
		Dimension sx = SwingUtil.getScreenSize();
		btEdit.setPreferredSize(new Dimension((int) (sx.width * 0.20), 16));
		add(btEdit, MIG.GROWX);
		JPanel p = new JPanel();
		p.setLayout(new MigLayout(MIG.INS0));
		add(p, MIG.get(MIG.GROWX));
		return p;
	}

	/**
	 * initalize
	 */
	public void init() {
		setLayout(new MigLayout(MIG.get(MIG.INS1, MIG.WRAP), "[]"));
		Dimension sx = SwingUtil.getScreenSize();
		this.setPreferredSize(new Dimension((int) (sx.width * 0.21), sx.height - (16 * 9)));
		// strands links
		pStrands = initLinksPanel("strands");
		pPersons = initLinksPanel("persons");
		pLocations = initLinksPanel("locations");
		pItems = initLinksPanel("items");
		pPlots = initLinksPanel("plots");
		//description
		JButton btEdit = initButton("description");
		add(btEdit, MIG.GROWX);
		edDescription = new JEditorPane(Html.TYPE, "");
		edDescription.setEditable(false);
		edDescription.setBackground(null);
		scrollerDescription = new JScrollPane(edDescription);
		scrollerDescription.setBorder(BorderFactory.createEmptyBorder());
		//scrollerDescription.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());;
		add(scrollerDescription, MIG.get(MIG.GROW));
		//notes
		btEdit = initButton("notes");
		add(btEdit, MIG.GROWX);
		edNotes = new JEditorPane(Html.TYPE, "");
		edNotes.setEditable(false);
		edNotes.setBackground(null);
		scrollerNotes = new JScrollPane(edNotes);
		scrollerNotes.setBorder(BorderFactory.createEmptyBorder());
		//scrollerNotes.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		add(scrollerNotes, MIG.get(MIG.GROW));
	}

	/**
	 * initalize a JButton with a JLabel
	 *
	 * @param label
	 * @return
	 */
	private JButton initButton(String label) {
		JButton bt = new JButton(IconUtil.getIconSmall(ICONS.K.EDIT));
		bt.setText(I18N.getMsg(label));
		bt.setToolTipText(I18N.getMsg("edit"));
		bt.setMargin(new Insets(0, 0, 0, 0));
		bt.setHorizontalAlignment(SwingConstants.LEADING);
		bt.setHorizontalTextPosition(SwingConstants.LEADING);
		switch (label) {
			case "strands":
				bt.setText(I18N.getMsg("scene.strand.links"));
				bt.addActionListener((ActionEvent evt) -> {
					btStrandAction();
				});
				break;
			case "persons":
				bt.addActionListener((ActionEvent evt) -> {
					btPersonAction();
				});
				break;
			case "locations":
				bt.addActionListener((ActionEvent evt) -> {
					btLocationAction();
				});
				break;
			case "items":
				bt.addActionListener((ActionEvent evt) -> {
					btItemAction();
				});
				break;
			case "plots":
				bt.addActionListener((ActionEvent evt) -> {
					btPlotAction();
				});
				break;
			case "description":
				bt.addActionListener((ActionEvent evt) -> {
					btDescriptionAction();
				});
				break;
			case "notes":
				bt.addActionListener((ActionEvent evt) -> {
					btNotesAction();
				});
				break;
			default:
				break;
		}
		return bt;
	}

	/**
	 * action to edit Strands
	 */
	private void btStrandAction() {
		TypistEntitiesListDlg dlg;
		if (typist != null) {
			dlg = new TypistEntitiesListDlg(typist, scene, "strand");
		} else {
			dlg = new TypistEntitiesListDlg(scriber, scene, "strand");
		}
		dlg.setModal(true);
		dlg.setVisible(true);
		if (!dlg.isCanceled()) {
			scene.setStrands(dlg.getStrands());
			refreshInfo(true);
			modified = true;
		}
	}

	/**
	 * action to edit Persons
	 */
	private void btPersonAction() {
		TypistEntitiesListDlg dlg;
		if (typist != null) {
			dlg = new TypistEntitiesListDlg(typist, scene, "person");
		} else {
			dlg = new TypistEntitiesListDlg(scriber, scene, "person");
		}
		dlg.setModal(true);
		dlg.setVisible(true);
		if (!dlg.isCanceled()) {
			scene.setPersons(dlg.getPersons());
			refreshInfo(true);
			modified = true;
		}
	}

	/**
	 * action to edit Locations
	 */
	private void btLocationAction() {
		TypistEntitiesListDlg dlg;
		if (typist != null) {
			dlg = new TypistEntitiesListDlg(typist, scene, "location");
		} else {
			dlg = new TypistEntitiesListDlg(scriber, scene, "location");
		}
		dlg.setModal(true);
		dlg.setVisible(true);
		if (!dlg.isCanceled()) {
			scene.setLocations(dlg.getLocations());
			refreshInfo(true);
			modified = true;
		}
	}

	/**
	 * action to edit Items
	 */
	private void btItemAction() {
		TypistEntitiesListDlg dlg;
		if (typist != null) {
			dlg = new TypistEntitiesListDlg(typist, scene, "item");
		} else {
			dlg = new TypistEntitiesListDlg(scriber, scene, "item");
		}
		dlg.setModal(true);
		dlg.setVisible(true);
		if (!dlg.isCanceled()) {
			scene.setItems(dlg.getItems());
			refreshInfo(true);
			modified = true;
		}
	}

	/**
	 * action to edit Plots
	 */
	private void btPlotAction() {
		TypistEntitiesListDlg dlg;
		if (typist != null) {
			dlg = new TypistEntitiesListDlg(typist, scene, "plot");
		} else {
			dlg = new TypistEntitiesListDlg(scriber, scene, "plot");
		}
		dlg.setModal(true);
		dlg.setVisible(true);
		if (!dlg.isCanceled()) {
			scene.setPlots(dlg.getPlots());
			refreshInfo(true);
			modified = true;
		}
	}

	/**
	 * action to edit Description
	 */
	private void btDescriptionAction() {
		//LOG.trace(TT + ".btDescriptionAction()");
		TypistEditText dlg;
		if (typist != null) {
			dlg = new TypistEditText(typist, "description", scene.getDescription());
		} else {
			dlg = new TypistEditText(scriber, "description", scene.getDescription());
		}
		dlg.setVisible(true);
		if (!dlg.isCanceled()) {
			scene.setDescription(dlg.getText());
			refreshInfo(true);
			modified = true;
		}
	}

	/**
	 * action to edit Notes
	 */
	private void btNotesAction() {
		//LOG.trace(TT + ".btNotesAction()");
		TypistEditText dlg;
		if (typist != null) {
			dlg = new TypistEditText(typist, "notes", scene.getNotes());
		} else {
			dlg = new TypistEditText(scriber, "notes", scene.getNotes());
		}
		dlg.setVisible(true);
		if (!dlg.isCanceled()) {
			scene.setNotes(dlg.getText());
			refreshInfo(true);
			modified = true;
		}
	}

	@SuppressWarnings("unchecked")
	private void refreshInfoEntities(JPanel p, List links, boolean toUpdate) {
		p.removeAll();
		if (!links.isEmpty()) {
			p.setBorder(javax.swing.BorderFactory.createEtchedBorder());
			for (AbstractEntity e : (List<AbstractEntity>) links) {
				p.add(new JLabel(e.getFullName()), MIG.WRAP);
			}
		} else {
			p.setBorder(null);
		}
	}

	/**
	 * refresh al info with update
	 *
	 * @param toUpdate
	 */
	public void refreshInfo(boolean toUpdate) {
		refreshInfoEntities(pStrands, scene.getStrands(), toUpdate);
		refreshInfoEntities(pPersons, scene.getPersons(), toUpdate);
		refreshInfoEntities(pLocations, scene.getLocations(), toUpdate);
		refreshInfoEntities(pItems, scene.getItems(), toUpdate);
		refreshInfoEntities(pPlots, scene.getPlots(), toUpdate);

		String text = Html.htmlToText(scene.getNotes());
		edNotes.setText(scene.getNotes());
		if (!text.isEmpty()) {
			edNotes.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		} else {
			edNotes.setBorder(null);
		}
		edNotes.setCaretPosition(0);

		text = Html.htmlToText(scene.getDescription());
		edDescription.setText(scene.getDescription());
		if (!text.isEmpty()) {
			edDescription.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		} else {
			edDescription.setBorder(null);
		}
		edDescription.setCaretPosition(0);

		this.revalidate();
		if (toUpdate) {
			if (typist != null) {
				typist.updatedSet();
			} else {
				scriber.updatedSet();
			}
		}
	}

	/**
	 * refresh all info for the given Scene
	 *
	 * @param s
	 */
	public void refreshInfo(Scene s) {
		scene = s;
		modified = false;
		refreshInfo(false);
	}

	/**
	 * get the MainFrame
	 *
	 * @return
	 */
	public MainFrame getMainFrame() {
		return (((TypistPanel) getParent()).getMainFrame());
	}

}
