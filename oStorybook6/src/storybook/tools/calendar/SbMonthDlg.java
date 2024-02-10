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

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import api.mig.swing.MigLayout;
import i18n.I18N;
import storybook.tools.calendar.SbCalendar.MONTH;
import storybook.tools.swing.js.JSTable;
import storybook.dialog.AbsDialog;

/**
 *
 * @author FaVdB
 */
public class SbMonthDlg extends AbsDialog {

	private List<MONTH> lsMonths;
	private final int nb;
	private JSTable table;

	public SbMonthDlg(SbCalendarDlg p, List<MONTH> l) {
		lsMonths = l;
		this.nb = l.size();
		initAll();
		setLocationRelativeTo(p);
		setModal(true);
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		Object data[][] = new Object[nb][3];
		for (int i = 0; i < lsMonths.size(); i++) {
			data[i][0] = lsMonths.get(i).name;
			data[i][1] = lsMonths.get(i).abbr;
			data[i][2] = lsMonths.get(i).days;
		}
		String title[] = {I18N.getMsg("name"), I18N.getMsg("abbreviation"), I18N.getMsg("calendar.month.nbdays")};
		table = new JSTable(data, title);
		JScrollPane scroller = new JScrollPane(table);

		setLayout(new MigLayout("wrap", "", ""));
		setTitle(I18N.getMsg("calendar.month.title"));
		add(scroller, "grow");
		add(getCancelButton(), "span, split 2, right");
		add(getOkButton(), "right");
		pack();
	}

	public List<MONTH> getMonths() {
		return this.lsMonths;
	}

	public void setMonths(List<MONTH> months) {
		this.lsMonths = months;
	}

	private boolean check() {
		for (int i = 0; i < table.getRowCount(); i++) {
			String n = (String) table.getModel().getValueAt(i, 0);
			if (n == null || n.isEmpty()) {
				error("calendar.error.name");
				return (false);
			}
			n = (String) table.getModel().getValueAt(i, 1);
			if (n == null || n.isEmpty()) {
				error("calendar.error.abbr");
				return (false);
			}
			Object no = table.getModel().getValueAt(i, 2);
			if ((no instanceof String) || ((int) no == 0)) {
				error("calendar.error.days");
				return (false);
			}
		}
		return (true);
	}

	private void error(String msg) {
		JOptionPane.showMessageDialog(this,
				I18N.getMsg(msg),
				I18N.getMsg(I18N.getMsg("error")),
				JOptionPane.YES_OPTION);
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!check()) {
					return;
				}
				canceled = false;
				for (int i = 0; i < table.getRowCount(); i++) {
					lsMonths.get(i).name = (String) table.getModel().getValueAt(i, 0);
					lsMonths.get(i).abbr = (String) table.getModel().getValueAt(i, 1);
					lsMonths.get(i).days = (int) table.getModel().getValueAt(i, 2);
				}
				dispose();
			}
		};
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
