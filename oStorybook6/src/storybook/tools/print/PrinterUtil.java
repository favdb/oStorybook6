/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package storybook.tools.print;

import i18n.I18N;
import java.awt.Component;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.MessageFormat;
import java.util.List;
import javax.print.PrintService;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.text.html.HTMLEditorKit;
import storybook.tools.LOG;

public class PrinterUtil {

	/**
	 * printHtml a foul HTML text
	 *
	 * @param component
	 * @param html the HTML String to printHtml (must be complete)
	 * @param header
	 * @param footer
	 */
	public static void printHtml(Component component, String html, String header, String footer) {
		HTMLEditorKit kit = new HTMLEditorKit();
		JEditorPane ed = new JEditorPane();
		ed.setEditorKit(kit);
		ed.setContentType("text/html");
		ed.setText(html);
		try {
			ed.print(new MessageFormat(header),
			   new MessageFormat(footer + "{0}"),
			   true, null, null, true);
			JOptionPane.showMessageDialog(component,
			   I18N.getMsg("print.ok"),
			   I18N.getMsg("print"),
			   JOptionPane.INFORMATION_MESSAGE);
		} catch (PrinterException ex) {
			LOG.err("Printing error ", ex);
			JOptionPane.showMessageDialog(component,
			   I18N.getMsg("print.error", ex.getLocalizedMessage()),
			   I18N.getMsg("print"),
			   JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * printHtml HTML text list of pages
	 *
	 * @param component
	 * @param ls
	 * @param header
	 * @param footer
	 * @param startnum : pages prior this number are printed without header and footer
	 */
	public static void printHtml(Component component,
	   List<String> ls, String header, String footer, int startnum) {
		HTMLEditorKit kit = new HTMLEditorKit();
		try {
			PrinterJob printJob = PrinterJob.getPrinterJob();
			if (printJob.printDialog()) {
				PrintService ps = printJob.getPrintService();
				JEditorPane ed = new JEditorPane();
				printJob.setPrintable(new ComponentPrinter.ComponentPrintable(ed, true));
				ed.setEditorKit(kit);
				ed.setContentType("text/html");
				for (int n = 0; n < ls.size(); n++) {
					ed.setText(ls.get(n));
					if (n < startnum) {
						ed.print(new MessageFormat(""),
						   new MessageFormat(""),
						   false, ps, null, true);
					} else {
						ed.print(new MessageFormat(header),
						   new MessageFormat(footer + " " + (n - startnum + 1) + ".{0}"),
						   false, ps, null, true);
					}
				}
				JOptionPane.showMessageDialog(component,
				   I18N.getMsg("print.ok"),
				   I18N.getMsg("print"),
				   JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (PrinterException ex) {
			LOG.err("Printing error ", ex);
			JOptionPane.showMessageDialog(component,
			   I18N.getMsg("print.error", ex.getLocalizedMessage()),
			   I18N.getMsg("print"),
			   JOptionPane.ERROR_MESSAGE);
		}
	}

}
