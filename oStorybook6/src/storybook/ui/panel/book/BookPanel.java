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
package storybook.ui.panel.book;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import resources.icons.ICONS;
import storybook.App;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.ctrl.Ctrl.PROPS;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.dialog.OptionsDlg;
import storybook.tools.ViewUtil;
import storybook.tools.swing.LaF;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSLabel;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.SbView;
import storybook.ui.Ui;
import storybook.ui.panel.AbstractScrollPanel;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class BookPanel extends AbstractScrollPanel {

	public static final int ZOOM_MIN = 2, ZOOM_MAX = 10;
	private static final String BT_MINUS = "btMinus", BT_PLUS = "btPlus";
	private int zoom;
	private JButton btMinus, btPlus;

	public BookPanel(MainFrame mainFrame) {
		// don't call super constructor here!
		this.mainFrame = mainFrame;
		setName("BookPanel");
	}

	@Override
	public void init() {
		this.withPart = true;
		zoom = Integer.min(App.preferences.bookGetZoom(), ZOOM_MAX);
	}

	@Override
	public JToolBar initToolbar() {
		super.initToolbar();
		toolbar.add(new JLabel(I18N.getColonMsg("size")));
		toolbar.add(btMinus = Ui.initButton(BT_MINUS, "", ICONS.K.MINUS, "zoom.out", this));
		toolbar.add(btPlus = Ui.initButton(BT_PLUS, "", ICONS.K.PLUS, "zoom.in", this));
		add(toolbar, MIG.get(MIG.SPAN, MIG.GROWX));
		return toolbar;
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.FLOWY, MIG.INS0)));
		initToolbar();
		panel = new JPanel(new MigLayout());
		if (!LaF.isDark()) {
			panel.setBackground(SwingUtil.getBackgroundColor());
		}
		scroller = new JScrollPane(panel);
		SwingUtil.setUnitIncrement(scroller);
		SwingUtil.setMaxPreferredSize(scroller);
		add(scroller, MIG.GROW);
		zoomSet(Integer.min(App.preferences.bookGetZoom(), ZOOM_MAX));
		ViewUtil.scrollToTop(scroller);
		registerKeyboardAction();
		panel.addMouseWheelListener(this);
	}

	@Override
	protected void zoomSet(int val) {
		//LOG.trace("zoomSet(val="+val+") old zoomValue="+zoomValue);
		if (val == zoom) {
			return;
		}
		if (btMinus != null) {
			btMinus.setEnabled(val > ZOOM_MIN);
			btPlus.setEnabled(val < ZOOM_MAX);
		}
		App.preferences.bookSetZoom(val);
		zoom = val;
		refresh();
	}

	@Override
	protected int zoomGetValue() {
		return App.preferences.bookGetZoom();
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
		//LOG.trace("BookPanel.modelPropertyChange(evt=" + evt.toString() + ")");
		String propName = evt.getPropertyName();
		Object newValue = evt.getNewValue();
		Object oldValue = evt.getOldValue();
		SbView view = (SbView) getParent().getParent();
		switch (Ctrl.getPROPS(propName)) {
			case REFRESH:
				refresh();
				return;
			case SHOWOPTIONS:
				if (!view.getName().equals(((SbView) newValue).getName())) {
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
				break;
			case BOOK_ZOOM:
				if (newValue instanceof Integer) {
					zoomSet((Integer) newValue);
				}
				return;
			default:
				break;
		}

		ActKey act = new ActKey(evt);
		switch (Book.getTYPE(act.type)) {
			case SCENE:
				if (PROPS.INIT.check(act.getCmd())) {
					refresh();
					return;
				}
				if (PROPS.UPDATE.check(act.getCmd())) {
					Scene oldScene = (Scene) oldValue;
					Scene newScene = (Scene) newValue;
					if (oldScene == null || newScene == null) {
						break;
					}
					if (!oldScene.getId().equals(newScene.getId())) {
						return;
					}
					if (!oldScene.getChapterSceneNo().equals(newScene.getChapterSceneNo())) {
						refresh();
						return;
					}
				}
				break;
			case STRAND:
				if (PROPS.DELETE.check(act.getCmd())) {
					refresh();
					return;
				}
				break;
			case PART:
				if (PROPS.CHANGE.check(act.getCmd())) {
					refresh();
					ViewUtil.scrollToTop(scroller);
					return;
				}
				break;
			default:
				break;
		}
		dispatchToBookInfoPanels(this, evt);
		dispatchToBookTextPanels(this, evt);
	}

	@Override
	public void refresh() {
		//LOG.trace("BookPanel.refresh()");
		Part part = getCbPart();
		List<Chapter> chapters = mainFrame.project.chapters.findByNumber(part);
		panel.removeAll();
		for (Chapter chapter : chapters) {
			List<Scene> scenes = mainFrame.project.scenes.findBy(chapter);
			for (Scene scene : scenes) {
				BookScenePanel scenePanel = new BookScenePanel(mainFrame, scene);
				panel.add(scenePanel.getInfoPanel(), MIG.get(MIG.TOP, MIG.GROW));
				panel.add(scenePanel.getTextPanel(), MIG.get(MIG.TOP, MIG.GROWX));
				panel.add(scenePanel.getCmdPanel(), MIG.get(MIG.TOP, MIG.WRAP));
			}
		}
		if (panel.getComponentCount() == 0) {
			panel.add(new JSLabel(I18N.getMsg("warning.no.scenes")));
		}
		panel.revalidate();
	}

	private static void dispatchToBookInfoPanels(Container cont, PropertyChangeEvent evt) {
		List<Component> ret = new ArrayList<>();
		SwingUtil.findComponentsByClass(cont, BookInfoPanel.class, ret);
		for (Component comp : ret) {
			BookInfoPanel panel = (BookInfoPanel) comp;
			panel.modelPropertyChange(evt);
		}
	}

	private static void dispatchToBookTextPanels(Container cont, PropertyChangeEvent evt) {
		List<Component> ret = new ArrayList<>();
		SwingUtil.findComponentsByClass(cont, BookTextPanel.class, ret);
		for (Component comp : ret) {
			BookTextPanel panel = (BookTextPanel) comp;
			panel.modelPropertyChange(evt);
		}
	}

	public JPanel getPanel() {
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		//LOG.trace("BookPanel.actionPerformed(evt="+evt.toString()+")");
		if (evt.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) evt.getSource();
			if (cb.getName().equals("cbPartFilter")) {
				refresh();
			}
		}
		if (evt.getSource() instanceof JButton) {
			JButton bt = (JButton) evt.getSource();
			switch (bt.getName()) {
				case BT_MINUS:
					if (zoom > ZOOM_MIN) {
						zoomSet(zoom - 1);
					}
					break;
				case BT_PLUS:
					if (zoom < ZOOM_MAX) {
						zoomSet(zoom + 1);
					}
					break;
				default:
					break;
			}
		}
	}

}
