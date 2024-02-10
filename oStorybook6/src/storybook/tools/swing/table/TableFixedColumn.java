/*
Storybook: Scene-based software for novelists and authors.
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
package storybook.tools.swing.table;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class TableFixedColumn extends JScrollPane implements RowSorterListener {

	private JTable scrollableTable;
	private JTable fixedTable;

	@SuppressWarnings("LeakingThisInConstructor")
	public TableFixedColumn(JTable table, int fixedColumns) {
		super(table);
		scrollableTable = table;
		scrollableTable.setName("scrollableTable");
		fixedTable = new JTable(scrollableTable.getModel());
		fixedTable.setName("fixedTable");
		fixedTable.setRowHeight(table.getRowHeight());
		fixedTable.setFocusable(false);
		fixedTable.setSelectionModel(scrollableTable.getSelectionModel());
		fixedTable.getTableHeader().setReorderingAllowed(false);
		if (fixedTable.getRowSorter() != null) {
			fixedTable.getRowSorter().addRowSorterListener(this);
		}
		fixedTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		fixedTable.setAutoCreateRowSorter(true);
		setCellRenderer(table, fixedTable);
		scrollableTable.setAutoCreateRowSorter(true);
		scrollableTable.getRowSorter().addRowSorterListener(this);
		scrollableTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		// Remove the fixed columns from the main table
		for (int i = 0; i < fixedColumns; i++) {
			TableColumnModel columnModel = scrollableTable.getColumnModel();
			columnModel.removeColumn(columnModel.getColumn(0));
		}
		// Remove the non-fixed columns from the fixed table
		while (fixedTable.getColumnCount() > fixedColumns) {
			TableColumnModel columnModel = fixedTable.getColumnModel();
			columnModel.removeColumn(columnModel.getColumn(fixedColumns));
		}
		// set header table cell renderer
		for (int c = 0; c < fixedTable.getColumnModel().getColumnCount(); ++c) {
			TableColumn column = fixedTable.getColumnModel().getColumn(c);
			column.setCellRenderer(new TableHeaderCellRenderer());
		}
		// Add the fixed table to the scroll pane
		fixedTable.setPreferredScrollableViewportSize(fixedTable.getPreferredSize());
		setRowHeaderView(fixedTable);
		setCorner(JScrollPane.UPPER_LEFT_CORNER, fixedTable.getTableHeader());
	}

	public JTable getFixedTable() {
		return fixedTable;
	}

	public JTable getScrollableTable() {
		return scrollableTable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void sorterChanged(RowSorterEvent e) {
		RowSorter<TableModel> rowSorter = (RowSorter<TableModel>) e.getSource();
		if (e.getSource() == scrollableTable.getRowSorter()) {
			fixedTable.setRowSorter(rowSorter);
		} else if (e.getSource() == fixedTable.getRowSorter()) {
			scrollableTable.setRowSorter(rowSorter);
		}
	}

	private void setCellRenderer(JTable srceTable, JTable destTable) {
		for (int col = 0; col < srceTable.getColumnCount(); col++) {
			TableColumn stcol = srceTable.getColumnModel().getColumn(col);
			TableColumn dtcol = destTable.getColumnModel().getColumn(col);
			dtcol.setCellRenderer(stcol.getCellRenderer());
			dtcol.setCellEditor(srceTable.getCellEditor());
			dtcol.setPreferredWidth(stcol.getPreferredWidth());
		}
	}

}
