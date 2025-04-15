/*
 * Copyright (C) 2018 FaVdB
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
import java.awt.HeadlessException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import resources.icons.IconUtil;
import storybook.Const;
import storybook.db.book.Book;
import storybook.db.book.BookParamExport;
import storybook.db.book.BookParamLayout;
import storybook.db.book.BookParamWeb;
import storybook.db.book.WEB_SUMMARY;
import storybook.db.chapter.Chapter;
import storybook.db.chapter.Chapters;
import storybook.db.endnote.Endnote;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.location.Locations;
import storybook.db.part.Part;
import storybook.db.person.Person;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.exim.EXIM;
import storybook.exim.importer.ImportDocument;
import storybook.review.Review;
import storybook.tools.DateUtil;
import storybook.tools.LOG;
import storybook.tools.ListUtil;
import storybook.tools.Markdown;
import storybook.tools.StringUtil;
import storybook.tools.clip.Clip;
import storybook.tools.file.IOUtil;
import storybook.tools.html.Html;
import storybook.tools.html.HtmlHome;
import storybook.ui.MainFrame;

/**
 * Export the whole book to an HTML file (or multifile, or String)
 *
 * @author FaVdB
 */
public class ExportBookToHtml extends AbstractExport {

	private static final String TT = "ExportBookToHtml.",
			ENDNOTEFILE = "endnote.html";
	private Part onlyPart = null;
	private Chapter chapterFirst, chapterLast, chapterPrior, chapterNext, chapterCur;
	private Scene scenePrior, sceneNext;
	private Strand strand;
	private List<Part> parts;
	private List<Chapter> chapters;
	private List<Scene> scenes;
	private List<Endnote> endnotes;
	private StringBuilder buffer = new StringBuilder();
	private boolean bSeparator, tocLink = false, toFile = false,
			review = false, withExternal = false;
	private String sceneSeparator = Const.SCENE_SEPARATOR, imageDir;
	private Chapter lastChapter;
	private BookParamLayout layout;
	private BookParamWeb web;
	private boolean advanced = false;

	private void setAdvanced() {
		advanced = true;
	}

	private boolean isAdvanced() {
		return advanced;
	}

	enum NAV {
		HOME,
		NAV_BACKWARD, NAV_BACKWARD_GRAYED,
		NAV_FORWARD, NAV_FORWARD_GRAYED,
		AR_LEFT, AR_LEFT_GRAYED,
		AR_RIGHT, AR_RIGHT_GRAYED,
		NAV_FIRST, NAV_FIRST_GRAYED,
		NAV_PREV, NAV_PREV_GRAYED,
		NAV_NEXT, NAV_NEXT_GRAYED,
		NAV_LAST, NAV_LAST_GRAYED,
		SUMMARY;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	};

	/**
	 * ExportBookToHtml class
	 *
	 * @param m: MainFrame
	 */
	public ExportBookToHtml(MainFrame m) {
		super(m, F_HTML);
		init();
	}

	/**
	 * initialize
	 */
	@SuppressWarnings("unchecked")
	private void init() {
		//LOG.trace(TT + "init()");
		layout = param.getLayout();
		web = book.param.getParamWeb();
		imageDir = param.getDirectory() + File.separator + "Images";
		sceneSeparator = mainFrame.getBook().getParam().getParamLayout().getSceneSeparatorValue();
		mainFrame.project.parts.sortByNumber();
		parts = (List) mainFrame.project.getList(Book.TYPE.PART);
		mainFrame.project.chapters.sortByNumber();
		chapters = (List) mainFrame.project.getList(Book.TYPE.CHAPTER);
		if (chapters != null && !chapters.isEmpty()) {
			chapterFirst = chapters.get(0);
			chapterLast = chapters.get(chapters.size() - 1);
		}
		mainFrame.project.scenes.sortByNumber();
		scenes = (List) mainFrame.project.getList(Book.TYPE.SCENE);
		mainFrame.project.endnotes.sortByNumber();
		endnotes = Endnote.find(mainFrame, Endnote.TYPE.ENDNOTE);
		withExternal = false;
	}

	public void initParam(String p) {
		while (p.length() < 3) {
			p += "0";
		}
		param.setFormat(F_HTML);
		param.setMultiChapter(p.charAt(0) == '1');
		param.setMultiScene(p.charAt(1) == '1');
		param.setHtmlNav(p.charAt(2) == '1');
	}

	/**
	 * do an export to a file
	 *
	 * @param mainFrame
	 * @param adv: true for an advanced HTML (with iframe)
	 *
	 * @return true if done
	 */
	public static boolean toFile(MainFrame mainFrame, boolean adv) {
		//LOG.trace(TT + "toFile(mainFrame)");
		ExportBookToHtml exp = new ExportBookToHtml(mainFrame);
		exp.withExternal = true;
		if (adv) {
			exp.setAdvanced();
		}
		boolean rc = exp.writeFile();
		if (adv) {
			// first copy the banner to the Images directory
			if (exp.web.getBanner()
					&& !exp.web.getBannerImg().isEmpty()) {
				File src = new File(exp.web.getBannerImg());
				File dest = new File(exp.imageDir + File.separator + src.getName());
				IOUtil.fileCopy(src, dest);
			}
			File inf = new File(mainFrame.getImageDir() + File.separator + "favicon.ico");
			if (inf.exists()) {
				File ouf = new File(exp.imageDir + File.separator + "favicon.ico");
				IOUtil.fileCopy(inf, ouf);
			}
			createHome(exp);
			createSummary(exp, adv);
			createCover(exp);
			if (exp.param.isMulti() && !exp.param.getHtmlNav()) {
				// context multi file && not nav bar then replace index.html by home.html
				//delete index.html
				File indexFile = new File(exp.param.getDirectory() + "/" + "index.html");
				indexFile.delete();
				File homeFile = new File(exp.param.getDirectory() + "/" + "home.html");
				homeFile.renameTo(indexFile);
			}
		}
		return rc;
	}

	/**
	 * create the home.tml for advanced
	 *
	 * @param exp
	 */
	private static void createHome(ExportBookToHtml exp) {
		HtmlHome.write2(exp);
	}

	/**
	 * create the summary.html for advanced
	 *
	 * @param exp
	 * @param adv
	 */
	private static void createSummary(ExportBookToHtml exp, boolean adv) {
		StringBuilder html = new StringBuilder();
		html.append(Html.DOCTYPE)
				.append(Html.HTML_B_LANG)
				.append(Html.META_UTF8)
				.append(getCssSummary(exp.book.param.getParamWeb()))
				.append(Html.HEAD_E)
				.append(Html.BODY_B)
				.append(exp.tocBookGet(0, adv))
				.append(Html.BODY_E).append(Html.HTML_E);
		IOUtil.fileWriteString(exp.param.getDirectory() + File.separator + "summary.html",
				html.toString());

	}

