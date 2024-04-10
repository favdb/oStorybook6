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

import java.awt.Container;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import api.mig.swing.MigLayout;
import resources.icons.IconUtil;
import storybook.App;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.interfaces.IPaintable;

/**
 * @author martin
 * @modified by: favdb
 *
 */
@SuppressWarnings("serial")
public class WaitingSplash extends JDialog implements IPaintable {

	private String text;
	private JLabel lbText;
	private final Container caller;

	public WaitingSplash(Container parent, String text) {
		this.caller = parent;
		this.text = text;
		init();
		initUi();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		String migset = MIG.get(MIG.FILL, MIG.CENTER, MIG.FLOWY);
		setLayout(new MigLayout(migset));
		setUndecorated(true);
		setAlwaysOnTop(true);
		JPanel panel = new JPanel(new MigLayout(migset));
		panel.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel lbHg = new JLabel(IconUtil.getIcon("hourglass"));
		lbText = new JLabel(text);
		lbText.setFont(App.getInstance().fonts.defGet());
		panel.add(lbHg, MIG.get(MIG.CENTER, "gap bottom 10"));
		panel.add(lbText);
		add(panel);
		SwingUtil.showDialog(this, caller);
		repaint();
	}

	public void setText(String s) {
		//App.trace("waiting: " + s);
		text = s;
		lbText.setText(s);
		repaint();
	}

}
