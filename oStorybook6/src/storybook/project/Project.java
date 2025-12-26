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
package storybook.project;

import assistant.Assistant;
import i18n.I18N;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import storybook.App;
import storybook.Const;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import storybook.db.attribute.Attribute;
import storybook.db.attribute.Attributes;
import storybook.db.book.Book;
import static storybook.db.book.Book.TYPE.*;
import storybook.db.book.BookUtil;
import storybook.db.category.Category;
import storybook.db.category.Categorys;
import storybook.db.challenge.Challenge;
import storybook.db.chapter.Chapter;
import storybook.db.chapter.Chapters;
import storybook.db.endnote.Endnote;
import storybook.db.endnote.Endnotes;
import storybook.db.episode.Episode;
import storybook.db.episode.Episodes;
import storybook.db.event.Event;
import storybook.db.event.Events;
import storybook.db.gender.Gender;
import storybook.db.gender.Genders;
import storybook.db.idea.Idea;
import storybook.db.idea.Ideas;
import storybook.db.item.Item;
import storybook.db.item.Items;
import storybook.db.location.Location;
import storybook.db.location.Locations;
import storybook.db.memo.Memo;
import storybook.db.memo.Memos;
import storybook.db.part.Part;
import storybook.db.part.Parts;
import storybook.db.person.Person;
import storybook.db.person.Persons;
import storybook.db.plot.Plot;
import storybook.db.plot.Plots;
import storybook.db.relation.Relation;
import storybook.db.relation.Relations;
import storybook.db.scene.Scene;
import storybook.db.scene.Scenes;
import storybook.db.status.Status;
import storybook.db.status.Statuss;
import storybook.db.strand.Strand;
import storybook.db.strand.Strands;
import storybook.db.tag.Tag;
import storybook.db.tag.Tags;
import storybook.dialog.ChooseFolderDlg;
import storybook.dialog.ExceptionDlg;
import storybook.exim.importer.ImportSQL;
import storybook.tools.DateUtil;
import storybook.tools.LOG;
import storybook.tools.ListUtil;
import storybook.tools.SbDuration;
import storybook.tools.file.IOUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.ColorUtil;
import storybook.tools.xml.Xml;
import storybook.tools.xml.XmlCleaner;
import storybook.tools.zip.ZipUtil;
import storybook.tools.zip.ZipXml;
import storybook.ui.MainFrame;

/**
 * class for a Project
 *
 * @author favdb
 */
public class Project {

	private static final String TT = "Project.";

	public Book book;
	public Attributes attributes;
	public Categorys categorys;
	public Chapters chapters;
	public Endnotes endnotes;
	public Episodes episodes;
	public Events events;
	public Genders genders;
	public Ideas ideas;
	public Items items;
	public Locations locations;
	public Memos memos;
	public Parts parts;
	public Persons persons;
	public Plots plots;
	public Relations relations;
	public Scenes scenes;
	public Statuss statuss;
	public Strands strands;
	public Tags tags;
	public Challenge challenge = new Challenge();
	private boolean fileOpened;
	private DocumentBuilder documentBuilder;
	private Document document;
	public Element rootNode;
	private File file;
	private String path, name, imageDir;
	private boolean typist;

	/**
	 * empty Project
	 */
	public Project() {
		// empty
	}

	/**
	 * Project for the given filename
	 *
	 * @param filename
	 */
	public Project(String filename) {
		this(new File(filename));
	}

	/**
	 * Project for the given file
	 *
	 * @param file
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Project(File file) {
		this.file = file;
		init();
		initData();
		loadData();
	}

	/**
	 * create a new Project
	 *
	 * @param file
	 * @param dlg
	 */
	@SuppressWarnings({"OverridableMethodCallInConstructor", "unchecked"})
	public Project(File file, ProjectNewDlg dlg) {
		setFile(file);
		initData();
		book.info.titleSet(dlg.getTitle());
		book.info.natureSet(dlg.getNature());
		initDefaultEntities();
		Strand strand = (Strand) strands.getList().get(0);
		//initialize parts
		Date objective = dlg.getObjective();
		for (int i = 1; i <= dlg.getNbParts(); i++) {
			Part part = new Part(i, String.format("%s %d", I18N.getMsg("part"), i));
			if (objective != null) {
				part.setObjectiveChars(BookUtil.computeObjectiveChars(dlg.getNature(), dlg.getNbParts()));
				part.setObjectiveTime(DateUtil.addDateTimeToTS(objective, null));
			}
			parts.add(part);
		}
		// initialize chapters
		Part defPart = (Part) parts.getList().get(0);
		for (int i = 1; i <= dlg.getNbChapters(); i++) {
			Chapter chapter = new Chapter(defPart, i, String.format("%s %d", I18N.getMsg("chapter"), i));
			if (objective != null) {
				chapter.setObjectiveChars(BookUtil.computeObjectiveChars(dlg.getNature(), dlg.getNbChapters()));
				chapter.setObjectiveTime(DateUtil.addDateTimeToTS(objective, null));
			}
			chapters.add(chapter);
		}
		// scenes
		if (dlg.getNbScenes() > 0) {
			Long id = 1L;
			for (Chapter chapter : (List<Chapter>) chapters.getList()) {
				for (int i = 1; i < dlg.getNbScenes(); i++) {
					scenes.add(Scenes.create(id++, (int) i, strand, chapter));
				}
			}

		} else {
			Chapter chapter = (Chapter) chapters.getList().get(0);
			scenes.add(Scenes.create(scenes.getLast() + 1L, 1, strand, chapter));
		}
		save();
		LOG.trace(file.getAbsolutePath() + " was created");
	}