	public static String getCssSummary(BookParamWeb web) {
		StringBuilder b = new StringBuilder("<style>\n");
		b.append("body {\n   font-family: sans-serif; font-size: 100%;\n}\n");
		for (int i = 0; i < 6; i++) {
			String nm = "h" + (i + 1) + " {\n";
			b.append(nm).append(web.getSummary().getCss(i, true)).append("}\n");
		}
		b.append("a {text-decoration: none;}\n"
				+ "a:link {color: inherit;}\n"
				+ "a:visited {color: inherit;}\n"
				+ "a:hover {font-weight: bold; color: blue;}\n"
				+ "</style>");
		return b.toString();
	}

	/**
	 * create the cover.html for advanced
	 *
	 * @param exp
	 * @param adv
	 */
	private static void createCover(ExportBookToHtml exp) {
		StringBuilder html = new StringBuilder();
		html.append(Html.DOCTYPE)
				.append(Html.HTML_B_LANG)
				.append(Html.META_UTF8)
				.append(Html.HEAD_E)
				.append(Html.BODY_B);
		String title = "", subtitle = "";
		//title
		if (exp.web.getBanner() && !exp.web.getBannerImg().isEmpty()) {
			title = exp.book.getTitle();
			subtitle = exp.book.getSubtitle();
		}
		html.append(Html.intoH(1, title, "text-align:center;"));
		html.append(Html.intoH(2, subtitle, "text-align:center;"));
		// authors
		html.append(Html.intoP(I18N.getColonMsg("author_s") + " " + exp.book.getAuthor(), "text-align:center;"));
		// licence
		html.append(Html.intoP(I18N.getColonMsg("copyright") + " " + exp.book.getCopyright(), "text-align:center;"));
		// blurb
		html.append(Html.intoP(exp.book.getBlurb()));
		// dedication
		html.append(ExportBookInfo.getDedication(exp.book, 20));
		// realized with
		/*html.append(Html.P_CENTER)
			.append(
				Html.intoSmall(Html.intoI(I18N.getMsg("export.by") + " " + Const.getFullName())))
			.append(Html.P_E);*/
		html.append(Html.BODY_E).append(Html.HTML_E);
		IOUtil.fileWriteString(exp.param.getDirectory() + File.separator + "cover.html",
				html.toString());
	}

	/**
	 * do an export to a String for printing
	 *
	 * @param mainFrame
	 * @return
	 */
	public static String toPrint(MainFrame mainFrame) {
		//LOG.trace(TT + ".toPrint(mainFrame)");
		ExportBookToHtml exp = new ExportBookToHtml(mainFrame);
		exp.tocLink = false;
		exp.review = false;
		exp.onlyPart = null;
		exp.initParam("000");
		StringBuilder buf = new StringBuilder();
		buf.append(Html.DOCTYPE);
		buf.append(Html.HTML_B);
		buf.append(Html.HEAD_B);
		buf.append("<title>").append(mainFrame.getBook().getTitle()).append("</title>");
		buf.append("<style>"
				+ "body {font-size: 12pt;}"
				+ "h1, h2, h3 {text-align: center;page-break-before:always;}"
				+ "em {background-color: yellow;}"
				+ "</style>");
		buf.append(Html.HEAD_E);
		buf.append(Html.BODY_B);
		buf.append(exp.bookGetTitle(false));
		buf.append(exp.bookGetText());
		buf.append(Html.BODY_E).append(Html.HTML_E);
		return buf.toString();
	}

	/**
	 * get the list of pages for printing
	 *
	 * @param mainFrame
	 * @return
	 */
	public static List<String> toPrintList(MainFrame mainFrame) {
		//LOG.trace(TT + ".toPrint(mainFrame)");
		ExportBookToHtml exp = new ExportBookToHtml(mainFrame);
		exp.tocLink = false;
		exp.review = false;
		exp.onlyPart = null;
		exp.initParam("000");
		Book bk = mainFrame.getBook();
		List<String> ls = new ArrayList<>();
		ls.add(exp.bookGetTitle(false));
		String str;
		ls.add(Html.intoTag("div", ExportBookInfo.getBlurb(bk), "style=\"font-style:italic;\""));
		str = ExportBookInfo.getDedication(bk, 50);
		if (!str.isEmpty()) {
			ls.add(str);
		}
		StringBuilder htmlContent = new StringBuilder();
		htmlContent.append(Html.DOCTYPE)
				.append(Html.HTML_B)
				.append(Html.HEAD_B)
				.append("<title>").append(mainFrame.project.book.getTitle()).append("</title>");
		htmlContent.append("<style>"
				+ " em {background-color: yellow;}"
				+ "</style>")
				.append(Html.HEAD_E);
		htmlContent.append(Html.BODY_B);
		ls.add(htmlContent.toString());

		@SuppressWarnings("unchecked")
		List<Part> parts = (List<Part>) bk.project.parts.getList();
		for (Part part : parts) {
			if (parts.size() > 1) {
				ls.add(Html.intoH(1, part.getName()));
			}
			List<Chapter> chapters = (List<Chapter>) bk.project.chapters.find(part);
			for (Chapter c : chapters) {
				exp.chapterGetContent(c);
				ls.add(exp.buffer.toString());
				exp.buffer = null;
			}
		}
		ls.add((Html.BODY_E));
		ls.add(Html.HTML_E);
		return ls;
	}

	/**
	 * do an export to clipboard, only text part without toc
	 *
	 * @param mainFrame
	 * @param onlyPart
	 */
	public static void toClipboard(MainFrame mainFrame, Part onlyPart) {
		//LOG.trace(TT + "toClipboarb(mainFrame," + (onlyPart == null ? "null" : onlyPart.getName()) + ")");
		ExportBookToHtml exp = new ExportBookToHtml(mainFrame);
		exp.tocLink = false;
		exp.onlyPart = onlyPart;
		exp.initParam("000");
		exp.param.setHighlight(Html.EM_LEFTASIS);
		try {
			String strText = exp.getString()
					.replaceAll("<a name=.*</a>", "");
			Clip.to(strText, "html");
			JOptionPane.showMessageDialog(mainFrame,
					I18N.getMsg("book.copy.text.confirmation"),
					I18N.getMsg("copied.title"), 1);
		} catch (HeadlessException exc) {
		}
	}

