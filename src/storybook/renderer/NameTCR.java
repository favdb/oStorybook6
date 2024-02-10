/*
Storybook: Scene-based software for novelists and authors.
Copyright (C) 2008 - 2011 Martin Mustun

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
 * table cell renderer for a Name containing renderer code as
 *
 * - first character must be 0=default, B=bold, I=italic<br>
 * - hhhhhh : hex codes for a color
 *
 * @author favdb
 */
public class NameTCR extends DefaultTableCellRenderer {

	private static final String SPAN_FORMAT = "<span style='color:%s;'>%s</span>";

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
	   boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
		   "", isSelected, hasFocus, row, column);
		if (value == null) {
			label.setText("");
		} else {
			if (value instanceof String) {
				String txt = (String) value;
				if (txt.contains("◘")) {
					String sx[] = txt.split("◘");
					StringBuilder result = new StringBuilder();
					if (sx.length > 1) {
						txt = sx[0];
						String aspect = sx[1];
						String color = aspect.substring(1);
						switch (aspect.charAt(0)) {
							case 'B':
								result.append("<b>")
								   .append(String.format(SPAN_FORMAT, color, txt))
								   .append("</b>");
								break;
							case 'I':
								result.append("<i>")
								   .append(String.format(SPAN_FORMAT, color, txt))
								   .append("</i>");
								break;
							default:
								result.append(String.format(SPAN_FORMAT, color, txt));
								break;
						}
					} else {
						result.append(txt.replace("◘", ""));
					}
					label.setText("<html>" + result.toString() + "</html>");
				} else {
					label.setText(txt);
				}
			} else {
				label.setText("value is not a String");
			}
		}
		return label;
	}

}