	/**
	 * initialize the default entities
	 */
	public void initDefaultEntities() {
		//initialize the default Strand
		Strand strand = new Strand(1,
				I18N.getMsg("strand.name.init_value"), I18N.getMsg("strand.abbr.init_value"),
				ColorUtil.PALETTE.LIGHT_BLUE.getRGB(), "");
		strands.add(strand);
		//initialize default Categories
		categorys.add(new Category(1, I18N.getMsg("category.central_character"), null));
		categorys.add(new Category(2, I18N.getMsg("category.minor_character"), null));
		//initialize default Genders
		genders.add(new Gender(I18N.getMsg("person.gender.male"), 6, 12, 18, 65));
		genders.add(new Gender(I18N.getMsg("person.gender.female"), 6, 12, 18, 65));
	}

	/**
	 * the the filename
	 *
	 * @return
	 */
	public String getFilename() {
		return (file == null ? "empty file name" : file.getAbsolutePath());
	}

	/**
	 * initalize the class
	 */
	private void init() {
		//LOG.trace(TT + "init()");
		if (file != null) {
			name = file.getName();
			path = file.getParent();
		}
	}

	/**
	 * initalize default arrays data
	 */
	public void initData() {
		//LOG.trace(TT + "initData()");
		attributes = new Attributes(this);
		categorys = new Categorys(this);
		chapters = new Chapters(this);
		endnotes = new Endnotes(this);
		episodes = new Episodes(this);
		events = new Events(this);
		genders = new Genders(this);
		ideas = new Ideas(this);
		items = new Items(this);
		locations = new Locations(this);
		memos = new Memos(this);
		parts = new Parts(this);
		persons = new Persons(this);
		plots = new Plots(this);
		relations = new Relations(this);
		scenes = new Scenes(this);
		statuss = new Statuss(this);
		strands = new Strands(this);
		tags = new Tags(this);
		book = new Book(this);
	}

	/**
	 * load the data
	 */
	private void loadData() {
		if (file != null) {
			if (open()) {
				load();
			}
		}
	}

	/**
	 * get an Entity of the given type for the given ID
	 *
	 * @param type
	 * @param id
	 * @return null if not find
	 */
	public AbstractEntity get(Book.TYPE type, Long id) {
		switch (type) {
			case ATTRIBUTE:
				return attributes.get(id);
			case CATEGORY:
				return categorys.get(id);
			case CHAPTER:
				return chapters.get(id);
			case ENDNOTE:
				return endnotes.get(id);
			case EPISODE:
				return episodes.get(id);
			case EVENT:
				return events.get(id);
			case GENDER:
				return genders.get(id);
			case IDEA:
				return ideas.get(id);
			case ITEM:
				return items.get(id);
			case LOCATION:
				return locations.get(id);
			case MEMO:
				return memos.get(id);
			case PART:
				return parts.get(id);
			case PERSON:
				return persons.get(id);
			case PLOT:
				return plots.get(id);
			case RELATION:
				return relations.get(id);
			case SCENE:
				return scenes.get(id);
			case STATUS:
				return statuss.get(id);
			case STRANDS:
				return strands.get(id);
			case TAG:
				return tags.get(id);
		}
		return null;
	}

	/**
	 * find an entity of the given type for the given name
	 *
	 * @param type
	 * @param text name to search
	 * @return null if not find
	 */
	public AbstractEntity findByName(Book.TYPE type, String text) {
		switch (type) {
			case ATTRIBUTE:
				return findByName(attributes, text);
			case CATEGORY:
				return findByName(categorys, text);
			case CHAPTER:
				return findByName(chapters, text);
			case ENDNOTE:
				return findByName(endnotes, text);
			case EPISODE:
				return findByName(episodes, text);
			case EVENT:
				return findByName(events, text);
			case GENDER:
				return findByName(genders, text);
			case IDEA:
				return findByName(ideas, text);
			case ITEM:
				return findByName(items, text);
			case LOCATION:
				return findByName(locations, text);
			case MEMO:
				return findByName(memos, text);
			case PART:
				return findByName(parts, text);
			case PERSON:
				return findByName(persons, text);
			case PLOT:
				return findByName(plots, text);
			case RELATION:
				return findByName(relations, text);
			case SCENE:
				return findByName(scenes, text);
			case STATUS:
				return findByName(statuss, text);
			case STRAND:
				return findByName(strands, text);
			case TAG:
				return findByName(tags, text);
		}
		return null;
	}

