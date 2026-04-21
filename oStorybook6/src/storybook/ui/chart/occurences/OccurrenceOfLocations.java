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

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import storybook.db.location.Location;
import storybook.tools.swing.ColorUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.chart.AbstractChartPanel;

/**
 *
 * @author favdb
 */
public class OccurrenceOfLocations extends AbstractChartPanel implements ActionListener {

	private OccurencesPanel occurencesPanel;
	private List<JCheckBox> countryCbList;
	protected List<String> selCountries;
	private Dataset dataset;

	public OccurrenceOfLocations(MainFrame mainFrame) {
		super(mainFrame, "charts.location_occurrence_title");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void initChart() {
		this.countryCbList = mainFrame.project.locations.cbCountry(this);
		this.selCountries = new ArrayList();
		updateSelectedCountries();
	}

	@Override
	protected void initChartUi() {
		dataset = new Dataset();
		createOccurenceOfLocations();
		occurencesPanel = new OccurencesPanel(chartTitle, "value", " ", dataset);
		this.panel.add(this.occurencesPanel, MIG.GROW);
	}

	@Override
	protected void initOptionsUi() {
		JPanel lp = new JPanel(new MigLayout(MIG.FLOWX));
		lp.setOpaque(false);
		lp.add(new JLabel(I18N.getColonMsg("location.country")));
		for (JCheckBox localJCheckBox : this.countryCbList) {
			lp.add(localJCheckBox);
		}
		this.optionsPanel.add(lp);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		//empty
	}

	private void updateSelectedCountries() {
		this.selCountries.clear();
		for (JCheckBox localJCheckBox : this.countryCbList) {
			if (localJCheckBox.isSelected()) {
				this.selCountries.add(localJCheckBox.getText());
			}
		}
	}

	public List<String> getSelectedCountries() {
		return selCountries;
	}

	@SuppressWarnings("unchecked")
	private void createOccurenceOfLocations() {
		dataset.items.clear();
		List<Location> locations;
		if (countryCbList.isEmpty()) {
			locations = (List<Location>) mainFrame.project.locations.getList();
		} else {
			locations = mainFrame.project.locations.findByCountries(selCountries);
		}
		double d = 0.0D;
		Color[] color = ColorUtil.getNiceColors();
		dataset.maxValue = 0L;
		dataset.idList.clear();
		int ncolor = 0;
		for (Location location : locations) {
			long l = mainFrame.project.scenes.countBy(location);
			dataset.items.add(new DatasetItem(location.getAbbr(), l, color[ncolor]));
			if (dataset.maxValue < l) {
				dataset.maxValue = l;
			}
			dataset.idList.add(location.getAbbr());
			ncolor++;
			if (ncolor >= color.length) {
				ncolor = 0;
			}
			d += l;
		}
		dataset.average = (d / locations.size());
	}

	@Override
	public JPanel getPanelToExport() {
		return occurencesPanel;
	}

}
