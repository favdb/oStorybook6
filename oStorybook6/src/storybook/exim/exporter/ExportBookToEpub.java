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
package storybook.exim.exporter;

import api.jsoup.Jsoup;
import api.jsoup.nodes.Document;
import api.jsoup.nodes.Element;
import api.jsoup.select.Elements;
import i18n.I18N;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.swing.JOptionPane;
import storybook.Const;
import storybook.db.book.Book;
import storybook.db.book.BookParamLayout;
import storybook.db.chapter.Chapter;
import storybook.db.endnote.Endnote;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.review.Review;
import storybook.tools.LOG;
import storybook.tools.Markdown;
import storybook.tools.StringUtil;
import storybook.tools.file.IOUtil;
import storybook.tools.html.Html;
import storybook.tools.zip.ZipUtil;
import storybook.ui.MainFrame;

/**
 * export the Book to an EPUB file
 *
 * @author favdb
 */
public class ExportBookToEpub extends AbstractExport {

	private static final String TT = "ExportBookToEpub";

	private static final String VERSION = "version",
	   XMLNS = "xmlns";
	private static final String ALIGN_CENTER = "text-align: center;",
	   DOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"",
	   ENDNOTE_FILE = "endnotes.xhtml",
	   EPUB = "epub",
	   MARGIN_BOTTOM = "margin-bottom: 6px;",
	   MARGIN_LEFT = "margin-left: 0.5cm;margin-right: 0.5cm;",
	   MARGIN_TOP = "margin-top: 6px;",
	   META_INF = "META-INF",
	   NAVPOINT_END = "</navPoint>\n",
	   OEBPS = "OEBPS",
	   OEBPS_IMAGES = OEBPS + File.separator + "Images",
	   OEBPS_CSS = OEBPS + File.separator + "css",
	   OEBPS_TEXT = OEBPS + File.separator + "Text",
	   TAB = "   ",
	   TYPE_XHTML = Html.getAttribute("media-type", "application/xhtml+xml"),
	   XML_HEAD = "<?xml" + Html.getAttribute(VERSION, "1.0")
	   + Html.getAttribute("encoding", "utf-8")
	   + Html.getAttribute("standalone", "no") + "?>\n",
	   XHTML = "http://www.w3.org/1999/xhtml",
	   XHTML_TAG = "<html" + Html.getAttribute(XMLNS, XHTML) + ">\n",
	   XHTML_DTD = "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd";

	private static final String CONTAINER = "<?xml" + Html.getAttribute(VERSION, "1.0") + "?>\n"
	   + "<container"
	   + Html.getAttribute(XMLNS, "urn:oasis:names:tc:opendocument:xmlns:container")
	   + Html.getAttribute(VERSION, "1.0")
	   + ">\n"
	   + "  <rootfiles>\n"
	   + "    <rootfile"
	   + Html.getAttribute("full-path", "OEBPS/content.opf")
	   + Html.getAttribute("media-type", "application/oebps-package+xml")
	   + "/>\n"
	   + "  </rootfiles>\n"
	   + "</container>";

	private final String CONTENT_HEAD
	   = "<?xml"
	   + Html.getAttribute(VERSION, "1.0")
	   + Html.getAttribute("encoding", "utf-8")
	   + Html.getAttribute("standalone", "yes")
	   + "?>\n";

	private final String CONTENT_PACKAGE
	   = "<package"
	   + Html.getAttribute(XMLNS, "http://www.idpf.org/2007/opf") + "\n"
	   + tab(2)
	   + Html.getAttribute("unique-identifier", "BookId")
	   + Html.getAttribute(VERSION, "2.0") + "\n"
	   + tab(2)
	   + Html.getAttribute("xmlns:opf", "http://www.idpf.org/2007/opf")
	   + ">\n";

	private final String NCX_HEAD = XML_HEAD
	   + "<ncx"
	   + Html.getAttribute(XMLNS, "http://www.daisy.org/z3986/2005/ncx/")
	   + Html.getAttribute(VERSION, "2005-1")
	   + Html.getAttribute("xml:lang", "fra")
	   + ">\n"
	   + tab(1) + Html.HEAD_B
	   + getMeta("dtb:depth", "1")
	   + getMeta("dtb:totalPageCount", "0")
	   + getMeta("dtb:maxPageNumber", "0")
	   + getMeta("dtb:uid", "%s")
	   + tab(1) + Html.HEAD_E;

	private List<Chapter> chapters;
	private List<Endnote> endnotes;
	private boolean SUMMARY = false;

