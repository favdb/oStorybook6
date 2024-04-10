/*
 * Copyright (C) 2023 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.ui.panel.story;

import api.mig.swing.MigLayout;
import assistant.AssistantDlg;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JLabel;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.tools.swing.LaF;
import storybook.ui.MIG;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public abstract class GraphAbstract extends AbstractPanel {

	private static final String TT = "GraphAbstract";

	public final StoryPanel storyPanel;
	public List<Story> stories = new ArrayList<>();

	public GraphAbstract(StoryPanel frame) {
		super(frame.getMainFrame());
		this.storyPanel = frame;
		initAll();
	}

	@Override
	public void initUi() {
		//LOG.trace(TT + ".initUi() for " + storyPanel.type.toString());
		setLayout(new MigLayout(MIG.get(MIG.FILL)));
		if (!LaF.isDark()) {
			setBackground(Color.white);
		}
		refresh();
	}

	@SuppressWarnings("unchecked")
	public void refreshData() {
		//LOG.trace(TT + ".refreshData() for " + storyPanel.type.toString());
		stories.clear();
		AssistantDlg dlg = new AssistantDlg(mainFrame, new Part());
		if (storyPanel.ckPartsIs()) {
			for (Part part : (List<Part>) mainFrame.project.parts.getList()) {
				Story f = new Story(storyPanel.type, dlg, part);
				if (f.getValue() > 0) {
					stories.add(f);
				}
			}
		}
		if (storyPanel.ckChaptersIs()) {
			for (Chapter chapter : (List<Chapter>) mainFrame.project.chapters.getList()) {
				Story f = new Story(storyPanel.type, dlg, chapter);
				if (f.getValue() > 0) {
					stories.add(f);
				}
			}
		}
		if (storyPanel.ckScenesIs()) {
			for (Scene scene : (List<Scene>) mainFrame.project.scenes.getList()) {
				Story f = new Story(storyPanel.type, dlg, scene);
				if (f.getValue() > 0) {
					stories.add(f);
				}
			}
		}
		Collections.sort(stories);
	}

	@Override
	public void refresh() {
		//LOG.trace(TT+".refresh()");
		removeAll();
		refreshData();
		drawStories();
		add(new JLabel(" "), MIG.posToString(1024, 768));// to force the size of this panel
	}

	public abstract void drawStories();

	public abstract void drawStory(Story fr, int n);

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// not used
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// not used
	}

}
