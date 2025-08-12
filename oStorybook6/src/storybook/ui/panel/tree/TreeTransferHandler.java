package storybook.ui.panel.tree;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.ui.MainFrame;

public class TreeTransferHandler
		implements DragGestureListener, DragSourceListener, DropTargetListener {

	private static final String TT = "DefaultTreeTransferHandler.";

	private final MainFrame mainFrame;
	public TreePanel treePanel;
	private final TreeEntity tree;
	private final DragSource dragSource; // dragsource
	private static TreeNode draggedNode;
	private TreeNode draggedNodeParent;
	private static BufferedImage image = null; //buff image
	private final Rectangle rect2D = new Rectangle();
	private final boolean drawImage;
	private final DropTarget dt;

	public TreeTransferHandler(TreePanel treePanel, int action) {
		this.mainFrame = treePanel.getMainFrame();
		this.treePanel = treePanel;
		this.tree = treePanel.getTree();
		this.drawImage = true;
		this.dragSource = new DragSource();
		this.dragSource.createDefaultDragGestureRecognizer(tree, action, this);
		dt = new DropTarget(tree, action, this);
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
	public boolean canPerformAction(TreeEntity target, TreeNode draggedNode, int action, Point location) {
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
						Scene s = (Scene) draggedObject;
						//dragged scene to a chapter is not allowed
						/*if (targetObject instanceof Chapter && Objects.equals(s.getChapter(), targetObject)) {
							return false;
						}*/
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
	public boolean executeDrop(TreeEntity targetTree, TreeNode dragged, TreeNode target, int action) {
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

	@Override
	public void dragDropEnd(DragSourceDropEvent dsde) {
		if (dsde.getDropSuccess()
				&& dsde.getDropAction() == DnDConstants.ACTION_MOVE
				&& draggedNodeParent != null) {
			((DefaultTreeModel) tree.getModel()).nodeStructureChanged(draggedNodeParent);
		}
	}

	@Override
	public final void dragEnter(DragSourceDragEvent dsde) {
		int action = dsde.getDropAction();
		if (action == DnDConstants.ACTION_COPY) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		} else {
			if (action == DnDConstants.ACTION_MOVE) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			} else {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}
		}
	}

	@Override
	public final void dragOver(DragSourceDragEvent dsde) {
		int action = dsde.getDropAction();
		if (action == DnDConstants.ACTION_COPY) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		} else {
			if (action == DnDConstants.ACTION_MOVE) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			} else {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}
		}
	}

	@Override
	public final void dropActionChanged(DragSourceDragEvent dsde) {
		int action = dsde.getDropAction();
		if (action == DnDConstants.ACTION_COPY) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		} else {
			if (action == DnDConstants.ACTION_MOVE) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			} else {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}
		}
	}

	@Override
	public final void dragExit(DragSourceEvent dse) {
		dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
	}

	@Override
	public final void dragGestureRecognized(DragGestureEvent dge) {
		TreePath path = tree.getSelectionPath();
		if (path != null) {
			draggedNode = (TreeNode) path.getLastPathComponent();
			draggedNodeParent = (TreeNode) draggedNode.getParent();
			if (drawImage) {
				Rectangle pathBounds = tree.getPathBounds(path); //getpathbounds of selectionpath
				JComponent lbl = (JComponent) tree.getCellRenderer()
						.getTreeCellRendererComponent(tree,
								draggedNode, false,
								tree.isExpanded(path),
								((DefaultTreeModel) tree.getModel())
										.isLeaf(path.getLastPathComponent()),
								0, false);
				lbl.setBounds(pathBounds);
				image = new BufferedImage(lbl.getWidth(), lbl.getHeight(),
						BufferedImage.TYPE_INT_ARGB_PRE);
				Graphics2D graphics = image.createGraphics();
				graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
				lbl.setOpaque(false);
				lbl.paint(graphics);
				graphics.dispose();
			}
			dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop, image,
					new Point(0, 0), new TreeTransferable(draggedNode), this);
		}
	}

	@Override
	public final void dragEnter(DropTargetDragEvent dtde) {
		Point pt = dtde.getLocation();
		int action = dtde.getDropAction();
		if (drawImage) {
			paintImage(pt);
		}
		if (canPerformAction(tree, draggedNode, action, pt)) {
			dtde.acceptDrag(action);
		} else {
			dtde.rejectDrag();
		}
	}

	@Override
	public final void dragExit(DropTargetEvent dte) {
		if (drawImage) {
			clearImage();
		}
	}

	@Override
	public final void dragOver(DropTargetDragEvent dtde) {
		Point pt = dtde.getLocation();
		int action = dtde.getDropAction();
		tree.autoscroll(pt);
		if (drawImage) {
			paintImage(pt);
		}
		if (canPerformAction(tree, draggedNode, action, pt)) {
			dtde.acceptDrag(action);
		} else {
			dtde.rejectDrag();
		}
	}

	@Override
	public final void dropActionChanged(DropTargetDragEvent dtde) {
		Point pt = dtde.getLocation();
		int action = dtde.getDropAction();
		if (drawImage) {
			paintImage(pt);
		}
		if (canPerformAction(tree, draggedNode, action, pt)) {
			dtde.acceptDrag(action);
		} else {
			dtde.rejectDrag();
		}
	}

	@Override
	public final void drop(DropTargetDropEvent dtde) {
		try {
			if (drawImage) {
				clearImage();
			}
			int action = dtde.getDropAction();
			Transferable transferable = dtde.getTransferable();
			Point pt = dtde.getLocation();
			if (transferable.isDataFlavorSupported(TreeTransferable.NODE_FLAVOR)
					&& canPerformAction(tree, draggedNode, action, pt)) {
				TreePath pathTarget = tree.getPathForLocation(pt.x, pt.y);
				TreeNode node = (TreeNode) transferable.getTransferData(TreeTransferable.NODE_FLAVOR);
				TreeNode newParentNode = (TreeNode) pathTarget.getLastPathComponent();
				if (executeDrop(tree, node, newParentNode, action)) {
					dtde.acceptDrop(action);
					dtde.dropComplete(true);
					return;
				}
			}
			dtde.rejectDrop();
			dtde.dropComplete(false);
		} catch (UnsupportedFlavorException | IOException e) {
			dtde.rejectDrop();
			dtde.dropComplete(false);
		}
	}

	private void paintImage(Point pt) {
		// empty
	}

	private void clearImage() {
		tree.paintImmediately(rect2D.getBounds());
	}

	public TreeEntity getTree() {
		return tree;
	}

	protected TreePanel getTreePanel() {
		return treePanel;
	}

	/**
	 * drop the given chapter before an other one
	 *
	 * @param chapter
	 * @param target
	 */
	private void dropChapterBeforeChapter(Chapter chapter, Chapter target) {
		mainFrame.project.chapters.insertBefore(chapter, target);
		mainFrame.getBookController().updateEntity(chapter);
		mainFrame.setUpdated();
	}

	/**
	 * drop the given chapter into the given part
	 *
	 * @param chapter
	 * @param target
	 */
	private void dropChapterInPart(Chapter chapter, Part target) {
		chapter.setPart(target);
		mainFrame.getBookController().updateEntity(chapter);
		mainFrame.setUpdated();
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
		if (target == null) {
			return;
		}
		Chapter old = scene.getChapter();
		Chapter chapter = target.getChapter();
		List<Scene> sc = (chapter == null ? mainFrame.project.scenes.findUnassigned()
				: mainFrame.project.scenes.find(chapter));
		if (Objects.equals(old, chapter)) {
			sc.remove(scene);
		}
		scene.setChapter(chapter);
		int idx = sc.indexOf(target);
		if (idx == -1) {
			sc.add(scene);
		} else {
			sc.add(idx, scene);
		}
		idx = 1;
		for (Scene s : sc) {
			s.setSceneno(idx++);
		}
		mainFrame.project.scenes.renumber(chapter);
		//mainFrame.project.scenes.insertBefore(scene, target);
		mainFrame.getBookController().updateEntity(scene);
		mainFrame.setUpdated();
	}

	/**
	 * drop the given scene into the given chapter in last position
	 *
	 * @param scene
	 * @param chapter
	 */
	private void dropSceneInChapter(Scene scene, Chapter chapter) {
		//LOG.trace(TT + "dropSceneInChapter(scene=" + LOG.trace(scene)
		//		+ ", chapter=" + LOG.trace(chapter) + ")");
		if (chapter == null || chapter.getId() == -1L) {
			//set unassigned
			scene.setChapter();
			List<Scene> ls = mainFrame.project.scenes.findUnassigned();
			int n = 1;
			for (Scene s : ls) {
				s.setSceneno(n++);
			}
		} else {
			List<Scene> ls = mainFrame.project.scenes.find(chapter);
			if (scene.hasChapter() && scene.getChapter().equals(chapter)) {
				ls.remove(scene);
			}
			@SuppressWarnings("null")
			int n = (ls.isEmpty() ? 1 : ls.get(ls.size() - 1).getSceneno());
			scene.setChapter(chapter);
			scene.setSceneno(++n);
			mainFrame.project.scenes.renumber(chapter);
		}
		mainFrame.getBookController().updateEntity(scene);
		mainFrame.setUpdated();
	}

}
