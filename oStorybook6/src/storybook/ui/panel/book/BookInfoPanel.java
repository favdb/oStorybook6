/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.ui.panel.book;

import api.mig.swing.MigLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.JLabel;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl.PROPS;
import storybook.db.book.Book;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractPanel;
import storybook.ui.panel.EntityLinksPanel;

/**
 * class for the information panel in the Book view
 *
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class BookInfoPanel extends AbstractPanel {

	private Scene scene;
	private JLabel lbStrand;
	private EntityLinksPanel personLinksPanel, itemLinksPanel, locationLinksPanel, strandLinksPanel;
	private StrandDateLabel lbDate;

	public BookInfoPanel(MainFrame mainFrame, Scene scene) {
		super(mainFrame);
		this.scene = scene;
		initAll();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		Object newValue = evt.getNewValue();
		ActKey act = new ActKey(evt);
		switch (Book.getTYPE(act.type)) {
			case STRAND:
				if (PROPS.UPDATE.check(act.getCmd())) {
					Strand newStrand = (Strand) newValue;
					if (scene.getStrand() != null && newStrand.getId().equals(scene.getStrand().getId())) {
						lbStrand.setText(newStrand.toString());
						lbStrand.setBackground(newStrand.getJColor());
					}
					strandLinksPanel.refresh();
				}
				break;
			case SCENE:
				if (PROPS.UPDATE.check(act.getCmd())) {
					Scene newScene = (Scene) newValue;
					if (!newScene.getId().equals(scene.getId())) {
						return;
					}
					if (newScene.getScenets() != null) {
						lbDate.setDate(newScene.getScenets());
					}
					lbDate.refresh();
					if (newScene.getStrand() != null) {
						lbStrand.setText(newScene.getStrand().toString());
						lbStrand.setBackground(Strand.getJColor(newScene.getStrand()));
					} else {
						Strand strand = (Strand) mainFrame.project.getList(Book.TYPE.STRAND).get(0);
						lbStrand.setText(strand.toString());
						lbStrand.setBackground(strand.getJColor());
					}
					personLinksPanel.refresh();
					itemLinksPanel.refresh();
					locationLinksPanel.refresh();
					strandLinksPanel.refresh();
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.FILLX, MIG.WRAP, MIG.HIDEMODE3, MIG.INS1, MIG.GAP1)));
		setOpaque(false);
		setBorder(SwingUtil.getBorderDefault());
		// strand
		if (scene.getStrand() == null
				&& mainFrame.project != null
				&& !mainFrame.project.strands.getList().isEmpty()) {
			scene.setStrand((Strand) mainFrame.project.strands.getList().get(0));
		}
		lbStrand = new JLabel(scene.getStrand().toString(), JLabel.CENTER);
		lbStrand.setBackground(scene.getStrand().getJColor());
		add(lbStrand, MIG.GROWX);
		// date
		lbDate = new StrandDateLabel(scene.getStrand(), scene.getScenets(), false);
		lbDate.setOpaque(false);
		add(lbDate, MIG.CENTER);
		// person links
		personLinksPanel = new EntityLinksPanel(mainFrame, scene, Book.TYPE.PERSON);
		if (!scene.getPersons().isEmpty()) {
			add(personLinksPanel, MIG.GROWX);
		}
		// location links
		locationLinksPanel = new EntityLinksPanel(mainFrame, scene, Book.TYPE.LOCATION);
		if (!scene.getLocations().isEmpty()) {
			add(locationLinksPanel, MIG.GROWX);
		}
		// item links
		itemLinksPanel = new EntityLinksPanel(mainFrame, scene, Book.TYPE.ITEM);
		if (!scene.getItems().isEmpty()) {
			add(itemLinksPanel, MIG.GROWX);
		}
		// strand links
		strandLinksPanel = new EntityLinksPanel(mainFrame, scene, Book.TYPE.STRAND);
		if (!scene.getStrands().isEmpty()) {
			add(strandLinksPanel, MIG.GROWX);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
