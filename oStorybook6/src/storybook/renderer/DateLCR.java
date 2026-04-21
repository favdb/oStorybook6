/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

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
package storybook.renderer;

import java.awt.Component;
import java.util.Date;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import storybook.tools.DateUtil;
import storybook.tools.LOG;
import storybook.ui.frames.main.MainFrame;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class DateLCR extends DefaultListCellRenderer {

	private final MainFrame mainFrame;

	public DateLCR(MainFrame main) {
		this.mainFrame = main;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean sel, boolean focus) {
		try {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, sel, focus);
			if (value instanceof String) {
				label.setText((String) value);
			} else if (value instanceof Date) {
				label.setText(DateUtil.simpleDateToString((Date) value));
			} else {
				if (value == null) {
					return label;
				} else {
					LOG.trace("LCRDate unkown date type is " + value.toString());
				}
			}
			return label;
		} catch (Exception ex) {
			return new JLabel("");
		}
	}
}
