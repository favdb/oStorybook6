/*
 * Copyright (C) 2016 favdb
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
package storybook.ui.chart.by;

import java.util.ArrayList;
import java.util.List;
import storybook.db.category.Category;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.project.Project;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.chart.AbstractPersonsChart;
import storybook.ui.chart.occurences.Dataset;
import storybook.ui.chart.occurences.DatasetItem;
import storybook.ui.chart.occurences.DureeScene;
import storybook.ui.chart.occurences.OccurencesPanel;

/**
 *
 * @author favdb
 */
public class StrandsByDate extends AbstractPersonsChart {

	private OccurencesPanel occurencesPanel;
	private Dataset dataset;

	public StrandsByDate(MainFrame mainFrame) {
		super(mainFrame, "tools.charts.overall.strand.date");
		this.partRelated = true;
	}

	@Override
	protected void initChartUi() {
		dataset = new Dataset();
		createStrandsDate();
		occurencesPanel = new OccurencesPanel(chartTitle, "date", "strands", dataset);
		this.panel.add(occurencesPanel, MIG.GROW);
	}

	public List<Category> getSelectedCategories() {
		return selectedCategories;
	}

	@SuppressWarnings("unchecked")
	private void createStrandsDate() {
		Project project = mainFrame.project;
		dataset.items.clear();
		Part part = getCbPart();
		@SuppressWarnings("unchecked")
		List<Strand> strands = project.strands.getList();
		List<Chapter> chapters;
		if (part != null) {
			chapters = project.chapters.find(part);
		} else {
			chapters = (List<Chapter>) project.chapters.getList();
		}
		for (Strand strand : strands) {
			List<DureeScene> dureeScene = new ArrayList<>();
			for (Chapter chapter : chapters) {
				@SuppressWarnings("unchecked")
				List<Scene> scenes = project.scenes.find(chapter);
				for (Scene scene : scenes) {
					DureeScene d = new DureeScene(mainFrame, scene);
					if (d.debut != null) {
						dureeScene.add(d);
					}
				}
			}
			if (!dureeScene.isEmpty()) {
				dureeScene = DureeScene.calculDureeScene(mainFrame, dureeScene);
				for (DureeScene d : dureeScene) {
					dataset.items.add(new DatasetItem(strand.getName(), d.debut, d.fin, strand.getJColor()));
				}
			}
		}
	}

}
