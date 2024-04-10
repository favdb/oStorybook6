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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import storybook.tools.LOG;
import storybook.tools.file.EnvUtil;
import storybook.tools.xml.Xml;

/**
 * XML parser for Assistant
 *
 * @author favdb
 */
public class AssistantXml {

	private static final String TT = "AssistantXml";

	//XML type: 0=bundled, 1=installed file, 2=local file
	private static int xmlType = 0;

	public static String traceNode(Node node) {
		if (node != null) {
			String s = ((Element) node).getAttribute("name");
			if (s.isEmpty()) {
				s = "no name";
			}
			return node.getNodeName() + "(" + s + ")";
		}
		return (node == null ? "null" : node.getNodeName());
	}

	/**
	 * get the document into HTML
	 *
	 * @param doc
	 * @return
	 */
	public static String getHtml(Document doc) {
		StringBuilder b = new StringBuilder();
		getNodeToHtml(-1, b, doc.getFirstChild());
		return b.toString();
	}

	/**
	 * get the String values into HTML
	 *
	 * @param values
	 * @return
	 */
	public static String getHtml(String values) {
		return getHtml(getXmlValues(values));
	}

	private static void getNodeToHtml(int level, StringBuilder b, Node root) {
		//LOG.trace(TT + ".getNodeToHtml(level=" + level + ", b, root=" + root.getNodeName() + ")");
		NodeList nodes = root.getChildNodes();
		if (root.getNodeName().equals("#text")) {
			b.append(root.getTextContent()).append("</p>");
			return;
		}
		String h = "h" + (level + 1) + ">";
		if (level > 0) {
			if (level == 2) {
				b.append("<p><b>").append(root.getNodeName()).append("</b>: ");
			} else if (!root.getNodeName().equals("scenetext")) {
				b.append("<").append(h).append(root.getNodeName()).append("</").append(h);
			}
		}
		for (int i = 0; i < nodes.getLength(); i++) {
			getNodeToHtml(level + 1, b, nodes.item(i));
			//b.append("<p>").append(root.getTextContent()).append("</p>");
		}
	}

	private File file = null, fileSave = null;
	private String fileName = "";
	private boolean fileOpened = false;
	private Document document;
	private Element rootNode;
	private String rootName;
	private String xmlString;
	private static String lastOpened = "";

	public AssistantXml(File file) {
		if (file != null && file.exists()) {
			this.file = file;
			this.fileName = file.getAbsolutePath();
			initLocal(this.fileName);
		} else {
			init();
		}
	}

	/**
	 * Initialize the XML file from bundled file or installed file
	 *
	 */
	private void init() {
		//LOG.trace(TT + ".init() from=\"" + (fileName == null ? "null" : fileName) + "\"");
		if (!initInstalled()) {
			initBundled();
		}
	}

	/**
	 * init from bundled file
	 *
	 * @return
	 */
	public boolean initBundled() {
		//LOG.trace(TT + ".initBundled()");
		String fn = "_" + Locale.getDefault().toString();
		while (!fn.isEmpty()) {
			if (fn.equals("_")) {
				fn = "";
			}
			String strf = "assistant/Assistant" + fn + ".xml";
			//LOG.trace("checking for \"" + strf + "\"");
			if (isResourceExists(Assistant.class, strf)) {
				LOG.trace("Initialize Assistant from bundled \"" + strf + "\"");
				file = null;
				fileName = "";
				xmlString = readResource(Assistant.class, strf);
				xmlType = 0;
				openXml();
				return true;
			}
			if (fn.contains("_")) {
				fn = fn.substring(0, fn.lastIndexOf("_"));
			}

		}
		return false;
	}

	/**
	 * init from installed file
	 *
	 * @return
	 */
	public boolean initInstalled() {
		//LOG.trace(TT + ".initInstalled()");
		String strf;
		String fn = "_" + Locale.getDefault().toString();
		// check if installed directory exists
		File installed = new File(EnvUtil.getUserDir() + File.separator + "Assistant");
		if (installed.exists()) {
			while (!fn.isEmpty()) {
				if (fn.equals("_")) {
					fn = "";
				}
				strf = "Assistant" + fn + ".xml";
				installed = new File(EnvUtil.getUserDir() + File.separator + strf);
				//LOG.trace("check for \"" + installed.getAbsolutePath() + "\"");
				if (installed.exists()) {
					this.fileName = installed.getAbsolutePath();
					LOG.trace("Initialize Assistant from installed \"" + fileName + "\"");
					xmlString = readFile(fileName);
					xmlType = 1;
					openXml();
					return true;
				}
				if (fn.contains("_")) {
					fn = fn.substring(0, fn.lastIndexOf("_"));
				}
			}
		}
		return false;
	}

