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
package storybook.tools.print;

import i18n.I18N;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.JOptionPane;
import storybook.tools.LOG;
import storybook.ui.MainFrame;

/**
 * printer for a Component
 *
 * inspired from https://stackoverflow.com/questions/340981/send-a-jpanel-to-the-printer
 *
 * @author favdb
 */
public class ComponentPrinter {

	private static final String TT = "ComponentPrinter.";

	private final Component component;
	private final MainFrame mainFrame;
	//private final String header, footer;

	public ComponentPrinter(MainFrame mainFrame, Component com, String header, String footer) {
		this.mainFrame = mainFrame;
		//this.header = header;
		//this.footer = footer;
		component = com;
	}

	public void print() {
		try {
			PrinterJob pj = PrinterJob.getPrinterJob();
			pj.setPrintable(new ComponentPrintable(component, false));
			if (pj.printDialog()) {
				pj.print();
				JOptionPane.showMessageDialog(mainFrame,
						I18N.getMsg("print.ok"),
						I18N.getMsg("print"),
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		} catch (PrinterException ex) {
			LOG.err("Printing error", ex);
			JOptionPane.showMessageDialog(mainFrame,
					I18N.getMsg("print.error", ex.getMessage()),
					I18N.getMsg("print"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		JOptionPane.showMessageDialog(mainFrame,
				I18N.getMsg("print.canceled"),
				I18N.getMsg("print"),
				JOptionPane.INFORMATION_MESSAGE);
	}

	static public void pr(MainFrame mainFrame, Component comp, String header, String footer) {
		ComponentPrinter p = new ComponentPrinter(mainFrame, comp, header, footer);
		p.print();
	}

	public static class ComponentPrintable implements Printable {

		private final Component comp;

		public ComponentPrintable(Component comp, boolean fill) {
			this.comp = comp;
		}

		@Override
		public int print(Graphics g, PageFormat format, int page_index) throws PrinterException {
			if (page_index > 0) {
				return Printable.NO_SUCH_PAGE;
			}
			Dimension dim = comp.getSize();
			double cHeight = dim.getHeight(),
					cWidth = dim.getWidth(),
					pHeight = format.getImageableHeight(),
					pWidth = format.getImageableWidth(),
					pXStart = format.getImageableX(),
					pYStart = format.getImageableY(),
					xRatio = pWidth / cWidth,
					yRatio = pHeight / cHeight,
					ratio = Math.min(xRatio, yRatio);
			Graphics2D g2 = (Graphics2D) g;
			g2.translate(pXStart, pYStart);
			g2.scale(ratio, ratio);
			comp.printAll(g2);
			return Printable.PAGE_EXISTS;
		}
	}
}
