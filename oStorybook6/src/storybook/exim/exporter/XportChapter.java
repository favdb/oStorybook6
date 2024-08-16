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

import storybook.db.abs.AbstractEntity;
import storybook.db.chapter.Chapter;
import storybook.exim.exporter.XportBook2Html.NAV;
import storybook.tools.StringUtil;
import storybook.tools.html.Html;

/**
 *
 * @author favdb
 */
public class XportChapter extends XportAbs {

	public XportChapter(XportBook2Html m) {
		super(m);
	}

	@Override
	public void set(AbstractEntity chapter) {
		super.set(chapter);
		lev = 2;
	}

	@Override
	public String begin(AbstractEntity chapter) {
		set(chapter);
		if (xport.layout.getChapterTitle()
			&& xport.getNbChapters((Chapter) chapter) > 1) {
			return getTitle();
		}
		return "";
	}

	@Override
	public String content() {
		// empty String
		return "";
	}

	@Override
	public String end() {
		// nav bar if miltiChapter and navbar
		Chapter chapter = (Chapter) entity;
		StringBuilder buf = new StringBuilder();
		if (!xport.param.getHtmlNav()) {
			return "";
		}
		buf.append("<p style=\"text-align: right;\">");
		String link = navbarLinkto(null, NAV.NAV_FIRST_GRAYED.toString());
		// first chapter
		if (!chapter.equals(xport.chapterFirst)) {
			link = navbarLinkto(xport.chapterFirst, NAV.NAV_FIRST.toString());
		}
		buf.append(link);

		// to previous chapter
		if (xport.chapterPrior != null && !((Chapter) entity).equals(xport.chapterPrior)) {
			link = navbarLinkto(xport.chapterPrior, NAV.NAV_BACKWARD.toString());
		} else {
			link = navbarLinkto(null, NAV.NAV_BACKWARD_GRAYED.toString());
		}
		buf.append(link);
		// to next chapter
		if (xport.chapterNext != null && !xport.chapterNext.equals(xport.chapterLast)) {
			link = navbarLinkto(xport.chapterNext, NAV.NAV_FORWARD.toString());
		} else if (chapter.equals(xport.chapterLast)) {
			link = xport.endnotesLinkTo(0, NAV.NAV_FORWARD.toString());
		} else {
			link = navbarLinkto(null, NAV.NAV_FORWARD_GRAYED.toString());
		}
		buf.append(link);
		// to last chapter
		if (!chapter.equals(xport.chapterLast)) {
			link = navbarLinkto(xport.chapterLast, NAV.NAV_LAST.toString());
		} else {
			link = navbarLinkto(null, NAV.NAV_LAST_GRAYED.toString());
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
	private String navbarLinkto(Chapter chapter, String img) {
		/*LOG.trace(TT + "navbarChapterLinkto("+
			"chapter="+LOG.printInfos(chapter)+
			", img="+(img==null?"null":img)+")");*/
		if (chapter == null) {
			return Html.intoIMG("Images/" + img + ".png", "", "");
		}
		String title = "";
		if (xport.layout.getChapterNumber()) {
			if (xport.layout.getChapterRoman()) {
				title = (String) StringUtil.intToRoman(chapter.getChapterno()) + " ";
			} else {
				title = chapter.getChapterno().toString() + " ";
			}
		}
		if (xport.layout.getChapterTitle()) {
			title += chapter.getName();
		}
		if (img != null && !img.isEmpty() && xport.param.getHtmlNavImage()) {
			title = Html.intoIMG("Images/" + img + ".png", title, title);
		}
		if (xport.isOpened && xport.param.isMulti()) {
			return (String.format("<a href=\"%s\">%s</a>", xport.filenameOfChapter(chapter), title));
		}
		return String.format("<a href=\"#C%s\">%s</a>", chapter.getIdent(), title);
	}

}
