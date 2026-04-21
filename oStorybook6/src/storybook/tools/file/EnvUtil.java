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
package storybook.tools.file;

import i18n.I18N;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import storybook.Const;
import storybook.ui.frames.main.MainFrame;

/**
 * @author martin
 *
 * mod FaVdB
 *
 * Tools to get environement data file
 */
public class EnvUtil {

	public static File getTempDir() {
		return new File(System.getProperty("java.io.tmpdir"));
	}

	public static File getHomeDir() {
		return new File(System.getProperty("user.home"));
	}

	public static String getDefaultExportDir(MainFrame mainFrame) {
		return mainFrame.getProject().getFile().getParent();
	}

	public static File getUserDir() {
		return new File(System.getProperty("user.dir"));
	}

	public static File getPrefDir() {
		return new File(System.getProperty("user.home") + File.separator
		   + ".ostorybook" + Const.STORYBOOK.VERSION_MAJOR.toString());
	}

	public static File getIniFile() {
		return (new File(getPrefDir().getAbsolutePath() + File.separator + "oStorybook.ini"));
	}

	public static File getIdeaboxFile() {
		return (new File(getPrefDir().getAbsolutePath() + File.separator + "ideabox.xml"));
	}

	public static void notAvailable() {
		JOptionPane.showMessageDialog(null,
		   I18N.getMsg("not.available"),
		   I18N.getMsg("error"),
		   JOptionPane.ERROR_MESSAGE);
	}

	public static String getDateString() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		return (formatter.format(new Date()));
	}

	public static File getLogFile() {
		return new File(getHomeDir() + File.separator + "oStorybook.log");
	}

}
