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
package storybook.exim.importer;

import api.jsoup.Jsoup;
import api.jsoup.nodes.Document;
import api.jsoup.nodes.Element;
import api.jsoup.select.Elements;
import i18n.I18N;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import storybook.App;
import storybook.Const;
import storybook.Pref;
import storybook.db.book.BookParamEditor;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.exim.doc.DOCX;
import storybook.exim.doc.ODT;
import storybook.project.Project;
import storybook.tools.LOG;
import storybook.tools.StringUtil;
import storybook.tools.file.FileFilter;
import storybook.tools.file.IOUtil;
import storybook.tools.html.Html;
import storybook.ui.frames.main.MainFrame;

/**
 * class for importing a document file like DOCX, ODT or HTML
 *
 * @author favdb
 */
public class ImportDocument {

	private static final String TT = "ImportDocument.";
	private File file = null;
	private Document document;
	private Project dbFile = null;
	private MainFrame mainFrame;
	private String thetitle;
	private String separator = Const.SCENE_SEPARATOR;
	private DOCX docx = null;
	private ODT odt = null;

	public ImportDocument(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		separator = App.preferences.getString(Pref.KEY.SCENE_SEPARATOR);
	}

	public ImportDocument(MainFrame mainFrame, File file) {
		this(mainFrame);
		this.file = file;
		separator = App.preferences.getString(Pref.KEY.SCENE_SEPARATOR);
	}

	public File getFile() {
		return file;
	}

	public boolean openDocument() {
		//LOG.trace(TT + "openDocument() for file=\"" + file.getAbsolutePath() + "\"");
		if (file == null || !file.exists()) {
			LOG.trace("file " + (file == null ? "null" : file.getAbsolutePath()) + " not exists");
			return false;
		}
		String ext = IOUtil.getExtension(file);
		try {
			switch (ext.toLowerCase()) {
				case "docx":
					docx = new DOCX(file);
					if (docx.open()) {
						document = Jsoup.parse(docx.getContentAsHtml(true));
					}
					break;
				case "odt":
					odt = new ODT(file);
					if (odt.open()) {
						document = Jsoup.parse(odt.getContentAsHtml(true));
					}
					break;
				case "html":
					document = Jsoup.parse(file, "UTF-8");
					break;
				case "txt":
					document = Jsoup.parse(
							Html.textToHTML(IOUtil.fileReadAsString(file)));
					break;
				case "epub":
					return true;
				default:
					LOG.err(TT + "openDocument() unknown ext=\"" + ext + "\"");
					return false;
			}
		} catch (IOException | SAXException | ParserConfigurationException ex) {
			LOG.err(TT + "openDocument() ", ex);
			return false;
		}
		return (document != null);
	}

	/**
	 * close the document file
	 */
	public void close() {
		if (file.getAbsolutePath().endsWith(".docx") && docx != null) {
			docx.close();
		} else if (file.getAbsolutePath().endsWith(".odt") && odt != null) {
			odt.close();
		} else if (file.getAbsolutePath().endsWith(".html")) {
			//nothing
		}
	}

	/**
	 * getter for the document
	 *
	 * @return
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * ask if document is a DOCX file
	 *
	 * @return
	 */
	public boolean isDocx() {
		return docx != null;
	}

	/**
	 * ask if document is a ODT file
	 *
	 * @return
	 */
	public boolean isOdt() {
		return odt != null;
	}

	/**
	 * get the document content as plain text
	 *
	 * @return
	 */
	public String getContentAsTxt() {
		//LOG.trace(TT + "getContentAsTxt()");
		return document.body().text();
	}

