/*
 * Copyright (C) 2021 favdb
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
package storybook.exim.doc;

import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import storybook.tools.LOG;

/**
 *
 * @author favdb
 */
public class STYLE {

	private static final String TT = "STYLE";

	static void trace(List<STYLE> styles) {
		for (STYLE s : styles) {
			s.trace();
		}
	}

	String type = "";
	Integer level = 0;
	Boolean bold = false, italic = false, underline = false;
	String name;

	/*
	STYLE for text fragmenty with effects
	 */
	public STYLE(String name, boolean bold, boolean italic, boolean underline) {
		this.name = name;
		this.type = "para";
		this.level = 0;
		this.bold = bold;
		this.italic = italic;
		this.underline = underline;
	}

	/*
	STYLE for special paragraph like heading
	 */
	public STYLE(String name, String type, int head, String style) {
		this.name = name;
		this.type = type;
		this.level = head;
		if (style.contains("bold")) {
			bold = true;
		}
		if (style.contains("italic")) {
			italic = true;
		}
		if (style.contains("underline")) {
			underline = true;
		}
	}

	public STYLE(String s) {
		this("x", "", 0, "");
		if (s.contains("<w:b/>")) {
			bold = true;
		}
		if (s.contains("<w:i/>")) {
			italic = true;
		}
		if (s.contains("<w:u ")) {
			underline = true;
		}
	}

	public STYLE(Node item) {
		this.name = ((Element) item).getAttribute("style:name");
		Node child = item.getFirstChild();
		if (child.getNodeName().equals("style:text-properties")) {
			if (((Element) child).getAttribute("fo:font-weight").equals("bold")) {
				this.bold = true;
			}
			if (((Element) child).getAttribute("fo:font-style").equals("italic")) {
				this.italic = true;
			}
			if (!((Element) child).getAttribute("style:text-underline-style").isEmpty()) {
				this.underline = true;
			}
		}
	}

	public String getName() {
		return name;
	}

	public String toTrace() {
		return "name=" + name + ", type=" + type + ", level=" + level
				+ ", bold=" + bold.toString()
				+ ", italic=" + italic.toString()
				+ ", underline=" + underline.toString();
	}

	/**
	 * find a STYLE from his name
	 *
	 * @param name to search
	 *
	 * @return corresponding STYLE, null if not found
	 */
	public static STYLE findStyle(List<STYLE> styles, String name) {
		for (STYLE s : styles) {
			if (s.name.equals(name)) {
				return s;
			}
		}
		return null;
	}

	/**
	 * find a title STYLE with level
	 *
	 * @param name to search
	 *
	 * @return corresponding STYLE, null if not found
	 */
	public static STYLE findStyle(List<STYLE> styles, int level) {
		//App.trace(TT + ".findStyle(styles, level=" + level + ")");
		for (STYLE s : styles) {
			if (s.level == level) {
				return s;
			}
		}
		return null;
	}

	private void trace() {
		LOG.trace("style name=" + name + ", type=" + type + ", level=" + level + ", b=" + bold
				+ ", i=" + italic + ", u=" + underline);
	}

}
