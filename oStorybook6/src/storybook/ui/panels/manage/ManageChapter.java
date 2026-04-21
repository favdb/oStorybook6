/*
Storybook: Scene-based software for novelists and authors.
Copyright (C) 2025 favdb

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
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import storybook.App;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.EntityUtil;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.scene.Scene;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.interfaces.IRefreshable;
import storybook.ui.panels.AbstractPanel;

/**
 * class for showing a Chapter for Manage view
 *
 * @author favdb
 */
@SuppressWarnings("serial")
public class ManageChapter extends AbstractPanel implements MouseListener, IRefreshable {

	private static final String TT = "ManageChapter.";

	public Manage manage;
	public Chapter chapter;
	private ManageTransfer transferHandler;
	private JPanel panel;
	private JScrollPane scroll;

	public ManageChapter(Manage manage, Chapter chapter) {
		super(manage.mainFrame);
		this.manage = manage;
		this.chapter = chapter;
		initAll();
	}

	/**
	 * initialize the class
	 */
	@Override
	public void init() {
		// empty
	}

	/**
	 * initialize the user interface
	 */
	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0)));
		transferHandler = new ManageTransfer(manage);
		panel = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0)));
		scroll = new JScrollPane(panel);
		scroll.setMinimumSize(manage.sceneSize);
		add(scroll, MIG.GROW);
		if (isForUnassignedScene()) {
			refreshUnassigned();
		} else {
			refreshChapter();
		}
		setTitle();
		Dimension d = manage.getScrollSize();
		d.height += manage.fontSize;
		this.setMinimumSize(d);
	}

	/**
	 * set the title of the chapter
	 */
	private void setTitle() {
		StringBuilder buf = new StringBuilder();
		MigLayout layout = new MigLayout(MIG.get(MIG.INS1, MIG.GAP1));
		if (isForUnassignedScene()) {
			layout = new MigLayout(MIG.get(MIG.INS0, MIG.GAP0));
			buf.append(I18N.getMsg("scenes.unassigned"));
		} else {
			if (App.preferences.manageGetVertical()) {
				layout = new MigLayout(MIG.get(MIG.INS0, MIG.GAP0, MIG.WRAP1), "[grow]");
			}
			buf.append(chapter.getName());
			this.setToolTipText(chapter.getName());
			setComponentPopupMenu(EntityUtil.createPopupMenu(mainFrame, chapter, EntityUtil.WITH_CHRONO));
		}
		setBorder(BorderFactory.createTitledBorder(buf.toString()));
		panel.setLayout(layout);
	}

	/**
	 * check if this is an unassigned scene
	 *
	 * @return
	 */
	public boolean isForUnassignedScene() {
		return chapter == null;
	}

	/**
	 * get this component
	 *
	 * @return
	 */
	protected ManageChapter getThis() {
		return this;
	}

	/**
	 * get the Chapter
	 *
	 * @return
	 */
	public Chapter getChapter() {
		return chapter;
	}

	/**
	 * get the scenes panel
	 *
	 * @return
	 */
	public List<ManageSceneDnd> getScenePanels() {
		List<ManageSceneDnd> list = new ArrayList<>();
		for (Component comp : getComponents()) {
			if (comp instanceof ManageSceneDnd && ((ManageSceneDnd) comp).scene != null) {
				list.add((ManageSceneDnd) comp);
			}
		}
		return list;
	}

	/**
	 * refresh the scenes in the chapter
	 */
	void refreshChapter() {
		setBorder(BorderFactory.createTitledBorder(chapter.getName()));
		setTitle();
		panel.removeAll();
		List<Scene> scenes = mainFrame.project.scenes.find(chapter);
		ManageSceneDnd begin = new ManageSceneDnd(this, null,
				ManageSceneDnd.TYPE.BEGIN, transferHandler);
		if (scenes.isEmpty()) {
			SwingUtil.setFixedSize(begin, manage.sceneSize);
		}
		panel.add(begin);
		for (Scene scene : scenes) {
			ManageSceneDnd dnd = new ManageSceneDnd(this, scene, transferHandler);
			if (dnd.getTransferHandler() == null) {
				dnd.setTransferHandler(transferHandler);
			}
			panel.add(dnd, MIG.GROWX);
			ManageSceneDnd next = new ManageSceneDnd(this, scene,
					ManageSceneDnd.TYPE.AFTER, transferHandler);
			if (next.getTransferHandler() == null) {
				next.setTransferHandler(transferHandler);
			}
			panel.add(next);
		}
		scroll.revalidate();
		scroll.repaint();
	}

	/**
	 * refresh the unassigned scenes
	 */
	void refreshUnassigned() {
		panel.removeAll();
		List<Scene> scenes = mainFrame.project.scenes.findUnassigned();
		ManageSceneDnd udnd = new ManageSceneDnd(this, null,
				ManageSceneDnd.TYPE.MAKE_UNASSIGNED, transferHandler);
		if (udnd.getTransferHandler() == null) {
			udnd.setTransferHandler(transferHandler);
		}
		if (scenes.isEmpty()) {
			SwingUtil.setFixedSize(udnd, manage.sceneSize);
		}
		panel.add(udnd);
		for (Scene scene : scenes) {
			if (scene.getChapter() == null) {
				ManageSceneDnd dnd = new ManageSceneDnd(this, scene,
						ManageSceneDnd.TYPE.UNASSIGNED, transferHandler);
				if (dnd.getTransferHandler() == null) {
					dnd.setTransferHandler(transferHandler);
				}
				SwingUtil.setFixedSize(dnd, manage.sceneSize);
				panel.add(dnd);
				ManageSceneDnd nextUdnd = new ManageSceneDnd(this, null,
						ManageSceneDnd.TYPE.MAKE_UNASSIGNED, transferHandler);
				if (nextUdnd.getTransferHandler() == null) {
					nextUdnd.setTransferHandler(transferHandler);
				}
				panel.add(nextUdnd);
			}
		}
		scroll.revalidate();
		scroll.repaint();
	}

	//// mouse gesture
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

	//// common methods
	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		Object newValue = evt.getNewValue();
		String propName = evt.getPropertyName();
		if (Ctrl.PROPS.MANAGE_ZOOM.check(propName)) {
			refresh();
			return;
		}
		ActKey act = new ActKey(propName);
		switch (Book.getTYPE(act.type)) {
			case CHAPTER:
				if (isUpdate(act)) {
					if (isForUnassignedScene()) {
						refreshUnassigned();
						return;
					}
					Chapter newChapter = (Chapter) newValue;
					if (!newChapter.getId().equals(chapter.getId())) {
						return;
					}
					chapter = newChapter;
					refreshChapter();
				}
				break;
			case SCENE:
				if (isUpdate(act)) {
					Chapter newSceneChapter = ((Scene) newValue).getChapter();
					if (newSceneChapter == null) {
						refreshUnassigned();
						return;
					}
					if (newSceneChapter.equals(chapter)) {
						refreshChapter();
					}
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

}
