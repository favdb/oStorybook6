/*
 * Copyright (C) 2018 FaVdB
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
package storybook.db.book;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.Icon;
import org.w3c.dom.Element;
import resources.icons.IconUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;
import storybook.project.Project;
import storybook.tools.StringUtil;
import storybook.tools.xml.XmlUtil;
import storybook.ui.MainFrame;

/**
 *
 * @author FaVdB
 */
public class Book {

	private static final String TT = "Book.";
	public final Project project;
	public BookInfo info;
	public BookParam param;
	private int sceneDateInit;

	public Book(Project project) {
		this.project = project;
		info = new BookInfo(this);
		param = new BookParam(this);
	}

	public void load() {
		//LOG.printInfos(TT + "load() " + (project.rootNode == null ? "rootNode null" : project.getFilename()));
		if (project.rootNode != null) {
			info.init();
			param.init();
		}
	}

	public int nbParts() {
		return project.parts.getList().size();
	}

	@SuppressWarnings("unchecked")
	public int nbChapters(Part... part) {
		int nb = 0;
		for (Object obj : project.chapters.getList()) {
			Chapter chapter = (Chapter) obj;
			if (chapter.hasPart()) {
				if (part != null && part.equals(chapter.getPart())) {
					nb++;
				} else {
					nb++;
				}
			}
		}
		return nb;
	}

	@SuppressWarnings("unchecked")
	public int nbScenes() {
		int nb = 0;
		for (Object obj : project.scenes.getList()) {
			Scene scene = (Scene) obj;
			if (!scene.getInformative()) {
				nb++;
			}
		}
		return nb;
	}

	public void setSceneDateInit(int value) {
		this.sceneDateInit = value;
	}

	public int getSceneDateInit() {
		return sceneDateInit;
	}

	/**
	 * datas for identitying the type of DB object
	 */
	public enum TYPE {
		INTERNAL,// internal data may be String, Boolean, Integer or Bytes
		ATTRIBUTE,// Person Attribute (in development)
		CATEGORY,
		CHAPTER,
		ENDNOTE,
		EPISODE,
		EVENT,
		GENDER,
		IDEA,
		IDEABOX,
		ITEM,
		//ITEMLINK,
		LOCATION,
		MEMO,
		PART,
		PERSON,
		PLOT,
		RELATION,
		SCENE,
		STRAND,
		TAG,
		TAGLINK,
		// these are plurials
		INTERNALS,
		ATTRIBUTES,
		CATEGORIES,
		CHAPTERS,
		ENDNOTES,
		EPISODES,
		EVENTS,
		GENDERS,
		IDEAS,
		ITEMS,
		ITEMLINKS,
		LOCATIONS,
		MEMOS,
		PARTS,
		PERSONS,
		PLOTS,
		RELATIONS,
		SCENES,
		STRANDS,
		TAGS,
		TAGLINKS,
		//not realy entities
		SCENARIO,
		SCENARIOS,
		STATUS,
		ALL,// all types for selection purpose
		NONE;

		private TYPE() {
		}

		@Override
		public String toString() {
			return this.name().toLowerCase();
		}

		public boolean compare(String str) {
			return this.name().toLowerCase().equals(str.toLowerCase());
		}

		public boolean isPart() {
			return this.name().equals("PART");
		}

		public boolean isChapter() {
			return this.name().equals("CHAPTER");
		}

		public boolean isScene() {
			return this.name().equals("SCENE");
		}
	}

	public static TYPE getTYPE(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (prop.contains("_")) {
			String v[] = prop.split("_");
			return (getTYPE(v[0]));
		}
		return (TYPE.NONE);

	}

	public static TYPE getTYPE(AbstractEntity entity) {
		if (entity == null) {
			return (TYPE.NONE);
		}
		return entity.getObjType();
	}

	public static TYPE getTYPE(String str) {
		if (str != null && !str.isEmpty()) {
			for (TYPE id : TYPE.values()) {
				if (id.compare(str)) {
					return (id);
				}
			}
		}
		return (TYPE.NONE);
	}

	public static Icon getIcon(TYPE type) {
		return IconUtil.getIconSmall("ent_" + type.toString());
	}

	/**
	 * KEY determining data associated with the Book like Title, Author, and so
	 * on
	 */
	public enum INFO {
		ASSISTANT("Assistant"),//Boolean
		AUTHOR("Author"),// String
		BLURB("Blurb"),// String
		COPYRIGHT("Copyright"),// String
		CREATION("creation"),// String date formated
		UPDATE("update"),// String date formated
		MARKDOWN("Markdown"),// Boolean
		NOTES("Notes"),// String
		DEDICATION("Dedication"),// String
		SCENARIO("Scenario"),// Boolean
		SUBTITLE("SubTitle"),// String
		TITLE("Title"),// String
		UUID("UUID"),// String
		ISBN("ISBN"),// String
		LANG("Language"),// String
		DB_VERSION("dbversion"),// String
		NATURE("Nature"),// Integer
		REVIEW("Review"),// boolean
		NONE("none");
		final private String text;

