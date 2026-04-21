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
package storybook.ui.panels.chrono;

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
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
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import resources.icons.IconUtil;
import storybook.App;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import static storybook.ctrl.Ctrl.PROPS.CHRONO_LAYOUTDIRECTION;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.scene.Scene;
import storybook.db.scene.SceneSticker;
import storybook.dialog.options.OptionsDlg;
import storybook.tools.MessageLabel;
import storybook.tools.ViewUtil;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.LaF;
import storybook.tools.swing.PrintUtil;
import storybook.tools.swing.js.JSZoomPanel;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.SbView;
import storybook.ui.Ui;
import static storybook.ui.panels.AbstractPanel.setMinMax;
import storybook.ui.panels.AbstractScrollPanel;

/**
 * class for ChronoPanel contains sub panels for nodate and for each date
 *
 * @author favdb
 */
public class Chrono extends AbstractScrollPanel
		implements Printable, MouseWheelListener, ItemListener {

	private static final String TT = "Chrono.";

	private final String CK_DIRECTION = "CkDirection",
			CK_NODATES = "CkNodates",
			ZOOM = "zoom";

	public final static int ZOOM_MIN = 1, ZOOM_MAX = 10;
	public boolean vertical, nodates;
	private JCheckBox ckDirection, ckNodates;
	private ChronoRow nodatePanel;
	private JScrollPane nodateScroller;
	//basic sizes
	public int iconSize, fontSize;
	//sizes for scene
	public Dimension sceneSize;
	public int textLen;
	//sizes for row
	public int rowWidth, rowHeight;
	private SceneSticker selected;

	public Chrono(MainFrame mainFrame) {
		super(mainFrame);
	}

	/**
	 * initialize the class
	 */
	@Override
	public void init() {
		//LOG.trace(TT + "init()");
		this.withPart = true;
		zoom = setMinMax(App.preferences.chronoGetZoom(), ZOOM_MIN, ZOOM_MAX);
		nodates = App.preferences.chronoGetLayoutNodates();
		vertical = App.preferences.chronoGetLayoutDirection();
		// standard sizes
		iconSize = Math.max(8, Math.min(64, (int) (IconUtil.getDefSize() * (zoom / 5.0))));
		fontSize = Math.max(6, Math.min(28, (int) (FontUtil.getDefault().getSize() * (zoom / 5.0))));
		sceneSize = SceneSticker.getDefaultSize(zoom);
		textLen = ((sceneSize.width * iconSize) * 3) / fontSize;
		rowWidth = sceneSize.width
				+ ((Integer) UIManager.get("ScrollBar.width"))
				+ (FontUtil.getWidth() * 2);
		rowHeight = sceneSize.height
				+ ((Integer) UIManager.get("ScrollBar.width"))
				+ (FontUtil.getHeight() * 2);
		/* trace for options
		StringBuilder b = new StringBuilder();
		b.append("   ").append("zoom=").append(zoom)
				.append(", nodates=").append(nodates ? "true" : "false")
				.append(", vertical=").append(vertical ? "true" : "false").append("\n");
		b.append("   ").append("iconSize=").append(iconSize)
				.append(", fontSize=").append(fontSize).append("\n");
		b.append("   ").append("leftWidth=").append(leftWidth)
				.append(", leftHeight=").append(leftHeight).append("\n");
		b.append("   ").append("scene width=").append(sceneSize.width)
				.append(", scene height=").append(sceneSize.height).append("\n");
		b.append("   ").append("textLen=").append(textLen).append("\n");
		b.append("   ").append("rowWidth=").append(rowWidth)
				.append(", rowHeight=").append(rowHeight).append("\n");
		LOG.trace("Options:\n" + b.toString());*/
	}

	/**
	 * initialize the user interface
	 *
	 */
	@Override
	public void initUi() {
		//LOG.trace(TT + "initUi()");
		setLayout(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0, MIG.HIDEMODE2, MIG.WRAP1)));
		add(initToolbar(), MIG.get(MIG.SPAN, MIG.GROWX));
		add(initNodates(), MIG.get(MIG.SPAN, MIG.GROWX));
		add(initRows(), MIG.GROW);
		refreshData();
	}

	/**
	 * initialize the toolbar
	 *
	 * @return
	 */
	@Override
	public JToolBar initToolbar() {
		if (toolbar != null) {
			return toolbar;
		}
		super.initToolbar();
		//ckNodates
		toolbar.add(ckNodates = Ui.initCheckBox(toolbar,
				CK_NODATES, "view.chrono.nodates", nodates, null,
				e -> changeNodate()));
		//ckDirection
		toolbar.add(ckDirection = Ui.initCheckBox(toolbar,
				CK_DIRECTION, "view.chrono.direction", vertical, null,
				e -> changeDirection()));
		//zoom panel to select the zoom factor
		toolbar.add(new JSZoomPanel(this, ZOOM, ZOOM_MIN, ZOOM_MAX, zoom));
		return toolbar;
	}

	/**
	 * initialize for nodate panel/scroller
	 *
	 * @return
	 */
	public JScrollPane initNodates() {
		//LOG.trace(TT + "initNodates()");
		nodatePanel = new ChronoRow(this, null, 'X');
		nodateScroller = new JScrollPane(nodatePanel);
		nodateScroller.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("scene.nodate")));
		nodateScroller.setMinimumSize(
				new Dimension(sceneSize.width, rowHeight));
		/*nodateScroller.setPreferredSize(new Dimension(LaF.getScreenWidth(),
				rowHeight));
		nodateScroller.setMaximumSize(
				new Dimension(LaF.getScreenWidth(), LaF.getScreenHeight()));*/
		return nodateScroller;
	}

	/**
	 * initialize for rows panel/scroller
	 *
	 * @return
	 */
	private JScrollPane initRows() {
		//LOG.trace(TT + "initRows()");
		if (rowsScroller != null) {
			this.remove(rowsScroller);
		}
		if (rowsPanel != null) {
			rowsPanel.removeMouseWheelListener(this);
			this.remove(rowsPanel);
		}
		rowsPanel = new JPanel(new MigLayout());
		rowsPanel.setMaximumSize(
				new Dimension(LaF.getScreenWidth(), LaF.getScreenHeight()));
		rowsScroller = new JScrollPane(rowsPanel);
		rowsScroller.setMinimumSize(
				new Dimension(rowWidth + (FontUtil.getWidth()),
						rowHeight + (FontUtil.getHeight())));
		rowsScroller.setPreferredSize(
				new Dimension(LaF.getScreenWidth(), LaF.getScreenHeight()));
		return rowsScroller;
	}

	/**
	 * ask if vertical mode
	 *
	 * @return
	 */
	public boolean isVertical() {
		return App.preferences.chronoGetLayoutDirection();
	}

	/**
	 * refresh all data
	 */
	public void refreshData() {
		if (nodatePanel == null || rowsPanel == null) {
			return;
		}
		//LOG.trace(TT + "refreshData()");
		nodatePanel.refreshData();
		refreshRows();
	}

	public void refreshRows() {
		//LOG.trace(TT + "refreshRows()");
		rowsPanel.removeAll();
		String mig = (!vertical ? MIG.get(MIG.INS1, MIG.GAP1, MIG.WRAP1) : MIG.get(MIG.INS1, MIG.GAP1));
		rowsPanel.setLayout(new MigLayout(mig));
		// check that there is at least one date
		if (mainFrame.project.scenes.getList().isEmpty()) {
			rowsPanel.add(new MessageLabel("warning.no.scenes", 2), MIG.get(MIG.SPAN, MIG.WRAP, MIG.GROWX));
			rowsPanel.revalidate();
			rowsPanel.repaint();
			return;
		}
		char period = 'H';
		List<Date> dates = mainFrame.project.scenes.findDistinctDates(period);

		//panel for all dates
		for (Date date : dates) {
			ChronoRow p = new ChronoRow(this, date, period);
			if (p.getNbScenes() > 0) {
				rowsPanel.add(p, MIG.TOP);
			}
		}
		if (selected != null) {
			selected.setSelected(true);
		}
		rowsScroller.repaint();
	}

	/**
	 * model property change
	 *
	 * @param evt
	 */
	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//LOG.trace(TT+"modelPropertyChange(evt=" + evt.toString() + ")");
		String propName = evt.getPropertyName();
		Object oldValue = evt.getOldValue();
		Object newValue = evt.getNewValue();
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
				zoom = setMinMax((Integer) newValue, ZOOM_MIN, ZOOM_MAX);
				//sl_zoom.setValue(zoom);
				refresh();
				return;
			case CHRONO_LAYOUTNODATES:
				nodates = (Boolean) evt.getNewValue();
				refresh();
				return;
			case CHRONO_LAYOUTDIRECTION:
				vertical = (Boolean) evt.getNewValue();
				refresh();
				return;
			case SHOWOPTIONS:
				if (!((View) evt.getNewValue()).getName().equals(SbView.VIEWNAME.CHRONO.toString())) {
					return;
				}
				OptionsDlg.show(mainFrame, view.getName());
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
				return;
			default:
				break;
		}
		ActKey act = new ActKey(evt);
		switch (Book.getTYPE(act.type)) {
			case PART:
				if (Ctrl.PROPS.CHANGE.check(act.getCmd())) {
					ViewUtil.scrollToTop(rowsScroller);
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
						break;
					default:
						break;
				}
				break;
			case STRAND:
				refresh();
				break;
			default:
				break;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	/**
	 * printing this panel
	 *
	 * @param g
	 * @param pageFormat
	 * @param pageIndex
	 * @return
	 * @throws PrinterException
	 */
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
		g2.drawString("Page: " + (pageIndex + 1),
				(int) pageWidth / 2 - 35,
				(int) (pageHeight + fontHeight - fontDesent));
		this.paint(g2);
		if (pageIndex < 4) {
			return Printable.PAGE_EXISTS;
		}
		return Printable.NO_SUCH_PAGE;
	}

	/**
	 * item state change for CbPart, CK_NODATES or CK_DIRECTION
	 *
	 * @param evt
	 */
	@Override
	public void itemStateChanged(ItemEvent evt) {
		//LOG.trace(TT + "itemStateChanged(evt=" + evt.toString() + ")");
		if (evt.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) evt.getSource();
			if (cb.getName().equals("cbPartFilter")) {
				refresh();
			}
		}
	}

	private void changeNodate() {
		//LOG.trace(TT + "changeNodate()");
		nodateScroller.setVisible(ckNodates.isSelected());
		App.preferences.chronoSetLayoutNodates(ckNodates.isSelected());
		revalidate();
		repaint();
	}

	private void changeDirection() {
		//LOG.trace(TT + "changeVertical()");
		vertical = ckDirection.isSelected();
		App.preferences.chronoSetLayoutDirection(vertical);
		refreshRows();
		revalidate();
		//repaint();
	}

	//**************
	//** zoom     **
	//**************
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

	/**
	 * save the zoom value to the preferences and refresh
	 *
	 * @param val
	 */
	public void zoomSave(int val) {
		//LOG.trace(TT + "zoomSave(val=" + val + ")");
		zoom = setMinMax(val, ZOOM_MIN, ZOOM_MAX);
		App.preferences.chronoSetZoom(val);
		refresh();
	}

	public void setSelected(SceneSticker sel) {
		if (selected != null) {
			selected.setSelected(false);
		}
		selected = sel;
		sel.setSelected(true);
		mainFrame.getBookController().infoShow(selected.getScene());
	}

}