	/**
	 * find an Entity into to the given entites list for the given name
	 *
	 * @param entities the entites list
	 * @param text the name to search
	 * @return
	 */
	private AbstractEntity findByName(AbsEntitys entities, String text) {
		for (Object p : entities.getList()) {
			if (((AbstractEntity) p).getName().equals(text)) {
				return (AbstractEntity) p;
			}
		}
		return null;
	}

	/**
	 * get the list of entites of the given type
	 *
	 * @param type
	 * @return null if type is unknown
	 */
	@SuppressWarnings("unchecked")
	public List<AbstractEntity> getList(Book.TYPE type) {
		switch (type) {
			case ATTRIBUTE:
			case ATTRIBUTES:
				return attributes.getList();
			case CATEGORY:
			case CATEGORIES:
				return categorys.getList();
			case CHAPTER:
			case CHAPTERS:
				return chapters.getList();
			case ENDNOTE:
			case ENDNOTES:
				return endnotes.getList();
			case EPISODE:
			case EPISODES:
				return episodes.getList();
			case EVENT:
			case EVENTS:
				return events.getList();
			case GENDER:
			case GENDERS:
				return genders.getList();
			case IDEA:
			case IDEAS:
				return ideas.getList();
			case ITEM:
			case ITEMS:
				return items.getList();
			case LOCATION:
			case LOCATIONS:
				return locations.getList();
			case MEMO:
			case MEMOS:
				return memos.getList();
			case PART:
			case PARTS:
				return parts.getList();
			case PERSON:
			case PERSONS:
				return persons.getList();
			case PLOT:
			case PLOTS:
				return plots.getList();
			case RELATION:
			case RELATIONS:
				return relations.getList();
			case SCENE:
			case SCENES:
				return scenes.getList();
			case STATUS:
				return statuss.getList();
			case STRAND:
			case STRANDS:
				return strands.getList();
			case TAG:
			case TAGS:
				return tags.getList();
		}
		return null;
	}

	/**
	 * load data from an Xml file
	 */
	public void load() {
		//LOG.trace(TT + "load() file=" + file.getAbsolutePath());
		if (rootNode != null) {
			book.load();
			loadEntities();
			challenge.fromXml(this, rootNode);
		} else {
			LOG.trace(TT + "load() not an OSBK file");
		}
	}

	/**
	 * open a XML Project file
	 *
	 * @return true if open is ok
	 */
	private boolean open() {
		//LOG.trace(TT + "open() file=" + file.getAbsolutePath());
		fileOpened = false;
		documentBuilder = null;
		List<String> entries = ZipUtil.listEntries(file.getAbsolutePath());
		if (entries.isEmpty()) {
			//not a ZIP
			LOG.err("'" + file.getAbsolutePath() + "' is not a OSBK file");
			return false;
		}
		if (entries.get(0).equals("script.sql")) {
			ImportSQL.execSql(file);
		}
		try {
			initDomFromZip(file.getAbsolutePath());
		} catch (IOException ex) {
			LOG.err(TT + "open() error ", ex);
		}
		if (document == null) {
			rootNode = null;
			return false;
		}
		rootNode = document.getDocumentElement();
		Element n = (Element) rootNode.getElementsByTagName("book").item(0);
		fileOpened = true;
		return true;
	}

	/**
	 * initialize the Document from current opened File
	 *
	 * @return
	 */
	public Document initDom() {
		//LOG.trace(TT + "initDom()");
		Document rc = null;
		try {
			rc = documentBuilder.parse(file);
		} catch (SAXException e) {
			ExceptionDlg.show(this.getClass().getSimpleName()
					+ ".readDom() Parsing error for " + file.getAbsolutePath(), e);
		} catch (IOException e) {
			ExceptionDlg.show(this.getClass().getSimpleName()
					+ ".readDom() I/O error for " + file.getAbsolutePath(), e);
		}
		return (rc);
	}

	/**
	 * close XML file
	 *
	 */
	public void close() {
		//LOG.trace(TT + "close()");
		if (fileOpened) {
			fileOpened = false;
			document = null;
			documentBuilder = null;
		}
	}

