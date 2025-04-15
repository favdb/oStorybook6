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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.Icon;

/**
 * @author martin
 *
 */
public class ColorIcon implements Icon {

	private int height = 8;
	private int width = 8;
	private Color color = null;

	public ColorIcon(Color c) {
		this.color = c;
	}

	public ColorIcon(Color c, Dimension dim) {
		this.color = c;
		this.height = dim.height;
		this.width = dim.width;
	}

	public ColorIcon(Color c, int height) {
		this.color = c;
		this.height = height;
		this.width = height;
	}

	public ColorIcon(Color c, int height, int width) {
		this.color = c;
		this.height = height;
		this.width = width;
	}

	/**
	 * @see javax.swing.Icon#getIconHeight()
	 */
	@Override
	public int getIconHeight() {
		return height;
	}

	/**
	 * @see javax.swing.Icon#getIconWidth()
	 */
	@Override
	public int getIconWidth() {
		return width;
	}

	public Color getColor() {
		return color;
	}

	/**
	 * @param component
	 * @param graphics
	 * @param x
	 * @param y
	 * @see javax.swing.Icon#paintIcon(Component, Graphics, int, int)
	 */
	@Override
	public void paintIcon(Component component, Graphics graphics, int x, int y) {
		graphics.setColor(color);
		graphics.drawRect(x, y, width, height);
		graphics.fillRect(x, y, width, height);
	}

	public Image toImage() {
		int w = getIconWidth();
		int h = getIconHeight();
		GraphicsEnvironment ge
				= GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(w, h);
		Graphics2D g = image.createGraphics();
		paintIcon(null, g, 0, 0);
		g.dispose();
		return image;
	}

}
