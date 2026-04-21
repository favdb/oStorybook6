/*
 * Copyright (C) 2025 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.ui.panels.chrono;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import storybook.db.scene.Scene;
import storybook.db.scene.SceneSticker;
import storybook.tools.DateUtil;
import storybook.ui.MIG;
import storybook.ui.panels.AbstractPanel;

/**
 * JPanel for a given Date, contains a scroller with scenes
 *
 * @author favdb
 */
public class ChronoRow extends AbstractPanel {

	private static final String TT = "ChronoRow.";

	//private JPanel panel;
	//private JScrollPane scroller;
	private final Date date;
	private char period;
	private final Chrono chrono;
	private int nbScenes;

	public ChronoRow(Chrono chrono, Date date, char period) {
		super(chrono.getMainFrame());
		this.chrono = chrono;
		this.date = date;
		this.period = period;
		initAll();
	}

	/**
	 * initalize the class
	 */
	@Override
	public void init() {
		//LOG.trace(TT + "init() for date=" + (date == null ? "null" : date.toString()));
		//empty
	}

	/**
	 * initialize the user interface
	 */
	@Override
	public void initUi() {
		//LOG.trace(TT + "initUi()");
		String mig = "";//MIG.get(MIG.INS1, MIG.GAP1);
		if (date != null && chrono.vertical) {
			mig = MIG.get(mig, MIG.WRAP1);
		}
		setLayout(new MigLayout(mig));
		if (date != null) {
			TitledBorder border = BorderFactory.createTitledBorder(DateUtil.dateToString(date, false));
			setBorder(border);
		}
		refreshData();
		this.setMinimumSize(SceneSticker.getDefaultSize(chrono.zoom));
	}

	/**
	 * refresh the data
	 */
	public void refreshData() {
		removeAll();
		nbScenes = 0;
		String lb = (date == null ? I18N.getMsg("scene.nodate")
				: DateUtil.dateToString(date, false));
		//LOG.trace(TT + "refreshData() for date=" + lb);
		@SuppressWarnings("unchecked")
		List<Scene> scenes = (List<Scene>) mainFrame.project.scenes.getList();
		for (Scene s : scenes) {
			if (date == null && s.getDateRel() == null) {
				add(new SceneSticker(chrono, s, chrono.zoom));
				nbScenes++;
			} else if (date != null && s.getDateRel() != null) {
				Date d = s.getDateRel();
				switch (period) {
					case 'D':
						d = DateUtil.forDay(d);
						break;
					case 'M':
						d = DateUtil.forMonth(d);
						break;
					case 'Y':
						d = DateUtil.forYear(d);
						break;
					default:
						d = DateUtil.forHour(d);
						break;
				}
				if (date.equals(d)) {
					add(new SceneSticker(chrono, s, chrono.zoom));
					nbScenes++;
				}
			}
		}
	}

	public int getNbScenes() {
		return nbScenes;
	}

	//*********************//
	//** common methodes **//
	//*********************//
	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

}