	/**
	 * load entities
	 *
	 * @return number of loaded entities
	 */
	private int loadEntities() {
		//LOG.trace(TT + "loadEntities()");
		int rc = -1;
		for (Book.TYPE t : Book.TYPE.values()) {
			rc += loadEntities(t);
		}
		for (Book.TYPE t : Book.TYPE.values()) {
			setLinks(t);
			this.changeHtmlLinks(t);
		}
		scenes.relativeDateInit();
		return rc;
	}

	/**
	 * load entities for the given type
	 *
	 * @param type
	 * @return number of loaded entities
	 */
	private int loadEntities(Book.TYPE type) {
		//LOG.trace(TT + "loadEntities(type=" + type.name() + ")");
		int n = 0;
		NodeList nodes = rootNode.getElementsByTagName(type.toString());
		if (nodes.getLength() > 0) {
			for (int i = 0; i < nodes.getLength(); i++) {
				switch (type) {
					case ATTRIBUTE:
						attributes.add(Attribute.fromXml(nodes.item(i)));
						n++;
						break;
					case CATEGORY:
						categorys.add(Category.fromXml(nodes.item(i)));
						n++;
						break;
					case CHAPTER:
						chapters.add(Chapter.fromXml(nodes.item(i)));
						n++;
						break;
					case ENDNOTE:
						endnotes.add(Endnote.fromXml(nodes.item(i)));
						n++;
						break;
					case EPISODE:
						episodes.add(Episode.fromXml(nodes.item(i)));
						n++;
						break;
					case EVENT:
						events.add(Event.fromXml(nodes.item(i)));
						n++;
						break;
					case GENDER:
						genders.add(Gender.fromXml(nodes.item(i)));
						n++;
						break;
					case IDEA:
						ideas.add(Idea.fromXml(nodes.item(i)));
						n++;
						break;
					case ITEM:
						items.add(Item.fromXml(nodes.item(i)));
						n++;
						break;
					case LOCATION:
						locations.add(Location.fromXml(nodes.item(i)));
						n++;
						break;
					case MEMO:
						memos.add(Memo.fromXml(nodes.item(i)));
						n++;
						break;
					case PART:
						parts.add(Part.fromXml(nodes.item(i)));
						n++;
						break;
					case PERSON:
						persons.add(Person.fromXml(nodes.item(i)));
						n++;
						break;
					case PLOT:
						plots.add(Plot.fromXml(nodes.item(i)));
						n++;
						break;
					case RELATION:
						relations.add(Relation.fromXml(nodes.item(i)));
						n++;
						break;
					case SCENE:
						scenes.add(Scene.fromXml(nodes.item(i)));
						n++;
						break;
					case STATUS:
						statuss.add(Status.fromXml(nodes.item(i)));
						n++;
						break;
					case STRAND:
						strands.add(Strand.fromXml(nodes.item(i)));
						n++;
						break;
					case TAG:
						tags.add(Tag.fromXml(nodes.item(i)));
						n++;
						break;
				}
			}
		}
		return n;
	}

	/**
	 * set the links for the given entities
	 *
	 * @param type
	 */
	public void setLinks(Book.TYPE type) {
		//LOG.trace(TT + "setLinks(type=" + type.name() + ")");
		switch (type) {
			case ATTRIBUTE:
				attributes.setLinks();
				break;
			case CATEGORY:
				categorys.setLinks();
				break;
			case CHAPTER:
				chapters.setLinks();
				break;
			case ENDNOTE:
				endnotes.setLinks();
				break;
			case EPISODE:
				episodes.setLinks();
				break;
			case EVENT:
				events.setLinks();
				break;
			case GENDER:
				genders.setLinks();
				break;
			case IDEA:
				ideas.setLinks();
				break;
			case ITEM:
				items.setLinks();
				break;
			case LOCATION:
				locations.setLinks();
				break;
			case MEMO:
				memos.setLinks();
				break;
			case PART:
				parts.setLinks();
				break;
			case PERSON:
				persons.setLinks();
				break;
			case PLOT:
				plots.setLinks();
				break;
			case RELATION:
				relations.setLinks();
				break;
			case SCENE:
				scenes.setLinks();
				break;
			case STATUS:
				statuss.setLinks();
				break;
			case STRAND:
				strands.setLinks();
				break;
			case TAG:
				tags.setLinks();
		}
	}

