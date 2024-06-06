/*
 * Copyright (C) 2023 favdb
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
package storybook.ui.panel.tell;

import api.mig.swing.MigLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import resources.icons.IconUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.tools.swing.Draw;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.LaF;
import storybook.ui.MIG;
import storybook.ui.Ui;

/**
 * tool for the Pyramid of Freytag sugested by Vincent
 *
 * see https://en.wikipedia.org/wiki/Gustav_Freytag (en) or
 * https://www.scenarmag.fr/2015/02/17/la-pyramide-de-freytag-ou-la-structure-en-5-actes/ (fr)
 *
 * @author favdb
 */
public class GraphFreytag extends GraphAbstract {

    private static final String TT = "GraphFreytag.";

    // basic pyramide
    List<Point> points, rising, falling;
    private Point pointStart, pointEvent, pointClimax, pointResolution, pointEnd;
    private static final int CHAR_HEIGHT = FontUtil.getHeight() * 2;

    public GraphFreytag(TellingPanel freytagFrame) {
	super(freytagFrame);
    }

    /**
     * initalize
     */
    @Override
    @SuppressWarnings("unchecked")
    public void init() {
	points = new ArrayList<>();
	rising = new ArrayList<>();
	falling = new ArrayList<>();
    }

    /**
     * set the initial points
     */
    private void initPoints() {
	//LOG.trace(TT + "initPoints()");
	points.clear();
	rising.clear();
	falling.clear();
	int xmargin = 40, ymargin = 40;
	int xstart = xmargin, ystart = 40, yend = 630, height = 780 - 40, width = 1024 - xstart;
	pointStart = new Point(xstart, yend);
	points.add(pointStart);
	pointEvent = new Point(xstart + CHAR_HEIGHT, pointStart.y);
	points.add(pointEvent);
	pointClimax = new Point((width / 2), ystart);
	// intermediate rising
	int n = getRising();
	int x = Math.abs(pointClimax.x - pointEvent.x) / (n + 1);
	int y = Math.abs(pointEvent.y - pointClimax.y) / (n + 1);
	for (int i = 1; i <= n; i++) {
	    int x1 = pointEvent.x + (x * i),
		    y1 = pointEvent.y - (y * i);
	    rising.add(new Point(x1, y1));
	}
	points.add(pointClimax);//490,50 -> climax
	pointResolution = new Point(((width - ymargin) - CHAR_HEIGHT), yend - ymargin);
	// intermediate falling
	n = getFalling();
	x = Math.abs(pointResolution.x - pointClimax.x) / (n + 1);
	y = Math.abs(pointClimax.y - pointResolution.y) / (n + 1);
	for (int i = 1; i <= n; i++) {
	    int x1 = pointClimax.x + (x * i), y1 = pointClimax.y + (y * i);
	    falling.add(new Point(x1 + IconUtil.getDefSize(), y1));
	}
	points.add(pointResolution);
	pointEnd = new Point(width - xmargin, pointResolution.y);
	points.add(pointEnd);
    }

    /**
     * get the rising of the graph
     *
     * @return
     */
    private int getRising() {
	int n = 0;
	for (Storytelling f : stories) {
	    if (f.getValue() == 3) {
		n++;
	    }
	}
	return n;
    }

    /**
     * get the falling of the graph
     *
     * @return
     */
    private int getFalling() {
	int n = 0;
	for (Storytelling f : stories) {
	    if (f.getValue() == 5) {
		n++;
	    }
	}
	return n;
    }

    /**
     * initialize the UI
     */
    @Override
    public void initUi() {
	//LOG.trace(TT+"initUi()");
	setLayout(new MigLayout(MIG.get(MIG.FILL)));
	if (!LaF.isDark()) {
	    setBackground(Color.white);
	}
	refresh();
    }

    /**
     * draw the graph
     */
    @Override
    public void drawStories() {
	initPoints();
	int n = 0, prev = 0;
	for (Storytelling f : stories) {
	    if (prev != f.getValue()) {
		n = 0;
	    }
	    Book.TYPE t = f.getType();
	    if ((storyPanel.ckPartsIs() && t.isPart())
		    || (storyPanel.ckChaptersIs() && t.isChapter())
		    || (storyPanel.ckScenesIs() && t.isScene())) {
		drawStory(f, n);
		n++;
	    }
	    prev = f.getValue();
	}
	add(new JLabel(" "), MIG.posToString(1024, 768));
    }

    /**
     * draw an element of the graph
     *
     * @param fr: the elemtn to draw
     * @param n : the indice of the element in case of more than one element to draw
     */
    @Override
    public void drawStory(Storytelling fr, int n) {
	//LOG.trace(TT+"drawFreytag(freytag='" + fr.getName() + "', n=" + n + ")");
	if (rising == null) {
	    return;
	}
	AbstractEntity entity = mainFrame.project.get(fr.getType(), fr.getId());
	JButton bt = Ui.initButton("btEdit", entity, e -> mainFrame.showEditorAsDialog(entity));
	bt.setOpaque(true);
	int xx = 0, yy = 630 + 2;
	int nx = n;
	Point pt;
	switch (fr.getValue()) {
	    case 1: // starting
		xx = pointStart.x - 40;
		yy = pointStart.y + 3;
		break;
	    case 2: // event
		xx = pointEvent.x - 20;
		yy = pointEvent.y - CHAR_HEIGHT;
		break;
	    case 3: // rising action(s)
		if (rising.size() > n) {
		    pt = rising.get(n);
		    xx = pt.x - 5;
		    yy = pt.y;
		    nx = 0;
		}
		break;
	    case 4: // climax
		xx = 400;
		yy = 0;
		break;
	    case 5: // falling action(s)
		if (falling.size() > n) {
		    pt = falling.get(n);
		    xx = pt.x;
		    yy = pt.y;
		    nx = 0;
		}
		break;
	    case 6: // resolution
		xx = pointResolution.x;
		yy = pointResolution.y - CHAR_HEIGHT;
		break;
	    case 7: // ending
		xx = pointEnd.x;
		yy = pointEnd.y + 3;
		break;
	}
	if (nx > 0) {
	    xx = xx + ((IconUtil.getDefSize() / 2) * nx);
	    yy = yy + ((IconUtil.getDefSize() / 2) * nx);
	}
	add(bt, MIG.posToString(xx, yy));
    }

    @Override
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	Draw.polyLine(g, points, Color.ORANGE, 5);
    }

}