	public static void execute(MainFrame mainFrame, String folder, boolean byScenes) {
		//LOG.printInfos(TT + ".execute(mainFrame, folder=" + folder + ")");
		ExportBookToEpub exp = new ExportBookToEpub(mainFrame, byScenes);
		exp.create(folder);
	}
	private File epubDir;
	private String epubDirText;
	private final BookParamLayout layout;
	private String uuid = UUID.randomUUID().toString();
	private final boolean byScenes;

	@SuppressWarnings("unchecked")
	public ExportBookToEpub(MainFrame mainFrame, boolean byScenes) {
		super(mainFrame, EPUB);
		layout = mainFrame.getBook().getParam().getParamLayout();
		if ((layout.getPartTitle() && book.project.parts.getCount() > 1)
		   || (layout.getChapterTitle() && book.project.chapters.getCount() > 1)) {
			SUMMARY = true;
		}
		this.byScenes = byScenes;
		if (!book.getUUID().isEmpty()) {
			uuid = book.getUUID();
		}
	}

	private String tab(int n) {
		String r = "";
		for (int i = 0; i < n; i++) {
			r += TAB;
		}
		return (r);
	}

	/**
	 * create folder structure and minimal files in the temporary folder "tempEpub"
	 *
	 * @param folder
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean create(String folder) {
		//LOG.printInfos(TT + ".create()");
		try {
			Path tmpDir = Files.createTempDirectory("tmpEpub");
			epubDir = tmpDir.toFile();
			if (epubDir.exists()) {
				epubDir.delete();
			}
			File epubFile = new File(param.getDirectory() + File.separator
			   + mainFrame.getProject().getName() + ".epub");
			if (epubFile.exists()) {
				// existing file, ask to replace it
				if (!askInfo("export.replace", epubFile.getAbsolutePath())) {
					LOG.trace("no replacing existing EPUB file");
					return false;
				}
				// yes content must be erased
				epubFile.delete();
			}
			// create master folder and sub-folders
			String dirs[] = {META_INF, OEBPS, OEBPS_CSS, OEBPS_IMAGES, OEBPS_TEXT};
			for (String d : dirs) {
				File dd = new File(epubDir + File.separator + d);
				if (!dd.exists() && !dd.mkdirs()) {
					infoMsg(true, "epub.error.create_dir", d);
					return false;
				}
			}
			epubDirText = epubDir.getPath() + File.separator + OEBPS_TEXT + File.separator;
			File container = new File(epubDir + File.separator + META_INF + File.separator
			   + "container.xml");
			container.delete();
			IOUtil.fileWriteString(container, CONTAINER);
			copyImages();
			endnotes = Endnote.find(mainFrame, Endnote.TYPE.ENDNOTE);
			writeCover();
			writeContentOpf();
			writeTocNcx();
			writeCoverBack();
			writeTitlepage();
			writeSummary();
			writeBook();
			writeEndnotes();
			String srcDir = epubDir.getPath();
			String dfn = param.getDirectory() + File.separator + mainFrame.project.getFile().getName();
			if (!folder.isEmpty()) {
				dfn = folder + File.separator + mainFrame.project.getFile().getName();
			}
			dfn = IOUtil.changeExt(dfn, "." + EPUB);
			File destFile = new File(dfn);
			ZipUtil.fromDir(srcDir, destFile, "application/epub+zip");
			epubDir.delete();
			JOptionPane.showMessageDialog(mainFrame,
			   I18N.getMsg("export.success", destFile.getAbsoluteFile()),
			   I18N.getMsg("epub.title"), JOptionPane.INFORMATION_MESSAGE);
			return true;
		} catch (IOException ex) {
			LOG.err("unable to create EPUB file", ex);
			return false;
		}
	}

	/**
	 * display a question and get the answer
	 *
	 * @param msg
	 * @param info
	 * @return
	 */
	private boolean askInfo(String msg, String info) {
		int n = JOptionPane.showConfirmDialog(mainFrame,
		   I18N.getMsg(msg, info),
		   I18N.getMsg(EPUB),
		   JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
			return (true);
		}
		return (false);
	}

	/**
	 * display a message
	 *
	 * @param error
	 * @param msg
	 * @param info
	 */
	private void infoMsg(boolean error, String msg, String info) {
		JOptionPane.showMessageDialog(mainFrame,
		   I18N.getMsg(msg, info),
		   I18N.getMsg(EPUB),
		   (error ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE));
	}

	/**
	 * get the file name for a given Chapter as a String
	 *
	 * @param chapter
	 * @return
	 */
	private String getFilename(Chapter chapter) {
		//LOG.printInfos(TT + ".chapterGetFilename(chapter=" + chapter.getName() + ")");
		return (String.format("P%02dC%02d.xhtml",
		   chapter.getPart().getNumber(), chapter.getChapterno()));
	}

