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
package storybook.ui.panel.book;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.JPanel;
import api.mig.swing.MigLayout;
import storybook.db.EntityUtil;
import storybook.db.scene.Scene;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.panel.AbstractScenePanel;

@SuppressWarnings("serial")
public class BookScenePanel extends AbstractScenePanel {

	private JPanel cmdPanel;
	private BookTextPanel textPanel;
	private BookInfoPanel infoPanel;

	public BookScenePanel(MainFrame mainFrame, Scene scene) {
		super(mainFrame, scene);
		init();
		initUi();
	}

	public JPanel getCmdPanel() {
		return cmdPanel;
	}

	public JPanel getInfoPanel() {
		return infoPanel;
	}

	public JPanel getTextPanel() {
		return textPanel;
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.INS1, MIG.GAP1)));
		setOpaque(false);
		setComponentPopupMenu(EntityUtil.createPopupMenu(mainFrame, scene, EntityUtil.WITH_CHRONO));
		refresh();
	}

	@Override
	public void refresh() {
		removeAll();
		infoPanel = new BookInfoPanel(mainFrame, scene);
		textPanel = new BookTextPanel(mainFrame, scene);
		cmdPanel = createCommandPanel();
		add(infoPanel);
		add(textPanel, MIG.GROWX);
		add(cmdPanel);
		revalidate();
		repaint();
	}

	private JPanel createCommandPanel() {
		JPanel panel = new JPanel(new MigLayout(MIG.get(MIG.FLOWY, MIG.INS0)));
		panel.setOpaque(false);
		// layout
		panel.add(getEditButton());
		panel.add(getDeleteButton());
		panel.add(getNewButton());
		return panel;
	}

	protected BookScenePanel getThis() {
		return this;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
