/*
 * Copyright (C) 2017 FaVdB
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
package storybook.tools.calendar;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.text.NumberFormatter;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.dialog.AbsDialog;
import storybook.project.PropertiesDlg;
import storybook.tools.LOG;
import storybook.tools.calendar.SbCalendar.DAY;
import storybook.tools.calendar.SbCalendar.MONTH;
import storybook.tools.html.Html;
import storybook.ui.MIG;

/**
 *
 * @author FaVdB
 */
public class SbCalendarDlg extends AbsDialog {

	public static boolean show(PropertiesDlg p, SbCalendar c) {
		SbCalendarDlg dlg = new SbCalendarDlg(p, c);
		dlg.setVisible(true);
		return (!dlg.canceled);
	}

	private JFormattedTextField tfHours;
	private JFormattedTextField tfMinutes;
	private JFormattedTextField tfSeconds;
	private JFormattedTextField tfStartDay;
	private JLabel lsDays;
	private JLabel lsMonths;
	private SbCalendar calendar;
	private JFormattedTextField tfYearDays;

	public SbCalendarDlg(PropertiesDlg parent, SbCalendar c) {
		super(parent.getMainFrame());
		calendar = c;
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		super.initUi();
		setLayout(new MigLayout(MIG.WRAP, "[]", ""));
		setTitle(I18N.getMsg("calendar"));
		this.setIconImage(IconUtil.getIconImageSmall(ICONS.K.VW_CHRONO));

		NumberFormatter deuxChiffres = new NumberFormatter(NumberFormat.getInstance());
		deuxChiffres.setValueClass(Integer.class);
		deuxChiffres.setMinimum(0);
		deuxChiffres.setMaximum(99);
		deuxChiffres.setAllowsInvalid(false);

		NumberFormatter troisChiffres = new NumberFormatter(NumberFormat.getInstance());
		troisChiffres.setValueClass(Integer.class);
		troisChiffres.setMinimum(0);
		troisChiffres.setMaximum(999);
		troisChiffres.setAllowsInvalid(false);

		JLabel lbYearDays = new JLabel(I18N.getMsg("calendar.yeardays"));
		tfYearDays = new JFormattedTextField(troisChiffres);
		tfYearDays.setColumns(3);
		tfYearDays.setValue(calendar.yeardays);
		add(lbYearDays, MIG.SPLIT2);
		add(tfYearDays);

		lsDays = new JLabel(getListDays());
		JButton btDays = new JButton(I18N.getMsg("calendar.day.title"));
		btDays.addActionListener((ActionEvent evt) -> {
			SbDayDlg dlg = new SbDayDlg(this, calendar.days);
			dlg.setVisible(true);
			if (!dlg.isCanceled()) {
				try {
					//TODO save the days for SbCalendar
				} catch (Exception ex) {
					LOG.err("unable to copy calendar", ex);
					return;
				}
				lsDays.setText(getListDays());
			}
		});
		add(btDays, MIG.SPLIT2);
		add(lsDays);

		lsMonths = new JLabel(getListMonths());
		JButton btMonths = new JButton(I18N.getMsg("calendar.month.title"));
		btMonths.addActionListener((ActionEvent evt) -> {
			SbMonthDlg dlg = new SbMonthDlg(this, calendar.months);
			dlg.setVisible(true);
			if (!dlg.isCanceled()) {
				int z = 0;
				for (MONTH m : dlg.getMonths()) {
					z += m.days;
				}
				lsMonths.setText(getListMonths());
				tfYearDays.setValue(z);
			}
		});
		add(btMonths, MIG.SPLIT2);
		add(lsMonths);

		JLabel lbHours = new JLabel(I18N.getMsg("calendar.hours"));
		tfHours = new JFormattedTextField(deuxChiffres);
		tfHours.setColumns(2);
		tfHours.setValue(calendar.hours);
		add(lbHours, MIG.SPLIT2);
		add(tfHours);

		JLabel lbMinutes = new JLabel(I18N.getMsg("calendar.minutes"));
		tfMinutes = new JFormattedTextField(deuxChiffres);
		tfMinutes.setColumns(2);
		tfMinutes.setValue(calendar.minutes);
		add(lbMinutes, MIG.SPLIT2);
		add(tfMinutes);

		JLabel lbSeconds = new JLabel(I18N.getMsg("calendar.seconds"));
		tfSeconds = new JFormattedTextField(deuxChiffres);
		tfSeconds.setColumns(2);
		tfSeconds.setValue(calendar.seconds);
		add(lbSeconds, MIG.SPLIT2);
		add(tfSeconds);

		JLabel lbStartDay = new JLabel(I18N.getMsg("calendar.startday"));
		tfStartDay = new JFormattedTextField(deuxChiffres);
		tfStartDay.setColumns(2);
		tfStartDay.setValue(calendar.first_day);
		add(lbStartDay, MIG.SPLIT2);
		add(tfStartDay);

		JButton btReinit = new JButton(I18N.getMsg("reinit"));
		btReinit.addActionListener((ActionEvent evt) -> {
			reinit();
			validate();
		});
		add(btReinit, MIG.get(MIG.SPAN, MIG.RIGHT, "split 3"));

		addOkCancel();

		pack();
		setLocationRelativeTo(mainFrame);
		setModal(true);
	}

	public void reinit() {
		lsDays.setText(getListDays());
		lsMonths.setText(getListMonths());
		tfHours.setValue(calendar.hours);
		tfMinutes.setValue(calendar.minutes);
		tfSeconds.setValue(calendar.seconds);
		tfStartDay.setValue(calendar.first_day);
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applySettings();
			}
		};
	}

	private void applySettings() {
		//LOG.trace(TT+"setSettings()");
		calendar.setUse(true);
		calendar.yeardays = Integer.parseInt(tfYearDays.getText());
		calendar.startday = Integer.parseInt(tfStartDay.getText());
		calendar.hours = Integer.parseInt(tfHours.getText());
		calendar.minutes = Integer.parseInt(tfMinutes.getText());
		calendar.seconds = Integer.parseInt(tfSeconds.getText());
		canceled = false;
		dispose();
	}

	public String getListDays() {
		StringBuilder r = new StringBuilder(Html.HTML_B);
		int i = 0;
		for (DAY d : calendar.days) {
			if (!r.toString().equals(Html.HTML_B)) {
				r.append(", ");
			}
			if (i > 0 && i % 5 == 0) {
				r.append(Html.BR);
			}
			r.append(d.getName());
			i++;
		}
		return (r.toString() + Html.HTML_E);
	}

	public String getListMonths() {
		StringBuilder r = new StringBuilder(Html.HTML_B);
		int i = 0;
		for (MONTH m : calendar.months) {
			if (!r.toString().equals(Html.HTML_B)) {
				r.append(", ");
			}
			if (i > 0 && i % 6 == 0) {
				r.append(Html.BR);
			}
			r.append(m.name);
			i++;
		}
		return (r.toString() + Html.HTML_E);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}
}