	/**
	 * get the Chapter title as a String
	 *
	 * @param chapter
	 * @return
	 */
	private String chapterGetTitle(Chapter chapter) {
		String title = "";
		if (param.getLayout().getChapterNumber()) {
			if (param.getLayout().getChapterRoman()) {
				title = (String) StringUtil.intToRoman(chapter.getChapterno()) + " ";
			} else {
				title = chapter.getChapterno().toString() + " ";
			}
		}
		if (param.getLayout().getChapterTitle()) {
			title += chapter.getName();
		}
		return title;
	}

	/**
	 * write the cover page
	 *
	 */
	private void writeCover() {
		//LOG.printInfos(TT+".writeCover()");
		String cover = epubDir + File.separator + "OEBPS/Images/" + "cover.jpeg";
		File destFile = new File(cover);
		// create the cover file image if not exists
		if (!destFile.exists() && !IOUtil.resourceCopyTo("cover/" + "cover.jpeg", destFile)) {
			return;
		}
		StringBuilder b = new StringBuilder();
		b.append(XML_HEAD);
		b.append(DOCTYPE)
		   .append(" \"" + XHTML_DTD + "\">\n");
		b.append("<html")
		   .append(Html.getAttribute(XMLNS, XHTML))
		   .append(Html.getAttribute("xml:lang", "en"))
		   .append(">\n");
		b.append(Html.HEAD_B).append("  <meta")
		   .append(Html.getAttribute("content", "true"))
		   .append(Html.getAttribute("name", "cover"))
		   .append(" />\n");
		b.append(Html.getHeadTitle("Cover"))
		   .append("<style type=\"text/css\">\n")
		   .append("@page {padding: 0pt; margin:0pt}\n")
		   .append("body { text-align: center; padding:0pt; margin: 0pt; }\n")
		   .append(Html.STYLE_E);
		b.append(Html.HEAD_E);
		b.append(Html.BODY_B);
		if (destFile.exists()) {
			b.append("  <div>\n");
			b.append("    <svg")
			   .append(Html.getAttribute(XMLNS, "http://www.w3.org/2000/svg"))
			   .append(Html.getAttribute("height", "100%"))
			   .append(Html.getAttribute("width", "100%"))
			   .append(Html.getAttribute("preserveAspectRatio", "none"))
			   .append(Html.getAttribute(VERSION, "1.1"))
			   .append(Html.getAttribute("viewBox", "0 0 380 550"))
			   .append(Html.getAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink"))
			   .append(">\n");
			b.append("      <image")
			   .append(Html.getAttribute("width", "380"))
			   .append(Html.getAttribute("height", "550"))
			   .append(Html.getAttribute("xlink:href", "../Images/cover.jpeg"))
			   .append("></image>\n");
			b.append("    </svg>\n");
			b.append("  </div>\n");
		} else {
			b.append(Html.intoPcenter(book.getAuthor()));
			b.append(Html.emptyLines(4));
			b.append(Html.intoTag("h1", book.getTitle()));
			b.append(Html.emptyLines(2));
		}
		b.append(Html.BODY_E);
		b.append(Html.HTML_E);
		// write the coverpage.xhtml
		destFile = new File(epubDirText + "coverpage.xhtml");
		writeToFile(destFile, b);
	}

	/**
	 * write the cover back page contains the blurb
	 *
	 */
	private void writeCoverBack() {
		//LOG.printInfos(TT+".writeCoverBack()");
		if (book.getBlurb().isEmpty()) {
			return;
		}
		StringBuilder b = new StringBuilder();
		b.append(XML_HEAD);
		b.append(DOCTYPE)
		   .append(" \"" + XHTML_DTD + "\">\n");
		b.append("<html xmlns=\"" + XHTML + "\" xml:lang=\"en\">\n");
		b.append(Html.HEAD_B)
		   .append("  <meta")
		   .append(Html.getAttribute("content", "true"))
		   .append(Html.getAttribute("name", "cover"))
		   .append(" />\n")
		   .append(Html.getHeadTitle("Coverback"))
		   .append("<style type=\"text/css\">\n")
		   .append("@page {padding: 0pt; margin:0pt}\n")
		   .append("body {")
		   .append("text-align: justify;")
		   .append("font-style: italic;")
		   .append("padding:0pt;")
		   .append("margin: 0pt;")
		   .append("}\n")
		   .append(Html.STYLE_E);
		b.append(Html.HEAD_E);
		b.append(Html.BODY_B);
		b.append(Html.intoP(Html.textToHTML(book.getBlurb())));
		b.append(Html.BODY_E);
		b.append(Html.HTML_E);
		File f = new File(epubDirText + "coverback.xhtml");
		writeToFile(f, b);
	}

	/**
	 * write the summary page, contains the table of content by chapter
	 *
	 */
	@SuppressWarnings("unchecked")
	private void writeSummary() {
		//LOG.printInfos(TT+".writeSummary()");
		if (!SUMMARY) {
			return;
		}
		StringBuilder b = new StringBuilder();
		b.append(XML_HEAD);
		b.append(DOCTYPE)
		   .append(" \"" + XHTML_DTD + "\">\n");
		b.append("<html")
		   .append(Html.getAttribute(XMLNS, XHTML))
		   .append(Html.getAttribute("xml:lang", "en")).append(">\n");
		b.append(Html.HEAD_B).append("  <meta")
		   .append(Html.getAttribute("content", "true"))
		   .append(Html.getAttribute("name", "summary"))
		   .append(" />\n");
		b.append(Html.getHeadTitle("Summary"))
		   .append("<style type=\"text/css\">\n")
		   .append("@page {padding: 0pt; margin:0pt}\n")
		   .append("body {")
		   .append("text-align: justify;")
		   .append("padding:0pt;")
		   .append("margin: 0pt;")
		   .append("}\n")
		   .append(Html.STYLE_E);
		b.append(Html.HEAD_E);
		b.append(Html.BODY_B);
		b.append(Html.P_B)
		   .append(Html.intoB(I18N.getMsg("export.book.toc")))
		   .append(Html.P_E);
		b.append(Html.P_B);
		String tb = "   ";
		for (Part part : (List<Part>) mainFrame.project.parts.getList()) {
			chapters = mainFrame.project.chapters.find(part);
			if (layout.getPartTitle() && book.nbParts() > 1) {
				String link = ((Part) part).getNumberName();
				if (!chapters.isEmpty()) {
					link = Html.intoA("",
					   getFilename((Chapter) mainFrame.project.chapters.getList().get(0)), part.getName());
				}
				b.append(tb).append(Html.intoP(link));
			}
			for (Chapter chapter : mainFrame.project.chapters.find(part)) {
				if (layout.getChapterTitle() && book.nbChapters() > 1) {
					String link = Html.intoA("", getFilename(chapter), chapterGetTitle(chapter));
					b.append(tb).append(tb).append(link).append(Html.BR);
				}
			}
		}
		b.append(Html.P_E);
		b.append(Html.BODY_E);
		b.append(Html.HTML_E);
		File f = new File(epubDirText + "summary.xhtml");
		writeToFile(f, b);
	}

	/**
	 * write the toc.ncx file
	 *
	 */
	private void writeTocNcx() {
		//LOG.printInfos(TT + ".writeTocNcx()");
		StringBuilder b = new StringBuilder();
		int n = 1;
		b.append(String.format(NCX_HEAD, uuid));
		b.append(tab(1))
		   .append(Html.intoTag("docTitle", Html.intoTag("text", book.getTitle())))
		   .append("\n");
		b.append(tab(1)).append("<navMap>\n");
		b.append(tab(2)).append(navPoint("chapter", n++));
		b.append(tab(3))
		   .append(Html.intoTag("navLabel",
			  Html.intoTag("text", I18N.getMsg("epub.cover"))))
		   .append("\n");
		b.append(tab(3)).append("<content")
		   .append(Html.getAttribute("src", "Text/coverpage.xhtml"))
		   .append("/>\n");
		b.append(tab(3)).append(NAVPOINT_END);
		b.append(tab(2)).append(navPoint("chapter", n++));
		b.append(tab(3)).append(Html.intoTag("navLabel",
		   Html.intoTag("text", I18N.getMsg("blurb"))));
		b.append(tab(3)).append("<content")
		   .append(Html.getAttribute("src", "Text/coverback.xhtml"))
		   .append("/>\n");
		b.append(tab(3)).append(NAVPOINT_END);
		if (SUMMARY) {
			b.append(tab(2)).append(navPoint("chapter", n++));
			b.append(tab(3)).append(Html.intoTag("navLabel",
			   Html.intoTag("text", I18N.getMsg("export.book.toc"))));
			b.append(tab(3)).append("<content")
			   .append(Html.getAttribute("src", "Text/summary.xhtml"))
			   .append("/>\n");
			b.append(tab(3)).append(NAVPOINT_END);
		}
		b.append(tab(2)).append(navPoint("chapter", n++));
		b.append(tab(3)).append(Html.intoTag("navLabel", Html.intoTag("text",
		   I18N.getMsg("epub.titlepage"))));
		b.append(tab(3)).append("<content")
		   .append(Html.getAttribute("src", "Text/titlepage.xhtml"))
		   .append("/>\n");
		b.append(tab(3)).append(NAVPOINT_END);
		for (Object part : mainFrame.project.getList(Book.TYPE.PART)) {
			for (Chapter c : mainFrame.project.chapters.find((Part) part)) {
				b.append(tab(2)).append(navPoint("chapter", n));
				b.append(tab(3)).append(Html.intoTag("navLabel",
				   Html.intoTag("text", getChapterTitle(c))));
				b.append(tab(3)).append("<content src=\"Text/")
				   .append(getFilename(c)).append("\"/>\n");
				b.append(tab(2)).append(NAVPOINT_END);
				n++;
			}
		}
		if (!endnotes.isEmpty()) {
			b.append(tab(2))
			   .append(navPoint("chapter", n));
			b.append(tab(3)).append(Html.intoTag("navLabel",
			   Html.intoTag("text", I18N.getMsg("endnotes"))));
			b.append(tab(3)).append("<content")
			   .append(Html.getAttribute("src", "Text/endnotes.xhtml"))
			   .append("/>\n");
			b.append(tab(2)).append(NAVPOINT_END);
		}
		b.append(tab(1)).append("</navMap>\n");
		b.append("</ncx>");
		File file = new File(epubDir + File.separator + "OEBPS/toc.ncx");
		IOUtil.fileWriteString(file, b.toString());
	}

	private String getHead(String title) {
		StringBuilder b = new StringBuilder();
		b.append(XML_HEAD)
		   .append(DOCTYPE)
		   .append("  \"" + XHTML_DTD + "\">\n");
		b.append(XHTML_TAG);
		b.append(Html.HEAD_B);
		b.append(Html.intoTag("title", title)).append("\n");
		b.append("<style type=\"text/css\">\n");
		if (mainFrame.project.book.info.scenarioGet()) {
			b.append("body {font-family: monospace;}\n"
			   + "p {"
			   + MARGIN_LEFT
			   + MARGIN_TOP
			   + MARGIN_BOTTOM
			   + "}\n");
		} else {
			b.append("body {text-align: justify;}\n"
			   + "p {"
			   + MARGIN_LEFT
			   + MARGIN_TOP
			   + MARGIN_BOTTOM
			   + "}\n");
		}
		b.append(Html.STYLE_E);
		b.append(Html.HEAD_E);
		return (b.toString());
	}

	private void writeBook() {
		//LOG.printInfos(TT + ".writeBook()");
		@SuppressWarnings("unchecked")
		List<Part> parts = (List) mainFrame.project.getList(Book.TYPE.PART);
		for (Part part : parts) {
			String partTitle = (parts.size() > 1 && layout.getPartTitle() ? part.getName() : "");
			chapters = mainFrame.project.chapters.find(part);
			for (Chapter chapter : chapters) {
				StringBuilder b = new StringBuilder();
				writeChapter(b, chapter, partTitle);
				partTitle = "";
			}
		}
	}

	private void writeChapter(StringBuilder b, Chapter chapter, String partTitle) {
		writeChapterStart(b, chapter, partTitle);
		List<Scene> scenes = mainFrame.project.scenes.findBy(chapter);
		for (Scene scene : scenes) {
			if (!scene.getInformative()) {
				writeScene(b, scene);
			}
		}
		writeChapterEnd(b, chapter);
	}

	private void writeChapterStart(StringBuilder b, Chapter chapter, String partTitle) {
		//LOG.printInfos(TT + ".writeChapterStart(b, chapter, partTitle)");
		b.append(getHead(chapter.getName()));
		b.append("<body id=\"x").append(getFilename(chapter)).append("\">\n");
		if (!partTitle.isEmpty()) {
			b.append(Html.intoH(1, partTitle, ALIGN_CENTER + "break-after:page;"));
		}
		if (chapters.size() > 1 && layout.getChapterTitle()) {
			b.append(Html.intoH(2, getChapterTitle(chapter), ALIGN_CENTER));
		}
		if (layout.getChapterDescription()) {
			b.append("<p style=\""
			   + "text-align:justify;"
			   + "margin-left: 1cm;"
			   + "margin-right: 1cm;\">")
			   .append(Html.intoI(chapter.getDescription()))
			   .append(Html.P_E);
		}
	}

	private String getChapterTitle(Chapter chapter) {
		StringBuilder b = new StringBuilder();
		if (layout.getChapterNumber()) {
			String t;
			if (layout.getChapterRoman()) {
				t = StringUtil.intToRoman(chapter.getChapterno()) + " ";
			} else {
				t = chapter.getChapterno() + " ";
			}
			b.append(t).append(getTitle(chapter.getName()));
		} else {
			b.append(chapter.getName());
		}
		return b.toString();
	}

	private void writeChapterEnd(StringBuilder b, Chapter chapter) {
		b.append(Html.BODY_E);
		b.append(Html.HTML_E);
		File f = new File(epubDirText + getFilename(chapter));
		writeToFile(f, b);
	}

	private void writeScene(StringBuilder b, Scene scene) {
		writeSceneStart(b, scene);
		String x = scene.getSummary();
		if (mainFrame.project.book.info.scenarioGet()
		   || mainFrame.project.book.info.markdownGet()) {
			Markdown md = new Markdown(TT, "text/plain", "");
			md.setHeader(scene, book.info.scenarioGet());
			md.setText(scene.getSummary());
			x = md.getHtmlBody();
		} else {
		}
		x = replaceEndnotes(scene, x);
		x = replaceImages(x);
		x = x.replace("<p align=\"center\">", "<p style=\"text-align:center;\">");
		x = x.replace("&nbsp;", " ");
		x = Html.toXhtmlBody(x);
		b.append(x);
		writeSceneEnd(b, scene);
	}

	private void writeSceneStart(StringBuilder b, Scene scene) {
		String sceneTitle = getTitle(scene.getTitle());
		String style = "text-align:center;";
		if (byScenes && !sceneTitle.isEmpty()) {
			style += "page-break-before: always;";
		}
		if (layout.getSceneTitle()) {
			b.append(Html.intoH(3, sceneTitle, style));
		}
		b.append(ExportBookToHtml.sceneGetDidascalie(param, scene));
	}

	private void writeSceneEnd(StringBuilder b, Scene scene) {
		if (param.getLayout().getShowReview()) {
			b.append(Review.reviewsToHtml(mainFrame, scene));
		}
		if (layout.getSceneSeparator()) {
			b.append(Html.intoPcenter(layout.getSceneSeparatorValue()));
		}
	}

	private String replaceEndnotes(Scene s, String text) {
		String rc = text;
		for (Endnote e : endnotes) {
			if (e.getScene().equals(s)) {
				String src = String.format("<a .*#endnote_%03d.*</a>", e.getId());
				rc = rc.replaceAll(src, e.getLinkToEndnote(ENDNOTE_FILE));
			}
		}
		return (rc);
	}

	private String getEndnoteFile() {
		return epubDirText + ENDNOTE_FILE;
	}

	private void writeEndnotes() {
		//LOG.printInfos(TT + ".writeEndnotes()");
		File f = new File(getEndnoteFile());
		if (!endnotes.isEmpty()) {
			String title = I18N.getMsg("endnotes");
			StringBuilder buf = new StringBuilder();
			buf.append(XML_HEAD)
			   .append(DOCTYPE)
			   .append("  \"" + XHTML_DTD + "\">\n");
			buf.append(XHTML_TAG);
			buf.append(Html.HEAD_B);
			buf.append(Html.intoTag("title", title));
			buf.append("<style")
			   .append(Html.getAttribute("type", "text/css"))
			   .append(">\n");
			buf.append("body {text-align: justify;}\n"
			   + "p {"
			   + MARGIN_LEFT
			   + MARGIN_TOP
			   + MARGIN_BOTTOM
			   + "}\n");
			buf.append("</style>\n");
			buf.append(Html.HEAD_E);
			buf.append("<body id=\"x").append(title).append("\">\n");
			buf.append(Html.intoH(2, title, "text-align:center;"));
			for (Endnote endnote : endnotes) {
				if (endnote.getScene() == null) {
					LOG.err("EpubExport.getNotes in the endnote n° "
					   + endnote.getNumber() + " the scene is null");
					continue;
				}
				if (endnote.getScene().getChapter() == null) {
					LOG.err("EpubExport.getNotes in the endnote n° "
					   + endnote.getNumber() + " the chapter is null");
					continue;
				}
				String link = endnote.getLinkToScene(getFilename(endnote.getScene().getChapter()));
				String text = endnote.getNotes();
				if (text.startsWith("<p>")) {
					text = "<p>" + link + text.substring(3);
				} else {
					text = "<p>" + link + " " + text + "</p>";
				}
				buf.append(text).append("\n");
			}
			buf.append(Html.BODY_E);
			buf.append(Html.HTML_E);
			writeToFile(f, buf);
		} else if (f.exists()) {
			f.delete();
		}
	}

	private void copyImages() {
		//LOG.printInfos(TT + ".copyImages()");
		try {
			String source = mainFrame.getProject().getPath()
			   + File.separator + "Images";
			String dest = epubDir + File.separator + OEBPS_IMAGES;
			File srcDir = new File(source);
			if (!srcDir.exists()) {
				LOG.err("Images directory \"" + source + "\" doesn't exists");
				return;
			}
			File destDir = new File(dest);
			IOUtil.dirCopy(srcDir, destDir);
			// remove the summary.png if exists
			File f = new File(destDir + File.separator + "summary.png");
			f.delete();
		} catch (IOException e) {
			LOG.err(TT + ".copyImages Exception", e);
		}
	}

	private void writeContentOpf() {
		//LOG.printInfos(TT + ".writeContentOpf()");
		StringBuilder b = new StringBuilder(CONTENT_HEAD);
		b.append(CONTENT_PACKAGE);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		StringBuilder bookid = new StringBuilder();
		if (!book.getUUID().isEmpty()) {
			bookid.append("<dc:identifier")
			   .append(Html.getAttribute("id", "BookId"))
			   .append(Html.getAttribute("opf:scheme", "uuid"))
			   .append(">").append(book.getUUID()).append("</dc:identifier>\n");
		}
		if (!book.getISBN().isEmpty()) {
			bookid.append("<dc:identifier")
			   .append(Html.getAttribute("id", "BookId"))
			   .append(Html.getAttribute("opf:scheme", "isbn"))
			   .append(">").append(book.getISBN()).append("</dc:identifier>\n");
		}
		if (book.getUUID().isEmpty() && book.getISBN().isEmpty()) {
			bookid.append("<dc:identifier")
			   .append(Html.getAttribute("id", "BookId"))
			   .append(Html.getAttribute("opf:scheme", "uuid"))
			   .append(">").append(uuid).append("</dc:identifier>\n");
		}
		b.append(tab(2)).append("<metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n")
		   .append(tab(3)).append(Html.intoTag("dc:title", book.getTitle())).append("\n");
		b.append(tab(3)).append(bookid.toString()).append("\n");
		b.append(tab(3)).append(
		   Html.intoTag("dc:creator", book.getAuthor(), "opf:role=\"aut\""))
		   .append("\n");
		b.append(tab(3)).append(Html.intoTag("dc:language", book.getLanguage()))
		   .append("\n");
		b.append(tab(3)).append(Html.intoTag("dc:type", "text"))
		   .append("\n");
		b.append(tab(3)).append(
		   Html.intoTag("dc:date",
			  dateFormat.format(new Date()), "opf:event=\"creation\""))
		   .append("\n");
		b.append(tab(3))
		   .append(Html.intoTag("dc:description", book.getBlurb()))
		   .append("\n");
		b.append(tab(3)).append("<meta content=\"")
		   .append(Const.getVersion())
		   .append("\" name=\"oStorybook version\" />\n");
		b.append(tab(2)).append("</metadata>\n");
		b.append(writeManifest());
		b.append(tab(2)).append("<spine toc=\"ncx\">\n");
		b.append(tab(3)).append("<itemref idref=\"coverpage\"/>\n");
		if (!book.getBlurb().isEmpty()) {
			b.append(tab(3)).append("<itemref idref=\"coverback\"/>\n");
		}
		b.append(tab(3)).append("<itemref idref=\"titlepage\"/>\n");
		if (SUMMARY) {
			b.append(tab(3)).append("<itemref idref=\"summary\"/>\n");
		}
		for (Object part : mainFrame.project.getList(Book.TYPE.PART)) {
			for (Chapter c : mainFrame.project.chapters.find((Part) part)) {
				b.append(tab(3))
				   .append("<itemref idref=\"x")
				   .append(getFilename(c).replace(".xhtml", ""))
				   .append("\"/>\n");
			}
		}
		if (!endnotes.isEmpty()) {
			b.append(tab(2)).append("<itemref idref=\"endnotes\"/>\n");
		}
		b.append(tab(2)).append("</spine>\n");
		b.append(tab(2)).append(Html.intoTag("guide", "<reference "
		   + "href=\"Text/coverpage.xhtml\" "
		   + "title=\"Cover\" "
		   + "type=\"cover\"/>"));
		b.append("</package>");
		File f = new File(epubDir + File.separator
		   + OEBPS + File.separator + "content.opf");
		IOUtil.fileWriteString(f, b.toString());
	}

	/**
	 * write manifest elements
	 *
	 * @return
	 */
	private String writeManifest() {
		//LOG.printInfos(TT + ".getManifest()");
		StringBuilder b = new StringBuilder();
		b.append(tab(2)).append("<manifest>\n");
		b.append(writeManifestCommon());
		b.append(writeManifestChapters());
		b.append(writeManifestEndnotes());
		b.append(tab(3)).append("<item href=\"toc.ncx\" id=\"ncx\" ")
		   .append("media-type=\"application/x-dtbncx+xml\"").append("/>\n");
		b.append(writeManifestImages());
		return b.toString();
	}

	private String writeManifestChapters() {
		StringBuilder b = new StringBuilder();
		for (Object part : mainFrame.project.getList(Book.TYPE.PART)) {
			for (Chapter chapter : mainFrame.project.chapters.find((Part) part)) {
				String chapterName = getFilename(chapter);
				b.append(tab(3)).append("<item href=\"Text/")
				   .append(chapterName).append("\" ")
				   .append("id=\"x")
				   .append(chapterName.replace(".xhtml", "")).append("\" ")
				   .append("media-type=\"application/xhtml+xml\"/>\n");
			}
		}
		return b.toString();
	}

	private String writeManifestEndnotes() {
		StringBuilder b = new StringBuilder();
		if (!endnotes.isEmpty()) {
			b.append(tab(3))
			   .append("<item href=\"Text/").append(ENDNOTE_FILE).append("\" ")
			   .append("id=\"endnotes\" ")
			   .append(TYPE_XHTML).append("/>\n");
		}
		return b.toString();
	}

	private String writeManifestImages() {
		StringBuilder b = new StringBuilder();
		//references to Images
		File dir = new File(epubDir + File.separator + OEBPS_IMAGES);
		if (dir.exists()) {
			for (File f : dir.listFiles()) {
				String n = f.getAbsolutePath().replace(epubDir + File.separator
				   + OEBPS + File.separator, "");
				String nshort = "img_" + f.getName();
				//nshort = nshort.replace(".", "_");
				String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1);
				b.append(tab(3))
				   .append("<item href=\"")
				   .append(n).append("\" id=\"").append(nshort)
				   .append("\" media-type=\"image/").append(ext).append("\"/>\n");
			}
		} else {
			LOG.trace(dir.getAbsolutePath() + " dir not exists");
		}
		b.append(tab(2)).append("</manifest>\n");
		return (b.toString());
	}

