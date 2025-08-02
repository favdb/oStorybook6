/*
Storybook: Scene-based software for novelists and authors.
Copyright (C) 2008 - 2011 Martin Mustun

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
package storybook.ui.panel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import storybook.db.EntityUtil;
import storybook.db.scene.Scene;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MainFrame;

/**
 * abstract for a scene panel with new, edit and delete button
 *
 * @author favdb
 */
abstract public class AbstractScenePanel extends AbstractGradientPanel {

	protected Scene scene;
	protected AbstractAction newAction;
	public JButton btNew, btEdit, btDelete;

	public AbstractScenePanel(MainFrame mainFrame, Scene scene) {
		super(mainFrame);
		this.scene = scene;
	}

	public AbstractScenePanel(MainFrame mainFrame, Scene scene,
			boolean showBgGradient, Color begBgCol, Color endBgCol) {
		super(mainFrame, showBgGradient,
				(scene.getInformative() ? Color.white : begBgCol),
				(scene.getInformative() ? Color.white : endBgCol));
		this.scene = scene;
	}

	public Scene getScene() {
		return scene;
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	protected AbstractAction getNewAction() {
		if (newAction == null) {
			newAction = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Scene newScene = new Scene();
					newScene.setStrand(scene.getStrand());
					newScene.setScenets(scene.getScenets());
					if (scene.hasChapter()) {
						newScene.setChapter(scene.getChapter());
					}
					mainFrame.showEditorAsDialog(newScene);
				}
			};
		}
		return newAction;
	}

	public JButton getEditButton() {
		if (btEdit == null) {
			btEdit = SwingUtil.getIconButton("edit", e -> EntityUtil.editEntity(mainFrame, scene));
		}
		return btEdit;
	}

	public JButton getDeleteButton() {
		if (btDelete == null) {
			btDelete = SwingUtil.getIconButton("delete", e -> EntityUtil.delete(mainFrame, scene));
		}
		return btDelete;
	}

	public JButton getNewButton() {
		if (btNew == null) {
			btNew = SwingUtil.getIconButton("new", getNewAction());
		}
		return btNew;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof AbstractScenePanel)) {
			return false;
		}
		AbstractScenePanel asp = (AbstractScenePanel) other;
		if (asp.getScene() == null || scene == null) {
			return false;
		}
		return asp.getScene().getId().equals(scene.getId());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + Objects.hashCode(this.scene);
		return hash;
	}
}