	/**
	 * do an export to a String for a JPanel content
	 *
	 * @param mainFrame
	 * @param strand
	 * @param onlyPart
	 * @param review
	 * @param level: 0=normal, 1=parts, 2=chapters
	 * @return
	 */
	public static String toPanel(MainFrame mainFrame,
			Strand strand, Part onlyPart, boolean review, int level) {
		/*LOG.trace(TT + "toPanel(m, strand=" + LOG.trace(strand)
				+ ",onlyPart=" + LOG.trace(onlyPart)
				+ ",level=" + level
				+ ")");*/
		ExportBookToHtml exp = new ExportBookToHtml(mainFrame);
		exp.strand = strand;
		exp.initParam("000");
		exp.tocLink = true;
		exp.review = review;
		exp.onlyPart = onlyPart;
		exp.bSeparator = true;
		StringBuilder b = new StringBuilder();
		b.append(exp.tocBookGet(level, false));
		b.append(exp.bookGetText());
		return b.toString();
	}

	/**
	 * get the book title part
	 *
	 * @param adv
	 * @return
	 */
	public String bookGetTitle(boolean adv) {
		StringBuilder b = new StringBuilder();
		b.append(ExportBookInfo.getTitle(book, false));
		if (adv) {
			b.append(ExportBookInfo.getBlurb(book));
		}
		if (!book.getDedication().isEmpty()) {
			b.append(replaceImages(ExportBookInfo.getDedication(book, 30)));
		}
		return b.toString();
	}

	/**
	 * write a file
	 *
	 * @return
	 */
	public boolean writeFile() {
		//LOG.trace(TT + "writeFile()");
		toFile = true;
		String fileName = IOUtil.removeExtension(mainFrame.project.getName());
		String imgdir = param.getDirectory() + File.separator + "Images" + File.separator;
		if (param.isHtml()) {
			// create the export dir if not exists
			File f = new File(imgdir);
			if (!f.exists() || f.isDirectory()) {
				f.mkdir();
			}
			if (param.isMulti()) {
				for (NAV nav : NAV.values()) {
					copyImage(nav.toString(), imgdir);
				}
				fileName = "index";
			}
		}
		if (!openFile(fileName, true)) {
			return false;
		}
		review = mainFrame.getBook().getParam().getParamLayout().getShowReview();
		writeText(ExportBookInfo.getTitle(book, false));
		writeText(Html.intoP(book.getBlurb(),
				"font-style: italic; text-align: center; margin-left: 10%; margin-right: 10%"));
		writeText(ExportBookInfo.getDedication(book, 30));
		writeText(tocBookGet(0, false));
		bSeparator = true;
		bookGetText();
		closeFile(SILENT);
		// copy used images the imageList was created by bookGetText
		for (String src : imageList) {
			File inFile = new File(src.replace("file:", ""));
			if (inFile.exists()) {
				File outFile = new File(imgdir + inFile.getName());
				IOUtil.fileCopy(inFile, outFile);
			}
		}
		JOptionPane.showMessageDialog(mainFrame,
				I18N.getMsg("export.success", param.getDirectory()),
				I18N.getMsg("export"), JOptionPane.INFORMATION_MESSAGE);
		return true;
	}

	/**
	 * get the Book as a HTML String
	 *
	 * @return
	 */
	public String getString() {
		//LOG.trace(TT + "getString()");
		StringBuilder buf = new StringBuilder();
		param.setMultiChapter(false);
		param.setMultiScene(false);
		param.setHtmlNav(false);
		buf.append(Html.DOCTYPE).append(Html.HTML_B).append(Html.BODY_B);
		buf.append(ExportBookInfo.getTitle(book, false));
		bSeparator = true;
		buf.append(bookGetText());
		buf.append(Html.BODY_E).append(Html.HTML_E);
		return buf.toString();
	}

	/**
	 * check if table of content is needed
	 *
	 * @return
	 */
	private boolean tocBookNeed() {
		return layout.getPartTitle()
				|| layout.getChapterTitle()
				|| layout.getSceneTitle()
				&& (parts.size() > 1 || chapters.size() > 1 || !scenes.isEmpty());
	}

	/**
	 * get the toc of the Book
	 *
	 * @return
	 */
	private String tocBookGet(int startlevel, boolean adv) {
		//LOG.trace(TT + "bookGetToc(level=" + level + ")");
		StringBuilder buf = new StringBuilder();
		if (!tocBookNeed()) {
			return ("<a name=\"toc\"></a>");
		}
		if (layout.getPartTitle()
				|| layout.getChapterTitle()
				|| layout.getSceneTitle()) {
			String t = I18N.getMsg("export.toc") + "</b>";
			if ((adv && web.getSummary().getTitled()) || !adv) {
				buf.append("<p><a name=\"toc\" /><b>")
						.append(t);
				if (adv) {
					buf.append(Html.P_E);
				}
			}
		}
		for (Part part : parts) {
			int level = startlevel;
			if (onlyPart != null && !part.equals(onlyPart)) {
				continue;
			}
			if (layout.getPartTitle()) {
				buf.append(tocPart(part, level, adv));
			}
			List<Chapter> lschapters = mainFrame.project.chapters.find(part);
			for (Chapter chapter : lschapters) {
				buf.append(tocChapter(part, chapter, level + 1, adv));
			}
		}
		if (this.onlyPart == null && endnotesHas()) {
			buf.append(Html.intoH(3, endnotesLinkTo(0)));
		}
		buf.append(Html.P_E);
		if (isOpened && param.isMulti()) {
			chapterNext = chapters.get(0);
			sceneNext = scenes.get(0);
			buf.append(navbar());
			buf.append(Html.BODY_E).append(Html.HTML_E);
			writeText(buf.toString());
			closeFile(SILENT);
			return "";
		}
		return buf.toString();
	}

	/**
	 * get the toc line
	 *
	 * @param level : level of indent
	 * @param href : the href link to
	 * @param text : text of the the link
	 * @return
	 */
	private String tocTo(int level, String href, String text, boolean adv) {
		if (EXIM.getTitle(text).isEmpty()) {
			return "";
		}
		StringBuilder b = new StringBuilder();
		if (!adv) {
			b.append(Html.BR);
			for (int i = 0; i < level; i++) {
				b.append("&emsp;");
			}
		}
		String cpl = "";
		if (adv) {
			cpl = " target=\"mc\"";
		}
		if (!param.isMulti()) {
			b.append(Html.intoA("toc" + href, "#" + href, EXIM.getTitle(text), cpl));
		} else {
			b.append(Html.intoA("toc" + href,
					book.getTitle() + "-" + href + ".html",
					EXIM.getTitle(text), cpl));
		}
		return (adv ? Html.intoH(level, b.toString()) : b.toString());
	}