	/**
	 * write manifest common elements
	 *
	 * @return
	 */
	private String writeManifestCommon() {
		StringBuilder b = new StringBuilder();
		b.append(tab(3)).append("<item href=\"Text/coverpage.xhtml\" ")
		   .append("id=\"coverpage\" ")
		   .append(TYPE_XHTML).append("/>\n");
		if (!book.getBlurb().isEmpty()) {
			b.append(tab(3)).append("<item href=\"Text/coverback.xhtml\" ")
			   .append("id=\"coverback\" ")
			   .append(TYPE_XHTML).append("/>\n");
		}
		b.append(tab(3)).append("<item href=\"Text/titlepage.xhtml\" ")
		   .append("id=\"titlepage\" ")
		   .append(TYPE_XHTML).append("/>\n");
		if (SUMMARY) {
			b.append(tab(3)).append("<item href=\"Text/summary.xhtml\" ")
			   .append("id=\"summary\" ")
			   .append(TYPE_XHTML).append("/>\n");
		}
		return b.toString();
	}

	/**
	 * write the title page of the Book (title, subtitle, etc... and dedication)
	 *
	 */
	private void writeTitlepage() {
		StringBuilder b = new StringBuilder();
		b.append(XML_HEAD).append(DOCTYPE).append("  \"" + XHTML_DTD + "\">\n")
		   .append(XHTML_TAG)
		   .append(Html.intoTag("head", Html.getHeadTitle(I18N.getMsg("epub.titlepage"))));
		b.append("<body class=\"ostorybook\" id=\"titlepage\">\n");
		b.append(Html.toXhtml(ExportBookInfo.getTitle(book, true)));
		b.append(ExportBookInfo.getDedication(book, 30));
		b.append(Html.BODY_E).append(Html.HTML_E);
		File f = new File(epubDirText + "titlepage.xhtml");
		writeToFile(f, b);
	}

