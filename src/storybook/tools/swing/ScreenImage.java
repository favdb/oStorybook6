/*
Storybook: Scene-based software for novelists and authors.
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

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * class to capture Swing components into an image
 *
 * @author http://www.discoverteenergy.com/files/ScreenImage.java
 */
public class ScreenImage {

	private ScreenImage() {
		// empty
	}

	/**
	 * Create a BufferedImage for Swing components. The entire component will be captured to an
	 * image.
	 *
	 * @param comp : Swing component to create image from
	 * @param name : name of file to be created or null, may be a png or jpg file name
	 * @return image the image for the given region
	 * @exception IOException if an error occurs during writing
	 */
	public static BufferedImage createImage(JComponent comp, String name) throws IOException {
		Dimension d = comp.getSize();
		if (d.width == 0) {
			d = comp.getPreferredSize();
			comp.setSize(d);
		}
		Rectangle region = new Rectangle(0, 0, d.width, d.height);
		return ScreenImage.createImage(comp, region, name);
	}

	/**
	 * Create a BufferedImage for Swing components. All or part of the component can be captured to
	 * an image.
	 *
	 * @param comp Swing component to create image from
	 * @param rect The region of the component to be captured to an image
	 * @param name name of file to be created or null
	 * @return the BufferedImage for the given region
	 * @throws IOException if an error occurs during writing
	 */
	public static BufferedImage createImage(JComponent comp, Rectangle rect, String name) throws IOException {
		boolean opaqueValue = comp.isOpaque();
		comp.setOpaque(true);
		BufferedImage image = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setClip(rect);
		comp.paint(g2d);
		g2d.dispose();
		comp.setOpaque(opaqueValue);
		ScreenImage.writeImage(image, name);
		return image;
	}

	/**
	 * Create a BufferedImage for AWT components.
	 *
	 * @param comp AWT component to create image from
	 * @param name name of file to be created or null
	 * @return the BuffredImage for the given region
	 * @exception AWTException see Robot class constructors
	 * @exception IOException if an error occurs during writing
	 */
	public static BufferedImage createImage(Component comp, String name) throws AWTException, IOException {
		Point p = new Point(0, 0);
		SwingUtilities.convertPointToScreen(p, comp);
		Rectangle rect = comp.getBounds();
		rect.x = p.x;
		rect.y = p.y;
		return ScreenImage.createImage(rect, name);
	}

	/**
	 * Convenience method to create a BufferedImage of the desktop
	 *
	 * @param name name of file to be created or null
	 * @return the BuffredImage for the given region
	 * @exception AWTException see Robot class constructors
	 * @exception IOException if an error occurs during writing
	 */
	public static BufferedImage createDesktopImage(String name) throws AWTException, IOException {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle region = new Rectangle(0, 0, d.width, d.height);
		return ScreenImage.createImage(region, name);
	}

	/**
	 * Create a BufferedImage from a rectangular region on the screen.
	 *
	 * @param rect region on the screen to create image from
	 * @param name name of file to be created or null
	 * @return the BuffredImage for the given region
	 * @exception AWTException see Robot class constructors
	 * @exception IOException if an error occurs during writing
	 */
	public static BufferedImage createImage(Rectangle rect, String name) throws AWTException, IOException {
		BufferedImage image = new Robot().createScreenCapture(rect);
		ScreenImage.writeImage(image, name);
		return image;
	}

	/**
	 * Write a BufferedImage to a File.
	 *
	 * @param image image to be written
	 * @param name name of file to be created
	 * @exception IOException if an error occurs during writing
	 */
	public static void writeImage(BufferedImage image, String name) throws IOException {
		if (name == null) {
			return;
		}
		int offset = name.lastIndexOf(".");
		String type = offset == -1 ? "png" : name.substring(offset + 1);
		ImageIO.write(image, type, new File(name));
	}

}
