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
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * cell renderer for number with separator
 *
 * @author favdb
 *
 */
@SuppressWarnings("serial")
public class NumericTCR extends DefaultTableCellRenderer {

	public NumericTCR() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel lbText = (JLabel) super.getTableCellRendererComponent(table,
				null, isSelected, hasFocus, row, column);
		if (value == null) {
			return lbText;
		} else if (value instanceof String) {
			lbText.setText((String) value);
		} else if (value instanceof Integer) {
			Integer v = (Integer) value;
			lbText.setText(String.format("%,d", v));
		}
		return lbText;
	}

}
