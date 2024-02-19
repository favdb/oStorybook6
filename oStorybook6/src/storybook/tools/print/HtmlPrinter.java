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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.text.View;
import storybook.tools.LOG;

/**
 * from https://www.tek-tips.com/faqs.cfm?fid=5935
 *
 * Inspired by DocumentRenderer which prints objects of type Document.
 *
 * @author favdb
 */
public class HtmlPrinter implements Printable {

	private static final String TT = "HtmlPrinter.";

	protected int currentPage = -1;
	protected JEditorPane jeditorPane;
	protected double pEndY = 0, pStartY = 0;
	protected PageFormat pFormat;
	protected PrinterJob pJob;
	private final Component parent;
	private final String header, footer;

	public static void pr(String msg, Component caller, String html, String header, String footer) {
		/*LOG.trace(TT + "pr(msg=" + msg + ",m"
		   + ", html len=" + html.length()
		   + ", header='" + header
		   + "', footer='" + footer + "')");*/
		String xheader = header.isEmpty() ? msg : header + " (" + msg + ")";
		String xfooter = footer.isEmpty() ? "Page {0}" : footer;
		HtmlPrinter dr = new HtmlPrinter(caller, html, xheader, xfooter);
		dr.printDialog();
	}

	/**
	 * The constructor initializes the pFormat and PJob variables.
	 *
	 * @param parent
	 * @param html the HTML source code
	 * @param header
	 * @param footer
	 */
	public HtmlPrinter(Component parent, String html, String header, String footer) {
		this.parent = parent;
		this.header = header;
		this.footer = footer;
		jeditorPane = new JEditorPane("text/html", html);
		pFormat = new PageFormat();
		pJob = PrinterJob.getPrinterJob();
	}

	/**
	 * The print method implements the Printable interface.
	 *
	 * @param pf
	 */
	@Override
	public int print(Graphics graphics, PageFormat pf, int pageIndex) {
		double scale = 1.0;
		Graphics2D graphics2D;
		View rootView;
		graphics2D = (Graphics2D) graphics;
		jeditorPane.setSize((int) pf.getImageableWidth(), Integer.MAX_VALUE);
		jeditorPane.validate();
		rootView = jeditorPane.getUI().getRootView(jeditorPane);
		if (jeditorPane.getMinimumSize().getWidth() > pf.getImageableWidth()) {
			scale = pf.getImageableWidth() / jeditorPane.getMinimumSize().getWidth();
			graphics2D.scale(scale, scale);
		}
		graphics2D.setClip((int) (pf.getImageableX() / scale),
		   (int) (pf.getImageableY() / scale),
		   (int) (pf.getImageableWidth() / scale),
		   (int) (pf.getImageableHeight() / scale));
		if (pageIndex > currentPage) {
			currentPage = pageIndex;
			pStartY += pEndY;
			pEndY = graphics2D.getClipBounds().getHeight();
		}
		graphics2D.translate(graphics2D.getClipBounds().getX(),
		   graphics2D.getClipBounds().getY());
		Rectangle allocation = new Rectangle(0,
		   (int) -pStartY,
		   (int) (jeditorPane.getMinimumSize().getWidth()),
		   (int) (jeditorPane.getPreferredSize().getHeight()));
		if (printView(graphics2D, allocation, rootView)) {
			return Printable.PAGE_EXISTS;
		} else {
			pStartY = 0;
			pEndY = 0;
			currentPage = -1;
			return Printable.NO_SUCH_PAGE;
		}
	}

	/**
	 * A protected method, printDialog(), displays the print dialog and initiates printing.
	 */
	protected void printDialog() {
		if (pJob.printDialog()) {
			pJob.setPrintable(this, pFormat);
			try {
				pJob.print();
				JOptionPane.showMessageDialog(parent,
				   I18N.getMsg("print.ok"),
				   I18N.getMsg("print"),
				   JOptionPane.INFORMATION_MESSAGE);
				return;
			} catch (PrinterException ex) {
				pStartY = 0;
				pEndY = 0;
				currentPage = -1;
				LOG.err("Printing error", ex);
				JOptionPane.showMessageDialog(parent,
				   I18N.getMsg("print.error", ex.getLocalizedMessage()),
				   I18N.getMsg("print"),
				   JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		JOptionPane.showMessageDialog(parent,
		   I18N.getMsg("print.cancelled"),
		   I18N.getMsg("print"),
		   JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * recursive method which iterates through the tree structure of the view sent to it.
	 *
	 * @param graphics2D
	 * @param allocation
	 * @param view
	 * @return
	 */
	protected boolean printView(Graphics2D graphics2D, Shape allocation, View view) {
		boolean pageExists = false;
		Rectangle clipRectangle = graphics2D.getClipBounds();
		Shape childAllocation;
		View childView;

		if (view.getViewCount() > 0
		   && !view.getElement().getName().equalsIgnoreCase("td")) {
			for (int i = 0; i < view.getViewCount(); i++) {
				childAllocation = view.getChildAllocation(i, allocation);
				if (childAllocation != null) {
					childView = view.getView(i);
					if (printView(graphics2D, childAllocation, childView)) {
						pageExists = true;
					}
				}
			}
		} else {
			if (allocation.getBounds().getMaxY() >= clipRectangle.getY()) {
				pageExists = true;
				if ((allocation.getBounds().getHeight() > clipRectangle.getHeight())
				   && (allocation.intersects(clipRectangle))) {
					view.paint(graphics2D, allocation);
				} else {
					if (allocation.getBounds().getY() >= clipRectangle.getY()) {
						if (allocation.getBounds().getMaxY() <= clipRectangle.getMaxY()) {
							view.paint(graphics2D, allocation);
						} else {
							if (allocation.getBounds().getY() < pEndY) {
								pEndY = allocation.getBounds().getY();
							}
						}
					}
				}
			}
		}
		return pageExists;
	}

}
