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
import java.net.URL;
import java.util.List;
import javax.swing.JOptionPane;
import resources.icons.IconUtil;
import storybook.Const;
import storybook.db.book.Book;
import storybook.db.book.BookParamExport;
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
import storybook.ui.MainFrame;

/**
 * Export the whole book to an HTML file (or multifile, or String)
 *
 * @author FaVdB
 */
public class ExportBookToHtml extends AbstractExport {

	private static final String TT = "ExportBookToHtml",
	   INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;",
	   ENDNOTEFILE = "endnote.html";
	public boolean withExternal = false;
	private Chapter chapterFirst, chapterLast, chapterPrior, chapterNext, chapterCur;
	private Scene scenePrior, sceneNext;
	private StringBuilder buffer = new StringBuilder();
	private boolean bSeparator;
	private String sceneSeparator = Const.SCENE_SEPARATOR;
	private Strand strand;
	private List<Part> parts;
	private List<Chapter> chapters;
	private List<Scene> scenes;
	private Part onlyPart = null;
	private boolean tocLink = false;
	private List<Endnote> endnotes;
	public boolean review = false;
	private List<Endnote> comments;
	private String imageDir;
	private boolean toFile = false;

	enum NAV {
		NAV_BACKWARD,
		NAV_BACKWARD_GRAYED,
		NAV_FORWARD,
		NAV_FORWARD_GRAYED,
		AR_LEFT,
		AR_LEFT_GRAYED,
		AR_RIGHT,
		AR_RIGHT_GRAYED,
		NAV_FIRST,
		NAV_FIRST_GRAYED,
		NAV_PREV,
		NAV_PREV_GRAYED,
		NAV_NEXT,
		NAV_NEXT_GRAYED,
		NAV_LAST,
		NAV_LAST_GRAYED,
		SUMMARY;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	};

	/**
	 * ExportBookToHtml class
	 *
	 * @param m
	 */
	public ExportBookToHtml(MainFrame m) {
		super(m, "html");
		init();
	}

	/**
	 * initialize
	 *
	 */
	@SuppressWarnings("unchecked")
	private void init() {
		//LOG.printInfos(TT + ".init()");
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
		comments = Review.find(mainFrame);
		withExternal = false;
	}

	/**
	 * do an export to a file
	 *
	 * @param mainFrame
	 * @return true if done
	 */
	public static boolean toFile(MainFrame mainFrame) {
		//LOG.printInfos(TT + ".toFile(mainFrame)");
		ExportBookToHtml exp = new ExportBookToHtml(mainFrame);
		exp.withExternal = true;
		return exp.writeFile();
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
		exp.param.setFormat(F_HTML);
		exp.param.setHtmlMultiChapter(false);
		exp.param.setHtmlMultiScene(false);
		exp.param.setHtmlNav(false);
		StringBuilder buf = new StringBuilder();
		buf.append(Html.DOCTYPE);
		buf.append(Html.HTML_B);
		buf.append(Html.HEAD_B);
		buf.append("<title>").append(mainFrame.getBook().getTitle()).append("</title>");
		buf.append("<style>"
		   + "body {font-size: 12pt;}"
		   + "em {background-color: yellow;}"
		   + "</style>");
		buf.append(Html.HEAD_E);
		buf.append(Html.BODY_B);
		buf.append(exp.bookGetTitle());
		buf.append(exp.bookGetText());
		return buf.toString();
	}

	/**
	 * do an export to clipboard, only text part without toc
	 *
	 * @param mainFrame
	 * @param onlyPart
	 */
	public static void toClipboard(MainFrame mainFrame, Part onlyPart) {
		//LOG.printInfos(TT + ".toClipboarb(mainFrame," + (onlyPart == null ? "null" : onlyPart.getName()) + ")");
		ExportBookToHtml exp = new ExportBookToHtml(mainFrame);
		exp.tocLink = false;
		exp.onlyPart = onlyPart;
		exp.param.setFormat(F_HTML);
		exp.param.setHtmlMultiChapter(false);
		exp.param.setHtmlMultiScene(false);
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
		/*LOG.printInfos(TT + ".toPanel(m, strand=" + (strand == null ? "null" : strand.toPrint())
				+ ",onlyPart=" + (onlyPart != null ? "true" : "false")
				+ ",tocLevel=" + tocLevel
				+ ")");*/
		ExportBookToHtml exp = new ExportBookToHtml(mainFrame);
		exp.strand = strand;
		exp.param.setFormat(F_HTML);
		exp.param.setHtmlMultiChapter(false);
		exp.param.setHtmlMultiScene(false);
		exp.param.setHtmlNav(false);
		exp.tocLink = true;
		exp.review = review;
		exp.onlyPart = onlyPart;
		exp.bSeparator = true;
		StringBuilder lbuf = new StringBuilder();
		lbuf.append(exp.bookGetToc(level));
		lbuf.append(exp.bookGetText());
		return lbuf.toString();
	}

