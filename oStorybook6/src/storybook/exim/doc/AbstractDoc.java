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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import storybook.tools.LOG;
import storybook.tools.file.IOUtil;
import storybook.tools.zip.ZipXml;
import storybook.tools.html.Html;
import storybook.ui.frames.main.MainFrame;

/**
 *
 * @author favdb
 */
public abstract class AbstractDoc {

	private static final String TT = "AbstractDoc";

	public File file;
	public ZipXml zipXml;
	public ZipEntry entry;
	public List<STYLE> styles;
	private String entryName = "unknown.xxx";

	AbstractDoc(File file) {
		this.file = file;
	}

	AbstractDoc(String filename) {
		this.file = new File(filename);
	}

	AbstractDoc(File file, String name) {
		this.file = file;
		setFileName(name);
	}

	AbstractDoc(String filename, String name) {
		this.file = new File(filename);
		setFileName(name);
	}

	public void setFileName(String name) {
		this.entryName = name;
	}

	public ZipEntry getEntry() {
		return entry;
	}

	public boolean open() throws IOException, SAXException, ParserConfigurationException {
		zipXml = new ZipXml(file);
		entry = zipXml.open(entryName);
		return (entry != null);
	}

	public void close() {
		try {
			if (zipXml != null) {
				zipXml.close();
				zipXml = null;
			}
		} catch (IOException ex) {
			//LOG.trace("close error, perhaps it's normal");
		}
	}

	public abstract String getContentAsHtml(boolean withHead);

	/**
	 * get content of the file as a plain text
	 *
	 * @return the plain text, may be empty
	 */
	public String getContentAsTxt() {
		//LOG.trace(TT + ".getContentAsTxt");
		return Html.htmlToText(getContentAsHtml(true), true);
	}

	public static void copyModele(File file) {
		IOUtil.resourceCopyTo("model/empty." + IOUtil.getExtension(file), file);
	}

	abstract void getAutomaticStyles(File file);

	public abstract boolean createDoc(MainFrame mainFrame, File file, String str);

	public static String pxToCm(String w, String h) {
		double max = 16.98;
		double fw = Double.parseDouble(w) * 0.0264583333;
		double fh = Double.parseDouble(h) * 0.0264583333;
		if (fw > max) {
			fh = (fh * max) / fw;
			fw = max;
		}
		return String.format("%.3fcm;%.3fcm", fw, fh);
	}

	public String imageType(String file) {
		String type = file.substring(file.lastIndexOf('.') + 1);
		if (type.equals("jpg")) {
			return "jpeg";
		}
		return type;
	}

	public void dumpStyles() {
		//LOG.trace(TT + ".dumpStyles()");
		for (STYLE s : styles) {
			LOG.trace(s.toTrace());
		}
	}

	public STYLE styleFindLevel(int lev) {
		//LOG.trace(TT + ".findStyleLevel(lev=" + lev + ")");
		for (STYLE s : styles) {
			if (s.level == lev) {
				return s;
			}
		}
		return styles.get(0);
	}

	/**
	 * get many text elements as a list
	 *
	 * @param tags: element tags
	 *
	 * @return a list of Element
	 */
	public List<Element> getManyElements(String start, String... tags) {
		//LOG.trace(TT + ".getManyElements(tags=" + Arrays.asList(tags) + ")");
		List<Element> nodes = new ArrayList<>();
		List<String> list = Arrays.asList(tags);
		Element rootNode = zipXml.document.getDocumentElement();
		NodeList textNodes = rootNode.getElementsByTagName(start);
		Node node = (textNodes.item(0)).getFirstChild();
		while (node != null) {
			if (list.contains(node.getNodeName())) {
				nodes.add((Element) node);
			}
			node = node.getNextSibling();
		}
		return nodes;
	}

}
