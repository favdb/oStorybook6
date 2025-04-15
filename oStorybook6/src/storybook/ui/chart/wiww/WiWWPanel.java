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
package storybook.ui.chart.wiww;

import api.mig.swing.MigLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import storybook.db.person.Person;
import storybook.tools.swing.ColorUtil;
import storybook.ui.interfaces.IPaintable;
import storybook.ui.interfaces.IRefreshable;

public class WiWWPanel extends JPanel implements IRefreshable, IPaintable {

	private WiWWContainer container;
	private boolean isSelected;

	public WiWWPanel(WiWWContainer container) {
		this(container, false);
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public WiWWPanel(WiWWContainer container, boolean bool) {
		this.container = container;
		this.isSelected = bool;
		init();
		initUi();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout("ins 0 1 0 1"));
		if (this.isSelected) {
			setBackground(UIManager.getColor("Table.selectionBackground"));
			setForeground(UIManager.getColor("Table.selectionForeground"));
		} else {
			setBackground(UIManager.getColor("Table.background"));
			setForeground(UIManager.getColor("Table.foreground"));
		}
		for (Person person : this.container.getCharacterList()) {
			JLabel label = new JLabel(person.getAbbr(), 0);
			Color lc2 = person.getJColor() == null ? ColorUtil.PALETTE.LIGHT_GREY.getColor() : person.getJColor();
			Color fg = label.getForeground();
			label.setToolTipText(person.getName());
			label.setBackground(lc2);
			add(label);
		}
	}

	@Override
	public void refresh() {
		removeAll();
		init();
		initUi();
	}
}
