/*
 * Copyright (C) 2017 favdb
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
package storybook.exim.exporter.options;

import api.mig.swing.MigLayout;
import i18n.I18N;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import storybook.db.book.BookParamExport;
import storybook.ui.MIG;

/**
 *
 * @author favdb
 */
public class CSVpanel extends JPanel {

	private JRadioButton csvComma;
	private JRadioButton csvDoubleQuotes;
	private JRadioButton csvSemicolon;
	private JRadioButton csvSingleQuotes;
	private final BookParamExport param;

	public CSVpanel(BookParamExport param) {
		this.param = param;
		this.setBorder(BorderFactory.createTitledBorder("CSV"));
		ButtonGroup group1 = new ButtonGroup();
		ButtonGroup group2 = new ButtonGroup();
		csvSingleQuotes = new JRadioButton(I18N.getMsg("export.options.csv.quoted.single"));
		csvDoubleQuotes = new JRadioButton(I18N.getMsg("export.options.csv.quoted.double"));
		//csvNoQuotes = new JRadioButton(I18N.getMsg("none"));
		if (null != param.getCsvQuote()) {
			switch (param.getCsvQuote()) {
				case "'":
					csvSingleQuotes.setSelected(true);
					break;
				case "\"":
					csvDoubleQuotes.setSelected(true);
					break;
				default:
					csvSingleQuotes.setSelected(true);
					break;
			}
		}
		group1.add(csvSingleQuotes);
		group1.add(csvDoubleQuotes);

		csvComma = new JRadioButton(I18N.getMsg("export.options.csv.separate.comma"));
		csvSemicolon = new JRadioButton(I18N.getMsg("export.options.csv.separate.semicolon"));
		if (";".equals(param.getCsvComma())) {
			csvSemicolon.setSelected(true);
		} else {
			csvComma.setSelected(true);
		}
		group2.add(csvComma);
		group2.add(csvSemicolon);

		//layout
		setLayout(new MigLayout(MIG.FILL, "[][]"));
		add(new JLabel(I18N.getColonMsg("export.options.csv.quoted")), MIG.SPAN);
		add(csvSingleQuotes);
		add(csvDoubleQuotes, MIG.WRAP);
		add(new JLabel(I18N.getColonMsg("export.options.csv.separate")), MIG.SPAN);
		add(csvComma);
		add(csvSemicolon);
		csvSingleQuotes.setSelected(param.getCsvQuote().equals("'"));
		csvDoubleQuotes.setSelected(param.getCsvQuote().equals("\""));
		csvComma.setSelected(param.getCsvComma().equals(","));
		csvSemicolon.setSelected(param.getCsvQuote().equals(";"));
	}

	public void apply() {
		param.setCsvQuote(csvSingleQuotes.isSelected() ? "'" : "\"");
		param.setCsvComma(csvComma.isSelected() ? "," : ";");
	}

}
