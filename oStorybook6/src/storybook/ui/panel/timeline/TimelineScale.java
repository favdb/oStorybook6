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

import java.awt.Color;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JPanel;
import static javax.swing.SwingConstants.*;
import api.mig.swing.MigLayout;
import storybook.tools.DateUtil;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.Draw;
import storybook.tools.swing.FontUtil;
import storybook.ui.MIG;

/**
 *
 * @author favdb
 */
public class TimelineScale extends JPanel {

	private static final String TT = "TimelineScale";

	private Date dateBegin, dateEnd, dateLast;
	private int fontWidth, fontHeight, fontSize;
	private static final String U_VERT = "┴", D_VERT = "┬";
	private Long period;//period to show in minutes equals to difference beetwen end and begin
	private int zoom;// screen width in preferences
	private int increment;
	private boolean sameDay;

	public TimelineScale(int zoom, Date begin, Date end) {
		super();
		this.zoom = zoom;
		this.dateBegin = begin;
		this.dateEnd = end;
		initAll();
	}

	private void initAll() {
		init();
		initUi();
	}

	private void init() {
		//LOG.trace(TT + ".init() zoom=" + zoom);
		fontWidth = FontUtil.getWidth();
		fontHeight = FontUtil.getHeight();
		fontSize = FontUtil.getHeight();
		dateLast = dateEnd;
		period = DateUtil.getMinutes(dateLast) - DateUtil.getMinutes(dateBegin);
		increment = (period.intValue() / 100) * 10;
		sameDay = DateUtil.isSameDay(dateBegin, dateEnd);
		if (DateUtil.difMinutes(dateEnd, dateBegin) < 1440L) {//moins de 1 jour
			if (DateUtil.difMinutes(dateEnd, dateBegin) > 1080) {//plus de 12 heures
				increment = 60;
			} else if (DateUtil.difMinutes(dateEnd, dateBegin) > 480) {//plus de 8 heures
				increment = 30;
			} else {//moins de 8 heures
				increment = 15;
			}
		} else {//plus d'un jour
			increment = 1440;
		}
		Date curDate = dateBegin;
		while (curDate.before(dateEnd)) {
			curDate = DateUtil.addMinutes(curDate, increment);
		}
		dateLast = curDate;
		period = DateUtil.getMinutes(dateLast) - DateUtil.getMinutes(dateBegin);
		increment = (period.intValue() / 100) * 10;
		sameDay = DateUtil.isSameDay(dateBegin, dateLast);
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

	private void initUi() {
		/*LOG.trace(TT + "initUit() period=" + period
		+ ", lastDate=" + DateUtil.dateToString(dateLast)
		+ ", increment=" + increment
	);*/
		if (this.getComponentCount() > 0) {
			this.removeAll();
		}
		setLayout(new MigLayout(MIG.INS0));
		setBackground(new Color(ColorUtil.PALETTE.PALE_TURQUOISE.getValue()));
		Date curDate;
		String ddt, mdate = "";
		int xpos;
		//first scale days
		if (!sameDay) {
			curDate = DateUtil.getZeroTimeDate(dateBegin);
			while (curDate.before(dateLast)) {
				ddt = DateUtil.dateToString(curDate);
				xpos = dateToPosx(curDate);
				if (!mdate.equals(getDate(ddt))) {
					setLabelTo(getDate(ddt), xpos, CENTER, 0, fontSize);
					setLabelTo(U_VERT, xpos, LEFT, 1, fontSize);
					mdate = getDate(ddt);
				}
				curDate = DateUtil.addDays(curDate, 1);
			}
			ddt = DateUtil.dateToString(curDate);
			xpos = dateToPosx(curDate);
			if (!mdate.equals(getDate(ddt))) {
				setLabelTo(getDate(ddt), xpos, CENTER, 0, fontSize);
				setLabelTo(U_VERT, xpos, LEFT, 1, fontSize);
			}
		}
		//second scale hours
		curDate = dateBegin;
		while (curDate.before(dateLast)) {
			ddt = DateUtil.dateToString(curDate);
			xpos = dateToPosx(curDate);
			setLabelTo(D_VERT, xpos, LEFT, 1, fontSize);
			setLabelTo(getTime(ddt), xpos, CENTER, 2, fontSize);
			curDate = DateUtil.addMinutes(curDate, increment);
		}
		ddt = DateUtil.dateToString(curDate, false);
		xpos = dateToPosx(curDate);
		setLabelTo(D_VERT, xpos, LEFT, 1, fontSize);
		setLabelTo(getTime(ddt), xpos, CENTER, 2, fontSize);
		//draw scale line
		int x1 = dateToPosx(dateBegin) + (5 * fontWidth) + fontWidth;
		int x2 = xpos + (5 * fontWidth) + fontWidth;
		int y1 = fontHeight + (fontHeight / 2);
		add(Draw.line(x1, y1, x2, y1, Color.BLACK, 2));
		period = DateUtil.difMinutes(curDate, dateBegin);
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	private String getDate(String date) {
		if (!date.contains(" ")) {
			return "";
		}
		String r[] = date.split(" ");
		return r[0];
	}

	private String getTime(String date) {
		if (!date.contains(" ")) {
			return "";
		}
		String r[] = date.split(" ");
		return r[1];
	}

	private int setLabelTo(String text, int x, int pos, int line, int sz) {
		//LOG.trace(TT + ".setLabelTo(text=\"" + text + "\", x=" + x + ", pos=" + pos + ", line=" + line + ", sz=" + sz);
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
	 * set a JSLabel
	 *
	 * @param panel: JPanel where to draw the entity
	 * @param tle: the entity to draw
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
		Long l1 = DateUtil.getMinutes(date);
		Long l0 = DateUtil.getMinutes(dateBegin);
		if (l1 <= l0) {
			return 0;
		}
		Long dif = l1 - l0;
		return getColorSize(dif.intValue());
	}

	private String posToString(int x, int y) {
		return "pos " + (x + (5 * fontWidth)) + " " + y;
	}

	/**
	 * compute size for color bar from duration
	 *
	 * @param duration: duration in minutes
	 * @return length of color bar
	 */
	public int getColorSize(Integer duration) {
		Float percent = (duration.floatValue() / period.floatValue());
		Float re = (Long.valueOf(zoom).floatValue() * percent);
		return Math.round(re);
	}

	public boolean isSameDay() {
		return sameDay;
	}

}
