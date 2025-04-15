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
package storybook.db.chapter;

import api.jdatechooser.calendar.JDateChooser;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.sql.Timestamp;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import storybook.App;
import storybook.Pref;
import storybook.db.EntityUtil;
import storybook.db.book.Book;
import storybook.db.part.Part;
import storybook.dialog.AbsDialog;
import storybook.tools.StringUtil;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class ChaptersCreateDlg extends AbsDialog {

	private JDateChooser dateChooser;
	private JTextField tfQuantity;
	private JComboBox<Object> partCombo;
	private JTextField tfSize;
	private JTextField tfFormat;
	private JCheckBox rbRoman;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public ChaptersCreateDlg(MainFrame parent) {
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
		setLayout(new MigLayout("", "", ""));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(I18N.getMsg("chapters.generate"));
		String split2 = "split 2";
		String wrap = "wrap";
		JLabel lb1 = new JLabel(I18N.getMsg("chapters.generate.text"));
		tfQuantity = new JTextField();
		tfQuantity.setColumns(2);
		JLabel lb2 = new JLabel(I18N.getMsg("part"));
		partCombo = new JComboBox<>();
		Part part = new Part();
		EntityUtil.cbFill(mainFrame, partCombo, Book.TYPE.PART, part, false, false);
		JLabel lbn = new JLabel(I18N.getMsg("chapters.generate.format"));
		tfFormat = new JTextField();
		tfFormat.setText(I18N.getMsg("chapter") + " %d");
		rbRoman = new JCheckBox(I18N.getMsg("chapters.generate.roman"));
		JLabel lb3 = new JLabel(I18N.getMsg("objective.size"));
		tfSize = new JTextField();
		tfSize.setColumns(8);
		JLabel lb4 = new JLabel(I18N.getMsg("objective.date"));
		dateChooser = new JDateChooser();
		dateChooser.setDateFormatString(App.preferences.getString(Pref.KEY.DATEFORMAT, "MM-dd-yyyy"));
		//layout
		add(lb1, split2);
		add(tfQuantity, wrap);
		add(lbn, wrap);
		add(tfFormat, "growx," + wrap);
		add(rbRoman, "right," + wrap);
		add(lb2, split2);
		add(partCombo, wrap);
		add(lb3, split2);
		add(tfSize, wrap);
		add(lb4, split2);
		add(dateChooser, wrap);
		add(getCancelButton(), split2 + ", right");
		add(getOkButton(), "right");
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
				if (quant < 1 || quant > 20) {
					showError(I18N.getMsg("chapters.generate.number.error"));
					return;
				}
				if (!tfFormat.getText().contains("%d")) {
					showError(I18N.getMsg("chapters.generate.format.error"));
					return;
				}
				int size = 0;
				try {
					size = Integer.parseInt(tfSize.getText());
				} catch (NumberFormatException evt) {
				}
				Timestamp xdate = getTimestamp();
				Part part = (Part) partCombo.getSelectedItem();
				int lastChapter = mainFrame.project.chapters.getLastNumber();
				for (int i = 0; i < quant; ++i) {
					int number = i + lastChapter + 1;
					Chapter ch = new Chapter();
					ch.setChapterno(number);
					ch.setPart(part);
					if (size != 0) {
						ch.setObjectiveChars(size);
					}
					if (xdate != null) {
						ch.setObjectiveTime(xdate);
					}
					if (rbRoman.isSelected()) {
						String x = StringUtil.intToRoman((int) ch.getChapterno());
						String f = tfFormat.getText().replace("%d", "%s");
						ch.setTitle(String.format(f, x));
					} else {
						String f = tfFormat.getText().replace("%d", "%02d");
						ch.setTitle(String.format(f, number));
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
