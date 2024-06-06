/*
 * Copyright (C) 2024 FaVdB
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
 * You should have received a fileCopy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.tools.print;

import api.mig.swing.MigLayout;
import i18n.I18N;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 *
 * @author favdb
 */
public class PrinterDlg extends JDialog {

	/*I18N values
printer=Pinter
printer.select=Select the printer
printer.to_file=Print to a file
printer.pages_all=All
printer.pages_from=From page
printer.pages.to=To
printer.paper=Paper format
printer.orientation_portrait=Portrait
printer.orientation_paysage=Paysage
printer.margins=Margins
printer.margins_top=Top
printer.margins_left=Left
printer.margins_right=Right
printer.margins_top=Top
	 */
	public PrinterDlg(JFrame caller) {
		super(caller);
		initialize();
	}

	private void initialize() {
		setLayout(new MigLayout());
		setTitle(I18N.getMsg("print"));
		//cb to select the printer
		//ck to select to print in a file
		//print all pages or selected ones
		//no copies
		//paging
		//paper format (default is A4)
		//orientation portait or paysage
		//margins left, right, top, bottom
		//buttons OK and Cancel
		pack();
		this.setLocationRelativeTo(getParent());
	}
}
