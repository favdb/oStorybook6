/*
 * Copyright (C) 2021 oStorybook Team
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
package storybook.tools.swing.js;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import storybook.App;
import storybook.Pref;
import storybook.db.abs.AbstractEntity;
import storybook.tools.clip.Clip;
import storybook.tools.html.Html;
import storybook.tools.xml.Xml;
import storybook.ui.MainFrame;

/**
 * The Class JSTable. A JTable able to hide/show columns through a dialog box.
 *
 * @author favdb
 */
@SuppressWarnings("serial")
public class JSTable extends JTable {

	private static final String TT = "JSTable";

	public static boolean isLocationOk(JTable table, int row, int col) {
		return !(row < 0 || row >= table.getRowCount()
		   || col < 0 || col >= table.getColumnCount());
	}

	// The minimum column width.
	private int widthMin = App.getInstance().fonts.defGet().getSize() * 3;

	// The maximum column width.
	private int widthMax = App.getInstance().fonts.defGet().getSize() * 64;

	// The visible columns.
	private final Map<String, Boolean> visibles = new HashMap<>();

	// The sizes of columns.
	private final Map<String, Integer> sizes = new HashMap<>();

	// The minimum sizes of columns.
	private final Map<String, Integer> minimumSizes = new HashMap<>();

	// is this table editable
	private boolean editable = false;

	// The editables columns.
	private final List<String> editables = new ArrayList<>();

	// highlighter color
	private Color highligtherColor = Color.decode("0xEEFFEE");

	/**
	 * Instantiates a new JS table.
	 */
	public JSTable() {
		super();
		init();
	}

	/**
	 * Instantiates a new JS table.
	 *
	 * @param numRows the num rows
	 * @param numColumns the num columns
	 */
	public JSTable(int numRows, int numColumns) {
		super(numRows, numColumns);
		init();
	}

	/**
	 * Instantiates a new JS table.
	 *
	 * @param data the data
	 * @param column the column
	 */
	public JSTable(Object[][] data, String[] column) {
		super(data, column);
		init();
	}

	/**
	 * Instantiates a new JS table.
	 *
	 * @param dataModel the data model
	 */
	public JSTable(TableModel dataModel) {
		super(dataModel);
		init();
	}

	/**
	 * Instantiates a new JS table.
	 *
	 * @param dataModel the data model
	 * @param columnModel the column model
	 */
	public JSTable(TableModel dataModel, TableColumnModel columnModel) {
		super(dataModel, columnModel);
		init();
	}

