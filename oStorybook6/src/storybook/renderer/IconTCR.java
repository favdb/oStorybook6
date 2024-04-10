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
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import resources.icons.IconUtil;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class IconTCR extends DefaultTableCellRenderer {

	public IconTCR() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);
		if (value == null) {
			return label;
		} else if (value instanceof String) {
			label.setIcon(IconUtil.getIconExternal((String) value, new Dimension(IconUtil.getDefDim())));
			label.setToolTipText((String) value);
		} else if (value instanceof Icon) {
			label.setIcon((Icon) value);
			label.setText("");
		}
		return label;
	}
}
