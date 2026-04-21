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
package storybook.ui.chart.occurences;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import storybook.db.category.Category;
import storybook.db.person.Person;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.chart.AbstractPersonsChart;

/**
 *
 * @author favdb
 */
public class OccurrenceOfPersons extends AbstractPersonsChart {

	private OccurencesPanel occurencesPanel;
	private Dataset dataset;

	public OccurrenceOfPersons(MainFrame mainFrame) {
		super(mainFrame, "charts.person_occurrence");
	}

	@Override
	protected void initChartUi() {
		createOccurenceOfPersons();
		occurencesPanel = new OccurencesPanel(chartTitle, "value", " ", dataset);
		this.panel.add(this.occurencesPanel, MIG.GROW);
	}

	public List<Category> getSelectedCategories() {
		return (selectedCategories);
	}

	private void createOccurenceOfPersons() {
		//LOG.trace(TT+"createOccurenceOfPersons()");
		dataset = new Dataset();
		dataset.items = new ArrayList<>();
		List<Person> categories = mainFrame.project.persons.findByCategories(this.selectedCategories);
		double d = 0.0D;
		dataset.maxValue = 0L;
		dataset.idList = new ArrayList<>();
		for (Person person : categories) {
			long l = mainFrame.project.scenes.countBy(person);
			dataset.items.add(new DatasetItem(person.getAbbr(), l, person.getJColor()));
			if (dataset.maxValue < l) {
				dataset.maxValue = l;
			}
			dataset.idList.add(person.getAbbr());
			d += l;
		}
		dataset.average = (d / dataset.items.size());
	}

	@Override
	public JPanel getPanelToExport() {
		return occurencesPanel;
	}

}
