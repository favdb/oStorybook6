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
package storybook.action;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import storybook.ctrl.ActKey;
import storybook.db.book.Book;
import storybook.tools.LOG;
import storybook.ui.MainFrame;
import storybook.ui.MainMenu;

/**
 * @author martin
 *
 */
public class MainActions implements PropertyChangeListener {

	private static final String TT = "MainActions.";

	//private ActionManager actionManager;
	private final MainFrame mainFrame;
	private MainMenu mainMenu;
	private Dimension menuDim;

	public MainActions(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	/**
	 * initialize the main actions
	 */
	public void init() {
		//LOG.trace(TT+"init()");
		//initActions();
		initUiFactory();
		if (mainFrame.isBlank()) {
			mainMenu.setMenuForBlank();
		}
	}

	/**
	 * initialize the UI
	 */
	private void initUiFactory() {
		//LOG.trace(TT+"initUiFactory()");
		mainMenu = new MainMenu(mainFrame);
		JMenuBar menubar = mainMenu.menuBar;
		if (menubar != null) {
			mainMenu.reloadRecentMenu();
			mainMenu.reloadWindowMenu();
			mainFrame.setJMenuBar(menubar);
		} else {
			LOG.err(TT + "initUiFactory() *** General error : unable to load main menu");
		}
		JToolBar toolBar = mainMenu.toolBar;
		if (toolBar != null) {
			toolBar.setName("MainToolbar");
			mainFrame.setMainToolBar(toolBar);
		}
		mainFrame.invalidate();
		mainFrame.validate();
		mainFrame.pack();
		mainFrame.repaint();
	}

	/**
	 * relaod the menu bar
	 */
	public void reloadMenuToolbar() {
		if (mainMenu != null) {
			mainMenu.reloadToolbar();
		}
	}

	/**
	 * property change
	 *
	 * @param evt
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		//LOG.trace(TT+".propertyChange(" + evt.getPropertyName() + "::" + evt.getNewValue() + ")");
		ActKey act = new ActKey(evt);
		if (!Book.TYPE.PART.compare(act.type)) {
			return;
		}
		reloadMenuToolbar();
	}

	public MainMenu getMainMenu() {
		return mainMenu;
	}

}