	/**
	 * Instantiates a new JS table.
	 *
	 * @param dataModel the data model
	 * @param columnModel the column model
	 * @param selectionModel the selection model
	 */
	public JSTable(TableModel dataModel, TableColumnModel columnModel, ListSelectionModel selectionModel) {
		super(dataModel, columnModel, selectionModel);
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

	/**
	 * Gets the min column with.
	 *
	 * @return the min column with
	 */
	public int getWidthMin() {
		return widthMin;
	}

	/**
	 * Sets the min column with.
	 *
	 * @param minColumnWith the new min column with
	 */
	public void setWidthMin(int minColumnWith) {
		this.widthMin = minColumnWith;
		pack();
	}

	/**
	 * Gets the max column with.
	 *
	 * @return the max column with
	 */
	public int getWidthMax() {
		return widthMax;
	}

	/**
	 * Sets the max column with.
	 *
	 * @param maxColumnWith the new max column with
	 */
	public void setWidthMax(int maxColumnWith) {
		this.widthMax = maxColumnWith;
		pack();
	}

	/**
	 * Inits the header click action.
	 *
	 */
	private void init() {
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setShowGrid(false);
		setAutoCreateRowSorter(true);
		setDragEnabled(true);
		setRowHeight(getRowHeight() + 8);
		// Create header listener
		getTableHeader().addMouseListener(new JSMouseAdapter());
	}

	/**
	 * check if the table is editable
	 *
	 * @return true if yes
	 */
	private boolean isEditable() {
		return editable;
	}

	/**
	 * set the table to be editable
	 *
	 * @param b: value to set
	 */
	public void setEditable(boolean b) {
		editable = b;
	}

	/**
	 * check if the cell is editable
	 *
	 * @param row
	 * @param column
	 * @return
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		String name = getColumnName(column);
		//if (editable) {
		return editables.contains(name);
		//}
		//return false;
	}

	/**
	 * mark column to be editable
	 *
	 * @param name
	 */
	public void setColumnEditable(String name) {
		//LOG.trace(TT + ".setColumnEditable(name=" + name + ")");
		if (name == null) {
			return;
		}
		TableColumn column = this.getColumn(name);
		if (column == null) {
			return;
		}
		if (!editables.contains(name)) {
			editables.add(name);
		}
	}

	public Map<String, Boolean> getVisibles() {
		return visibles;
	}

	/**
	 * Compact columns.
	 */
	public void packAll() {
		pack();
	}

	public void pack() {
		for (int column = 0; column < getColumnCount(); column++) {
			int width = widthMin;
			for (int row = 0; row < getRowCount(); row++) {
				TableCellRenderer renderer = getCellRenderer(row, column);
				Component comp = prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width + 1, width);
			}

			TableColumn tableColumn = columnModel.getColumn(column);
			TableCellRenderer renderer = tableColumn.getHeaderRenderer();
			if (renderer == null) {
				renderer = getTableHeader().getDefaultRenderer();
			}
			Component headComp = renderer.getTableCellRendererComponent(this,
			   tableColumn.getHeaderValue(), false, false, -1, column);
			width = Math.max(headComp.getPreferredSize().width + 1 + 24, width);

			if ((widthMax > 0) && (width > widthMax)) {
				width = widthMax;
			}
			columnModel.getColumn(column).setPreferredWidth(width);

			String name = getColumnName(column);
			sizes.put(name, width);
			minimumSizes.put(name, width);
		}
	}

	/**
	 * set the highlighter color
	 *
	 * @param color
	 */
	public void setHighligtherColor(Integer color) {
		highligtherColor = new Color(color);
	}

	/**
	 * dialog for changing the visibles
	 *
	 * @param p
	 */
	public void changeVisibility(Point p) {
		saveColumnSizes();
		// Show dialog
		JSTableDialog dlg = new JSTableDialog(this);
		if (p != null) {
			dlg.setLocation(p);
		}
		dlg.setVisible(true);
		switch (dlg.rc) {
			case 1:// Compact action
				pack();
				break;
			case 2:
				// Visibility action
				updateFromCheckBoxes(dlg.getPanel());
				restoreColumnSizes();
				revalidate();
				repaint();
				break;
			default:
				break;
		}
	}

	/**
	 * save the columns sizes
	 *
	 */
	private void saveColumnSizes() {
		// save column sizes
		for (int i = 0; i < this.getColumnModel().getColumnCount(); i++) {
			TableColumn column = this.getColumnModel().getColumn(i);
			String name = this.getColumnName(i);
			if (column.getWidth() > 0) {
				sizes.put(name, column.getWidth());
			}
			if (column.getMinWidth() > 0) {
				minimumSizes.put(name, column.getMinWidth());
			}
		}
	}

	/**
	 * set the column to be visible
	 *
	 * @param name: String name of the column
	 * @param b: boolean true for visible column
	 */
	public void setColumnVisible(String name, boolean b) {
		//LOG.trace(TT + ".setColumnVisible(name=" + name + ", value=" + (b ? "true" : "false") + ")");
		try {
			TableColumn column = this.getColumn(name);
			if (column == null) {
				return;
			}
			Integer width = sizes.get(name);
			if (width == null || width < 0) {
				width = widthMin;
				sizes.put(name, width);
				minimumSizes.put(name, width);
			}
			if (b) {
				column.setMinWidth(minimumSizes.get(name));
				column.setMaxWidth(Integer.MAX_VALUE);
				column.setWidth(width);
				column.setPreferredWidth(Math.max(32, width));
			} else {
				column.setMinWidth(0);
				column.setMaxWidth(0);
				column.setWidth(0);
				column.setPreferredWidth(0);

			}
			visibles.put(name, b);
		} catch (Exception ex) {
			// ignore exception
		}
	}

