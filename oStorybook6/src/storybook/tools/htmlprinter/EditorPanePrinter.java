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
package storybook.tools.htmlprinter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import javax.print.PrintService;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.html.HTMLEditorKit;

public class EditorPanePrinter extends JPanel implements Pageable, Printable {

	JEditorPane panel;
	Paper paper;
	Insets margins;
	ArrayList<PagePanel> pages;
	int pageWidth, pageHeight;
	View rootView;
	public static int PAGE_SHIFT = 20;
	PageFormat pageFormat;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public EditorPanePrinter(String html, Paper paper, Insets margins) {
		initialize(html, paper, margins);
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public EditorPanePrinter(JEditorPane pane, Paper paper, Insets margins) {
		initialize(pane.getText(), paper, margins);
	}

	public void initialize(String html, Paper paper, Insets margins) {
		panel = new JEditorPane();
		panel.setEditorKit(new HTMLEditorKit());
		panel.setContentType("text/html");
		panel.setText(html);
		this.paper = paper;
		this.margins = margins;
		this.pageWidth = (int) paper.getWidth();
		this.pageHeight = (int) paper.getHeight();
		pageFormat = new PageFormat();
		paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
		pageFormat.setPaper(paper);

		doPagesLayout();
	}

	public void doPagesLayout() {
		setLayout(null);
		removeAll();
		this.rootView = panel.getUI().getRootView(panel);
		panel.setSize(pageWidth - margins.top - margins.bottom, Integer.MAX_VALUE);
		Dimension d = panel.getPreferredSize();
		panel.setSize(pageWidth - margins.top - margins.bottom, d.height);
		calculatePageInfo();
		int count = pages.size();
		this.setPreferredSize(new Dimension(pageWidth * 2 + 50, PAGE_SHIFT + count * (pageHeight + PAGE_SHIFT)));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		AffineTransform old = ((Graphics2D) g).getTransform();
		((Graphics2D) g).setTransform(old);
	}

	protected void calculatePageInfo() {
		pages = new ArrayList<>();
		int startY = 0;
		int endPageY = getEndPageY(startY);
		while (startY + pageHeight - margins.top - margins.bottom < panel.getHeight()) {
			Shape pageShape = getPageShape(startY,
					pageWidth - margins.left - margins.right,
					pageHeight - margins.top - margins.bottom,
					panel);
			pages.add(new PagePanel(startY, endPageY, pageShape));
			startY = endPageY;
			endPageY = getEndPageY(startY);
		}
		Shape pageShape = getPageShape(startY,
				pageWidth - margins.left - margins.right,
				pageHeight - margins.top - margins.bottom,
				panel);
		pages.add(new PagePanel(startY, endPageY, pageShape));
		int count = 0;
		for (PagePanel pi : pages) {
			add(pi);
			pi.setLocation(PAGE_SHIFT, PAGE_SHIFT + count * (pageHeight + PAGE_SHIFT));
			count++;
		}
	}

	protected int getEndPageY(int startY) {
		int desiredY = startY + pageHeight - margins.top - margins.bottom;
		int realY = desiredY;
		for (int x = 1; x < pageWidth; x++) {
			View v = getLeafViewAtPoint(new Point(x, realY), rootView);
			if (v != null) {
				Rectangle alloc = PrintApp.getAllocation(v, panel).getBounds();
				if (alloc.height > pageHeight - margins.top - margins.bottom) {
					continue;
				}
				if (alloc.y + alloc.height > desiredY) {
					realY = Math.min(realY, alloc.y);
				}
			}
		}

		return realY;
	}

	protected View getLeafViewAtPoint(Point p, View root) {
		return getLeafViewAtPoint(p, root, panel);
	}

	public static View getLeafViewAtPoint(Point p, View root, JEditorPane sourcePane) {
		int pos = sourcePane.viewToModel(p);
		View v = sourcePane.getUI().getRootView(sourcePane);
		while (v.getViewCount() > 0) {
			int i = v.getViewIndex(pos, Position.Bias.Forward);
			v = v.getView(i);
		}
		Shape alloc = PrintApp.getAllocation(root, sourcePane);
		if (alloc.contains(p)) {
			return v;
		}
		return null;
	}

	public static Shape getPageShape(int pageStartY, int pageWidth, int pageHeight, JEditorPane sourcePane) {
		Area result = new Area(new Rectangle(0, 0, pageWidth, pageHeight));
		View rootView = sourcePane.getUI().getRootView(sourcePane);
		Rectangle last = new Rectangle();
		for (int x = 1; x < pageWidth; x++) {
			View v = getLeafViewAtPoint(new Point(x, pageStartY), rootView, sourcePane);
			if (v != null) {
				Rectangle alloc = PrintApp.getAllocation(v, sourcePane).getBounds();
				if (alloc.y < pageStartY && alloc.y + alloc.height > pageStartY) {
					if (!alloc.equals(last)) {
						Rectangle r = new Rectangle(alloc);
						r.y -= pageStartY;
						result.subtract(new Area(r));
					}
				}
				last = alloc;
			}
		}

		last = new Rectangle();
		for (int x = 1; x < pageWidth; x++) {
			View v = getLeafViewAtPoint(new Point(x, pageStartY + pageHeight), rootView, sourcePane);
			if (v != null) {
				Rectangle alloc = PrintApp.getAllocation(v, sourcePane).getBounds();
				if (alloc.y < pageStartY + pageHeight && alloc.y + alloc.height > pageStartY + pageHeight) {
					if (!alloc.equals(last)) {
						Rectangle r = new Rectangle(alloc);
						r.y -= pageStartY;
						result.subtract(new Area(r));
					}
				}
				last = alloc;
			}
		}

		return result;
	}
	//pageable methods

	@Override
	public int getNumberOfPages() {
		return pages.size();
	}

	@Override
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		return pageFormat;
	}