	/**
	 * change HTML links for the given type
	 *
	 * @param type
	 */
	public void changeHtmlLinks(Book.TYPE type) {
		//LOG.trace(TT + "setLinks(type=" + type.name() + ")");
		String rpath = getPath();
		switch (type) {
			case ATTRIBUTE:
				attributes.changeHtmlLinks(rpath);
				break;
			case CATEGORY:
				categorys.changeHtmlLinks(rpath);
				break;
			case CHAPTER:
				chapters.changeHtmlLinks(rpath);
				break;
			case ENDNOTE:
				endnotes.changeHtmlLinks(rpath);
				break;
			case EPISODE:
				episodes.changeHtmlLinks(rpath);
				break;
			case EVENT:
				events.changeHtmlLinks(rpath);
				break;
			case GENDER:
				genders.changeHtmlLinks(rpath);
				break;
			case IDEA:
				ideas.changeHtmlLinks(rpath);
				break;
			case ITEM:
				items.changeHtmlLinks(rpath);
				break;
			case LOCATION:
				locations.changeHtmlLinks(rpath);
				break;
			case MEMO:
				memos.changeHtmlLinks(rpath);
				break;
			case PART:
				parts.changeHtmlLinks(rpath);
				break;
			case PERSON:
				persons.changeHtmlLinks(rpath);
				break;
			case PLOT:
				plots.changeHtmlLinks(rpath);
				break;
			case RELATION:
				relations.changeHtmlLinks(rpath);
				break;
			case SCENE:
				scenes.changeHtmlLinks(rpath);
				break;
			case STATUS:
				statuss.changeHtmlLinks(rpath);
				break;
			case STRAND:
				strands.changeHtmlLinks(rpath);
				break;
			case TAG:
				tags.changeHtmlLinks(rpath);
		}
	}

	/**
	 * write the Project to the given File, if File exists then delete it
	 *
	 * @param outfile
	 * @return
	 */
	public boolean save(File outfile) {
		//LOG.trace(TT + "save(outfile=" + (file == null ? "null" : file.getAbsolutePath()) + ")");
		if (outfile == null) {
			return false;
		}
		String fname = outfile.getAbsolutePath();
		// force the extension to the OSBK extension
		File out = new File(IOUtil.changeExt(fname, Const.STORYBOOK.FILE_EXT_OSBK.toString()));
		if (out.exists()) {
			out.delete();
		}
		StringBuilder b = new StringBuilder();
		book.setMaj();
		b.append(Xml.HEADER);
		b.append("<book>\n");
		b.append(book.info.toXml());
		b.append(book.param.toXml());
		b.append(attributes.toXml());
		b.append(categorys.toXml());
		b.append(chapters.toXml());
		b.append(endnotes.toXml());
		b.append(episodes.toXml());
		b.append(events.toXml());
		b.append(genders.toXml());
		b.append(ideas.toXml());
		b.append(items.toXml());
		b.append(locations.toXml());
		b.append(memos.toXml());
		b.append(parts.toXml());
		b.append(persons.toXml());
		b.append(plots.toXml());
		b.append(relations.toXml());
		b.append(scenes.toXml());
		b.append(statuss.toXml());
		b.append(strands.toXml());
		b.append(tags.toXml());
		if (challenge.isActive()) {
			challenge.lastWordsSet(BookUtil.getNbWords(this));
			b.append(challenge.toXml());
		}
		b.append("</book>");
		this.setFile(outfile);
		ZipUtil.writeString(out.getAbsolutePath(), "db.xml", b.toString());
		return true;
	}

	/**
	 * write the Project to the current File
	 *
	 * @return true if OK
	 */
	public boolean save() {
		//LOG.trace(TT + "write()");
		return save(this.file);
	}

	/**
	 * write the given entity to save it
	 *
	 * @param entity
	 */
	public void write(AbstractEntity entity) {
		//LOG.trace(TT + "write(entity=" + LOG.trace(entity) + ")");
		switch (entity.getObjType()) {
			case ATTRIBUTE:
				attributes.save(entity);
				break;
			case CATEGORY:
				categorys.save(entity);
				break;
			case CHAPTER:
				chapters.save(entity);
				break;
			case ENDNOTE:
				endnotes.save(entity);
				break;
			case EPISODE:
				episodes.save(entity);
				break;
			case EVENT:
				events.save(entity);
				break;
			case GENDER:
				genders.save(entity);
				break;
			case IDEA:
				ideas.save(entity);
				break;
			case ITEM:
				items.save(entity);
				break;
			case LOCATION:
				locations.save(entity);
				break;
			case MEMO:
				memos.save(entity);
				break;
			case PART:
				parts.save(entity);
				break;
			case PERSON:
				persons.save(entity);
				break;
			case PLOT:
				plots.save(entity);
				break;
			case RELATION:
				relations.save(entity);
				break;
			case SCENE:
				scenes.save(entity);
				break;
			case STATUS:
				statuss.save(entity);
				break;
			case STRAND:
				strands.save(entity);
				break;
			case TAG:
				tags.save(entity);
		}
	}