	/**
	 * get the book title part
	 *
	 * @return
	 */
	public String bookGetTitle() {
		StringBuilder b = new StringBuilder();
		b.append(Html.intoP(ExportBook.getTitle(book, false)));
		b.append(ExportBook.getDedication(book, 30));
		return b.toString();
	}

	/**
	 * write a file
	 *
	 * @return
	 */
	public boolean writeFile() {
		//LOG.trace(TT + ".writeFile()");
		toFile = true;
		String fileName = mainFrame.getProject().getName();
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
		writeText(ExportBook.getTitle(book, false));
		writeText(Html.intoP(book.getBlurb(),
		   "font-style: italic; text-align: center; margin-left: 10%; margin-right: 10%"));
		writeText(ExportBook.getDedication(book, 30));
		bookGetToc(0);
		bSeparator = true;
		bookGetText();
		closeFile(SILENT);
		// copy used images
		for (String src : imageList) {
			File inFile = new File(src.replace("file://", ""));
			File outFile = new File(imgdir + inFile.getName());
			IOUtil.fileCopy(inFile, outFile);
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
		//LOG.printInfos(TT + ".getString()");
		StringBuilder buf = new StringBuilder();
		param.setHtmlMultiChapter(false);
		param.setHtmlMultiScene(false);
		param.setHtmlNav(false);
		buf.append(Html.DOCTYPE).append(Html.HTML_B).append(Html.BODY_B);
		buf.append(ExportBook.getTitle(book, false));
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
	private boolean bookNeedToc() {
		return param.getLayout().getPartTitle()
		   || param.getLayout().getChapterTitle()
		   || param.getLayout().getSceneTitle()
		   && (parts.size() > 1 || chapters.size() > 1 || !scenes.isEmpty());
	}

	/**
	 * get the toc of the Book
	 *
	 * @return
	 */
	private String bookGetToc(int level) {
		//LOG.printInfos(TT + ".bookGetToc(level=" + level + ")");
		StringBuilder buf = new StringBuilder();
		if (!bookNeedToc()) {
			return ("<a name=\"toc\"></a>");
		}
		if (param.getLayout().getPartTitle()
		   || param.getLayout().getChapterTitle()
		   || param.getLayout().getSceneTitle()) {
			buf.append("<p><a name=\"toc\" /><b>")
			   .append(I18N.getMsg("export.toc")).append("</b>");
		}
		for (Part part : parts) {
			if (onlyPart != null && !part.equals(onlyPart)) {
				continue;
			}
			if (param.getLayout().getPartTitle()) {
				buf.append(tocPart(part, level));
			}
			List<Chapter> lschapters = mainFrame.project.chapters.find(part);
			for (Chapter chapter : lschapters) {
				buf.append(tocChapter(part, chapter, level));
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
		writeText(buf.toString());
		return buf.toString();
	}

	private String tocTo(int level, String href, String text) {
		if (EXIM.getTitle(text).isEmpty()) {
			return "";
		}
		StringBuilder b = new StringBuilder();
		b.append(Html.BR);
		for (int i = 0; i < level; i++) {
			b.append("&emsp;");
		}
		if (!param.isMulti()) {
			b.append(Html.intoA("toc" + href,
			   "#" + href,
			   EXIM.getTitle(text)));
		} else {
			b.append(Html.intoA("toc" + href,
			   book.getTitle() + "-" + href + ".html",
			   EXIM.getTitle(text)));
		}
		return b.toString();
	}

	private String titleOf(int level, String name, String text) {
		if (EXIM.getTitle(text).isEmpty()) {
			return "";
		}
		if (level == 0 || text.isEmpty()) {
			return Html.intoA(name, "", "");
		}
		return "<h" + level + ">"
		   + Html.intoA(name, "", EXIM.getTitle(text))
		   + "</h" + level + ">\n";
	}

	/**
	 * get the Part table of content
	 *
	 * @param part
	 * @param level
	 * @return
	 */
	public String tocPart(Part part, int level) {
		//LOG.printInfos(TT + ".tocPart(part=" + LOG.printInfos(part) + ", level=" + level + ")");
		if (param.getLayout().getPartTitle() && (level == 0 || level == 1)) {
			return tocTo(1, part.getIdent(), part.getName());
		}
		return "";
	}

	public String titlePart(Part part) {
		if (param.getLayout().getPartTitle()) {
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
	 * @return
	 */
	public String tocChapter(Part part, Chapter chapter, int level) {
		//LOG.printInfos(TT + ".tocChapter(" + "part=" + LOG.printInfos(part)
		//+ ", chapter=" + LOG.printInfos(chapter) + ", level=" + level + ")");
		int n = 1 + level;
		if (param.getLayout().getPartTitle()) {
			n++;
		}
		StringBuilder b = new StringBuilder();
		if (param.getLayout().getChapterTitle()) {
			b.append(tocTo(n, chapter.getIdent(), chapterGetTitle(chapter)));
		}
		for (Scene scene : mainFrame.project.scenes.findBy(chapter)) {
			b.append(tocScene(scene, n));
		}
		return b.toString();
	}

	/**
	 * get the Scene table of content
	 *
	 * @param scene
	 * @param level
	 * @return
	 */
	private String tocScene(Scene scene, int level) {
		//LOG.printInfos(TT + ".tocScene(scene=" + LOG.printInfos(scene) + ", level=" + level + ")");
		if (!param.getHtmlMultiScene() && !param.getLayout().getSceneTitle()) {
			return "";
		}
		//if (level != 0 && level < 3) {
		//	return "";
		//}
		int n = 2;
		if (param.getLayout().getPartTitle()) {
			n++;
		}
		if (param.getLayout().getChapterTitle()) {
			n++;
		}
		if (sceneIsOK(scene)) {
			return tocTo(n, scene.getIdent(), scene.getName());
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
		/*App.printInfos(TT+".linkToScene(scene="+App.traceEntity(scene)+
			", img="+(img==null?"null":img)+")");*/
		if (scene == null) {
			return "scene is null";
		}
		String linktext = scene.getName();
		if (image != null && !image.isEmpty()) {
			linktext = Html.intoIMG(imageDir + image + ".png", linktext, linktext);
		}
		if (isOpened && param.isMulti()) {
			return String.format("<a href=\"%s/%s\">%s</a>",
			   filenameOfScene(scene), scene.getIdent(), linktext);
		}
		return String.format("<a href=\"#%s\">%s</a>", scene.getIdent(), linktext);
	}

	/**
	 * get a link to an Endnote
	 *
	 * @param n
	 * @param img
	 * @return
	 */
	private String endnotesLinkTo(int n, String... img) {
		//App.printInfos(TT+".linkToEndnotes()");
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
			buffer.append(Html.intoTag("div", ExportBook.getBlurb(book), "style=\"font-style:italic;\""));
		}
		buffer.append(ExportBook.getDedication(book, 50));
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

	/* ******** get a Chapter ****** */
	/**
	 * get the begining Chapter context, open the file if it's needed
	 *
	 * @param chapter
	 */
	private void chapterGetBegin(Chapter chapter) {
		if (param.getHtmlMultiChapter()) {
			openFile(filenameOfChapter(chapter).replace(Html.EXT, ""), false);
		}
		chapterCur = chapter;
		chapterPrior = chapterFindPrior(chapter);
		chapterNext = chapterFindNext(chapter);
		if (buffer == null) {
			buffer = new StringBuilder();
		}
	}

	/**
	 * get a Chapter in HTML code
	 *
	 * @param chapter
	 */
	private void chapterGetContent(Chapter chapter) {
		//App.printInfos(TT + ".chapterGet(chapter=" + App.traceEntity(chapter) + ")");
		chapterGetBegin(chapter);
		int lev = 1;
		if (param.getLayout().getPartTitle()) {
			lev++;
		}
		if (!buffer.toString().isEmpty() && param.getHtmlMultiChapter()) {
			openFile(book.getTitle() + "-C" + chapter.getIdent(), false);
		}
		if (param.getLayout().getChapterTitle()) {
			buffer.append(titleOf(lev, chapter.getIdent(), chapterGetTitle(chapter)));
		}
		buffer.append(chapterGetDidascalie(chapter));
		buffer.append(chapterGetDescription(chapter));
		for (Scene scene : mainFrame.project.scenes.findBy(chapter)) {
			if (sceneIsOK(scene)) {
				sceneGetContent(scene);
			}
		}
		chapterGetEnd();
	}

	/**
	 * do the ending action for the current Chapter
	 */
	private void chapterGetEnd() {
		if (param.getHtmlMultiChapter() && buffer.length() > 0) {
			buffer.append(Html.P_E);
			buffer.append(navbar());
			writeText(buffer.toString());
			closeFile(SILENT);
			buffer = new StringBuilder();
		}
	}

	/**
	 * get the Chapter description
	 *
	 * @param chapter
	 * @return
	 */
	private String chapterGetDescription(Chapter chapter) {
		//App.printInfos(TT + ".chapterGetDescription(chapter=" + App.traceEntity(chapter) + ")");
		if (param.getLayout().getChapterDescription() && !chapter.getDescription().isEmpty()) {
			return (Html.intoP(Html.intoI(chapter.getDescription()), "margin-left: 3.5cm;text-align: right;"));
		}
		return "";
	}

	/**
	 * get the Chapter title, if needed, with number
	 *
	 * @param chapter
	 * @return
	 */
	private String chapterGetTitle(Chapter chapter) {
		StringBuilder b = new StringBuilder();
		if (param.getLayout().getChapterTitle()) {
			if (param.getLayout().getChapterNumber()) {
				if (param.getLayout().getChapterRoman()) {
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
	private void sceneGetContent(Scene scene) {
		//LOG.printInfos(TT + ".sceneGetContent(scene=" + LOG.printInfos(scene) + ")");
		sceneBegin(scene);
		if (param.getLayout().getSceneTitle() && !getTitle(scene.getName()).isEmpty()) {
			int n = 2;
			if (param.getLayout().getPartTitle()) {
				n++;
			}
			if (param.getLayout().getChapterTitle()) {
				n++;
			}
			buffer.append(titleOf(n, scene.getIdent(), getTitle(scene.getName())));
		}
		if (param.getLayout().getSceneDidascalie()) {
			buffer.append(sceneGetDidascalie(param, scene));
		}
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
			md.setText(scene.getTextToHtml(param.getHtmlMultiScene()));
			x = md.getHtmlBody();
		} else {
			x = Html.emTag(x, param.getHighlight());
		}
		if (param.isMulti() && x.contains("<a href=\"#S")) {
			x = linkReplaceInternal(x);
		}
		x = x.replace("<p align=\"center\">", "<p style=\"text-align:center;\">");
		buffer.append(x).append("\n");
		if (review) {
			buffer.append(Review.reviewsToHtml(mainFrame, scene));
		}
		if (bookNeedToc() && !isOpened && tocLink) {
			buffer.append(tocGetLink());
		}
		if (param.getLayout().getSceneSeparator() && bSeparator) {
			buffer.append(Html.P_CENTER).append(sceneSeparator).append("</p>\n");
		}
		sceneEnd();
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
	private void sceneBegin(Scene scene) {
		if (param.getHtmlMultiScene()) {
			openFile(filenameOfScene(scene).replace(Html.EXT, ""), false);
		}
		scenePrior = sceneFindPrior(scene);
		sceneNext = sceneFindNext(scene);
		if (buffer == null) {
			buffer = new StringBuilder();
		}
	}

	/**
	 * do the endind action for the current Scene
	 */
	private void sceneEnd() {
		if (param.getHtmlMultiScene() && buffer.length() > 0) {
			buffer.append(navbar());
			writeText(buffer.toString());
			closeFile(SILENT);
			buffer = new StringBuilder();
		}
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
			img = imageDir + "summary.png";
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
		if (param.getHtmlMultiScene()) {
			writeText(buf.toString());
			closeFile(SILENT);
			buf = new StringBuilder();
		}
		return (buf.toString());
	}

	/**
	 * get the didascalie of a Scene
	 *
	 * @param param
	 * @param scene
	 * @return a String reprenting the HTML code
	 */
	public static String sceneGetDidascalie(BookParamExport param, Scene scene) {
		//App.printInfos(TT+".getDidascalie("+scene.titleGet()+")");
		String rc = "";
		if (param.getLayout().getSceneDidascalie()) {
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
		//LOG.printInfos(TT+".linkReplaceInternal(x="+x+")");
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
					if (param.getHtmlMultiChapter()) {
						if (!sc.hasChapter()) {
							break;
						}
						r = String.format("%s-%s%s#%s",
						   book.getTitle(),
						   sc.getChapter().getIdent(),
						   Html.EXT,
						   sc.getIdent());
					} else if (param.getHtmlMultiScene()) {
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
		String link = Html.intoIMG("Images/" + NAV.SUMMARY + ".png", toc, toc);
		if (param.getHtmlNavImage()) {
			link = Html.intoA("", "index.html", link);
		} else {
			link = Html.intoA("", "index.html", toc);
		}
		b.append(Html.intoTD(link));

		if (param.getHtmlMultiChapter()) {
			b.append(Html.intoTD(navbarChapter(), "text-align:right"));
		}
		if (param.getHtmlMultiScene()) {
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
	 * get a link to a Chapter
	 *
	 * @param chapter
	 * @param img
	 * @return
	 */
	private String navbarChapterLinkto(Chapter chapter, String img) {
		/*LOG.printInfos(TT+".linkToChapter("+
			"chapter="+LOG.printInfos(chapter)+
			", img="+(img==null?"null":img)+")");*/
		if (chapter == null) {
			return Html.intoIMG("Images/" + img + ".png", "", "");
		}
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
				link = Html.intoIMG(imageDir + NAV.AR_LEFT_GRAYED + ".png", "", "");
			}
		}
		buf.append(link);

		// to next scene
		if (sceneNext != null) {
			link = sceneLinkTo(sceneNext, NAV.AR_RIGHT.toString());
		} else {
			link = Html.intoIMG(imageDir + NAV.AR_RIGHT_GRAYED + ".png", "", "");
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
		if (!param.getHtmlMultiChapter() && !param.getHtmlMultiScene()) {
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
	private void partGetBegin(Part part) {
		if (param.isMulti()) {
			//openFile(partGetFilename(part).replace(Html.EXT, ""), true);
			//buffer = new StringBuilder();
		}
	}

	/**
	 * get the content of a given Part
	 *
	 * @param part
	 */
	private void partGetContent(Part part) {
		partGetBegin(part);
		buffer.append(partGetTitle(part));
		for (Chapter chapter : chapters) {
			if (chapter.hasPart() && chapter.getPart().equals(part)) {
				chapterGetContent(chapter);
			}
		}
		partGetEnd();
	}

	/**
	 * get the ending of a given Part
	 */
	private void partGetEnd() {
		if (param.isMulti()) {
			writeFileEnd();
		}
	}

	/**
	 * get the file name for a Part
	 *
	 * @param part
	 * @return
	 */
	private String partGetFilename(Part part) {
		return (String.format("%s-P%s.html", book.getTitle(), part.getIdent()));
	}

	/**
	 * get title of a Part
	 *
	 * @param part
	 * @return
	 */
	public String partGetTitle(Part part) {
		//App.printInfos(TT+".getPartTitle("+part.getName()+")");
		String buf = "";
		if (param.getLayout().getPartTitle()) {
			buf = Html.intoH(1, part.getName());
		}
		return buf;
	}

	/**
	 * get the Chapter didascalie (dates and locations)
	 *
	 * @param chapter
	 * @return
	 */
	private String chapterGetDidascalie(Chapter chapter) {
		//App.printInfos(TT + ".chapterGetDateLocation(chapter=" + App.traceEntity(chapter) + ")");
		StringBuilder b = new StringBuilder();
		if (param.getLayout().getChapterDateLocation()) {
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
	 * copy an image from resources to the export directory
	 *
	 * @param img: image source name without extent
	 * @param dir: the export directory
	 */
	private void copyImage(String img, String dir) {
		File fdir = new File(dir);
		fdir.mkdirs();
		File out = new File(dir + File.separator + img + ".png");
		if (out.exists()) {
			return;

		}
		Class<?> x = IconUtil.class;
		String path = "png/" + img.toLowerCase().replace(".", "/") + ".png";
		URL imgURL = x.getResource(path);
		InputStream resource = x.getResourceAsStream(path);
		if (resource == null) {
			LOG.err(TT + ".copyImage(img=" + img + ", dir=" + dir + ")" + "unable to found " + path);
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

	private void writeFileEnd() {
		buffer.append(Html.BODY_E);
		buffer.append(Html.HTML_E);
		writeText(buffer.toString());
		// todo copy any images
		// from source directory
		// to Images directory
		// and convert if it's PNG
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
				// todo replace any name with PNG by JPEG
				imageList.add(n.attr("src"));
				n.attr("src", "Images/" + src.getName());
				n.attr("alt", src.getName().replace('.', '-'));
			}
		}
		document.outputSettings().syntax(api.jsoup.nodes.Document.OutputSettings.Syntax.xml);
		return document.body().html();
	}

}
