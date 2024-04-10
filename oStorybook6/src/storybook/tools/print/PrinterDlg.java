/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
