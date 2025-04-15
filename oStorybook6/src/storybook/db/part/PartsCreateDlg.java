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
package storybook.db.part;

import api.jdatechooser.calendar.JDateChooser;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.sql.Timestamp;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import storybook.App;
import storybook.Pref;
import storybook.dialog.AbsDialog;
import storybook.tools.StringUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class PartsCreateDlg extends AbsDialog {

	private JDateChooser dateChooser;
	private JTextField tfQuantity;
	private JTextField tfSize;
	private JTextField tfFormat;
	private JCheckBox rbRoman;

	public PartsCreateDlg(MainFrame parent) {
		super(parent);
		mainFrame = parent;
		initUi();
		this.setLocationRelativeTo(mainFrame);
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		JLabel lb1 = new JLabel(I18N.getMsg("parts.generate.text"));
		tfQuantity = new JTextField();
		tfQuantity.setColumns(2);
		JLabel lbn = new JLabel(I18N.getMsg("parts.generate.format"));
		tfFormat = new JTextField();
		tfFormat.setText(I18N.getMsg("part") + " %d");
		rbRoman = new JCheckBox(I18N.getMsg("parts.generate.roman"));
		JLabel lb3 = new JLabel(I18N.getMsg("objective.size"));
		tfSize = new JTextField();
		tfSize.setColumns(8);
		JLabel lb4 = new JLabel(I18N.getMsg("objective.date"));
		dateChooser = new JDateChooser();
		dateChooser.setDateFormatString(App.preferences.getString(Pref.KEY.DATEFORMAT, "MM-dd-yyyy"));
		//layout
		setLayout(new MigLayout("", "", ""));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(I18N.getMsg("parts.generate"));
		add(lb1, MIG.SPLIT2);
		add(tfQuantity, MIG.WRAP);
		add(lbn, MIG.WRAP);
		add(tfFormat, MIG.get(MIG.GROWX, MIG.WRAP));
		add(rbRoman, MIG.get(MIG.RIGHT, MIG.WRAP));
		add(lb3, MIG.SPLIT2);
		add(tfSize, MIG.WRAP);
		add(lb4, MIG.SPLIT2);
		add(dateChooser, MIG.WRAP);
		add(getCancelButton(), MIG.get(MIG.RIGHT, MIG.SPLIT2));
		add(getOkButton(), MIG.RIGHT);
		pack();
		setLocationRelativeTo(mainFrame);
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int quant = 0;
				try {
					quant = Integer.parseInt(tfQuantity.getText());
				} catch (NumberFormatException evt) {
					// ignore
				}
				if (quant < 1 || quant > 20 || !tfFormat.getText().contains("%d")) {
					showError(I18N.getMsg("parts.generate.format.error"));
					return;
				}
				int size = 0;
				try {
					size = Integer.parseInt(tfSize.getText());
				} catch (NumberFormatException evt) {
				}
				Timestamp xdate = getTimestamp();
				int lastPart = mainFrame.project.parts.getLastNumber();
				for (int i = 0; i < quant; ++i) {
					int number = i + lastPart + 1;
					Part ch = new Part();
					ch.setNumber(number);
					if (size != 0) {
						ch.setObjectiveChars(size);
					}
					if (xdate != null) {
						ch.setObjectiveTime(xdate);
					}
					if (rbRoman.isSelected()) {
						String x = StringUtil.intToRoman((int) ch.getNumber());
						String f = tfFormat.getText().replace("%d", "%s");
						ch.setName(String.format(f, x));
					} else {
						String f = tfFormat.getText().replace("%d", "%02d");
						ch.setName(String.format(f, number));
					}
					mainFrame.getBookController().newEntity(ch);
				}
				canceled = false;
				dispose();
			}

		};
	}

	private void showError(String msg) {
		JOptionPane.showMessageDialog(this, msg, I18N.getMsg("error"), JOptionPane.ERROR_MESSAGE);
	}

	public Timestamp getTimestamp() {
		if (dateChooser.getDate() == null) {
			return null;
		}
		Date date = dateChooser.getDate();
		return (new Timestamp(date.getTime()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
