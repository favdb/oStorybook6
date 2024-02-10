package storybook.ui.panel.tree;

import java.awt.datatransfer.*;
import java.util.*;
import javax.swing.tree.*;

public class TransferableNode implements Transferable {

	public static final DataFlavor NODE_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Node");
	private final TreeNode node;
	private final DataFlavor[] flavors = {NODE_FLAVOR};

	public TransferableNode(TreeNode draggedNode) {
		node = draggedNode;
	}

	@Override
	public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (flavor == NODE_FLAVOR) {
			return node;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return Arrays.asList(flavors).contains(flavor);
	}
}
