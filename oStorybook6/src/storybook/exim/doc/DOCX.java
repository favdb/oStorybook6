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
 * You should have received a fileCopy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.exim.doc;

import api.jsoup.Jsoup;
import api.jsoup.nodes.Document;
import api.jsoup.select.Elements;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import storybook.tools.LOG;
import storybook.tools.html.Html;
import storybook.tools.xml.Xml;
import storybook.tools.zip.ZipUtil;
import storybook.tools.zip.ZipXml;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class DOCX extends AbstractDoc {

	private static final String TT = "DOCX.";
	public static final String F_DOC = "word/document.xml",
			F_RELS = "word/_rels/document.xml.rels",
			F_STYLES = "word/styles.xml",
			HEADING = "heading",
			STYLE_ID = "w:styleId",
			W_BIDI = "w:bidi",
			W_BR = "w:br",
			W_HYPERLINK = "w:hyperlink",
			W_NUMID = "<w:numId w:val=\"",
			W_NUMPR = "w:numPr",
			W_P = "w:p",
			W_TYPE_PARA = "w:type=\"paragraph\"",
			W_PPR = "w:pPr",
			W_R = "w:r",
			W_RPR = "w:rPr",
			W_STYLE = "w:pStyle",
			W_T = "w:t",
			W_VAL = "w:val",
			W_VAL0 = "w:val=\"0\"";

	private NodeList listRels;
	private STYLE curStyle;
	//private BufferedWriter writer;
	private Node textNode;
	private ArrayList<Object> imgFiles;
	private Element docElement;
	private org.w3c.dom.Document doc;
	private int liNum = 1;

	public DOCX(File file) {
		//LOG.log(TT + "(file=" + (file == null ? "null" : file.getAbsolutePath()) + ")");
		super(file, F_DOC);
	}

	/**
	 * open the zipped document.xml
	 *
	 * @return
	 * @throws IOException
	 * @throws org.xml.sax.SAXException
	 * @throws javax.xml.parsers.ParserConfigurationException
	 */
	@Override
	public boolean open() throws IOException, SAXException, ParserConfigurationException {
		//LOG.log(TT + "open()");
		super.open();
		openRels();
		return (entry != null);
	}

	/**
	 * get content of the document as plain text
	 *
	 * @param withHead
	 * @return
	 */
	@Override
	public String getContentAsHtml(boolean withHead) {
		//LOG.log(TT + "getContentAsHtml(withHead=" + (withHead ? "true" : "false") + ")");
		getAutomaticStyles(file);
		NodeList nodes = zipXml.getNodelist(W_P);
		if (nodes.getLength() < 1) {
			return "";
		}
		StringBuilder out = new StringBuilder();
		if (withHead) {
			out.append(Html.HTML_B);
			out.append(Html.intoTag("head", "<meta charset=\"UTF-8\">\n"))
					.append(Html.BODY_B);
		}
		for (int i = 0; i < nodes.getLength(); i++) {
			Element el = (Element) nodes.item(i);
			out.append(getParagraph(el));
		}
		if (withHead) {
			out.append(Html.BODY_E);
			out.append(Html.HTML_E);
		}
		String res = out.toString();
		return res;
	}

	private String getParagraph(Node node) {
		//LOG.trace(TT + "getParagraph(node=" + node.getNodeName() + ")");
		StringBuilder b = new StringBuilder();
		STYLE s = styleFind(styleGetP(node));
		if (s != null && s.type.equals(HEADING)) {
			b.append(getTitle(node, s));
		} else {
			if (isList(node)) {
				//c'est une liste
				if (isList == 0) {
					b.append(Html.UL_B);
				}
				isList++;
				b.append(Html.LI_B);
				b.append(getP(node));
				b.append(Html.LI_E);
			} else {
				if (isList > 0) {
					b.append(Html.UL_E);
					isList = 0;
				}
				b.append(getP(node));
			}
		}
		return b.toString();
	}

	private boolean isList(Node node) {
		String x = Xml.nodeToString(node);
		return (x.contains("<w:ilvl w:val=\"0\"/>") && !x.contains("w:outlineLvl"));
	}

	private String styleGetP(Node node) {
		//LOG.trace(TT + "styleGetP(node=" + node.getNodeName() + ")");
		NodeList c0 = ((Element) node).getElementsByTagName(W_PPR);
		if (c0.getLength() < 1) {
			return "";
		}
		NodeList child = ((Element) c0.item(0)).getElementsByTagName(W_STYLE);
		if (child.getLength() < 0) {
			return "";
		}
		Element el = (Element) child.item(0);
		if (el == null) {
			return "";
		}
		return ((Element) child.item(0)).getAttribute(W_VAL);
	}

	private String getTitle(Node node, STYLE style) {
		//LOG.trace(TT + "getTitle(node=" + node.getNodeName() + ", style=" + style.toTrace());
		StringBuilder b = new StringBuilder();
		b.append("<h").append(style.level.toString()).append(">");
		b.append(node.getTextContent());
		b.append("</h").append(style.level.toString()).append(">\n");
		return b.toString();
	}

	private STYLE style = null;

	private String getP(Node node) {
		//LOG.trace(TT + "getP(node=" + node.getNodeName() + ")");
		StringBuilder b = new StringBuilder(Html.P_B);
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node nn = nodes.item(i);
			if (nn.getNodeName().startsWith("w:bookmark")) {
				continue;
			}
			switch (nn.getNodeName()) {
				case W_HYPERLINK:// c'est un lien
					String s = Html.intoA("", findRels(nn), nn.getTextContent(), "");
					b.append(s);
					break;
				case W_BR:
					b.append(Html.BR);
					break;
				case W_RPR:
					style = new STYLE(Xml.nodeToString(nn));
					break;
				case W_R:
					b.append(getR(nn));
					break;
				default:// c'est un paragraphe
					if (nn.getNodeName().equals(W_T)) {
						b.append(styleSet(style)).append(nn.getTextContent()).append(styleSetEnd(style));
					}
					break;
			}
		}
		b.append(Html.P_E);
		return b.toString();
	}

	private int isList = 0;

	private String getR(Node node) {
		//LOG.trace(TT + "getR(node="+node.getNodeName()+")");
		StringBuilder b = new StringBuilder();
		NodeList nodes = ((Element) node).getChildNodes();
		STYLE s = new STYLE(Xml.nodeToString(node));
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			switch (n.getNodeName()) {
				case W_BR:
					b.append(Html.BR);
					break;
				case W_RPR:
					s = new STYLE(Xml.nodeToString(n));
					break;
				default:
					if (n.getNodeName().equals(W_T)) {
						b.append(styleSet(s)).append(n.getTextContent()).append(styleSetEnd(s));
					}
					break;
			}
		}
		return b.toString();
	}

	private String styleSet(STYLE style) {
		StringBuilder b = new StringBuilder();
		if (style != null) {
			b.append(style.bold ? Html.B_BEG : "");
			b.append(style.italic ? Html.I_B : "");
			b.append(style.underline ? Html.U_B : "");
		}
		return b.toString();
	}

	private String styleSetEnd(STYLE style) {
		StringBuilder b = new StringBuilder();
		if (style != null) {
			b.append(style.underline ? Html.U_E : "");
			b.append(style.italic ? Html.I_E : "");
			b.append(style.bold ? Html.B_END : "");
		}
		return b.toString();
	}

	private STYLE styleFind(String name) {
		//LOG.trace(TT + "findStyles(name=" + name + ")");
		for (STYLE s : styles) {
			if (s.name.equals(name)) {
				return s;
			}
		}
		return null;
	}

	private void openRels() {
		//LOG.trace(TT + "openRels()");
		try {
			ZipXml xml = new ZipXml(file);
			if (xml.open(F_RELS) != null) {
				listRels = xml.getNodelist("Relationship");
			} else {
				listRels = null;
			}
		} catch (IOException | SAXException | ParserConfigurationException ex) {
		}
	}

	private String findRels(Node node) {
		//LOG.trace(TT + "findRels(node=" + node.getNodeName() + ")");
		if (listRels != null) {
			String x = Xml.attributeGet(node, "r:id");
			if (x.isEmpty()) {
				return "";
			}
			for (int i = 0; i < listRels.getLength(); i++) {
				Node rel = listRels.item(i);
				String z = Xml.attributeGet(rel, "Id");
				if (z.equals(x)) {
					String target = Xml.attributeGet(rel, "Target");
					return (target.startsWith("http") ? target : "");
				}
			}
		}
		return "";
	}

	@Override
	void getAutomaticStyles(File file) {
		//LOG.log(TT + "getAutomaticStyles(file=" + file.getAbsolutePath() + ")");
		styles = new ArrayList<>();
		ZipXml xml = new ZipXml(file);
		try {
			if (xml.open(F_STYLES) != null) {
				NodeList list = xml.getNodelist("w:style");
				for (int i = 0; i < list.getLength(); i++) {
					Node node = list.item(i);
					String s = Xml.nodeToString(node);
					if (s.contains(HEADING) || s.contains("w:outlineLvl")) {
						styleSetTitle(s);
					} else if (s.contains(W_TYPE_PARA) && s.contains("w:val=\"List")) {
						String name = Xml.attributeGet(node, STYLE_ID);
						styles.add(new STYLE(name, "list", 0, ""));
					} else if (s.contains(W_TYPE_PARA)) {
						String name = Xml.attributeGet(node, STYLE_ID);
						String src = Xml.nodeToString(node);
						boolean bold = src.contains("<w:b/>");
						boolean italic = src.contains("<w:i/>");
						boolean underline = src.contains("<w:u");
						styles.add(new STYLE(name, bold, italic, underline));
					}
				}
			} else {
				LOG.err("unable to open word/styles");
			}
		} catch (IOException | SAXException | ParserConfigurationException ex) {
			// empty
		}
	}

	/**
	 * create a DOCX document
	 *
	 * @param mainFrame
	 * @param file : fhe file to create
	 * @param str : the HTML code to convert
	 * @return : true if all is OK
	 */
	@Override
	public boolean createDoc(MainFrame mainFrame, File file, String str) {
		//LOG.trace(TT + "createDoc(mainFrame, file=" + file.getAbsolutePath() + ", str len=" + str.length() + ")");
		if (!file.exists()) {
			copyModele(file);
		}
		imgFiles = new ArrayList<>();
		try {
			Path tmpDir = Files.createTempDirectory("tmpDoc");
			ZipUtil.deflate(file, tmpDir);
			zipXml = new ZipXml(file);
			entry = zipXml.open(F_DOC);
			if (entry == null) {
				LOG.err("xml is null");
				return false;
			}
			getAutomaticStyles(file);
			//STYLE.trace(styles);
			doc = zipXml.document;
			docElement = doc.getDocumentElement();
			textNode = docElement.getElementsByTagName("w:body").item(0);
			if (textNode == null) {
				LOG.err(TT + ".docxCreate textNode is null");
				return false;
			}
			while (true) {
				NodeList nodes = textNode.getChildNodes();
				if (nodes.getLength() < 1) {
					break;
				}
				textNode.removeChild(nodes.item(0));
			}
			String html = str.replace("<p> ", "<p>").replace(" </p>", "</p>");
			Document jsoup = Jsoup.parse(html);
			Elements htmlNodes = jsoup.body().children();
			for (int i = 0; i < htmlNodes.size(); i++) {
				api.jsoup.nodes.Element el = htmlNodes.get(i);
				if (el.nodeName().startsWith("h")) {
					writeTitle(el);
				} else if (el.nodeName().startsWith("ul")) {
					writeUL(el);
				} else if (el.nodeName().startsWith("ol")) {
					writeOL(el);
				} else if (el.nodeName().startsWith("p")) {
					writeP(textNode, el);
				}
			}
			writeSectPr();
			closeDoc(file, tmpDir);
		} catch (IOException | SAXException | ParserConfigurationException | TransformerException ex) {
			LOG.err(TT + ".createDoc Exception", ex);
			return false;
		}
		return true;
	}

	private void styleSetTitle(String title) {
		//LOG.trace(TT + "styleSetTitle(title=" + title + ")");
		String sx = Xml.attributeGet(title, "w:outlineLvl w:val=\"");
		String name = Xml.attributeGet(title, "w:styleId=\"");
		styles.add(new STYLE(name, HEADING, (Integer.parseInt(sx)) + 1, ""));
	}

	private void writeSectPr() {
		//LOG.trace(TT + "writeSectPr()");
		Element sect = Xml.childCreate(doc, textNode, "w:sectPr");
		Xml.childCreate(doc, sect, "w:type", "w:val=\"nextPage\"");
		Xml.childCreate(doc, sect, "w:pgSz", "w:h=\"16838\"", "w:w=\"11906\"");
		Xml.childCreate(doc, sect, "w:pgMar",
				"w:bottom=\"1134\"",
				"w:footer=\"0\"",
				"w:gutter=\"0\"",
				"w:header=\"0\"",
				"w:left=\"1134\"",
				"w:right=\"1134\"",
				"w:top=\"1134\"/");
		Xml.childCreate(doc, sect, "w:pgNumType", "w:fmt=\"decimal\"");
		Xml.childCreate(doc, sect, "w:formProt", "w:val=\"false\"");
		Xml.childCreate(doc, sect, "w:textDirection", "w:val=\"lrTb\"");
		Xml.childCreate(doc, sect, "w:docGrid",
				"w:charSpace=\"0\"",
				"w:linePitch=\"100\"",
				"w:type=\"default\"");
	}

	/**
	 * close the DOCX
	 *
	 * @param file: file to output
	 * @param tmpDir: temporary dir
	 * @throws TransformerConfigurationException
	 * @throws TransformerException
	 * @throws IOException
	 */
	private void closeDoc(File file, Path tmpDir) throws TransformerConfigurationException, TransformerException, IOException {
		//LOG.trace(TT + "closeDoc(file=" + file.getAbsolutePath() + ", tmpDir=" + tmpDir.toString());
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		File contentfile = new File(tmpDir + File.separator + F_DOC);
		StreamResult result = new StreamResult(contentfile);
		transformer.transform(source, result);
		zipXml.close();
		ZipUtil.fromDir(tmpDir.toFile().getAbsolutePath(), file);
		File f = tmpDir.toFile();
		f.delete();
		close();
	}

	/**
	 * write a XML node for a title
	 *
	 * @param el : the HTML element to write
	 */
	private void writeTitle(api.jsoup.nodes.Element el) {
		//LOG.trace(TT + "writeTitle(el=\"" + el.html() + "\")");
		String nodename = el.nodeName().toLowerCase();
		if (!nodename.startsWith(("h"))) {
			return;
		}
		int level = Integer.parseInt(nodename.substring(1));
		Element p = Xml.childCreate(doc, textNode, W_P);
		Element ppr = Xml.childCreate(doc, p, W_PPR);
		Element pstyle = Xml.childCreate(doc, ppr, W_STYLE);
		Xml.childCreate(doc, ppr, "w:spacing", "w:before=\"240\"", "w:after=\"120\"");
		Xml.childCreate(doc, ppr, W_RPR);
		STYLE s = STYLE.findStyle(styles, level);
		if (s != null) {
			pstyle.setAttribute("w:val", s.getName());
		} else {
			LOG.err(TT + ".writeTile level=" + level + " not found");
		}
		Element r = Xml.childCreate(doc, p, W_R);
		Xml.childCreate(doc, r, W_RPR);
		Element t = Xml.childCreate(doc, r, W_T);
		t.setTextContent(el.text());
	}

	/**
	 * write a XML node for an ordered list
	 *
	 * @param el
	 */
	private void writeOL(api.jsoup.nodes.Element el) {
		//LOG.trace(TT + "writeOL(el=\"" + el.html().replace("\n", "") + "\")");
		Elements lis = el.getAllElements();
		liNum = 1;
		for (int i = 0; i < lis.size(); i++) {
			api.jsoup.nodes.Element li = lis.get(i);
			writeLI(li, "ol");
		}
	}

	/**
	 * write a XML node for an unorderd list
	 *
	 * @param el
	 */
	private void writeUL(api.jsoup.nodes.Element el) {
		//LOG.trace(TT + "writeUL(element=\"" + el.html().replace("\n", "") + "\")");
		Elements lis = el.children();
		for (int i = 0; i < lis.size(); i++) {
			writeLI(lis.get(i), "ul");
		}
	}

	/**
	 * write a XML node for an item of an ordered or unordered lis
	 *
	 * @param el
	 * @param stylename
	 * @return
	 */
	private void writeLI(api.jsoup.nodes.Element el, String type) {
		//LOG.trace(TT + "writeLI(element=\"" + el.html() + "\", type=" + type + ")");
		Element p = Xml.childCreate(doc, textNode, W_P);
		Element ppr = Xml.childCreate(doc, p, W_PPR);
		Xml.childCreate(doc, ppr, W_STYLE, "w:val=\"Normal\"");
		Xml.childCreate(doc, ppr, W_BIDI, W_VAL0);
		Xml.childCreate(doc, ppr, "w:jc", "w:val=\"left\"");
		Xml.childCreate(doc, ppr, W_RPR);
		Element r = Xml.childCreate(doc, p, W_R);
		Xml.childCreate(doc, r, W_RPR);
		Element t = Xml.childCreate(doc, r, W_T);
		if (type.equals("ul")) {
			t.setTextContent("● " + el.text());
		} else {
			t.setTextContent(liNum + ". " + el.text());
			liNum++;
		}
	}

	/**
	 * write a XML node for a paragraph
	 *
	 * @param toNode
	 * @param el
	 */
	private void writeP(Node toNode, api.jsoup.nodes.Element el) {
		//LOG.trace(TT + "writeP(toNode=" + toNode.getNodeName() + ", el=\"" + el.toString() + "\")");
		Element p = Xml.childCreate(doc, toNode, W_P);
		Element ppr = Xml.childCreate(doc, p, W_PPR);
		curStyle = STYLE.findStyle(styles, "Normal");
		Xml.childCreate(doc, ppr, W_STYLE, "w:val=\"Normal\"");
		Xml.childCreate(doc, ppr, W_BIDI, W_VAL0);
		Xml.childCreate(doc, ppr, "w:jc", "w:val=\"left\"");
		Xml.childCreate(doc, ppr, W_RPR);
		List<api.jsoup.nodes.Node> frags = el.childNodes();
		if (!frags.isEmpty()) {
			for (int i = 0; i < frags.size(); i++) {
				Element r = Xml.childCreate(doc, p, W_R);
				Xml.childCreate(doc, r, W_RPR);
				if (frags.get(i) instanceof api.jsoup.nodes.TextNode) {
					Element t = Xml.childCreate(doc, r, W_T, "xml:space=\"preserve\"");
					t.setTextContent(frags.get(i).toString());
				} else {
					writeR(r, (api.jsoup.nodes.Element) frags.get(i));
				}
			}
		} else {
			Element r = Xml.childCreate(doc, p, W_R);
			Xml.childCreate(doc, r, W_RPR);
			Element t = Xml.childCreate(doc, r, W_T);
			t.setTextContent(el.text());
		}
	}

	private void writeFragment(Element r, api.jsoup.nodes.Element node) {
		//LOG.trace(TT + "writeFragment(r, node=\"" + node.toString() + "\")");
		if (curStyle.bold || curStyle.italic || curStyle.underline) {
			Element rpr = Xml.childCreate(doc, r, W_RPR);
			if (curStyle.underline) {
				Xml.childCreate(doc, rpr, "w:u", "w:val=\"single\"");
			}
			if (curStyle.italic) {
				Xml.childCreate(doc, rpr, "w:i");
				Xml.childCreate(doc, rpr, "w:iCs");
			}
			if (curStyle.bold) {
				Xml.childCreate(doc, rpr, "w:b");
				Xml.childCreate(doc, rpr, "w:bCs");
			}
		}
		Element t = Xml.childCreate(doc, r, W_T);
		t.setTextContent(node.text());
	}

	private void writeSubR(Element r, api.jsoup.nodes.Element node) {
		for (api.jsoup.nodes.Element e : node.children()) {
			writeR(r, e);
		}
	}

	private void writeR(Element r, api.jsoup.nodes.Element node) {
		//LOG.trace(TT + "writeR(toNode=" + r.getNodeName() + ", el=" + node.toString() + ")");
		switch (node.nodeName()) {
			case "br":
				Xml.childCreate(doc, r, "w:br");
				break;
			case "b":
				curStyle.bold = true;
				if (!node.children().isEmpty()) {
					writeSubR(r, node);
				} else {
					writeFragment(r, node);
				}
				curStyle.bold = false;
				break;
			case "i":
				curStyle.italic = true;
				if (!node.children().isEmpty()) {
					writeSubR(r, node);
				} else {
					writeFragment(r, node);
				}
				curStyle.italic = false;
				break;
			case "u":
				curStyle.underline = true;
				if (!node.children().isEmpty()) {
					writeSubR(r, node);
				} else {
					writeFragment(r, node);
				}
				curStyle.underline = false;
				break;
			case "a":
				writeLink(r, node);
				break;
			case "img":
				writeImage(r, node);
				break;
			case "#text":
				Element t = Xml.childCreate(doc, r, W_T, "xml:space=\"preserve\"");
				t.setTextContent(node.text());
				break;
			default:
				if (!node.children().isEmpty()) {
					writeSubR(r, node);
				} else {
					writeFragment(r, node);
				}
				break;
		}
	}

	private void writeLink(Element r, api.jsoup.nodes.Element el) {
		//LOG.trace(TT + "writeLink(r=" + r.getNodeName() + ", el=\"" + el.html() + "\")");
		Element t = Xml.childCreate(doc, r, W_T);
		String src = ((api.jsoup.nodes.Node) el).attr("href");
		t.setTextContent(el.text() + " (" + src + ")");
	}

	private void writeImage(Element r, api.jsoup.nodes.Element el) {
		//LOG.trace(TT + "writeImage(r=" + r.getNodeName() + ", el=\"" + el.html() + "\")");
		Element t = Xml.childCreate(doc, r, W_T);
		String src = ((api.jsoup.nodes.Node) el).attr("src");
		if (!src.isEmpty()) {
			if (src.startsWith("file://")) {
				src = src.replace("file://", "");
			}
		}
		String alt = el.attr("alt") + " ";
		t.setTextContent("{" + src + "} " + alt);
	}

}
