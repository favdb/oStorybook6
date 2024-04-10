/*
 * Copyright (C) 2020 favdb
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
 * You should have received a fileCopy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.exim.exporter;

import i18n.I18N;
import java.awt.Color;
import java.awt.HeadlessException;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import storybook.exim.EXIM;
import storybook.tools.LOG;
import storybook.tools.file.IOUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.ScreenImage;
import storybook.ui.MainFrame;
import storybook.ui.SbView;
import storybook.ui.SbViewFactory;
import storybook.ui.chart.AbstractChartPanel;
import storybook.ui.chart.by.PersonsByScene;
import storybook.ui.chart.wiww.WiWW;
import storybook.ui.panel.info.InfoPanel;
import storybook.ui.panel.memoria.MemoriaPanel;

/**
 *
 * @author favdb
 */
public class Exporter {

	private static final String TT = "Exporter.";

	private final MainFrame mainFrame;
	private SbView view;

	public Exporter(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public boolean exec(SbView view) {
		//LOG.trace(TT + "exec(view=" + (view != null ? view.getName() : "null") + ")");
		this.view = view;
		if (view == null) {
			return (false);
		}
		if (SbViewFactory.isTable(view)) {
			ExportSTable.export(mainFrame, view);
			return (true);
		}
		if (!allowed()) {
			//LOG.trace("view not allowed to export");
			return (false);
		}
		JTable t;
		switch (SbView.getVIEW(view)) {
			case CHART_OCCURRENCE_OF_ITEMS:
			case CHART_OCCURRENCE_OF_LOCATIONS:
			case CHART_OCCURRENCE_OF_PERSONS:
			case CHART_PERSONS_BY_DATE:
			case CHART_STRANDS_BY_DATE:
				AbstractChartPanel pAbstractChart = (AbstractChartPanel) view.getComponent();
				EXIM.exporter(mainFrame, pAbstractChart.getPanelToExport());
				break;
			case CHART_PERSONS_BY_SCENE:
				PersonsByScene pChartPersonsByScene = (PersonsByScene) view.getComponent();
				t = pChartPersonsByScene.table;
				exportJTable(pChartPersonsByScene, t);
				break;
			case CHART_WIWW:
				WiWW wiww = (WiWW) view.getComponent();
				t = wiww.table;
				exportJTable(wiww, t);
				break;
			case EPISODES:
				ExportEpisode.toFile(mainFrame, "html");
				break;
			case INFO:
				ExportInfoView exportInfo = new ExportInfoView(mainFrame);
				exportInfo.exec((InfoPanel) view.getComponent());
				break;
			case MEMORIA:
				MemoriaPanel memoria = (MemoriaPanel) view.getComponent();
				EXIM.exporter(mainFrame, memoria.getPanelToExport());
				break;
			case PLAN:
				break;
			case TREE:
				break;
			default:
				break;
		}
		return true;
	}

	private boolean allowed() {
		switch (SbView.getVIEW(view)) {
			case CHART_OCCURRENCE_OF_ITEMS:
			case CHART_OCCURRENCE_OF_LOCATIONS:
			case CHART_OCCURRENCE_OF_PERSONS:
			case CHART_PERSONS_BY_DATE:
			case CHART_PERSONS_BY_SCENE:
			case CHART_STRANDS_BY_DATE:
			case CHART_WIWW:
			case EPISODES:
			case INFO:
			case MEMORIA:
			case PLAN:
			case TREE:
				return (true);
			default:
				return (false);
		}
	}

	private void success(JPanel panel, String file) {
		JOptionPane.showMessageDialog(panel,
		   file + "\n" + I18N.getMsg("export.success"),
		   I18N.getMsg("export"), 1);
	}

	/**
	 * exprt a JPanel as a PNG Image file
	 *
	 * @param panel
	 * @return
	 */
	public boolean exportPanel(JPanel panel) {
		//LOG.trace(TT+"exportPanel(panel="+(panel!=null?panel.getName():"null")+")");
		if (panel == null) {
			LOG.log("unable to export a null panel");
			return (false);
		}
		try {
			String filename = EXIM.getFileName(mainFrame, panel.getName(), "png");
			if (!EXIM.askExists(panel, filename)) {
				return false;
			}
			ScreenImage.createImage(panel, filename);
			success(panel, filename);
		} catch (HeadlessException | IOException ex) {
			return false;
		}
		return true;
	}

	private void exportJTable(JPanel p, JTable table) {
		//LOG.trace(TT+"exportJTable(table="+(table!=null?table.getName():"null")+")");
		String filename = EXIM.getFileName(mainFrame, table.getName(), "html");
		if (!EXIM.askExists(p, filename)) {
			return;
		}
		StringBuilder b = new StringBuilder();
		b.append(Html.HTML_B);
		b.append(Html.HEAD_B);
		b.append(Html.intoTag("title", table.getName()));
		b.append(Html.HEAD_E);
		b.append(Html.BODY_B);
		b.append(Html.intoH(1, table.getName(), Html.AL_CENTER));
		b.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"0\">\n");
		b.append(Html.TR_B);
		TableModel model = table.getModel();
		for (int c = 0; c < model.getColumnCount(); c++) {
			TableColumn column = table.getColumn(filename);
			if (column.getPreferredWidth() > 0) {
				b.append(Html.TD_B);
				b.append(model.getColumnName(c));
				b.append(Html.BR).append("c=").append(c);
				b.append(Html.TD_E);
			}
		}
		b.append(Html.TR_E);
		for (int r = 0; r < table.getRowCount(); r++) {
			b.append(Html.TR_B);
			for (int c = 0; c < table.getColumnCount(); c++) {
				Object o = model.getValueAt(r, c);
				if (o instanceof Color) {
					b.append("    <td style=\"background-color:#")
					   .append(ColorUtil.getHexName((Color) o))
					   .append("\">");
				} else {
					b.append(Html.TD_B);
				}
				String v = "&nbsp;" + r + "," + c;
				if (o instanceof String) {
					v = (String) o;
				}
				b.append(v);
				b.append(Html.TD_E);
			}
			b.append(Html.TR_E);
		}
		b.append(Html.TABLE_E);
		b.append(Html.BODY_E);
		b.append(Html.HTML_E);
		IOUtil.fileWriteString(filename, b.toString());
	}

}
