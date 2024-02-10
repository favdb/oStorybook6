/*
 * Copyright (C) 2016 favdb
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
package storybook.exim.importer;

import i18n.I18N;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import storybook.ctrl.Ctrl;
import storybook.db.abs.AbstractEntity;
import storybook.dialog.ExceptionDlg;
import storybook.model.Model;
import storybook.ui.MainFrame;

/**
 * Importer class
 *
 * @author favdb
 */
public class Importer {

	private static final String TT = "Importer";

	public String fileName;
	private final String fileExtension;
	private boolean fileOpened = false;
	private Document document;
	public String rootName;
	public Element rootNode;
	private DocumentBuilder documentBuilder;
	Model bookModel;
	private final MainFrame mainFrame;
	private Ctrl bookCtrl;
	private String msgCheck;

	public Importer(MainFrame m, File xml) {
		this(m, xml.getAbsolutePath());
	}

	public Importer(MainFrame m, String n) {
		mainFrame = m;
		fileName = n;
		fileExtension = fileName.substring(fileName.lastIndexOf("."));
		bookCtrl = mainFrame.getBookController();
	}

	/**
	 * exec import
	 *
	 * @param xml
	 * @return true if import is OK
	 */
	public boolean exec(String xml) {
		return false;
	}

	/**
	 * open import file
	 *
	 * @return true if open is OK
	 */
	public boolean open() {
		//LOG.printInfos(TT+".open()");
		return fileName.endsWith(".xml") ? openXml() : openSb();
	}

	/**
	 * check if file is opened
	 *
	 * @return
	 */
	public boolean isOpened() {
		return fileOpened;
	}

	public String getType() {
		return (fileExtension);
	}

	/**
	 * check if file is XML
	 *
	 * @return
	 */
	public boolean isXml() {
		return fileExtension.equals(".xml");
	}

	/**
	 * close import file
	 */
	public void close() {
		//App.printInfos(TT+".close()");
		if (fileName.endsWith(".xml")) {
			closeXml();
		} else {
			closeSb();
		}
		if (bookModel != null) {
			mainFrame.project.attributes.deleteOrphans();
		}
	}

	/**
	 * open XML file
	 *
	 * @return true if open is ok
	 */
	private boolean openXml() {
		//LOG.printInfos(TT+".openXml()");
		fileOpened = false;
		documentBuilder = null;
		try {
			DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
			df.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			df.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			documentBuilder = df.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			ExceptionDlg.show(this.getClass().getSimpleName()
			   + ".openXml() DocumentBuilder error:\n" + ex.getLocalizedMessage(), ex);
			return false;
		}
		document = readDom();
		if (document == null) {
			rootNode = null;
			return false;
		}
		rootNode = document.getDocumentElement();
		Element n = (Element) rootNode.getElementsByTagName("book").item(0);
		if (n != null) {
			rootName = n.getFirstChild().getNodeName();
		}
		fileOpened = true;
		return true;
	}

	/**
	 * read document
	 *
	 * @return
	 */
	public Document readDom() {
		//LOG.printInfos(TT+".initDom()");
		Document rc = null;
		try {
			rc = documentBuilder.parse(new File(fileName));
		} catch (SAXException e) {
			ExceptionDlg.show(this.getClass().getSimpleName()
			   + ".readDom() Parsing error for " + fileName, e);
		} catch (IOException e) {
			ExceptionDlg.show(this.getClass().getSimpleName()
			   + ".readDom() I/O error for " + fileName, e);
		}
		return (rc);
	}

	/**
	 * close XMLfile
	 *
	 */
	public void closeXml() {
		//LOG.printInfos(TT+".close()");
		if (fileOpened) {
			fileOpened = false;
			document = null;
			documentBuilder = null;
		}
	}

	/**
	 * open import file
	 *
	 * @return
	 */
	private boolean openSb() {
		//App.printInfos(TT+".openSb()");
		boolean rc = false;
		/*if (rc) {
			fileOpened = true;
		}*/
		return (rc);
	}

	/**
	 * close import file
	 */
	private void closeSb() {
		//LOG.printInfos("Importer.closeSb()");
		if (isOpened()) {

		}
		fileOpened = false;
	}

	/**
	 * return true if entity exists and not force
	 *
	 * @param entity
	 * @param force
	 * @return
	 *
	 */
	public boolean write(ImportEntity entity, boolean force) {
		//LOG.printInfos(TT+".write("+EntityUtil.getEntityName(entity.entity)+") "+(force?"force write":""));
		AbstractEntity n;
		AbstractEntity old = null;
		if (check(entity)) {
			if (!force) {
				return true;
			}
			old = mainFrame.project.get(entity.entity.getObjType(), entity.entity.getId());
		}
		n = ImportUtil.updateEntity(mainFrame, entity, old);
		if (old == null) {
			bookCtrl.newEntity(n);
		} else {
			bookCtrl.updateEntity(n);
		}
		return false;
	}

	public boolean writeAll(List<ImportEntity> entities, boolean force) {
		//LOG.printInfos(TT+".writeAll(entities nb="+entities.size()+(force?",force":"unforced")+") ");
		if (entities.isEmpty()) {
			return (false);
		}
		boolean rc = false;
		if (bookModel == null) {
			bookModel = mainFrame.getBookModel();
		}
		bookCtrl = mainFrame.getBookController();
		for (ImportEntity entity : entities) {
			if (write(entity, force)) {
				JOptionPane.showMessageDialog(null,
				   I18N.getMsg("import.dlg.notok", entity.entity.getName() + " : " + entity.entity.getName()),
				   I18N.getMsg("import"),
				   JOptionPane.ERROR_MESSAGE);
				rc = true;
				break;
			}
		}
		return (rc);
	}

	/**
	 * check if the entity exists already, return String if yes
	 *
	 *
	 */
	boolean check(ImportEntity entity) {
		//LOG.printInfos(TT+".check("+EntityUtil.getEntityName(entity.entity)+")");
		boolean rc = false;
		if (mainFrame.project.get(entity.entity.getObjType(), entity.entity.getId()) != null) {
			rc = true;
		}
		return (rc);
	}

	public String checkAll(List<ImportEntity> entities) {
		//LOG.printInfos(TT+".checkAll(entities nb="+entities.size()+")");
		if (entities.isEmpty()) {
			return ("");
		}
		msgCheck = "";
		for (ImportEntity entity : entities) {
			boolean msg = check(entity);
			if (msg) {
				msgCheck += entity.entity.getName() + ", ";
			}
		}
		if (!msgCheck.isEmpty()) {
			msgCheck = msgCheck.substring(0, msgCheck.length() - 2);
		}
		return (msgCheck);
	}

	public List<ImportEntity> list(String tobj) {
		return ImportUtil.list(mainFrame.project, this, tobj);
	}

}
