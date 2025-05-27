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
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.Date;
import java.util.List;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.tools.DateUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public class ChronoStrand extends AbstractPanel {

	private static final String TT = "ChronoStrand";

	@SuppressWarnings("unchecked")
	public static int nbScenes(ChronoDate chronoDate, Strand strand) {
		/*LOG.trace(TT + "nbScene("
				+ "chronoDate.date=" + chronoDate.date.toString()
				+ ", strand=" + strand.toString() + ")");*/
		int nb = 0;
		if (chronoDate != null) {
			List<Scene> scenes = chronoDate.mainFrame.project.scenes.getWithDates();
			if (chronoDate.date == null) {
				scenes = (List<Scene>) chronoDate.mainFrame.project.scenes.getList();
			}
			for (Scene scene : scenes) {
				if (scene.getStrand() != null && scene.getStrand().equals(strand)
						&& checkDate(scene.getDateRel(), chronoDate.date)) {
					nb++;
				}
			}
		}
		return nb;
	}

	public static boolean checkDate(Date indate, Date refdate) {
		if (indate == null && refdate == null) {
			return true;
		} else if (indate == null || refdate == null) {
			return false;
		}
		String ins = DateUtil.dateToStandard(indate);
		String res = DateUtil.dateToStandard(refdate);
		res = res.substring(0, res.length() - 3);
		return ins.startsWith(res);
	}

	public final ChronoDate chronoDate;
	public final Strand strand;

	public ChronoStrand(ChronoDate date, Strand strand) {
		super(date.mainFrame);
		this.chronoDate = date;
		this.strand = strand;
		initAll();
	}

	@Override
	public void init() {
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		//LOG.trace(TT + ".initUi()");
		setBorder(SwingUtil.getBorderDefault());
		String migValue = "";
		if (!chronoDate.chrono.vertical) {
			setLayout(new MigLayout(MIG.get(MIG.WRAP1)));
		} else {
			setLayout(new MigLayout(MIG.FLOWX));
			migValue = MIG.get(MIG.GROWX, MIG.WRAP);
		}
		this.setBackground(strand.getJColor());
		List<Scene> scenes = mainFrame.project.scenes.getList();
		for (Scene sc : scenes) {
			if (chronoDate.date == null && sc.getDateRel() == null) {
				add(new SceneSticker(mainFrame, sc));
			} else if (chronoDate.date != null && sc.getDateRel() != null
					&& chronoDate.date.equals(sc.getDateRel())) {
				add(new SceneSticker(mainFrame, sc));
			}
		}
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

}
