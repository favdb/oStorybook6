/*
 * Copyright (C) 2019 favdb
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
package storybook.ui.chart.by;

import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.part.Part;
import storybook.db.person.Person;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.project.Project;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.ReadOnlyTable;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.table.TableColorCellRenderer;
import storybook.tools.swing.table.TableFixedColumn;
import storybook.tools.swing.table.TableHeaderCellRenderer;
import storybook.tools.swing.table.TableHeaderMouseListener;
import storybook.tools.swing.table.ToolTipHeader;
import storybook.ui.MainFrame;
import storybook.ui.chart.AbstractPersonsChart;
import storybook.ui.chart.legend.StrandsLegendPanel;

public class PersonsByScene extends AbstractPersonsChart implements ChangeListener {

	public JTable table;
	private JSlider colSlider;
	private JCheckBox cbShowUnusedPersons;
	private int colWidth = 50;

	public PersonsByScene(MainFrame paramMainFrame) {
		super(paramMainFrame, "report.person.scene.title");
		this.partRelated = true;
	}

	@Override
	protected void initChartUi() {
		JLabel label = new JLabel(this.chartTitle);
		label.setFont(FontUtil.getBold());
		table = createTable();
		table.setName(chartTitle);
		TableFixedColumn localFixedColumnScrollPane = new TableFixedColumn(this.table, 1);
		localFixedColumnScrollPane.getRowHeader().setPreferredSize(new Dimension(200, 20));
		panel.setName(I18N.getMsg("report.person.scene.title"));
		panel.add(label, "center");
		panel.add(localFixedColumnScrollPane, "grow, h pref-20");
		panel.add(new StrandsLegendPanel(this.mainFrame), "gap push");
	}

	@Override
	protected void initOptionsUi() {
		super.initOptionsUi();
		this.cbShowUnusedPersons = new JCheckBox();
		this.cbShowUnusedPersons.setSelected(true);
		this.cbShowUnusedPersons.setText(I18N.getMsg("chart.common.unused.characters"));
		this.cbShowUnusedPersons.setOpaque(false);
		this.cbShowUnusedPersons.addActionListener(this);
		this.optionsPanel.add(this.cbShowUnusedPersons, "right,gap push");
		this.optionsPanel.add(new JLabel(IconUtil.getIconSmall(ICONS.K.SIZE)), "gap 20");
		this.colSlider = SwingUtil.createSafeSlider(0, 5, 200, this.colWidth);
		this.colSlider.setMinorTickSpacing(1);
		this.colSlider.setMajorTickSpacing(2);
		this.colSlider.setSnapToTicks(false);
		this.colSlider.addChangeListener(this);
		this.colSlider.setOpaque(false);
		this.optionsPanel.add(this.colSlider);
	}

	@Override
	public void refresh() {
		this.colWidth = this.colSlider.getValue();
		super.refresh();
		this.colSlider.setValue(this.colWidth);
		setTableColumnWidth();
	}

	@SuppressWarnings("unchecked")
	private JTable createTable() {
		//TODO à refaire
		Project project = mainFrame.project;
		Part part = getCbPart();
		List<Person> persons = project.persons.findByCategories(selectedCategories);
		List<Scene> scenes;
		if (part != null) {
			scenes = project.scenes.findByPart(part);
		} else {
			scenes = project.scenes.getList();
		}
		String[] strScenes = new String[scenes.size() + 1];
		strScenes[0] = "";
		int i = 1;
		for (Scene scene : scenes) {
			strScenes[i] = scene.getName();
			i++;
		}
		Object scenesIterator = new ArrayList();
		String[] string2 = new String[scenes.size() + 1];
		Iterator personsIterator = persons.iterator();
		Object localObject6;
		while (personsIterator.hasNext()) {
			Person person = (Person) personsIterator.next();
			int j = 0;
			Object[] string3 = new Object[scenes.size() + 1];
			string3[(j++)] = person.getName();
			int n = 0;
			localObject6 = scenes.iterator();
			while (((Iterator) localObject6).hasNext()) {
				Scene localScene = (Scene) ((Iterator) localObject6).next();
				if (localScene.getPersons().contains(person)) {
					n = 1;
					if (localScene.getStrand() != null) {
						string3[j] = ColorUtil.darker(Strand.getJColor(localScene.getStrand()), 0.05D);
					} else {
						string3[j] = null;
					}
				} else {
					string3[j] = null;
				}
				string2[j] = localScene.getFullTitle();
				j++;
			}
			if ((cbShowUnusedPersons == null) || (cbShowUnusedPersons.isSelected()) || (n != 0)) {
				((List) scenesIterator).add(string3);
			}
		}
		Object[][] localObject31 = new Object[((List) scenesIterator).size()][];
		i = 0;
		Iterator localObject4 = ((List) scenesIterator).iterator();
		while (localObject4.hasNext()) {
			Object[] arrayOfObject1 = (Object[]) localObject4.next();
			localObject31[(i++)] = arrayOfObject1;
		}
		JTable ntable = new ReadOnlyTable((Object[][]) localObject31, strScenes);
		if (ntable.getModel().getRowCount() == 0) {
			return ntable;
		}
		ntable.getColumnModel().getColumn(0).setPreferredWidth(200);
		ntable.getColumnModel().getColumn(0).setCellRenderer(new TableHeaderCellRenderer());
		for (int k = 1; k < ntable.getColumnCount(); k++) {
			int m = ntable.getColumnModel().getColumn(k).getModelIndex();
			Object localObject5 = ntable.getModel().getValueAt(0, m);
			localObject6 = ntable.getColumnModel().getColumn(k);
			if ((localObject5 == null) || (localObject5 instanceof Color)) {
				((TableColumn) localObject6).setPreferredWidth(colWidth);
				((TableColumn) localObject6).setCellRenderer(new TableColorCellRenderer(false));
			}
		}
		ntable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		ntable.getTableHeader().setReorderingAllowed(false);
		ToolTipHeader toolTip = new ToolTipHeader(ntable.getColumnModel());
		toolTip.setToolTipStrings((String[]) string2);
		toolTip.setToolTipText("Default ToolTip TEXT");
		ntable.setTableHeader(toolTip);
		TableHeaderMouseListener mouse = new TableHeaderMouseListener(mainFrame, ntable);
		ntable.getTableHeader().addMouseListener(mouse);
		return ntable;
	}

	@Override
	public void stateChanged(ChangeEvent paramChangeEvent) {
		setTableColumnWidth();
	}

	private void setTableColumnWidth() {
		this.colWidth = this.colSlider.getValue();
		for (int i = 0; i < this.table.getColumnCount(); i++) {
			TableColumn localTableColumn = this.table.getColumnModel().getColumn(i);
			localTableColumn.setPreferredWidth(this.colWidth);
		}
	}

	@Override
	public JPanel getPanelToExport() {
		//TODO remplacer par génération code html
		return panel;
	}

}
