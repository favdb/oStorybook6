/*
 * Copyright (C) 2024 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.exim.exporter;

import i18n.I18N;
import java.io.File;
import java.util.List;
import storybook.db.book.Book;
import storybook.db.book.BookParamLayout;
import storybook.db.book.BookParamWeb;
import storybook.db.chapter.Chapter;
import storybook.db.endnote.Endnote;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.project.Project;
import storybook.tools.html.Html;
import storybook.ui.frames.main.MainFrame;

/**
 * class for exporting a book to Html
 *
 * @author favdb
 */
public class XportBook2Html extends AbstractExport {

	private static final String TT = "XportBook2Html.";
	public BookParamLayout layout;
	public BookParamWeb web;
	public String imageDir, sceneSeparator;
	public Chapter chapterFirst, chapterLast, chapterPrior, chapterNext;
	public List<Part> parts;
	public List<Chapter> chapters;
	public List<Scene> scenes;
	public List<Endnote> endnotes;
	public boolean withExternal;
	private XportPart xportPart;
	private XportChapter xportChapter;
	private XportScene xportScene;
	private Project project;

	public int getNbChapters(Chapter chapter) {
		return project.chapters.find(chapter.getPart()).size();
	}

	public int getNbParts() {
		return project.parts.getCount();
	}

	public int getNbScenes(Scene scene) {
		return project.scenes.find(scene.getChapter()).size();
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

	public XportBook2Html(MainFrame mainFrame) {
		super(mainFrame, "html");
		init();
	}

	@SuppressWarnings("unchecked")
	private void init() {
		//LOG.trace(TT + "init()");
		project = mainFrame.project;
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
		xportPart = new XportPart(this);
		xportChapter = new XportChapter(this);
		xportScene = new XportScene(this);
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

	public static boolean toFile(MainFrame m) {
		boolean rc = true;
		XportBook2Html exp = new XportBook2Html(m);
		return rc;
	}

	public static String toPanel(MainFrame m) {
		String b = "";
		XportBook2Html exp = new XportBook2Html(m);
		return b;
	}

	public static boolean toPrint(MainFrame m) {
		boolean rc = true;
		XportBook2Html exp = new XportBook2Html(m);
		return rc;
	}

	/**
	 * get a link to an Endnote
	 *
	 * @param n
	 * @param img
	 * @return
	 */
	public String endnotesLinkTo(int n, String... img) {
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
			href = "endnote.html";
		}
		return Html.intoA("", href + number, text);
	}

	/**
	 * get the Chapter file name
	 *
	 * @param chapter
	 * @return
	 */
	public String filenameOfChapter(Chapter chapter) {
		return (String.format("%s-%s.html", book.getTitle(), chapter.getIdent()));
	}

	/**
	 * get the Scene file name
	 *
	 * @param scene
	 * @return
	 */
	public String filenameOfScene(Scene scene) {
		if (!param.isMultiChapter() && !param.isMultiScene()) {
			return ("");
		}
		return Scene.getFilename(scene, book.getTitle());
	}

}
