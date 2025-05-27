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
package storybook.ui.panel.timeline;

import api.mig.swing.MigLayout;
import java.awt.Color;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JPanel;
import static javax.swing.SwingConstants.*;
import storybook.tools.DateUtil;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.Draw;
import storybook.tools.swing.FontUtil;
import storybook.ui.MIG;

/**
 * class to draw the timeline
 *
 * @author favdb
 */
public class TimelineScale extends JPanel {

	private static final String TT = "TimelineScale.";

	private final Date dateBegin, dateEnd;
	private int fontWidth, fontHeight, fontSize;
	private static final String U_VERT = "┴", D_VERT = "┬";
	private long totalMinutes; // durée totale en minutes
	private int zoom; // largeur d'écran en préférences
	private int increment; // incrément en minutes pour l'échelle
	private boolean sameDay;
	// Constants for the increments
	private static final int INCREMENT_15_MIN = 15;
	private static final int INCREMENT_30_MIN = 30;
	private static final int INCREMENT_1_HOUR = 60;
	private static final int INCREMENT_1_DAY = 1440;
	// Thresholds in minutes
	private static final long THRESHOLD_8_HOURS = 480L;
	private static final long THRESHOLD_12_HOURS = 1080L;
	private static final long THRESHOLD_1_DAY = 1440L;

	public TimelineScale(int zoom, Date begin, Date end) {
		super();
		this.zoom = zoom;
		this.dateBegin = begin;
		this.dateEnd = end;
		initAll();
	}

	/**
	 * initialize all
	 */
	private void initAll() {
		init();
		initUi();
	}

	/**
	 * initialize
	 */
	private void init() {
		//LOG.trace(TT + "init() zoom=" + zoom);
		// Initialize the Font properties
		fontWidth = FontUtil.getWidth();
		fontHeight = FontUtil.getHeight();
		fontSize = FontUtil.getHeight();
		// Compute the total duration
		totalMinutes = DateUtil.difMinutes(dateEnd, dateBegin);
		sameDay = DateUtil.isSameDay(dateBegin, dateEnd);
		// Compute the incrément from duration
		increment = calculateIncrement(totalMinutes);
	}

	/**
	 * compute increment
	 *
	 * @param durationMinutes
	 * @return
	 */
	private int calculateIncrement(long durationMinutes) {
		if (durationMinutes < THRESHOLD_8_HOURS) {
			return INCREMENT_15_MIN; // less than 8 hours -> 15 min
		} else if (durationMinutes < THRESHOLD_12_HOURS) {
			return INCREMENT_30_MIN; // 8-12 hours -> 30 min
		} else if (durationMinutes < THRESHOLD_1_DAY) {
			return INCREMENT_1_HOUR; // 12-24 hours -> 1 hour
		} else {
			return INCREMENT_1_DAY; // more than 1 day -> 1 day
		}
	}

	public Date getDateBegin() {
		return dateBegin;
	}

	public Date getDateEnd() {
		return dateEnd;
	}

	public int getFontWidth() {
		return fontWidth;
	}

	public int getFontHeight() {
		return fontHeight;
	}

	public int getFontSize() {
		return fontSize;
	}

	/**
	 * initialize the UI
	 */
	private void initUi() {
		//LOG.trace(TT + "initUi() totalMinutes=" + totalMinutes + ", increment=" + increment);
		// Clean existing components
		if (this.getComponentCount() > 0) {
			this.removeAll();
		}
		setLayout(new MigLayout(MIG.INS0));
		setBackground(new Color(ColorUtil.PALETTE.PALE_TURQUOISE.getValue()));
		// Draw the scale for the days (if needed)
		if (!sameDay) {
			drawDayScale();
		}
		// Draw the scale for hours/times
		drawTimeScale();
		// Draw the scale line
		drawScaleLine();
	}

	private void drawDayScale() {
		Date currentDate = DateUtil.getZeroTimeDate(dateBegin);
		String lastDateStr = "";
		// Safety limit to avoid infinite loops
		int maxIterations = (int) (totalMinutes / INCREMENT_1_DAY) + 2;
		int iterations = 0;
		while (currentDate.before(dateEnd) && iterations < maxIterations) {
			String dateStr = DateUtil.dateToString(currentDate);
			int xpos = dateToPosx(currentDate);
			String dayStr = getDate(dateStr);
			if (!lastDateStr.equals(dayStr) && !dayStr.isEmpty()) {
				setLabelTo(dayStr, xpos, CENTER, 0, fontSize);
				setLabelTo(U_VERT, xpos, LEFT, 1, fontSize);
				lastDateStr = dayStr;
			}
			currentDate = DateUtil.addDays(currentDate, 1);
			iterations++;
		}
	}

