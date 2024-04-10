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
package storybook.renderer;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import storybook.db.abs.AbstractEntity;
import storybook.db.endnote.Endnote;
import storybook.db.idea.Idea;
import storybook.tools.swing.FontUtil;

/**
 * @author favdb
 *
 */
@SuppressWarnings("serial")
public class EntityLCR extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean sel, boolean focus) {
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, sel, focus);
		if (value instanceof Idea) {
			AbstractEntity entity = (AbstractEntity) value;
			label.setText(entity.getName());
		} else if (value instanceof Endnote) {
			Endnote entity = (Endnote) value;
			label.setText(entity.getNumber().toString());
		} else if (value instanceof String) {
			label.setText((String) value);
			label.setMinimumSize(new Dimension(FontUtil.getWidth(), FontUtil.getHeight()));
		} else if (value instanceof AbstractEntity) {
			AbstractEntity entity = (AbstractEntity) value;
			label.setIcon(entity.getIcon());
			label.setText(entity.getFullName());
		}
		return label;
	}
}
