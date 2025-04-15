/*
 * $Id$
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package storybook.tools.swing.js;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.plaf.UIResource;

/**
 * Icon class for rendering icon which indicates user control of column visibility.
 *
 * @author Amy Fowler from project JXSwing, modified by favdb
 *
 * @version 1.0
 */
public class JSColumnControlIcon implements Icon, UIResource {

	private final int width = 10, height = 10;

	/**
	 * TO DO: need to support small, medium, large
	 */
	public JSColumnControlIcon() {
		//empty
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Color color = c.getForeground();
		g.setColor(color);

		// draw horizontal lines
		g.drawLine(x, y, x + 8, y);
		g.drawLine(x, y + 2, x + 8, y + 2);
		g.drawLine(x, y + 8, x + 2, y + 8);

		// draw vertical lines
		g.drawLine(x, y + 1, x, y + 7);
		g.drawLine(x + 4, y + 1, x + 4, y + 4);
		g.drawLine(x + 8, y + 1, x + 8, y + 4);

		// draw arrow
		g.drawLine(x + 3, y + 6, x + 9, y + 6);
		g.drawLine(x + 4, y + 7, x + 8, y + 7);
		g.drawLine(x + 5, y + 8, x + 7, y + 8);
		g.drawLine(x + 6, y + 9, x + 6, y + 9);

	}

	public static void main(String args[]) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel label = new JLabel(new JSColumnControlIcon());
		frame.getContentPane().add(BorderLayout.CENTER, label);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setSize(new Dimension(64, 64));
		frame.setVisible(true);
	}

}
