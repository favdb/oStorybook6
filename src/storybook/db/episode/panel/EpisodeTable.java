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

import i18n.I18N;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.strand.Strand;
import storybook.tools.LOG;

/**
 * table for episodes manage columns for number, title, link, plot
 *
 * @author favdb
 */
public class EpisodeTable extends JTable {

	private static final String TT = "EpisodeTable";
	private DefaultTableModel model;
	private final EpisodePanel panel;

	public EpisodeTable(EpisodePanel panel) {
		super();
		this.panel = panel;
		init();
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		//LOG.trace(TT + ".prepareRenderer(rendere=" + (renderer == null ? "null" : "ok" + ", row=" + row + ", column=" + column + ")"));
		Component comp = super.prepareRenderer(renderer, row, column);
		if (!comp.getBackground().equals(getSelectionBackground())) {
			if (row % 2 == 0) {
				comp.setBackground(UIManager.getColor("Table.oddBackground"));
				comp.setForeground(UIManager.getColor("Table.oddForeground"));
			} else {
				comp.setBackground(UIManager.getColor("Table.background"));
				comp.setForeground(UIManager.getColor("Table.foreground"));
			}
		}
		return comp;
	}

	private void init() {
		this.setName(TT);
		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.getTableHeader().setReorderingAllowed(false);
		this.setRowHeight(panel.getRowHeight());
		this.setShowGrid(true);
		this.setIntercellSpacing(new Dimension(2, 2));
		this.setCellSelectionEnabled(true);
		this.setRowSelectionAllowed(true);
		this.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		initModel();
		initRenderer();
	}

