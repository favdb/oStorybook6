/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

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
package storybook.tools.swing;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * some utilities for JTree
 *
 * @author martin
 *
 */
public class TreeUtil {

	private static final String TT = "TreeUtil";

	private TreeUtil() {
		// empty
	}

	/**
	 * check if order is descendant
	 *
	 * @param path1
	 * @param path2
	 * @return
	 */
	public static boolean isDescendant(TreePath path1, TreePath path2) {
		int count1 = path1.getPathCount();
		int count2 = path2.getPathCount();
		if (count1 <= count2) {
			return false;
		}
		while (count1 != count2) {
			path1 = path1.getParentPath();
			count1--;
		}
		return path1.equals(path2);
	}

	/**
	 * get the expansion state
	 *
	 * @param tree
	 * @param row
	 * @return
	 */
	public static String expansionStateGet(JTree tree, int row) {
		//LOG.trace(TT + ".expansionStateGet(tree, row="+row+")");
		TreePath rowPath = tree.getPathForRow(row);
		StringBuilder buf = new StringBuilder();
		int rowCount = tree.getRowCount();
		for (int i = row; i < rowCount; i++) {
			TreePath path = tree.getPathForRow(i);
			if (i == row || isDescendant(path, rowPath)) {
				if (tree.isExpanded(path)) {
					buf.append(",").append(String.valueOf(i - row));
				}
			} else {
				break;
			}
		}
		return buf.toString();
	}

	/**
	 * set expansion state
	 *
	 * @param tree
	 * @param row
	 * @param state
	 */
	public static void expanstionStateSet(JTree tree, int row, String state) {
		//LOG.trace(TT + ".expansionStateSet(tree, row=" + row + ", state=" + state + ")");
		StringTokenizer stok = new StringTokenizer(state, ",");
		while (stok.hasMoreTokens()) {
			int token = row + Integer.parseInt(stok.nextToken());
			tree.expandRow(token);
		}
	}

	/**
	 * save expansion state
	 *
	 * @param tree
	 * @return
	 */
	public static List<String> expansionSave(JTree tree) {
		//LOG.trace(TT + ".expansionSave(tree)");
		List<String> list = new ArrayList<>();
		int rowCount = tree.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			TreePath path = tree.getPathForRow(i);
			if (tree.isExpanded(path)) {
				list.add(path.toString());
			}
		}
		return list;
	}

	/**
	 * restore expansion state
	 *
	 * @param tree
	 * @param list
	 */
	public static void expansionRestore(JTree tree, List<String> list) {
		//LOG.trace(TT + ".expansionRestore(tree, list nb=" + list.size() + ")");
		int rowCount = tree.getRowCount();
		for (String s : list) {
			for (int i = 0; i < rowCount; i++) {
				TreePath path = tree.getPathForRow(i);
				if (s.equals(path.toString())) {
					tree.expandRow(i);
					rowCount = tree.getRowCount();
				}
			}
		}
	}

}
