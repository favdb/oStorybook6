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
package storybook.exim.doc;

import api.jsoup.Jsoup;
import api.jsoup.nodes.Document;
import api.jsoup.nodes.Element;
import api.jsoup.select.Elements;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.project.Project;
import storybook.tools.LOG;
import storybook.tools.xml.Xml;

/**
 *
 * @author favdb
 */
public class EPUB {

	private static final String TT = "EPUB",
	   EXTS = "html,xhtml,css,opf,ncx";

	private String mainDirectory = "/OPS/";
	private final File epubFile;
	private String title = "", description = "", author = "", uuid = "", isbn = "";
	private final List<String> texts = new ArrayList<>(), imgs = new ArrayList<>();

	/**
	 * check if the file is a correct EPUB
	 *
	 * @param infile: the file to check
	 * @return true if OK
	 */
	public static boolean check(File infile) {
		return infile.exists();
	}

	public EPUB(File infile) {
		epubFile = infile;
	}

	/**
	 * set the title of the book
	 *
	 * @param value
	 */
	public void setTitle(String value) {
		this.title = value;
	}

	/**
	 * get the title of the book
	 *
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * set the author of the book
	 *
	 * @param value
	 */
	public void setAuthor(String value) {
		this.author = value;
	}

	/**
	 * get the author of the book
	 *
	 * @return
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * set the description of the book
	 *
	 * @param value
	 */
	public void setDescription(String value) {
		this.description = value;
	}

	/**
	 * get the description of the book
	 *
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * set the UUID of the book
	 *
	 * @param value
	 */
	public void setUUID(String value) {
		this.uuid = value;
	}

	/**
	 * get the UUID of the book
	 *
	 * @return
	 */
	public String getUUID() {
		return uuid;
	}

	/**
	 * set the ISBN of the book
	 *
	 * @param value
	 */
	public void setISBN(String value) {
		this.isbn = value;
	}

	/**
	 * get the ISBN of the book
	 *
	 * @return
	 */
	public String getISBN() {
		return isbn;
	}

	/**
	 * get the text files list
	 *
	 * @return the String list
	 */
	public List<String> getTextFiles() {
		return texts;
	}

	/**
	 * get the image files list
	 *
	 * @return the String list
	 */
	public List<String> getImgFiles() {
		return imgs;
	}

	/**
	 * print the resulting content.opf for tracing
	 */
	public void printOpf() {
		LOG.trace("title", title);
		LOG.trace("author", author);
		LOG.trace("isbn", isbn);
		LOG.trace("uuid", uuid);
		LOG.trace("description", description);
		LOG.trace("text files:");
		for (String f : texts) {
			LOG.trace("  - ", f);
		}
		LOG.trace("image files:");
		if (imgs.isEmpty()) {
			LOG.trace(" empty");
		} else {
			for (String f : imgs) {
				LOG.trace("  - ", f);
			}
		}
	}

	/**
	 * open the EPUB and loadthe book informations from content.opf
	 *
	 * @return true if OK
	 */
	public boolean open() {
		LOG.trace(TT + "readOPF() infile=" + epubFile);
		boolean rc = false;
		try (ZipFile zipFile = new ZipFile(URLDecoder.decode(epubFile.getAbsolutePath(), "UTF-8"))) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();
				if (zipEntry.isDirectory()) {
					mainDirectory = "/" + zipEntry.getName() + "/";
				} else if (zipEntry.toString().endsWith(".opf")) {
					InputStream inputStream = zipFile.getInputStream(zipEntry);
					Xml xml = new Xml(inputStream);
					if (!xml.open()) {
						return false;
					}
					Node metadata = xml.getNode("metadata");
					if (metadata == null) {
						return false;
					}
					title = xml.getChildValue(metadata, "dc:title");
					author = xml.getChildValue(metadata, "dc:creator", "opf:role=\"aut\"");
					uuid = xml.getChildValue(metadata, "dc:identifier", "opf:scheme=\"uuid\"");
					isbn = xml.getChildValue(metadata, "dc:identifier", "opf:scheme=\"isbn\"");
					description = xml.getChildValue(metadata, "dc:description");
					Node manifest = xml.getNode("manifest");
					if (manifest == null) {
						return false;
					}
					NodeList items = manifest.getChildNodes();
					if (items.getLength() > 0) {
						for (int i = 0; i < items.getLength(); i++) {
							Node item = items.item(i);
							if (Xml.attributeGet(item, "media-type").equals("application/xhtml+xml")) {
								texts.add(Xml.attributeGet(item, "href"));
							}
						}
					}
					rc = true;
				}
			}
		} catch (IOException ex) {
			LOG.err(TT + ".readOPF error", ex);
		}
		return rc;
	}

	/**
	 * read the content of a zipped file as a chapter and a scene
	 *
	 * @param project
	 * @param wDir: the working directory
	 * @param item: the file name to extract
	 * @return
	 */
	public String readChapter(Project project, String wDir, String item) {
		LOG.trace(TT + ".readChapter(mainFrame, item=" + item + ")");
		String rc = "";
		try {
			//create the chapter
			Chapter chapter = new Chapter();
			chapter.setPart((Part) project.parts.getFirst());
			chapter.setChapterno(project.chapters.getLastNumber() + 1);
			chapter.setTitle(chapter.getDefaultName());
			project.chapters.add(chapter);
			//create the scene
			Document html = Jsoup.parse(new File(wDir + mainDirectory + item), "utf-8");
			String text = html.body().outerHtml();
			createScene(project, text);
			return rc;
		} catch (IOException ex) {
			LOG.err(TT + ".readChapter error", ex);
			return "";
		}
	}

	/**
	 * create a Scene
	 *
	 * @param mainFrame
	 * @param text
	 */
	private void createScene(Project project, String text) {
		LOG.trace(TT + ".createScene(mainFrame, text.len=" + text.length() + ")");
		Strand strand = (Strand) project.strands.getFirst();
		Chapter chapter = (Chapter) project.chapters.getLastChapter();
		int no = project.scenes.getLastNumber() + 1;
		Scene scene = new Scene();
		scene.setChapter(chapter);
		scene.setSceneno(no);
		scene.setTitle(String.format("%02d.%02d", chapter.getChapterno(), no));
		scene.setStrand(strand);
		String t = text;
		if (text.contains("<img")) {
			Document doc = Jsoup.parse(text);
			Elements elems = doc.getElementsByTag("img");
			for (int i = 0; i < elems.size(); i++) {
				Element el = elems.get(i);
				String src = el.attr("src");
				if (src.toLowerCase().endsWith(".png")) {
					src = src.replace(".png", ".jpeg").replace(".PNG", ".jpeg");
					el.attr("src", src);
				}
				if (src.toLowerCase().endsWith(".png")) {
					src = src.replace(".bmp", ".jpeg").replace(".BMP", ".jpeg");
					el.attr("src", src);
				}
				File f = new File(src);
				src = "./Images/" + f.getName();
				el.attr("img", src);
			}
			t = doc.body().outerHtml();
		}
		scene.setSummary(t);
		project.scenes.add(scene);
	}

}
