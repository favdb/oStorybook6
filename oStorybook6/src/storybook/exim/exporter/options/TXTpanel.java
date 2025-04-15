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
package storybook.exim.exporter.options;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ItemEvent;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import storybook.db.book.BookParamExport;
import storybook.ui.MIG;

/**
 *
 * @author favdb
 */
public class TXTpanel extends JPanel {

	private JRadioButton rbOther;
	private JTextField txSeparator;
	private JRadioButton rbTab;
	private final BookParamExport param;

	public TXTpanel(BookParamExport param) {
		this.param = param;
		setLayout(new MigLayout(MIG.FILL, "[][]"));
		setBorder(BorderFactory.createTitledBorder("TXT"));
		add(new JLabel(I18N.getMsg("export.options.csv.separate")), MIG.SPAN);
		ButtonGroup group1 = new ButtonGroup();
		rbTab = new JRadioButton("tab");
		rbOther = new JRadioButton(I18N.getMsg("other"));
		rbOther.addItemListener((java.awt.event.ItemEvent evt) -> {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				txSeparator.setVisible(true);
			} else {
				txSeparator.setVisible(false);
			}
		});
		group1.add(rbTab);
		group1.add(rbOther);
		add(rbTab);
		add(rbOther);
		txSeparator = new JTextField();
		txSeparator.setHorizontalAlignment(JTextField.CENTER);
		txSeparator.setColumns(1);
		if (!param.getTxtTab()) {
			txSeparator.setText(param.getTxtSeparator());
			txSeparator.setVisible(true);
		} else {
			txSeparator.setVisible(false);
		}
		add(txSeparator);
		rbTab.setSelected(param.getTxtTab());
		rbOther.setSelected(!param.getTxtTab());
	}

	public void apply() {
		param.setTxtTab(rbTab.isSelected());
		if (!rbTab.isSelected()) {
			param.setTxtSeparator(txSeparator.getText());
		}
	}

}
