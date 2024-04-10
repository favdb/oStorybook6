/*
 * Copyright (C) 2022 favdb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package assistant.app;

import api.mig.swing.MigLayout;
import assistant.Assistant;
import assistant.AssistantPanel;
import i18n.I18N;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import resources.icons.IconUtil;
import storybook.App;
import static storybook.App.preferences;
import storybook.Pref;
import storybook.tools.LOG;
import storybook.tools.swing.LaF;

/**
 *
 * @author favdb
 */
public class AppAssistant extends JDialog implements ActionListener {

	private static final String TT = "AssistantApp.";

	public static final String VERSION = "0.3", VERSION_YEAR = "2024";

	private static String filename = "";
	private static boolean subJar = false;
	private static AppAssistant app;
	private AssAction appAction;
	private AssMenu appMenu;
	private boolean modified = false;

	private AssistantPanel dataPanel;

	public static void show(String args) {
		subJar = true;
		java.awt.EventQueue.invokeLater(() -> {
			app = new AppAssistant();
			app.setVisible(true);
		});
	}

	public static void main(String args[]) {
		LOG.setTrace();
		App.initPref();
		LaF.init();
		String s = preferences.getString(Pref.KEY.ASSISTANT);
		if (!s.isEmpty()) {
			Assistant.init(s);
		}
		//LOG.trace(TT + "main(args=" + ListUtil.join(args) + ")");
		for (String str : args) {
			switch (str) {
				case "--sub":
					subJar = true;
					break;
				case "--file":
					File f = new File(str);
					if (f.exists() && str.toLowerCase().endsWith("xml")) {
						filename = str;
					}
					break;
			}
		}
		EventQueue.invokeLater(() -> {
			app = new AppAssistant();
			app.setVisible(true);
		});
	}

	public AssistantPanel getDataPanel() {
		return dataPanel;
	}

	public static AppAssistant getInstance() {
		return app;
	}

	public AppAssistant() {
		initAll();
	}

	public void initAll() {
		init();
		initUi();
	}

	public void init() {
		//LOG.trace(TT + "init()");
		setModal(true);
		File file = null;
		if (!filename.isEmpty()) {
			file = new File(filename);
			if (!file.exists()) {
				file = null;
			}
		}
		IconUtil.setDefSize();
		appAction = new AssAction(this);
		appMenu = new AssMenu(this);
		appMenu.setSaveEnable(false);
		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new MainFrameWindowAdaptor());
		I18N.initMsgProp();
		setIconImage(IconUtil.getIconImage("icon"));
		setName("mainDialog");
		setLayout(new MigLayout());
		setJMenuBar(appMenu);
		dataPanel = new AssistantPanel(this, true, null);
	}

	public void initUi() {
		//LOG.trace(TT + "initUi()");
		setAppTitle();
		dataPanel.reinit();
		add(dataPanel);
		pack();
		setLocationRelativeTo(null);
	}

	public AssAction getAppAction() {
		return appAction;
	}

	public AssMenu getAppMenu() {
		return appMenu;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	public void setModified(boolean b) {
		//LOG.trace(TT + "setModified(b=" + (b ? "true" : "false") + ")");
		modified = b;
		setAppTitle();
		appMenu.setSaveEnable(b);
	}

	public boolean isModified() {
		return modified;
	}

	public void setAppTitle() {
		String star = (modified ? "*" : "");
		this.setTitle(star + I18N.getMsg("assistant", Assistant.getLastOpened()) + star);
	}

	public void setSubjar() {
		subJar = true;
	}

	public void close() {
		//LOG.trace(TT + "close()");
		if (modified) {
			Object[] choix = {
				I18N.getMsg("cancel"),
				I18N.getMsg("file.save"),
				I18N.getMsg("ignore")
			};
			int n = JOptionPane.showOptionDialog(null,
			   I18N.getMsg("close.confirm"),
			   I18N.getMsg("exit"),
			   JOptionPane.YES_NO_OPTION,
			   JOptionPane.QUESTION_MESSAGE,
			   null, choix, choix[0]);
			if (n == 0) {
				return;
			}
			if (n == 1) {
				if (!appAction.fileSave()) {
					return;
				}
			}
		}
		if (subJar) {
			dispose();
		} else {
			System.exit(0);
		}
	}

	public void refreshAll() {
		dataPanel.reinit();
	}

	private class MainFrameWindowAdaptor extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent evt) {
			close();
		}

		@Override
		public void windowOpened(WindowEvent e) {
			// empty
		}

		@Override
		public void windowClosed(WindowEvent e) {
			// empty
		}

		@Override
		public void windowIconified(WindowEvent e) {
			// empty
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
			// empty
		}

		@Override
		public void windowActivated(WindowEvent e) {
			// empty
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
			// empty
		}
	}

}
