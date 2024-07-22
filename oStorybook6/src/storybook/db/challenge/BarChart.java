/*
 * Copyright (C) 2024 favdb
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
package storybook.db.challenge;

import api.mig.swing.MigLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import static javax.swing.SwingConstants.RIGHT;
import storybook.App;
import storybook.Pref;
import storybook.tools.swing.Draw;
import storybook.tools.swing.FontUtil;
import storybook.ui.MIG;
import storybook.ui.chart.occurences.DatasetItem;

/**
 * simple bar chart
 *
 * @author favdb
 */
public class BarChart extends JPanel {

	public static final String TT = "BarChart.";

	public static boolean VERTICAL = true, HORIZONTAL = false;

	private boolean mode = false;// true for vertical
	private Integer nbValues = 0, objective = -1;// array of values
	private List<DatasetItem> listItems;// list of values
	private Integer max;// maximum value

	/**
	 * set the BarChart
	 *
	 * @param mode: true for vertical mode
	 * @param nbValues: number of values
	 * @param maxValue: max value to determine scale
	 * @param values: list of DayaItem of values
	 */
	public BarChart(boolean mode, Integer nbValues, Integer maxValue, List<DatasetItem> values) {
		this.mode = mode;
		this.nbValues = nbValues;
		this.listItems = values;
		this.max = maxValue;
		this.setLayout(new MigLayout());
		initialize();
	}

	/**
	 * initialize labels
	 */
	private void initialize() {
		//LOG.trace(TT + "initialize()");
		if (mode) {
			initVertical();
		} else {
			initHorizontal();
		}
	}

	/**
	 * initialize labels for vertical mode
	 */
	private void initVertical() {
		//LOG.trace(TT + "initVertical()");
		this.removeAll();
		int gap = 4,
			barWidth = Math.min(getWidth() / nbValues, FontUtil.getWidth() * 5) - gap,
			bottom = getHeight() - FontUtil.getHeight();
		for (int i = 0; i < nbValues; i++) {
			int barX = i * (barWidth + gap);
			JLabel lb = new JLabel((i + 1) + "");
			lb.setToolTipText(listItems.get(i).getName());
			lb.setBackground(Color.PINK);
			add(lb, MIG.getPos(barX, bottom));
		}
	}

	/**
	 * initialize labels for horizontal mode
	 */
	private void initHorizontal() {
		//LOG.trace(TT + "initHorizontal()");
		int inc = FontUtil.getHeight() / 3;
		int barHeight = FontUtil.getHeight() + inc;
		int marginTop = FontUtil.getHeight() / 3;
		for (int i = 0; i < nbValues; i++) {
			JTextField lb = new JTextField((i + 1) + "");
			lb.setToolTipText(listItems.get(i).getName());
			lb.setColumns(2);
			lb.setHorizontalAlignment(RIGHT);
			lb.setEditable(false);
			lb.setBorder(null);
			int barY = marginTop + (i * barHeight) + (inc / 2);
			add(lb, MIG.getPos(0, barY));
		}
		add(new JLabel(""), MIG.getPos(getWidth(), 0));
	}

	public void setNbValues(int nb) {
		this.nbValues = nb;
	}

	/**
	 * set the values and re-initialize
	 *
	 * @param values
	 */
	public void setValues(List<DatasetItem> values) {
		//LOG.trace(TT + "setValues(values nb=" + values.size() + ")");
		this.listItems = values;
		this.max = 0;
		for (DatasetItem value : values) {
			max = Math.max(max, value.value.intValue());
		}
		initialize();
	}

	public void setObjective(int v) {
		this.objective = v;
	}

	public void setMax(int max) {
		this.max = max;
	}

	/**
	 * paint the chart
	 *
	 * @param g
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (listItems == null || listItems.size() == 0 || nbValues == 0) {
			return;
		}
		if (mode) {
			paintVertical(g);
		} else {
			paintHorizontal(g);
		}
	}

	/**
	 * paint for horizontal mode
	 *
	 * @param g
	 */
	protected void paintHorizontal(Graphics g) {
		JLabel lb = new JLabel("99");
		int width = getWidth();
		int gap = FontUtil.getHeight() / 3;
		int barHeight = FontUtil.getHeight() + gap;
		int maxBarWidth = width - (FontUtil.getWidth() * 12);
		int marginLeft = 50, marginTop = FontUtil.getHeight() / 3;
		int nmax = Math.max(max, objective);
		if (objective != -1 && nmax != 0) {
			int xz = Math.max(maxBarWidth * objective / Math.max(max, objective), nmax);
			Point p1 = new Point(xz, 0);
			Point p2 = new Point(xz, Math.min(getHeight(), barHeight * nmax));
			Draw.line(g, p1, p2, Color.ORANGE, 1);
		}
		for (int i = 0; i < nbValues; i++) {
			//LOG.trace("i=" + i + "> name=" + listItems.get(i).toString());
			int value = listItems.get(i).value.intValue();
			if (value > 0) {
				int barWidth = maxBarWidth * value / max;
				int barY = marginTop + (i * barHeight);

				if (App.preferences.getBoolean(Pref.KEY.LAF_COLORED)) {
					g.setColor(Color.decode(App.preferences.getString(Pref.KEY.LAF_COLOR)));
				} else {
					g.setColor(Color.BLUE);
				}
				g.fillRect(marginLeft, barY + gap, barWidth, barHeight - (gap * 2));
				g.setColor(Color.LIGHT_GRAY);
				g.drawRect(marginLeft, barY + gap, barWidth, barHeight - (gap * 2));

				// Dessiner la valeur sur chaque barre
				String valueStr = String.valueOf(value);
				g.setColor(lb.getForeground());
				g.drawString(valueStr, marginLeft + barWidth + 5, barY + (barHeight / 2 + g.getFontMetrics().getAscent() / 2) - 2);
			}
		}
	}

	/**
	 * paint for vertical mode
	 *
	 * @param g
	 */
	protected void paintVertical(Graphics g) {
		int gap = 4,
			width = getWidth(),
			height = getHeight(),
			barWidth = Math.min(width / nbValues, FontUtil.getWidth() * 5) - gap;

		int top = FontUtil.getHeight(),
			bottom = FontUtil.getHeight(),
			barHeight = height - top - bottom;
		Color def = ((new JTextField()).getForeground());

		for (int i = 0; i < nbValues; i++) {
			int value = listItems.get(i).value.intValue();
			int barX = i * (barWidth + gap);
			int barY = top + (barHeight - (barHeight * value / max));
			int barHeightValue = barHeight * value / max;
			// drawing value of the bar
			if (App.preferences.getBoolean(Pref.KEY.LAF_COLORED)) {
				g.setColor(Color.decode(App.preferences.getString(Pref.KEY.LAF_COLOR)));
			} else {
				g.setColor(Color.BLUE);
			}
			g.fillRect(barX, barY, barWidth - 2, barHeightValue);
			g.setColor(Color.LIGHT_GRAY);
			g.drawRect(barX, barY, barWidth - 2, barHeightValue);
			// value label for the bar
			if (value > 0) {
				String str = String.valueOf(value);
				int vwidth = g.getFontMetrics().stringWidth(str);
				g.setColor(def);
				g.drawString(str, barX + (barWidth - vwidth) / 2, barY - 5);
			}
		}
	}

}
