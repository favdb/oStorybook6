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
package storybook.ui.panels.tell;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JButton;
import resources.icons.IconUtil;
import storybook.db.abs.AbstractEntity;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.FontUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;

/**
 * graphic for the Vogler representation
 *
 *
 * @author favdb
 */
public class GraphVogler extends GraphAbstract {

    private static final String TT = "GraphVogler.";

    int xc, yc;
    private int rayon;

    public GraphVogler(TellingPanel frame) {
	super(frame);
    }

    /**
     * initialize
     */
    @Override
    public void init() {
	xc = 1024 / 2;
	yc = 800 / 2;
	rayon = Math.min(xc, yc) * 80 / 100;
    }

    /**
     * draw the graph
     */
    @Override
    public void drawStories() {
	//LOG.trace(TT + "refresh()");
	int n = 0, old = -1;
	for (Storytelling v : stories) {
	    AbstractEntity entity = mainFrame.project.get(v.getType(), v.getId());
	    int value = v.getValue();
	    if (old != value) {
		n = 0;
	    }
	    drawStory(v, n);
	    n++;
	}
    }

    /**
     * draw an element of the graph
     *
     * @param fr: the element to draw
     * @param n : the index of the element in case of multiple element to draw at this point
     */
    @Override
    public void drawStory(Storytelling fr, int n) {
	AbstractEntity entity = mainFrame.project.get(fr.getType(), fr.getId());
	JButton bt = Ui.initButton("btEdit" + entity.getId().toString(), entity,
		e -> mainFrame.showEditorAsDialog(entity));
	bt.setOpaque(true);
	int gapX = (FontUtil.getWidth() * 2), gapY = FontUtil.getHeight();
	double angle = fr.getValue() * Math.PI / 6.0 - Math.PI / 2.0;
	double x = xc + rayon * Math.cos(angle);
	double y = yc + rayon * Math.sin(angle);
	if (n > 0) {
	    if (x > xc) {
		x = x + (IconUtil.getDefSize() / 2);
		y = y + (IconUtil.getDefSize() / 2);
	    } else {
		x = x - (IconUtil.getDefSize() / 2);
		y = y - (IconUtil.getDefSize() / 2);
	    }
	}
	add(bt, MIG.posToString((int) x + gapX, (int) y - gapY));
    }

    @Override
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	g.setFont(FontUtil.getBold());
	((Graphics2D) g).setStroke(new BasicStroke(48));
	g.setColor(ColorUtil.lighter(new Color(ColorUtil.PALETTE.ORANGE.getRGB()), 0.2));
	g.drawOval(xc - rayon, yc - rayon, rayon * 2, rayon * 2);
	((Graphics2D) g).setStroke(new BasicStroke(3));
	g.setColor(new Color(ColorUtil.PALETTE.OLIVE_DRAB.getRGB()));
	for (int i = 2; i < 60; i += 20) {
	    double anglesec = (i * ((Math.PI) / 30.0) - (Math.PI / 2.0));
	    int xsf = xc + (int) (0.9 * rayon * Math.cos(anglesec));
	    int ysf = yc + (int) (0.9 * rayon * Math.sin(anglesec));
	    g.drawLine(xc, yc, xsf, ysf);
	}
	for (int i = 1; i <= 12; i++) {
	    double angle = i * Math.PI / 6.0 - Math.PI / 2.0;
	    double x = xc + (rayon - 8) * Math.cos(angle);
	    double y = yc + (rayon - 4) * Math.sin(angle);
	    if (i > 9) {
		x -= FontUtil.getWidth();
	    }
	    g.drawString("" + i, (int) x, (int) y);
	}
    }

}
