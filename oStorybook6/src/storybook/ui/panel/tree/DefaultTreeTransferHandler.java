package storybook.ui.panel.tree;

import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import storybook.App;
import storybook.ctrl.Ctrl;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.ui.MainFrame;

public class DefaultTreeTransferHandler extends AbstractTreeTransferHandler {

	private static final String TT = "DefaultTreeTransferHandler";

	public DefaultTreeTransferHandler(TreePanel treePanel, int action) {
		super(treePanel, action, true);
	}

	@Override
	public boolean canPerformAction(Tree target, TreeNode draggedNode, int action, Point location) {
		TreePath pathTarget = target.getPathForLocation(location.x, location.y);
		if (pathTarget == null) {
			target.setSelectionPath(null);
			return (false);
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
		return (false);
	}

	@Override
	public boolean executeDrop(Tree targetTree, TreeNode draggedNode, TreeNode targetNode, int action) {
		if (action == DnDConstants.ACTION_MOVE) {
			Object draggedObject = ((DefaultMutableTreeNode) draggedNode).getUserObject();
			Object targetObject = ((DefaultMutableTreeNode) targetNode).getUserObject();

			if ((draggedObject instanceof Scene) && (targetObject instanceof Chapter)) {
				dropSceneInChapter((Scene) draggedObject, (Chapter) targetObject);
			} else if ((draggedObject instanceof Scene) && (targetObject instanceof Scene)) {
				dropSceneBeforeScene((Scene) draggedObject, (Scene) targetObject);
			} else if ((draggedObject instanceof Chapter) && (targetObject instanceof Part)) {
				dropChapterInPart((Chapter) draggedObject, (Part) targetObject);
			} else if ((draggedObject instanceof Chapter) && (targetObject instanceof Chapter)) {
				dropChapterBeforeChapter((Chapter) draggedObject, (Chapter) targetObject);
			}
			return (true);
		}
		return (false);
	}

	private void dropChapterBeforeChapter(Chapter dragged, Chapter target) {
		MainFrame frame = (MainFrame) SwingUtilities.getWindowAncestor(getTree());
		List<Chapter> toUpdate = new ArrayList<>();
		List<Chapter> chapters = frame.project.chapters.find(target.getPart());
		int refChapterNumber = target.getChapterno();
		for (Chapter chapter : chapters) {
			int chapterNum = chapter.getChapterno();
			if (!chapter.equals(dragged) && chapterNum >= refChapterNumber) {
				chapter.setChapterno(chapterNum + 1);
				toUpdate.add(chapter);
			}
		}
		dragged.setPart(target.getPart());
		dragged.setChapterno(refChapterNumber);
		Ctrl ctrl = frame.getBookController();
		ctrl.updateEntity(dragged);
		for (Chapter chapter : toUpdate) {
			ctrl.updateEntity(chapter);
		}
		ctrl.updateEntity(target.getPart());
		int newNum = 1;
		List<Chapter> chapters1 = frame.project.chapters.find(target.getPart());
		for (Chapter chapter : chapters1) {
			chapter.setChapterno(newNum++);
		}
		for (Chapter chapter : chapters1) {
			ctrl.updateEntity(chapter);
		}
		ctrl.updateEntity(target.getPart());
		getTreePanel().treeRefresh();
		frame.setUpdated();
	}

	private void dropChapterInPart(Chapter dragged, Part target) {
		MainFrame frame = (MainFrame) SwingUtilities.getWindowAncestor(getTree());
		List<Chapter> chapters = frame.project.chapters.find(target);
		int lastChapterNumber = 0;
		for (Chapter chapter : chapters) {
			int chapterNum = chapter.getChapterno();
			if (chapterNum > lastChapterNumber) {
				lastChapterNumber = chapterNum;
			}
		}
		dragged.setPart(target);
		dragged.setChapterno(lastChapterNumber + 1);
		Ctrl ctrl = frame.getBookController();
		ctrl.updateEntity(dragged);
		ctrl.updateEntity(target);
		getTreePanel().treeRefresh();
		frame.setUpdated();
	}

	private void dropSceneBeforeScene(Scene dragged, Scene target) {
		MainFrame frame = (MainFrame) SwingUtilities.getWindowAncestor(getTree());
		List<Scene> toUpdate = new ArrayList<>();
		List<Scene> scenes = frame.project.scenes.find(target.getChapter());
		int refSceneNumber = target.getSceneno();
		for (Scene scene : scenes) {
			int sceneNum = scene.getSceneno();
			if (!scene.equals(dragged)
			   && sceneNum >= refSceneNumber) {
				scene.setSceneno(sceneNum + 1);
				toUpdate.add(scene);
			}
		}
		dragged.setChapter(target.getChapter());
		dragged.setSceneno(refSceneNumber);
		Ctrl ctrl = frame.getBookController();
		ctrl.updateEntity(dragged);
		for (Scene scene : toUpdate) {
			ctrl.updateEntity(scene);
		}
		ctrl.updateEntity(target.getChapter());
		int newNum = 1;
		List<Scene> scenes1 = frame.project.scenes.find(target.getChapter());
		for (Scene scene : scenes1) {
			scene.setSceneno(newNum++);
		}
		for (Scene scene : scenes1) {
			ctrl.updateEntity(scene);
		}
		ctrl.updateEntity(target.getChapter());
		getTreePanel().treeRefresh();
		frame.setUpdated();
	}

	private void dropSceneInChapter(Scene dragged, Chapter target) {
		//LOG.trace(TT + ".dropSceneInChapter(dragged=" + dragged.getName() + ", target=" + target.getName() + ")");
		MainFrame frame = (MainFrame) SwingUtilities.getWindowAncestor(getTree());
		List<Scene> scenes = frame.project.scenes.find(target);
		int lastSceneNumber = 0;
		for (Scene scene : scenes) {
			lastSceneNumber = Math.max(lastSceneNumber, scene.getSceneno());
		}
		if (target.getChapterno() == -1) {
			dragged.setChapter(null);
		} else {
			dragged.setChapter(target);
		}
		int c = 1;
		if (!App.preferences.sceneIsRenumAuto()) {
			c = App.preferences.sceneGetRenumInc();
		}
		dragged.setSceneno(lastSceneNumber + c);
		Ctrl ctrl = frame.getBookController();
		ctrl.updateEntity(dragged);
		if (target.getChapterno() != -1) {
			ctrl.updateEntity(target);
		}
		frame.project.scenes.renumber(frame, target);
		frame.setUpdated();
	}

}
