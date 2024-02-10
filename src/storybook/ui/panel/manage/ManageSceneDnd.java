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
package storybook.ui.panel.manage;

import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import storybook.db.scene.Scene;

@SuppressWarnings("serial")
public class ManageSceneDnd extends ManageScene implements MouseListener, MouseMotionListener, DropTargetListener {

	private static final String TT = "DTScenePanel";

	private MouseEvent firstMouseEvent = null;
	private final DropTarget dt;

	public ManageSceneDnd(ManageChapter manageChapter, Scene scene) {
		this(manageChapter, scene, TYPE.SCENE);
	}

	public ManageSceneDnd(ManageChapter manageChapter, Scene scene, TYPE type) {
		super(manageChapter, scene, type);
		addMouseMotionListener(this);
		setAutoscrolls(true);
		dt = new DropTarget(this, this);
		addMouseListener(this);
	}

	@Override
	public void initUi() {
		super.initUi();
		if (scene != null) {
			manage.sceneAdd(this);
		}
	}

	@Override
	public void mouseClicked(MouseEvent evt) {
		if (scene == null) {
			return;
		}
		manage.sceneSelect(this);
		if (evt.getClickCount() == 2) {
			mainFrame.showEditorAsDialog(scene);
			/*EditEntityAction act = new EditEntityAction(mainFrame, scene, false);
			act.actionPerformed(null);*/
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// empty
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (getScene() == null) {
			firstMouseEvent = null;
			return;
		}
		firstMouseEvent = e;
		manage.sceneSelect(this);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		firstMouseEvent = null;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (getScene() == null) {
			return;
		}

		if (firstMouseEvent != null) {
			e.consume();
			int action = TransferHandler.MOVE;
			/*int dx = Math.abs(e.getX() - firstMouseEvent.getX());
			int dy = Math.abs(e.getY() - firstMouseEvent.getY());
			if (dx > 5 || dy > 5) {*/
			JComponent comp = (JComponent) e.getSource();
			TransferHandler handler = comp.getTransferHandler();
			handler.exportAsDrag(comp, firstMouseEvent, action);
			firstMouseEvent = null;
			//}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// empty
	}

	@Override
	public void dragEnter(DropTargetDragEvent evt) {
		//LOG.trace(TT + ".dragEnter(evt=" + evt.toString() + ")");
		manage.sceneSetTarget(this);
	}

	@Override
	public void dragOver(DropTargetDragEvent evt) {
		//LOG.trace(TT + ".dragOver(evt=" + evt.toString() + ")");
		manage.sceneSetTarget(this);
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent evt) {
		//LOG.trace(TT + ".dropActionChanged(evt=" + evt.toString() + ")");
	}

	@Override
	public void dragExit(DropTargetEvent evt) {
		//LOG.trace(TT + ".dragExit(evt=" + evt.toString() + ")");
		this.setBackground(bkcolor);
		manage.sceneResetTarget(this);
	}

	@Override
	public void drop(DropTargetDropEvent evt) {
		//LOG.trace(TT + ".drop(evt=" + evt.toString() + ")");
		this.getTransferHandler().importData(this, evt.getTransferable());
	}

}
