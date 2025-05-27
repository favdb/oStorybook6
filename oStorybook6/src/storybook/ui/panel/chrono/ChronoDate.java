/*
 * Copyright (C) 2022 favdb
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
package storybook.ui.panel.chrono;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.Date;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import storybook.db.book.Book;
import storybook.db.strand.Strand;
import storybook.tools.DateUtil;
import storybook.tools.swing.js.JSLabelVertical;
import storybook.ui.MIG;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public class ChronoDate extends AbstractPanel {

	private static final String TT = "ChronoDate.";

	public final Date date;
	public final Chrono chrono;

	public ChronoDate(Chrono chrono, Date date) {
		super(chrono.mainFrame);
		this.chrono = chrono;
		this.date = date;
		initAll();
	}

	@Override
	public void init() {
	}

	@Override
	public void initUi() {
		//LOG.printInfos(TT + ".initUi()");
		String migValue = "";
		if (chrono.vertical) {
			setLayout(new MigLayout());
			migValue = MIG.TOP;
		} else {
			setLayout(new MigLayout(MIG.get(MIG.WRAP1)));
			migValue = MIG.get(MIG.SPAN, MIG.GROWX);
		}
		String lbVal = I18N.getMsg("scene.nodate");
		if (date != null) {
			lbVal = DateUtil.dateToString(date, true);
		} else {
			this.setBackground(Color.red);
		}
		JLabel lbDate = new JLabel(lbVal);
		lbDate.setHorizontalAlignment(SwingConstants.CENTER);
		if (chrono.vertical) {
			lbDate.setUI(new JSLabelVertical(false));
		}
		add(lbDate, migValue);
		@SuppressWarnings("unchecked")
		List<Strand> strands = (List) mainFrame.project.getList(Book.TYPE.STRAND);
		for (Strand strand : strands) {
			if (ChronoStrand.nbScenes(this, strand) > 0) {
				add(new ChronoStrand(this, strand), migValue);
			}
		}
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	}

}