		private INFO(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text.toLowerCase();
		}
	}

	/**
	 * Specific parameters for the Book
	 */
	public enum PARAM {
		BACKUP_AUTO,// Boolean
		BACKUP_DIR,// String
		BACKUP_INCREMENT,// Integer
		CALENDAR_DAYS,// String
		CALENDAR_HOURS,// Integer
		CALENDAR_MONTHS,// Integer
		CALENDAR_STARTDAY,// Integer
		CALENDAR_USE,// Boolean
		CALENDAR_YEARDAYS,// Integer
		EDITOR_EXTENSION,// String
		EDITOR_MODLESS,// Boolean
		EDITOR_NAME,// String
		EDITOR_TEMPLATE,// String
		EDITOR_USE,// Boolean
		EPUB_COVER,// String
		EPUB_COVER_NOTEXT,// Boolean
		EXPORT,
		EXPORT_DIR,// String
		EXPORT_FORMAT,// String
		IMAGE_DIR,// String
		IMPORT_DIRECTORY,// String
		IMPORT_FILE,// String
		LAYOUT_BOOK,// String
		LAYOUT_SCENE_SEPARATOR,// String
		LAYOUT_SHOW_REVIEW,// Boolean
		SCENE_DATE_INIT,// Integer
		NONE;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	/**
	 * check if a key exists in KEY or PARAM
	 *
	 * @param key
	 * @return
	 */
	public static boolean checkKey(String key) {
		for (INFO k : INFO.values()) {
			if (key.equals(k.toString())) {
				return true;
			}
		}
		for (PARAM k : PARAM.values()) {
			if (key.equals(k.toString())) {
				return true;
			}
		}
		return false;
	}

	public static int checkInteger(int value, int min, int max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}

	///////////////////////////////////
	public String getString(Element el, String key) {
		return el.getAttribute(key.toLowerCase());
	}

	public String getString(Element el, PARAM key) {
		return el.getAttribute(key.toString());
	}

	public boolean getBoolean(Element el, PARAM key) {
		return el.getAttribute(key.toString()).equals("1");
	}

	public Integer getInteger(Element el, PARAM key) {
		String r = el.getAttribute(key.toString());
		if (StringUtil.isNumeric(r)) {
			return Integer.valueOf(r);
		}
		return -1;
	}

	public void setString(Element el, PARAM key, String value) {
		el.setAttribute(key.toString(), value);
	}

	public void setBoolean(Element el, PARAM key, boolean value) {
		el.setAttribute(key.toString(), (value ? "1" : "0"));
	}

	public void setInteger(Element el, PARAM key, Integer value) {
		el.setAttribute(key.toString(), value.toString());
	}
	///////////////////////////////////

	public void setCreation() {
		info.setCreation();
	}

	public void setCreation(String val) {
		info.creationSet(val);
	}

	public String getCreation() {
		return (info.creationGet());
	}

	public void setMaj() {
		info.majSet();
	}

	public String getMaj() {
		return info.majGet();
	}

	public Date getMajDate() {
		return info.majDateGet();
	}

	public void setMaj(String c) {
		info.majSet(c);
	}

	public String getTitle() {
		return (info.titleGet());
	}

	public void setTitle(String value) {
		info.titleSet(value);
	}

	public String getSubtitle() {
		return (info.subtitleGet());
	}

	public void setSubtitle(String value) {
		info.subtitleSet(value);
	}

	public String getAuthor() {
		return (info.authorGet());
	}

	public void setAuthor(String value) {
		info.authorSet(value);
	}

	public String getCopyright() {
		return (info.copyrightGet());
	}

	public void setCopyright(String value) {
		info.copyrightSet(value);
	}

	public String getBlurb() {
		return (info.blurbGet());
	}

	public void setBlurb(String value) {
		info.blurbSet(value);
	}

	public String getNotes() {
		return info.notesGet();
	}

	public void setNotes(String val) {
		info.notesSet(val);
	}

	public String getDedication() {
		return info.dedicationGet();
	}

	public void setDedication(String val) {
		info.dedicationSet(val);
	}

	public boolean getMarkdown() {
		return info.markdownGet();
	}

	public void setMarkdown(boolean val) {
		info.markdownSet(val);
	}

	public boolean getScenario() {
		return info.scenarioGet();
	}

	public void setScenario(boolean val) {
		info.scenarioSet(val);
	}

	public String getISBN() {
		return info.isbnGet();
	}

	public void setISBN(String val) {
		info.isbnSet(val);
	}

	public String getUUID() {
		return info.uuidGet();
	}

	public void setUUID(String str) {
		info.uuidSet(str);
	}

	public String getLanguage() {
		return info.languageGet();
	}

	public void setLanguage(String str) {
		info.languageSet(str);
	}

	public boolean getReview() {
		return info.reviewGet();
	}

	public void setReview(boolean val) {
		info.reviewSet(val);
	}