	/**
	 * Initialize the XML file from local file
	 *
	 */
	private boolean initLocal(String fileName) {
		//LOG.trace(TT + ".init() from=\"" + (fileName == null ? "null" : fileName) + "\"");
		File f = new File(fileName);
		if (f.exists()) {
			this.file = f;
			this.fileName = f.getAbsolutePath();
			xmlType = 2;
			xmlString = readFile(fileName);
			LOG.trace("Initialize Assistant from local file \"" + fileName + "\"");
			openXml();
			return true;
		}
		return false;
	}

	/**
	 * check if the file is a valid Assistant XML file
	 *
	 * @param file
	 * @return
	 */
	public static boolean isValid(File file) {
		String str = readFile(file.getAbsolutePath());
		Document doc = readDom(str);
		return doc != null;
	}

	/**
	 * openXml an existing file
	 *
	 * @return true if open is OK
	 */
	public boolean openXml() {
		//LOG.trace(TT + ".openXml() from=\"" + fileName + "\"");
		document = null;
		rootNode = null;
		rootName = null;
		document = readDom(xmlString);
		if (document == null) {
			LOG.err("readDom return null");
			rootNode = null;
			return false;
		}
		rootNode = document.getDocumentElement();
		rootName = rootNode.getNodeName();
		fileOpened = true;
		fileSave = (fileName.contains("file") ? file : null);
		return true;
	}

	/**
	 * check if the file was opened
	 *
	 * @return true if the file was opened
	 */
	public boolean isOpened() {
		return this.fileOpened;
	}

	/**
	 * get the File object of the opened AssistantXml file
	 *
	 * @return
	 */
	public File getFile() {
		return file;
	}

