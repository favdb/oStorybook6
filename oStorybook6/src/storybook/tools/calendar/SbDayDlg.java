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
import storybook.tools.calendar.SbCalendar.DAY;
import storybook.tools.swing.js.JSTable;
import storybook.dialog.AbsDialog;

/**
 *
 * @author FaVdB
 */
class SbDayDlg extends AbsDialog {

	public List<DAY> lsDays;
	private final int nb;
	private JSTable table;

	SbDayDlg(SbCalendarDlg p, List<DAY> l) {
		this.lsDays = l;
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
		for (int i = 0; i < lsDays.size(); i++) {
			data[i][0] = lsDays.get(i).getName();
			data[i][1] = lsDays.get(i).getAbbr();
		}
		String title[] = {I18N.getMsg("name"), I18N.getMsg("abbreviation")};
		table = new JSTable(data, title);
		JScrollPane scroller = new JScrollPane(table);

		setLayout(new MigLayout("wrap", "", ""));
		setTitle(I18N.getMsg("calendar.day.title"));
		add(scroller, "grow");
		add(getCancelButton(), "span, split 2, right");
		add(getOkButton(), "right");
		pack();
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
					lsDays.get(i).setName((String) table.getModel().getValueAt(i, 0));
					lsDays.get(i).setAbbr((String) table.getModel().getValueAt(i, 1));
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
