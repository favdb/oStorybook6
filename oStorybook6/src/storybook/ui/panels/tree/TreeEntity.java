package storybook.ui.panels.tree;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

/**
 * class for the tree
 *
 * @author jean
 *
 */
@SuppressWarnings("serial")
public class TreeEntity extends JTree {

	public TreeEntity(TreeNode arg0) {
		super(arg0);
	}

	Insets autoscrollInsets = new Insets(20, 20, 20, 20); // insets

	/**
	 * automatic scroll to the given location
	 *
	 * @param cursorLocation
	 */
	public void autoscroll(Point cursorLocation) {
		Insets insets = getAutoscrollInsets();
		Rectangle outer = getVisibleRect();
		Rectangle inner = new Rectangle(outer.x + insets.left, outer.y + insets.top,
				outer.width - (insets.left + insets.right), outer.height - (insets.top + insets.bottom));
		if (!inner.contains(cursorLocation)) {
			Rectangle scrollRect = new Rectangle(cursorLocation.x - insets.left, cursorLocation.y - insets.top,
					insets.left + insets.right, insets.top + insets.bottom);
			scrollRectToVisible(scrollRect);
		}
	}

	/**
	 * get the scroll insets
	 *
	 * @return
	 */
	public Insets getAutoscrollInsets() {
		return autoscrollInsets;
	}
}
