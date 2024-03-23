/*
 * Copyright (C) 2020 favdb
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
package storybook.tools.swing;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Font;
import java.util.Enumeration;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import storybook.App;
import storybook.Pref;
import storybook.tools.LOG;

/**
 *
 * @author favdb
 */
public class LaF {

	private LaF() {
		// empty
	}

	private static boolean DARK_THEME = false;

	public static boolean isDark() {
		return DARK_THEME;
	}

	public static void init() {
		String lafName = App.preferences.getString(Pref.KEY.LAF, "light");
		try {
			switch (lafName.toLowerCase()) {
				case "advanced":
					DARK_THEME = false;
					UIManager.setLookAndFeel(new FlatIntelliJLaf());
					break;
				case "dark":
					DARK_THEME = true;
					UIManager.setLookAndFeel(new FlatDarkLaf());
					break;
				case "dracula":
					DARK_THEME = true;
					UIManager.setLookAndFeel(new FlatDarculaLaf());
					break;
				default:
					DARK_THEME = false;
					UIManager.setLookAndFeel(new FlatLightLaf());
					break;
			}
		} catch (UnsupportedLookAndFeelException ex) {
			LOG.err("Failed to initialize Flat Laf", ex);
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (ClassNotFoundException
			   | IllegalAccessException
			   | InstantiationException
			   | UnsupportedLookAndFeelException ex1) {
				LOG.err("unable to initialize default Laf");
			}
		}
		Color cb = (Color) UIManager.get("Table.background");
		Color cf = (Color) UIManager.get("Table.foreground");
		if (App.preferences.getBoolean(Pref.KEY.LAF_COLORED)) {
			cb = Color.decode(App.preferences.getString(Pref.KEY.LAF_COLOR));
		}
		if (DARK_THEME) {
			UIManager.put("Table.oddBackground", ColorUtil.darker(cb, 0.5));
			UIManager.put("Table.oddForeground", ColorUtil.lighter(cf, 0.5));
		} else {
			UIManager.put("Table.oddBackground", ColorUtil.lighter(ColorUtil.getPastel(cb), 0.30));
			UIManager.put("Table.oddForeground", ColorUtil.darker(cf, 0.75));
		}
	}

	public static void changeFont() {
		UIDefaults defaults = UIManager.getDefaults();
		Enumeration newKeys = defaults.keys();
		while (newKeys.hasMoreElements()) {
			Object obj = newKeys.nextElement();
			Object current = UIManager.get(obj);
			if (current instanceof FontUIResource) {
				defaults.put(obj, new FontUIResource(App.fonts.defGet()));
			} else if (current instanceof Font) {
				defaults.put(obj, App.fonts.defGet());
			}
		}
	}

}
