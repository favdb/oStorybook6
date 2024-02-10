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
package storybook.dialog.chooser;

import api.jdatechooser.calendar.JDateChooser;
import api.jdatechooser.calendar.JTextFieldDateEditor;
import api.mig.swing.MigLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.sql.Timestamp;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Pref;
import storybook.tools.DateUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class DateChooserPanel extends JPanel {

	private final MainFrame mainFrame;
	private JDateChooser dateChooser;
	private JSpinner timeSpinner;
	private JButton btPrevDay;
	private JButton btNextDay;
	private JButton btLastDate;
	private JButton btFirstDate;
	private JButton btClearTime;

	public DateChooserPanel(MainFrame parent) {
		mainFrame = parent;
		init();
	}

	private void init() {
		dateChooser = new JDateChooser();
		timeSpinner = new javax.swing.JSpinner();
		JSpinner.DateEditor timeEditor
		   = new JSpinner.DateEditor(
			  timeSpinner,
			  App.preferences.getString(Pref.KEY.TIMEFORMAT));
		timeSpinner.setEditor(timeEditor);
		timeSpinner.setValue(DateUtil.getZeroTimeDate());
		timeSpinner.setPreferredSize(new Dimension(80, 30));
		btPrevDay = new javax.swing.JButton();
		btPrevDay.setIcon(IconUtil.getIconSmall(ICONS.K.NAV_PREV));
		btPrevDay.addActionListener((java.awt.event.ActionEvent evt) -> {
			Date date;
			if (dateChooser.getDate() == null) {
				date = mainFrame.project.scenes.findDateFirst();
			} else {
				date = DateUtil.addDays(dateChooser.getDate(), -1);
			}
			dateChooser.setDate(date);
		});

		btNextDay = new javax.swing.JButton();
		btNextDay.setIcon(IconUtil.getIconSmall(ICONS.K.NAV_NEXT));
		btNextDay.addActionListener((java.awt.event.ActionEvent evt) -> {
			Date date;
			if (dateChooser.getDate() == null) {
				date = mainFrame.project.scenes.findDateLast();
			} else {
				date = DateUtil.addDays(dateChooser.getDate(), 1);
			}
			dateChooser.setDate(date);
		});
		btLastDate = new javax.swing.JButton();
		btLastDate.setIcon(IconUtil.getIconSmall(ICONS.K.NAV_LAST));
		btLastDate.addActionListener((java.awt.event.ActionEvent evt) -> {
			Date date = mainFrame.project.scenes.findDateLast();
			dateChooser.setDate(date);
		});
		btFirstDate = new javax.swing.JButton();
		btFirstDate.setIcon(IconUtil.getIconSmall(ICONS.K.NAV_FIRST));
		btFirstDate.addActionListener((java.awt.event.ActionEvent evt) -> {
			Date date = mainFrame.project.scenes.findDateFirst();
			dateChooser.setDate(date);
		});
		btClearTime = new javax.swing.JButton();
		btClearTime.setIcon(IconUtil.getIconSmall(ICONS.K.CLEAR));
		//btClearTime.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
		btClearTime.addActionListener((java.awt.event.ActionEvent evt) -> {
			timeSpinner.setValue(DateUtil.getZeroTimeDate());
		});
		//layout
		setLayout(new MigLayout(MIG.INS0));
		add(dateChooser);
		add(btFirstDate);
		add(btPrevDay);
		add(btNextDay);
		add(btLastDate);
		add(timeSpinner);
		add(btClearTime);
	}

	public void setDate(Date date) {
		dateChooser.setDate(date);
		if (date != null && timeSpinner.isVisible()) {
			timeSpinner.setValue(date);
		}
	}

	public Date getDate() {
		return (dateChooser.getDate());
	}

	public Timestamp getTimestamp() {
		if (dateChooser.getDate() == null) {
			return null;
		}
		Date date = dateChooser.getDate();
		Date time = (Date) timeSpinner.getValue();
		return DateUtil.addDateTimeToTS(date, time);
	}

	public void hideTime() {
		timeSpinner.setVisible(false);
		btClearTime.setVisible(false);
	}

	public void hideButtons() {
		btFirstDate.setVisible(false);
		btLastDate.setVisible(false);
		btPrevDay.setVisible(false);
		btNextDay.setVisible(false);

	}

	public void showOnlyDate() {
		hideTime();
		hideButtons();
	}

	public boolean hasError() {
		JTextFieldDateEditor tf = (JTextFieldDateEditor) dateChooser.getComponent(1);
		return tf.getForeground() == Color.red;
	}

	@Override
	public void setEnabled(boolean enabled) {
		dateChooser.setEnabled(enabled);
		timeSpinner.setEnabled(enabled);
		btClearTime.setEnabled(enabled);
		btFirstDate.setEnabled(enabled);
		btLastDate.setEnabled(enabled);
		btPrevDay.setEnabled(enabled);
		btNextDay.setEnabled(enabled);
	}
}
