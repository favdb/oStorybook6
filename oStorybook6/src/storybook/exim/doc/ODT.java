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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import api.jsoup.Jsoup;
import api.jsoup.internal.StringUtil;
import api.jsoup.nodes.Document;
import api.jsoup.select.Elements;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import storybook.tools.LOG;
import storybook.tools.file.IOUtil;
import storybook.tools.html.Html;
import storybook.tools.xml.Xml;
import storybook.tools.zip.ZipUtil;
import storybook.tools.zip.ZipXml;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class ODT extends AbstractDoc {

	private static final String TT = "ODT";
	private static final String F_ODT = "content.xml",
			LINK_HREF = "xlink:href",
			TEXT_A = "text:a",
			TEXT_BR = "text:line-break",
			TEXT_H = "text:h",
			TEXT_LIST = "text:list",
			TEXT_LIST_ITEM = "text:list-item",
			TEXT_P = "text:p",
			TEXT_SPAN = "text:span",
			TEXT_STYLE_NAME = "text:style-name";
	private Node textNode;
	private Element docElement;

	public ODT(String filename) {
		//LOG.trace(TT + ".ODT(filename=" + filename + ")");
		super(filename, F_ODT);
	}

	public ODT(File file) {
		//LOG.trace(TT + ".ODT(file=" + (file == null ? "null" : file.getAbsolutePath()) + ")");
		super(file, F_ODT);
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
		//LOG.trace(TT + ".open() for file=" + file.getAbsolutePath());
		return (super.open());
	}

	/**
	 * get content of the document as HTML
	 *
	 * @param withHead
	 * @return
	 */
	@Override
	public String getContentAsHtml(boolean withHead) {
		//LOG.trace(TT + ".getContentAsHtml(withHead=" + (withHead ? "true" : "false") + ")");
		getStyles();
		List<Element> nodes = getManyElements("office:text", TEXT_H, TEXT_P, TEXT_LIST);
		if (nodes.isEmpty()) {
			return "";
		}
		StringBuilder out = new StringBuilder();
		if (withHead) {
			out.append(Html.HTML_B);
			out.append("\n").append(Html.BODY_B);
		}
		for (int i = 0; i < nodes.size(); i++) {
			Element el = nodes.get(i);
			switch (el.getNodeName()) {
				case TEXT_H:
					String lev = el.getAttribute("text:outline-level");
					if (!lev.isEmpty() && StringUtil.isNumeric(lev)) {
						out.append(Html.intoH(Integer.valueOf(lev), el.getTextContent()));
					} else {
						out.append(Html.intoP(el.getTextContent()));
					}
					break;
				case TEXT_LIST:
					out.append(getList(el));
					break;
				default:
					out.append(Html.intoP(getParagraph(el)));
					break;
			}
		}
		if (withHead) {
			out.append(Html.BODY_E);
			out.append(Html.HTML_E);
		}
		return out.toString();
	}

	/**
	 * get element text as HTML
	 *
	 * @param element to parse
	 *
	 * @return a String, may be empty
	 */
	private String getList(Element element) {
		StringBuilder b = new StringBuilder(Html.UL_B);
		NodeList nodes = element.getElementsByTagName(TEXT_LIST_ITEM);
		for (int i = 0; i < nodes.getLength(); i++) {
			Element el = (Element) nodes.item(i);
			b.append(Html.intoLI(el.getTextContent()));
		}
		b.append(Html.UL_E);
		return b.toString();
	}

	/**
	 * get a paragraph as a HTML String
	 *
	 * @param element to parse
	 *
	 * @return the content of the element
	 */
	private String getParagraph(Element element) {
		StringBuilder b = new StringBuilder();
		NodeList nodes = ((Node) element).getChildNodes();
		STYLE style = findStyle(element.getAttribute(TEXT_STYLE_NAME));
		if (style != null) {
			b.append(getStyle(style));
		}
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName() == null || node.getNodeName().isEmpty()) {
				b.append(((Element) node).getTextContent());
			} else {
				switch (node.getNodeName()) {
					case "#text":
						b.append(node.getTextContent());
						break;
					case TEXT_A:
						String x = Html.intoA("", Xml.attributeGet(node, LINK_HREF), node.getTextContent(), "");
						b.append(x);
						break;
					case TEXT_SPAN:
						String st = ((Element) node).getAttribute(TEXT_STYLE_NAME);
						b.append(getStyle(findStyle(st)));
						b.append(node.getTextContent());
						b.append(getStyleEnd(findStyle(st)));
						break;

					case TEXT_BR:
						b.append(Html.BR);
						break;
					default:
						break;
				}
			}
		}
		if (style != null) {
			b.append(getStyleEnd(style));
		}
		return b.toString();
	}

	/**
	 * get a String style for a STYLE
	 *
	 * @param style
	 *
	 * @return a String as starting bold, italic and/or underline
	 */
	private String getStyle(STYLE style) {
		//LOG.trace(TT + ".getStyle(style=" + (style == null ? "null" : style.name));
		StringBuilder b = new StringBuilder();
		if (style != null) {
			if (style.bold) {
				b.append(Html.B_BEG);
			}
			if (style.italic) {
				b.append(Html.I_B);
			}
			if (style.underline) {
				b.append(Html.U_B);
			}
		}
		return b.toString();
	}

	/**
	 * get a String end style for a STYLE
	 *
	 * @param style
	 *
	 * @return a String as ending bold, italic and/or underline
	 */
	private String getStyleEnd(STYLE style) {
		StringBuilder b = new StringBuilder();
		if (style != null) {
			if (style.underline) {
				b.append(Html.U_E);
			}
			if (style.italic) {
				b.append(Html.I_E);
			}
			if (style.bold) {
				b.append(Html.B_END);
			}
		}
		return b.toString();
	}

	/**
	 * find a STYLE from his name
	 *
	 * @param name to search
	 *
	 * @return corresponding STYLE, null if not found
	 */
	private STYLE findStyle(String name) {
		for (STYLE s : styles) {
			if (s.name.equals(name)) {
				return s;
			}
		}
		return null;
	}

	/**
	 * get automatic styles
	 *
	 * @param file not used for ODT
	 *
	 */
	@Override
	void getAutomaticStyles(File file) {
		//LOG.trace(TT + ".getAutomaticStyles(file)");
		NodeList list = zipXml.getNodelist("office:automatic-styles");
		if (list.getLength() > 0) {
			NodeList nodes = list.item(0).getChildNodes();
			if (nodes.getLength() > 0) {
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					if (node.getNodeName().equals("#text")) {
						continue;
					}
					if (((Element) node).getAttribute("style:family").equals("text")) {
						styles.add(new STYLE(node));
					}
				}
			}
		}
	}

	/**
	 * get all styles
	 *
	 */
	private void getStyles() {
		//LOG.trace(TT + ".getStyles()");
		styles = new ArrayList<>();
		getAutomaticStyles(file);
		ZipXml xml = new ZipXml(file);
		try {
			if (xml.open("styles.xml") != null) {
				NodeList list = xml.getNodelist("style:style");
				for (int i = 0; i < list.getLength(); i++) {
					Node node = list.item(i);
					boolean bold = false, italic = false, underline = false;
					String s = Xml.nodeToString(node);
					String name = ((Element) node).getAttribute("style:name");
					String level = ((Element) node).getAttribute("style:default-outline-level");
					if (s.contains("italic")) {
						italic = true;
					}
					if (s.contains("bold")) {
						bold = true;
					}
					if (s.contains("style:text-underline-style=")) {
						underline = true;
					}
					if (italic || bold || underline) {
						STYLE style = new STYLE(name, bold, italic, underline);
						if (!level.isEmpty()) {
							style.level = Integer.valueOf(level);
						}
						styles.add(style);
					}
				}
				xml.close();
			} else {
				LOG.err("unable to open styles.xml");
			}
		} catch (IOException | SAXException | ParserConfigurationException ex) {
		}
	}

	List<File> imgFiles;

	/**
	 * create an ODT document
	 *
	 * @param mainFrame
	 * @param file: fhe file to create
	 * @param str: the HTML code to convert
	 * @return : true if all is OK
	 */
	@Override
	public boolean createDoc(MainFrame mainFrame, File file, String str) {
		// LOG.trace(TT + ".createDoc(mainFrame,file=" + file.getAbsolutePath() + ", str len=" + str.length() + ")");
		if (!file.exists()) {
			copyModele(file);
		}
		imgFiles = new ArrayList<>();
		try {
			Path tmpDir = Files.createTempDirectory("tmpDoc");
			ZipUtil.deflate(file, tmpDir);
			zipXml = new ZipXml(file);
			entry = zipXml.open(F_ODT);
			if (entry == null) {
				LOG.err("xml is null");
				return false;
			}
			getStyles();
			docElement = zipXml.document.getDocumentElement();
			textNode = docElement.getElementsByTagName("office:text").item(0);
			if (textNode == null) {
				LOG.err(TT + ".createDoc textNode is null");
				return false;
			}
			List<Element> rmNodes = getManyElements("office:text", TEXT_H, TEXT_P, TEXT_LIST);
			for (int i = 0; i < rmNodes.size(); i++) {
				rmNodes.get(i).getParentNode().removeChild(rmNodes.get(i));
			}
			Document jsoup = Jsoup.parse(str);
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
					writeParagraph(textNode, el);
				}
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(zipXml.document);
			File contentfile = new File(tmpDir + File.separator + F_ODT);
			StreamResult result = new StreamResult(contentfile);
			transformer.transform(source, result);
			zipXml.close();
			if (!imgFiles.isEmpty()) {
				createManifest(tmpDir, imgFiles);
			}
			ZipUtil.fromDir(tmpDir.toFile().getAbsolutePath(), file, "application/vnd.oasis.opendocument.text");
			File f = tmpDir.toFile();
			f.delete();
		} catch (IOException | SAXException | ParserConfigurationException | TransformerException ex) {
			LOG.err(TT + ".createDoc Exception", ex);
			return false;
		}
		return true;
	}

	/**
	 * write a XML node for a title
	 *
	 * @param element : the HTML element to write
	 */
	private void writeTitle(api.jsoup.nodes.Element element) {
		//LOG.trace(TT + ".writeTitle(textNodes, el=" + el.html() + ")");
		Element child = zipXml.document.createElement(TEXT_H);
		int level = Integer.parseInt(element.nodeName().substring(1));
		child.setAttribute(TEXT_STYLE_NAME, "Heading_20_" + level);
		child.setAttribute("text:outline-level", level + "");
		child.setTextContent(element.text());
		textNode.appendChild(child);
	}

	/**
	 * write a XML node for an unorderd list
	 *
	 * @param element
	 */
	private void writeUL(api.jsoup.nodes.Element element) {
		//LOG.trace(TT + ".writeUL(textNodes, el=" + el.html().replace("\n", "") + ")");
		Element ul = zipXml.document.createElement(TEXT_LIST);
		ul.setAttribute("xml:id", ("list" + ThreadLocalRandom.current().nextInt()).replace("-", ""));
		ul.setAttribute(TEXT_STYLE_NAME, "WWNum2");
		Elements lis = element.getElementsByTag("li");
		for (int i = 0; i < lis.size(); i++) {
			ul.appendChild(writeLI((api.jsoup.nodes.Element) lis.get(i), "P1"));
		}
		textNode.appendChild(ul);
	}

	/**
	 * write a XML node for an item of an ordered or unordered lis
	 *
	 * @param element
	 * @param stylename
	 * @return
	 */
	private Element writeLI(api.jsoup.nodes.Element element, String stylename) {
		//LOG.trace(TT + ".writeLI(element=" + element.html() + ")");
		Element p = zipXml.document.createElement(TEXT_P);
		p.setTextContent(Html.htmlToText(element.text()).trim());
		if (!stylename.isEmpty()) {
			p.setAttribute(TEXT_STYLE_NAME, stylename);
		}
		Element li = zipXml.document.createElement(TEXT_LIST_ITEM);
		li.appendChild(p);
		return li;
	}

	/**
	 * write a XML node for an ordered list
	 *
	 * @param el
	 */
	private void writeOL(api.jsoup.nodes.Element el) {
		//LOG.trace(TT + ".writeUL(textNodes, el=" + el.html().replace("\n", "") + ")");
		Element ul = zipXml.document.createElement(TEXT_LIST);
		ul.setAttribute("xml:id", ("list" + ThreadLocalRandom.current().nextInt()).replace("-", ""));
		ul.setAttribute(TEXT_STYLE_NAME, "WWNum3");
		Elements lis = el.getElementsByTag("li");
		for (int i = 0; i < lis.size(); i++) {
			ul.appendChild(writeLI((api.jsoup.nodes.Element) lis.get(i), "P1"));
		}
		textNode.appendChild(ul);
	}

	/**
	 * write a XML node for a paragraph
	 *
	 * @param toNode
	 * @param htmlElement
	 */
	private void writeParagraph(Node toNode, api.jsoup.nodes.Element htmlElement) {
		//LOG.trace(TT + ".writeP(textNodes, el=" + htmlElement.html() + ")");
		Element p = zipXml.document.createElement(TEXT_P);
		p.setAttribute(TEXT_STYLE_NAME, "P1");
		List<api.jsoup.nodes.Node> htmlNodes = htmlElement.childNodes();
		for (api.jsoup.nodes.Node htmlNode : htmlNodes) {
			switch (htmlNode.nodeName()) {
				case "br":
					Element span = zipXml.document.createElement(TEXT_BR);
					p.appendChild(span);
					break;
				case "font":
				case "#text":
					String str = Html.htmlToText(htmlNode.outerHtml());
					Text t = zipXml.document.createTextNode(htmlNode.toString());
					t.setTextContent(str);
					p.appendChild(t);
					break;
				case "b":
					writeFragment(p, (api.jsoup.nodes.Element) htmlNode, "b");
					break;
				case "i":
					writeFragment(p, (api.jsoup.nodes.Element) htmlNode, "i");
					break;
				case "u":
					writeFragment(p, (api.jsoup.nodes.Element) htmlNode, "u");
					break;
				case "a":
					writeLink(p, (api.jsoup.nodes.Element) htmlNode);
					break;
				case "img":
					writeImage(p, (api.jsoup.nodes.Element) htmlNode);
					break;
				default:
					LOG.trace(htmlNode.nodeName() + " (ignored)");
					break;
			}
		}
		toNode.appendChild(p);
	}

	/**
	 * write a XML node for a link
	 *
	 * @param toNode
	 * @param htmlNode
	 */
	private void writeLink(Node toNode, api.jsoup.nodes.Element htmlNode) {
		//LOG.trace(TT + ".writeA(toNode=" + XmlUtil.NodeToString(toNode) + ", \nhtmlNode=" + htmlNode.nodeName() + ")");
		writeFragment(toNode, htmlNode, "");
	}

	private int numImage = 1;

	/**
	 * write a XML link to an Image
	 *
	 * @param toNode
	 * @param htmlNode
	 */
	private void writeImage(Node toNode, api.jsoup.nodes.Element htmlNode) {
		//LOG.trace(TT + ".writeImage(toNode=" + XmlUtil.NodeToString(toNode) + ", \nhtmlNode=" + htmlNode.nodeName() + ")");
		String src = ((api.jsoup.nodes.Node) htmlNode).attr("src");
		if (!src.isEmpty()) {
			src = src.substring(src.indexOf(':') + 1);
		}
		if (src.startsWith("//")) {
			src = src.substring(2);
		}
		Text t = zipXml.document.createTextNode("{" + src + "}");
		toNode.appendChild(t);
	}

	/**
	 * write a XML node for a text fragment
	 *
	 * @param toNode
	 * @param element
	 * @param fmt
	 */
	private void writeFragment(Node toNode, api.jsoup.nodes.Element element, String fmt) {
		//LOG.trace(TT + ".writeSpan(textNodes, el=" + htmlNode.html() + ")");
		Element span = zipXml.document.createElement(TEXT_SPAN);
		if (!fmt.isEmpty()) {
			span.setAttribute(TEXT_STYLE_NAME, "Text" + fmt.toUpperCase());
		}
		span.setTextContent(" " + Html.htmlToText(element.text()) + " ");
		toNode.appendChild(span);
	}

	/**
	 * create the manifest.xml for a listed images files
	 *
	 * @param dest: path to the destination file
	 * @param images: list of th images files
	 */
	private void createManifest(Path dest, List<File> images) {
		StringBuilder buf = new StringBuilder();
		String manifestEntry = "<manifest:file-entry ";
		String typeXml = "manifest:media-type=\"text/xml\"/>";
		buf.append("<manifest:manifest "
				+ "xmlns:manifest=\"urn:oasis:names:tc:opendocument:xmlns:manifest:1.0\" "
				+ "xmlns:loext=\"urn:org:documentfoundation:names:experimental:office:xmlns:loext:1.0\" "
				+ "manifest:version=\"1.3\">");
		buf.append(manifestEntry)
				.append("manifest:full-path=\"/\" "
						+ "manifest:version=\"1.3\" manifest:media-type=\"application/vnd.oasis.opendocument.text\"/>");
		buf.append(manifestEntry)
				.append("manifest:full-path=\"styles.xml\" ")
				.append(typeXml);
		File dir = new File(dest.toString() + File.separator + "Pictures");
		dir.mkdir();
		for (File f : images) {
			String name = f.getName();
			File outfile = new File(dir.getAbsolutePath() + File.separator + "name");
			IOUtil.fileCopy(f, outfile);
			buf.append(manifestEntry).append("manifest:full-path=\"Pictures/");
			buf.append(name);
			buf.append("\" manifest:media-type=\"image/");
			if (name.endsWith("jpg")) {
				buf.append("jpeg");
			} else {
				String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
				//LOG.trace("name=" + name + ", ext=" + ext);
				buf.append(ext);
			}
			buf.append("\"/>");
		}
		buf.append(manifestEntry)
				.append("manifest:full-path=\"content.xml\" ")
				.append(typeXml);
		buf.append(manifestEntry)
				.append("manifest:full-path=\"meta.xml\" ")
				.append(typeXml);
		buf.append(manifestEntry)
				.append("manifest:full-path=\"settings.xml\" ")
				.append(typeXml);
		buf.append(manifestEntry)
				.append("manifest:full-path=\"manifest.rdf\" "
						+ "manifest:media-type=\"application/rdf+xml\"/>");
		buf.append("</manifest:manifest>");
		IOUtil.fileWriteString(dest.toString() + File.separator + "META-INF" + File.separator + "manifest.xml", buf.toString());
	}

}