	private void drawTimeScale() {
		Date currentDate = (Date) dateBegin.clone();
		// Safety limit to avoid infinite loops
		int maxIterations = (int) (totalMinutes / increment) + 2;
		int iterations = 0;
		while (currentDate.before(dateEnd) && iterations < maxIterations) {
			String dateTimeStr = DateUtil.dateToString(currentDate);
			int xpos = dateToPosx(currentDate);
			setLabelTo(D_VERT, xpos, LEFT, 1, fontSize);
			setLabelTo(getTime(dateTimeStr), xpos, CENTER, 2, fontSize);
			currentDate = DateUtil.addMinutes(currentDate, increment);
			iterations++;
		}
		// Final mark
		String finalDateTime = DateUtil.dateToString(dateEnd, false);
		int finalXpos = dateToPosx(dateEnd);
		setLabelTo(D_VERT, finalXpos, LEFT, 1, fontSize);
		setLabelTo(getTime(finalDateTime), finalXpos, CENTER, 2, fontSize);
	}

	private void drawScaleLine() {
		int x1 = dateToPosx(dateBegin) + (5 * fontWidth) + fontWidth;
		int x2 = dateToPosx(dateEnd) + (5 * fontWidth) + fontWidth;
		int y1 = fontHeight + (fontHeight / 2);
		add(Draw.line(x1, y1, x2, y1, Color.BLACK, 2));
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	private String getDate(String dateTime) {
		if (!dateTime.contains(" ")) {
			return "";
		}
		return dateTime.split(" ")[0];
	}

	private String getTime(String dateTime) {
		if (!dateTime.contains(" ")) {
			return "";
		}
		return dateTime.split(" ")[1];
	}

	private int setLabelTo(String text, int x, int pos, int line, int sz) {
		//LOG.trace(TT + "setLabelTo(text=\"" + text
		//		+ "\", x=" + x
		//		+ ", pos=" + pos
		//		+ ", line=" + line
		//		+ ", sz=" + sz + ")");
		int xx = x;
		int yy = line * fontHeight;
		JLabel lb = new JLabel(text);
		lb.setFont(FontUtil.getFixed(sz));
		int len = text.length() * fontWidth;
		if (text.length() > 1) {
			switch (pos) {
				case LEFT:
					break;
				case CENTER:
					xx = xx - (len / 2);
					break;
				case RIGHT:
					xx = xx - len;
					break;
				case 9:
					xx = xx - (len / 2);
					yy = yy + (fontHeight / 2);
					break;
				default:
					break;
			}
		}
		this.add(lb, posToString(xx + 3, yy));
		return xx;
	}

	/**
	 * Position a timeline entity on the panel
	 *
	 * @param panel: JPanel where to draw the entity
	 * @param tle: entity to draw
	 * @param line: line where to draw
	 */
	public void setEntityTo(JPanel panel, TimelineEntity tle, int line) {
		if (tle == null) {
			panel.add(new JLabel(" "), "pos 0 " + (fontHeight * (5 + 16)));
			return;
		}
		int xx = dateToPosx(tle.getDate()) + fontWidth;
		int yy = ((line * fontHeight) * 2) + (fontHeight * 5);
		panel.add(tle, posToString(xx, yy));
	}

	private int dateToPosx(Date date) {
		long dateMinutes = DateUtil.getMinutes(date);
		long beginMinutes = DateUtil.getMinutes(dateBegin);
		if (dateMinutes <= beginMinutes) {
			return 0;
		}
		long diffMinutes = dateMinutes - beginMinutes;
		return getColorSize((int) diffMinutes);
	}

	private String posToString(int x, int y) {
		return "pos " + (x + (5 * fontWidth)) + " " + y;
	}

	/**
	 * Compute the size of the color bar from duration
	 *
	 * @param durationMinutes: duration in minutes
	 * @return length of the color bar
	 */
	public int getColorSize(int durationMinutes) {
		if (totalMinutes == 0) {
			return 0;
		}
		float percent = (float) durationMinutes / (float) totalMinutes;
		float result = (float) zoom * percent;
		return Math.round(result);
	}

	public boolean isSameDay() {
		return sameDay;
	}
}
