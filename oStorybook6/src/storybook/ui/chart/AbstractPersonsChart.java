/*
 * SbApp: Open Source software for novelists and authors.
 * Original idea 2008 - 2012 Martin Mustun
 * Copyrigth (C) Favdb
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
 */
package storybook.ui.chart;

import i18n.I18N;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import storybook.db.category.Category;
import storybook.db.category.Categorys;
import storybook.ui.MainFrame;

public abstract class AbstractPersonsChart extends AbstractChartPanel {

	protected List<JCheckBox> categoryCbList;
	protected List<Category> selectedCategories;

	public AbstractPersonsChart(MainFrame mainFrame, String str) {
		super(mainFrame, str);
	}

	@Override
	protected void initChart() {
		this.categoryCbList = Categorys.cbCategory(this.mainFrame, this);
		this.selectedCategories = new ArrayList<>();
		updateSelectedCategories();
	}

	@Override
	protected void initOptionsUi() {
		JLabel label = new JLabel(I18N.getColonMsg("category_s"));
		this.optionsPanel.add(label, "split " + this.categoryCbList.size() + 1);
		for (JCheckBox localJCheckBox : this.categoryCbList) {
			this.optionsPanel.add(localJCheckBox);
		}
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		updateSelectedCategories();
		refreshChart();
	}

	private void updateSelectedCategories() {
		this.selectedCategories.clear();
		for (JCheckBox cb : this.categoryCbList) {
			if (cb.isSelected()) {
				Category cat = (Category) cb.getClientProperty("CbCategory");
				this.selectedCategories.add(cat);
			}
		}
	}
}
