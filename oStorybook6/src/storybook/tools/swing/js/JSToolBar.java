/*
 * Copyright (C) 2022 favdb
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
package storybook.tools.swing.js;

import api.mig.swing.MigLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.JToolBar;
import storybook.App;
import storybook.Pref;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.LaF;
import storybook.ui.MIG;

/**
 * a JToolBar with default background color and optional gradient (color from preferences)
 *
 * @author favdb
 */
public class JSToolBar extends JToolBar {

	private final boolean showGradient;

	public JSToolBar(boolean showGradient) {
		super();
		this.showGradient = showGradient;
		setLayout(new MigLayout(MIG.get(MIG.FLOWX, MIG.HIDEMODE3, MIG.INS1)));
		setBorder(BorderFactory.createRaisedBevelBorder());
		if (App.preferences.getBoolean(Pref.KEY.LAF_COLORED)) {
			Color c = Color.decode(App.preferences.getString(Pref.KEY.LAF_COLOR));
			if (LaF.isDark()) {
				ColorUtil.darker(c, 0.5);
			} else {
				ColorUtil.lighter(c, 0.5);
			}
			setBackground(c);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		if (showGradient) {
			Graphics2D g2d = (Graphics2D) g;
			Color c = Color.decode(App.preferences.getString(Pref.KEY.LAF_COLOR));
			Color c2 = Color.WHITE;
			if (!LaF.isDark()) {
				c2 = Color.BLACK;
			}
			c2 = ColorUtil.blend(c, c2);
			GradientPaint gradient = new GradientPaint(0, 0,
					c,
					this.getWidth() / 2,
					this.getHeight(),
					c2, true);
			g2d.setPaint(gradient);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
		} else {
			super.paintComponent(g);
		}
	}

}
