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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.book;

import org.w3c.dom.Element;

/**
 *
 * @author favdb
 */
public abstract class BookParamAbstract {

	public String type;
	public BookParam param;
	public Element node;

	public BookParamAbstract(BookParam param, String type) {
		this.param = param;
		this.type = type;
	}

	abstract protected void init();

	public abstract String toXml();

	public int hash() {
		return toString().hashCode();
	}

	public void refresh() {
		init();
	}

	public Element getNodeElement(String name) {
		if (param == null
		   || param.book == null
		   || param.book.project == null
		   || param.book.project.rootNode == null) {
			return null;
		}
		Element nx = (Element) param.book.project.rootNode.getElementsByTagName("param").item(0);
		return (Element) nx.getElementsByTagName(name).item(0);
	}

}
