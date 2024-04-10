/*
 * Copyright (C) 2023 favdb
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
package storybook.tools;

import i18n.I18N;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.tools.exec.CallProcess;
import storybook.tools.file.EnvUtil;
import static storybook.tools.file.EnvUtil.getHomeDir;

/**
 *
 * @author favdb
 */
public class Dictaphone {

	private static boolean exists;
	private static boolean started;

	public Dictaphone() {
		// empty
	}

	/**
	 * initialize the dictaphone
	 */
	public static void init() {
		//LOG.trace(TT + ".initDictaphone()");
		File f = new File(getHomeDir().getAbsolutePath()
			+ File.separator + ".storybook5/dictaphone");
		exists = f.exists();
	}

	/**
	 * check if dictaphone is installed
	 *
	 * @return
	 */
	public static boolean installed() {
		if (!App.isDev()) {
			return false;
		}
		return exists;
	}

	/**
	 * check if the dictaphone is started
	 *
	 * @return
	 */
	public static boolean isStarted() {
		return started;
	}

	/**
	 * Intialize the menu item for start/stop the dictaphone
	 *
	 * @param it the menu item to initialize
	 */
	public static void start(JMenuItem it) {
		if (started) {
			started = false;
			it.setText(I18N.getMsg("dictaphone.start"));
			it.setIcon(IconUtil.getIconSmall(ICONS.K.DICTAPHONE_START));
		} else {
			started = true;
			it.setText(I18N.getMsg("dictaphone.stop"));
			it.setIcon(IconUtil.getIconSmall(ICONS.K.DICTAPHONE_STOP));
		}
	}

	/**
	 * Initialize the button for start/stop the dictaphone
	 *
	 * @param it
	 */
	public static void start(JButton it) {
		String cmd = EnvUtil.getPrefDir().getAbsolutePath()
			+ File.separator + "dictaphone"
			+ File.separator + "nerd-dictation"
			+ File.separator + "nerd-dictation.py";
		if (started) {
			started = false;
			it.setToolTipText(I18N.getMsg("dictaphone.start"));
			it.setIcon(IconUtil.getIconSmall(ICONS.K.DICTAPHONE_START));
			CallProcess.runCommand("python3", cmd, "end");
		} else {
			started = true;
			it.setToolTipText(I18N.getMsg("dictaphone.stop"));
			it.setIcon(IconUtil.getIconSmall(ICONS.K.DICTAPHONE_STOP));
			CallProcess.runCommand("python3", cmd, "begin",
				"--continuous",
				"--full-sentence",
				"--punctuate-from-previous-timeout", "2",
				"--idle-time", "0.1"
			);
		}
	}

}
