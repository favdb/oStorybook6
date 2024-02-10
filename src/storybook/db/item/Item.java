/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.item;

import org.w3c.dom.Node;
import storybook.db.book.Book;
import storybook.db.tag.AbsTag;
import storybook.tools.xml.XmlKey.XK;
import storybook.tools.xml.XmlUtil;

public class Item extends AbsTag {

	public Item() {
		super(Book.TYPE.ITEM, "111");
	}

	public static Item fromXml(Node node) {
		Item p = new Item();
		fromXmlBeg(node, p);
		p.setCategory(XmlUtil.getString(node, XK.CATEGORY));
		fromXmlEnd(node, p);
		return (p);
	}

}
