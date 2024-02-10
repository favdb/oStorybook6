/*
 * Copyright (C) 2016 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.exim.exporter;

import i18n.I18N;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import storybook.App;
import storybook.Pref;
import storybook.tools.DateUtil;
import storybook.tools.LOG;
import storybook.ui.MainFrame;
import storybook.ui.SbView;
import storybook.db.abs.AbsTable;

/**
 * Export only the visibles columns from a JSTable
 *
 * allowed formats are html, txt, csv
 *
 * @author favdb
 */
public class ExportSTable extends AbstractExport {

	private static final String TT = "ExportSTable";

	private final JTable xtable;

	/**
	 *
	 * @param mainFrame
	 * @param view
	 */
	public ExportSTable(MainFrame mainFrame, SbView view) {
		super(mainFrame, App.preferences.getString(Pref.KEY.EXP_FORMAT));
		this.xtable = getSTable(view);
	}

	/**
	 * get the JSTable object
	 *
	 * @param view
	 * @return
	 */
	public static JTable getSTable(SbView view) {
		AbsTable comp = (AbsTable) view.getComponent();
		return comp.getTable();
	}

	enum KW {
		ID,
		NAME,
		TITLE,
		DESCRIPTION,
		NOTES,
		ASSISTANT,
		STATUS,
		CATEGORY,
		EXPORT,
		DATABASE,
		XML,
		HTML,
		KEY,
		VALUE,
		STEP,
		POLTI,
		PARAM,
		BACKUP,
		DIRECTORY,
		AUTO,
		INCREMENT,
		EDITOR,
		MODLESS,
		XUSE,
		EXTEND,
		TEMPLATE,
		IMPORT, FILE,
		SCENE_START, SCENE_END,
		LOCATION,
		PERSON,
		ADRESS,
		CITY,
		COUNTRY,
		ALTITUDE,
		NUMBER,
		DATE,
		COLOR,
		SORT;

		@Override
		public String toString() {
			return this.name().toLowerCase().replace("_", ".");
		}
	}

	public static void export(MainFrame m, SbView view) {
		//LOG.trace(TT + ".export(mainFrame, view=" + view.toString() + ")");
		String viewName = I18N.getMsg(view.getName().toLowerCase());
		if (getSTable(view) == null) {
			LOG.err(TT + ".export(...) error view " + viewName + " doesn't contain a JSTable");
			return;
		}
		if (ExportOptionsDlg.show(m)) {
			return;
		}
		ExportSTable export = new ExportSTable(m, view);
		String tname = "table_" + viewName;
		if (export.askFileExists(tname)
		   && JOptionPane.showConfirmDialog(m,
			  I18N.getMsg("export.replace", export.param.getFileName()),
			  I18N.getMsg(KW.EXPORT.toString()),
			  JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
			return;
		}
		if (!export.openFile(tname, false)) {
			return;
		}
		export.writeTable();
		export.closeFile(true);
	}

	public void writeTable() {
		//App.trace(TT+".writeTable() table="+name);
		if (!isOpened) {
			return;
		}
		if (param.isHtml()) {
			writeText("<table border=\"1\" cellspacing=\"0\" cellpadding=\"0\">\n");
		}
		String beginField = param.getTxtSeparator();
		String endField = "";
		if (param.getFormat().equalsIgnoreCase("csv")) {
			beginField = param.getCsvQuote();
			endField = beginField;
		}
		List<ColumnHeader> headers = writeHeaders();
		for (int row = 0; row < xtable.getRowCount(); row++) {
			switch (param.getFormat()) {
				case "html":
					writeText("<tr>\n");
					break;
				case "xml":
					writeText("<"
					   + xtable.getName().toLowerCase().replace("table", "")
					   + ">" + "\n");
					break;
				case "csv":
				case "txt":
					//writeText("\n");
					break;
				default:
					break;
			}
			for (int j = 0; j < headers.size(); j++) {
				int col = headers.get(j).idx;
				Object obj = xtable.getModel().getValueAt(row, col);
				if (obj == null) {
					obj = "";
				}
				String value = obj.toString();
				if (value.startsWith("java.awt.Color")) {
					value = convertColor(value);
				} else if (obj instanceof Boolean) {
					value = (boolean) obj ? I18N.getMsg("yes") : "";
				} else if (obj instanceof Date) {
					value = DateUtil.simpleDateTimeToString((Date) obj, false);
				}
				if (col != -1) {
					switch (param.getFormat()) {
						case "html":
							writeText("<td>" + value + "</td>");
							break;
						case "xml":
							writeText("key, value\n");
							break;
						case "csv":
							writeText(beginField + value + endField
							   + (j < headers.size() - 1 ? param.getCsvComma() : ""));
							break;
						case "txt":
							writeText(value + (j < headers.size() - 1 ? param.getTxtSeparator() : ""));
							break;
						default:
							break;
					}
				}
			}
			switch (param.getFormat()) {
				case "html":
					writeText("</tr>\n");
					break;
				case "xml":
					writeText("</"
					   + xtable.getName().toLowerCase().replace("table", "")
					   + ">" + "\n");
					break;
				case "csv":
				case "txt":
					writeText("\n");
					break;
				default:
					break;
			}
		}
		if (param.isHtml()) {
			writeText("</table>\n");
		}
	}

	private String convertColor(String value) {
		String r = value.replace("java.awt.Color", "");
		r = r.replace("[", "");
		r = r.replace("]", "");
		String[] c = r.split(",");
		return "(" + c[0].substring(2) + "," + c[1].substring(2) + "," + c[2].substring(2) + ")";
	}

	private List<ColumnHeader> writeHeaders() {
		List<ColumnHeader> headers = new ArrayList<>();
		for (int i = 0; i < xtable.getColumnCount(); i++) {
			TableColumn column = xtable.getColumnModel().getColumn(i);
			String l1 = (String) column.getHeaderValue();
			int modelIdx = column.getModelIndex();
			if (modelIdx == -1) {
				continue;
			}
			int col = xtable.convertColumnIndexToView(modelIdx);
			if (col == -1) {
				continue;
			}
			if (column.getPreferredWidth() > 0) {
				headers.add(new ColumnHeader(l1, modelIdx));
			}
		}
		if (!headers.isEmpty()) {
			if (param.isHtml()) {
				writeText("<tr>\n");
			}
			for (int j = 0; j < headers.size(); j++) {
				ColumnHeader header = headers.get(j);
				switch (param.getFormat()) {
					case "xml":
						break;
					case "html":
						writeText("<td>" + header.getName() + "</td>\n");
						break;
					case "csv":
						writeText(param.getCsvQuote() + header.getName() + param.getCsvQuote()
						   + param.getCsvComma() + (j < headers.size() - 1 ? param.getTxtSeparator() : ""));
						break;
					case "txt":
						writeText(header.getName() + (j < headers.size() - 1 ? param.getTxtSeparator() : ""));
						break;
					default:
						break;
				}
			}
			switch (param.getFormat()) {
				case "html":
					writeText("</tr>\n");
					break;
				case "xml":
					break;
				case "csv":
				case "txt":
					writeText("\n");
					break;
				default:
					break;
			}
		}
		return headers;
	}

	public class ColumnHeader {

		private final String name;
		private final int idx;

		ColumnHeader(String n, int s) {
			name = n;
			idx = s;
		}

		public String getName() {
			return (name);
		}

		public int getSize() {
			return (idx);
		}

	}
}
