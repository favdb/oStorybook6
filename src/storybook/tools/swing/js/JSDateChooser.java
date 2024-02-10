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
package storybook.tools.swing.js;

import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.sql.Timestamp;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import api.jdatechooser.calendar.JDateChooser;
import api.jdatechooser.calendar.JTextFieldDateEditor;
import api.mig.swing.MigLayout;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Pref;
import storybook.tools.DateUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractPanel;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class JSDateChooser extends AbstractPanel {

	private JDateChooser dateToChoose;
	private JSpinner timeSpinner;
	private int showDateTime;//0=show both, 1=show only date, 2=show only time

	public JSDateChooser(MainFrame mainFrame, int showDateTime) {
		super(mainFrame);
		this.showDateTime = showDateTime;
		initAll();
	}

	public boolean hasError() {
		JTextFieldDateEditor tf = (JTextFieldDateEditor) dateToChoose.getComponent(1);
		return tf.getForeground() == Color.red;
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
		//App.trace("JSDateChooser.initUi()");
		setLayout(new MigLayout(MIG.get(MIG.FLOWX, MIG.INS0)));
		dateToChoose = new JDateChooser();
		dateToChoose.setDateFormatString(App.preferences.getString(Pref.KEY.DATEFORMAT, "MM-dd-yyyy"));
		dateToChoose.setMinimumSize(new Dimension(120, 20));
		add(dateToChoose, MIG.TOP);
		if (showDateTime != 1) {
			add(new JLabel(I18N.getColonMsg("time")));
			timeSpinner = new JSpinner(new SpinnerDateModel());
			JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(
					timeSpinner,
					App.preferences.getString(Pref.KEY.TIMEFORMAT));
			timeSpinner.setEditor(timeEditor);
			timeSpinner.setValue(DateUtil.getZeroTimeDate());
			timeSpinner.setPreferredSize(new Dimension(80, 30));
			add(timeSpinner);
			JButton btClear = new JButton(IconUtil.getIconSmall(ICONS.K.CLEAR));
			SwingUtil.setForcedSize(btClear, new Dimension(20, 20));
			btClear.setToolTipText(I18N.getMsg("reset"));
			btClear.addActionListener(getClearTimeAction());
			add(btClear);
		}
	}

	@Override
	public void setEnabled(boolean b) {
		dateToChoose.setEnabled(b);
		if (showDateTime != 1) {
			timeSpinner.setEnabled(b);
		}
	}

	public void setDate(Date date) {
		dateToChoose.setDate(date);
		if (showDateTime != 1) {
			timeSpinner.setValue(date);
		}
	}

	public Date getDate() {
		if (timeSpinner != null) {
			Date time = (Date) timeSpinner.getValue();
			return DateUtil.addDateTime(dateToChoose.getDate(), time);
		}
		return (dateToChoose.getDate());
	}

	public Timestamp getTimestamp() {
		if (dateToChoose.getDate() == null) {
			return null;
		}
		Date date = getDate();
		Date time = null;
		if (timeSpinner != null) {
			time = (Date) timeSpinner.getValue();
		}
		return DateUtil.addDateTimeToTS(date, time);
	}

	private AbstractAction getClearTimeAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timeSpinner.setValue(DateUtil.getZeroTimeDate());
			}
		};
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}
}