	private String titleOf(int level, String name, String text, String... style) {
		if (EXIM.getTitle(text).isEmpty()) {
			return "";
		}
		if (level == 0 || text.isEmpty()) {
			return Html.intoA(name, "", "");
		}
		return "<h" + level
				+ (style != null && style.length > 0 ? " style=\"" + style[0] + "\"" : "")
				+ ">"
				+ Html.intoA(name, "", EXIM.getTitle(text))
				+ "</h" + level + ">\n";
	}

	/**
	 * get the Part table of content
	 *
	 * @param part
	 * @param level
	 * @param adv: true for targeted link
	 * @return
	 */
	public String tocPart(Part part, int level, boolean adv) {
		/*LOG.trace(TT + "tocPart(part=" + LOG.trace(part) + ", level=" + level
		+ ", adv=" + (adv ? "true" : "false") + ")");*/
		if (layout.getPartTitle() && (level == 0 || level == 1)) {
			return tocTo(1, part.getIdent(), part.getName(), adv);
		} else {
			return "";
		}
	}

	public String titlePart(Part part) {
		if (layout.getPartTitle()) {
			return titleOf(1, part.getIdent(), part.getName());
		} else {
			return "";
		}
	}

	/**
	 * get the Chapter table of content
	 *
	 * @param part
	 * @param chapter
	 * @param level: 0=normal, 1=parts, 2=chapters, 3=scenes
	 * @param adv: true for advanced
	 * @return
	 */
	public String tocChapter(Part part, Chapter chapter, int level, boolean adv) {
		//LOG.trace(TT + "tocChapter(" + "part=" + LOG.trace(part)
		//	+ ", chapter=" + LOG.trace(chapter) + ", level=" + level + ")");
		int n = level;
		if (layout.getPartTitle()) {
			n++;
		}
		StringBuilder b = new StringBuilder();
		if (layout.getChapterTitle()) {
			if (param.isMultiScene()) {
				Scene s = mainFrame.project.scenes.findFirst(chapter);
				if (s != null) {
					b.append(tocTo(n, s.getIdent(), chapterGetTitle(chapter), adv));
				} else {
					b.append(Html.intoH(n, chapterGetTitle(chapter), "color: gray;"));
				}
			} else {
				b.append(tocTo(n, chapter.getIdent(), chapterGetTitle(chapter), adv));
			}
		} else {
			b.append(tocTo(n, "", chapterGetTitle(chapter), adv));
		}
		for (Scene scene : mainFrame.project.scenes.findBy(chapter)) {
			b.append(tocScene(scene, adv));
		}
		return b.toString();
	}

	/**
	 * get the Scene table of content
	 *
	 * @param scene
	 * @param adv: true for advanced
	 * @return
	 */
	private String tocScene(Scene scene, boolean adv) {
		/*LOG.trace(TT + "tocScene(scene=" + LOG.trace(scene) + ", level=" + level
		+ ", adv=" + (adv ? "true" : "false") + ")");*/
		if (!param.isMultiScene() && !layout.getSceneTitle()) {
			return "";
		}
		int n = 1;
		if (layout.getPartTitle() && mainFrame.project.parts.getCount() > 1) {
			n++;
		}
		if (layout.getChapterTitle()) {
			n++;
		}
		String href = scene.getIdent();
		if (param.isMulti()) {
			StringBuilder b = new StringBuilder();
			if (!adv) {
				b.append(Html.BR);
				for (int i = 0; i < n + scene.getLevel(); i++) {
					b.append("&emsp;");
				}
			}
			b.append(Html.intoA("toc" + href,
					book.getTitle() + "-" + scene.getIdent() + ".html" + "#" + scene.getIdent(),
					EXIM.getTitle(scene.getName()),
					(adv ? "target=\"mc\"" : "")));
			return (adv ? Html.intoH(n/* + scene.getLevel()*/, b.toString()) : b.toString());
		}
		if (sceneIsOK(scene)) {
			return tocTo(n/* + scene.getLevel()*/, scene.getIdent(), scene.getName(), adv);
		}
		return "";
	}

	/**
	 * get a link to a Scene
	 *
	 * @param scene
	 * @param image
	 * @return
	 */
	public String sceneLinkTo(Scene scene, String image) {
		/*LOG.trace(TT + "sceneLinkTo(scene="+LOG.trace(scene)+
			", image="+(image==null?"null":image)+")");*/
		if (scene == null) {
			return "scene is null";
		}
		String link = scene.getName();
		if (image != null && !image.isEmpty()) {
			link = Html.intoIMG("Images/" + image + ".png", link, link, 16);
		}
		if (param.isMulti()) {
			return String.format("<a href=\"%s\">%s</a>",
					filenameOfScene(scene), link);
		}
		return String.format("<a href=\"#%s\">%s</a>", scene.getIdent(), link);
	}

	/**
	 * get a link to an Endnote
	 *
	 * @param n
	 * @param img
	 * @return
	 */
	private String endnotesLinkTo(int n, String... img) {
		//App.trace(TT + "linkToEndnotes()");
		if (endnotes.size() < 1) {
			return "";
		}
		String text = I18N.getMsg("endnotes");
		String href = "#endnotes";
		String number = String.format("%03d", n);
		if (n == 0) {
			number = "";
		} else {
			number = "#" + number;
		}
		if (isOpened && param.isMulti()) {
			if (img != null && img.length > 0) {
				text = Html.intoIMG(img[0], text, text);
			}
			href = ENDNOTEFILE;
		}
		return Html.intoA("", href + number, text);
	}

	/**
	 * get the HTML body (includes parts, chapters and scenes)
	 *
	 * @return a String representation of the HTML code
	 */
	public String bookGetText() {
		//LOG.trace(TT + ".bookGetText()");
		buffer = new StringBuilder();
		if (!withExternal) {
			buffer.append(Html.intoTag("div", ExportBookInfo.getBlurb(book), "style=\"font-style:italic;\""));
		}
		buffer.append(ExportBookInfo.getDedication(book, 50));
		for (Part part : parts) {
			if (onlyPart != null && !part.equals(onlyPart)) {
				continue;
			}
			partGetContent(part);
		}
		if (isOpened && param.isMulti()) {
			buffer.append(navbar());
		}
		writeText(buffer.toString());
		if (onlyPart == null && endnotesHas()) {
			if (param.isMulti()) {
				openFile(ENDNOTEFILE.replace(Html.EXT, ""), true);
				buffer = new StringBuilder();
			}
			buffer.append(endnotesGet());
			if (tocLink) {
				buffer.append(tocGetLink());
			} else {
				if (param.isMulti()) {
					buffer.append(navbar());
				}
			}
			writeText(buffer.toString());
		}
		return buffer.toString();
	}

