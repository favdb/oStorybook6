/*
 * Copyright (C) 2017 favdb
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
package storybook.exim.exporter;

import i18n.I18N;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JOptionPane;
import storybook.App;
import storybook.db.book.Book;
import storybook.db.book.BookParamExport;
import storybook.dialog.ExceptionDlg;
import storybook.exim.EXIM;
import storybook.tools.LOG;
import storybook.tools.html.CSS;
import storybook.tools.html.Html;
import storybook.tools.xml.Xml;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public abstract class AbstractExport {

	private static final String TT = "AbstractExport";

	private static final String EXPORT_TITLE = "Export";
	public static final String F_CSV = "csv",
	   F_DOCX = "docx", F_ODT = "odt",
	   F_HTML = "html",
	   F_OSBK = "osbk",
	   F_TXT = "txt",
	   F_XML = "xml",
	   F_ZXML = "zxml";
	public static boolean TOC_LINK = true,
	   MULTI_CHAPTER = true,
	   MULTI_SCENE = true,
	   IS_NAV = true,
	   IS_NAV_IMAGE = true;

	public BookParamExport param;
	public boolean isOpened;
	public BufferedWriter outStream;
	public MainFrame mainFrame;
	public String name;
	public static final boolean VERBOSE = true;
	public static final boolean SILENT = false;
	public Book book;
	private String htmlTitle = "";
	public List<String> imageList = new ArrayList<>();

	public AbstractExport(MainFrame mainFrame, String format) {
		this.mainFrame = mainFrame;
		book = mainFrame.getBook();
		param = book.getParam().getParamExport();
		param.refresh();
		if (param.getDirectory() == null || param.getDirectory().isEmpty()) {
			param.setDirectory(mainFrame.getProject().getPath() + File.separator + book.getTitle());
		}
		param.setFormat(format);
		isOpened = false;
	}

	public String getTitle(String title) {
		if (title.contains("@")) {
			String tx[] = title.split("@");
			if (tx.length > 1) {
				return tx[1];
			}
			return "";
		}
		return title;
	}

	public void setHtmlTitle(String title) {
		this.htmlTitle = title;
	}

	public String getFileName(String n) {
		return param.getDirectory() + File.separator + n + "." + param.getFormat().toString();
	}

	public boolean askFileExists(String n) {
		//LOG.printInfos(TT + ".askExists(n=" + n + ")");
		this.name = n;
		param.setFileName(getFileName(n));
		File file = new File(param.getFileName());
		return file.exists();
	}

	/**
	 * open a filefor export
	 *
	 * @param name name of the file
	 * @param ask if file exists ask for replacing it
	 *
	 * @return
	 */
	public boolean openFile(String name, boolean ask) {
		//LOG.trace(TT + "openFile(name='" + name + "', ask=" + (ask ? "true" : "false") + ")");
		if (book.getUUID().isEmpty()) {
			book.setUUID(UUID.randomUUID().toString());
			mainFrame.setUpdated();
		}
		isOpened = false;
		param = mainFrame.getBook().getParam().getParamExport();
		this.name = name;
		// check/create if export directory exists
		String dir = param.getDirectory();
		File fdir = new File(dir);
		if (!fdir.exists() && fdir.isDirectory()) {
			fdir.mkdirs();
		}
		if (!dir.endsWith(File.separator)) {
			dir += File.separator;
		}
		// set the export file name
		//fdir = new File(dir);
		if (!(fdir.exists() && fdir.isDirectory())) {
			JOptionPane.showMessageDialog(mainFrame,
			   I18N.getMsg("export.dir.error") + "\n" + fdir.getAbsolutePath(),
			   I18N.getMsg("export"), 1);
			return (false);
		}
		fdir = new File(dir + name + "." + param.getFormat());
		if (ask && !EXIM.askExists(null, fdir.getAbsolutePath())) {
			return false;
		}
		try {
			outStream = new BufferedWriter(new OutputStreamWriter(
			   new FileOutputStream(fdir.getAbsolutePath()), "UTF-8"));
		} catch (IOException ex) {
			ExceptionDlg.show(this.getClass().getSimpleName()
			   + ".openFile(...) outStream error", ex);
			return (false);
		}
		switch (param.getFormat()) {
			case F_ZXML:
			case F_XML:
				writeHeaderXml();
				break;
			case F_HTML:
				writeHeaderHtml();
				break;
			case F_TXT:
				writeHeaderTxt();
				break;
			default:
				break;
		}
		isOpened = true;
		return true;
	}

	public boolean setOutStream(String filename) {
		try {
			outStream = new BufferedWriter(new OutputStreamWriter(
			   new FileOutputStream(param.getFileName()), "UTF-8"));
			switch (param.getFormat()) {
				case F_ZXML:
				case F_XML:
					writeHeaderXml();
					break;
				case F_HTML:
					writeHeaderHtml();
					break;
				case F_TXT:
					writeHeaderTxt();
					break;
				default:
					break;
			}
		} catch (IOException ex) {
			ExceptionDlg.show(this.getClass().getSimpleName()
			   + ".setOutStream(filename=" + filename + ") error", ex);
			return false;
		}
		return true;
	}

	/**
	 * write a tring to the current file
	 *
	 * @param str
	 * @return
	 */
	public boolean write(String str) {
		try {
			outStream.write(str, 0, str.length());
			outStream.flush();
		} catch (IOException ex) {
			ExceptionDlg.show(this.getClass().getSimpleName()
			   + ".write(str len=" + str.length() + ")", ex);
			ExceptionDlg.show(EXPORT_TITLE, ex);
			return false;
		}
		return true;
	}

	public boolean writeHeaderTxt() {
		return true;
	}

	public boolean writeHeaderXml() {
		//LOG.printInfos("AbstractExporter.writeHeaderXml()");
		StringBuilder b = new StringBuilder();
		b.append(Xml.HEADER);
		if (param.isXml()) {
			b.append("<book uuid=\"").append(book.getUUID()).append("\">\n");
		} else if (param.isOsbk()) {
			b.append("<storyboard title=\"").append(book.getTitle()).append("\">\n");
		} else {
			b.append("<book>\n");
		}
		return write(b.toString());
	}

	/**
	 * writeHeaderHtml
	 *
	 * @return false if not OK else true
	 */
	public boolean writeHeaderHtml() {
		//LOG.printInfos(TT+".writeHeaderHtml()");
		StringBuilder b = new StringBuilder();
		b.append(Html.DOCTYPE);
		b.append(Html.HTML_B_LANG);
		b.append(Html.HEAD_B);
		b.append(Html.META_UTF8);
		if (!htmlTitle.isEmpty()) {
			b.append(Html.intoTag("title", htmlTitle));
		} else {
			b.append(Html.intoTag("title", mainFrame.getBook().getTitle()));
		}
		BookParamExport paramBook = mainFrame.getBook().getParam().getParamExport();
		if (mainFrame.project.book.info.scenarioGet()) {
			if (paramBook.getHtmlCss().isEmpty()) {
				b.append(Html.STYLE_B);
				b.append(CSS.forScenario(false));
				b.append(Html.STYLE_E);
			} else {
				b.append(CSS.fromFile(paramBook.getHtmlCss(), App.fonts.monoGet()));
			}
		} else {
			b.append(CSS.fromFile(paramBook.getHtmlCss(), null));
		}
		b.append(Html.HEAD_E);
		b.append(Html.BODY_B);
		return write(b.toString());
	}

	public void writeHtml(String str) {
		writeText(Html.intoTag("tr", str));
	}

	public void writeXml(int lev, String label, String str) {
		String tab = "    ";
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < lev; i++) {
			b.append(tab);
		}
		b.append("<").append(label).append(">");
		if (!str.isEmpty()) {
			b.append(str);
			b.append("</").append(label).append(">");
		}
		b.append("\n");
		writeText(b.toString());
	}

	public void writeXmlAttribute(int lev, String label, String str) {
		if (str.trim().isEmpty()) {
			return;
		}
		String tab = "    ";
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < lev; i++) {
			b.append(tab);
		}
		b.append("<").append(label).append(" ").append(str).append(" />\n");
		writeText(b.toString());
	}

	private static String xmlTab(int lev) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < lev; i++) {
			b.append("    ");
		}
		return (b.toString());
	}

	public static String stringAttribute(int lev, String key, String val) {
		if (val == null || val.isEmpty() || val.trim().equals(",")) {
			return "";
		}
		if (lev > 9) {
			return "\n" + xmlTab(lev - 10) + key + "=\"" + val + "\"";
		}
		return xmlTab(lev) + " " + key + "=\"" + val + "\"";
	}

	public static String stringAttribute(int lev, String key, boolean... val) {
		StringBuilder vs = new StringBuilder();
		for (boolean v : val) {
			vs.append((v ? "1" : "0"));
		}
		return stringAttribute(lev, key, vs.toString());
	}

	public static String stringAttribute(int lev, String key, Integer val) {
		return stringAttribute(lev, key, val.toString());
	}

	public static String stringAttribute(int lev, String key, Long val) {
		return stringAttribute(lev, key, val.toString());
	}

	/**
	 * write a String to th current output Stream
	 *
	 * @param str
	 */
	public void writeText(String str) {
		if (!isOpened) {
			return;
		}
		if ("".equals(str)) {
			return;
		}
		try {
			//force conversion to UTF-8
			byte[] bytes = str.getBytes();
			String forced = new String(bytes, StandardCharsets.UTF_8);
			//write the forced String to file
			outStream.write(str, 0, forced.length());
			outStream.flush();
		} catch (IOException ex) {
			LOG.err(TT + ".writeText(" + str + ")", ex);
		}
	}

	public void closeFile(boolean verbose) {
		//LOG.printInfos(TT + ".closeFile(verbose=" + (verbose ? "true" : "false")+")");
		if (isOpened == false) {
			return;
		}
		switch (param.getFormat()) {
			case "osbk":
				writeText("</storyboard>\n");
				break;
			case F_ZXML:
			case F_XML:
				writeText("</book>\n");
				break;
			case F_HTML:
				writeText(Html.BODY_E + Html.HTML_E);
				break;
			case F_TXT:
			case F_CSV:
			default:
				break;
		}
		try {
			outStream.close();
			isOpened = false;
			if (verbose) {
				JOptionPane.showMessageDialog(mainFrame,
				   I18N.getMsg("export.success", param.getFileName()),
				   I18N.getMsg("export"), JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (IOException ex) {
			LOG.err(TT + ".closeFile(...)", ex);
		}
	}

}
