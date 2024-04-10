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
package storybook.ui.panel.manage;

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
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.action.ScrollToEntityAction;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.dialog.OptionsDlg;
import storybook.tools.ViewUtil;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractScrollPanel;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class Manage extends AbstractScrollPanel implements ChangeListener {

	private static final String TT = "ManagePanel";

	public final static int ZOOM_MIN = 1, ZOOM_MAX = 10;
	public final static int COLS_MIN = 1, COLS_MAX = 10;
	private static final String ZOOM_SLIDER = "ZoomSlider";
	private static final String HIDE_UNASSIGNED = "HideUnassigned", VERTICAL = "Vertical";
	private int nbCols;
	private JCheckBox ckHide;
	private JScrollPane unassignedScoller;
	public Dimension sceneSize;
	public int textLen;
	public Dimension leftSize;
	private ManageChapter unassignedPanel;
	private JCheckBox ckVertical;
	public int iconSize;
	private JPanel columnsPanel;
	private JSlider sl_columns;
	private ManageSceneDnd sceneSelected;
	private ManageSceneDnd sceneTarget;
	List<ManageSceneDnd> scenes;
	private Scene sceneCurrent = null;
	private int zoom;
	private JButton btZoomOut;
	private JButton btZoomIn;
	private JLabel lbZoom;

	public Manage(MainFrame mainFrame) {
		super(mainFrame);
	}

	@Override
	protected void zoomSet(int val) {
		App.preferences.manageSetZoom(val);
		mainFrame.getBookController().manageSetZoom(val);
	}

	@Override
	protected int zoomGetValue() {
		return App.preferences.manageGetZoom();
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
		//LOG.trace(TT+".modelPropertyChange(...)");
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
				zoomAdjust(-1);
				return;
			case MANAGE_COLUMNS:
				sl_columns.setValue((Integer) newValue);
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
			ViewUtil.scrollToTop(scroller);
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

	@Override
	public void init() {
		//LOG.trace(TT+".init()");
		scenes = new ArrayList<>();
		iconSize = IconUtil.getDefSize();
		nbCols = setMinMax(App.preferences.manageGetColumns(), COLS_MIN, COLS_MAX);
		zoom = setMinMax(App.preferences.manageGetZoom(), ZOOM_MIN, ZOOM_MAX);
		// left panel size is:
		int width = ((iconSize + 2) * 2) + 6;
		int height = FontUtil.getHeight() * 5;
		height = Math.max(height, (iconSize + 7) * 3);
		leftSize = new Dimension(width, height);
		// all scene panel size
		// width =left width+28 characters+2for gap
		width = width + (FontUtil.getWidth() * (zoom * 4)) + 4;
		// height = height + 6 for gap
		sceneSize = new Dimension(width, height + 8);
		textLen = (((width - leftSize.width - 4) / FontUtil.getWidth()) * 5) - 2;
	}

	@Override
	public void initUi() {
		//LOG.trace(TT+".initUI()");
		setLayout(new MigLayout(MIG.get(MIG.WRAP, MIG.HIDEMODE2, MIG.FILL, MIG.INS0, MIG.GAP + " 1"), "[]"));
		add(initToolbar(), MIG.GROWX);
		unassignedPanel = new ManageChapter(this);
		unassignedScoller = new JScrollPane(unassignedPanel);
		unassignedScoller.setMinimumSize(
		   new Dimension(sceneSize.width, (FontUtil.getWidth() - 1) * I18N.getMsg("scene.unassigned").length()));
		add(unassignedScoller, MIG.GROWX);
		panel = new JPanel(new MigLayout(MIG.WRAP + " " + nbCols));
		scroller = new JScrollPane(panel);
		add(scroller);
		SwingUtil.setUnitIncrement(scroller);
		SwingUtil.setMaxPreferredSize(scroller);
		refreshData();
		ViewUtil.scrollToTop(scroller);
		panel.addMouseWheelListener(this);
	}

	public void refreshData() {
		//LOG.trace(TT+".refreshData()");
		scenes = new ArrayList<>();
		Part part = getCbPart();
		List<Chapter> chapters = mainFrame.project.chapters.find(part);
		panel.removeAll();
		unassignedPanel.refresh();
		MigLayout layout = new MigLayout(MIG.get(MIG.INS0, MIG.GAP1, MIG.WRAP + " " + nbCols));
		if (!App.preferences.manageGetVertical()) {
			layout = new MigLayout(MIG.get("ins 2", MIG.GAP + " 2", MIG.WRAP), "[grow]");
		}
		panel.setLayout(layout);
		// "chapter" for unassigned scenes
		unassignedScoller.setVisible(!App.preferences.manageGetHideUnassigned());
		// chapters
		for (Chapter chapter : chapters) {
			if (chapter != null) {
				ManageChapter pchapter = new ManageChapter(this, chapter);
				if (!App.preferences.manageGetVertical()) {
					panel.add(pchapter, MIG.GROWX);
				} else {
					panel.add(pchapter, MIG.GROWY);
				}
			}
		}
		if (panel.getComponentCount() == 0) {
			panel.add(new JLabel(I18N.getMsg("warning.no.chapters")), MIG.GROW);
		}
		if (sceneCurrent != null) {
			ManageSceneDnd msc = sceneFind(sceneCurrent);
			if (msc != null) {
				sceneSelect(msc);
			}
		}
		revalidate();
		repaint();
	}

	@Override
	public JToolBar initToolbar() {
		super.initToolbar();
		ckHide = new JCheckBox(I18N.getMsg("preferences.manage.hide_unassigned"));
		ckHide.setName(HIDE_UNASSIGNED);
		ckHide.setSelected(App.preferences.manageGetHideUnassigned());
		ckHide.addActionListener((ActionEvent evt) -> {
			App.preferences.manageSetHideUnassgned(ckHide.isSelected());
			refresh();
		});
		toolbar.add(ckHide, MIG.GROWY);
		ckVertical = new JCheckBox(I18N.getMsg("vertical"));
		ckVertical.setName(VERTICAL);
		ckVertical.setSelected(App.preferences.manageGetVertical());
		ckVertical.addActionListener((ActionEvent evt) -> {
			App.preferences.manageSetVertical(ckVertical.isSelected());
			sl_columns.setEnabled(ckVertical.isSelected());
			refresh();
		});
		toolbar.add(ckVertical, MIG.GROWY);
		columnsPanel = new JPanel(new MigLayout());
		columnsPanel.setOpaque(false);
		columnsPanel.setBorder(SwingUtil.getBorderDefault());
		toolbar.add(columnsPanel);
		columnsPanel.add(new JLabel(I18N.getColonMsg("columns")));
		sl_columns = new JSlider(JSlider.HORIZONTAL, ZOOM_MIN, ZOOM_MAX, nbCols);
		sl_columns.setName(ZOOM_SLIDER);
		sl_columns.setMajorTickSpacing(1);
		sl_columns.setMinorTickSpacing(1);
		sl_columns.setOpaque(false);
		//sl_columns.setPaintTicks(true);
		sl_columns.addChangeListener(this);
		columnsPanel.add(sl_columns);
		sl_columns.setEnabled(ckVertical.isSelected());
		JPanel zoomPanel = new JPanel(new MigLayout());
		zoomPanel.setOpaque(false);
		zoomPanel.add(new JLabel(I18N.getColonMsg("zoom")));
		zoomPanel.add(btZoomOut = SwingUtil.createButton("", ICONS.K.MINUS, "zoom.out", e -> zoomAdjust(-1)));
		zoomPanel.add(lbZoom = new JLabel("" + zoom));
		zoomPanel.add(btZoomIn = SwingUtil.createButton("", ICONS.K.PLUS, "zoom.in", e -> zoomAdjust(1)));
		toolbar.add(zoomPanel);
		return (toolbar);
	}

	private void zoomAdjust(int n) {
		zoom += n;
		if (zoom < ZOOM_MIN) {
			zoom = ZOOM_MIN;
		}
		App.preferences.manageSetZoom(zoom);
		init();
		btZoomOut.setEnabled(zoom > ZOOM_MIN);
		btZoomIn.setEnabled(zoom < ZOOM_MAX);
		lbZoom.setText("" + zoom);
		refresh();
	}

	private static void dispatchToChapterPanels(Container cont, PropertyChangeEvent evt) {
		List<Component> ret = new ArrayList<>();
		SwingUtil.findComponentsByClass(cont, ManageChapter.class, ret);
		for (Component comp : ret) {
			ManageChapter panel = (ManageChapter) comp;
			panel.modelPropertyChange(evt);
		}
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
	public void stateChanged(ChangeEvent e) {
		Component comp = (Component) e.getSource();
		if (ZOOM_SLIDER.equals(comp.getName())) {
			JSlider slider = (JSlider) e.getSource();
			if (!slider.getValueIsAdjusting()) {
				App.preferences.manageSetColumns(slider.getValue());
				refresh();
			}
		}
	}

	public void sceneAdd(ManageSceneDnd msc) {
		scenes.add(msc);
	}

	public ManageSceneDnd sceneFind(Scene sc) {
		for (ManageSceneDnd msc : scenes) {
			if (msc.getScene() != null && msc.getScene().getId().equals(sc.getId())) {
				return msc;
			}
		}
		return null;
	}

	public void sceneSelect(Scene sc) {
		ManageSceneDnd msc = sceneFind(sc);
		if (msc != null) {
			sceneSelect(msc);
		}
	}

	public void sceneSelect(ManageSceneDnd p) {
		if (sceneSelected != null) {
			sceneSelected.setBorder(sceneSelected.borderDefault);
		}
		sceneSelected = p;
		sceneCurrent = p.getScene();
		p.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.focusColor"), 2));
		mainFrame.getBookController().infoSetTo(p.getScene());
	}

	public void sceneSelect(ManageScene srce) {
		sceneCurrent = srce.getScene();
		sceneSelect((ManageSceneDnd) srce);
	}

	public ManageSceneDnd sceneGetSelected() {
		return sceneSelected;
	}

	public boolean sceneIsSelected(ManageSceneDnd p) {
		if (sceneSelected == null) {
			return false;
		}
		return sceneSelected.equals(p);
	}

	public void sceneSetInformative() {
		//LOG.trace(TT + ".sceneSetUnassigned() for " + sceneSelected.getName());
		Scene scene = sceneSelected.getScene();
		scene.setInformative(true);
		mainFrame.getBookController().updateEntity(scene);
		mainFrame.setUpdated();
		sceneSelected = null;
		refreshData();
	}

	public void sceneSetInformative(ManageScene srce) {
		//LOG.trace(TT + ".sceneSetUnassigned(srce=" + srce.toString() + ")");
		if (srce.getScene() != null) {
			sceneSelect(srce);
			sceneSetInformative();
		}
	}

	public void sceneSetUnassigned() {
		//LOG.trace(TT + ".sceneSetUnassigned() for " + sceneSelected.getName());
		Scene scene = sceneSelected.getScene();
		Chapter chapter = scene.getChapter();
		scene.setChapter();
		mainFrame.getBookController().updateEntity(scene);
		mainFrame.project.scenes.renumber(mainFrame, chapter);
		sceneSelected = null;
		refreshData();
	}

	public void sceneSetUnassigned(ManageScene srce) {
		//LOG.trace(TT + ".sceneSetUnassigned(srce=" + srce.toString() + ")");
		if (srce.getScene() != null) {
			sceneSelect(srce);
			sceneSetUnassigned();
		}
	}

	public void sceneMoveToBegin(ManageSceneDnd srce, Chapter chapter) {
		//LOG.trace(TT + ".sceneMoveToBegin(srce=" + srce.getName()
		// + ", " + AbstractEntity.trace(chapter) + ")");
		Scene srceScene = srce.getScene();
		Chapter srceChapter = srceScene.getChapter();
		srceScene.setChapter(chapter);
		srceScene.setSceneno(-1);
		mainFrame.getBookController().updateEntity(srceScene);
		if (srce.getType() != ManageSceneDnd.TYPE.BEGIN
		   && !Objects.equals(srceChapter, chapter)) {
			mainFrame.project.scenes.renumber(mainFrame, srceChapter);
		}
		mainFrame.project.scenes.renumber(mainFrame, chapter);
		refreshData();
	}

	public void sceneMoveToNext(ManageSceneDnd srce, ManageSceneDnd dest) {
		//LOG.trace(TT + ".sceneMoveToNext(srce=" + srce.getName()
		//  + ", dest=" + dest.getName() + ")");
		Scene srceScene = srce.getScene();
		Chapter srceChapter = srceScene.getChapter();
		Scene destScene = dest.getScene();
		Chapter destChapter = destScene.getChapter();
		List<Scene> lscenes = mainFrame.project.scenes.findBy(destChapter);
		if (Objects.equals(srceChapter, destChapter)) {
			lscenes.remove(srceScene);
		}
		int idx = lscenes.indexOf(destScene);
		srceScene.setChapter(destChapter);
		srceScene.setSceneno(destScene.getSceneno() + 1);
		lscenes.add(idx, srceScene);
		mainFrame.getBookController().updateEntity(srceScene);
		if (!Objects.equals(srceChapter, destChapter)) {
			mainFrame.project.scenes.renumber(mainFrame, srceChapter);
		}
		mainFrame.project.scenes.renumber(mainFrame, destChapter);
		refreshData();
	}

	public void sceneSwap(ManageSceneDnd srce, ManageSceneDnd dest) {
		//LOG.trace(TT + ".sceneSwap(srce=" + srce.getName() + ", dest=" + dest.getName() + ")");
		Scene srceScene = srce.getScene();
		Scene destScene = dest.getScene();
		int originNum = srceScene.getSceneno();
		Chapter srceChapter = srceScene.getChapter();
		int destNum = destScene.getSceneno();
		Chapter destChapter = destScene.getChapter();
		srceScene.setSceneno(destNum);
		srceScene.setChapter(destChapter);
		mainFrame.getBookController().updateEntity(srceScene);
		destScene.setSceneno(originNum);
		destScene.setChapter(srceChapter);
		mainFrame.getBookController().updateEntity(destScene);
		refreshData();
	}

	public void sceneSetTarget(ManageSceneDnd target) {
		if (sceneTarget != null) {
			for (ManageSceneDnd msc : scenes) {
				msc.resetBackground();
			}
		}
		sceneTarget = target;
		sceneTarget.setBackground(SwingUtil.getSelectBackground());
	}

	public void sceneResetTarget(ManageSceneDnd target) {
		target.resetBackground();
		sceneTarget = null;
	}

	private void scrollToChapter(Chapter chapter) {
		if (chapter == null) {
			return;
		}
		final ScrollToEntityAction action = new ScrollToEntityAction(this, panel, chapter);
		Timer timer = new Timer(200, action);
		timer.setRepeats(false);
		timer.start();
	}

	private void scrollToScene(Scene scene) {
		if (scene == null) {
			return;
		}
		final ScrollToEntityAction action = new ScrollToEntityAction(this, panel, scene);
		Timer timer = new Timer(200, action);
		timer.setRepeats(false);
		timer.start();
	}

}
