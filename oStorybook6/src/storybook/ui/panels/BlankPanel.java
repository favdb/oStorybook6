/*
STORYBOOK: Open Source software for novelists and authors.
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
package storybook.ui.panels;

import api.mig.swing.MigLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import resources.icons.IconUtil;
import storybook.Const;
import storybook.tools.html.Html;
import storybook.tools.net.Net;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.LaF;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class BlankPanel extends AbstractPanel {

	private static final String TT = "BlankPanel";

	public BlankPanel(MainFrame mainFrame) {
		super(mainFrame);
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		//LOG.trace(TT + ".initUi()");
		setLayout(new MigLayout(MIG.get(MIG.FLOWX, MIG.WRAP1), "[center]"));
		Dimension dim = SwingUtil.getScreenSize();
		setPreferredSize(new Dimension(dim.width / 2, dim.height / 2));
		setBorder(BorderFactory.createLoweredBevelBorder());
		removeAll();
		add(new JLabel(IconUtil.getImageIcon("banner")), MIG.WRAP);
		JLabel lbVersion = new JLabel(Const.getFullName());
		lbVersion.setForeground(LaF.isDark() ? ColorUtil.lighter(Color.GREEN, 0.5) : ColorUtil.darker(Color.GREEN, 0.5));
		add(lbVersion, MIG.WRAP);
		JTextPane tp = new JTextPane();
		tp.setContentType(Html.TYPE);
		tp.setOpaque(true);
		tp.setEditable(false);
		tp.setText(Const.getGpl());
		tp.setCaretPosition(0);
		tp.addHyperlinkListener((HyperlinkListener) e -> {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				//LOG.trace(TT + " opening URL in default navigator");
				Net.openUrl(e);
			}
		});
		JScrollPane scroller = new JScrollPane(tp);
		scroller.setBorder(BorderFactory.createLineBorder(ColorUtil.darker(Color.green, 0.5)));
		scroller.setMaximumSize(SwingUtil.getScreenSize());
		add(scroller, MIG.GROW);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
