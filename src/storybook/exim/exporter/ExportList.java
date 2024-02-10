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
package storybook.exim.exporter;

import i18n.I18N;
import java.util.List;
import javax.swing.JOptionPane;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.tools.LOG;
import storybook.ui.MainFrame;

/**
 * export list of entities
 *
 * @author favdb
 */
public class ExportList extends AbstractExport {

	private static final String TT = "ExportList";
	private String startLine;
	private String fieldStart;
	private String fieldEnd;
	private String endLine;
	private String fieldSep;

	public ExportList(MainFrame m, String type) {
		super(m, type);
	}

	public static void doExec(MainFrame m, String type, String name) {
		if (type.equalsIgnoreCase(F_XML) || type.equalsIgnoreCase(F_ZXML)) {
			LOG.err(TT + " XML format not allowed for list export");
			JOptionPane.showMessageDialog(m,
			   I18N.getMsg("export.not_allowed"),
			   I18N.getMsg(ExportTable.KW.EXPORT.toString()),
			   JOptionPane.OK_OPTION);
			return;
		}
		ExportList export = new ExportList(m, type);
		if (export.askFileExists(name)
		   && JOptionPane.showConfirmDialog(m,
			  I18N.getMsg("export.replace", export.param.getFileName()),
			  I18N.getMsg(ExportTable.KW.EXPORT.toString()),
			  JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
			return;
		}
		if (!export.openFile(name, true)) {
			return;
		}
		export.writeList();
		export.closeFile(true);
	}

	private void writeList() {
		if (!isOpened) {
			return;
		}
		switch (param.getFormat()) {
			case F_HTML:
				writeText("<table border=\"1\" cellspacing=\"0\" cellpadding=\"0\">\n");
				startLine = "<tr>";
				fieldStart = "<td>";
				fieldEnd = "</td>";
				fieldSep = "";
				endLine = "</tr>\n";
				break;
			case F_TXT:
				startLine = "";
				fieldStart = "";
				fieldEnd = "";
				if (param.getTxtTab()) {
					fieldSep = "\t";
				} else {
					fieldSep = param.getTxtSeparator();
				}
				endLine = "\n";
				break;
			case F_CSV:
				startLine = "";
				fieldStart = param.getCsvQuote();
				fieldEnd = fieldStart;
				fieldSep = param.getCsvComma();
				endLine = "\n";
				break;
		}
		String fmt = "%s%s%s";
		writeText(startLine);
		writeText(String.format(fmt, fieldStart, "ID", fieldEnd) + (fieldSep));
		writeText(String.format(fmt, fieldStart, "Name", fieldEnd) + (fieldSep));
		writeText(String.format(fmt, fieldStart, "Abbr", fieldEnd));
		writeText(endLine);
		@SuppressWarnings("unchecked")
		List<AbstractEntity> entities = (List) mainFrame.project.getList(Book.getTYPE(name.replace("list_", "")));
		LOG.trace("name='" + name + "->" + Book.getTYPE(name.replace("list_", "")));
		for (AbstractEntity entity : entities) {
			writeText(startLine);
			writeText(String.format(fmt, fieldStart, entity.getId().toString(), fieldEnd) + (fieldSep));
			writeText(String.format(fmt, fieldStart, entity.getName(), fieldEnd) + (fieldSep));
			writeText(String.format(fmt, fieldStart, entity.getAbbr(), fieldEnd));
			writeText(endLine);
		}
		if (param.getFormat().equals(F_HTML)) {
			writeText("</table>\n");
		}
	}
}
