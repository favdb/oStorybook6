/*
 * Copyright (C) 2024 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.tools.swing.splash;

/**
 *
 * @author favdb
 */
import java.awt.*;
import java.awt.event.*;

public class SplashDemo extends Frame implements ActionListener {

	static void renderSplashFrame(Graphics2D g, int frame) {
		final String[] comps = {"foo", "bar", "baz"};
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(120, 140, 200, 40);
		g.setPaintMode();
		g.setColor(Color.BLACK);
		g.drawString("Loading " + comps[(frame / 5) % 3] + "...", 120, 150);
	}

	@SuppressWarnings("LeakingThisInConstructor")
	public SplashDemo() {
		super("SplashScreen demo");
		setSize(300, 200);
		setLayout(new BorderLayout());
		Menu m1 = new Menu("File");
		MenuItem mi1 = new MenuItem("Exit");
		m1.add(mi1);
		mi1.addActionListener(this);
		this.addWindowListener(closeWindow);

		MenuBar mb = new MenuBar();
		setMenuBar(mb);
		mb.add(m1);
		final SplashScreen splash = SplashScreen.getSplashScreen();
		if (splash == null) {
			System.out.println("SplashScreen.getSplashScreen() returned null");
			return;
		}
		Graphics2D g = splash.createGraphics();
		if (g == null) {
			System.out.println("g is null");
			return;
		}
		for (int i = 0; i < 100; i++) {
			renderSplashFrame(g, i);
			splash.update();
			try {
				Thread.sleep(90);
			} catch (InterruptedException e) {
			}
		}
		splash.close();
		setVisible(true);
		toFront();
	}

	public void actionPerformed(ActionEvent ae) {
		System.exit(0);
	}

	private static WindowListener closeWindow = new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
			e.getWindow().dispose();
		}
	};

	public static void main(String args[]) {
		SplashDemo test = new SplashDemo();
	}

}