	/**
	 * delete the given entity
	 *
	 * @param entity
	 */
	public void delete(AbstractEntity entity) {
		switch (entity.getObjType()) {
			case ATTRIBUTE:
				attributes.delete(entity);
				break;
			case CATEGORY:
				categorys.delete(entity);
				break;
			case CHAPTER:
				chapters.delete(entity);
				break;
			case ENDNOTE:
				endnotes.delete(entity);
				break;
			case EPISODE:
				episodes.delete(entity);
				break;
			case EVENT:
				events.delete(entity);
				break;
			case GENDER:
				genders.delete(entity);
				break;
			case IDEA:
				ideas.delete(entity);
				break;
			case ITEM:
				items.delete(entity);
				break;
			case LOCATION:
				locations.delete(entity);
				break;
			case MEMO:
				memos.delete(entity);
				break;
			case PART:
				parts.delete(entity);
				break;
			case PERSON:
				persons.delete(entity);
				break;
			case PLOT:
				plots.delete(entity);
				break;
			case RELATION:
				relations.delete(entity);
				break;
			case SCENE:
				scenes.delete(entity);
				break;
			case STATUS:
				statuss.delete(entity);
				break;
			case STRAND:
				strands.delete(entity);
				break;
			case TAG:
				tags.delete(entity);
		}
	}

	/**
	 * check if this project file is OK
	 *
	 * @return
	 */
	public boolean isOK() {
		//LOG.trace("DbFile.isOK()");
		boolean rc = true;
		// file doesn't exist
		if (!file.exists()) {
			JOptionPane.showMessageDialog(null,
					I18N.getMsg("project.not.exist.text", file.getPath()),
					I18N.getMsg("project.not.exist.title"),
					JOptionPane.ERROR_MESSAGE);
			App.getInstance().reloadMenuBars();
			return false;
		}
		// file is read-only
		if (!file.canWrite()) {
			JOptionPane.showMessageDialog(null,
					I18N.getMsg("err.db.read.only", file.getPath()),
					I18N.getMsg("warning"),
					JOptionPane.ERROR_MESSAGE);
			rc = false;
		}
		return rc;
	}

	/**
	 * check if the file is allready opened
	 *
	 * @return
	 */
	public boolean isAlreadyOpened() {
		//LOG.trace(TT+"isAlreadyOpened("+h2Name+")");
		boolean rc = false;
		List<MainFrame> mainFrames = App.getInstance().getMainFrames();
		if (!mainFrames.isEmpty()) {
			for (MainFrame mainFrame : mainFrames) {
				if (mainFrame.isBlank()) {
					continue;
				}
				if (mainFrame.getProject().getFilename().equals(file.getAbsolutePath())) {
					rc = true;
					mainFrame.setVisible(true);
					break;
				}
			}
		}
		return (rc);
	}

	/**
	 * get the name of the Project
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * get the Project path name
	 *
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * set the Project path name
	 */
	public void setPath() {
		if (file != null) {
			this.path = file.getParent();
		}
	}

	/**
	 * get the Project file
	 *
	 * @return
	 */
	public File getFile() {
		return file;
	}

	/**
	 * set the Project file
	 *
	 * @param file
	 */
	public void setFile(File file) {
		this.file = file;
		this.name = file.getName();
		setPath();
	}

	/**
	 * get a standard OSBK filename from path and name
	 *
	 * @return
	 */
	public String getOSBK() {
		return path + File.separator + name + ".osbk";
	}

	/**
	 * restore a backuped Project from the given file to the given file
	 *
	 * @param srce
	 * @param dest
	 */
	public void doRestore(File srce, File dest) {
		//TODO restore a backuped project
	}

	/**
	 * restore a backuped Project from the given file
	 *
	 * @param srce
	 */
	public void doRestore(File srce) {
		//TODO restore a backuped project
	}

	/**
	 * get HTML formatted informations about the Project
	 *
	 * @param detailed
	 * @return
	 */
	public String getInfo(boolean detailed) {
		StringBuilder buf = new StringBuilder();
		if (detailed) {
			buf.append(Html.keyToHtml("date.creation", book.getCreation()));
			buf.append(Html.keyToHtml("date.maj", book.getMaj()));
		}
		buf.append(Html.keyToHtml("book.title", book.getTitle()));
		buf.append(Html.keyToHtml("book.subtitle", book.getSubtitle()));
		buf.append(Html.keyToHtml("book.author", book.getAuthor()));
		buf.append(Html.keyToHtml("book.copyright", book.getCopyright()));
		if (detailed) {
			buf.append(Html.keyToHtml("book.blurb", book.getBlurb()));
			buf.append(Html.keyToHtml("book.dedication", book.getDedication()));
			buf.append(Html.keyToHtml("notes", book.getNotes()));
			buf.append(Html.keyToHtml("book.UUID", book.getUUID()));
			buf.append(Html.keyToHtml("book.ISBN", book.getISBN()));
			buf.append(Html.keyToHtml("assistant", Assistant.toHtml(book.info.assistantGet())));
			buf.append(Html.keyToHtml("scenario", book.getScenario()));
			buf.append(Html.keyToHtml("markdown", book.getMarkdown()));
			buf.append(Html.keyToHtml("review", book.getReview()));
			buf.append(Html.keyToHtml("language", book.getLanguage()));
		}
		return buf.toString();
	}

