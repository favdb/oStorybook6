/*
 * Copyright (C) 2022 favdb
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
package storybook.db.episode.panel;

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author favdb
 */
public class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {

	public MultiLineCellRenderer() {
		setEditable(false);
		setLineWrap(true);
		setWrapStyleWord(true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof String) {
			setText((String) value);
			setCaretPosition(0);
		} else {
			setText("");
		}
		if (isSelected) {
			setBorder(BorderFactory.createLineBorder(UIManager.getColor("Table.oddForeground")));
		} else {
			setBorder(BorderFactory.createEmptyBorder());
		}
		return this;
	}
}