	@Override
	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		return this;
	}

	@Override
	public int print(Graphics g, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		if (pageIndex < pages.size()) {
			pageFormat.getPaper().setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
			pages.get(pageIndex).isPrinting = true;
			pages.get(pageIndex).paint(g);
			pages.get(pageIndex).isPrinting = false;
			return PAGE_EXISTS;
		}
		return NO_SUCH_PAGE;
	}

	class PagePanel extends JPanel {

		int pageStartY;
		int pageEndY;
		Shape pageShape;
		boolean isPrinting = false;
		JPanel innerPage = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				AffineTransform old = ((Graphics2D) g).getTransform();
				Shape oldClip = g.getClip();
				Area newClip = new Area(oldClip);
				if (isPrinting) {
					newClip = new Area(pageShape);
				} else {
					newClip.intersect(new Area(pageShape));
				}
				g.setClip(newClip);
				g.translate(0, -pageStartY);
				panel.paint(g);
				for (Component c : panel.getComponents()) {
					AffineTransform tmp = ((Graphics2D) g).getTransform();
					g.translate(c.getX(), c.getY());
					((Container) c).getComponent(0).paint(g);
					((Graphics2D) g).setTransform(tmp);
				}
				((Graphics2D) g).setTransform(old);
				g.setClip(oldClip);
			}
		};

		public PagePanel() {
			this(0, 0, null);
		}

		public PagePanel(int pageStartY, int pageEndY, Shape pageShape) {
			this.pageStartY = pageStartY;
			this.pageEndY = pageEndY;
			this.pageShape = pageShape;
			setSize(pageWidth, pageHeight);
			setBackground(Color.white);
			setLayout(null);
			add(innerPage);
			innerPage.setBounds(margins.left,
					margins.top, pageWidth - margins.left - margins.right,
					pageHeight - margins.top - margins.bottom);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.black);
			g.drawRect(0, 0, getWidth() - 2, getHeight() - 2);
		}
	}

	public void print() {
		print((PrintService) null);
	}

	public void print(PrintService ps) {
		try {
			PrinterJob pj = PrinterJob.getPrinterJob();
			JFrame tmp = null;
			if (this.getParent() == null) {
				tmp = new JFrame();
				tmp.getContentPane().add(new JScrollPane(this));
				tmp.pack();
				tmp.setVisible(false);
			}
			pj.setPageable(this);
			if (ps != null) {
				pj.setPrintService(ps);
			}
			pj.print();

			if (tmp != null) {
				tmp.dispose();
			}
		} catch (PrinterException e1) {
			e1.printStackTrace(System.err);
		}
	}
}
