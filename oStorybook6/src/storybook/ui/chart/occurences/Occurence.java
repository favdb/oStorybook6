/*
 * Copyright (C) 2017 favdb
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
package storybook.ui.chart.occurences;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.GroupLayout;
import static javax.swing.GroupLayout.Alignment.LEADING;
import javax.swing.JPanel;
import storybook.tools.DateUtil;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.SwingUtil;

/**
 *
 * @author favdb
 */
public class Occurence extends JPanel {

	private static final String TT = "Occurence";

	private final String type;
	private final Dataset dataset;

	public Occurence(String type, Dataset dataset) {
		this.type = type;
		this.dataset = dataset;
		init();
	}

	private void init() {
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING).addGap(0, 400, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(LEADING).addGap(0, 300, Short.MAX_VALUE));
	}

	public String getType() {
		return type;
	}

	@Override
	public void paintComponent(Graphics g) {
		if (dataset == null || dataset.items.isEmpty()) {
			return;
		}
		super.paintComponent(g);
		if ("date".equals(type)) {
			drawDate(g);
		}
		if ("value".equals(type)) {
			drawValue(g);
		}
	}

	public void redraw() {
		Graphics g = this.getGraphics();
		paintComponent(g);
	}

	void drawDate(Graphics g) {
		//LOG.trace(TT+"drawDate(g) dataset nb=" + dataset.items.size());
		createListId();
		dataset.marginT = 0;
		dataset.marginB = this.getHeight();
		String maxStr = "";
		for (DatasetItem item : dataset.items) {
			if (maxStr.length() < item.name.length()) {
				maxStr = item.name;
			}
		}
		maxStr += "W";
		Rectangle2D r = g.getFont().getStringBounds(maxStr, g.getFontMetrics().getFontRenderContext());
		dataset.marginL = (int) r.getWidth();
		dataset.marginR = this.getWidth();
		if (!dataset.items.isEmpty()) {
			drawDateXaxis(g);
			drawDateYaxis(g);
			drawDateArea(g);
		}
	}

	private void drawDateXaxis(Graphics g) {
		//Xaxis est la ligne des dates
		Date minDate = null;
		Date maxDate = null;
		for (DatasetItem item : dataset.items) {
			if (minDate == null) {
				minDate = item.debut;
			}
			if (DateUtil.toMinutes(minDate) > DateUtil.toMinutes(item.debut)) {
				minDate = item.debut;
			}
			if (maxDate == null) {
				maxDate = item.fin;
			}
			if (DateUtil.toMinutes(maxDate) < DateUtil.toMinutes(item.fin)) {
				maxDate = item.fin;
			}
		}
		long dif = DateUtil.toMinutes(maxDate) - DateUtil.toMinutes(minDate);
		if (dif < 0) {
			dif = 10L;
		}
		dataset.intervalDate = dif / 10;
		dataset.intervalX = (dataset.marginR - dataset.marginL) / 11;
		dataset.firstDate = minDate;
		dataset.lastDate = maxDate;
		String f = "yyyy MMM";
		if (dif < (24 * 60)) {
			f = "HH:mm";
		} else if (dif < (7 * 24 * 60)) {
			f = "d MMM HH:mm";
		} else if (dif < (30 * 24 * 60)) {
			f = "d MMM";
		} else if (dif < (120 * 24 * 60)) {
			f = "d MMM yyyy";
		} else if (dif < (365 * 24 * 60)) {
			f = "MMM yyyy";
		}
		SimpleDateFormat formatter = new SimpleDateFormat(f);
		for (int i = 0; i < 11; i++) {
			int x = dataset.marginL + (i * dataset.intervalX);
			int y = g.getFont().getSize();
			if (minDate != null) {
				g.drawString(formatter.format(minDate), x, y);
				minDate = DateUtil.addMinutes(minDate, (int) dataset.intervalDate);
			}
		}
		dataset.marginT += (g.getFont().getSize() * 2);
		dataset.areaHeight = dataset.marginB - dataset.marginT;
	}

	private void drawDateYaxis(Graphics g) {
		//yaxis est la colonne des Id
		int gap = (g.getFont().getSize() * 2);
		dataset.intervalY = dataset.areaHeight / (dataset.idList.size());
		if (dataset.intervalY == dataset.areaHeight) {
			dataset.intervalY = dataset.areaHeight / 2;
		}
		int i = 0;
		for (String strId : dataset.idList) {
			Color s = g.getColor();
			int y = (dataset.intervalY / 2) + (i * dataset.intervalY) + gap;
			g.drawString(strId, 0, y);
			g.setColor(s);
			i++;
		}
		dataset.areaWidth = dataset.marginR - dataset.marginL;
	}

	private void drawDateArea(Graphics g) {
		g.setColor(Color.lightGray);
		int gap = (g.getFont().getSize() * 2);
		int hauteur = (dataset.intervalY / 3);
		if (hauteur < g.getFont().getSize()) {
			hauteur = g.getFont().getSize();
		}
		for (int i = 0; i < dataset.idList.size(); i++) {
			DatasetItem item = dataset.getItem(dataset.idList.get(i));
			if (item.color == null) {
				continue;
			}
			Color s = g.getColor();
			int y = (dataset.intervalY / 2) + (i * dataset.intervalY) + gap - (g.getFont().getSize() / 3);
			g.setColor(ColorUtil.lighter(Color.gray, 0.7D));
			g.drawLine(dataset.marginL, y, dataset.marginR, y);
			g.setColor(s);
		}

		for (DatasetItem item : dataset.items) {
			if (item.color == null) {
				continue;//nothing to draw
			}
			drawDateItem(g, item, gap, hauteur);
			if ((item.subItems != null) && (!item.subItems.isEmpty())) {
				for (DatasetItem subItem : item.subItems) {
					drawDateItem(g, subItem, gap, hauteur);
				}
			}
		}
	}

	private void drawDateItem(Graphics g, DatasetItem item, int gap, int hauteur) {
		//LOG.trace(TT+"drawDateItem(...)");
		if (dataset.intervalDate == 0) {
			return;
		}
		long amplitude = DateUtil.toMinutes(dataset.lastDate) - DateUtil.toMinutes(dataset.firstDate);
		long debut = DateUtil.toMinutes(item.debut) - DateUtil.toMinutes(dataset.firstDate);
		long fin = DateUtil.toMinutes(item.fin) - DateUtil.toMinutes(dataset.firstDate);
		if (fin < 0) {
			fin = amplitude;
		}
		long x = ((dataset.areaWidth / amplitude) * debut);
		if (x < 0) {
			x = x * (-1);
		}
		int y = (dataset.intervalY / 2)
		   + (dataset.getIdIndex(item.name) * dataset.intervalY)
		   + gap - (g.getFont().getSize() / 3) - hauteur;
		long largeur = (((dataset.areaWidth / amplitude) * (fin - debut)));
		x += dataset.marginL;
		if (largeur > 0) {
			drawHorizontal(g, (int) x, (int) y, (int) largeur, hauteur * 2, item.color);
		}

	}

	private void drawHorizontal(Graphics g, int x, int y, int largeur, int hauteur, Color c) {
		GradientPaint gp = new GradientPaint(0, 0, ColorUtil.lighter(c, 0.5D), 0, hauteur / 2, c, true);
		Graphics2D g2d = (Graphics2D) g;
		Paint op = g2d.getPaint();
		g2d.setPaint(gp);
		g2d.fillRect(x, y, largeur, hauteur);
		g2d.setPaint(op);
		g2d.setColor(g.getColor());
		g.drawRect(x, y, largeur, hauteur);
		g.drawRect(x + 1, y + 1, largeur - 2, hauteur - 2);
		g.setColor(c);
	}

	void drawValue(Graphics g) {
		//LOG.trace(TT+"drawValue(g) dataset nb=" + dataset.items.size());
		createListId();
		dataset.marginT = 0;
		String maxStr = "W" + Long.toString(dataset.maxValue);
		Rectangle2D r = g.getFont().getStringBounds(maxStr, g.getFontMetrics().getFontRenderContext());
		dataset.marginL = (int) r.getWidth();
		dataset.marginR = this.getWidth();
		dataset.areaWidth = dataset.marginR - dataset.marginL;
		dataset.marginB = this.getHeight() - (g.getFont().getSize() * 2);
		dataset.areaHeight = dataset.marginB - dataset.marginT;
		if (!dataset.items.isEmpty()) {
			drawValueXaxis(g);
			drawValueYaxis(g);
			drawValueArea(g);
		}
	}

	private void drawValueXaxis(Graphics g) {
		//Xaxis est la ligne des Id
		dataset.intervalX = dataset.areaWidth / (dataset.idList.size());
	}

	private void drawValueYaxis(Graphics g) {
		//yaxis c'est la colonne des valeurs
		createListId();
		long dif = dataset.maxValue;
		if (dif < 20) {
			dif = 20L;
		}
		dataset.intervalValue = dif / 20;
		if (dataset.intervalValue * 20 > dataset.maxValue) {
			dataset.maxValue = dataset.intervalValue * 20;
		}
		dataset.intervalY = (int) (dataset.areaHeight / dataset.maxValue);
		for (long j = 0; j <= dataset.maxValue; j += dataset.intervalValue) {
			int x = 0;
			int y = (int) (dataset.marginT + (j * dataset.intervalY))
			   + (g.getFont().getSize() / 2) + g.getFont().getSize();
			g.drawString(Long.toString(dataset.maxValue - j), x, y);
		}
	}

	private void drawValueArea(Graphics g) {
		int gapX = (dataset.intervalX / 4);
		int gapY = (g.getFont().getSize() / 2);
		Color s = g.getColor();
		for (int y = 0; y <= dataset.marginB + dataset.intervalY; y += dataset.intervalY) {
			g.setColor(ColorUtil.lighter(Color.gray, 0.7D));
			g.drawLine(dataset.marginL,
			   y - gapY, dataset.marginR,
			   y - (g.getFont().getSize() / 2));
			g.setColor(s);
		}
		Graphics2D g2d = (Graphics2D) g;
		for (DatasetItem item : dataset.items) {
			int i = dataset.getIdIndex(item.name);
			int x = dataset.marginL + (i * dataset.intervalX) + gapX;
			int largeur = (gapX * 2);
			int j = (int) (item.value / dataset.intervalValue);
			int hauteur = (j * dataset.intervalY);
			int y = (int) (dataset.marginB - hauteur) - gapY + (g.getFont().getSize());
			SwingUtil.drawVertical(g, x, y, largeur, hauteur, item.color, item.value);
			Font nf = new Font("Sans", Font.BOLD, 15);
			Font of = g.getFont();
			g.setFont(nf);
			int x1 = (x + (gapX)) + (g.getFont().getSize() / 2);
			int y1 = (int) (dataset.marginB - g.getFont().getSize());
			SwingUtil.drawRotatedString(item.name, g2d, (float) x1, (float) y1, item.color);
			g.setFont(of);
		}

	}

	private void createListId() {
		dataset.idList = new ArrayList<>();
		for (DatasetItem item : dataset.items) {
			if (dataset.getIdIndex(item.name) == -1) {
				dataset.idList.add(item.name);
			}
			if (item.subItems != null) {
				for (DatasetItem subItem : item.subItems) {
					if (dataset.getIdIndex(subItem.name) == -1) {
						dataset.idList.add(subItem.name);
					}
				}
			}
		}
	}

}
