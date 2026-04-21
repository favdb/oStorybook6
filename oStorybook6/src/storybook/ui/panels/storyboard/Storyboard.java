/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2016 FaVdB

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
package storybook.ui.panels.storyboard;

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import storybook.App;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.tools.LOG;
import storybook.tools.ViewUtil;
import storybook.tools.swing.LaF;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.SbView;
import storybook.ui.Ui;
import static storybook.ui.Ui.BNONE;
import storybook.ui.panels.AbstractScrollPanel;
import storybook.ui.panels.EntityLinksPanel;

@SuppressWarnings("serial")
public class Storyboard extends AbstractScrollPanel implements Printable, MouseWheelListener {

	private static final String CLIENT_PROPERTY_STRAND_ID = "strand_id";
	private boolean layoutDirection;
	private final List<JLabel> strandLabels;
	private JCheckBox ckDirection;

	public Storyboard(MainFrame mainFrame) {
		// don't call super constructor here!
		this.mainFrame = mainFrame;
		strandLabels = new ArrayList<>();
	}

	@Override
	protected void zoomSet(int val) {
		// empty
	}

	@Override
	protected int zoomGetValue() {
		return (20);
	}

	@Override
	protected int zoomGetMin() {
		return (20);
	}

	@Override
	protected int zoomGetMax() {
		return (20);
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		Object newValue = evt.getNewValue();
		SbView newSbView;
		SbView viewSb;
		View newView;
		View view;
		switch (Ctrl.getPROPS(evt)) {
			case REFRESH:
				newSbView = (SbView) newValue;
				viewSb = (SbView) getParent().getParent();
				if (viewSb == newSbView) {
					refresh();
				}
				return;
			case PRINT:
				newView = (View) newValue;
				view = (View) getParent().getParent();
				if (view == newView) {
					// PrintUtil.printComponent(this);
					PrinterJob pj = PrinterJob.getPrinterJob();
					pj.setPrintable(this);
					pj.printDialog();
					try {
						pj.print();
					} catch (PrinterException e) {
						LOG.err("StoryboardPanel error", e);
					}
				}
				return;
			case STORYBOARD_DIRECTION:
				layoutDirection = (Boolean) evt.getNewValue();
				refresh();
				return;
			case SHOWINFO:
				if (newValue instanceof Scene) {
					Scene scene = (Scene) newValue;
					ViewUtil.scrollToScene(this, rowsPanel, scene);
					return;
				}
				if (newValue instanceof Chapter) {
					Chapter chapter = (Chapter) newValue;
					ViewUtil.scrollToChapter(this, rowsPanel, chapter);
					return;
				}
				break;
			default:
				break;
		}
		ActKey act = new ActKey(evt);
		switch (Book.getTYPE(act.type)) {
			case STRAND:
			case SCENE:
				refresh();
				break;
			case PART:
				if (isChange(act) || isDelete(act)) {
					ViewUtil.scrollToTop(rowsScroller);
					refresh();
					return;
				}
				break;
			default:
				break;
		}
		dispatchToStoryboardScenePanels(this, evt);
		dispatchToStrandLinksPanels(this, evt);
		dispatchToPersonLinksPanels(this, evt);
		dispatchToLocationLinksPanels(this, evt);
		dispatchToItemLinksPanels(this, evt);
		dispatchToPlotLinksPanels(this, evt);
		dispatchToSpacePanels(this, evt);
		switch (Book.getTYPE(act.type)) {
			case STRAND:
				if (isUpdate(act)) {
					for (JLabel lb : strandLabels) {
						long id = (Long) lb.getClientProperty(CLIENT_PROPERTY_STRAND_ID);
						Strand strand = (Strand) mainFrame.project.get(Book.TYPE.STRAND, id);
						lb.setBackground(strand.getJColor());
					}
				}
				break;
			case SCENE:
				if (isEdit(act)) {
					return;
				}
				if (isUpdate(act)) {
					refresh();
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void init() {
		this.withPart = true;
		layoutDirection = App.preferences.storyboardGetDirection();
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0, MIG.WRAP1)));
		//no part select
		add(initToolbar(), MIG.GROWX);
		ckDirection = Ui.initCheckBox(null, "ckDirection", "view.storyboard.direction",
				layoutDirection, BNONE, this);
		toolbar.add(ckDirection);
		rowsPanel = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0), "", "[top]"));
		if (!LaF.isDark()) {
			rowsPanel.setBackground(SwingUtil.getBackgroundColor());
		}
		rowsScroller = new JScrollPane(rowsPanel);
		SwingUtil.setUnitIncrement(rowsScroller);
		SwingUtil.setMaxPreferredSize(rowsScroller);
		add(rowsScroller, "grow");
		refresh();
		registerKeyboardAction();
		rowsPanel.addMouseWheelListener(this);
	}

	@Override
	public void refresh() {
		// must be done before the session is opened
		Part part = null;
		if (cbPartFilter != null && cbPartFilter.getSelectedIndex() > 0) {
			part = (Part) cbPartFilter.getSelectedItem();
		}
		List<Chapter> chapters;
		chapters = mainFrame.project.chapters.findByNumber(part);
		rowsPanel.removeAll();
		if (chapters.isEmpty()) {
			rowsPanel.add(new JLabel(I18N.getMsg("warning.no.scenes")));
			rowsPanel.revalidate();
			rowsPanel.repaint();
			return;
		}
		for (Chapter chapter : chapters) {
			StoryboardChapter rowPanel = new StoryboardChapter(mainFrame, chapter);
			rowsPanel.add(rowPanel, (layoutDirection ? "grow" : "wrap"));
		}
		rowsPanel.revalidate();
		revalidate();
		repaint();
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

	private static void dispatchToStoryboardScenePanels(Container cont, PropertyChangeEvent evt) {
		List<Component> ret = new ArrayList<>();
		SwingUtil.findComponentsByClass(cont, StoryboardScene.class, ret);
		for (Component comp : ret) {
			StoryboardScene panel = (StoryboardScene) comp;
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

	private static void dispatchToPlotLinksPanels(Container cont, PropertyChangeEvent evt) {
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

	private static void dispatchToSpacePanels(Container cont, PropertyChangeEvent evt) {
		List<Component> ret = new ArrayList<>();
		SwingUtil.findComponentsByClass(cont, StoryboardSpace.class, ret);
		for (Component comp : ret) {
			StoryboardSpace panel = (StoryboardSpace) comp;
			panel.modelPropertyChange(evt);
		}
	}

	public JPanel getPanel() {
		return rowsPanel;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) evt.getSource();
			if (cb.getName().equals("cbPartFilter")) {
				refresh();
			}
		}
		if (evt.getSource() instanceof JCheckBox) {
			JCheckBox ck = (JCheckBox) evt.getSource();
			if (ck.getName().equals("ckDirection")) {
				layoutDirection = ck.isSelected();
				App.preferences.storyboardSetDirection(layoutDirection);
				refresh();
			}
		}
	}

}
