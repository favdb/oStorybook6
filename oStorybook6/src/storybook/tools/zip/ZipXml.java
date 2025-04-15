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
package storybook.tools.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import storybook.tools.LOG;
import storybook.tools.ListUtil;
import storybook.tools.TextUtil;

/**
 * class for parsing a ZipXML file (for ODT or DOCX)
 *
 * @author favdb
 */
public class ZipXml {

	public static final String TT = "ZipXml.";

	private final File file;
	private ZipFile zipFile;
	public Document document;
	private Enumeration<? extends ZipEntry> entries;

	/**
	 * get content from a DOCX or ODT file as a HTML String
	 *
	 * @param file
	 * @return
	 */
	public static String getDocumentText(File file) {
		if (file.getAbsolutePath().endsWith("odt")) {
			return ZipXml.getDocumentText(file, "content.xml", "text:h, text:p");
		} else if (file.getAbsolutePath().endsWith("docx")) {
			return ZipXml.getDocumentText(file, "word/document.xml", "w:t");
		}
		return "";
	}

	/**
	 * get content from a DOCX or ODT file using specified entry and tag
	 *
	 * @param file the file to parse
	 * @param docEntry the entry to parse
	 * @param tags tags to parse, tags is a String containige list of tags
	 * @return a HTML String
	 */
	public static String getDocumentText(File file, String docEntry, String tags) {
		return getDocumentText(file, docEntry, tags.split(","));
	}

	/**
	 * get content from a DOCX or ODT file using specified entry and tag
	 *
	 * @param file the file to parse
	 * @param docEntry the entry to parse
	 * @param tags parse only these tags (may be unique)
	 * @return a HTML String
	 */
	public static String getDocumentText(File file, String docEntry, String[] tags) {
		if (!file.exists()) {
			return "";
		}
		String s = "";
		try {
			ZipXml xml = new ZipXml(file);
			ZipEntry entry = xml.open(docEntry);
			if (entry == null) {
				LOG.err(TT + "getText entry " + docEntry + " not exists");
			}
			s = xml.getTextContent(tags);
			xml.close();
		} catch (ParserConfigurationException | SAXException | IOException ex) {
			LOG.err(TT + "getText(...)", ex);
		}
		while (s.contains("  ")) {
			s = s.replace("  ", " ");
		}
		return s;
	}

	/**
	 * define the ZipXml class
	 *
	 * @param file File to intial set
	 */
	public ZipXml(File file) {
		//LOG.trace(TT + "(file=" + file.getAbsolutePath() + ")");
		this.file = file;
	}

	/**
	 * open the compressed file and initialize the entry to parse
	 *
	 * @param entryName
	 *
	 * @return the corresponding entry
	 *
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public ZipEntry open(String entryName) throws IOException, SAXException, ParserConfigurationException {
		//LOG.trace(TT + ".zipOpen(entryName=\"" + entryName + "\")");
		zipFile = new ZipFile(file);
		ZipEntry zipEntry = getEntry(entryName);
		if (zipEntry != null) {
			document = getDocument(zipEntry);
		}
		return zipEntry;
	}

	/**
	 * get the ZipEntry to parse
	 *
	 * @param entryName
	 * @return the ZipEntry
	 */
	public ZipEntry getEntry(String entryName) {
		entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.getName().equals(entryName)) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * close te compressed file
	 *
	 * @throws IOException
	 */
	public void close() throws IOException {
		//LOG.trace(TT + ".close()");
		zipFile.close();
	}

	/**
	 * get the Document to parse
	 *
	 * @param entry of the compressed file to parse
	 *
	 * @return the Document
	 *
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Document getDocument(ZipEntry entry) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputStream zinput = zipFile.getInputStream(entry);
		Document doc = documentBuilder.parse(zinput);
		if (doc == null) {
			return null;
		}
		return doc;
	}

	/**
	 * get a NodeList corresponding to a tag
	 *
	 * @param doc the Document to parse
	 * @param tag the tag to search
	 *
	 * @return the resulting NodeList
	 */
	public NodeList getNodelist(Document doc, String tag) {
		Element rootNode = doc.getDocumentElement();
		return rootNode.getElementsByTagName(tag);
	}

	/**
	 * get a NodeList corresponding to a tag from the current Document
	 *
	 * @param tag the tag to search
	 *
	 * @return the resulting NodeList
	 */
	public NodeList getNodelist(String tag) {
		Element rootNode = document.getDocumentElement();
		return rootNode.getElementsByTagName(tag);
	}

	/**
	 * get the text content of a tag
	 *
	 * @param tag to search
	 *
	 * @return
	 */
	public String getTextContent(String tag) {
		if (document == null) {
			return "";
		}
		Element rootNode = document.getDocumentElement();
		NodeList nodes = rootNode.getElementsByTagName(tag);
		if (nodes.getLength() < 1) {
			return "";
		}
		List<String> list = new ArrayList<>();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node el = nodes.item(i);
			list.add(el.getTextContent());
		}
		return ListUtil.join(list, " ");
	}

	/**
	 * get the text content of tags
	 *
	 * @param tags to search
	 *
	 * @return
	 */
	public String getTextContent(String[] tags) {
		List<String> list = new ArrayList<>();
		for (String tag : tags) {
			list.add(getTextContent(tag.trim()));
		}
		return TextUtil.normalPunctuation(ListUtil.join(list, " "));
	}

}
