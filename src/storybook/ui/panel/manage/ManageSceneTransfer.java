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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import storybook.tools.LOG;

@SuppressWarnings("serial")
public class ManageSceneTransfer extends TransferHandler {

	private static final String TT = "SceneTransferHandler";

	private final DataFlavor sceneFlavor = DataFlavor.stringFlavor;
	private ManageSceneDnd sourceScene;
	private final Manage manage;

	public ManageSceneTransfer(Manage manage) {
		this.manage = manage;
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		//LOG.trace(TT + ".importData(comp=" + comp.toString() + "\n, t=" + t.toString() + ")");
		if (canImport(comp, t.getTransferDataFlavors())) {
			if (sourceScene == null) {
				if (manage.sceneGetSelected() == null) {
					LOG.err(TT + ".importData sourceScene is null");
					return true;
				}
				sourceScene = manage.sceneGetSelected();
			}
			ManageSceneDnd destScene = (ManageSceneDnd) comp;
			if (destScene == null) {
				LOG.err(TT + ".importData destScene is null");
				manage.refreshData();
				return true;
			}
			if (destScene.getType() == ManageScene.TYPE.MAKE_UNASSIGNED) {
				manage.sceneSetUnassigned();
				return true;
			}
			// don't drop on myself
			if (sourceScene == destScene) {
				manage.refreshData();
				return true;
			}
			if (destScene.getType() != ManageSceneDnd.TYPE.BEGIN && !destScene.getScene().hasChapter()) {
				// this is an unassigned scene to set to unassigned, just refresh
				manage.refreshData();
				return true;
			}
			switch (destScene.getType()) {
				case BEGIN:
					manage.sceneMoveToBegin(sourceScene, destScene.getChapter());
					break;
				case SCENE:// uncomment to swap scenes
				//manage.sceneSwap(sourceScene, destScene);
				//break;
				case NEXT:
					manage.sceneMoveToNext(sourceScene, destScene);
					break;
				case MAKE_UNASSIGNED:
					manage.sceneSetUnassigned();
					break;
				default:
					break;
			}
		}
		return false;
	}

	@Override
	protected Transferable createTransferable(JComponent comp) {
		sourceScene = (ManageSceneDnd) comp;
		return new SceneTransferable(sourceScene);
	}

	@Override
	public int getSourceActions(JComponent comp) {
		return COPY_OR_MOVE;
	}

	@Override
	protected void exportDone(JComponent comp, Transferable data, int action) {
		// empty
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] flavors) {
		for (DataFlavor flavor : flavors) {
			if (sceneFlavor.equals(flavor)) {
				return true;
			}
		}
		return false;
	}

	class SceneTransferable implements Transferable {

		private final String sceneId;

		SceneTransferable(ManageSceneDnd pic) {
			sceneId = Long.toString(pic.getScene().getId());
		}

		@Override
		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return sceneId;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{sceneFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return sceneFlavor.equals(flavor);
		}
	}

}
