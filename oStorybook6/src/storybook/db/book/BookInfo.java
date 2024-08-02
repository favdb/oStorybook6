/*
Copyright 2018, FaVdB
Modifications :

This file is part of oStorybook.

    oStorybook is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License.

    oStorybook is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with oStorybook.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.book;

import i18n.I18N;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import storybook.db.SbDate;
import storybook.db.book.Book.INFO;
import storybook.db.status.Status;
import storybook.tools.StringUtil;
import storybook.tools.xml.XmlUtil;
import static storybook.tools.xml.XmlUtil.*;

/**
 *
 * @author FaVdB
 */
public class BookInfo {

	private static final String TT = "BookInfo.";

	private Book book;
	public String creation = "",
		maj = "",
		title = "",
		subtitle = "",
		author = "",
		copyright = "",
		UUID = "",
		ISBN = "",
		language = "",
		blurb = "",
		notes = "",
		dedication = "",
		assistant = "",
		assistantValue = "";
	private boolean scenario = false,
		markdown = false,
		calendarUse = false,
		review = false;
	private Status status;
	private Integer nature = 0;
	private Element elInfo;
	private String dbVersion;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public BookInfo(Book book) {
		this.book = book;
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public BookInfo(Book book, String name) {
		this(book);
		titleSet(name);
	}

	public String getString(Element el, INFO key) {
		return el.getAttribute(key.toString());
	}

	public boolean getBoolean(Element el, INFO key) {
		return el.getAttribute(key.toString()).equals("1");
	}

	public Integer getInteger(Element el, INFO key) {
		String r = el.getAttribute(key.toString());
		if (StringUtil.isNumeric(r)) {
			return Integer.valueOf(r);
		}
		return -1;
	}

	public void setString(INFO key, String value) {
		elInfo.setAttribute(key.toString(), value);
	}

	public void setBoolean(INFO key, boolean value) {
		elInfo.setAttribute(key.toString(), (value ? "1" : "0"));
	}

	public void setInteger(INFO key, Integer value) {
		elInfo.setAttribute(key.toString(), value.toString());
	}

	public void init() {
		//LOG.trace(TT + "init() " + (book.project.rootNode == null ? "rootNode null" : book.project.getFilename()));
		NodeList nodes = book.project.rootNode.getElementsByTagName("info");
		if (nodes.getLength() > 0) {
			elInfo = (Element) nodes.item(0);
		} else {
			elInfo = null;
		}
		if (elInfo != null) {
			creationSet(getString(elInfo, INFO.CREATION));
			majSet(getString(elInfo, INFO.UPDATE));
			titleSet(getString(elInfo, INFO.TITLE));
			subtitleSet(getString(elInfo, INFO.SUBTITLE));
			authorSet(getString(elInfo, INFO.AUTHOR));
			copyrightSet(getString(elInfo, INFO.COPYRIGHT));
			isbnSet(getString(elInfo, INFO.ISBN));
			uuidSet(getString(elInfo, INFO.UUID));
			languageSet(getString(elInfo, INFO.LANG));
			natureSet(getInteger(elInfo, INFO.NATURE));
			reviewSet(getBoolean(elInfo, INFO.REVIEW));
			scenarioSet(getBoolean(elInfo, INFO.SCENARIO));
			markdownSet(getBoolean(elInfo, INFO.MARKDOWN));
			blurbSet(Book.getText(elInfo, INFO.BLURB));
			notesSet(Book.getText(elInfo, INFO.NOTES));
			dedicationSet(Book.getText(elInfo, INFO.DEDICATION));
			assistantSet(Book.getText(elInfo, INFO.ASSISTANT));
		}
	}

	public String toXml() {
		//LOG.trace(TT + ".toXml()");
		StringBuilder b = new StringBuilder("    <info ");
		b.append(setAttribute(0, INFO.CREATION.toString(), creationGet()));
		b.append(setAttribute(0, INFO.UPDATE.toString(), majGet()));
		b.append(setAttribute(8, INFO.UUID.toString(), uuidGet()));
		b.append(setAttribute(8, INFO.ISBN.toString(), isbnGet()));
		b.append(setAttribute(8, INFO.LANG.toString(), languageGet()));
		b.append(setAttribute(8, INFO.TITLE.toString(), titleGet()));
		b.append(setAttribute(8, INFO.SUBTITLE.toString(), subtitleGet()));
		b.append(setAttribute(8, INFO.AUTHOR.toString(), authorGet()));
		b.append(setAttribute(8, INFO.COPYRIGHT.toString(), copyrightGet()));
		b.append(setAttribute(8, INFO.REVIEW.toString(), reviewGet() ? "1" : "0"));
		b.append(setAttribute(0, INFO.SCENARIO.toString(), scenarioGet() ? "1" : "0"));
		b.append(setAttribute(0, INFO.MARKDOWN.toString(), markdownGet() ? "1" : "0"));
		b.append(setAttribute(0, INFO.NATURE.toString(), natureGet() + ""));
		b.append(">\n");
		b.append(XmlUtil.setChild(2, INFO.BLURB.toString(), blurbGet(), false));
		b.append(XmlUtil.setChild(2, INFO.NOTES.toString(), notesGet(), false));
		b.append(XmlUtil.setChild(2, INFO.DEDICATION.toString(), dedicationGet(), true));
		b.append(Book.setChildText(INFO.ASSISTANT, assistantGet()));
		b.append("    </info>\n");
		return b.toString();
	}

	public void setCreation() {
		creation = SbDate.getToDay().getDateTimeToString();
	}

	public void creationSet(String c) {
		if (c.isEmpty()) {
			setCreation();
		} else {
			creation = c;
		}
	}

	public String creationGet() {
		return (creation);
	}

	public Date creationDateGet() {
		return oneDate(creation);
	}

	public void majSet() {
		maj = SbDate.getToDay().getDateTimeToString();
	}

	public void majSet(String c) {
		if (c.isEmpty()) {
			BookInfo.this.majSet();
		}
		maj = c;
	}

	public String majGet() {
		return (maj);
	}

	public Date majDateGet() {
		return oneDate(maj);
	}

	public Date oneDate(String str) {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
		String dateStr = str;
		if (str == null || str.isEmpty()) {
			dateStr = format.format(new Date());
		}
		Date date = new Date();
		try {
			date = format.parse(dateStr);
		} catch (ParseException e) {
		}
		return date;
	}

	public void titleSet(String s) {
		title = s;
	}

	public String titleGet() {
		return title;
	}

	public void subtitleSet(String s) {
		subtitle = s;
	}

	public String subtitleGet() {
		return (subtitle == null ? "" : subtitle);
	}

	public void authorSet(String s) {
		author = s;
	}

	public String authorGet() {
		return (author == null ? "" : author);
	}

	public void copyrightSet(String x) {
		copyright = x;
	}

	public String copyrightGet() {
		return (copyright == null ? "" : copyright);
	}

	public void blurbSet(String s) {
		blurb = s;
	}

	public String blurbGet() {
		return blurb;
	}

	public void scenarioSet(boolean val) {
		scenario = val;
	}

	public boolean scenarioGet() {
		return scenario;
	}

	public void isbnSet(String s) {
		ISBN = s;
	}

	public String isbnGet() {
		return ISBN;
	}

	public void uuidSet(String s) {
		UUID = s;
	}

	public String uuidGet() {
		return UUID;
	}

	public void languageSet(String s) {
		language = s;
	}

	public String languageGet() {
		String x = language;
		if (x.isEmpty()) {
			x = I18N.getCountryLanguage(Locale.getDefault()).substring(0, 2);
		}
		return (x);
	}

	public void markdownSet(boolean x) {
		markdown = x;
	}

	public boolean markdownGet() {
		return (markdown);
	}

	public boolean isMarkdown() {
		return (markdown);
	}

	public void reviewSet(boolean value) {
		review = value;
	}

	public boolean reviewGet() {
		return review;
	}

	public void notesSet(String s) {
		notes = s;
	}

	public String notesGet() {
		return notes;
	}

	public void dedicationSet(String s) {
		dedication = s;
	}

	public String dedicationGet() {
		return dedication;
	}

	public void assistantSet(String s) {
		assistant = s;
	}

	public String assistantGet() {
		return assistant;
	}

	public String dbVersionGet() {
		return dbVersion;
	}

	public void dbVersionSet(String value) {
		this.dbVersion = value;
	}

	/**
	 * get nature of project:<br>
	 * <ul>
	 * <li>0=Other or undefined:</li>
	 * <li>1=Long Novel (more than 90000 words):90000</li>
	 * <li>2=Novel (more than 40000 words):40000</li>
	 * <li>3=Short Novel (more than 17500 words):17500</li>
	 * <li>4=Novela (more than 7500 words):7500</li>
	 * <li>5=Short Novela (less than 7500 words):</li>
	 * </ul>
	 *
	 * @return
	 */
	public int natureGet() {
		return this.nature;
	}

	/**
	 * set the project nature
	 *
	 * @param nature
	 */
	public void natureSet(int nature) {
		this.nature = Book.checkInteger(nature, 0, 5);
	}

	public static String isDataOK(String titre, String soustitre, String auteur, String droits) {
		String rc = "";
		String tolong = " " + I18N.getMsg("err.value.too.long");
		if (titre.isEmpty()) {
			rc += I18N.getMsg("book.title") + " " + I18N.getMsg("error.missing") + "\n";
		} else if (titre.length() > 128) {
			rc += I18N.getColonMsg("book.title") + tolong + "\n";
		}
		if (soustitre.length() > 256) {
			rc += I18N.getColonMsg("book.subtitle") + tolong + "\n";
		}
		if (auteur.length() > 256) {
			rc += I18N.getColonMsg("book.author") + tolong + "\n";
		}
		if (droits.length() > 256) {
			rc += I18N.getColonMsg("book.copyright") + tolong + "\n";
		}
		return (rc);
	}

}
