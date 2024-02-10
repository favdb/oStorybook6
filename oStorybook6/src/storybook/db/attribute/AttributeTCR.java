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
package storybook.db.attribute;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import storybook.tools.LOG;
import storybook.tools.ListUtil;

/**
 * Table cell renderer for Attribute entity
 *
 * @author favdb
 */
@SuppressWarnings("serial")
public class AttributeTCR extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
	   boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel label = new JLabel();
		try {
			if (value == null) {
				return label;
			}
			if (value instanceof String) {
				label.setText((String) value);
			}
			if (value instanceof List) {
				List list = (List) value;
				if (list.isEmpty()) {
					return label;
				}
				if (list.get(0) instanceof Attribute) {
					List<String> ls = new ArrayList<>();
					@SuppressWarnings("unchecked")
					List<Attribute> lsattr = (List<Attribute>) value;
					for (Attribute a : lsattr) {
						ls.add(a.getName());
					}
					label.setText(ListUtil.join(ls, ", "));
				}
			}
		} catch (Exception e) {
			LOG.err("AttributesTCR error", e);
		}
		return label;
	}

}
