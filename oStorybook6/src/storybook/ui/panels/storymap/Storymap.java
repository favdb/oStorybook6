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
package storybook.ui.panels.storymap;

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import storybook.App;
import storybook.Pref;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import static storybook.ctrl.Ctrl.PROPS.EXPORT;
import storybook.db.book.Book;
import storybook.db.strand.Strand;
import storybook.exim.EXIM;
import storybook.tools.LOG;
import storybook.tools.print.ComponentPrinter;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.panels.AbstractPanel;

/**
 * the panel for the Storymap view
 *
 * @author favdb
 */
public class Storymap extends AbstractPanel implements ItemListener {

	private static final String TT = "Storymap.",
			CB_TYPE = "cbType", CB_ZOOM = "cbZoom",
			CK_HORIZONTAL = "ckHorizontal", CK_TITLE = "ckTitle";

	public Map<String, String> labelMap;
	public Map<String, Icon> iconMap;
	private VisualizationViewer<String, Number> vv;
	public Strand curStrand;
	private StorymapGraph graph;
	private JScrollPane scroll;
	private JComboBox cbTypes;
	private JComboBox cbZoom;
	private JCheckBox ckHorizontal;
	private JCheckBox ckTitle;

	public Storymap(MainFrame mainFrame) {
		super(mainFrame);
	}

	@Override
	public void init() {
		//LOG.trace(TT + ".init()");
	}

	@Override
	public void initUi() {
		//LOG.trace(TT + ".initUi()");
		setLayout(new MigLayout(MIG.get(MIG.INS0, MIG.FILLX)));
		initToolbar();
		add(toolbar, MIG.get(MIG.SPAN, MIG.GROWX));
		graph = new StorymapGraph(this);
		scroll = new JScrollPane(graph);
		SwingUtil.setMaxPreferredSize(scroll);
		add(scroll, MIG.get(MIG.GROW, MIG.SPAN));
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//LOG.trace(TT+"modelPropertyChange(evt=" + evt.toString() + ")");
		String propName = evt.getPropertyName();
		if (propName == null || "SHOWINFO".equalsIgnoreCase(propName)) {
			return;
		}
		View newView;
		View oldView;
		try {
			switch (Ctrl.getPROPS(propName)) {
				case REFRESH:
					newView = (View) evt.getNewValue();
					oldView = (View) getParent().getParent();
					if (oldView == newView) {
						refresh();
					}
					return;
				case EXPORT:
					newView = (View) evt.getNewValue();
					oldView = (View) getParent().getParent();
					if (newView == oldView) {
						EXIM.exporter(mainFrame, vv);
					}
					return;
				case PRINT:
					newView = (View) evt.getNewValue();
					oldView = (View) getParent().getParent();
					if (newView == oldView) {
						printAction();
					}
					return;
				default:
					if ((propName.startsWith("Update"))
							|| (propName.startsWith("Delete"))
							|| (propName.startsWith("New"))) {
						refresh();
					}
					break;
			}
			ActKey act = new ActKey(evt);
			switch (Book.getTYPE(act.type)) {
				case SCENE:
				case STRAND:
					if (Ctrl.PROPS.NEW.check(act.getCmd())
							|| Ctrl.PROPS.UPDATE.check(act.getCmd())
							|| Ctrl.PROPS.DELETE.check(act.getCmd())) {
						refresh();
					}
					break;
				default:
					break;
			}
		} catch (Exception exc) {
			LOG.err(TT + "modelPropertyChange(" + evt.toString() + ")", exc);
		}
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		//LOG.trace(TT + ".actionPerformed(evt=" + evt.toString() + ")");
		if (evt.getSource() instanceof JCheckBox) {
			JCheckBox ck = (JCheckBox) evt.getSource();
			switch (ck.getName()) {
				case CK_HORIZONTAL:
				case CK_TITLE:
					saveParam();
					refresh();
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void refresh() {
		graph.refresh();
	}

	@Override
	@SuppressWarnings("unchecked")
	public JToolBar initToolbar() {
		super.initToolbar();
		String param = App.preferences.getString(Pref.KEY.STORYMAP);
		while (param.length() < 5) {
			param += '0';
		}
		// type selection: scenes (default) or chapters
		toolbar.add(new JLabel(I18N.getColonMsg("types")));
		cbTypes = new JComboBox();
		cbTypes.setName(CB_TYPE);
		cbTypes.addItem(I18N.getMsg("scenes"));
		cbTypes.addItem(I18N.getMsg("chapters"));
		cbTypes.setSelectedIndex(param.charAt(0) == '1' ? 1 : 0);
		cbTypes.addItemListener(this);
		toolbar.add(cbTypes);
		// zoom selection: small, medium, large
		toolbar.add(new JLabel(I18N.getColonMsg("zoom")));
		cbZoom = new JComboBox();
		cbZoom.setName(CB_ZOOM);
		cbZoom.addItem(I18N.getMsg("icon.size.small"));
		cbZoom.addItem(I18N.getMsg("icon.size.medium"));
		cbZoom.addItem(I18N.getMsg("icon.size.large"));
		cbZoom.addItem(I18N.getMsg("icon.size.tall"));
		int n = param.charAt(1) - '0';
		if (n >= 0 && n <= 3) {
			cbZoom.setSelectedIndex(n);
		}
		cbZoom.addItemListener(this);
		toolbar.add(cbZoom);
		// horizontal/vertical
		ckHorizontal = new JCheckBox(I18N.getMsg("horizontal"));
		ckHorizontal.setName(CK_HORIZONTAL);
		ckHorizontal.addActionListener(this);
		ckHorizontal.setSelected(param.charAt(2) == '1');
		toolbar.add(ckHorizontal);
		// with titles
		ckTitle = new JCheckBox(I18N.getMsg("title"));
		ckTitle.setName(CK_TITLE);
		ckTitle.addActionListener(this);
		ckTitle.setSelected(param.charAt(3) == '1');
		toolbar.add(ckTitle);
		return toolbar;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) e.getSource();
			switch (cb.getName()) {
				case CB_TYPE:
				case CB_ZOOM:
					saveParam();
					refresh();
					break;
				default:
					break;
			}
		}
	}

	public boolean getHorizontal() {
		return ckHorizontal.isSelected();
	}

	public int getTypes() {
		return cbTypes.getSelectedIndex();
	}

	public int getZoom() {
		return cbZoom.getSelectedIndex() + 1;
	}

	public boolean getCkTitle() {
		return ckTitle.isSelected();
	}

	private void saveParam() {
		String param = "";
		param += cbTypes.getSelectedIndex() + "";
		param += cbZoom.getSelectedIndex() + "";
		param += ckHorizontal.isSelected() ? "1" : "0";
		param += ckTitle.isSelected() ? "1" : "0";
		App.preferences.setString(Pref.KEY.STORYMAP, param);
	}

	private void printAction() {
		ComponentPrinter.pr(mainFrame, graph, mainFrame.getBook().getTitle(), "");
	}

}
