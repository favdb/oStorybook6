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
package storybook.ui.panels.manage;

import java.awt.Color;
import java.awt.Component;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import storybook.db.scene.Scene;

/**
 * class for manging scenes with DnD gesture
 *
 * @author favdb
 */
@SuppressWarnings("serial")
public class ManageSceneDnd extends ManageScene
		implements MouseListener, MouseMotionListener, DropTargetListener {

	private static final String TT = "ManageSceneDnd.";

	private MouseEvent firstMouseEvent = null;
	private DropTarget dt;
	private Color defaultBackgroundColor;

	/**
	 * default class definiton
	 *
	 * @param manageChapter
	 * @param scene
	 */
	public ManageSceneDnd(ManageChapter manageChapter, Scene scene) {
		this(manageChapter, scene, TYPE.SCENE);
	}

	/**
	 * *
	 * complete class definition
	 *
	 * @param manageChapter
	 * @param scene
	 * @param type
	 */
	public ManageSceneDnd(ManageChapter manageChapter, Scene scene, TYPE type) {
		super(manageChapter, scene, type);
	}

	/**
	 * default class definition with transferable object
	 *
	 * @param chapter
	 * @param scene
	 * @param transfer
	 */
	public ManageSceneDnd(ManageChapter chapter, Scene scene, ManageTransfer transfer) {
		this(chapter, scene);
		setTransferHandler(transfer);
	}

	/**
	 * complete class definition with transferable object
	 *
	 * @param chapter
	 * @param scene
	 * @param type
	 * @param transfer
	 */
	public ManageSceneDnd(ManageChapter chapter, Scene scene, TYPE type, ManageTransfer transfer) {
		super(chapter, scene, type);
		setTransferHandler(transfer);
	}

	/**
	 * initialize the user interface to add somethings
	 */
	@Override
	public void initUi() {
		super.initUi();
		addMouseListener(this);
		addMouseMotionListener(this);
		setAutoscrolls(true);
		defaultBackgroundColor = this.getBackground();
		dt = new DropTarget(this, DnDConstants.ACTION_MOVE, this, true);
		if (scene != null) {
			for (Component c : this.getComponents()) {
				if (c instanceof JLabel) {
					c.addMouseListener(this);
					c.addMouseMotionListener(this);
					if (c instanceof JComponent) {
						((JComponent) c).setTransferHandler(null);
					}
				}
			}
			manage.sceneAdd(this);
		}
	}

	//** Mouse gesture **//
	@Override
	public void mouseClicked(MouseEvent evt) {
		//LOG.trace(TT + "mouseClicked(evt)");
		if (scene == null) {
			return;
		}
		manage.sceneSelect(this);
		if (evt.getClickCount() == 2) {
			mainFrame.showEditorAsDialog(scene);
		}
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
	public void mousePressed(MouseEvent e) {
		//LOG.trace(TT + "mousePressed(evt)");
		if (scene == null) {
			return;
		}
		firstMouseEvent = e;
	}

	@Override
	public void mouseReleased(MouseEvent evt) {
		//empty
	}

	@Override
	public void mouseDragged(MouseEvent evt) {
		//LOG.trace(TT+"mouseDragged(evt)");
		if (firstMouseEvent == null) {
			return;
		}
		int dx = Math.abs(evt.getX() - firstMouseEvent.getX());
		int dy = Math.abs(evt.getY() - firstMouseEvent.getY());
		if (dx > 5 || dy > 5) {
			TransferHandler handler = getTransferHandler();
			if (handler != null) {
				handler.exportAsDrag(this, firstMouseEvent, TransferHandler.MOVE);
			}
			firstMouseEvent = null;
			manage.sceneSelect(this);
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		//empty
	}

	//// drag'n drop gesture ////
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		//LOG.trace(TT + "dragEnter(dtde)");
		if (isDropValid(dtde)) {
			dtde.acceptDrag(DnDConstants.ACTION_MOVE);
			setBackground(Color.LIGHT_GRAY);
		} else {
			dtde.rejectDrag();
		}
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		//LOG.trace(TT + "dragOver(dtde)");
		if (isDropValid(dtde)) {
			dtde.acceptDrag(DnDConstants.ACTION_MOVE);
			setBackground(UIManager.getColor("TextField.selectionBackground"));
		} else {
			dtde.rejectDrag();
			setBackground(defaultBackgroundColor);
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		// Empty
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		//LOG.trace(TT + "dragExit(dtde)");
		setBackground(defaultBackgroundColor);
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		//LOG.trace(TT + "drop(dtde)");
		try {
			TransferHandler handler = getTransferHandler();
			if (handler != null) {
				handler.importData(this, dtde.getTransferable());
			}
			dtde.getDropTargetContext().dropComplete(true);
		} catch (InvalidDnDOperationException e) {
			dtde.getDropTargetContext().dropComplete(false);
		} finally {
			setBackground(defaultBackgroundColor);
			firstMouseEvent = null;
			manage.clearSelection();
		}
	}

	/**
	 * check if the current selected scene would be dropped as a targeted
	 *
	 * @param dtde
	 * @return
	 */
	private boolean isDropValid(DropTargetDragEvent dtde) {
		//LOG.trace(TT + "isDropValid(dtde)");
		if (dtde.isDataFlavorSupported(ManageTransfer.sceneFlavor)) {
			ManageSceneDnd target = manage.sceneGetSelected();
			boolean b = type == TYPE.BEGIN
					|| type == TYPE.AFTER
					|| type == TYPE.MAKE_UNASSIGNED;
			return target != null && !target.equals(this) && b;
		}
		return false;
	}

}
