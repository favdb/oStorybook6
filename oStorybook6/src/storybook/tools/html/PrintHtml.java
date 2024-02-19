/*
 * Copyright (C) 2024 favdb
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
package storybook.tools.html;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import storybook.tools.LOG;

public class PrintHtml {

	public static void pr(String html) {
		LOG.trace("PrintHtml.pr(html len=" + html.length() + ")");
		JEditorPane editorPane = new JEditorPane("text/html", html);
		JFrame frm = new JFrame();
		frm.setUndecorated(true);
		frm.setSize(new Dimension(1, 1));
		frm.add(editorPane);
		//frm.pack();
		frm.setLocationRelativeTo(null);
		frm.setVisible(true);
		class PrintableEditorPane implements Printable {

			private final JEditorPane editorPane;

			public PrintableEditorPane(JEditorPane editorPane) {
				this.editorPane = editorPane;
			}

			@Override
			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
				if (pageIndex > 0) {
					return NO_SUCH_PAGE;
				}
				editorPane.setSize((int) pageFormat.getImageableWidth(), 1);
				graphics.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
				editorPane.print(graphics);
				return PAGE_EXISTS;
			}
		}
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPrintable(new PrintableEditorPane(editorPane));
		try {
			if (job.printDialog()) {
				job.print();
			}
		} catch (PrinterException e) {
			e.printStackTrace(System.err);
		}
		frm.dispose();
	}
}
