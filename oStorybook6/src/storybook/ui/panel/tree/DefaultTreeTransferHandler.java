package storybook.ui.panel.tree;

import java.awt.Point;
import java.awt.dnd.DnDConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.tools.LOG;
import storybook.ui.MainFrame;

public class DefaultTreeTransferHandler extends AbstractTreeTransferHandler {

	private static final String TT = "DefaultTreeTransferHandler.";

	public DefaultTreeTransferHandler(TreePanel treePanel, int action) {
		super(treePanel, action, true);
	}

	/**
	 * check if drop is allowed
	 *
	 * @param target
	 * @param draggedNode
	 * @param action
	 * @param location
	 * @return
	 */
	@Override
	public boolean canPerformAction(Tree target, TreeNode draggedNode, int action, Point location) {
		TreePath pathTarget = target.getPathForLocation(location.x, location.y);
		if (pathTarget == null) {
			target.setSelectionPath(null);
			return false;
		}
		target.setSelectionPath(pathTarget);
		if (action == DnDConstants.ACTION_MOVE) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathTarget.getLastPathComponent();
			Object draggedObject = ((DefaultMutableTreeNode) draggedNode).getUserObject();
			if (node != null) {
				Object targetObject = node.getUserObject();
				if (targetObject != null) {
					if (draggedObject instanceof Scene) {
						return (targetObject instanceof Chapter) || (targetObject instanceof Scene);
					} else if (draggedObject instanceof Chapter) {
						return (targetObject instanceof Part) || (targetObject instanceof Chapter);
					}
				}
			}
		}
		return false;
	}

	/**
	 * execute the drop action
	 *
	 * @param targetTree
	 * @param dragged
	 * @param target
	 * @param action
	 * @return
	 */
	@Override
	public boolean executeDrop(Tree targetTree, TreeNode dragged, TreeNode target, int action) {
		if (action == DnDConstants.ACTION_MOVE) {
			Object draggedObj = ((DefaultMutableTreeNode) dragged).getUserObject();
			Object targetObj = ((DefaultMutableTreeNode) target).getUserObject();

			if ((draggedObj instanceof Scene) && (targetObj instanceof Chapter)) {
				dropSceneInChapter((Scene) draggedObj, (Chapter) targetObj);
			} else if ((draggedObj instanceof Scene) && (targetObj instanceof Scene)) {
				dropSceneBeforeScene((Scene) draggedObj, (Scene) targetObj);
			} else if ((draggedObj instanceof Chapter) && (targetObj instanceof Part)) {
				dropChapterInPart((Chapter) draggedObj, (Part) targetObj);
			} else if ((draggedObj instanceof Chapter) && (targetObj instanceof Chapter)) {
				dropChapterBeforeChapter((Chapter) draggedObj, (Chapter) targetObj);
			}
			return true;
		}
		return false;
	}

	/**
	 * drop the given chapter before an other one
	 *
	 * @param chapter
	 * @param target
	 */
	private void dropChapterBeforeChapter(Chapter chapter, Chapter target) {
		MainFrame frame = treePanel.getMainFrame();// SwingUtilities.getWindowAncestor(getTree());
		frame.project.chapters.insertBefore(chapter, target);
		frame.getBookController().updateEntity(chapter);
		getTreePanel().treeRefresh();
		frame.setUpdated();
	}

	/**
	 * drop the given chapter into the given part
	 *
	 * @param chapter
	 * @param target
	 */
	private void dropChapterInPart(Chapter chapter, Part target) {
		MainFrame frame = treePanel.getMainFrame();// SwingUtilities.getWindowAncestor(getTree());
		frame.project.chapters.insertInto(chapter, target);
		frame.getBookController().updateEntity(chapter);
		getTreePanel().treeRefresh();
		frame.setUpdated();
	}

	/**
	 * drop the given scene before an other one and renumber the chapter
	 *
	 * @param scene
	 * @param target
	 */
	private void dropSceneBeforeScene(Scene scene, Scene target) {
		/*LOG.trace(TT + "dropSceneBeforeScene("
				+ "scene=" + LOG.trace(scene)
				+ ", target=" + LOG.trace(target) + ")");*/
		MainFrame frame = treePanel.getMainFrame();// SwingUtilities.getWindowAncestor(getTree());
		frame.project.scenes.insertBefore(scene, target);
		frame.project.scenes.renumber(target.getChapter());
		frame.getBookController().updateEntity(scene);
		frame.setUpdated();
	}

	/**
	 * drop the given scene into the given chapter in last position
	 *
	 * @param scene
	 * @param chapter
	 */
	private void dropSceneInChapter(Scene scene, Chapter chapter) {
		LOG.trace(TT + "dropSceneInChapter(scene=" + LOG.trace(scene)
				+ ", chapter=" + LOG.trace(chapter) + ")");
		MainFrame frame = (MainFrame) SwingUtilities.getWindowAncestor(getTree());
		frame.project.scenes.insertInto(scene, chapter);
		//renumber the scenes of the chapter for increment
		frame.project.scenes.renumber(chapter);
		frame.getBookController().updateEntity(scene);
		frame.setUpdated();
	}

}
