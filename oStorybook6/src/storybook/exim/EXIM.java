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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.exim;

import i18n.I18N;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import storybook.exim.exporter.Exporter;
import storybook.exim.importer.Importer;
import storybook.tools.StringUtil;
import storybook.tools.file.IOUtil;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.SbView;

/**
 * common static class for import or export
 *
 * @author favdb
 */
public class EXIM {

	private static String TT = "EXIM";

	private EXIM() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * call export for a SbView
	 *
	 * @param mainFrame
	 * @param view
	 * @return : false if panel is null, else return result of export
	 */
	public static boolean exporter(MainFrame mainFrame, SbView view) {
		if (view == null) {
			return false;
		}
		Exporter exp = new Exporter(mainFrame);
		return exp.exec(view);
	}

	/**
	 * call export for a JPanel
	 *
	 * @param mainFrame
	 * @param panel
	 * @return : false if panel is null, else return result of export
	 */
	public static boolean exporter(MainFrame mainFrame, JPanel panel) {
		if (panel == null) {
			return false;
		}
		Exporter exp = new Exporter(mainFrame);
		return exp.exportPanel(panel);
	}

	/**
	 * call import for a File
	 *
	 * @param mainFrame
	 * @param xml
	 * @return :false if File is null, else return result of import
	 */
	public static boolean importer(MainFrame mainFrame, File xml) {
		if (xml == null) {
			return false;
		}
		Importer exp = new Importer(mainFrame, xml);
		return exp.exec(xml.getAbsolutePath());
	}

	/**
	 * get directory for export
	 *
	 * @param mainFrame
	 * @return
	 */
	public static String getDir(MainFrame mainFrame) {
		String name = mainFrame.getBook().getParam().getParamExport().getDirectory();
		File file = new File(name);
		if (name.isEmpty()) {
			name = mainFrame.getProject().getPath();
		}
		if (!file.exists()) {
			if (!file.mkdir()) {
				return mainFrame.getProject().getPath();
			}
		}
		return name;
	}

	/**
	 * get standard normalized file name for an export
	 *
	 * @param mf: the MainFrame to set the directory
	 * @param dest: the file name
	 * @param ext: extension
	 * @return
	 */
	public static String getFileName(MainFrame mf, String dest, String ext) {
		if (dest == null || dest.isEmpty()) {
			return null;
		}
		String destname = StringUtil.escapeTxt(IOUtil.filenameCleanup(dest));
		String[] x = destname.split(" ");
		destname = "";
		for (String z : x) {
			destname += StringUtil.capitalize(z);
		}
		StringBuilder filename = new StringBuilder(getDir(mf));
		if (filename.toString().endsWith(File.separator)) {
			filename.append(File.separator);
		}
		filename.append(destname);
		if (ext != null && !ext.isEmpty()) {
			filename.append(".").append(ext);
		}
		return filename.toString();
	}

	/**
	 * check if a file name exists, if yes ask for replacing
	 *
	 * @param p
	 * @param name
	 * @return
	 */
	public static boolean askExists(JComponent p, String name) {
		File file = new File(name);
		return !(file.exists() && JOptionPane.showConfirmDialog(p,
		   I18N.getMsg("export.replace", name),
		   I18N.getMsg("export"),
		   JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION);
	}

	/**
	 * extension for export to a file
	 */
	public static String[] FILE_EXT = {
		"docx,MS Word",
		"odt,LibreOffice",
		"latex,LaTeX",
		"txt,Text",
		"other," + I18N.getMsg("other")
	};

	/**
	 * get type editor for an extension
	 *
	 * @param ext
	 * @return
	 */
	public static String getEditor(String ext) {
		int n = findExt(ext);
		if (n > -1) {
			String xname[] = FILE_EXT[n].split(",");
			return xname[1];
		}
		return "";
	}

	public static int findExt(String ext) {
		//LOG.printInfos(TT + ".findExt(ext=" + ext + ")");
		for (int i = 0; i < FILE_EXT.length; i++) {
			String xname[] = FILE_EXT[i].split(",");
			if (xname[0].equals(ext)) {
				return i;
			}
		}
		return FILE_EXT.length - 1;
	}

	public static String getTitle(String string) {
		if (!string.contains("@")) {
			return string;
		}
		String nn[] = string.split("@");
		if (nn.length < 2) {
			return "";
		}
		return nn[1];
	}

}