	/*public static boolean isUseCalendar(Project project) {
		return project.book.param.getParamCalendar().getUse();
	}

	public static SbCalendar getCalendar(Project m) {
		SbCalendar c = new SbCalendar();
		if (m == null) {
			return (c);
		}
		c.setUse(m.book.param.getParamCalendar().getUse());
		if (c.getUse()) {
			SbCalendar cx = m.book.param.getParamCalendar().getCalendar();
			c.yeardays = cx.yeardays;
			c.setHours(cx.hours);
			c.setDays(cx.getListDays());
			c.setMonths(cx.getListMonths());
			c.setStartday(cx.startday);
		}
		return (c);
	}

	public static void storeCalendar(Project m, SbCalendar c) {
		if (m == null) {
			return;
		}
		BookParamCalendar p = m.book.param.getParamCalendar();
		p.setUse(c.getUse());
		p.setYeardays(c.yeardays);
		p.setHours(c.getListHours());
		p.setDays(c.getListDays());
		p.setMonths(c.getListMonths());
		p.setStartDay(c.startday);
	}*/
	/**
	 * get number of Strands
	 *
	 * @param m
	 * @return
	 */
	public static int getNbStrands(MainFrame m) {
		return m.project.strands.getList().size();
	}

	/**
	 * get number of Parts
	 *
	 * @param m
	 * @return
	 */
	public static int getNbParts(MainFrame m) {
		return m.project.parts.getList().size();
	}

	/**
	 * get nummber of Chapters
	 *
	 * @param m
	 * @return
	 */
	public static int getNbChapters(MainFrame m) {
		return m.project.chapters.getList().size();
	}

	/**
	 * get number of Scenes
	 *
	 * @param m
	 * @return
	 */
	public static int getNbScenes(MainFrame m) {
		return m.project.scenes.getList().size();
	}

	/**
	 * get number of Scenes in the given Chapter
	 *
	 * @param mainFrame
	 * @param chapter
	 * @return
	 */
	public static int getNbScenesInChapter(MainFrame mainFrame, Chapter chapter) {
		if (chapter == null) {
			return (0);
		}
		return mainFrame.project.scenes.find(chapter).size();
	}

	/**
	 * get number of Persons
	 *
	 * @param mainFrame
	 * @return
	 */
	public static int getNbPersons(MainFrame mainFrame) {
		return mainFrame.project.getList(TYPE.PERSON).size();
	}

	/**
	 * get number of Locations
	 *
	 * @param mainFrame
	 * @return
	 */
	public static int getNbLocations(MainFrame mainFrame) {
		return mainFrame.project.getList(TYPE.LOCATION).size();
	}

	/**
	 * get number of Items
	 *
	 * @param mainFrame
	 * @return
	 */
	public static int getNbItems(MainFrame mainFrame) {
		return mainFrame.project.getList(TYPE.ITEM).size();
	}

	/**
	 * get the param for backup
	 *
	 * @return
	 */
	public BookParamBackup getParamBackup() {
		return param.getParamBackup();
	}

	/**
	 * get the param
	 *
	 * @return
	 */
	public BookParam getParam() {
		return param;
	}

	/**
	 * is using an external editor (like MSWord or LibreOffice Writer)
	 *
	 * @return
	 */
	public boolean isXeditorUse() {
		return param.getParamEditor().getUse();
	}

	/**
	 * get all existing types of Entity
	 *
	 * @return
	 */
	public static List<Book.TYPE> getTypes() {
		List<Book.TYPE> list = new ArrayList<>();
		list.add(Book.TYPE.ATTRIBUTE);
		list.add(Book.TYPE.CHAPTER);
		list.add(Book.TYPE.CATEGORY);
		list.add(Book.TYPE.ENDNOTE);
		list.add(Book.TYPE.EVENT);
		list.add(Book.TYPE.GENDER);
		list.add(Book.TYPE.IDEA);
		list.add(Book.TYPE.INTERNAL);
		list.add(Book.TYPE.ITEM);
		//list.add(Book.TYPE.ITEMLINK);
		list.add(Book.TYPE.LOCATION);
		list.add(Book.TYPE.MEMO);
		list.add(Book.TYPE.PART);
		list.add(Book.TYPE.PERSON);
		list.add(Book.TYPE.PLOT);
		list.add(Book.TYPE.RELATION);
		list.add(Book.TYPE.SCENE);
		list.add(Book.TYPE.STRAND);
		list.add(Book.TYPE.TAG);
		//list.add(Book.TYPE.TAGLINK);
		return list;
	}

	public static String setChildText(INFO key, String text) {
		return setChildText(key.toString(), text);
	}

	public static String setChildText(PARAM key, String text) {
		return setChildText(key.toString(), text);
	}

	public static String setChildText(String key, String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		return "<" + key + ">" + text + "</" + key + ">\n";
	}

	public static String getText(Element node, INFO key) {
		return getText(node, key.toString());
	}

	public static String getText(Element node, PARAM key) {
		return getText(node, key.toString());
	}

	public static String getText(Element node, String key) {
		return XmlUtil.getText(node, key);
	}

}
