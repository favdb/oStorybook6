/*
 * Copyright (C) 2017 favdb
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.ui.panel.memoria;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import storybook.App;
import storybook.Pref;
import storybook.tools.swing.js.JSLabel;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractOptions;

/**
 *
 * @author favdb
 */
public class MemoriaOpt extends AbstractOptions implements ActionListener {

	private JComboBox cbLayout;
	private JCheckBox ckAll;

	public MemoriaOpt(MainFrame m) {
		super(m);
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		setLayout(new MigLayout("wrap,fill"));
		// balloon or tree layout
		add(new JSLabel(I18N.getColonMsg("graph.presentation")));
		cbLayout = new JComboBox();
		cbLayout.addItem(I18N.getMsg("memoria.layout.balloon"));
		cbLayout.addItem(I18N.getMsg("memoria.layout.spring"));
		cbLayout.addItem(I18N.getMsg("memoria.layout.tree"));
		cbLayout.setSelectedIndex(App.preferences.getInteger(Pref.KEY.MEMORIA_LAYOUT));
		cbLayout.addActionListener(this);
		add(cbLayout, "split 2");
		ckAll = new JCheckBox(I18N.getMsg("memoria.all"));
		ckAll.setSelected(App.preferences.getBoolean(Pref.KEY.MEMORIA_ALL));
		ckAll.setVisible(cbLayout.getSelectedIndex() == 1);
		ckAll.addActionListener(this);
		add(ckAll);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int val = cbLayout.getSelectedIndex();
		ckAll.setVisible(cbLayout.getSelectedIndex() == 1);
		mainFrame.getBookController().memoriaSetLayout(val);
		App.preferences.memoriaSetLayout(val);
		App.preferences.memoriaSetAll(ckAll.isSelected());
	}
}