	/**
	 * restore the initial columns sizes
	 *
	 */
	public void restoreColumnSizes() {
		//LOG.trace(TT + ".restoreColumnSizes() widthMin=" + widthMin);
		for (int i = 0; i < this.getColumnModel().getColumnCount(); i++) {
			TableColumn column = this.getColumnModel().getColumn(i);
			String name = getColumnName(i);
			Integer iwidth = sizes.get(name);
			if (iwidth != null) {
				int width = iwidth;
				Boolean xxx = visibles.get(name);
				if (i < visibles.size() && xxx != null) {
					if (!xxx) {
						column.setMinWidth(0);
						column.setMaxWidth(0);
						column.setWidth(0);
						column.setPreferredWidth(0);
					} else {
						if (width < 1) {
							width = widthMin;
						}
						column.setMinWidth(Math.min(minimumSizes.get(name), widthMin));
						column.setMaxWidth(Integer.MAX_VALUE);
						column.setWidth(width);
						column.setPreferredWidth(Math.max(32, width));
					}
				}
			}
		}
	}

	/**
	 * Update visibles from check boxes.
	 *
	 * @param panel the panel
	 */
	private void updateFromCheckBoxes(JPanel panel) {
		//LOG.trace(TT + ".updateFromCheckBoxes(panel)");
		visibles.clear();
		for (Component comp : panel.getComponents()) {
			if (comp instanceof JCheckBox) {
				JCheckBox cb = (JCheckBox) comp;
				visibles.put(cb.getText(), cb.isSelected());
			}
		}
	}

	public void selectRow(int row) {
		if (row >= 0 && row < this.getRowCount()) {
			this.setRowSelectionInterval(row, row);
		}
	}

	/**
	 * set selected Cell
	 *
	 * @param row
	 * @param col
	 */
	private void setCellSelected(int row, int col) {
		this.selectRow(row);
		if (col >= 0 && col < this.getColumnModel().getColumnCount()) {
			this.setColumnSelectionInterval(col, col);
		}
	}

	/**
	 * copy the JSTable to the clipboard
	 *
	 * @param mainFrame
	 * @param entity
	 */
	public void copyToClipboard(MainFrame mainFrame, AbstractEntity entity) {
		//LOG.trace(TT + ".copyToClipboard(mainFrame"
		//	+ ", entity=" + LOG.trace(entity) + ")");
		StringBuilder b = new StringBuilder();
		switch (App.preferences.getString(Pref.KEY.EXP_FORMAT)) {
			case "txt":
				b.append(entity.toText());
				break;
			case "csv":
				b.append(entity.toCsv(
				   "",
				   "",
				   App.preferences.getString(Pref.KEY.EXP_CSV_SEPARATOR)));
				break;
			case "html":
				b.append(Html.intoHTML(Html.intoTag("body", entity.toDetail(2))));
				break;
			case "xml":
				b.append(Xml.intoXml(entity.toXml()));
				break;
		}
		Clip.to(b.toString(), App.preferences.getString(Pref.KEY.EXP_FORMAT));
		App.getInstance().enablePaste(true);
		Clip.okMessage(mainFrame, entity.getObjType().toString());
	}

	/**
	 * The internal Class JSMouseAdapter.
	 */
	private class JSMouseAdapter extends MouseAdapter {

		/**
		 * Mouse clicked.
		 *
		 * @param e the event
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			// Only concerned by right clicks
			if (e.getButton() == MouseEvent.BUTTON3) {
				changeVisibility(e.getLocationOnScreen());
				e.consume();
			}
		}
	}

}
