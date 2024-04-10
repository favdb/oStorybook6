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
import java.util.List;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSLabel;
import storybook.ui.MIG;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public class ChronoStrand extends AbstractPanel {

	private static final String TT = "ChronoStrand";

	public static int nbScenes(ChronoDate chronoDate, Strand strand) {
		int nb = 0;
		List<Scene> scenes = chronoDate.mainFrame.project.scenes.getWithDates();
		for (Scene scene : scenes) {
			if (scene.getStrand().equals(strand) && (scene.hasDate() && scene.getDate().equals(chronoDate.date))) {
				nb++;
			}
		}
		return nb;
	}

	public final ChronoDate date;
	public final Strand strand;

	public ChronoStrand(ChronoDate date, Strand strand) {
		super(date.mainFrame);
		this.date = date;
		this.strand = strand;
		initAll();
	}

	@Override
	public void init() {
	}

	@Override
	public void initUi() {
		//LOG.trace(TT + ".initUi()");
		setBorder(SwingUtil.getBorderDefault());
		String migValue = "";
		if (!date.chrono.vertical) {
			setLayout(new MigLayout(MIG.get(MIG.WRAP1)));
		} else {
			setLayout(new MigLayout(MIG.FLOWX));
			migValue = MIG.get(MIG.GROWX, MIG.WRAP);
		}
		JSLabel lb = new JSLabel(strand.getName());
		lb.setBackground(strand.getJColor());
		add(lb, migValue);
		List<Scene> scenes = mainFrame.project.scenes.getWithDates();
		for (Scene scene : scenes) {
			if (scene.getStrand().equals(strand) && (scene.hasDate() && scene.getDate().equals(date.date))) {
				add(new ChronoScenePanel(mainFrame, scene));
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
