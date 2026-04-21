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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import storybook.tools.LOG;
import static storybook.ui.panels.manage.ManageScene.TYPE.AFTER;
import static storybook.ui.panels.manage.ManageScene.TYPE.BEGIN;
import static storybook.ui.panels.manage.ManageScene.TYPE.MAKE_UNASSIGNED;
import static storybook.ui.panels.manage.ManageScene.TYPE.SCENE;
import static storybook.ui.panels.manage.ManageScene.TYPE.UNASSIGNED;

/**
 * Transfer handler class for Manage view
 *
 * @author favdb
 */
@SuppressWarnings("serial")
public class ManageTransfer extends TransferHandler {

	private static final String TT = "ManageTransfer.";

	public final static DataFlavor sceneFlavor = new DataFlavor(ManageSceneDnd.class, "ManageSceneDnd");
	private ManageSceneDnd sourceScene;
	private final Manage manage;

	public ManageTransfer(Manage manage) {
		this.manage = manage;
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		for (DataFlavor flavor : transferFlavors) {
			if (sceneFlavor.equals(flavor)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected Transferable createTransferable(JComponent comp) {
		//LOG.trace(TT + "createTransferable(comp)");
		if (!(comp instanceof ManageSceneDnd)) {
			return null;
		}
		ManageSceneDnd sceneDnd = (ManageSceneDnd) comp;
		if (sceneDnd.scene == null) {
			return null;
		}
		sourceScene = sceneDnd;
		Transferable t = new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[]{sceneFlavor};
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return sceneFlavor.equals(flavor);
			}

			@Override
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				if (isDataFlavorSupported(flavor)) {
					return sceneDnd.scene.getName();
				}
				throw new UnsupportedFlavorException(flavor);
			}
		};
		return t;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.MOVE;
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		//LOG.trace(TT + "importData(comp, t)");
		try {
			if (!canImport(comp, t.getTransferDataFlavors())) {
				return false;
			}
			ManageSceneDnd source = sourceScene;
			if (source == null) {
				source = manage.sceneGetSelected();
			}
			if (source == null || source.scene == null) {
				return false;
			}
			ManageSceneDnd dest = (ManageSceneDnd) comp;
			if (source.equals(dest)) {
				return false;
			}
			boolean result = false;
			switch (dest.getType()) {
				case BEGIN:
					if (dest.getChapter() != null) {
						manage.sceneMoveToBegin(source, dest.getChapter());
						result = true;
					}
					break;
				case AFTER:
					if (dest.scene != null) {
						manage.sceneMoveAfter(source, dest);
						result = true;
					}
					break;
				case MAKE_UNASSIGNED:
					manage.sceneSetUnassigned();
					result = true;
					break;
				case SCENE:
				case UNASSIGNED:
					result = false;
					break;
				default:
					result = false;
					break;
			}
			if (result) {
				sourceSceneClear();
			}
			return result;
		} catch (Exception ex) {
			LOG.err(TT + "importData: exception " + ex.getMessage());
			return false;
		}
	}

	/**
	 * clear the source information
	 */
	private void sourceSceneClear() {
		//LOG.trace(TT+"clearSourceScene()");
		sourceScene = null;
	}

}
