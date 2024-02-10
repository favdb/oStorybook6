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
package storybook.ui.panel.typist;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.book.BookUtil;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.person.Person;
import storybook.db.plot.Plot;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.decorator.CheckBoxPanel;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;

/**
 * Dialog to select list of Persons, Plots, Items or Locations
 *
 * @author favdb
 */
public class TypistEntitiesListDlg extends JDialog {

	private static final String TT = "EntitiesListDlg.";

	private final Scene scene;
	private final String typeEntity;
	private CheckBoxPanel cbPanel;
	private final MainFrame mainFrame;
	private final TypistPanel typist;
	private boolean canceled = false;

	public TypistEntitiesListDlg(TypistPanel comp, Scene s, String t) {
		super(comp.getMainFrame().getFullFrame());
		this.typist = comp;
		this.mainFrame = comp.getMainFrame();
		scene = s;
		typeEntity = t;
		initialize();
	}

	private void initialize() {
		//LOG.printInfos(TT + ".initialize()");
		if (mainFrame != null) {
			setIconImage(mainFrame.getIconImage());
		}
		// layout
		setTitle(I18N.getMsg(typeEntity));
		setIconImage(IconUtil.getIconImageSmall("ent_" + typeEntity));
		setLayout(new MigLayout(MIG.get(MIG.WRAP, MIG.FILL), "[][][][][]"));
		setPreferredSize(new Dimension(800, 600));
		JPanel p = createEntitiesPanel();
		add(p, MIG.get(MIG.SPAN, MIG.GROW));
		JButton btEdit = Ui.initButton("btEdit", "edit", ICONS.K.EDIT, "");
		btEdit.addActionListener((ActionEvent evt) -> {
			AbstractEntity pointedEntity = cbPanel.getPointedEntity();
			if (pointedEntity != null) {
				mainFrame.showEditorAsDialog(pointedEntity, btEdit);
			}
		});
		add(btEdit);
		JButton btNew = Ui.initButton("btNew", "new", ICONS.K.NEW, "", evt -> newEntity());
		add(btNew);
		JPanel pb = new JPanel(new MigLayout());
		pb.add(Ui.initButton("btCancel", "cancel", ICONS.K.CANCEL, "", e -> {
			canceled = true;
			dispose();
		}));
		pb.add(Ui.initButton("btOk", "ok", ICONS.K.OK, "", e -> dispose()));
		add(pb, MIG.get(MIG.SPAN, MIG.RIGHT));
		pack();
		setLocationRelativeTo(typist);
		setModal(true);
	}

	public boolean isCanceled() {
		return canceled;
	}

	private void newEntity() {
		boolean b = mainFrame.showEditorAsDialog(
		   BookUtil.getNewEntity(mainFrame, Book.getTYPE(typeEntity)));
		if (!b) {
			cbPanel.refresh();
		}
	}

	public List<AbstractEntity> getEntities() {
		List<AbstractEntity> sel = new ArrayList<>();
		List<AbstractEntity> entities = cbPanel.getSelectedEntities();
		for (AbstractEntity entity : entities) {
			sel.add((AbstractEntity) entity);
		}
		return sel;
	}

	public List<Person> getPersons() {
		List<Person> sel = new ArrayList<>();
		List<AbstractEntity> entities = cbPanel.getSelectedEntities();
		for (AbstractEntity entity : entities) {
			sel.add((Person) entity);
		}
		return sel;
	}

	public List<Location> getLocations() {
		List<Location> sel = new ArrayList<>();
		List<AbstractEntity> entities = cbPanel.getSelectedEntities();
		for (AbstractEntity entity : entities) {
			sel.add((Location) entity);
		}
		return sel;
	}

	public List<Item> getItems() {
		List<Item> sel = new ArrayList<>();
		List<AbstractEntity> entities = cbPanel.getSelectedEntities();
		for (AbstractEntity entity : entities) {
			sel.add((Item) entity);
		}
		return sel;
	}

	public List<Plot> getPlots() {
		List<Plot> sel = new ArrayList<>();
		List<AbstractEntity> entities = cbPanel.getSelectedEntities();
		for (AbstractEntity entity : entities) {
			sel.add((Plot) entity);
		}
		return sel;
	}

	public List<Strand> getStrands() {
		List<Strand> sel = new ArrayList<>();
		List<AbstractEntity> entities = cbPanel.getSelectedEntities();
		for (AbstractEntity entity : entities) {
			sel.add((Strand) entity);
		}
		return sel;
	}

	@SuppressWarnings({"unchecked", "unchecked"})
	private JPanel createEntitiesPanel() {
		//LOG.trace(TT + "createEntitiesPanel()");
		MigLayout layout = new MigLayout();
		JPanel panel = new JPanel(layout);
		Book.TYPE t = Book.getTYPE(typeEntity);
		cbPanel = new CheckBoxPanel(mainFrame, t, scene.getStrand());
		JScrollPane scroller = new JScrollPane(cbPanel);
		SwingUtil.setUnitIncrement(scroller);
		SwingUtil.setMaxPreferredSize(scroller);
		panel.add(scroller, MIG.GROW);
		cbPanel.setAutoSelect(false);
		switch (t) {
			case PERSON:
				cbPanel.selectEntities(scene.getPersons());
				break;
			case LOCATION:
				cbPanel.selectEntities(scene.getLocations());
				break;
			case ITEM:
				cbPanel.selectEntities(scene.getItems());
				break;
			case PLOT:
				cbPanel.selectEntities(scene.getPlots());
				break;
			case STRAND:
				cbPanel.selectEntities(scene.getStrands());
				break;
			default:
				break;
		}
		return panel;
	}

}
