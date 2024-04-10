/*
 Storybook: Open Source software for novelists and authors.
 Copyright (C) 2008 - 2012 Martin Mustun

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.ui.panel.chrono;

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import storybook.App;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.ctrl.Ctrl.PROPS;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.scene.Scene;
import storybook.db.scene.Scenes;
import storybook.db.strand.Strand;
import storybook.db.strand.StrandLabel;
import storybook.dialog.OptionsDlg;
import storybook.tools.ViewUtil;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.LaF;
import storybook.tools.swing.PrintUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSLabelVertical;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.MessageLabel;
import storybook.ui.SbView;
import storybook.ui.SbView.VIEWNAME;
import storybook.ui.panel.AbstractScrollPanel;
import storybook.ui.panel.EntityLinksPanel;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class ChronoPanel extends AbstractScrollPanel
   implements Printable, MouseWheelListener, ItemListener, ChangeListener {

	private static final String TT = "ChronoPanel";

	private final String CB_DIRECTION = "CbDirection", SL_ZOOM = "SlZoom";

	public final static int ZOOM_MIN = 2;
	public final static int ZOOM_MAX = 15;
	private int zoom;
	private static final String CLIENT_PROPERTY_STRAND_ID = "strand_id";
	private boolean direction;
	private List<StrandLabel> strandLabels = new ArrayList<>();
	private JSlider sl_zoom;
	private JCheckBox ckDirection;
	private JPanel panelData;

	public ChronoPanel(MainFrame mainFrame) {
		// don't call super constructor here!
		this.mainFrame = mainFrame;
	}

	@Override
	protected void zoomSet(int val) {
		zoom = val;
		App.preferences.chronoSetZoom(val);
		mainFrame.getBookController().chronoSetZoom(val);
	}

	@Override
	protected int zoomGetValue() {
		return zoom;
	}

	@Override
	protected int zoomGetMin() {
		return ZOOM_MIN;
	}

	@Override
	protected int zoomGetMax() {
		return ZOOM_MAX;
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//LOG.printInfos(TT + ".modelPropertyChange(evt=" + evt.toString() + ")");
		Object oldValue = evt.getOldValue();
		Object newValue = evt.getNewValue();
		String propName = evt.getPropertyName();
		SbView view = (SbView) getParent().getParent();
		switch (Ctrl.getPROPS(propName)) {
			case REFRESH:
				if (view == ((SbView) newValue)) {
					refresh();
				}
				return;
			case PRINT:
				if (view == ((View) newValue)) {
					PrintUtil.printComponent(this);
				}
				return;
			case CHRONO_ZOOM:
				zoom = setMinMax(ZOOM_MIN, ZOOM_MAX, (Integer) newValue);
				sl_zoom.setValue(zoom);
				return;
			case CHRONO_LAYOUTDIRECTION:
				direction = (Boolean) evt.getNewValue();
				refresh();
				return;
			case SHOWOPTIONS:
				if (!((View) evt.getNewValue()).getName().equals(VIEWNAME.CHRONO.toString())) {
					return;
				}
				OptionsDlg.show(mainFrame, view.getName());
				return;
			case SHOWINFO:
				if (newValue instanceof Scene) {
					Scene scene = (Scene) newValue;
					ViewUtil.scrollToScene(this, panel, scene);
					return;
				}
				if (newValue instanceof Chapter) {
					Chapter chapter = (Chapter) newValue;
					ViewUtil.scrollToChapter(this, panel, chapter);
					return;
				}
				return;
			default:
				break;
		}
		ActKey act = new ActKey(evt);
		switch (Book.getTYPE(act.type)) {
			case PART:
				if (PROPS.CHANGE.check(act.getCmd())) {
					ViewUtil.scrollToTop(scroller);
					refresh();
				}
				break;
			case SCENE:
			case STRAND:
				refresh();
				break;
			default:
				break;
		}
		dispatchToChronoScenePanels(this, evt);
		dispatchToStrandLinksPanels(this, evt);
		dispatchToPersonLinksPanels(this, evt);
		dispatchToLocationLinksPanels(this, evt);
		dispatchToItemLinksPanels(this, evt);
		dispatchToPlotLinksPanels(this, evt);
		dispatchToSpacePanels(this, evt);
		switch (Book.getTYPE(act.type)) {
			case SCENE:
				switch (Ctrl.getPROPS(act.getCmd())) {
					case EDIT:
						return;
					case UPDATE:
						if (oldValue == null) {
							return;
						}
						Scene newScene = (Scene) newValue;
						Scene oldScene = (Scene) oldValue;
						// strand changed
						if (newScene != null && oldScene != null) {
							if (newScene.getStrand() != null
							   && oldScene.getStrand() != null
							   && !newScene.getStrand().getId().equals(oldScene.getStrand().getId())) {
								refresh();
								return;
							}
							// "informative" changed
							if (!newScene.getInformative().equals(oldScene.getInformative())) {
								refresh();
								return;
							}
							// date changed
							if (newScene.hasScenets() && !oldScene.hasScenets()) {
								refresh();
								return;
							}
							if (oldScene.hasScenets() && !newScene.hasScenets()) {
								refresh();
								return;
							}
							if (newScene.hasScenets() && oldScene.hasScenets()) {
								Date oldDate = oldScene.getDate();
								Date newDate = newScene.getDate();
								if (!oldDate.equals(newDate)) {
									refresh();
								}
							}
						}
						break;
					default:
						break;
				}
				break;
			case STRAND:
				if (PROPS.UPDATE.check(act.getCmd())) {
					for (StrandLabel lb : strandLabels) {
						long id = (Long) lb.getClientProperty(CLIENT_PROPERTY_STRAND_ID);
						Strand strand = (Strand) mainFrame.project.get(Book.TYPE.STRAND, id);
						lb.setBackground(strand.getJColor());
						if (!ColorUtil.isDark(strand.getJColor())) {
							lb.setForeground(Color.BLACK);
						}
					}
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void init() {
		this.withPart = true;
		zoom = setMinMax(ZOOM_MIN, ZOOM_MAX, App.preferences.chronoGetZoom());
		direction = App.preferences.chronoGetLayoutDirection();
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.INS0));
		add(initToolbar(), MIG.get(MIG.SPAN, MIG.GROWX));
		panel = new JPanel(new MigLayout(MIG.INS1, "", "[top]"));
		if (!LaF.isDark()) {
			panel.setBackground(SwingUtil.getBackgroundColor());
		}
		scroller = new JScrollPane(panel);
		SwingUtil.setUnitIncrement(scroller);
		SwingUtil.setMaxPreferredSize(scroller);
		add(scroller, MIG.GROW);
		registerKeyboardAction();
		panel.addMouseWheelListener(this);
		refresh();
	}

	@Override
	public JToolBar initToolbar() {
		super.initToolbar();
		ckDirection = new JCheckBox();
		ckDirection.setName(CB_DIRECTION);
		ckDirection.addItemListener(this);
		ckDirection.setText(I18N.getMsg("vertical"));
		ckDirection.setOpaque(false);
		ckDirection.setSelected(direction);
		ckDirection.setToolTipText(I18N.getColonMsg("statusbar.change.layout.direction"));
		toolbar.add(ckDirection);

		JPanel zp = new JPanel();
		zp.setBorder(SwingUtil.getBorderDefault());
		zp.setBackground(toolbar.getBackground());
		zp.add(new JLabel(I18N.getColonMsg("zoom")));
		sl_zoom = new JSlider(JSlider.HORIZONTAL, ZOOM_MIN, ZOOM_MAX, zoom);
		sl_zoom.setName(SL_ZOOM);
		sl_zoom.setMajorTickSpacing(5);
		sl_zoom.setMinorTickSpacing(1);
		//sl_zoom.setOpaque(false);
		//sl_zoom.setPaintTicks(true);
		sl_zoom.addChangeListener(this);
		zp.add(sl_zoom);
		toolbar.add(zp);
		return (toolbar);
	}

	@Override
	public void refresh() {
		if (panel == null) {
			return;
		}
		panel.removeAll();
		// must be done before the session is opened
		//Part part = (mainFrame.showAllParts ? (Part) null : mainFrame.partGetCurrent());
		// check that there is a date for each scene
		List<Scene> scenes = Scenes.find(mainFrame);
		for (Scene scene : scenes) {
			if (!scene.hasDate()) {
				panel.add(new MessageLabel("warning.chronology", 3), MIG.get(MIG.SPAN, MIG.WRAP, MIG.GROWX));
				break;
			}
		}
		scenes = mainFrame.project.scenes.getWithDates();
		List<Date> dates = mainFrame.project.scenes.findDistinctDates(scenes, 'H');
		// check that there is at least one date
		if (dates.isEmpty()) {
			panel.add(new MessageLabel("warning.no.scenes", 2));
			panel.revalidate();
			panel.repaint();
			return;
		}
		@SuppressWarnings("unchecked")
		List<Strand> strands = (List) mainFrame.project.getList(Book.TYPE.STRAND);
		panel.add(panelData = new JPanel(new MigLayout(MIG.get(MIG.INS0))));
		if (!LaF.isDark()) {
			panelData.setBackground(SwingUtil.getBackgroundColor());
		}
		if (direction) {
			refreshVertical(dates, strands);
		} else {
			refreshHorizontal(dates, strands);
		}
		panel.revalidate();
		revalidate();
		repaint();
	}

	private void refreshVertical(List<Date> dates, List<Strand> strands) {
		for (Date date : dates) {
			int i = 0;
			for (Strand strand : strands) {
				StrandLabel lbStrand = new StrandLabel(strand, CLIENT_PROPERTY_STRAND_ID);
				strandLabels.add(lbStrand);
				String wrap = "";
				if (i == strands.size() - 1) {
					wrap = MIG.WRAP + ",";
				}
				panelData.add(lbStrand, wrap + MIG.GROW);
				++i;
			}
			i = 0;
			for (Strand strand : strands) {
				String wrap = "";
				if (i == strands.size() - 1) {
					wrap = MIG.WRAP + ",";
				}
				ColumnPanel colPanel = new ColumnPanel(mainFrame, strand, date);
				panelData.add(colPanel, wrap + MIG.GROW);
				++i;
			}
		}
	}

	private void refreshHorizontal(List<Date> dates, List<Strand> strands) {
		for (Strand strand : strands) {
			int i = 0;
			for (Date date : dates) {
				StrandLabel lbStrand = new StrandLabel(strand, CLIENT_PROPERTY_STRAND_ID);
				lbStrand.setUI(new JSLabelVertical(false));
				strandLabels.add(lbStrand);
				panelData.add(lbStrand, MIG.GROW);
				String wrap = "";
				if (i == dates.size() - 1) {
					wrap = "," + MIG.WRAP;
				}
				RowPanel rowPanel = new RowPanel(mainFrame, strand, date);
				panelData.add(rowPanel, MIG.GROW + wrap);
				++i;
			}
		}
	}

	@Override
	public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.black);
		int fontHeight = g2.getFontMetrics().getHeight();
		int fontDesent = g2.getFontMetrics().getDescent();
		double pageHeight = pageFormat.getImageableHeight();
		double pageWidth = pageFormat.getImageableWidth();
		g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		// bottom center
		g2.drawString("Page: " + (pageIndex + 1), (int) pageWidth / 2 - 35, (int) (pageHeight + fontHeight - fontDesent));
		this.paint(g2);
		if (pageIndex < 4) {
			return Printable.PAGE_EXISTS;
		}
		return Printable.NO_SUCH_PAGE;
	}

	private static void dispatchToChronoScenePanels(Container cont, PropertyChangeEvent evt) {
		List<Component> ret = new ArrayList<>();
		SwingUtil.findComponentsByClass(cont, ChronoScenePanel.class, ret);
		for (Component comp : ret) {
			ChronoScenePanel panel = (ChronoScenePanel) comp;
			panel.modelPropertyChange(evt);
		}
	}

	private static void dispatchToPersonLinksPanels(Container cont, PropertyChangeEvent evt) {
		List<Component> ret = new ArrayList<>();
		SwingUtil.findComponentsByClass(cont, EntityLinksPanel.class, ret);
		for (Component comp : ret) {
			EntityLinksPanel panel = (EntityLinksPanel) comp;
			panel.modelPropertyChange(evt);
		}
	}

	private static void dispatchToLocationLinksPanels(Container cont, PropertyChangeEvent evt) {
		List<Component> ret = new ArrayList<>();
		SwingUtil.findComponentsByClass(cont, EntityLinksPanel.class, ret);
		for (Component comp : ret) {
			EntityLinksPanel panel = (EntityLinksPanel) comp;
			panel.modelPropertyChange(evt);
		}
	}

	private static void dispatchToItemLinksPanels(Container cont, PropertyChangeEvent evt) {
		List<Component> ret = new ArrayList<>();
		SwingUtil.findComponentsByClass(cont, EntityLinksPanel.class, ret);
		for (Component comp : ret) {
			EntityLinksPanel panel = (EntityLinksPanel) comp;
			panel.modelPropertyChange(evt);
		}
	}

	private static void dispatchToStrandLinksPanels(Container cont, PropertyChangeEvent evt) {
		List<Component> ret = new ArrayList<>();
		SwingUtil.findComponentsByClass(cont, EntityLinksPanel.class, ret);
		for (Component comp : ret) {
			EntityLinksPanel panel = (EntityLinksPanel) comp;
			panel.modelPropertyChange(evt);
		}
	}

	private static void dispatchToPlotLinksPanels(Container cont, PropertyChangeEvent evt) {
		List<Component> ret = new ArrayList<>();
		SwingUtil.findComponentsByClass(cont, EntityLinksPanel.class, ret);
		for (Component comp : ret) {
			EntityLinksPanel panel = (EntityLinksPanel) comp;
			panel.modelPropertyChange(evt);
		}
	}

	private static void dispatchToSpacePanels(Container cont, PropertyChangeEvent evt) {
		List<Component> ret = new ArrayList<>();
		SwingUtil.findComponentsByClass(cont, SpacePanel.class, ret);
		for (Component comp : ret) {
			SpacePanel panel = (SpacePanel) comp;
			panel.modelPropertyChange(evt);
		}
	}

	public JPanel getPanel() {
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		// empty
	}

	@Override
	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) evt.getSource();
			if (cb.getName().equals("cbPartFilter")) {
				refresh();
			}
		} else if (evt.getSource() instanceof JCheckBox) {
			JCheckBox cb = (JCheckBox) evt.getSource();
			boolean val = cb.isSelected();
			if (cb.getName().equals(CB_DIRECTION)) {
				direction = val;
				App.preferences.chronoSetLayoutDirection(val);
				refresh();
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Component comp = (Component) e.getSource();
		if (SL_ZOOM.equals(comp.getName())) {
			JSlider slider = (JSlider) e.getSource();
			if (!slider.getValueIsAdjusting()) {
				int val = slider.getValue();
				App.preferences.chronoSetZoom(val);
				zoom = val;
				//sl_zoom.setValue(val);
				refresh();
			}
		}
	}

}
