/**
 * Copyright 2020 by FaVdB
 *
 * This file is part of oStorybook.
 *
 *  oStorybook is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License.
 *
 *  oStorybook is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with oStorybook. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package storybook.db.book;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * class for Book parameters
 *
 * @author favdb
 */

public class BookParam {

	private static final String TT = "BookParam.";

	private BookParamBackup paramBackup;
	//TODO calendar
	private BookParamCalendar paramCalendar;
	private BookParamEditor paramEditor;
	private BookParamExport paramExport;
	private BookParamImport paramImport;
	private BookParamLayout paramLayout;
	public final Book book;
	private Element elInfo;
	private BookParamWeb paramWeb;

	public BookParam(Book book) {
		this.book = book;
		if (book.project != null && book.project.rootNode != null) {
			NodeList nodes = book.project.rootNode.getElementsByTagName("info");
			if (nodes.getLength() > 0) {
				elInfo = (Element) nodes.item(0);
			} else {
				elInfo = null;
			}
		}
		init();
	}

	public BookParamBackup getParamBackup() {
		return paramBackup;
	}

	public void setParamBackup(BookParamBackup b) {
		paramBackup = b;
	}

	public BookParamCalendar getParamCalendar() {
		return (paramCalendar);
	}

	public void setParamCalendar(BookParamCalendar b) {
		paramCalendar = b;
	}

	public BookParamEditor getParamEditor() {
		return paramEditor;
	}

	public void setParamEditor(BookParamEditor b) {
		paramEditor = b;
	}

	public BookParamExport getParamExport() {
		return paramExport;
	}

	public void setParamExport(BookParamExport b) {
		paramExport = b;
	}

	public BookParamImport getParamImport() {
		return (paramImport);
	}

	public void setParamImport(BookParamImport b) {
		paramImport = b;
	}

	public BookParamLayout getParamLayout() {
		return paramLayout;
	}

	public void setParamLayout(BookParamLayout b) {
		paramLayout = b;
	}

	public void init() {
		//LOG.trace(TT + "init()");
		paramBackup = new BookParamBackup(this);
		paramEditor = new BookParamEditor(this);
		paramExport = new BookParamExport(this);
		paramImport = new BookParamImport(this);
		paramLayout = new BookParamLayout(this);
		paramWeb = new BookParamWeb(this);
		//TODO calendar
		//paramCalendar.init();
	}

	public String toXml() {
		//LOG.trace(TT + "toXml()");
		StringBuilder b = new StringBuilder("    <param>\n");
		b.append(paramBackup.toXml());
		b.append(paramEditor.toXml());
		b.append(paramExport.toXml());
		b.append(paramImport.toXml());
		b.append(paramLayout.toXml());
		b.append(paramWeb.toXml());
		//TODO calendar
		//b.append(paramCalendar.toXml());
		b.append("    </param>\n");
		return b.toString();
	}

	public BookParamWeb getParamWeb() {
		return paramWeb;
	}

	public void setParamWeb(BookParamWeb web) {
		this.paramWeb = web;
	}

}
