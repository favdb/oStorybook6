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
package storybook.review;

import i18n.I18N;
import java.util.Date;
import java.util.List;
import resources.MainResources;
import storybook.db.book.Book;
import storybook.db.chapter.Chapter;
import storybook.db.endnote.Endnote;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.db.scene.Scenes;
import storybook.exim.exporter.AbstractExport;
import storybook.tools.DateUtil;
import storybook.tools.file.IOUtil;
import storybook.tools.html.Html;

/**
 *
 * @author favdb
 */
public class ExportReview extends AbstractExport {

	private static final String TT = "ExportReview";

	private static final String EXPORT_TITLE = "Reviews Export",
	   MSG_SCENE_MISSING = I18N.getMsg("comment.missing_scene"),
	   MSG_CHAPTER_MISSING = I18N.getMsg("comment.missing_chapter"),
	   MSG_PART_MISSING = I18N.getMsg("comment.missing_part");
	private final ReviewPanel reviewsPanel;
	private boolean titlePart = false;
	private Part curPart = null;
	private Chapter curChapter = null;
	private Scene curScene = null;

	public static boolean exec(ReviewPanel commentsPanel, String format) {
		//LOG.printInfos(TT + ".exec(commentsPanel, format='" + format + "')");
		ExportReview exp = new ExportReview(commentsPanel, format);
		return exp.exec();
	}
	private boolean titleChapter;

	public ExportReview(ReviewPanel reviewPanel, String format) {
		super(reviewPanel.getMainFrame(), format);
		this.reviewsPanel = reviewPanel;
	}

	private boolean exec() {
		//LOG.printInfos(TT + ".exec()");
		boolean rc = false;
		if (reviewsPanel != null) {
			titlePart = mainFrame.project.getList(Book.TYPE.PART).size() > 1;
			titleChapter = mainFrame.project.getList(Book.TYPE.CHAPTER).size() > 1;
			String fname = mainFrame.getProject().getName() + "_" + "Review";
			fname = IOUtil.filenameCleanup(fname);
			if (openFile(fname, true)) {
				writeText(writeComments());
				closeFile(true);
				rc = true;
			}
		}
		return rc;
	}

	@Override
	public boolean writeHeaderTxt() {
		//LOG.printInfos(TT + ".writeHeaderTxt()");
		StringBuilder buf = new StringBuilder();
		buf.append(I18N.getMsg("review")).append(" '")
		   .append(book.getTitle()).append("'\n");
		buf.append(I18N.getMsg("date")).append(" : ")
		   .append(DateUtil.dateToString(new Date(), true)).append("\n\n");
		return write(buf.toString());
	}

	@Override
	public boolean writeHeaderHtml() {
		//LOG.printInfos(TT + ".writeHeaderHtml()");
		StringBuilder buf = new StringBuilder();
		buf.append(Html.DOCTYPE);
		buf.append(Html.HTML_B_LANG);
		buf.append(Html.HEAD_B);
		buf.append(Html.META_UTF8);
		buf.append(Html.intoTag("title", I18N.getMsg("review") + " " + mainFrame.getBook().getTitle()));
		buf.append(Html.STYLE_B);
		buf.append(IOUtil.resourceRead("css/review.css", MainResources.class));
		buf.append("table, tr {\n"
		   + "width:100%;\n"
		   + "border: 1px solid black; \n"
		   + "border-collapse: collapse;\n"
		   + "}\n");
		buf.append(Html.STYLE_E);
		buf.append(Html.HEAD_E);
		buf.append(Html.BODY_B);
		buf.append(Html.intoH(1, I18N.getMsg("review") + " : " + book.getTitle()));
		buf.append(Html.intoP(Html.intoI(I18N.getMsg("date") + " "
		   + DateUtil.dateToString(new Date(), true))));
		return write(buf.toString());
	}