	/**
	 * replace image link
	 *
	 * @param x
	 * @return
	 */
	private String replaceImages(String x) {
		final Document document = Jsoup.parse(x);
		Elements nodes = document.getElementsByTag("img");
		for (Element n : nodes) {
			if (!n.hasAttr("alt")) {
				File src = new File(n.attr("src"));
				n.attr("src", "../Images/" + src.getName());
				n.attr("alt", src.getName().replace('.', '-'));
			}
		}
		document.outputSettings().syntax(api.jsoup.nodes.Document.OutputSettings.Syntax.xml);
		return document.body().html();
	}

	/**
	 * get a navpoint
	 *
	 * @param classe
	 * @param n
	 * @return
	 */
	private String navPoint(String classe, int n) {
		return "<navPoint "
		   + "class=\"" + classe + "\" "
		   + "id=\"navPoint-" + n + "\" "
		   + "playOrder=\"" + n + "\">\n";
	}

	/**
	 * get a meta String
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	private String getMeta(String name, String value) {
		return tab(2)
		   + "<meta "
		   + Html.getAttribute("name", name)
		   + Html.getAttribute("content", value) + "/>\n";
	}

	/**
	 * write a file content
	 *
	 * @param f
	 * @param b : content
	 */
	private void writeToFile(File f, StringBuilder b) {
		IOUtil.fileWriteString(f, b.toString().replace("<br>", "<br />"));
	}
}
