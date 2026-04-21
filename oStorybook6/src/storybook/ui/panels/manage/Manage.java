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
package storybook.ui.panels.manage;

import api.infonode.docking.View;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.UIManager;
import storybook.App;
import storybook.action.ScrollToEntityAction;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.db.scene.SceneSticker;
import storybook.dialog.options.OptionsDlg;
import storybook.tools.ViewUtil;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSZoomPanel;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.panels.AbstractPanel;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class Manage extends AbstractPanel {

	private static final String TT = "Manage.";

	public final static int ZOOM_MIN = 1, ZOOM_MAX = 10;
	private static final String ZOOM = "manage.zoom",
			HIDE_UNASSIGNED = "HideUnassigned",
			VERTICAL = "Vertical";

	private JCheckBox ckUnassigned;
	public Dimension sceneSize, leftSize;
	private JCheckBox ckVertical;
	public int zoom, iconSize, fontSize, textLen;
	private ManageSceneDnd sceneSelected, sceneTarget;
	List<ManageSceneDnd> scenes;
	private Scene sceneCurrent = null;
	private JPanel rowsPanel;
	private JScrollPane rowsScroller;
	private ManageChapter unassignedPanel;

	public Manage(MainFrame mainFrame) {
		super(mainFrame);
	}

	//// zoom actions ////
	protected void zoomSet(int val) {
		App.preferences.manageSetZoom(val);
		mainFrame.getBookController().manageSetZoom(val);
	}

	protected int zoomGetValue() {
		return App.preferences.manageGetZoom();
	}

	protected int zoomGetMin() {
		return ZOOM_MIN;
	}

	protected int zoomGetMax() {
		return ZOOM_MAX;
	}

	public void zoomSave(int val) {
		App.preferences.manageSetZoom(val);
		init();
		refresh();
	}

	/**
	 * initialize the class
	 */
	@Override
	public void init() {
		scenes = new ArrayList<>();
		iconSize = SceneSticker.getIconSize(zoom);
		fontSize = SceneSticker.getFontSize(zoom);
		zoom = setMinMax(App.preferences.manageGetZoom(), ZOOM_MIN, ZOOM_MAX);
		int width = ((iconSize + 2) * 2) + 6;
		int height = FontUtil.getHeight() * 5;
		height = Math.max(height, (iconSize + 7) * 3);
		leftSize = SceneSticker.getDefaultLeft(zoom);
		width = width + (FontUtil.getWidth() * (zoom * 4)) + 4;
		sceneSize = new Dimension(width, height + 8);
		sceneSize = SceneSticker.getDefaultSize(zoom);
		textLen = (((width - leftSize.width - 4) / FontUtil.getWidth()) * 5) - 2;
	}

	/**
	 * get the size for a scroll panel
	 *
	 * @return
	 */
	public Dimension getScrollSize() {
		return new Dimension(sceneSize.width,
				sceneSize.height + iconSize);
	}

	/**
	 * initalize the user interface
	 */
	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.WRAP, MIG.HIDEMODE2, MIG.FILL, MIG.INS0, MIG.GAP0), "[]"));
		add(initToolbar(), MIG.GROWX);
		add(initUnassigned(), MIG.get(MIG.SPAN, MIG.GROWX));
		add(initRows(), MIG.get(MIG.SPAN, MIG.GROW));
	}

	/**
	 * initalize the unassigned panel
	 *
	 * @return
	 */
	private JPanel initUnassigned() {
		unassignedPanel = new ManageChapter(this, null);
		unassignedPanel.setMinimumSize(getScrollSize());
		return unassignedPanel;
	}

	/**
	 * initalize the chapters panel
	 *
	 * @return
	 */
	private JScrollPane initRows() {
		rowsPanel = new JPanel(new MigLayout());
		rowsScroller = new JScrollPane(rowsPanel);
		SwingUtil.setUnitIncrement(rowsScroller);
		SwingUtil.setMaxPreferredSize(rowsScroller);
		refreshData();
		ViewUtil.scrollToTop(rowsScroller);
		return rowsScroller;
	}

	/**
	 * refresh all data
	 */
	public void refreshData() {
		unassignedPanel.refresh();
		unassignedPanel.setVisible(!App.preferences.manageGetUnassigned());
		// refresh chapters
		scenes = new ArrayList<>();
		Part part = getCbPart();
		List<Chapter> chapters = mainFrame.project.chapters.find(part);
		rowsPanel.removeAll();
		MigLayout layout = new MigLayout(MIG.get(MIG.INS0, MIG.GAP1));
		if (!App.preferences.manageGetVertical()) {
			layout = new MigLayout(MIG.get(MIG.INS0, MIG.GAP0, MIG.WRAP), "[grow]");
		}
		rowsPanel.setLayout(layout);
		for (Chapter chapter : chapters) {
			if (chapter != null) {
				ManageChapter pchapter = new ManageChapter(this, chapter);
				if (!App.preferences.manageGetVertical()) {
					rowsPanel.add(pchapter, MIG.GROWX);
				} else {
					rowsPanel.add(pchapter, MIG.GROWY);
				}
			}
		}
		if (rowsPanel.getComponentCount() == 0) {
			rowsPanel.add(new JLabel(I18N.getMsg("warning.no.chapters")), MIG.GROW);
		}
		if (sceneCurrent != null) {
			ManageSceneDnd msc = sceneFind(sceneCurrent);
			if (msc != null) {
				sceneSelect(msc);
			}
		}
		unassignedPanel.revalidate();
		rowsPanel.revalidate();
		revalidate();
		repaint();
	}

	/**
	 * initialize the toolbar
	 *
	 * @return
	 */
	@Override
	public JToolBar initToolbar() {
		super.initToolbar();
		ckUnassigned = new JCheckBox(I18N.getMsg("preferences.manage.hide_unassigned"));
		ckUnassigned.setName(HIDE_UNASSIGNED);
		ckUnassigned.setSelected(App.preferences.manageGetUnassigned());
		ckUnassigned.addActionListener((ActionEvent evt) -> {
			App.preferences.manageSetUnassgned(ckUnassigned.isSelected());
			refresh();
		});
		toolbar.add(ckUnassigned, MIG.GROWY);
		ckVertical = new JCheckBox(I18N.getMsg("vertical"));
		ckVertical.setName(VERTICAL);
		ckVertical.setSelected(App.preferences.manageGetVertical());
		ckVertical.addActionListener((ActionEvent evt) -> {
			App.preferences.manageSetVertical(ckVertical.isSelected());
			refresh();
		});
		toolbar.add(ckVertical, MIG.GROWY);
		toolbar.add(new JSZoomPanel(this, ZOOM, ZOOM_MIN, ZOOM_MAX, zoom));
		return (toolbar);
	}

	/**
	 * dispatch evet for sub panel
	 *
	 * @param cont
	 * @param evt
	 */
	private static void dispatchToChapterPanels(Container cont, PropertyChangeEvent evt) {
		List<Component> ret = new ArrayList<>();
		SwingUtil.findComponentsByClass(cont, ManageChapter.class, ret);
		for (Component comp : ret) {
			ManageChapter panel = (ManageChapter) comp;
			panel.modelPropertyChange(evt);
		}
	}

	/**
	 * add a ManageSceneDnd to the list
	 *
	 * @param msc
	 */
	public void sceneAdd(ManageSceneDnd msc) {
		scenes.add(msc);
	}

	/**
	 * find a ManageSceneDnd in the list
	 *
	 * @param sc
	 * @return
	 */
	public ManageSceneDnd sceneFind(Scene sc) {
		for (ManageSceneDnd msc : scenes) {
			if (msc.scene != null && msc.scene.getId().equals(sc.getId())) {
				return msc;
			}
		}
		return null;
	}

	/**
	 * select a scene
	 *
	 * @param sc
	 */
	public void sceneSelect(Scene sc) {
		ManageSceneDnd msc = sceneFind(sc);
		if (msc != null) {
			sceneSelect(msc);
		}
	}

	/**
	 * select a ManageSceneDnd
	 *
	 * @param p
	 */
	public void sceneSelect(ManageSceneDnd p) {
		if (sceneSelected != null) {
			sceneSelected.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Panel.background"), 2));
		}
		sceneSelected = p;
		sceneCurrent = p.scene;
		p.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Panel.foreground"), 2));
		mainFrame.getBookController().infoSetTo(p.scene);
	}

	/**
	 * select a ManageScene
	 *
	 * @param srce
	 */
	public void sceneSelect(ManageScene srce) {
		sceneCurrent = srce.scene;
		sceneSelect((ManageSceneDnd) srce);
	}

	/**
	 * get the selected ManageSceneDnd
	 *
	 * @return
	 */
	public ManageSceneDnd sceneGetSelected() {
		return sceneSelected;
	}

	/**
	 * chek if the given ManageSceneDnd is selected
	 *
	 * @param p
	 * @return
	 */
	public boolean sceneIsSelected(ManageSceneDnd p) {
		if (sceneSelected == null) {
			return false;
		}
		return sceneSelected.equals(p);
	}

	/**
	 * set a Scene to be unassigned
	 */
	public void sceneSetUnassigned() {
		//LOG.trace(TT + "sceneSetUnassigned() for " + sceneSelected.getName());
		Scene scene = sceneSelected.scene;
		Chapter chapter = scene.getChapter();
		scene.setChapter();
		mainFrame.getBookController().updateEntity(scene);
		mainFrame.project.scenes.renumber(chapter);
		sceneSelected = null;
		mainFrame.setUpdated();
		unassignedPanel.repaint();
		refreshData();
	}

	/**
	 * set the given ManageScene to be unassigned
	 *
	 * @param srce
	 */
	public void sceneSetUnassigned(ManageScene srce) {
		//LOG.trace(TT + "sceneSetUnassigned(srce=" + srce.toString() + ")");
		if (srce.scene != null) {
			sceneSelect(srce);
			sceneSetUnassigned();
		}
	}

	public void sceneMoveToBegin(ManageSceneDnd srce, Chapter chapter) {
		//LOG.trace(TT + "sceneMoveToBegin(srce=" + LOG.trace(srce.scene)
		//		+ ", " + LOG.trace(chapter) + ")");
		List<Scene> ls = mainFrame.project.scenes.find(chapter);
		@SuppressWarnings("null")
		int start = (ls.isEmpty() ? 0 : ls.get(0).getSceneno());
		Scene srceScene = srce.scene;
		if (ls.contains(srceScene)) {
			ls.remove(srceScene);
		}
		srceScene.setChapter(chapter);
		srceScene.setSceneno(start++);
		//renumber the other scenes of this chapter
		for (Scene sx : ls) {
			sx.setSceneno(start++);
		}
		mainFrame.getBookController().updateEntity(srceScene);
		mainFrame.setUpdated();
		refreshData();
	}

	public void sceneMoveAfter(ManageSceneDnd srceDnd, ManageSceneDnd destDnd) {
		//LOG.trace(TT + "sceneMoveAfter(srce=" + LOG.trace(srceDnd.scene)
		//		+ ", dest=" + LOG.trace(destDnd.scene) + ")");
		Scene srce = srceDnd.scene;
		Scene dest = destDnd.scene;
		Chapter chapter = dest.getChapter();
		List<Scene> ls = mainFrame.project.scenes.findBy(chapter);
		if (ls.contains(srce)) {
			ls.remove(srce);
		}
		int num = dest.getSceneno() + 1;
		srce.setChapter(chapter);
		srce.setSceneno(num++);
		boolean b = false;
		for (Scene scene : ls) {
			if (b) {
				scene.setSceneno(num++);
			}
			if (scene.getId().equals(dest.getId())) {
				b = true;
			}
		}
		mainFrame.getBookController().updateEntity(srce);
		mainFrame.setUpdated();
		refreshData();
	}

	public void sceneSetTarget(ManageSceneDnd target) {
		if (sceneTarget != null) {
			for (ManageSceneDnd msc : scenes) {
				msc.resetBackground();
			}
		}
		if (target.type == ManageScene.TYPE.BEGIN
				|| target.type == ManageScene.TYPE.AFTER
				|| target.type == ManageScene.TYPE.MAKE_UNASSIGNED) {
			sceneTarget = target;
			sceneTarget.setBackground(SwingUtil.getSelectBackground());
			sceneTarget.repaint(); // AJOUT : Forcer le repaint
		}
	}

	public void sceneResetTarget(ManageSceneDnd target) {
		if (target != null) {
			target.resetBackground();
			sceneTarget = null;
			target.repaint();
			repaint();
		}
	}

	private void scrollToChapter(Chapter chapter) {
		if (chapter == null) {
			return;
		}
		final ScrollToEntityAction action = new ScrollToEntityAction(this, rowsPanel, chapter);
		Timer timer = new Timer(200, action);
		timer.setRepeats(false);
		timer.start();
	}

	private void scrollToScene(Scene scene) {
		if (scene == null) {
			return;
		}
		final ScrollToEntityAction action = new ScrollToEntityAction(this, rowsPanel, scene);
		Timer timer = new Timer(200, action);
		timer.setRepeats(false);
		timer.start();
	}

	public void clearSelection() {

	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) evt.getSource();
			if (cb.getName().equals("cbPartFilter")) {
				refresh();
			}
		}
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		Object newValue = evt.getNewValue();
		View newView = null;
		if (newValue instanceof View) {
			newView = (View) evt.getNewValue();
		}
		View view = (View) getParent().getParent();
		switch (Ctrl.getPROPS(evt.getPropertyName())) {
			case REFRESH:
				if (view.equals(newView)) {
					refresh();
				}
				return;
			case SHOWOPTIONS:
				if (view.equals(newView)) {
					OptionsDlg.show(mainFrame, view.getName());
				}
				return;
			case SHOWINFO:
				if (newValue instanceof Scene) {
					Scene scene = (Scene) newValue;
					scrollToScene(scene);
					return;
				}
				if (newValue instanceof Chapter) {
					Chapter chapter = (Chapter) newValue;
					scrollToChapter(chapter);
					return;
				}
				break;
			case MANAGE_ZOOM:
				zoom = (Integer) newValue + 1;
				refresh();
				return;
			case MANAGE_HIDEUNASSIGNED:
			case MANAGE_VERTICAL:
				refresh();
				return;
			default:
				break;
		}
		ActKey act = new ActKey(evt);
		String actType = act.type;
		if (Book.TYPE.PART.compare(actType) && Ctrl.PROPS.CHANGE.check(act.getCmd())) {
			refresh();
			ViewUtil.scrollToTop(rowsScroller);
			return;
		}
		if (Book.TYPE.STRAND.compare(actType)) {
			refresh();
			return;
		}
		if (Book.TYPE.CHAPTER.compare(actType)) {
			refresh();
		}
		dispatchToChapterPanels(this, evt);
	}

}