	/**
	 * remove the MainFrame for this Project
	 */
	public void remove() {
		//TODO remove the mainFrame for this project
	}

	/**
	 * backup the Project
	 */
	public void doBackup() {
		//TODO backup this Project
	}

	/**
	 *
	 * @param filename
	 * @param increment
	 */
	public void doBackup(String filename, boolean increment) {
		//TODO backup this Project to the given file name
	}

	/**
	 * set/unset typist mode
	 *
	 * @param typist
	 */
	public void setTypist(boolean typist) {
		this.typist = typist;
	}

	/**
	 * get typist mode
	 *
	 * @return
	 */
	public boolean getTypist() {
		return this.typist;
	}

	/**
	 * set the image directory
	 *
	 * @param value
	 */
	public void setImageDir(String value) {
		this.imageDir = value;
	}

	/**
	 * get the image directory
	 *
	 * @return
	 */
	public String getImageDir() {
		return this.imageDir;
	}

	/**
	 * get the trace in HTML format
	 *
	 * @param html
	 * @return
	 */
	public String trace(boolean html) {
		StringBuilder b = new StringBuilder();
		b.append(getInfo(true));
		int textLength = BookUtil.getNbChars(this);
		int words = Math.max(1, BookUtil.getNbWords(this));
		String size = String.format("%,d %s (%,d %s) => %d %s",
				words, I18N.getMsg("words"), textLength, I18N.getMsg("characters"),
				words / 480, I18N.getMsg("pages"));//nb de pages sur la base de 480 mots par page
		b.append(Html.keyToHtml("file.info.text.length", size));
		b.append(Html.keyToHtml("title", book.getTitle()));
		b.append(Html.keyToHtml("author", book.getAuthor()));
		b.append(Html.keyToHtml("copyright", book.getCopyright()));
		b.append(Html.keyToHtml("strands", strands.getCount()));
		b.append(Html.keyToHtml("parts", parts.getCount()));
		b.append(Html.keyToHtml("chapters", chapters.getCount()));
		b.append(Html.keyToHtml("scenes", scenes.getCount()));
		b.append(Html.keyToHtml("persons", persons.getCount()));
		b.append(Html.keyToHtml("locations", locations.getCount()));
		b.append(Html.keyToHtml("items", items.getCount()));
		b.append(Html.keyToHtml("plots", plots.getCount()));
		b.append(Html.keyToHtml("tags", tags.getCount()));
		b.append(Html.keyToHtml("endnotes", endnotes.getCount()));
		b.append(Html.keyToHtml("events", events.getCount()));
		b.append(Html.keyToHtml("episodes", episodes.getCount()));
		b.append(Html.keyToHtml("ideas", ideas.getCount()));
		b.append(Html.keyToHtml("memos", memos.getCount()));
		if (!html) {
			String str = b.toString()
					.replace("<tr>", "").replace("</tr>\n", "@")
					.replace("<td valign=\"top\"><b>", "").replace("</b>", "=")
					.replace("<td>", "")
					.replace("</td>", "")
					.replace("\n", "").replace("@", "\n");
			return str.substring(0, str.lastIndexOf("\n"));
		} else {
			return (b.toString());
		}
	}

	/**
	 * get the Document from the ZIPed file
	 *
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public boolean initDomFromZip(String filePath) throws IOException {
		//LOG.trace(TT + "initDomFromZip(filePath=" + filePath + ")");
		try {
			String validPath = XmlCleaner.cleanZip(filePath);
			//LOG.trace("validPath=" + validPath);
			ZipXml zipXml = new ZipXml(new File(validPath));
			ZipEntry entry = zipXml.open("db.xml");
			if (entry == null) {
				return false;
			}
			this.document = zipXml.document;
			return true;
		} catch (Exception ex) {
			LOG.err("error opening ZIP file", ex);
		}
		return false;
	}

	/**
	 * archive the Project into a ZIP file with extend OSBZ
	 *
	 * @return
	 */
	public boolean archive() {
		boolean rc = true;
		String fn = IOUtil.changeExt(this.file.getAbsolutePath(), ".osbz");
		File zout = new File(fn);
		if (zout.exists()) {
			zout.delete();
		}
		// set the message into install.txt
		String msg = Const.getNameVersion() + "\n" + this.book.getTitle();
		IOUtil.fileWriteString(this.getPath() + File.separator + "install.txt", msg);
		archiveProject(new File(this.getPath()), zout);
		IOUtil.fileDelete(new File(this.getPath() + File.separator + "install.txt"));
		return rc;
	}

