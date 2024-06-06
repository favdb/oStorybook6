/*
Storybook: Scene-based software for novelists and authors.
Copyright (C) 2008 - 2011 Martin Mustun

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

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import storybook.App;
import storybook.Pref;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.EntityUtil;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.scene.Scene;
import storybook.model.Model;
import storybook.tools.LOG;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSLabelVertical;
import storybook.ui.MIG;
import storybook.ui.interfaces.IRefreshable;
import storybook.ui.panel.AbstractPanel;

@SuppressWarnings("serial")
public class ManageChapter extends AbstractPanel implements MouseListener, IRefreshable {

	public ManagePanel manage;
	private Chapter chapter;
	private ManageSceneTransfer sceneTransferHandler;
	private int prefWidth;

	public ManageChapter(ManagePanel manage) {
		super(manage.mainFrame);
		this.manage = manage;
	}

	public ManageChapter(ManagePanel manage, Chapter chapter) {
		this(manage);
		this.chapter = chapter;
		initAll();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		Object newValue = evt.getNewValue();
		Object oldValue = evt.getOldValue();
		String propName = evt.getPropertyName();
		if (Ctrl.PROPS.MANAGE_ZOOM.check(propName)) {
			refresh();
			return;
		}
		ActKey act = new ActKey(propName);
		switch (Book.getTYPE(act.type)) {
			case CHAPTER:
				if (isUpdate(act)) {
					if (chapter == null) {
						return;
					}
					Chapter newChapter = (Chapter) newValue;
					if (!newChapter.getId().equals(chapter.getId())) {
						return;
					}
					chapter = newChapter;
					refresh();
				}
				break;
			case SCENE:
				if (isUpdate(act)) {
					Chapter newSceneChapter = ((Scene) newValue).getChapter();
					Chapter oldSceneChapter = ((Scene) oldValue).getChapter();
					if (newSceneChapter == null && chapter == null) {
						refresh();
						return;
					}
					if (chapter == null || newSceneChapter == null || oldSceneChapter == null) {
						refresh();
						return;
					}
					if (!newSceneChapter.getId().equals(chapter.getId())
					   && !oldSceneChapter.getId().equals(chapter.getId())) {
						return;
					}
					refresh();
				}
				break;
			case STRAND:
				if (isUpdate(act)) {
					refresh();
				}
				break;
			default:
				break;
		}

	}

	private void setZoomedSize(int zoomValue) {
		prefWidth = 50 + zoomValue * 10;
	}

	@Override
	public void init() {
		//LOG.trace(TT+".init()");
		try {
			setZoomedSize(App.preferences.manageGetZoom());
		} catch (Exception e) {
			LOG.err("ChapterPanel error", e);
			setZoomedSize(Pref.KEY.MANAGE_ZOOM.getInteger());
		}
	}

	@Override
	public void initUi() {
		//LOG.trace(TT+".initUI()");
		MigLayout layout = new MigLayout(MIG.get(MIG.INS0, MIG.GAP0));
		if (isForUnassignedScene()) {
			layout = new MigLayout(MIG.FLOWX, "[]", "[fill]");
		} else {
			if (App.preferences.manageGetVertical()) {
				layout = new MigLayout(MIG.get(MIG.GAP0, MIG.WRAP), "[grow]");
			}
		}
		setLayout(layout);
		setBorder(SwingUtil.getBorderDefault());
		if (!isForUnassignedScene()) {
			setPreferredSize(new Dimension(prefWidth, 80));
		}
		setComponentPopupMenu(EntityUtil.createPopupMenu(mainFrame, chapter, EntityUtil.WITH_CHRONO));
		StringBuilder buf = new StringBuilder();
		if (chapter == null) {
			JLabel lbChapter = new JLabel(I18N.getMsg("scenes.unassigned"));
			lbChapter.setUI(new JSLabelVertical(false));
			add(lbChapter, MIG.get(MIG.TOP));
		} else {
			JLabel lbChapter = new JLabel();
			buf.append(chapter.getChapternoStr());
			buf.append(" ");
			int tl = (manage.sceneSize.width / FontUtil.getWidth()) - 8;
			buf.append(chapter.getTitle(tl));
			lbChapter.setText(buf.toString());
			lbChapter.setToolTipText(chapter.getName());
			if (App.preferences.manageGetVertical()) {
				add(lbChapter);
			} else {
				add(lbChapter, MIG.get(MIG.SPAN, MIG.WRAP));
			}
			this.setToolTipText(chapter.getName());
		}
		sceneTransferHandler = new ManageSceneTransfer(manage);
		Model model = mainFrame.getBookModel();
		List<Scene> scenes = mainFrame.project.scenes.findBy(chapter);
		if (chapter == null) {
			// show all unassigned scenes
			for (Scene scene : scenes) {
				ManageSceneDnd dtScene = new ManageSceneDnd(this, scene, ManageSceneDnd.TYPE.UNASSIGNED);
				dtScene.setTransferHandler(sceneTransferHandler);
				add(dtScene);
			}
			// to make a scene unassigned
			ManageSceneDnd makeUnassigned = new ManageSceneDnd(this, null, ManageSceneDnd.TYPE.MAKE_UNASSIGNED);
			makeUnassigned.setTransferHandler(sceneTransferHandler);
			makeUnassigned.setMaximumSize(new Dimension(Short.MAX_VALUE, manage.sceneSize.height));
			makeUnassigned.setMinimumSize(manage.sceneSize);
			makeUnassigned.setPreferredSize(new Dimension(Short.MAX_VALUE, manage.sceneSize.height));
			add(makeUnassigned, MIG.GROWX);
		} else {
			ManageSceneDnd begin = new ManageSceneDnd(this, null, ManageSceneDnd.TYPE.BEGIN);
			begin.setTransferHandler(sceneTransferHandler);
			if (scenes.isEmpty()) {
				SwingUtil.setForcedSize(begin, manage.sceneSize);
			}
			add(begin);
			/*if (!mainFrame.getPref().manageGetVertical()) {
				add(begin, MIG.GROWX);
			} else {
				add(begin, MIG.CENTER);
			}*/
			int i = 0;
			for (Scene scene : scenes) {
				// scene
				ManageSceneDnd dtScene = new ManageSceneDnd(this, scene);
				dtScene.setTransferHandler(sceneTransferHandler);
				add(dtScene, MIG.GROWX);
				// move next
				ManageSceneDnd next = new ManageSceneDnd(this, scene, ManageSceneDnd.TYPE.NEXT);
				next.setTransferHandler(sceneTransferHandler);
				add(next);
				++i;
			}
		}
	}

	public boolean isForUnassignedScene() {
		return chapter == null;
	}

	protected ManageChapter getThis() {
		return this;
	}

	public Chapter getChapter() {
		return chapter;
	}

	public List<ManageSceneDnd> getDTScenePanels() {
		List<ManageSceneDnd> list = new ArrayList<>();
		for (Component comp : getComponents()) {
			if (comp instanceof ManageSceneDnd && ((ManageSceneDnd) comp).getScene() != null) {
				list.add((ManageSceneDnd) comp);
			}
		}
		return list;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (getChapter() == null) {
			return;
		}
		requestFocusInWindow();
		if (e.getClickCount() == 2) {
			EntityUtil.createEntity(mainFrame, getChapter());
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	void refreshUnassigned() {
		this.removeAll();
		JLabel lbChapter = new JLabel(I18N.getMsg("scenes.unassigned"));
		lbChapter.setUI(new JSLabelVertical(false));
		add(lbChapter);
		List<Scene> scenes = mainFrame.project.scenes.findBy((Chapter) null);
		for (Scene scene : scenes) {
			if (scene.getChapter() == null) {
				ManageSceneDnd dtScene = new ManageSceneDnd(this, scene, ManageSceneDnd.TYPE.UNASSIGNED);
				dtScene.setTransferHandler(sceneTransferHandler);
				SwingUtil.setForcedSize(dtScene, manage.sceneSize);
				add(dtScene, MIG.GROWY);
			}
		}
		// to make a scene unassigned
		ManageSceneDnd unassigned = new ManageSceneDnd(this, null, ManageSceneDnd.TYPE.MAKE_UNASSIGNED);
		unassigned.setTransferHandler(sceneTransferHandler);
		unassigned.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		unassigned.setMinimumSize(new Dimension(Short.MAX_VALUE, manage.sceneSize.height));
		unassigned.setPreferredSize(manage.sceneSize);
		add(unassigned, MIG.GROWX);
	}

}
