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
package storybook.tools;

import i18n.I18N;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import api.infonode.docking.DockingWindow;
import api.infonode.docking.RootWindow;
import api.infonode.docking.TabWindow;
import api.infonode.docking.View;
import api.infonode.docking.WindowBar;
import api.infonode.docking.properties.TabWindowProperties;
import api.infonode.docking.util.StringViewMap;
import resources.MainResources;
import storybook.App;
import storybook.Pref;
import storybook.ctrl.Ctrl;
import storybook.tools.file.EnvUtil;
import storybook.ui.MainFrame;
import storybook.ui.SbView;
import storybook.ui.SbViewFactory;
import storybook.ui.panel.AbstractPanel;

/**
 * class to manage the layout
 *
 * @author martin
 *
 */
public class DockUtil {

	private static final String TT = "DockUtil",
		LAYOUT_EXT = ".layout";

	private DockUtil() {
		// nothing
	}

	/**
	 * set the respect minimum size
	 *
	 * @param mainFrame
	 */
	public static void setRespectMinimumSize(MainFrame mainFrame) {
		SbView view = mainFrame.getView(SbView.VIEWNAME.TREE);
		DockingWindow win = view.getWindowParent();
		if (win instanceof TabWindow) {
			TabWindowProperties props = new TabWindowProperties();
			props.setRespectChildWindowMinimumSize(true);
			((TabWindow) win).getTabWindowProperties().addSuperObject(props);
		} else if (win instanceof WindowBar) {
			// can't be set for a WindowBar
		}
	}

	/**
	 * set the layout from a binary data
	 *
	 * @param mainFrame
	 * @param hex
	 */
	public static void layoutSet(MainFrame mainFrame, String hex) {
		mainFrame.cursorSetWaiting();
		byte[] ba = hex.getBytes();
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(ba))) {
			RootWindow rootWindow = mainFrame.getRootWindow();
			rootWindow.read(in, true);
		} catch (NullPointerException | IOException e) {
			LOG.err(TT + ".layoutSet(...) error ");
		}
		setRespectMinimumSize(mainFrame);
		unloadHiddenViews(mainFrame);
		mainFrame.cursorSetDefault();
	}

	/**
	 * unload the hiden views
	 *
	 * @param mainFrame
	 */
	public static void unloadHiddenViews(MainFrame mainFrame) {
		SbViewFactory viewFactory = mainFrame.getViewFactory();
		StringViewMap viewMap = viewFactory.getViewMap();
		Ctrl documentCtrl = mainFrame.getBookController();
		int c = viewMap.getViewCount();
		for (int i = 0; i < c; ++i) {
			View view = viewMap.getViewAtIndex(i);
			if (view instanceof SbView) {
				SbView sbView = (SbView) view;
				if (sbView.isWindowShowing()) { // window is showing
					if (!sbView.isLoaded()) {
						viewFactory.loadView(sbView);
						documentCtrl.attachView((AbstractPanel) view.getComponent());
					}
				} else // window is not showing
				if (sbView.isLoaded()) {
					documentCtrl.detachView((AbstractPanel) view.getComponent());
					viewFactory.unloadView(sbView);
				}
			}
		}
	}

	/**
	 * save the layout to the preference directory
	 *
	 * @param mainFrame
	 * @param name
	 */
	public static void layoutSave(MainFrame mainFrame, String name) {
		try {
			RootWindow rootWindow = mainFrame.getRootWindow();
			if (rootWindow == null) {
				return;
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
				rootWindow.write(out, false);
			}
			byte[] ba = bos.toByteArray();
			File f = new File(EnvUtil.getPrefDir().getAbsolutePath()
				+ File.separator + name + LAYOUT_EXT);
			try (FileOutputStream fos = new FileOutputStream(f)) {
				fos.write(ba);
			}
			List<String> list = App.preferences.dockingGetList();
			list.add(name);
			App.preferences.dockingSetList(Pref.KEY.DOCKING_LAYOUT, list);
			App.preferences.setString(Pref.KEY.LAYOUT_LAST_USED, name);
			App.getInstance().reloadMenuBars();
			App.getInstance().reloadStatusBars();
		} catch (IOException e) {
			LOG.err("*** " + TT + ".saveLoayout(" + mainFrame.getName()
				+ "," + name + ") Exception", e);
		}
	}

	/**
	 * get the menu layout
	 *
	 * @param mainFrame
	 * @param menu
	 * @return
	 */
	public static JMenu layoutGetMenu(MainFrame mainFrame, JMenu menu) {
		String[] list = {
			"default",
			"only_book",
			"only_chrono",
			"only_manage",
			"only_reading",
			"persons_locations",
			"tags_items",
			"screenplay",
			"theaterplay",
			"storyboard"
		};
		menu.removeAll();
		for (String key : list) {
			JMenuItem m = new JMenuItem();
			m.setFont(App.getInstance().fonts.defGet());
			m.setName(key);
			m.setText(I18N.getMsg("docking.layout." + key));
			m.addActionListener((ActionEvent evt) -> {
				layoutLoadFromResource(mainFrame, key);
			});
			menu.add(m);
		}
		return (menu);
	}

	/**
	 * load a layout from the preference directory
	 *
	 * @param mainFrame
	 * @param name
	 * @return
	 */
	public static boolean layoutLoadFromFile(MainFrame mainFrame, String name) {
		//LOG.trace(TT+".loadLayout(mainFrame, name='"+name+"')");
		try {
			Path path = Paths.get(EnvUtil.getPrefDir().getAbsolutePath()
				+ File.separator + name + LAYOUT_EXT);
			if (!path.toFile().exists()) {
				LOG.err("Layout not find: " + path.toString());
				return false;
			}
			byte[] ba = Files.readAllBytes(path);
			try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(ba))) {
				RootWindow rootWindow = mainFrame.getRootWindow();
				rootWindow.read(in, true);
			} catch (Exception ex) {
				LOG.err("unable to restore layout, setting default");
				layoutLoadFromResource(mainFrame, "default");
				return (false);
			}
			mainFrame.cursorSetWaiting();
			setRespectMinimumSize(mainFrame);
			unloadHiddenViews(mainFrame);
			mainFrame.cursorSetDefault();
			return true;
		} catch (IOException ex) {
			LOG.err("*** " + TT + ".loadLoayout(" + mainFrame.getName()
				+ "," + name + ") Exception :" + ex.getMessage());
		}
		return false;
	}

	/**
	 * load layout from a resource file
	 *
	 * @param mainFrame
	 * @param name
	 */
	public static void layoutLoadFromResource(MainFrame mainFrame, String name) {
		LOG.trace(TT + ".layoutLoadFile(mainframe, name='" + name + "')");
		String x = "layout/" + name + LAYOUT_EXT;
		try {
			InputStream stream = MainResources.class.getResourceAsStream(x);
			BufferedInputStream buf = new BufferedInputStream(stream);
			byte[] b = new byte[stream.available()];
			int r = buf.read(b);
			ObjectInputStream bis = new ObjectInputStream(new ByteArrayInputStream(b));
			if (b != null && r > 0) {
				try (ObjectInputStream in = bis) {
					RootWindow rootWindow = mainFrame.getRootWindow();
					rootWindow.read(in, true);
				}
				mainFrame.cursorSetWaiting();
				setRespectMinimumSize(mainFrame);
				unloadHiddenViews(mainFrame);
				mainFrame.cursorSetDefault();
			}
		} catch (IOException ex) {
			LOG.err("unable to load docking resource : '" + x + "'");
		}
	}
}
