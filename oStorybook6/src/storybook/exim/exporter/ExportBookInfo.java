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
package storybook.exim.exporter;

import i18n.I18N;
import storybook.Const;
import storybook.db.book.Book;
import storybook.tools.html.Html;

/**
 * common functions for exporting a Book to HTML
 *
 * @author favdb
 */
public class ExportBookInfo {

	private static final String TT = "ExportBook.";

	private ExportBookInfo() {
		//empty
	}

	/**
	 * get the title page with common informations: title, author, copyright, ISBN and UUID, by
	 *
	 * @param book: the book parameters
	 * @param isbn: true for including ISBN and/or UUID
	 *
	 * @return
	 */
	public static String getTitle(Book book, boolean isbn) {
		//LOG.trace(TT + "getTitle(book)");
		StringBuilder t = new StringBuilder();
		t.append(Html.P_B).append(Html.BR).append(Html.BR).append(Html.P_E);
		t.append(Html.intoH(1, book.getTitle(), "text-align:center;"));
		t.append(Html.P_CENTER);
		if (!book.getSubtitle().isEmpty()) {
			t.append(Html.intoB(book.getSubtitle())).append(Html.BR).append(Html.BR);
		}
		t.append(I18N.getMsg("author_s")).append(" ").append(book.getAuthor()).append(Html.BR);
		if (!book.getCopyright().isEmpty()) {
			t.append(Html.intoI(book.getCopyright())).append(Html.BR).append(Html.BR);
		}
		t.append(Html.P_E);
		if (isbn) {
			if (!book.getISBN().isEmpty()) {
				t.append(Html.intoPcenter("ISBN: " + book.getISBN()));
			} else {
				t.append(Html.P_EMPTY);
			}
			if (!book.getUUID().isEmpty()) {
				t.append(Html.intoPcenter("UUID: " + book.getUUID()));
			} else {
				t.append(Html.P_EMPTY);
			}
		}
		t.append(Html.P_CENTER);
		t.append(
				Html.intoSmall(
						Html.intoI(I18N.getMsg("export.by") + " " + Const.getFullName())
				)
		);
		t.append(Html.P_E);
		return t.toString();
	}

	public static String getBlurb(Book book) {
		//LOG.trace(TT + "getBlurb(book)");
		StringBuilder b = new StringBuilder();
		b.append(Html.P_EMPTY).append(Html.intoP(book.getBlurb()));
		return b.toString();
	}

	public static String getDedication(Book book, Integer lmargin) {
		//LOG.trace(TT + "getDedication(book, lmargin=" + lmargin.toString() + ")");
		StringBuilder b = new StringBuilder();
		if (!book.getDedication().isEmpty()) {
			b.append(Html.P_EMPTY);
			b.append("<div style=\"")
					.append("padding-left: ").append(lmargin.toString()).append("%;")
					.append("text-align: justify;")
					.append("font-style: italic;")
					.append("\">");
			b.append(book.getDedication());
			b.append("</div>");
			b.append(Html.P_EMPTY);
		}
		return b.toString();
	}

}
