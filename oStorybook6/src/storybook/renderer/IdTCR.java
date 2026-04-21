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
import storybook.db.scene.Scene;
import storybook.tools.LOG;
import storybook.ui.frames.main.MainFrame;

@SuppressWarnings("serial")
public class IdTCR extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
	   boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
		   null, isSelected, hasFocus, row, column);
		if (value instanceof String) {
			label.setText((String) value);
		} else if (value instanceof Long) try {
			Long id = (Long) value;
			if (id != -1L) {
				MainFrame mainFrame = (MainFrame) table.getClientProperty("MainFrame");
				Scene scene = mainFrame.project.scenes.get(id);
				if (scene != null) {
					label.setText(scene.getTitle());
				}
			}
		} catch (Exception e) {
			LOG.err("SceneIdTCR", e);
		}
		return label;
	}
}
