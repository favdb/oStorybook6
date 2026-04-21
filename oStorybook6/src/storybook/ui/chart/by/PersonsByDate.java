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

import i18n.I18N;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import storybook.db.category.Category;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.person.Person;
import storybook.db.scene.Scene;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.chart.AbstractPersonsChart;
import storybook.ui.chart.occurences.Dataset;
import storybook.ui.chart.occurences.DatasetItem;
import storybook.ui.chart.occurences.DureeScene;
import storybook.ui.chart.occurences.OccurencesPanel;

/**
 *
 * @author favdb
 */
public class PersonsByDate extends AbstractPersonsChart {

	private OccurencesPanel timelinePanel;
	private Dataset dataset;

	public PersonsByDate(MainFrame mainFrame) {
		super(mainFrame, "charts.person_by_date");
		this.partRelated = true;
	}

	@Override
	protected void initChartUi() {
		dataset = new Dataset();
		createPersonsDate();
		if (dataset.items.isEmpty()) {
			panel.removeAll();
			panel.add(new JLabel(I18N.getMsg("warning.chronology")));
			return;
		}
		timelinePanel = new OccurencesPanel(chartTitle, "date", "persons", dataset);
		this.panel.add(timelinePanel, MIG.GROW);
	}

	public List<Category> getSelectedCategories() {
		return selectedCategories;
	}

	@SuppressWarnings("unchecked")
	public void createPersonsDate() {
		dataset.items.clear();
		Part part = getCbPart();
		List<Person> persons = mainFrame.project.persons.findByCategories(this.selectedCategories);
		List<Chapter> chapters;
		if (part == null) {
			chapters = (List<Chapter>) mainFrame.project.chapters.getList();
		} else {
			chapters = mainFrame.project.chapters.find(part);
		}
		List<DureeScene> durees = DureeScene.initScenes(mainFrame);
		if (durees.isEmpty()) {
			return;
		}
		for (Person p : persons) {
			for (Chapter chapter : chapters) {
				List<Scene> scenes = mainFrame.project.scenes.find(chapter);
				for (Scene scene : scenes) {
					List<Person> lpersons = scene.getPersons();
					if (!lpersons.isEmpty() && lpersons.contains(p)) {
						for (DureeScene d : durees) {
							if (d.id == scene.getId()) {
								dataset.items.add(new DatasetItem(p.getAbbr(), d.debut, d.fin, p.getJColor()));
							}
						}
					}
				}
			}
		}
	}

	@Override
	public JPanel getPanelToExport() {
		return timelinePanel;
	}

}
