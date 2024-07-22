/*
 * Copyright (C) 2021 favdb
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
package storybook.tools.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author favdb
 */
public class Draw extends JPanel {

    private static int LINE = 1;
    private static int RECTANGLE = 2;
    private static int CIRCLE = 3;

    private int fromX;
    private int fromY;
    private int toX;
    private int toY;
    private int type = LINE;
    private Color color;
    private int thickness;

    /**
     * draw a geometric figure like LINE, RECTANGLE or CIRCLE
     *
     * @param type: int type designed (1=LINE, 2=RECTANGLE, 3=CIRCLE
     *
     * @param c1: JComponent from
     * @param c2: JComponent to
     * @param color
     * @param thickness
     */
    public Draw(int type, JComponent c1, JComponent c2, Color color, int thickness) {
	this.type = type;
	Point p1 = c1.getLocation();
	Point p2 = c2.getLocation();
	this.fromX = p1.x;
	this.fromY = p1.y;
	this.toX = p2.x;
	this.toY = p2.y;
	this.color = color;
	this.thickness = thickness;
	init();
    }

    public Draw(int type, JComponent c1, JComponent c2, Color color) {
	this(type, c1, c2, color, 1);
    }

    public Draw(int type, JComponent c1, JComponent c2) {
	this(type, c1, c2, Color.BLACK, 1);
    }

    /**
     * draw a geometric figure like LINE, RECTANGLE or CIRCLE
     *
     * @param type: int type designed (1=LINE, 2=RECTANGLE, 3=CIRCLE
     * @param x1: origin x coordinate
     * @param y1: origin y coordinate
     * @param x2: destination x coordinate
     * @param y2: destination y coordinate
     * @param color
     * @param thickness
     */
    public Draw(int type, int x1, int y1, int x2, int y2, Color color, int thickness) {
	this.type = type;
	this.fromX = x1;
	this.fromY = y1;
	this.toX = x2;
	this.toY = y2;
	this.color = color;
	this.thickness = thickness;
	init();
    }

    /**
     * draw a geometric figure like LINE, RECTANGLE or CIRCLE with default color and thickness=1
     *
     * @param type: int type designed (1=LINE, 2=RECTANGLE, 3=CIRCLE
     * @param x1: origin x coordinate
     * @param y1: origin y coordinate
     * @param x2: destination x coordinate
     * @param y2: destination y coordinate
     */
    public Draw(int type, int x1, int y1, int x2, int y2) {
	this(type, x1, y1, x2, y2, Color.BLACK, 1);
    }

    /**
     * draw a geometric figure like LINE, RECTANGLE or CIRCLE with defined color and thickness=1
     * from and to points
     *
     * @param p1: origin point
     * @param p2: destination point
     * @param color: the defined color
     */
    public Draw(Point p1, Point p2, Color color) {
	this(LINE, p1.x, p1.y, p2.x, p2.y, color, 1);
    }

    /**
     * draw a geometric figure like LINE, RECTANGLE or CIRCLE with default color and thickness=1
     * from and to points
     *
     * @param p1: origin point
     * @param p2: destination point
     */
    public Draw(Point p1, Point p2) {
	this(LINE, p1.x, p1.y, p2.x, p2.y, Color.BLACK, 1);
    }

    private void init() {
	Rectangle r = new Rectangle(fromX, fromY, toX + thickness + 1, toY + thickness + 1);
	Dimension dim = new Dimension(r.width, r.height);
	this.setSize(dim);
	this.setMinimumSize(dim);
	this.setPreferredSize(dim);
	this.setMaximumSize(dim);
    }

