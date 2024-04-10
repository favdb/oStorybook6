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

import assistant.Assistant;
import assistant.Assistant.SECTION;
import assistant.AssistantDlg;
import i18n.I18N;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import storybook.tools.file.IOUtil;

/**
 *
 * @author favdb
 */
public class AssAction {

	private static final String TT = "AppAction";

	private final AppAssistant app;

	public AssAction(AppAssistant app) {
		this.app = app;
	}

	public void fileOpen() {
		//LOG.trace(TT + ".fileOpen()");
		File file = IOUtil.fileSelect(app, "", "xml", "", "");
		if (file == null || !file.exists()) {
			return;
		}
		Assistant.init(file);
		if (Assistant.getXml().isOpened()) {
			Assistant.getXml().setSave(file);
			app.initUi();
			app.getAppMenu().setSaveEnable(true);
		}
	}

	public boolean fileSave() {
		//LOG.trace(TT + ".fileSave()");
		if (Assistant.getXml().getSave() == null) {
			File file = askFileSave(false);
			if (file == null) {
				return false;
			}
		}
		Assistant.getXml().save();
		app.setModified(false);
		return true;
	}

	public void fileSaveAs() {
		//LOG.trace(TT + ".fileSaveAs()");
		File file = askFileSave(true);
		if (file != null) {
			Assistant.getXml().setSave(file);
			Assistant.getXml().save();
			app.setModified(false);
			app.getAppMenu().setSaveEnable(true);
		}
	}

	public File askFileSave(boolean create) {
		//LOG.trace(TT + ".askFileSave(create="+(create?"true":"false")+")");
		File file = null;
		JFileChooser jfc = new JFileChooser();
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("Assistant (*.xml)", "xml"));
		int i = jfc.showSaveDialog(app);
		if (i == JFileChooser.APPROVE_OPTION) {
			file = jfc.getSelectedFile();
			if (file == null) {
				return null;
			}
			String filename = file.getAbsolutePath();
			if (!filename.endsWith(".xml") && !filename.endsWith(".XML")) {
				filename += ".xml";
				file = new File(filename);
			}
			if (create && file.exists()) {
				int n = JOptionPane.showConfirmDialog(jfc,
				   I18N.getMsg("file.exists"),
				   I18N.getMsg("save"),
				   JOptionPane.YES_NO_OPTION);
				if (n != JOptionPane.YES_OPTION) {
					return null;
				}
			}
		}
		return file;
	}

	public void fileExit() {
		app.close();
	}

	public void helpAbout() {
		//LOG.trace(TT + ".helpAbout()");
		StringBuilder msg = new StringBuilder();
		msg.append("Version ").append(AppAssistant.VERSION)
		   .append(" (").append(AppAssistant.VERSION_YEAR).append(")")
		   .append("\n").append("(c) GPL V2");
		JTextArea textArea = new JTextArea(6, 20);
		textArea.setText(msg.toString());
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		JOptionPane.showMessageDialog(app,
		   scrollPane,
		   I18N.getMsg("assistant"),
		   JOptionPane.INFORMATION_MESSAGE);
	}

	public void testDialog(SECTION type, String values) {
		AssistantDlg dlg = new AssistantDlg(app, Assistant.getXml(), type, values);
		dlg.setVisible(true);
		if (!dlg.isCanceled()) {
			dlg.showResult();
		}
	}

}