	/**
	 * get all element from document
	 *
	 * @param session
	 */
	public void getAll(Project session) {
		//LOG.trace(TT + "getAll()");
		Strand strand = (Strand) mainFrame.project.strands.findOrderBySort().get(0);
		Element bd = document.body();
		Elements docNodes = document.body().getAllElements();
		Part part = (Part) mainFrame.project.parts.getFirst();
		Chapter chapter = (Chapter) mainFrame.project.chapters.getFirst();
		@SuppressWarnings("null")
		int nPart = part != null ? part.getNumber() : 1;
		@SuppressWarnings("null")
		int nChapter = chapter != null ? chapter.getChapterno() : 1;
		List<String> buffer = new ArrayList<>();
		for (Element node : docNodes) {
			String nodeHtml = node.html();
			String nodeTxt = node.text();
			switch (node.nodeName()) {
				case "h1":
					if (!buffer.isEmpty()) {
						getScenes(session, strand, chapter, buffer);
					}
					part = getPart(node, nPart++);
					session.write(part);
					buffer = new ArrayList<>();
					break;
				case "h2":
					if (!buffer.isEmpty()) {
						getScenes(session, strand, chapter, buffer);
					}
					if (part == null) {
						part = new Part();
						part.setName(I18N.getMsg("part") + nPart);
						part.setNumber(nPart++);
						session.write(part);
					}
					chapter = getChapter(part, node, nChapter++);
					session.write(chapter);
					buffer = new ArrayList<>();
					break;
				case "h3":
				case "h4":
				case "h5":
				case "h6":
					if (buffer.toString().length() + nodeTxt.length() > max) {
						getScenes(session, strand, chapter, buffer);
						buffer = new ArrayList<>();
					}
					int n = Integer.parseInt(node.nodeName().substring(1));
					if (n < 2 || n > 6) {
						n = 3;
					}
					buffer.add(Html.intoH(n, nodeTxt));
					break;
				case "ul":
					if (buffer.toString().length() + nodeHtml.length() > max) {
						getScenes(session, strand, chapter, buffer);
						buffer = new ArrayList<>();
					}
					buffer.add(Html.intoTag("ul", nodeHtml));
					break;
				case "ol":
					if (buffer.toString().length() + nodeHtml.length() > max) {
						getScenes(session, strand, chapter, buffer);
						buffer = new ArrayList<>();
					}
					buffer.add(Html.intoTag("ol", nodeHtml));
					break;
				case "p":
					if (node.parent().equals(bd)) {
						buffer.add(nodeHtml);
					}
					break;
				default:
					break;
			}
		}
		if (!buffer.isEmpty()) {
			getScenes(session, strand, chapter, buffer);
		}
	}

	/**
	 * get the Part
	 *
	 * @param el
	 * @param n
	 * @return
	 */
	private Part getPart(Element el, int n) {
		//LOG.trace(TT + "getPart(el, n=" + n + ")");
		Part part = new Part();
		part.setName(el.text());
		part.setNumber(n);
		return part;
	}

	/**
	 * get the Chapter
	 *
	 * @param part
	 * @param el
	 * @param n
	 * @return
	 */
	private Chapter getChapter(Part part, Element el, int n) {
		//LOG.trace(TT + "getScene(part=" + part.getName() + ", elChapter, n=" + n + ")");
		Chapter chapter = new Chapter();
		chapter.setTitle(el.text());
		chapter.setChapterno(n);
		chapter.setPart(part);
		return chapter;
	}

	int numScene = 1;
	int max = 30000;

	/**
	 * get the scenes
	 *
	 * @param session
	 * @param strand
	 * @param chapter
	 * @param texts
	 * @return
	 */
	private int getScenes(Project session, Strand strand, Chapter chapter, List<String> texts) {
		/*LOG.trace(TT + "getScenes(session,"
				+ " strand=" + strand.getName()
				+ ", chapter=" + chapter.getName()
				+ "texts nb=" + texts.size() + ")");*/
		StringBuilder b = new StringBuilder();
		int ns = 1;
		for (String p : texts) {
			String ps = Html.intoP(p);
			if (p.replace(" ", "").equals(separator)) {
				if (!b.toString().isEmpty()) {
					ns = getSceneText(session, strand, chapter, ns, b.toString());
				}
				b = new StringBuilder();
			} else {
				if (b.toString().length() > max) {
					ns = getSceneText(session, strand, chapter, ns, b.toString());
					b = new StringBuilder();
				}
				b.append(ps);
			}
		}
		if (!b.toString().isEmpty()) {
			ns = getSceneText(session, strand, chapter, ns, b.toString());
		}
		return ns;
	}