	private void initModel() {
		model = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int cols) {
				return !(cols == 0 || cols == 2);
			}
		};
		model.addColumn(I18N.getMsg(EpisodePanel.NCOL.NUMBER.getName()));
		model.addColumn(I18N.getMsg(EpisodePanel.NCOL.NAME.getName()));
		model.addColumn(I18N.getMsg(EpisodePanel.NCOL.LINKS.getName()));
		model.addColumn(I18N.getMsg(EpisodePanel.NCOL.PLOT.getName()));
		for (Strand strand : panel.getStrands()) {
			model.addColumn(strand.getName());
		}
		this.setModel(model);
		this.getColumnModel().getColumn(EpisodePanel.NCOL.NUMBER.ordinal()).setPreferredWidth(panel.getCharW() * 3);//number
		this.getColumnModel().getColumn(EpisodePanel.NCOL.NAME.ordinal()).setPreferredWidth(panel.getCharW() * 24);//title
		this.getColumnModel().getColumn(EpisodePanel.NCOL.LINKS.ordinal()).setPreferredWidth(IconUtil.getDefSize() * 4);//scene
		for (int col = EpisodePanel.NCOL.PLOT.ordinal(); col < this.getColumnCount(); col++) {
			this.getColumnModel().getColumn(col).setPreferredWidth(panel.getCharW() * 32);
		}
		((DefaultTableCellRenderer) this.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(0);
	}

	/**
	 * initialize the renderer
	 *
	 */
	public void initRenderer() {
		//LOG.trace(TT + ".tableInitRenderer()");
		MultiLineCellRenderer multiRenderer = new MultiLineCellRenderer();
		MultiLineCellEditor multiEditor = new MultiLineCellEditor(panel);
		EpisodeCellRenderer defRenderer = new EpisodeCellRenderer(panel.isTitle());
		this.getColumnModel().getColumn(
				EpisodePanel.NCOL.NUMBER.ordinal()).setCellRenderer(defRenderer);//number
		this.getColumnModel().getColumn(
				EpisodePanel.NCOL.NAME.ordinal()).setCellEditor(multiEditor);//title
		this.getColumnModel().getColumn(
				EpisodePanel.NCOL.NAME.ordinal()).setCellRenderer(multiRenderer);//title
		this.getColumnModel().getColumn(
				EpisodePanel.NCOL.LINKS.ordinal()).setCellRenderer(defRenderer);//scene
		for (int col = EpisodePanel.NCOL.PLOT.ordinal(); col < this.getColumnCount(); col++) {
			this.getColumnModel().getColumn(col).setCellEditor(multiEditor);//title
			this.getColumnModel().getColumn(col).setCellRenderer(multiRenderer);//title
		}
		this.setDefaultEditor(Integer.class, null);
		this.setDefaultEditor(AbstractEntity.class, null);
	}

	/**
	 * select rows
	 *
	 * @param rows
	 */
	public void selectRows(int[] rows) {
		this.getSelectionModel().clearSelection();
		for (int row : rows) {
			this.getSelectionModel().addSelectionInterval(row, row);
		}
	}

	/**
	 * add a row to selection
	 *
	 * @param row
	 */
	public void selectAddRow(int row) {
		//LOG.trace(TT + ".selectAddRow(row=" + row + ")");
		int s = this.getSelectedRows()[0];
		this.addRowSelectionInterval(s, row);
		this.setColumnSelectionInterval(0, this.getColumnCount() - 1);
	}

	/**
	 * select a row
	 *
	 * @param row
	 */
	public void selectRow(int row) {
		//LOG.trace(TT + ".selectRow(row=" + row + ")");
		this.getSelectionModel().clearSelection();
		this.setRowSelectionInterval(row, row);
		this.setColumnSelectionInterval(0, this.getColumnCount() - 1);
	}

	/**
	 * select a row
	 *
	 * @param row
	 */
	public void selectAllRow(int row) {
		//LOG.trace(TT + ".selectRow(row=" + row + ")");
		this.getSelectionModel().clearSelection();
		this.setRowSelectionInterval(row, row);
		this.setColumnSelectionInterval(0, this.getColumnCount() - 1);
	}

	/**
	 * select a cell at a specified row and column
	 *
	 * @param row
	 * @param col
	 */
	public void selectCell(int row, int col) {
		//LOG.trace(TT + ".selectRow(row=" + row + ")");
		this.getSelectionModel().clearSelection();
		this.setRowSelectionInterval(row, row);
		this.setColumnSelectionInterval(col, col);
		Rectangle rect = this.getCellRect(row, col, true);
		this.scrollRectToVisible(rect);
	}

	/**
	 * add an empty row
	 */
	public void addRow() {
		List<Object> objs = new ArrayList<>();
		int row = model.getRowCount() + 1;
		objs.add(row);//number
		objs.add("");//title
		objs.add(IconUtil.getIconSmall(ICONS.K.UNKNOWN));//scene
		objs.add("");//plot
		panel.getStrands().forEach(action -> objs.add(""));
		model.addRow(objs.toArray());
	}

	/**
	 * get the row at the mouse pointed to
	 *
	 * @param e: mouse event where to get the the point
	 * @return : the row number
	 */
	public int pointToRow(MouseEvent e) {
		return ((JTable) e.getSource()).rowAtPoint(e.getPoint());
	}

	/**
	 * get the column at the mouse pointed to
	 *
	 * @param e: mouse event where to get the point
	 * @return : the column number
	 */
	public int pointToCol(MouseEvent e) {
		return ((JTable) e.getSource()).columnAtPoint(e.getPoint());
	}

	/**
	 * move a row after an other one
	 */
	private void moveRowAfter(int row, int dest) {
		if (dest < this.getRowCount()) {
			model.moveRow(row, row, dest);
		}
	}

	/**
	 * move selected row(s) after dest
	 *
	 * @param dest
	 */
	public void moveAfter(int dest) {
		// there is only one selected row
		if (this.getSelectedRows().length == 1) {
			//this is a single row selection
			int origin = this.getSelectedRows()[0];
			int newdest = (origin < dest ? dest : dest + 1);
			moveRowAfter(origin, newdest);
			selectRow(newdest);
			panel.setModified();
			return;
		}
		int[] rows = this.getSelectedRows();
		if (isArrayContains(rows, dest)) {
			LOG.trace(TT + ".moveAfter(dest=" + dest + ") the selected rows contains destination. Abort");
			return;
		}
		if (rows[rows.length - 1] >= (this.getRowCount() - 1)) {
			LOG.trace(TT + ".moveAfter(dest=" + dest + ") the last selected row is the last row. Abort");
			return;
		}
		// selected rows are contigus
		int start = rows[0], end = rows[rows.length - 1], cible = dest + 1, count = end - start;
		if (dest > start) {
			cible = Math.max(start, dest - count);
		}
		model.moveRow(start, end, cible);
		int n = dest - count;
		if (dest < start) {
			n = dest + 1;
		}
		for (int row = 0; row < rows.length; row++) {
			rows[row] = n++;
		}
		selectRows(rows);
		panel.setModified();
	}

	/**
	 * move rows before
	 *
	 * @param dest
	 */
	public void moveBefore(int dest) {
		//LOG.trace(TT + ".moveBefore(dest=" + dest + ")");
		int[] rows = this.getSelectedRows();
		//if there is only one selected row
		if (rows.length < 2 && dest > 0) {
			int origin = rows[0];
			model.moveRow(origin, origin, dest);
			selectRow((origin < dest ? dest - 1 : dest));
			return;
		}
		for (int row1 : rows) {
			if (row1 == 0) {
				return;
			}
		}
		// there is more than one selected row
		int[] selectedRows = rows.clone();
		for (int i = 0; i < rows.length; i++) {
			model.moveRow(rows[i], rows[i], (dest < 1 ? 0 : dest - 1));
			selectedRows[i] = rows[i] - 1;
		}
		selectRows(selectedRows);
		panel.setModified();
	}

	/**
	 * remove a row
	 *
	 * @param row
	 */
	public void removeRow(int row) {
		model.removeRow(row);
	}

	/**
	 * get the cell object
	 *
	 * @param tb
	 * @param row
	 * @return
	 */
	public List<Object> getRow(JTable tb, int row) {
		List<Object> objs = new ArrayList<>();
		for (int col = 0; col < tb.getColumnCount(); col++) {
			objs.add(tb.getValueAt(row, col));
		}
		return objs;
	}

	/**
	 * check if the rows array contains the row
	 *
	 * @param values: the rows array
	 * @param val: the row to check for
	 * @return true if the array contains the row
	 */
	public boolean isArrayContains(int[] values, int val) {
		for (int v : values) {
			if (v == val) {
				return true;
			}
		}
		return false;
	}

}
