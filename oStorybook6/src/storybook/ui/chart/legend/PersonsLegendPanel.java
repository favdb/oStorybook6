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

import api.mig.swing.MigLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Set;
import javax.swing.JLabel;
import storybook.db.person.Person;
import storybook.tools.swing.ColorUtil;
import storybook.ui.frames.main.MainFrame;

public class PersonsLegendPanel extends AbstractLegendPanel {

	private Collection<Person> collection;

	public PersonsLegendPanel(MainFrame paramMainFrame, Set<Person> paramSet) {
		super(paramMainFrame);
		this.collection = paramSet;
		initAll();
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout("wrap 10"));
		setOpaque(false);
		for (Person localPerson : this.collection) {
			JLabel lbAbbr = new JLabel(localPerson.getAbbreviation(), 0);
			lbAbbr.setPreferredSize(new Dimension(50, 18));
			if (localPerson.getColor() != null) {
				lbAbbr.setBackground(ColorUtil.darker(localPerson.getJColor(), 0.05D));
			} else {
				lbAbbr.setBackground(ColorUtil.PALETTE.LIGHT_GREY.getColor());
			}
			JLabel lbName = new JLabel();
			lbName.setText(localPerson.getFullName());
			add(lbAbbr, "sg");
			add(lbName, "gap after 10");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
