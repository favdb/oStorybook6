/*
Storybook: Scene-based software for novelists and authors.
Copyright (C) 2008-2009 Martin Mustun

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
package storybook.tools.swing.js;

import java.awt.Color;
import javax.swing.Icon;
import javax.swing.JLabel;
import storybook.App;
import storybook.tools.swing.ColorUtil;

@SuppressWarnings("serial")
public class JSLabel extends JLabel {

	public JSLabel() {
		super();
		setFont(App.getInstance().fonts.defGet());
	}

	public JSLabel(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
		setFont(App.getInstance().fonts.defGet());
	}

	public JSLabel(Icon image) {
		super(image);
		setFont(App.getInstance().fonts.defGet());
	}

	public JSLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		setFont(App.getInstance().fonts.defGet());
	}

	public JSLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		setFont(App.getInstance().fonts.defGet());
	}

	public JSLabel(String text) {
		super(text);
		setFont(App.getInstance().fonts.defGet());
	}

	public JSLabel(Icon icon, Color bkcolor) {
		super(icon);
		setFont(App.getInstance().fonts.defGet());
		setBackground(bkcolor);
	}

	@Override
	public void setBackground(Color bg) {
		if (bg == null) {
			setOpaque(false);
		} else {
			super.setBackground(bg);
			setOpaque(true);
			setForeground(ColorUtil.isDark(bg) ? Color.WHITE : Color.BLACK);
		}
	}

}
