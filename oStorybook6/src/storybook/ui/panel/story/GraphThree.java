/*
 * Copyright (C) 2023 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.ui.panel.story;

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
import storybook.ui.MIG;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class GraphThree extends GraphAbstract {

	private static final String TT = "ClassicGraph";

	private static final int CHAR_HEIGHT = FontUtil.getHeight() * 2;
	private List<Point> points;

	public GraphThree(StoryPanel panel) {
		super(panel);
	}

	@Override
	public void init() {
		initPoints();
	}

	private void initPoints() {
		//LOG.printInfos(this, ".initPoints()");
		points = new ArrayList<>();
		int xmargin = 40, ymargin = 40;
		int xstart = xmargin, ystart = ymargin, yend = 630, height = 780 - ymargin, width = 1024 - xmargin;
		Point pointStart = new Point(xstart, yend);
		points.add(pointStart);
		Point pointEvent = new Point(xstart + CHAR_HEIGHT, pointStart.y);
		points.add(pointEvent);
		Point pointClimax = new Point((width / 2), ystart);
		// intermediate rising
		int n = 3;
		int x = Math.abs(pointClimax.x - pointEvent.x) / (n + 1);
		int y = Math.abs(pointEvent.y - pointClimax.y) / (n + 1);
		for (int i = 1; i <= n; i++) {
			int x1 = pointEvent.x + (x * i) + (x / 2), y1 = pointEvent.y - (y * i);
			points.add(new Point(x1, y1));
		}
		points.add(pointClimax);
		Point pointResolution = new Point(((width - ymargin) - CHAR_HEIGHT), yend - ymargin);
		// intermediate falling
		n = 3;
		x = Math.abs(pointResolution.x - pointClimax.x) / (n + 1);
		y = Math.abs(pointClimax.y - pointResolution.y) / (n + 1);
		for (int i = 1; i <= n; i++) {
			int x1 = pointClimax.x + (x * i), y1 = pointClimax.y + (y * i);
			points.add(new Point(x1 + IconUtil.getDefSize(), y1));
		}
		points.add(pointResolution);
		Point pointEnd = new Point(width - xmargin, pointResolution.y);
		points.add(pointEnd);
	}

	@Override
	public void drawStories() {
		//LOG.printInfos(TT + ".drawStories() nb stories=" + stories.size());
		int n = 0, prev = 0;
		for (Story f : stories) {
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

	@Override
	public void drawStory(Story fr, int n) {
		//LOG.printInfos(this.getClass(), ".drawStory(fr=" + fr.getName() + ", n=" + n + ") value=" + fr.getValue());
		AbstractEntity entity = mainFrame.project.get(fr.getType(), fr.getId());
		JButton bt = Ui.initButton("btEdit", entity, e -> mainFrame.showEditorAsDialog(entity));
		bt.setOpaque(true);
		Point pt = points.get(fr.getValue());
		int xx = pt.x, yy = pt.y;
		if (n > 0) {
			xx = xx + (IconUtil.getDefSize() * n);
			yy = yy + (IconUtil.getDefSize() * n);
		}
		add(bt, MIG.posToString(xx + 5, yy));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Draw.polyLine(g, points, Color.ORANGE, 5);
		for (int i = 0; i < points.size() - 2; i++) {
			Point p1 = points.get(i), p2 = points.get(i + 1);
			Draw.arrow2(g, p1, p2, Color.ORANGE, 5);
		}
	}

}