	/* *************************** */
 /* ******** get Chapter ****** */
 /* *************************** */
	/**
	 * get the begining Chapter context, open the file if it's needed
	 *
	 * @param chapter
	 */
	private String chapterGetBegin(Chapter chapter) {
		if (param.isMultiChapter()) {
			openFile(filenameOfChapter(chapter).replace(Html.EXT, ""), false);
		}
		chapterCur = chapter;
		chapterPrior = chapterFindPrior(chapter);
		chapterNext = chapterFindNext(chapter);
		if (buffer == null) {
			buffer = new StringBuilder();
		}
		return "";
	}

	/**
	 * get a Chapter in HTML code
	 *
	 * @param chapter
	 */
	private String chapterGetContent(Chapter chapter) {
		//LOG.trace(TT + ".chapterGet(chapter=" + App.traceEntity(chapter) + ")");
		StringBuilder b = new StringBuilder(chapterGetBegin(chapter));
		int lev = 1;
		if (layout.getPartTitle()) {
			lev++;
		}
		if (!buffer.toString().isEmpty() && param.isMulti()) {
			openFile(book.getTitle() + "-" + chapter.getIdent(), false);
		}
		if (layout.getChapterTitle()) {
			b.append(titleOf(lev, chapter.getIdent(), chapterGetTitle(chapter)));
			buffer.append(titleOf(lev, chapter.getIdent(), chapterGetTitle(chapter)));
		}
		b.append(chapterGetDidascalie(chapter));
		buffer.append(chapterGetDidascalie(chapter));
		b.append(chapterGetDescription(chapter));
		buffer.append(chapterGetDescription(chapter));
		if (param.isMultiScene()) {
			buffer.append(navbar());
			writeText(buffer.toString());
			closeFile(SILENT);
			buffer = new StringBuilder();
		}
		for (Scene scene : mainFrame.project.scenes.findBy(chapter)) {
			if (sceneIsOK(scene)) {
				sceneGetContent(scene);
			}
		}
		b.append(chapterGetEnd());
		return b.toString();
	}

	/**
	 * do the ending action for the current Chapter
	 */
	private String chapterGetEnd() {
		if (param.isMultiChapter() && !buffer.toString().isEmpty()) {
			buffer.append(Html.P_E);
			buffer.append(navbar());
			writeText(buffer.toString());
			closeFile(SILENT);
			buffer = new StringBuilder();
		}
		return "";
	}

	/**
	 * get the Chapter description
	 *
	 * @param chapter
	 * @return
	 */
	private String chapterGetDescription(Chapter chapter) {
		//App.trace(TT + "chapterGetDescription(chapter=" + App.traceEntity(chapter) + ")");
		if (layout.getChapterDescription() && !chapter.getDescription().isEmpty()) {
			return (Html.intoP(Html.intoI(chapter.getDescription()), "margin-left: 3.5cm;text-align: right;"));
		}
		return "";
	}

	/**
	 * get the Chapter didascalie (dates and locations)
	 *
	 * @param chapter
	 * @return
	 */
	private String chapterGetDidascalie(Chapter chapter) {
		//App.trace(TT + "chapterGetDateLocation(chapter=" + App.traceEntity(chapter) + ")");
		StringBuilder b = new StringBuilder();
		if (layout.getChapterDateLocation()) {
			String date = DateUtil.getNiceDates(Chapters.findDates(mainFrame, chapter));
			String loc = ListUtil.join(Locations.find(mainFrame, chapter), ", ");
			if (!(date + loc).isEmpty()) {
				StringBuilder bx = new StringBuilder();
				if (!date.isEmpty()) {
					bx.append(date);
				}
				if (!loc.isEmpty()) {
					if (!date.isEmpty()) {
						bx.append(" : ");
					}
					bx.append(loc);
				}
				b.append(Html.intoP(Html.intoI(bx.toString()), "margin-right: 3.5cm;"));
			}
		}
		return b.toString();
	}

	/**
	 * get the Chapter title, if needed, with number
	 *
	 * @param chapter
	 * @return
	 */
	private String chapterGetTitle(Chapter chapter) {
		StringBuilder b = new StringBuilder();
		if (layout.getChapterTitle()) {
			if (layout.getChapterNumber()) {
				if (layout.getChapterRoman()) {
					b.append(StringUtil.intToRoman(chapter.getChapterno())).append(" ");
				} else {
					b.append(chapter.getChapterno().toString()).append(" ");
				}
			}
			b.append(getTitle(chapter.getName()));
		}
		return b.toString();
	}

	/**
	 * find the prior chapter
	 *
	 * @param chapter
	 * @return null if there is no prior chapter
	 */
	private Chapter chapterFindPrior(Chapter chapter) {
		for (int i = 0; i < scenes.size(); i++) {
			Chapter s = chapters.get(i);
			if (s.getId().equals(chapter.getId())) {
				if (i > 0) {
					return chapters.get(i - 1);
				}
				break;
			}
		}
		return null;
	}

	/**
	 * find the next chapter
	 *
	 * @param chapter
	 * @return null if there is no next chapter
	 */
	private Chapter chapterFindNext(Chapter chapter) {
		for (int i = 0; i < chapters.size(); i++) {
			Chapter s = chapters.get(i);
			if (s.getId().equals(chapter.getId())) {
				if (i < chapters.size() - 1) {
					return chapters.get(i + 1);
				}
				break;
			}
		}
		return null;
	}

	/**
	 * check for Scene possible insertion
	 *
	 * @param scene
	 * @return true if the Scene would be inserted
	 */
	private boolean sceneIsOK(Scene scene) {
		return !(scene.getInformative()
				|| (strand != null
				&& (!strand.equals(scene.getStrand())
				&& !scene.getStrands().contains(strand))));
	}