	/**
	 * get the scene text
	 *
	 * @param session
	 * @param strand
	 * @param chapter
	 * @param n
	 * @param text
	 * @return
	 */
	private int getSceneText(Project session, Strand strand, Chapter chapter, int n, String text) {
		int nScene = n;
		String t = text;
		while (t.length() > max) {
			Scene scene = getScene(chapter, nScene++, t.substring(0, max));
			scene.setStrand(strand);
			session.write(scene);
			t = t.substring(max);
		}
		if (t.length() > 0) {
			Scene scene = getScene(chapter, nScene++, t);
			scene.setStrand(strand);
			session.write(scene);
		}
		return nScene;
	}

	/**
	 * get the Scene
	 *
	 * @param chapter
	 * @param n
	 * @param text
	 * @return
	 */
	public Scene getScene(Chapter chapter, int n, String text) {
		/*LOG.trace(TT + "getScene(chapter=" + chapter.getName()
				+ ", n=" + n
				+ ", text len=" + text.length() + ")");*/
		Scene scene = new Scene();
		scene.setChapter(chapter);
		scene.setSceneno(numScene++);
		if (chapter == null) {
			scene.setTitle(String.format("XX.%02d", n));
		} else {
			scene.setTitle(String.format("%02d.%02d", chapter.getChapterno(), n));
		}
		scene.setSummary(text.replaceAll("<!--  .*-->", ""));
		return scene;
	}

	/**
	 * set the file
	 *
	 * @param file
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * select the file
	 *
	 * @return
	 */
	public File selectFile() {
		//LOG.trace(TT + "selectFile()");
		final JFileChooser fc = new JFileChooser();
		//fc.setCurrentDirectory(new File(file.getAbsolutePath()));
		FileFilter filter = new FileFilter("book");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		int ret = fc.showOpenDialog(null);
		if (ret == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			if (!f.exists()) {
				JOptionPane.showMessageDialog(null,
						I18N.getMsg("project.not.exist.text", f),
						I18N.getMsg("project.not.exist.title"),
						JOptionPane.ERROR_MESSAGE);
				return null;
			}
			this.file = f;
			return f;
		}
		return null;
	}

	/**
	 * get the title
	 *
	 * @return
	 */
	public String getTitle() {
		return thetitle;
	}

	/**
	 * open the DB file
	 *
	 * @return
	 */
	public boolean openDB() {
		//LOG.trace(TT + "openDB()");
		dbFile = null;
		thetitle = JOptionPane.showInputDialog(I18N.getMsg("title"));
		if (thetitle == null) {
			return false;
		}
		if (thetitle.isEmpty()) {
			LOG.trace(TT + "openDB() properties title empty, file import ignored");
			return false;
		}
		String filename = App.getFileName(StringUtil.escapeTxt(thetitle));
		if (filename.isEmpty()) {
			return false;
		}
		if (filename.endsWith(".mv.db")) {
			filename = filename.replace(".mv.db", "");
		}
		if (filename.endsWith(Const.STORYBOOK.FILE_EXT_OSBK.toString())) {
			filename = filename.replace(Const.STORYBOOK.FILE_EXT_OSBK.toString(), "");
		}
		File fOsbk = new File(filename + Const.STORYBOOK.FILE_EXT_OSBK.toString());
		File fMvdb = new File(filename + ".mv.db");
		if (fOsbk.exists() || fMvdb.exists()) {
			int ret = JOptionPane.showConfirmDialog(null,
					I18N.getMsg("file.exists", filename),
					I18N.getMsg("file.save.overwrite.title"),
					JOptionPane.YES_NO_OPTION);
			if (ret == JOptionPane.NO_OPTION) {
				return false;
			}
			IOUtil.fileDelete(fOsbk);
			IOUtil.fileDelete(fMvdb);
		}
		dbFile = new Project(filename + ".xml");
		return (dbFile.getFile() != null && document != null);
	}

	/**
	 * get the current DB file
	 *
	 * @return
	 */
	public Project getProject() {
		return mainFrame.project;
	}

	/**
	 * set the external editor corresponding to the document file
	 *
	 * @param session
	 */
	public void setExternalEditor(Project session) {
		String ext = "odt", name = "LibreOffice Writer", template = "Modele.odt";
		if (isDocx()) {
			ext = "docx";
			name = "MS Word";
			template = "Modele.docx";
		}
		BookParamEditor p = session.book.param.getParamEditor();
		p.setExtension(ext);
		p.setName(name);
		p.setTemplate(template);
	}

}
