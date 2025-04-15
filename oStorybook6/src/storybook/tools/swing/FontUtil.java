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
package storybook.tools.swing;

import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JLabel;
import javax.swing.plaf.FontUIResource;
import storybook.App;

/**
 * @author martin
 *
 */
public class FontUtil {

	private static Font defaultFont;

	private FontUtil() {
		//empty
	}

	public static int getHeight() {
		return getHeight(new JLabel("W"));
	}

	public static int getHeight(JLabel x) {
		FontMetrics fnt = x.getFontMetrics(x.getFont());
		return fnt.getHeight();
	}

	public static int getWidth() {
		return getWidth(new JLabel("W"));
	}

	public static int getWidth(JLabel x) {
		FontMetrics fnt = x.getFontMetrics(x.getFont());
		int n = 0, r = 0;
		for (int i = '0'; i < '9'; i++) {
			n += fnt.charWidth(i);
			r++;
		}
		for (int i = 'A'; i < 'Z'; i++) {
			n += fnt.charWidth(i);
			r++;
		}
		for (int i = 'a'; i < 'z'; i++) {
			n += fnt.charWidth(i);
			r++;
		}
		if (r == 0) {
			r = 1;
		}
		return n / r;
	}

	public static Font getFixed(int sz) {
		return new Font(Font.MONOSPACED, Font.PLAIN, sz);
	}

	public static Font getItalic(Font def) {
		return new Font(def.getName(), Font.ITALIC, def.getSize());
	}

	public static Font getBold(Font def) {
		return new Font(def.getName(), Font.BOLD, def.getSize());
	}

	public static Font getBold() {
		return new Font(defaultFont.getName(), Font.BOLD, defaultFont.getSize());
	}

	public static Font getDefault() {
		return defaultFont;
	}

	public static void setDefault(Font font) {
		if (font == null) {
			return;
		}
		SwingUtil.setUIFont(new FontUIResource(font.getName(), font.getStyle(), font.getSize()));
		defaultFont = font;
	}

	public static String getString(Font font) {
		if (font == null) {
			return (getString(App.fonts.defGet()));
		}
		StringBuilder buf = new StringBuilder();
		buf.append(font.getName());
		buf.append(", ");
		switch (font.getStyle()) {
			case Font.BOLD:
				buf.append("bold");
				break;
			case Font.ITALIC:
				buf.append("italic");
				break;
			default:
				buf.append("plain");
				break;
		}
		buf.append(", ").append(font.getSize());
		return buf.toString();
	}

	public static Font getFont(String str) {
		//LOG.trace(TT+".getFont(str="+str+")");
		String s[] = str.split(",");
		if (s.length < 3) {
			return (getDefault());
		}
		int style;
		switch (s[1].trim()) {
			case "1":
				style = Font.BOLD;
				break;
			case "2":
				style = Font.ITALIC;
				break;
			default:
				style = Font.PLAIN;
				break;
		}
		int sz = Integer.parseInt(s[2].trim());
		if (sz == 0) {
			sz = 12;
		}
		return new Font(s[0], style, sz);
	}

	public static Font getSmall() {
		//LOG.trace(TT+".getSmallFont()");
		int sz = App.fonts.defGet().getSize();
		sz = (sz / 4) * 3;
		return new Font("Default", 0, sz);
	}

}
