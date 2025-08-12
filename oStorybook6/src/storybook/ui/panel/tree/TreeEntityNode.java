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
import javax.swing.tree.DefaultMutableTreeNode;
import storybook.db.abs.AbstractEntity;

/**
 * class for an AbstractEntity node in a JTree
 *
 * @author favdb
 */
@SuppressWarnings("serial")
public class TreeEntityNode extends DefaultMutableTreeNode {

	private final String text;
	private final AbstractEntity entity;

	/**
	 * initialize for a real entity
	 *
	 * @param entity
	 */
	public TreeEntityNode(AbstractEntity entity) {
		super();
		this.text = entity.getName();
		this.entity = entity;
	}

	/**
	 * initialize for a virtual entity
	 *
	 * @param textKey text for the node
	 * @param entity
	 */
	public TreeEntityNode(String textKey, AbstractEntity entity) {
		super();
		this.text = I18N.getMsg(textKey);
		this.entity = entity;
	}

	/**
	 * get the entity
	 *
	 * @return
	 */
	public AbstractEntity getEntity() {
		return entity;
	}

	@Override
	public String toString() {
		return text;
	}
}
