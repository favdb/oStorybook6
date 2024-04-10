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
package storybook.ui.panel.tree;

import i18n.I18N;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import storybook.App;
import storybook.db.abs.AbstractEntity;
import storybook.db.chapter.Chapter;
import storybook.db.person.Person;
import storybook.db.scene.Scene;
import storybook.db.status.Status;
import storybook.tools.TextUtil;
import storybook.tools.html.Html;

@SuppressWarnings("serial")
class EntityTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final String SPAN_FORMAT = "<span style='color:%s;'>%s</span>";

	public EntityTreeCellRenderer() {
		super();
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
		boolean sel, boolean expanded, boolean leaf, int row,
		boolean hasFocus) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object object = node.getUserObject();
		if (leaf) {
			if (object instanceof Person && ((Person) object).getGender() != null) {
				setLeafIcon(((Person) object).getGender().getIcon());
			} else if (object instanceof Scene) {
				setLeafIcon(Status.getStatusIcon(((Scene) object).getStatus()));
			} else if (object instanceof AbstractEntity) {
				Icon icon = ((AbstractEntity) object).getIcon();
				setLeafIcon(icon);
			} else {
				setLeafIcon(null);
			}
		}
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (!leaf && object instanceof AbstractEntity) {
			Icon icon = ((AbstractEntity) object).getIcon();
			setIcon(icon);
			setText(((AbstractEntity) object).getName());
		} else if (leaf && object instanceof AbstractEntity) {
			AbstractEntity entity = (AbstractEntity) object;
			if (entity.getId() == -1L && object instanceof Chapter) {
				setText(I18N.getMsg("scenes.unassigned"));
			} else {
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
				setText("<html>" + text.toString() + "</html>");
			}
		}
		setNotes(object);
		return this;
	}

	private void setNotes(Object object) {
		String texte = null;
		if (object instanceof AbstractEntity) {
			AbstractEntity entity = (AbstractEntity) object;
			texte = entity.getNotes();
		}
		if (!Html.htmlToText(texte).equals("")) {
			setToolTipText(Html.HTML_B + texte + Html.HTML_E);
		} else {
			setToolTipText(null);
		}

	}
}
