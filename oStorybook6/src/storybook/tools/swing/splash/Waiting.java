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
package storybook.tools.swing.splash;

import api.mig.swing.MigLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import resources.icons.IconUtil;
import storybook.App;
import storybook.tools.LOG;
import storybook.ui.MIG;

/**
 * waiting dialog
 *
 */
@SuppressWarnings("serial")
public class Waiting extends JDialog {

	private String text;
	private JLabel lbText;

	public Waiting(JFrame caller, String text) {
		super(caller, false);
		this.text = text;
		initialize();
	}

	private void initialize() {
		String migset = MIG.get(MIG.FILL, MIG.CENTER, MIG.FLOWY);
		setLayout(new MigLayout(migset));
		setUndecorated(true);
		setAlwaysOnTop(true);
		// logo
		JLabel lbLogo = new JLabel(IconUtil.getIcon("banner"));
		lbLogo.setBackground(Color.WHITE);
		lbLogo.setBorder(BorderFactory.createEtchedBorder());
		add(lbLogo, MIG.GROWX);

		JPanel panel = new JPanel(new MigLayout(migset));
		JLabel lbHg = new JLabel(IconUtil.getIcon("hourglass"));
		lbText = new JLabel(text);
		lbText.setFont(App.fonts.defGet());
		panel.add(lbHg, MIG.get(MIG.CENTER, "gap bottom 10"));
		panel.add(lbText);
		add(panel);
		pack();
		this.setLocationRelativeTo(getParent());
		setVisible(true);
		repaint();
	}

	public void setText(String s) {
		LOG.trace(s);
		lbText.setText(s);
		lbText.repaint();
	}

}