	/**
	 * get the Scene HTML code
	 *
	 * @param scene
	 */
	private String sceneGetContent(Scene scene) {
		//LOG.trace(TT + "sceneGetContent(scene=" + LOG.printInfos(scene) + ")");
		StringBuilder sb = new StringBuilder();
		sb.append(sceneBegin(scene));
		String x = scene.getTextToHtml(param.isMulti());
		if (withExternal) {
			x = getTextExternal(scene, param.isMulti());
		}
		if (toFile) {
			x = replaceImages(x);
		}
		if (book.info.markdownGet()) {
			Markdown md = new Markdown("ExportBook", "text/plain", "");
			md.setHeader(scene, book.info.scenarioGet());
			md.setText(scene.getTextToHtml(param.isMultiScene()));
			x = md.getHtmlBody();
		} else {
			x = Html.emTag(x, param.getHighlight());
		}
		if (param.isMulti() && x.contains("<a href=\"#S")) {
			x = linkReplaceInternal(x);
		}
		x = x.replace("<p align=\"center\">", "<p style=\"text-align:center;\">");
		sb.append(x).append("\n");
		if (review) {
			sb.append(Review.reviewsToHtml(mainFrame, scene));
		}
		if (tocBookNeed() && !isOpened && tocLink) {
			sb.append(tocGetLink());
		}
		buffer.append(sb.toString());
		buffer.append(sceneEnd());
		if (param.isMultiScene() && !buffer.toString().isEmpty()) {
			buffer.append(navbar());
			writeText(buffer.toString());
			closeFile(SILENT);
			buffer = new StringBuilder();
		}
		return sb.toString();
	}

	/**
	 * find the prior Scene
	 *
	 * @param scene
	 * @return null if there is no prior Scene
	 */
	private Scene sceneFindPrior(Scene scene) {
		for (int i = 0; i < scenes.size(); i++) {
			Scene s = scenes.get(i);
			if (s.getId().equals(scene.getId())) {
				if (i > 0) {
					return scenes.get(i - 1);
				}
				break;
			}
		}
		return null;
	}

	/**
	 * find the next Scene
	 *
	 * @param scene
	 * @return null if there is no next Scene
	 */
	private Scene sceneFindNext(Scene scene) {
		for (int i = 0; i < scenes.size(); i++) {
			Scene s = scenes.get(i);
			if (s.getId().equals(scene.getId())) {
				if (i < scenes.size() - 1) {
					return scenes.get(i + 1);
				}
				break;
			}
		}
		return null;
	}

	/**
	 * get the begining of Scene, open the file if it's needed
	 *
	 * @param scene
	 */
	private String sceneBegin(Scene scene) {
		StringBuilder sb = new StringBuilder();
		if (param.isMultiScene()) {
			if (!buffer.toString().isEmpty()) {
				buffer.append(navbar());
				writeText(buffer.toString());
				closeFile(SILENT);
				buffer = new StringBuilder();
			}
			openFile(filenameOfScene(scene).replace(Html.EXT, ""), false);
		}
		scenePrior = sceneFindPrior(scene);
		sceneNext = sceneFindNext(scene);
		if (buffer == null) {
			buffer = new StringBuilder();
		}
		StringBuilder title = new StringBuilder();
		String spanB = WEB_SUMMARY.getAdvancedStyle(book.param.getParamWeb(), true);
		if (layout.getSceneTitle() && !getTitle(scene.getName()).isEmpty()) {
			int n = 1;
			if (param.isMultiScene() && isAdvanced()) {
				if (mainFrame.project.parts.getList().size() > 1) {
					title.append(scene.getChapter().getPart().getName()).append(" > ");
				}
				if (mainFrame.project.chapters.getList().size() > 1) {
					title.append(scene.getChapter().getName()).append(" > ");
				}
			} else {
				spanB = "";
			}
			if (layout.getPartTitle()) {
				n++;
			}
			if (layout.getChapterTitle()) {
				n++;
			}
			if (!scene.getChapter().equals(lastChapter) && layout.getChapterTitle()) {
				lastChapter = scene.getChapter();
				//sb.append(titleOf(n, lastChapter.getIdent(), getTitle(lastChapter.getName())));
			}
			title.append(getTitle(scene.getName()));
			sb.append(titleOf(n + 1, scene.getIdent(), title.toString(), spanB));
		} else if (param.isMultiScene() && isAdvanced()
				&& layout.getSceneTitle() && !scene.getName().isEmpty()) {
			if (mainFrame.project.parts.getList().size() > 1) {
				title.append(scene.getChapter().getPart().getName()).append(" > ");
			}
			if (mainFrame.project.chapters.getList().size() > 1) {
				title.append(scene.getChapter().getName());
			}
			sb.append(titleOf(3, scene.getIdent(), title.toString(), spanB));
		}
		if (layout.getSceneDidascalie()) {
			sb.append(sceneGetDidascalie(param, scene));
		}
		return sb.toString();
	}

	/**
	 * do the ending action for the current Scene
	 */
	private String sceneEnd() {
		if (layout.getSceneSeparator() && bSeparator) {
			buffer.append(Html.P_CENTER).append(sceneSeparator).append("</p>\n");
		}
		return "";
	}

	/**
	 * get the link to toc
	 *
	 * @return
	 */
	public String tocGetLink() {
		//return Html.intoP(
		//		Html.intoA("", "#toc", I18N.getMsg("toc")),
		//		Html.FONT_SIZE + ":8px;" + Html.ALIGN_LEFT);
		String img = "file://" + mainFrame.getImageDir() + "/summary.png";
		if (isOpened) {
			img = imageDir + "/" + "summary.png";
		}
		StringBuilder b = new StringBuilder();
		b.append("<table "
				+ "style=\"border: none; "
				+ "border-collapse: collapse; "
				+ "width:100%; "
				+ "background-color: rgb(220, 220, 220); "
				+ "\">\n").append(Html.TR_B);
		b.append(Html.intoTD(
				Html.intoA("", "#toc", Html.intoIMG(img, "", I18N.getMsg("toc"), 20)),
				"style=\""
				+ Html.FONT_SIZE + ":8px;"
				+ Html.AL_LEFT, "text-align:center;padding:0px;"
				+ "\""));
		b.append(Html.TR_E).append(Html.TABLE_E);
		return b.toString();
	}

	/**
	 * get the Endnotes
	 *
	 * @return the String text of the HTML code
	 */
	public String endnotesGet() {
		StringBuilder buf = new StringBuilder();
		buf.append(Html.intoA("endnotes", "", ""));
		buf.append(Html.intoH(1, I18N.getMsg("endnote")));
		for (Object obj : endnotes) {
			Endnote endnote = (Endnote) obj;
			if (endnote.getScene() == null) {
				LOG.err(TT + ".getNotes endnote n° " + endnote.getNumber() + " the scene is null");
				continue;
			}
			String link = endnote.getLinkToScene(filenameOfScene(endnote.getScene()));
			String text = endnote.getNotes();
			if (text.startsWith(Html.P_B)) {
				text = Html.P_B + link + text.substring(3);
			} else {
				text = Html.P_B + link + " " + text + Html.P_E;
			}
			buf.append(text).append("\n");
		}
		if (param.isMultiScene()) {
			writeText(buf.toString());
			closeFile(SILENT);
			buf = new StringBuilder();
		}
		return (buf.toString());
	}

