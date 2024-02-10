/*
 * SbApp: Open Source software for novelists and authors.
 * Original idea 2008 - 2012 Martin Mustun
 * Copyrigth (C) Favdb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package storybook.ui.chart.wiww;

import java.awt.Component;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import storybook.tools.DateUtil;

public class WiWWTableCellRenderer implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
	   boolean isSelected, boolean hasFocus, int row, int column) {
		if ((value == null) || (!(value instanceof WiWWContainer))) {
			return new JLabel("");
		}
		if (value instanceof Date) {
			return new JLabel(DateUtil.dateToString((Date) value, false));
		}
		return new WiWWPanel((WiWWContainer) value, isSelected);
	}
}
