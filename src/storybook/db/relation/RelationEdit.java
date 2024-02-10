/*
 * Copyright (C) 2019 favdb
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
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import storybook.Const;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.person.Person;
import storybook.db.scene.Scene;
import storybook.edit.AbstractEditor;
import storybook.edit.Editor;
import storybook.tools.html.Html;
import storybook.tools.swing.js.JSCheckList;
import storybook.ui.MIG;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class RelationEdit extends AbstractEditor {

	private JComboBox cbSceneStart;
	private JComboBox cbSceneEnd;
	private JSCheckList lsPersons;
	private JSCheckList lsLocations;
	private JSCheckList lsItems;
	private JTextField tfDescription;

	public RelationEdit(Editor m, AbstractEntity e) {
		super(m, e, "010");
		initAll();
	}

	@Override
	public void initUpper() {
		Relation relation = (Relation) entity;
		JPanel px = new JPanel(new MigLayout(MIG.INS1));
		px.add(new JLabel(I18N.getColonMsg("scene.start")));
		Scene d = relation.getStartScene();
		cbSceneStart = Ui.getCB(px, this, Book.TYPE.SCENE, d, null, BEMPTY);
		px.add(new JLabel(I18N.getColonMsg("scene.end")));
		Scene f = relation.getEndScene();
		cbSceneEnd = Ui.getCB(px, this, Book.TYPE.SCENE, f, null, BEMPTY);
		pUpper.add(px, MIG.SPAN);

		pUpper.add(new JLabel(I18N.getColonMsg("description")));
		tfDescription = new JTextField(Html.htmlToText(relation.getDescription()));
		pUpper.add(tfDescription, MIG.GROWX);

		JPanel links = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.FILLX)));
		lsPersons = Ui.initCkList(links, mainFrame, Book.TYPE.PERSON,
		   relation.getPersons(), null, BBORDER);
		lsLocations = Ui.initCkList(links, mainFrame, Book.TYPE.LOCATION,
		   relation.getLocations(), null, BBORDER);
		lsItems = Ui.initCkList(links, mainFrame, Book.TYPE.ITEM,
		   relation.getItems(), null, BBORDER);
		pUpper.add(links, MIG.get(MIG.NEWLINE, MIG.SPAN, MIG.GROW));
	}

	@Override
	public boolean verifier() {
		resetError();
		if (cbSceneStart.getSelectedItem() != null
		   && cbSceneEnd.getSelectedItem() == null) {
			errorMsg(cbSceneEnd, Const.ERROR_MISSING);
		}
		if (lsPersons.getSelectedEntities().isEmpty()
		   && lsLocations.getSelectedEntities().isEmpty()
		   && lsItems.getSelectedEntities().isEmpty()) {
			errorMsg(lsPersons, "error.must_select");
		}
		return (msgError.isEmpty());
	}

	@Override
	public void apply() {
		Relation relation = (Relation) entity;
		if (cbSceneStart.getSelectedIndex() > 0) {
			relation.setStartScene((Scene) cbSceneStart.getSelectedItem());
		} else {
			relation.setStartScene(null);
		}
		if (cbSceneEnd.getSelectedIndex() > 0) {
			relation.setEndScene((Scene) cbSceneEnd.getSelectedItem());
		} else {
			relation.setEndScene(null);
		}
		List<AbstractEntity> ls;
		{
			ls = lsPersons.getSelectedEntities();
			List<Person> lsc = new ArrayList<>();
			for (AbstractEntity e : ls) {
				lsc.add((Person) e);
			}
			relation.setPersons(lsc);
		}
		{
			ls = lsLocations.getSelectedEntities();
			List<Location> lsc = new ArrayList<>();
			for (AbstractEntity e : ls) {
				lsc.add((Location) e);
			}
			relation.setLocations(lsc);
		}
		{
			ls = lsItems.getSelectedEntities();
			List<Item> lsc = new ArrayList<>();
			for (AbstractEntity e : ls) {
				lsc.add((Item) e);
			}
			relation.setItems(lsc);
		}
		entity.setDescription(tfDescription.getText());
		super.apply();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