	/**
	 * get the didascalie of a Scene
	 *
	 * @param exp
	 * @param scene
	 * @return a String reprenting the HTML code
	 */
	public static String sceneGetDidascalie(BookParamExport exp, Scene scene) {
		//App.trace(TT + "getDidascalie("+scene.titleGet()+")");
		String rc = "";
		if (exp.param.getParamLayout().getSceneDidascalie()) {
			if (!scene.getPersons().isEmpty()) {
				rc += "<i>" + Html.intoB(I18N.getMsg("persons")) + " : ";
				for (Person p : scene.getPersons()) {
					rc += p.getFullName() + ", ";
				}
				rc = rc.substring(0, rc.length() - 2);
				rc += "</i>" + Html.BR;
			}
			if (!scene.getLocations().isEmpty()) {
				rc += "<i>" + Html.intoB(I18N.getMsg("locations")) + " : ";
				for (Location p : scene.getLocations()) {
					rc += p.getFullName() + ", ";
				}
				rc = rc.substring(0, rc.length() - 2);
				rc += "</i>" + Html.BR;
			}
			if (!scene.getItems().isEmpty()) {
				rc += "<i>" + Html.intoB(I18N.getMsg("items")) + " : ";
				for (Item p : scene.getItems()) {
					rc += p.getName() + ", ";
				}
				rc = rc.substring(0, rc.length() - 2);
				rc += "</i>" + Html.BR;
			}
			if (!scene.getScenario_pitch().isEmpty()) {
				rc += "<i>" + Html.intoB(I18N.getMsg("scenario.pitch")) + " : ";
				rc += scene.getScenario_pitch();
				rc += "</i>" + Html.BR;
			}
			if (!rc.isEmpty()) {
				rc = Html.intoP(rc, "text-align:right");
			}
		}
		return (rc);
	}

	/**
	 * replace internal link to real link
	 *
	 * @param x: String for the text to parse
	 *
	 * @return the replacing String
	 */
	private String linkReplaceInternal(String x) {
		//LOG.trace(TT + "linkReplaceInternal(x="+x+")");
		if (!x.contains("<a href=\"#S") || !param.isMulti()) {
			return (x);
		}
		try {
			Document doc = Jsoup.parse(x);
			Elements links = doc.select("a");
			for (Element e : links) {
				String href = e.attr("href");
				if (href.startsWith("#S")) {
					String r = href.replace("#S", "");
					Long id = Long.valueOf(r);
					if (id == -1L) {
						continue;
					}
					Scene sc = (Scene) mainFrame.project.get(Book.TYPE.SCENE, id);
					if (sc == null) {
						continue;
					}
					if (param.isMultiChapter()) {
						if (!sc.hasChapter()) {
							break;
						}
						r = String.format("%s-%s%s#%s",
								book.getTitle(),
								sc.getChapter().getIdent(),
								Html.EXT,
								sc.getIdent());
					} else if (param.isMultiScene()) {
						r = String.format("%s-%s%s",
								book.getTitle(),
								sc.getChapter().getIdent(),
								Html.EXT);
					}
					e.attr("href", r);
				}
			}
			return doc.select("body").get(0).children().toString();
		} catch (NumberFormatException ex) {
			return x;
		}
	}

	/**
	 * get nav bar for chapter and/or scene
	 *
	 * @return
	 */
	private String navbar() {
		if (!param.getHtmlNav()) {
			return "";
		}
		StringBuilder b = new StringBuilder();
		b.append("<table style=\"width:100%; background-color: lightgray;\">\n");
		b.append(Html.TR_B);
		// to summary
		String toc = I18N.getMsg("export.nav.summary");
		String link = Html.intoIMG("Images/" + NAV.HOME + ".png", toc, toc, 16);
		if (param.getHtmlNavImage()) {
			link = Html.intoA("", "index.html", link);
		} else {
			link = Html.intoA("", "index.html", toc);
		}
		b.append(Html.intoTD(link));

		if (param.isMultiChapter()) {
			b.append(Html.intoTD(navbarChapter(), "text-align:right"));
		}
		if (param.isMultiScene()) {
			b.append(Html.intoTD(navbarScene(), "text-align:right"));
		}
		b.append(Html.TR_E);
		b.append(Html.TABLE_E);
		return b.toString();
	}

	/**
	 * get Chapter nav bar
	 *
	 * @return
	 */
	private String navbarChapter() {
		StringBuilder buf = new StringBuilder();
		if (!param.getHtmlNav()) {
			return "";
		}
		buf.append("<p style=\"text-align: right;\">");
		String link = navbarChapterLinkto(null, NAV.NAV_FIRST_GRAYED.toString());
		// first chapter
		if (chapterCur != null && !chapterCur.equals(chapterFirst)) {
			link = navbarChapterLinkto(chapterFirst, NAV.NAV_FIRST.toString());
		}
		buf.append(link);

		// to previous chapter
		if (chapterPrior != null && !chapterCur.equals(chapterPrior)) {
			link = navbarChapterLinkto(chapterPrior, NAV.NAV_BACKWARD.toString());
		} else {
			link = navbarChapterLinkto(null, NAV.NAV_BACKWARD_GRAYED.toString());
		}
		buf.append(link);
		// to next chapter
		if (chapterNext != null && !chapterNext.equals(chapterLast)) {
			link = navbarChapterLinkto(chapterNext, NAV.NAV_FORWARD.toString());
		} else if (chapterCur != null && chapterCur.equals(chapterLast)) {
			link = endnotesLinkTo(0, NAV.NAV_FORWARD.toString());
		} else {
			link = navbarChapterLinkto(null, NAV.NAV_FORWARD_GRAYED.toString());
		}
		buf.append(link);
		// to last chapter
		if (chapterCur != null && !chapterCur.equals(chapterLast)) {
			link = navbarChapterLinkto(chapterLast, NAV.NAV_LAST.toString());
		} else {
			link = navbarChapterLinkto(null, NAV.NAV_LAST_GRAYED.toString());
		}
		buf.append(link);
		buf.append(Html.P_E);
		return (buf.toString());
	}

