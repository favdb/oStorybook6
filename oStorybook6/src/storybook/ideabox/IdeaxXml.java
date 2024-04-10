/*
 * Copyright (C) 2023 favdb
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
package storybook.ideabox;

import i18n.I18N;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import storybook.db.idea.Idea;
import storybook.tools.LOG;
import storybook.tools.file.EnvUtil;
import storybook.tools.file.IOUtil;
import storybook.tools.xml.Xml;

/**
 * class to access the ideabox XML file
 *
 * @author favdb
 */
public class IdeaxXml {

	private static final String TT = "IdeaxXml",
			ER_COPY_UNABLE = "ideabox.copy_unable",
			ER_NOT_FIND = I18N.getMsg("ideabox.not_find"),
			ER_NOT_OPENED = I18N.getMsg("ideabox.not_opened"),
			X_IDEA = "idea",
			X_IDEABOX = "ideabox",
			X_UUID = "uuid";

	public Xml xml = null;
	public List<Idea> ideas = new ArrayList<>();

	public IdeaxXml() {
		initialize();
	}

	/**
	 * open the ideabox XML file
	 */
	private void initialize() {
		//LOG.trace(TT + ".initialize()");
		if (xml != null && xml.isOpened()) {
			xml.close();
		}
		xml = null;
		File file = EnvUtil.getIdeaboxFile();
		if (!file.exists()) {
			fileCreate();
		}
		fileLoad();
	}

	/**
	 * load the ideabox XML file
	 */
	public void fileLoad() {
		//LOG.trace(TT + ".fileLoad() for " + EnvUtil.getIdeaboxFile().getAbsolutePath());
		try {
			ideas = new ArrayList<>();
			xml = new Xml(EnvUtil.getIdeaboxFile());
			if (!xml.open()) {
				LOG.err(ER_NOT_OPENED);
				return;
			}
			NodeList elems = xml.getRoot().getElementsByTagName(X_IDEA);
			for (int i = 0; i < elems.getLength(); i++) {
				Idea idea = new Idea(elems.item(i));
				ideas.add(idea);
			}
		} catch (FileNotFoundException ex) {
			LOG.err(ER_NOT_OPENED, ex);
		}
	}

	/**
	 * create an empty ideabox XML file
	 */
	private void fileCreate() {
		//LOG.trace(TT + ".fileCreate()");
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n")
				.append("<ideabox>\n</ideabox>");
		IOUtil.fileWriteString(EnvUtil.getIdeaboxFile(), b.toString());
	}

	/**
	 * save the ideabox XML file
	 */
	public void fileSave() {
		//LOG.trace(TT + ".fileSave() for " + ideas.size() + " ideas");
		Element root = xml.getRoot();
		while (root.hasChildNodes()) {
			while (root.getFirstChild().hasChildNodes()) {
				Node el = root.getFirstChild();
				el.removeChild(el.getFirstChild());
			}
			root.removeChild(root.getFirstChild());
		}
		int nb = 0;
		for (Idea idea : ideas) {
			Element el = idea.toXml(xml.getDocument(), null);
			root.appendChild(el);
		}
		Transformer tf;
		try {
			tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			tf.setOutputProperty(OutputKeys.METHOD, "xml");
			tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource domSource = new DOMSource(xml.getDocument());
			StreamResult sr = new StreamResult(EnvUtil.getIdeaboxFile());
			tf.transform(domSource, sr);
		} catch (TransformerConfigurationException ex) {
			LOG.err(TT + ".fileSave() error", ex);
		} catch (TransformerException ex) {
			LOG.err(TT + ".fileSave() error", ex);
		}
	}

	/**
	 * close the ideabox XML file
	 */
	public void close() {
		if (xml != null && xml.isOpened()) {
			xml.close();
		}
	}

	/**
	 * find an Idea index by the UUID in the ideas list
	 *
	 * @param uuid to find, may not be null and not empty
	 * @return
	 */
	private int ideaFindByUUID(String uuid) {
		if (uuid != null && !uuid.isEmpty()) {
			for (int i = 0; i < ideas.size(); i++) {
				if (ideas.get(i).getUuid().equals(uuid)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * update an Idea in the opened ideabox XML document
	 *
	 * @param idea may not be null
	 * @return true if OK
	 */
	public boolean ideaUpdate(Idea idea) {
		//LOG.trace(TT + ".ideaUpdate(idea='" + idea.getName() + "')");
		if (idea == null) {
			return false;
		}
		if (xml == null || xml.getDocument() == null) {
			//LOG.err(TT + ".ideaUpdate(idea) xml=null || xml.getDocument()=null");
			return false;
		}
		int n = ideaFindByUUID(idea.getUuid());
		if (n < 0) {
			return false;
		}
		ideas.set(n, idea);
		return true;
	}

	/**
	 * add an Idea in the ideabox XML document
	 *
	 * @param idea
	 * @return : true if OK
	 */
	public boolean ideaAdd(Idea idea) {
		//LOG.trace(TT + ".ideaAdd(idea=" + LOG.trace(idea) + ")");
		if (idea == null) {
			return false;
		}
		idea.setUuid();
		ideas.add(idea);
		fileSave();
		return true;
	}

	/**
	 * delete an Idea in the ideabox XML document
	 *
	 * @param idea
	 * @return true if delete is OK
	 */
	public boolean ideaDelete(Idea idea) {
		//LOG.trace(TT + ".ideaDelete(idea='" + LOG.trace(idea) + "')");
		if (idea == null) {
			return false;
		}
		for (int i = 0; i < ideas.size(); i++) {
			if (ideas.get(i).getUuid().equals(idea.getUuid())) {
				ideas.remove(i);
				fileSave();
				fileLoad();
				return true;
			}
		}
		LOG.err("idea to delete not find");
		return false;
	}

	/**
	 * remove an Idea from the XML Document
	 *
	 * @param idea to remove
	 */
	private void ideaRemove(Idea idea) {
		//LOG.trace(TT+".ideaRemove(idea="+LOG.trace(idea));
		if (idea == null) {
			return;
		}
		try {
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile("//idea[@uuid=\"" + idea.getUuid() + "\"]");
			NodeList nodeList = (NodeList) expr.evaluate(xml.getDocument(), XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element el = (Element) nodeList.item(i);
				if (el.getAttribute("uuid").equals(idea.getUuid())) {
					while (el.hasChildNodes()) {
						el.removeChild(el.getFirstChild());
					}
					xml.getRoot().removeChild(el);
					break;
				}
			}
		} catch (XPathExpressionException ex) {
			LOG.err("ideaRemove error for idea=" + LOG.trace(idea), ex);
		}
	}

	public int findUUID(String uuid) {
		for (int i = 0; i < ideas.size(); i++) {
			if (ideas.get(i).getUuid().equals(uuid)) {
				return i;
			}
		}
		return -1;
	}

}
