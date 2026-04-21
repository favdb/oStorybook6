/*
 * SbApp: Open Source software for novelists and authors.
 * Original idea 2008 - 2012 Martin Mustun
 * Copyrigth (C) Favdb
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
 */
package storybook.ui.chart.legend;

import i18n.I18N;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JLabel;
import storybook.db.strand.Strand;
import storybook.tools.swing.ColorUtil;
import storybook.ui.frames.main.MainFrame;

public class StrandsLegendPanel extends AbstractLegendPanel {

	public StrandsLegendPanel(MainFrame paramMainFrame) {
		super(paramMainFrame);
		initAll();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		setOpaque(false);
		add(new JLabel(I18N.getMsg("charts.caption.strands")));
		for (Strand strand : (List<Strand>) mainFrame.project.strands.findOrderBySort()) {
			JLabel label = new JLabel("    ", 0);
			label.setToolTipText(strand.getName());
			//label.setPreferredSize(new Dimension(100, 20));
			label.setBackground(ColorUtil.darker(strand.getJColor(), 0.05D));
			add(label, "sg");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