	private String writeComments() {
		//LOG.printInfos(TT + ".writeComments()");
		StringBuilder b = new StringBuilder();
		if (param.getFormat().equals(F_HTML)) {
			b.append(Html.TABLE_B);
		}
		for (Scene scene : Scenes.find(mainFrame)) {
			int i = 1;
			List<Endnote> reviews = Review.find(mainFrame, scene);
			if (!reviews.isEmpty()) {
				b.append(getSceneTitle(scene));
			}
			for (Endnote en : reviews) {
				switch (param.getFormat()) {
					case F_HTML:
						b.append(Html.intoTR(writeComment(i++, en)));
						break;
					case F_TXT:
						b.append(writeComment(i++, en));
						break;
					default:
						b.append(writeComment(i++, en));
						break;
				}
			}
		}
		if (param.getFormat().equals(F_HTML)) {
			b.append(Html.TABLE_E);
		}
		return b.toString();
	}

	private String writeComment(Integer i, Endnote en) {
		//LOG.printInfos(TT + ".writeComment(en=" + LOG.printInfos(en) + ") for " + param.getFormat());
		StringBuilder b = new StringBuilder();
		switch (param.getFormat()) {
			case F_HTML:
				b.append(Html.intoTD("[" + i.toString() + "] ", "style=\"vertical-align:top;\""))
				   .append(Html.intoTD(en.getNotes().replace("<p> in </p>", "")));
				break;
			case F_TXT:
				b.append("[").append(i.toString()).append("] ");
				String txt = en.getNotes().replace("</div>", "===");
				b.append(Html.htmlToText(txt, true)).append("\n---\n");
				break;
			case F_XML:
			case F_ZXML:
				b.append(en.toXml());
				break;
		}
		return b.toString();
	}

	private String getSceneTitle(Scene scene) {
		//LOG.printInfos(TT + ".getSceneTitle(en='" + LOG.printInfos(en) + "')");
		if (scene == null) {
			return MSG_SCENE_MISSING;
		}
		StringBuilder b = new StringBuilder();
		b.append(getChapterTitle(scene));
		if (curScene == null || !curScene.equals(scene)) {
			curScene = scene;
			switch (param.getFormat()) {
				case F_HTML:
					b.append(Html.intoH(3, curScene.getName()));
					break;
				case F_TXT:
					b.append("### ").append(curScene.getName()).append("\n");
					break;
				default:
					return "";
			}
		}
		return b.toString();
	}

	private String getChapterTitle(Scene scene) {
		//LOG.printInfos(TT + ".getChapterTitle(scene='" + LOG.printInfos(scene) + "')");
		if (!titleChapter) {
			return "";
		}
		Chapter chapter = scene.getChapter();
		if (chapter == null) {
			return MSG_CHAPTER_MISSING;
		}
		StringBuilder b = new StringBuilder();
		b.append(getPartTitle(chapter));
		if (curChapter == null || !curChapter.equals(chapter)) {
			curChapter = chapter;
			switch (param.getFormat()) {
				case F_HTML:
					b.append(Html.intoH(2, curChapter.getName()));
					break;
				case F_TXT:
					b.append("## ").append(curChapter.getName()).append("\n");
					break;
				default:
					return "";
			}
		}
		return b.toString();
	}

	private String getPartTitle(Chapter chapter) {
		//LOG.printInfos(TT + ".getPartTitle(chapter='" + LOG.printInfos(chapter) + "')");
		if (!titlePart) {
			return "";
		}
		Part part = chapter.getPart();
		if (!chapter.hasPart()) {
			return MSG_PART_MISSING;
		}
		StringBuilder b = new StringBuilder();
		if (curPart == null || !curPart.equals(part)) {
			curPart = part;
			switch (param.getFormat()) {
				case F_HTML:
					b.append(Html.intoH(1, curPart.getName()));
					break;
				case F_TXT:
					b.append("# ").append(curPart.getName()).append("\n");
					break;
				default:
					return "";
			}
		}
		return b.toString();
	}

}
