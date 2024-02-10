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
package storybook.db.status;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import storybook.App;
import storybook.db.scene.SceneStatus;

@SuppressWarnings("serial")
public class StatusTCR extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel lbStatus = (JLabel) super.getTableCellRendererComponent(table,
			value, isSelected, hasFocus, row, column);
		int st;
		if (value instanceof SceneStatus) {
			st = ((SceneStatus) value).getNumber();
		} else if (value instanceof Integer) {
			st = (Integer) value;
		} else {
			return lbStatus;
		}
		JLabel lb = new JLabel(Status.getStatusIcon(st));
		lb.setFont(App.getInstance().fonts.defGet());
		lb.setBackground(lbStatus.getBackground());
		lb.setOpaque(true);
		lb.setToolTipText(Status.getStatusMsg(st));
		return (lb);
	}
}
