/*
Storybook: Scene-based software for novelists and authors.
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
package storybook.ui.panels.tree;

import i18n.I18N;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import storybook.App;
import storybook.db.abs.AbstractEntity;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.person.Person;
import storybook.db.scene.Scene;
import storybook.db.status.Status;
import storybook.tools.TextUtil;
import storybook.tools.html.Html;

/**
 * renderer class for the tree view
 *
 * @author favdb
 */
@SuppressWarnings("serial")
class TreeEntityRenderer extends DefaultTreeCellRenderer {

	private static final String SPAN_FORMAT = "<span style='color:%s;'>%s</span>";

	public TreeEntityRenderer() {
		super();
	}

	/**
	 * render itself
	 *
	 * @param tree
	 * @param value
	 * @param sel
	 * @param expanded
	 * @param leaf
	 * @param row
	 * @param hasFocus
	 * @return
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object object = node.getUserObject();
		if (object instanceof AbstractEntity) {
			JLabel lb = new JLabel();
			AbstractEntity entity = ((AbstractEntity) object);
			Icon icon = entity.getIcon();
			String txt = setNodeText(entity);
			if (entity instanceof Chapter && entity.getId() == -1L) {
				txt = I18N.getMsg("scenes.unassigned");
			} else if (entity instanceof Part && entity.getId() == -1L) {
				txt = I18N.getMsg("chapters.no_part");
			} else if (object instanceof Scene) {
				icon = Status.getStatusIcon(((Scene) object).getStatus());
			} else if (object instanceof Person && ((Person) object).getGender() != null) {
				icon = ((Person) object).getGender().getIcon();
			}
			lb.setIcon(icon);
			lb.setText(txt);
			setNotes(lb, object);
			return lb;
		}
		return this;
	}

	/**
	 * set the node text for the given entity
	 *
	 * @param entity
	 * @return
	 */
	private String setNodeText(AbstractEntity entity) {
		String txt = (entity.hasNotes() ? "*" : "") + entity.getName();
		if (App.preferences.treeviewGetTrunc()) {
			txt = TextUtil.ellipsize(txt, App.preferences.treeviewGetChar());
		}
		StringBuilder text = new StringBuilder();
		String aspect = entity.getAspect();
		if (!aspect.isEmpty()) {
			switch (aspect.charAt(0)) {
				case 'B':
					text.append("<b>")
							.append(String.format(SPAN_FORMAT, entity.getAspect().substring(1), txt))
							.append("</b>");
					break;
				case 'I':
					text.append("<i>")
							.append(String.format(SPAN_FORMAT, entity.getAspect().substring(1), txt))
							.append("</i>");
					break;
				default:
					text.append(String.format(SPAN_FORMAT, entity.getAspect().substring(1), txt));
					break;
			}
		} else {
			text.append(txt);
		}
		return text.toString();
	}

	/**
	 * set the notes information for the given object
	 *
	 * @param comp
	 * @param object
	 */
	private void setNotes(JComponent comp, Object object) {
		String texte = null;
		if (object instanceof AbstractEntity) {
			AbstractEntity entity = (AbstractEntity) object;
			texte = entity.getNotes();
		}
		if (!Html.htmlToText(texte).equals("")) {
			comp.setToolTipText(Html.intoHTML(Html.intoI(texte)));
		} else {
			comp.setToolTipText(null);
		}
	}
}