	/**
	 * get a link to a Chapter for the nav bar
	 *
	 * @param chapter
	 * @param img
	 * @return
	 */
	private String navbarChapterLinkto(Chapter chapter, String img) {
		/*LOG.trace(TT + "linkToChapter("+
			"chapter="+LOG.printInfos(chapter)+
			", img="+(img==null?"null":img)+")");*/
		if (chapter == null) {
			return Html.intoIMG("Images/" + img + ".png", "", "");
		}
		String title = "";
		if (layout.getChapterNumber()) {
			if (layout.getChapterRoman()) {
				title = (String) StringUtil.intToRoman(chapter.getChapterno()) + " ";
			} else {
				title = chapter.getChapterno().toString() + " ";
			}
		}
		if (layout.getChapterTitle()) {
			title += chapter.getName();
		}
		if (img != null && !img.isEmpty() && param.getHtmlNavImage()) {
			title = Html.intoIMG("Images/" + img + ".png", title, title);
		}
		if (isOpened && param.isMulti()) {
			return (String.format("<a href=\"%s\">%s</a>", filenameOfChapter(chapter), title));
		}
		return String.format("<a href=\"#C%s\">%s</a>", chapter.getIdent(), title);
	}

	/**
	 * get nav bar for the current Scene
	 *
	 * @return
	 */
	private String navbarScene() {
		StringBuilder buf = new StringBuilder();
		buf.append("<p style=\"text-align: right;\">");
		String link = "";
		// to prior scene
		if (scenePrior != null) {
			link = sceneLinkTo(scenePrior, NAV.AR_LEFT.toString());
		} else {
			if (param.getHtmlNavImage()) {
				link = Html.intoIMG("Images/" + NAV.AR_LEFT_GRAYED + ".png", "", "", 16);
			}
		}
		buf.append(link);

		// to next scene
		if (sceneNext != null) {
			link = sceneLinkTo(sceneNext, NAV.AR_RIGHT.toString());
		} else {
			link = Html.intoIMG("Images/" + NAV.AR_RIGHT_GRAYED + ".png", "", "", 16);
		}
		buf.append(link);
		return buf.toString();
	}

	/**
	 * get the Chapter file name
	 *
	 * @param chapter
	 * @return
	 */
	private String filenameOfChapter(Chapter chapter) {
		return (String.format("%s-%s.html", book.getTitle(), chapter.getIdent()));
	}

	/**
	 * get the Scene file name
	 *
	 * @param scene
	 * @return
	 */
	private String filenameOfScene(Scene scene) {
		if (!param.isMultiChapter() && !param.isMultiScene()) {
			return ("");
		}
		return Scene.getFilename(scene, book.getTitle());
	}

	/**
	 * check if endnotes is not empty
	 *
	 * @return
	 */
	private boolean endnotesHas() {
		return (endnotes != null && !endnotes.isEmpty());
	}

	/* **** get a Part **** */
	/**
	 * get the begining of a given Part
	 *
	 * @param part
	 */
	private String partGetBegin(Part part) {
		if (param.isMulti() && part != null) {
			//openFile(partGetFilename(part).replace(Html.EXT, ""), true);
			//buffer = new StringBuilder();
		}
		return partGetTitle(part);
	}

	/**
	 * get the content of a given Part
	 *
	 * @param part
	 */
	private String partGetContent(Part part) {
		buffer.append(partGetBegin(part));
		for (Chapter chapter : chapters) {
			if (chapter.hasPart() && chapter.getPart().equals(part)) {
				chapterGetContent(chapter);
			}
		}
		buffer.append(partGetEnd());
		return buffer.toString();
	}

	/**
	 * get the ending of a given Part
	 */
	private String partGetEnd() {
		if (param.isMulti()) {
			writeFileEnd();
		}
		return "";
	}

	/**
	 * get title of a Part
	 *
	 * @param part
	 * @return
	 */
	public String partGetTitle(Part part) {
		//App.trace(TT + "getPartTitle("+part.getName()+")");
		if (layout.getPartTitle()) {
			String s = getTitle(part.getName());
			if (!s.isEmpty()) {
				return titleOf(1, part.getIdent(), getTitle(part.getName()));
			}
		}
		return "";
	}

	/**
	 * copy an image from resources to the export directory
	 *
	 * @param img: image source name without extent
	 * @param outdir: the export directory
	 */
	private void copyImage(String img, String outdir) {
		File fdir = new File(outdir);
		fdir.mkdirs();
		File out = new File(outdir + File.separator + img + ".png");
		if (out.exists()) {
			return;

		}
		Class<?> x = IconUtil.class;
		String path = "png/" + img.toLowerCase().replace(".", "/") + ".png";
		InputStream resource = x.getResourceAsStream(path);
		if (resource == null) {
			LOG.err(TT + ".copyImage(img=" + img + ", dir=" + outdir + ")" + "unable to found " + path);
			return;
		}
		try {
			InputStream in = resource;
			try (OutputStream writer = new BufferedOutputStream(new FileOutputStream(out))) {
				byte[] bx = new byte[1024];
				int length;
				while ((length = in.read(bx)) >= 0) {
					writer.write(bx, 0, length);
				}
			}
		} catch (IOException ex) {
			LOG.err("ExportBookHtml.copyImage " + ex.getLocalizedMessage());
		}
	}

	/**
	 * get the text for an external
	 *
	 * @param scene
	 * @param isMulti
	 * @return
	 */
	public String getTextExternal(Scene scene, boolean isMulti) {
		String txt = scene.getSummary();
		if (scene.getOdf() != null && !scene.getOdf().isEmpty()) {
			File f = new File(scene.getOdf());
			if (f.exists()) {
				ImportDocument doc = new ImportDocument(mainFrame, f);
				if (doc.openDocument()) {
					txt = doc.getDocument().body().html();
					doc.close();
				}
			}
		}
		if (isMulti) {
			txt = txt.replace("#endnote", "./endnote.html#endnote");
		}
		return txt;
	}

	/**
	 * write the end of the file
	 */
	private void writeFileEnd() {
		buffer.append(Html.BODY_E);
		buffer.append(Html.HTML_E);
		writeText(buffer.toString());
		closeFile(false);
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
			if (n.attr("src").startsWith("file:")) {
				File src = new File(n.attr("src"));
				// add the image file name to the images list
				imageList.add(n.attr("src"));
				n.attr("src", "Images/" + src.getName());
				n.attr("alt", src.getName().replace('.', '-'));
			}
		}
		document.outputSettings().syntax(api.jsoup.nodes.Document.OutputSettings.Syntax.xml);
		return document.body().html();
	}

}
