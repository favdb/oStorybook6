/*
 * Copyright (C) 2021 favdb
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
package storybook.exim.exporter;

import assistant.Assistant;
import i18n.I18N;
import java.io.File;
import java.util.List;
import javax.swing.JOptionPane;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.book.BookUtil;
import storybook.dialog.ExceptionDlg;
import storybook.exim.EXIM;
import storybook.tools.LOG;
import storybook.tools.file.IOUtil;
import storybook.tools.html.Html;
import storybook.ui.MainFrame;
import storybook.ui.panel.info.InfoPanel;

/**
 * export the Info view data
 *
 * @author favdb
 */
public class ExportInfoView extends AbstractExport {

	private static final String TT = "ExportInfo.";

	private InfoPanel infoPanel;
	private AbstractEntity entity;

	public ExportInfoView(MainFrame mainFrame) {
		super(mainFrame, "html");
	}

	private void writeSummary() {
		File file = mainFrame.getProject().getFile();
		int textLength = BookUtil.getNbChars(mainFrame.project);
		int words = BookUtil.getNbWords(mainFrame.project);
		writeText(Html.TABLE_B);
		writeText(Html.keyToHtml("file.info.filename", file.toString()));
		writeText(Html.keyToHtml("file.info.creation", book.getCreation()));
		writeText(Html.keyToHtml("file.info.maj", book.getMaj()));
		String size = String.format("%,d %s (%,d %s)",
				words, I18N.getMsg("words"), textLength, I18N.getMsg("characters"));
		writeText(Html.keyToHtml("book.title", book.getTitle()));
		writeText(Html.keyToHtml("book.subtitle", book.getSubtitle()));
		writeText(Html.keyToHtml("book.author", book.getAuthor()));
		writeText(Html.keyToHtml("book.copyright", book.getCopyright()));
		writeText(Html.keyToHtml("book.blurb", book.getBlurb()));
		if (!book.getUUID().isEmpty()) {
			writeText(Html.keyToHtml("book.UUID", book.getUUID()));
		}
		if (!book.getISBN().isEmpty()) {
			writeText(Html.keyToHtml("book.ISBN", book.getISBN()));
		}
		writeText(Html.keyToHtml("file.info.text.length", size));
		writeText(Html.keyToHtml("strands", count(Book.TYPE.STRAND)));
		writeText(Html.keyToHtml("parts", count(Book.TYPE.PART)));
		writeText(Html.keyToHtml("chapters", count(Book.TYPE.CHAPTER)));
		writeText(Html.keyToHtml("scenes", count(Book.TYPE.SCENE)));
		writeText(Html.keyToHtml("persons", count(Book.TYPE.PERSON)));
		writeText(Html.keyToHtml("locations", count(Book.TYPE.LOCATION)));
		writeText(Html.keyToHtml("items", count(Book.TYPE.ITEM)));
		writeText(Html.keyToHtml("tags", count(Book.TYPE.TAG)));
		writeText(Html.keyToHtml("endnotes", count(Book.TYPE.ENDNOTE)));
		writeText(Html.keyToHtml("events", count(Book.TYPE.EVENT)));
		writeText(Html.keyToHtml("ideas", count(Book.TYPE.IDEA)));
		writeText(Html.keyToHtml("memos", count(Book.TYPE.MEMO)));
		writeText(Html.keyToHtml("notes", book.getNotes()));
		if (!book.info.assistantGet().isEmpty()) {
			String str = Assistant.toHtml(book.info.assistantGet());
			writeText(Html.keyToHtml("assistant", str));
		}
		writeText(Html.TABLE_E);
	}

	private int count(Book.TYPE type) {
		return mainFrame.project.getList(type).size();
	}

	public static boolean exec(MainFrame mainFrame, String type) {
		LOG.trace(TT + "exec(mainFrame, type='" + type + "')");
		if (type.equals("summary")) {
			ExportInfoView exp = new ExportInfoView(mainFrame);
			if (exp.openFile(I18N.getMsg("export.book.summary"), true)) {
				exp.writeSummary();
			}
			exp.closeFile(true);
			return true;
		}
		@SuppressWarnings("unchecked")
		List<AbstractEntity> entities = (List) mainFrame.project.getList(Book.getTYPE(type));
		if (entities.isEmpty()) {
			return true;
		}
		ExportInfoView exp = new ExportInfoView(mainFrame);
		if (exp.openFile("info_" + I18N.getMsg(type), true)) {
			for (AbstractEntity entity : entities) {
				exp.writeText(entity.toDetail(3));
			}
			exp.closeFile(true);
			return true;
		}
		return false;
	}

	public boolean exec(InfoPanel infoPanel) {
		//LOG.trace("ExportInfoView.exec(infoPanel)");
		this.infoPanel = infoPanel;
		this.entity = infoPanel.getEntity();
		// open the output file to write
		if (entity == null) {
			return false;
		}
		String fname = "info_" + I18N.getMsg(entity.getObjType().toString()) + "_" + entity.getName();
		fname = IOUtil.filenameCleanup(fname);
		if (openFile(fname, true)) {
			writeText(infoPanel.getInfoPane().getText());
			closeFile(true);
		}
		return true;
	}

	@Override
	public boolean openFile(String name, boolean askexists) {
		isOpened = false;
		param.setFileName(param.getDirectory() + File.separator + name + ".html");
		this.name = name;
		File file = new File(param.getDirectory());
		if (!(file.exists() && file.isDirectory()) && askexists) {
			JOptionPane.showMessageDialog(mainFrame,
					I18N.getMsg("export.dir.error"),
					I18N.getMsg("export"), 1);
			return false;
		}
		if (!EXIM.askExists(infoPanel, param.getFileName())) {
			return false;
		}
		try {
			outBuffer = new StringBuilder();
			outFile = new File(param.getFileName());
			//outStream = new BufferedWriter(new FileWriter(param.getFileName()));
		} catch (Exception ex) {
			ExceptionDlg.show(this.getClass().getSimpleName()
					+ ".openFile(...) outStream error", ex);
			return (false);
		}
		isOpened = true;
		writeHeaderHtml();
		return true;
	}

}
