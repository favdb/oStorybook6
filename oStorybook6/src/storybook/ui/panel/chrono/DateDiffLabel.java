/*
Storybook: Scene-based software for novelists and authors.
Copyright (C) 2008 - 2011 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.ui.panel.chrono;

import i18n.I18N;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.JLabel;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.tools.DateUtil;
import storybook.tools.html.Html;

@SuppressWarnings("serial")
public class DateDiffLabel extends JLabel {

	private Date date1;
	private Date date2;

	public DateDiffLabel(Date date1, Date date2) {
		this(date1, date2, false);
	}

	public DateDiffLabel(Date date1, Date date2, boolean isVertical) {
		super("", JLabel.CENTER);
		this.date1 = date1;
		this.date2 = date2;
		String text = I18N.getColonMsg("preferences.datediff") + " " + getDays();
		DateFormat formatter = I18N.getLongDateFormatter();
		String text2 = "(" + formatter.format(date1) + " - " + formatter.format(date2) + ")";
		setFont(App.fonts.defGet());
		setText(getDays() + " " + text2);
		setToolTipText(Html.HTML_B + text + Html.BR + text2 + Html.HTML_E);
		setIcon(IconUtil.getIconSmall(ICONS.K.DATEDIFF));
	}

	public final int getDays() {
		return DateUtil.daysBetween(date1, date2);
	}
}
