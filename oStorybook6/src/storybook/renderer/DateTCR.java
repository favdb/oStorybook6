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

import i18n.I18N;
import java.awt.Component;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import storybook.tools.DateUtil;

/**
 * @author martin
 *
 */
public class DateTCR extends DefaultTableCellRenderer {

	public DateTCR() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
	   boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
		   value, isSelected, hasFocus, row, column);
		label.setToolTipText("");
		if (value instanceof Date) {
			label.setText(DateUtil.dateToString((Date) value, false));
		} else if (value instanceof String) {
			label.setText((String) value);
		} else {
			label.setText("?");
			label.setToolTipText(I18N.getMsg("date.none"));
		}
		return label;
	}

	/*@Override
	public void setValue(Object value) {
		try {
			if (value instanceof Date) {
				setText(DateUtil.dateToString((Date) value, false));
			} else {
				setText("");
			}
		} catch (Exception e) {
			setText("");
		}
	}*/
}
