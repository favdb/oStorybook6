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
package storybook.ui.panel.storyboard;

import api.mig.swing.MigLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import resources.icons.IconUtil;
import storybook.App;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractPanel;

@SuppressWarnings("serial")
public class StoryboardChapter extends AbstractPanel {

	private final Chapter chapter;

	public StoryboardChapter(MainFrame mainFrame, Chapter chapter) {
		super(mainFrame);
		this.chapter = chapter;
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		boolean layoutDirection = App.preferences.storyboardGetDirection();
		try {
			setLayout(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0), "[]", "[]"));
			setOpaque(false);
			// date
			JLabel lbChapter = new JLabel(chapter.getIcon());
			lbChapter.setText(chapter.getName());
			lbChapter.setToolTipText(chapter.getFullName());
			add(lbChapter, "wrap");
			// scenes by chapter
			List<Scene> sceneList = mainFrame.project.scenes.find(chapter);
			if (sceneList.isEmpty()) {
				StoryboardSpace spacePanel = new StoryboardSpace(mainFrame, chapter);
				add(spacePanel, "grow");
			} else {
				ImageIcon img = IconUtil.getImageIcon("striph");
				//img = IconUtil.resizeIcon(img, IconUtil.getDefSize());
				int w = img.getIconWidth() - 8;
				lbChapter.setMaximumSize(new Dimension(w, IconUtil.getDefSize()));
				MigLayout layout2 = new MigLayout(MIG.get(MIG.INS0, MIG.GAP0));
				JPanel colPanel = new JPanel(layout2);
				//colPanel.setOpaque(false);
				for (Scene scene : sceneList) {
					if (scene.getStrand() == null) {
						scene.setStrand((Strand) mainFrame.project.strands.getFirst());
					}
					StoryboardScene csp = new StoryboardScene(mainFrame, scene);
					if (layoutDirection) {
						colPanel.add(csp);
					} else {
						colPanel.add(csp, "wrap");
					}
				}
				add(colPanel);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		ActKey act = new ActKey(evt);
		if (Book.TYPE.SCENE.compare(act.type)
		   && (Ctrl.PROPS.NEW.check(act.getCmd()) || Ctrl.PROPS.DELETE.check(act.getCmd()))) {
			refresh();
			if (getParent() != null) {
				getParent().validate();
			} else {
				validate();
			}
		}
	}

}
