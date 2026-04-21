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
import storybook.db.item.Item;
import storybook.tools.swing.ColorUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.chart.AbstractChartPanel;

/**
 *
 * @author favdb
 */
public class OccurrenceOfItems extends AbstractChartPanel {

	private OccurencesPanel occurencesPanel;
	private List<JCheckBox> categoriesCB;
	protected List<String> categoriesSel;
	private Dataset dataset;

	public OccurrenceOfItems(MainFrame mainFrame) {
		super(mainFrame, "charts.item_occurrence_title");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void initChart() {
		this.categoriesCB = cbItemCategory(this.mainFrame, this);
		this.categoriesSel = new ArrayList();
		updateSelectedCategories();
	}

	@Override
	protected void initChartUi() {
		dataset = new Dataset();
		createOccurenceOfCategories();
		occurencesPanel = new OccurencesPanel(chartTitle, "value", " ", dataset);
		this.panel.add(this.occurencesPanel, MIG.GROW);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void initOptionsUi() {
		JPanel lp = new JPanel(new MigLayout(MIG.FLOWX));
		lp.setOpaque(false);
		JLabel label = new JLabel(I18N.getColonMsg("category"));
		lp.add(label);
		categoriesCB.clear();
		for (String cat : mainFrame.project.items.findCategories()) {
			JCheckBox cb = new JCheckBox(cat);
			cb.setSelected(true);
			categoriesCB.add(cb);
			lp.add(cb);
		}
		updateSelectedCategories();
		optionsPanel.add(lp);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		updateSelectedCategories();
		refreshChart();
	}

	private void updateSelectedCategories() {
		categoriesSel.clear();
		for (JCheckBox cb : categoriesCB) {
			if (cb.isSelected()) {
				categoriesSel.add(cb.getText());
			}
		}
	}

	public List<String> getSelectedCategories() {
		return categoriesSel;
	}

	private void createOccurenceOfCategories() {
		dataset.items.clear();
		@SuppressWarnings("unchecked")
		List<Item> items = (List) mainFrame.project.items.findByCategory();
		double d = 0.0D;
		Color[] color = ColorUtil.getNiceColors();
		dataset.maxValue = 0L;
		dataset.idList = new ArrayList<>();
		int ncolor = 0;
		for (Item item : items) {
			if (categoriesCB.isEmpty() || categoriesSel.contains(item.getCategory())) {
				long l = mainFrame.project.scenes.countBy(item);
				dataset.items.add(new DatasetItem(item.getAbbr(), l, color[ncolor]));
				if (dataset.maxValue < l) {
					dataset.maxValue = l;
				}
				dataset.idList.add(item.getAbbr());
				ncolor++;
				if (ncolor >= color.length) {
					ncolor = 0;
				}
				d += l;
			}
		}
		if (!items.isEmpty()) {
			dataset.average = (d / items.size());
		}
	}

	@Override
	public JPanel getPanelToExport() {
		return occurencesPanel;
	}

	/**
	 * get a JCheckBox for Item Categories
	 *
	 * @param mainFrame
	 * @param comp
	 * @return
	 */
	public List<JCheckBox> cbItemCategory(MainFrame mainFrame, ActionListener comp) {
		List<JCheckBox> list = new ArrayList<>();
		List<String> categories = mainFrame.project.items.findCategories();
		for (String category : categories) {
			JCheckBox chb = new JCheckBox(category);
			chb.setOpaque(false);
			chb.addActionListener(comp);
			chb.setSelected(true);
			list.add(chb);
		}
		return list;
	}

}
