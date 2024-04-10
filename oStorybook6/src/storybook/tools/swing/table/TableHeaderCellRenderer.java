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
package storybook.tools.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import storybook.tools.DateUtil;

public class TableHeaderCellRenderer implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(
	    JTable table, Object value,
	    boolean isSelected, boolean hasFocus,
	    int row, int column) {
	if (value == null) {
	    return new JLabel("");
	}
	if (value instanceof Date) {
	    return new JLabel(DateUtil.dateToString((Date) value, false));
	}
	JLabel label = new JLabel(value.toString());
	Color color = UIManager.getColor("Table.background");
	if (isSelected) {
	    color = UIManager.getColor("Table.selectionBackground");
	}
	label.setOpaque(true);
	label.setBackground(color);
	return label;
    }
}
