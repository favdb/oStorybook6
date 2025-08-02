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
package assistant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import storybook.App;
import storybook.db.book.Book;
import storybook.tools.LOG;
import storybook.tools.xml.Xml;

/**
 * class for Assistant data
 *
 * @author favdb
 */
public class Assistant {

	private static final String TT = "Assistant.";
	private static boolean bClassic;
	private static boolean bFreytag;
	private static boolean bVogler;

	/**
	 * field types
	 */
	public enum FIELD_TYPE {
		TEXTFIELD, TEXTAREA, LISTBOX, COMBOBOX;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	/**
	 * get the field type
	 *
	 * @param value
	 * @return
	 */
	public static FIELD_TYPE getTYPE(String value) {
		for (FIELD_TYPE type : FIELD_TYPE.values()) {
			if (value.equalsIgnoreCase(type.name())) {
				return type;
			}
		}
		return FIELD_TYPE.TEXTFIELD;
	}

	/**
	 * SECTION types
	 *
	 */
	private static final String BOOK_SECTION = "book, part, chapter, scene, strand";

	public enum SECTION {
		NONE, BOOK, PERSON, LOCATION, ITEM, PLOT;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	/**
	 * get the SECTION type
	 *
	 * @param value
	 * @return
	 */
	public static SECTION getSECTION(String value) {
		//LOG.trace(TT + ".getSECTION(value=" + value + ")");
		if (BOOK_SECTION.contains(value.toLowerCase())) {
			return SECTION.BOOK;
		}
		for (SECTION section : SECTION.values()) {
			if (value.equalsIgnoreCase(section.toString())) {
				return section;
			}
		}
		LOG.err(TT + ".getSECTION(value'" + value + "') section is unknown");
		return SECTION.BOOK;
	}

	public static AssistantXml xml;

	public Assistant() {
		//empty
	}

	public Assistant(String filename) {
		init(filename);
	}

	/**
	 * initialize Assistant with fileName XML
	 *
	 * @param fileName
	 */
	public static void init(String fileName) {
		//LOG.trace(TT + ".init(fileName=" + fileName + ")");
		init(new File(fileName));
	}

	/**
	 * initialize Assistant with file XML
	 *
	 * @param xmlFile
	 */
	public static void init(File xmlFile) {
		//LOG.trace(TT + "init(xmlFile='" + (xmlFile == null ? "null" : xmlFile.getAbsolutePath()) + "')");
		xml = new AssistantXml(xmlFile);
		setClassic();
		setFreytag();
		setVogler();
	}

	public static void setClassic() {
		bClassic = !xml.findName("combobox", "three").isEmpty();
	}

	public static boolean hasClassic() {
		return bClassic;
	}

	public static void setFreytag() {
		bFreytag = !xml.findName("combobox", "freytag").isEmpty();
	}

	public static boolean hasFreytag() {
		return bFreytag;
	}

	public static void setVogler() {
		bVogler = !xml.findName("combobox", "vogler").isEmpty();
	}

	public static boolean hasVogler() {
		return bVogler;
	}

	/**
	 * get the Assistant values as a HTML String
	 *
	 * @param values
	 * @return
	 */
	public static String toHtml(String values) {
		return AssistantXml.getHtml(values);
	}

	/**
	 * convert the Assistant values in HTML to a text String (removes the HTML tags and carriage
	 * return)
	 *
	 * @param values
	 * @return
	 */
	public static String toText(String values) {
		if (values == null || values.isEmpty()) {
			return "";
		}
		String v = toHtml(values).replace("\n", "");
		v = v.replace("<h1>", "").replace("</h1>", "=>");
		v = v.replace("<h2>", "").replace("</h2>", ": ");
		v = v.replace("<h3>", "").replace("</h3>", "=");
		v = v.replace("<b>", "").replace("</b>: ", "=\"");
		v = v.replace("<p>", "").replace("</p>", "\" ");
		return v;
	}

	/**
	 * check if Book has the assistant Text checked
	 *
	 * @param book
	 * @param toString
	 * @return
	 */
	public boolean isExists(Book book, String toString) {
		//to change if book assistant has to check
		return true;
	}

	/**
	 * check if SECTION exists in Assistant
	 *
	 * @param section
	 * @return
	 */
	public boolean isExists(String section) {
		if (xml == null || section.isEmpty()) {
			return false;
		}
		Node node = xml.getSectionSingle(section);
		return (node != null);
	}

	/**
	 * get list of options for a combobox or a listbox
	 *
	 * @param section
	 * @param item: the item to search
	 *
	 * @return
	 */
	public static List<String> getListOf(String section, String item) {
		List<String> list = new ArrayList<>();
		Assistant assist = App.getAssistant();
		if (assist != null) {
			Node node = xml.getSectionSingle(section);
			if (node != null) {
				NodeList childs = AssistantXml.getTab(node);
				if (childs.getLength() > 0) {
					NodeList cbx = childs.item(0).getChildNodes();
					for (int i = 0; i < cbx.getLength(); i++) {
						if (Xml.attributeGet(cbx.item(i), "name").equals(item)) {
							NodeList elems = cbx.item(i).getChildNodes();
							for (int j = 0; j < elems.getLength(); j++) {
								String val = Xml.attributeGet(elems.item(j), "title");
								if (val != null && !val.isEmpty()) {
									list.add(val);
								}
							}
						}
					}
				}
			}
		}
		return list;
	}

	public static String getLastOpened() {
		return getXml().getLastOpened();
	}

	/**
	 * get the Assistant XML
	 *
	 * @return
	 */
	public static AssistantXml getXml() {
		if (xml == null) {
			xml = new AssistantXml(null);
		}
		return xml;
	}

	/**
	 * set the Assistant XML for a local file name
	 *
	 * @param name
	 */
	public static void setXml(String name) {
		File f = new File(name);
		if (AssistantXml.isValid(f)) {
			xml = new AssistantXml(f);
		}
	}

}
