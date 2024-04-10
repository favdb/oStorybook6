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

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import storybook.App;
import storybook.db.scene.IntensityPanel;
import storybook.tools.swing.ColorIcon;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class ColorTCR extends DefaultTableCellRenderer {

	public ColorTCR() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
		boolean selected, boolean focus, int row, int column) {
		JLabel lbText = (JLabel) super.getTableCellRendererComponent(table, null,
			selected, focus, row, column);
		if (value == null) {
			return lbText;
		}
		int sz = App.fonts.defGet().getSize();
		if (value instanceof String) {
			lbText.setIcon(null);
		} else if (value instanceof Integer) {
			Integer v = ((Integer) value) - 1;
			Color color = IntensityPanel.colors[v];
			lbText.setIcon(new ColorIcon(color, sz));
			lbText.setText("" + (v + 1));
			lbText.setHorizontalTextPosition(SwingConstants.LEFT);
		} else if (value instanceof Color) {
			Color color = (Color) value;
			lbText.setIcon(new ColorIcon(color, sz));
		}
		return lbText;
	}

}
