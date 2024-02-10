/*
 * Copyright (C) 2019 favdb
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.attribute;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 *
 * @author favdb
 */
public class AttributeLCR extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean sel, boolean focus) {
		try {
			JLabel lb = (JLabel) super.getListCellRendererComponent(list, value, index, sel, focus);
			if (value instanceof String) {
				lb.setText((String) value);
			} else if (value instanceof Attribute) {
				lb.setText(((Attribute) value).toString());
			}
			return lb;
		} catch (Exception e) {
			return new JLabel("");
		}
	}
}
