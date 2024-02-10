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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import resources.icons.ICONS;
import storybook.App;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public class StoryPanel extends AbstractPanel {

	public final STORYTYPE type;
	public List<Story> stories;
	private JCheckBox ckParts, ckChapters, ckScenes;
	public GraphAbstract graph;
	private String oldOptions = "";

	public enum ACT {
		CK_PARTS, CK_CHAPTERS, CK_SCENES, NONE;
	}

	public enum STORYTYPE {
		THREE, FREYTAG, VOGLER;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	public StoryPanel(MainFrame mainFrame, STORYTYPE type) {
		super(mainFrame);
		this.type = type;
	}

	@Override
	public void init() {
		//not used
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.FILLX));
		setPreferredSize(new Dimension(1024, 800));
		add(initToolbar(), MIG.get(MIG.SPAN, MIG.GROWX));
		switch (type) {
			case THREE:
				graph = new GraphThree(this);
				break;
			case FREYTAG:
				graph = new GraphFreytag(this);
				break;
			case VOGLER:
				graph = new GraphVogler(this);
		}
		JScrollPane scroll = new JScrollPane(graph);
		add(scroll, MIG.GROW);
	}

	@Override
	public JToolBar initToolbar() {
		super.initToolbar();
		toolbar.setLayout(new MigLayout(MIG.get(MIG.HIDEMODE3)));
		toolbar.setFloatable(false);
		// JButton to create a new Chapter
		toolbar.add(Ui.initButton("btCreate", "", ICONS.K.NEW_CHAPTER, "new.chapter", e -> createChapter()));
		JPanel ckPanel = new JPanel(new MigLayout());
		// add JCheckBox for Parts
		ckParts = Ui.initCheckBox(null, "ckParts", "parts", true, "");
		ckPanel.add(ckParts);
		// add JCheckBox for Chapters
		ckChapters = Ui.initCheckBox(null, "ckChapters", "chapters", true, "");
		ckPanel.add(ckChapters);
		// add JCheckBox for Scenes
		ckScenes = Ui.initCheckBox(null, "ckScenes", "scenes", true, "");
		ckPanel.add(ckScenes);
		toolbar.add(ckPanel);
		oldOptions = App.preferences.storyGetOptions(type.toString());
		ckParts.setSelected(oldOptions.charAt(0) == '1');
		ckChapters.setSelected(oldOptions.charAt(1) == '1');
		ckScenes.setSelected(oldOptions.charAt(2) == '1');
		ckParts.addActionListener(e -> {
			refresh();
		});
		ckChapters.addActionListener(e -> {
			refresh();
		});
		ckScenes.addActionListener(e -> {
			refresh();
		});
		return toolbar;
	}

	public boolean ckPartsIs() {
		return (ckParts != null ? ckParts.isSelected() : false);
	}

	public boolean ckChaptersIs() {
		return (ckChapters != null ? ckChapters.isSelected() : false);
	}

	public boolean ckScenesIs() {
		return (ckScenes != null ? ckScenes.isSelected() : false);
	}

	private void createChapter() {
		if (StoryNewChapter.show(this)) {
			refresh();
		}
	}

	private void saveOptions() {
		String s = (ckParts.isSelected() ? "1" : "0")
		   + (ckChapters.isSelected() ? "1" : "0")
		   + (ckScenes.isSelected() ? "1" : "0");
		if (!s.equals(oldOptions)) {
			App.preferences.storySetOptions(type.toString(), s);
			mainFrame.setUpdated();
			oldOptions = s;
		}
	}

	@Override
	public void refresh() {
		//LOG.trace(this, ".refresh()");
		saveOptions();
		graph.refresh();
		revalidate();
		repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		//LOG.trace("for " + type.toString() + "\n" + TT + ".modelPropertyChange(evt=" + evt.toString() + ")");
		if (evt.getNewValue() instanceof Part
		   || evt.getNewValue() instanceof Chapter
		   || evt.getNewValue() instanceof Scene) {
			refresh();
		} else {
			String pname = evt.getPropertyName();
			String nv = evt.getNewValue().toString().toLowerCase();
			if (("REFRESH".equals(pname) && nv.contains(type.toString().toLowerCase()))) {
				refresh();
			}
		}
	}

}
