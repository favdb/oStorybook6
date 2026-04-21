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
package storybook.ui.panels.tell;

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
import storybook.ui.frames.main.MainFrame;
import storybook.ui.Ui;
import storybook.ui.panels.AbstractPanel;

/**
 *
 * @author favdb
 */
public class TellingPanel extends AbstractPanel {

    private static final String TT = "StoryPanel.";

    public final TELLTYPE type;
    public List<Storytelling> stories;
    private JCheckBox ckParts, ckChapters, ckScenes;
    public TellingAbstract graph;
    private String oldOptions = "";

    public enum ACT {
	CK_PARTS, CK_CHAPTERS, CK_SCENES, NONE;
    }

    public enum TELLTYPE {
	THREE, FREYTAG, VOGLER;

	@Override
	public String toString() {
	    return name().toLowerCase();
	}
    }

    public TellingPanel(MainFrame mainFrame, TELLTYPE type) {
	super(mainFrame);
	this.type = type;
    }

    /**
     * initialize
     */
    @Override
    public void init() {
	//not used
    }

    /**
     * initialize the UI
     */
    @Override
    public void initUi() {
	setLayout(new MigLayout(MIG.FILLX));
	setPreferredSize(new Dimension(1024, 800));
	add(initToolbar(), MIG.get(MIG.SPAN, MIG.GROWX));
	switch (type) {
	    case THREE:
		graph = new TellingThree(this);
		break;
	    case FREYTAG:
		graph = new TellingFreytag(this);
		break;
	    case VOGLER:
		graph = new TellingVogler(this);
	}
	JScrollPane scroll = new JScrollPane(graph);
	add(scroll, MIG.GROW);
    }

    /**
     * initialize the tool bar
     *
     * @return
     */
    @Override
    public JToolBar initToolbar() {
	super.initToolbar();
	toolbar.setLayout(new MigLayout(MIG.get(MIG.HIDEMODE3)));
	toolbar.setFloatable(false);
	// JButton to create a new Chapter
	toolbar.add(Ui.initButton("btCreate", "", ICONS.K.NEW_CHAPTER,
		"new.chapter", e -> createChapter()));
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

    /**
     * check if a part is selected
     *
     * @return
     */
    public boolean ckPartsIs() {
	return (ckParts != null ? ckParts.isSelected() : false);
    }

    /**
     * chack if a chapter is selected
     *
     * @return
     */
    public boolean ckChaptersIs() {
	return (ckChapters != null ? ckChapters.isSelected() : false);
    }

    /**
     * check if a scene is selected
     *
     * @return
     */
    public boolean ckScenesIs() {
	return (ckScenes != null ? ckScenes.isSelected() : false);
    }

    /**
     * call the Chapter creation
     */
    private void createChapter() {
	if (TellingNewChapter.show(this)) {
	    refresh();
	}
    }

    /**
     * save the options
     */
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

    /**
     * refresh
     */
    @Override
    public void refresh() {
	//LOG.trace(TT + "refresh()");
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
	//LOG.trace(TT + "modelPropertyChange(evt=" + evt.toString() + ") for " + type.toString());
	String pname = evt.getPropertyName();
	if ("SHOWINFO".equals(pname)) {
	    return;
	}
	Object obj = (evt.getNewValue() != null ? evt.getNewValue() : evt.getOldValue());
	if ("REFRESH".equals(pname)
		|| obj instanceof Part
		|| obj instanceof Chapter
		|| obj instanceof Scene) {
	    refresh();
	}
    }

}
