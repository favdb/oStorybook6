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
package storybook.action;

import i18n.I18N;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JPanel;
import storybook.db.strand.Strand;
import storybook.tools.ViewUtil;
import storybook.ui.panel.AbstractPanel;

/**
 * @author martin
 *
 */
public class ScrollToStrandDateAction implements ActionListener {

	private boolean found = false;
	private final AbstractPanel container;
	private final JPanel panel;
	private final Strand strand;
	private final Date date;
	private final JLabel lbWarning;

	public ScrollToStrandDateAction(AbstractPanel container, JPanel panel,
		Strand strand, Date date, JLabel lbWarning) {
		this.container = container;
		this.panel = panel;
		this.strand = strand;
		this.date = date;
		this.lbWarning = lbWarning;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		found = ViewUtil.scrollToStrandDate(container, panel, strand, date);
		if (!found) {
			lbWarning.setText(I18N.getMsg("navigation.date.not.found"));
		} else {
			lbWarning.setText(" ");
		}
		lbWarning.setVisible(!found);
	}

	public boolean isFound() {
		return found;
	}
}
