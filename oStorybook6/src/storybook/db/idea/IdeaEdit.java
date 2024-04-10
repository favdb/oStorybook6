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
package storybook.db.idea;

import i18n.I18N;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import storybook.Const;
import storybook.db.DB;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.edit.AbstractEditor;
import storybook.edit.Editor;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class IdeaEdit extends AbstractEditor {

	private JComboBox cbCategory;
	private JComboBox cbStatus;

	public IdeaEdit(Editor m, AbstractEntity e) {
		super(m, e, "010");
		initAll();

	}

	public IdeaEdit(AbstractEntity e) {
		super(e, "010");
		initAll();

	}

	@Override
	public void initUpper() {
		Idea idea = (Idea) entity;
		if (idea.getId() < 1) {
			//set default name
			idea.setName(I18N.getMsg("idea") + " " + Ideas.getNextNumber(mainFrame));
			tfName.setText(idea.getName());
		}
		JLabel lb = new JLabel(idea.getUuid());
		lb.setEnabled(false);
		pUpper.add(lb, MIG.get(MIG.SPAN, MIG.RIGHT));
		cbCategory = Ui.initAutoCombo(pUpper, DB.DATA.CATEGORY,
		   Idea.findCategories(mainFrame), idea.getCategory(),
		   "010");
		SwingUtil.setCBsize(cbCategory);
		cbStatus = Ui.initStatus(pUpper, idea.getStatus(), BMANDATORY);
	}

	@Override
	public boolean verifier() {
		resetError();
		if (cbStatus.getSelectedIndex() < 0) {
			errorMsg(cbStatus, Const.ERROR_MISSING);
		}
		checkDescNotes(2);
		return (msgError.isEmpty());
	}

	@Override
	public void apply() {
		Idea idea = (Idea) entity;
		idea.setStatus(cbStatus.getSelectedIndex());
		idea.setCategory((String) cbCategory.getSelectedItem());
		super.apply();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	public void toScene() {
		if (hNotes.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null,
			   I18N.getMsg("error.missing.notes"),
			   I18N.getMsg("error"),
			   JOptionPane.ERROR_MESSAGE);
			return;
		}
		String newName = I18N.getColonMsg("idea") + " " + entity.getName();
		Scene scene = (Scene) mainFrame.project.findByName(Book.TYPE.SCENE, newName);
		if (scene != null) {
			JOptionPane.showMessageDialog(null,
			   I18N.getMsg("error.exists"),
			   I18N.getMsg("error"),
			   JOptionPane.ERROR_MESSAGE);
			return;
		}
		@SuppressWarnings("unchecked")
		List<Scene> scenes = (List) mainFrame.project.getList(Book.TYPE.SCENE);
		scene = new Scene();
		scene.setName(newName);
		scene.setTitle(newName);
		scene.setSceneno(Scene.getNextNumber(scenes));
		scene.setNotes(hNotes.getText());
		@SuppressWarnings("unchecked")
		List<Strand> strands = (List) mainFrame.project.getList(Book.TYPE.STRAND);
		if (strands != null && !strands.isEmpty()) {
			scene.setStrand(strands.get(0));
		}
		cbStatus.setSelectedIndex(Idea.STATUS.STARTED.ordinal());
		mainFrame.getBookModel().ENTITY_New(scene);
		mainFrame.setUpdated();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
