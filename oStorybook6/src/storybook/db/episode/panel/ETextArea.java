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

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import storybook.tools.swing.js.JSTable;

/**
 *
 * @author favdb
 */
public class ETextArea extends JTextArea implements KeyListener {

	int row, col;
	private final MultiLineCellEditor cellEditor;
	private final JTable table;

	public ETextArea(MultiLineCellEditor cellEditor) {
		JPanel p = new JPanel();
		this.setBackground(p.getBackground());
		this.setForeground(p.getForeground());
		this.cellEditor = cellEditor;
		table = cellEditor.getEpisodePanel().getEpisodeTable();
		row = table.getSelectedRow();
		col = table.getSelectedColumn();
		this.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		this.setBackground(cellEditor.getEpisodePanel().getEpisodeTable().getBackground());
		this.addKeyListener(this);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_TAB && e.isShiftDown()) {
			col--;
			if (col < 0) {
				row--;
				col = table.getColumnCount() - 1;
			}
			if (row < 0) {
				row = table.getRowCount() - 1;
			}
			settingNextCell(row, col);
			e.consume();
		} else if (e.getKeyCode() == KeyEvent.VK_TAB) {
			col++;
			if (col >= table.getColumnCount()) {
				col = 0;
				row++;
			}
			if (row >= table.getRowCount()) {
				cellEditor.getEpisodePanel().episodeCreate();
			}
			settingNextCell(row, col);
			e.consume();
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
			String t = getText();
			int i = this.getCaretPosition();
			String tx;
			if (i < t.length()) {
				tx = t.substring(0, i) + "\n" + t.substring(i);
			} else {
				tx = t + "\n ";
				i++;
			}
			setText(tx);
			setCaretPosition(i);
			e.consume();
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			row++;
			if (row < table.getRowCount()) {
				settingNextCell(row, col);
			} else {
				cellEditor.getEpisodePanel().episodeCreate();
				settingNextCell(row, col);
			}
			e.consume();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// empty
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// empty
	}

	private void settingNextCell(int row, int col) {
		SwingUtilities.invokeLater(() -> {
			scrollTableToCell(row, col);
			table.editCellAt(row, col);
		});
	}

	public void scrollTableToCell(int row, int col) {
		if (!JSTable.isLocationOk(table, row, col)) {
			return;
		}
		table.setRowSelectionInterval(row, row);
		table.setColumnSelectionInterval(col, col);
		if (!(table.getParent() instanceof JViewport)) {
			return;
		}
		JViewport viewport = (JViewport) table.getParent();
		Rectangle rect = table.getCellRect(row, col, true);
		Point pt = viewport.getViewPosition();
		rect.setLocation(rect.x - pt.x, rect.y - pt.y);
		viewport.scrollRectToVisible(rect);
	}

}