	/**
	 * compress the given directory into the given zipped File
	 *
	 * @param srcDir
	 * @param destZip
	 * @return true if OK
	 */
	public boolean archiveProject(File srcDir, File destZip) {
		try {
			FileOutputStream dest = new FileOutputStream(destZip);
			ZipOutputStream zos = new ZipOutputStream(dest);
			archiveProjectFiles(srcDir, srcDir, zos, destZip);
			zos.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private void archiveProjectFiles(File dir,
			File curDir, ZipOutputStream zos, File destZip) throws Exception {
		byte[] data = new byte[2048];
		File[] files = curDir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					archiveProjectFiles(dir, f, zos, destZip);
				} else if (!f.equals(destZip)) {
					if (f.getAbsolutePath().endsWith(".osbk5")) {
						continue;
					}
					FileInputStream fi = new FileInputStream(f);
					// creating structure and avoiding duplicate file names
					String nx = f.getAbsolutePath().replace(dir.getAbsolutePath(), "");
					ZipEntry entry = new ZipEntry(nx);
					zos.putNextEntry(entry);
					int count;
					try (BufferedInputStream origin = new BufferedInputStream(fi, 2048)) {
						while ((count = origin.read(data, 0, 2048)) != -1) {
							zos.write(data, 0, count);
						}
					}
				}
			}
		}
	}

	/**
	 * check if the file is OSBK
	 *
	 * @param file
	 * @return
	 */
	public static boolean isOsbk(File file) {
		//LOG.trace(TT + "isOsbk(file=" + file.getAbsolutePath() + ")");
		boolean rc = false;
		List<String> entries = ZipUtil.listEntries(file.getAbsolutePath());
		if (!entries.isEmpty() && entries.get(0).equals("db.xml")) {
			rc = true;
		}
		return rc;
	}

	/**
	 * check if the file is SQL
	 *
	 * @param file
	 * @return
	 */
	public static boolean isSql(File file) {
		//LOG.trace(TT + "isSql(file=" + file.getAbsolutePath() + ")");
		boolean rc = false;
		List<String> entries = ZipUtil.listEntries(file.getAbsolutePath());
		if (!entries.isEmpty() && entries.get(0).equals("script.sql")) {
			rc = true;
		}
		return rc;
	}

	/**
	 * check if the file is archive TXT
	 *
	 * @param file
	 * @return
	 */
	public static boolean isArchive(File file) {
		//LOG.trace(TT + "isArchive(file=" + file.getAbsolutePath() + ")");
		boolean rc = false;
		List<String> entries = ZipUtil.listEntries(file.getAbsolutePath());
		String entryName = ListUtil.find(entries, "install.txt");
		if (!entryName.isEmpty()) {
			List<String> str = ZipUtil.getContent(file, "/install.txt");
			if (!str.isEmpty() && str.get(0).startsWith(Const.getNameVersion())) {
				rc = true;
			}
		}
		return rc;
	}

	/**
	 * select and load an archive TXT from the given file
	 *
	 * @param file
	 * @return
	 */
	public static Project loadArchive(File file) {
		LOG.trace(TT + "loadArchive(file=" + file.getAbsolutePath() + ")");
		//select the target folder
		ChooseFolderDlg dlg = new ChooseFolderDlg((MainFrame) null, "project.select_folder");
		dlg.setVisible(true);
		if (dlg.isCanceled()) {
			return null;
		}
		File folder = dlg.getFolder();
		if (folder != null) {
			try {
				ZipUtil.deflate(file.getAbsolutePath(), folder.getAbsolutePath());
				//search the OSBK file
				for (File fl : IOUtil.dirList(folder)) {
					String fn = fl.getAbsolutePath();
					if (fl.isFile() && fn.endsWith(".osbk")) {
						return new Project(fn);
					}
				}
			} catch (IOException ex) {
			}
			//unzip file into folder
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public String getDuration() {
		int nbw = 0;
		SbDuration sbd = new SbDuration();
		for (Part p : (List<Part>) parts.getList()) {
			List<Chapter> cs = chapters.find(p);
			for (Chapter c : cs) {
				List<Scene> sx = scenes.find(c);
				for (Scene s : sx) {
					sbd.add(s.getSbDuration());
				}
			}
		}
		return sbd.toText();
	}

	boolean script = false;

	public void setScript(boolean b) {
		this.script = b;
	}

	public boolean getScript() {
		return script;
	}

}