	public static Document readDom(String string) {
		//LOG.trace(TT + ".readDom()");
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(new InputSource(new StringReader(string)));
		} catch (IOException | ParserConfigurationException | SAXException e) {
			LOG.err("unable to read XML document", e);
			return null;
		}
	}

	public void closeXml() {
		//LOG.trace(TT + ".closeXml()");
		if (fileOpened) {
			document = null;
			fileOpened = false;
		}
	}

	public NodeList getSections() {
		if (rootNode == null) {
			LOG.err("rootNode in getSections is null");
			return null;
		}
		return rootNode.getChildNodes();
	}

	public NodeList getSection(Assistant.SECTION section) {
		return getSection(section.toString());
	}

	/**
	 * get the list of nodes for a SECTION
	 *
	 * @param section
	 * @return
	 */
	public NodeList getSection(String section) {
		//LOG.trace(TT + ".getSection(section='" + section + "')");
		if (rootNode == null) {
			return null;
		}
		if (section.equals("chapter")
				|| section.equals("scene")
				|| section.equals("strand")) {
			return rootNode.getElementsByTagName("book");
		}
		return rootNode.getElementsByTagName(section);
	}

	/**
	 * get the first node for a SECTION
	 *
	 * @param section
	 * @return
	 */
	public Node getSectionSingle(String section) {
		NodeList nodes = getSection(section);
		if (nodes == null || nodes.getLength() == 0) {
			//LOG.err(TT + ".getSectionSingle(section='" + section + "') error nodes is null");
			return null;
		}
		return nodes.item(0);
	}

	public Node findTab(Node node, String n) {
		//LOG.trace(TT + ".findTab(node=" + node.getNodeName() + ", n=" + n + ")");
		if (node == null) {
			return null;
		}
		NodeList childs = node.getChildNodes();
		if (childs.getLength() < 1) {
			return null;
		}
		for (int i = 0; i < childs.getLength(); i++) {
			Node nx = childs.item(i);
			if (nx == null || nx.getNodeName().equals("#text")) {
				continue;
			}
			String name = getAttribute(nx, "title");
			if (name != null && n.equalsIgnoreCase(name)) {
				return childs.item(i);
			}
		}
		return null;
	}

	public NodeList getTabs(Element element) {
		if (element == null) {
			return null;
		}
		return element.getElementsByTagName("tab");
	}

	public NodeList getNodes(Element node) {
		return node.getChildNodes();
	}

	public void removeAllChild(final Node node) {
		if (node == null) {
			return;
		}
		Node n;
		while ((n = ((Element) node).getFirstChild()) != null) {
			((Element) node).removeChild(n);
		}
	}

	public static boolean isResourceExists(Class fromClass, String in) {
		URL u = fromClass.getResource(in);
		return u != null;
	}

	public static String readResource(Class fromClass, String in) {
		//LOG.trace("readResource(fromClass, in=\"" + in + "\")");
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(
							fromClass
									.getResourceAsStream(in), "UTF-8"));
			for (int c = br.read(); c != -1; c = br.read()) {
				sb.append((char) c);
			}
		} catch (IOException ex) {
			return "";
		}
		lastOpened = "internal \"" + in + "\"";
		return sb.toString();
	}

	public static String readFile(String filePath) {
		//LOG.trace(TT+".readFile(filePath=" + filePath + ")");
		byte[] buffer = new byte[(int) new File(filePath).length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(filePath));
			f.read(buffer);
		} catch (FileNotFoundException ex) {
			return ("");
		} catch (IOException ex) {
			return ("");
		} finally {
			if (f != null) {
				try {
					f.close();
				} catch (IOException e) {
					LOG.err(TT + ".readFileAsString(filepath=" + filePath + ")", e);
				}
				lastOpened = "file \"" + filePath + "\"";
				//LOG.trace(I18N.getMsg("assistant.loaded", lastOpened));
			}
		}
		return (new String(buffer));
	}

	/**
	 * create a child node
	 *
	 * @param node: the parent node
	 * @param cat:the node type
	 * @param tfName: the name attribute
	 * @param tfTitle: the title attribute
	 * @param tfPrompt: the prompt attribute
	 * @return
	 */
	public Node createNode(Node node, String cat, String tfName, String tfTitle, String tfPrompt) {
		/*LOG.trace(TT + ".createNode("
				+ "node=\"" + node.getNodeName() + "\", "
				+ "cat=\"" + cat + "\", "
				+ "tfName=\"" + tfName + "\", "
				+ "tfTitle=\"" + tfTitle + "\", "
				+ "tfPrompt=\"" + tfPrompt + "\", "
				+ ")");*/
		Element e = document.createElement(cat);
		e.setAttribute("name", tfName);
		e.setAttribute("title", tfTitle);
		if (!tfPrompt.isEmpty()) {
			e.setAttribute("prompt", tfPrompt);
		}
		node.appendChild(e);
		return (Node) e;
	}

	public static String getAttribute(Node node, String key) {
		//LOG.trace(TT + ".getAttribute(node=" + traceNode(node) + ", key=\"" + key + "\")");
		if (node == null) {
			return "";
		}
		return ((Element) node).getAttribute(key);
	}

	public static void setAttribute(Node node, String key, String value) {
		/*LOG.trace(TT + ".setAttribute("
				+ "node=\"" + node.getNodeName() + "\""
				+ ", key=\"" + key + "\""
				+ ", value=\"" + value + "\")");*/
		Element e = (Element) node;
		e.setAttribute(key, value.trim());
	}

	public static NodeList getTab(Node node) {
		if (node == null) {
			LOG.err(TT + "getElementsByTabName node is null");
			return null;
		}
		return ((Element) node).getElementsByTagName("tab");
	}

	public boolean save() {
		if (fileSave == null) {
			LOG.err(TT + ".save() name of file is null");
			return false;
		}
		return Xml.save(document, fileSave);
	}

	public void save(File file) {
		setSave(file);
	}

	public File getSave() {
		return fileSave;
	}

	public void setSave(File file) {
		this.fileSave = file;
	}

	public String getLastOpened() {
		return lastOpened;
	}

	public Node refreshNode(Node tab, String cat, String nodeName) {
		NodeList ls = ((Element) tab).getElementsByTagName(cat.trim());
		if (ls.getLength() > 0) {
			for (int i = 0; i < ls.getLength(); i++) {
				Node nd = ls.item(i);
				if (((Element) nd).getAttribute("name").equals(nodeName.trim())) {
					return nd;
				}
			}
		}
		return null;
	}

	public static Document getXmlValues(String string) {
		//LOG.trace(TT + ".getXmlValues(values=\"" + string + "\")");
		String head = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
				beg = "<assistant>",
				start = head + beg,
				foot = "</assistant>";
		if (string == null || string.startsWith("<html>")) {
			return AssistantXml.readDom(start + "" + foot);
		} else if (string.startsWith(beg)) {
			return AssistantXml.readDom(string);
		} else {
			return AssistantXml.readDom(start + string + foot);
		}
	}

	public static String getText(Document doc, String key) {
		//LOG.trace(TT + ".getText(doc, key=" + key + ")");
		NodeList childs = doc.getElementsByTagName(key.trim());
		if (childs.getLength() > 0) {
			Element el = (Element) childs.item(0);
			return el.getTextContent();
		}
		return "";
	}

	public String traceAll() {
		return rootNode.getTextContent();
	}

}
