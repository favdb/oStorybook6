/*
 * Copyright (C) 2020 favdb
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
 * You should have received a fileCopy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.tools.file;

import i18n.I18N;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import storybook.db.chapter.Chapter;
import storybook.db.scene.Scene;
import storybook.exim.doc.DOCX;
import storybook.exim.doc.ODT;
import storybook.tools.LOG;
import storybook.tools.StringUtil;
import storybook.tools.html.Html;
import storybook.ui.frames.main.MainFrame;

/**
 * class to hold external Editor File
 *
 * @author favdb
 */
public class XEditorFile {

	private static final String TT = "XEditorFile";

	private final MainFrame mainFrame;
	private String path;

	public XEditorFile(MainFrame mainFrame, Scene scene) {
		this.mainFrame = mainFrame;
		String name = scene.getOdf();
		if (name == null || name.isEmpty()) {
			name = getDefaultName(mainFrame, scene);
		}
		this.path = name;
	}

	/**
	 * get the path of the external editor
	 *
	 * @return
	 */
	public String getPath() {
		return (path);
	}

	/**
	 * set the path of the external file
	 *
	 * @param path
	 */
	public void setPath(String path) {
		if (path == null || path.isEmpty()) {
			this.path = "";
		} else {
			this.path = path;
		}
	}

	/**
	 * set the path name of the external file for Scene
	 *
	 * @param scene
	 */
	public void setPath(Scene scene) {
		this.path = getDefaultName(mainFrame, scene);
	}

	/**
	 * create an external file from Scene
	 *
	 * @param scene
	 * @return
	 */
	public boolean create(Scene scene) {
		File file = createFile(mainFrame, scene);
		if (file == null) {
			return (false);
		}
		this.path = file.getAbsolutePath();
		return (true);
	}

	/**
	 * get the default external file name for a scene
	 *
	 * @param mainFrame
	 * @param scene
	 * @return the name
	 */
	public static String getDefaultName(MainFrame mainFrame, Scene scene) {
		String str = "";
		if (!mainFrame.getBook().isXeditorUse()) {
			return (str);
		}
		String str1 = "";
		Chapter chapter = scene.getChapter();
		if (chapter != null) {
			str1 += chapter.getChapterno();
		}
		if (str1.length() < 2) {
			str1 = "0" + str1;
		}
		String str2 = "" + scene.getSceneno();
		if (str2.length() < 2) {
			str2 = "0" + str2;
		}
		str = mainFrame.getProject().getPath() + File.separator
		   + I18N.getMsg("chapter") + str1
		   + "-"
		   + I18N.getMsg("scene") + str2
		   + "." + mainFrame.getBook().getParam().getParamEditor().getExtension();
		return (str);
	}

	/**
	 * create a new external File for a scene
	 *
	 * @param mainFrame
	 * @param scene
	 * @return the path name of the created file
	 */
	public static File createFile(MainFrame mainFrame, Scene scene) {
		//LOG.printInfos(TT + ".createFile(mainFrame, scene=" + scene.getName() + ")");
		String dest = getDefaultName(mainFrame, scene);
		if (scene.getOdf() != null && !scene.getOdf().isEmpty()) {
			dest = scene.getOdf();
		}
		return createFile(mainFrame, dest);
	}

	/**
	 * create a new external file depending on default extension
	 *
	 * @param mainFrame
	 * @param dest
	 * @return
	 */
	public static File createFile(MainFrame mainFrame, String dest) {
		//LOG.printInfos(TT + ".createFile(mainFrame, dest=" + dest + ")");
		File file = new File(dest);
		if (!file.exists()) {
			switch (mainFrame.getBook().getParam().getParamEditor().getExtension()) {
				case "odt":
					ODT odt = new ODT(file);
					odt.createDoc(mainFrame, file, Html.P_EMPTY);
					break;
				case "docx":
					DOCX docx = new DOCX(file);
					docx.createDoc(mainFrame, file, Html.P_EMPTY);
					break;
				default:
					return null;
			}
		}
		return (file);
	}

	/**
	 * get the file name for a Scene
	 *
	 * @param mainFrame
	 * @param scene
	 * @return
	 */
	public static String getFilePath(MainFrame mainFrame, Scene scene) {
		String stored = scene.getOdf();
		if ((stored != null) && (!stored.isEmpty())) {
			return stored;
		} else {
			return getDefaultFilePath(mainFrame, scene.getName());
		}
	}

	/**
	 * get the external file name for the scene name
	 *
	 * @param mainFrame
	 * @param sceneName
	 * @return
	 */
	public static String getDefaultFilePath(MainFrame mainFrame, String sceneName) {
		String path = mainFrame.getProject().getPath();
		String ext = "." + mainFrame.getBook().getParam().getParamEditor().getExtension();
		String str = path + File.separator + StringUtil.capitalize(sceneName).replace(" ", "") + ext;
		return (str);
	}

	/**
	 * System launchExternal for an external file with default application
	 *
	 * @param filename
	 * @return the external file name
	 */
	public static String launchExternal(String filename) {
		//LOG.printInfos(TT + ".launch(name=" + filename + ")");
		File file = new File(filename);
		if (!file.exists()) {
			return "";
		}
		try {
			Desktop.getDesktop().edit(file);
		} catch (IOException e) {//don't modify to IOException
			try {
				Desktop.getDesktop().open(file);
			} catch (IOException ex) {//don't modify to IOException
				LOG.err(I18N.getMsg("xeditor.launching_error"), ex);
				return "";
			}
		}
		return filename;
	}

	/**
	 * System launchExternal for an external file for a Scene
	 *
	 * @param mainFrame
	 * @param scene
	 * @return the external file name
	 */
	public static String launchExternal(MainFrame mainFrame, Scene scene) {
		String name;
		if ((scene.getOdf() == null) || (scene.getOdf().isEmpty())) {
			name = getDefaultFilePath(mainFrame, scene.getName());
		} else {
			name = scene.getOdf();
		}
		return launchExternal(name);
	}

	/**
	 * get a File object for the current path
	 *
	 * @return
	 */
	public File getFile() {
		return (new File(path));
	}

}