    @Override
    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	if (color != null) {
	    g2.setColor(color);
	}
	if (thickness > 0) {
	    g2.setStroke(new BasicStroke(thickness));
	}
	if (type == LINE) {
	    g2.drawLine(fromX, fromY, toX, toY);
	} else if (type == RECTANGLE) {
	    g2.drawRect(fromX, fromY, toX, toY);
	} else if (type == CIRCLE) {
	    g2.drawOval(fromX, fromY, toX, toY);
	}
    }

    public static Draw line(int fromX, int fromY, int toX, int toY, Color color, int thick) {
	return (new Draw(LINE, fromX, fromY, toX, toY, color, thick));
    }

    public static void line(Graphics g, int fromX, int fromY, int toX, int toY, Color color, int thick) {
	Point fromP = new Point(fromX, fromY), toP = new Point(fromY, toY);
	line(g, fromP, toP, color, thick);
    }

    public static void line(Graphics g, Point fromX, Point toX, Color kolor, int thick) {
	Color oc = g.getColor();
	g.setColor(kolor);
	((Graphics2D) g).setStroke(getStroke(thick));
	g.drawLine(fromX.x, fromX.y, toX.x, toX.y);
	g.setColor(oc);
    }

    /**
     * Draw a line from a point to another one, with thickness an color
     *
     * @param p1: from point
     * @param p2: to point
     * @param color: the line color
     * @param thick: the thickness
     * @param gapX: the x gap
     * @param gapY: the y gap
     * @param horiz: boolean to indicate if it is horizontal
     * @param g: graphic object
     */
    public static void line(Graphics g, Point p1, Point p2,
	    Color color, int thick, int gapX, int gapY, int horiz) {
	int x1 = p1.x,
		y1 = p1.y,
		x2 = p2.x,
		y2 = p2.y;
	Color oc = g.getColor();
	g.setColor(color);
	((Graphics2D) g).setStroke(new BasicStroke(thick));
	if (horiz == 0) {
	    if (p1.x != p2.x) {
		int xx = x1 + gapX;
		if (p1.x > p2.x) {
		    xx = x1 - gapX;
		}
		g.drawLine(x1, y1, xx, y1);
		g.drawLine(xx, y1, x2, y1);
		g.drawLine(x2, y1, x2, y2);
	    } else {
		g.drawLine(x1, y1, x2, y2);
	    }
	} else {
	    if (p1.y != p2.y) {
		int yy = y1 + gapY;
		g.drawLine(x1, y1, x1, yy);
		g.drawLine(x1, yy, x2, yy);
		g.drawLine(x2, yy, x2, y2);
	    } else {
		g.drawLine(x1, y1, x2, y2);
	    }
	}
	g.setColor(oc);
    }

    public static Draw rectangle(int fromX, int fromY, int toX, int toY, Color color, int thickness) {
	return (new Draw(RECTANGLE, fromX, fromY, toX, toY, color, thickness));
    }

    public static Draw circle(int fromX, int fromY, int toX, int toY, Color color, int thickness) {
	return (new Draw(CIRCLE, fromX, fromY, toX, toY, color, thickness));
    }

    private static final Float strokes[] = {1.0f, 2.0f, 3.0f, 4.0f};

    private static BasicStroke getStroke(int depth) {
	if (depth >= 1 && depth <= strokes.length) {
	    return (new BasicStroke(strokes[depth - 1]));
	}
	return (new BasicStroke(strokes[0]));
    }

    /**
     * Draw a vertical line with a possible drop-off
     *
     * @param g graphic context
     * @param p1 from point
     * @param p2 to point
     * @param kolor the line color
     * @param depth the thickness
     * @param gapY the x gap to use
     *
     */
    public static void Vline(Graphics g, Point p1, Point p2, Color kolor, int depth, int gapY) {
	int x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y;
	Color oc = g.getColor();
	g.setColor(kolor);
	((Graphics2D) g).setStroke(getStroke(depth));
	if (p1.x != p2.x) {
	    int yy = y1 + gapY;
	    g.drawLine(x1, y1, x1, yy);
	    g.drawLine(x1, yy, x2, yy);
	    g.drawLine(x2, yy, x2, y2);
	} else {
	    g.drawLine(x1, y1, x2, y2);
	}
	g.setColor(oc);
    }

    /**
     * Draw a horizontal line with a possible drop-off
     *
     * @param g graphic context
     * @param p1 from point
     * @param p2 to point
     * @param kolor the color of the line to draw
     * @param depth the thickness
     * @param gapX the y gap to use
     *
     */
    public static void Hline(Graphics g, Point p1, Point p2, Color kolor, int depth, int gapX) {
	int x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y;
	Color oc = g.getColor();
	g.setColor(kolor);
	((Graphics2D) g).setStroke(getStroke(depth));
	if (p1.y != p2.y) {
	    int xx = x1 + gapX;
	    g.drawLine(x1, y1, xx, y1);
	    g.drawLine(xx, y1, xx, y2);
	    g.drawLine(xx, y2, x2, y2);
	} else {
	    g.drawLine(x1, y1, x2, y2);
	}
	g.setColor(oc);
    }

    public static void Vgap(Graphics g, Point p0, Point pz, Color kolor, int depth, int gapX, int gapY) {
	Color oc = g.getColor();
	g.setColor(kolor);
	((Graphics2D) g).setStroke(getStroke(depth));
	/*int x1 = p0.x, y1 = p0.y, x2 = pz.x, y2 = pz.y;
		int xx1 = x1 - gapX, yy1 = y1 + gapY, yy2 = y2 - gapY;
		g.drawLine(x1, y1, x1, yy1);//p0->p1
		g.drawLine(x1, yy1, xx1, yy1);//p1->p2
		g.drawLine(xx1, yy1, xx1, yy2);//p2->p3
		g.drawLine(xx1, yy2, xx1, y2);//p3->p4
		g.drawLine(xx1, y2, x2, y2);//p4->p5*/
	Point p1 = addPoint(p0, 0, gapY);
	line(g, p0, p1, kolor, depth);
	Point p2 = addPoint(p1, gapX, 0);
	line(g, p1, p2, kolor, depth);
	Point p3 = addPoint(pz, gapX, -gapY);
	line(g, p2, p3, kolor, depth);
	Point p4 = addPoint(p3, -gapX, 0);
	line(g, p3, p4, kolor, depth);
	Point p5 = addPoint(p4, 0, gapY);
	line(g, p4, p5, kolor, depth);
	g.setColor(oc);
    }

    public static void Hgap(Graphics g, Point p0, Point pz, Color kolor, int depth, int gapX, int gapY) {
	int x1 = p0.x, y1 = p0.y, x2 = pz.x, y2 = pz.y;
	Color oc = g.getColor();
	g.setColor(kolor);
	((Graphics2D) g).setStroke(getStroke(depth));
	int xx1 = x1 + gapX, yy1 = y1 - gapY, xx2 = x2 - gapX;
	g.drawLine(x1, y1, xx1, y1);
	g.drawLine(xx1, y1, xx1, yy1);
	g.drawLine(xx1, yy1, xx2, yy1);
	g.drawLine(xx2, yy1, xx2, y2);
	g.drawLine(xx2, y2, x2, y2);
	g.setColor(oc);
    }

    private static Point addPoint(Point p, int sx, int sy) {
	return new Point(p.x + sx, p.y + sy);
    }

    public static void polyLine(Graphics g, List<Point> p, Color kolor, int depth) {
	int size = p.size();
	List<Integer> lx = new ArrayList<>();
	List<Integer> ly = new ArrayList<>();
	for (int i = 0; i < p.size(); i++) {
	    lx.add(p.get(i).x);
	    ly.add(p.get(i).y);
	}
	Color oc = g.getColor();
	g.setColor(kolor);
	((Graphics2D) g).setStroke(getStroke(depth));
	g.drawPolyline(lx.stream().mapToInt(i -> i).toArray(), ly.stream().mapToInt(i -> i).toArray(), size);
	g.setColor(oc);
    }

    public static void arrow2(Graphics g, Point p1, Point p2, Color color, int stroke) {
	Graphics2D g2 = (Graphics2D) g;
	Color oc = g.getColor();
	g.setColor(color);
	g2.setStroke(new BasicStroke(8));
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	//g2.draw(new Line2D.Double(p1, p2));
	drawArrowHead(g2, p2, p1, Color.GREEN);
	g.setColor(oc);
    }

    private static void drawArrowHead(Graphics2D g2, Point start, Point end, Color color) {
	g2.setPaint(color);
	double dy = start.y - end.y;
	double dx = start.x - end.x;
	double theta = Math.atan2(dy, dx);
	double x, y, rho = theta + Math.toRadians(40);
	for (int j = 0; j < 2; j++) {
	    x = start.x - 20 * Math.cos(rho);
	    y = start.y - 20 * Math.sin(rho);
	    g2.draw(new Line2D.Double(start.x, start.y, x, y));
	    rho = theta - Math.toRadians(40);
	}
    }
}
