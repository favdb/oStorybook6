/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.book;

import i18n.I18N;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import storybook.App;
import storybook.Pref;
import storybook.db.abs.AbstractEntity;
import storybook.db.attribute.Attribute;
import storybook.db.category.Category;
import storybook.db.chapter.Chapter;
import storybook.db.endnote.Endnote;
import storybook.db.episode.Episode;
import storybook.db.event.Event;
import storybook.db.gender.Gender;
import storybook.db.idea.Idea;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.memo.Memo;
import storybook.db.part.Part;
import storybook.db.person.Person;
import storybook.db.plot.Plot;
import storybook.db.relation.Relation;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.db.tag.Tag;
import storybook.dialog.MessageDlg;
import storybook.project.Project;
import storybook.tools.Markdown;
import storybook.tools.StringUtil;
import storybook.tools.file.EnvUtil;
import storybook.tools.file.FileFilter;
import storybook.ui.MainFrame;

/**
 * some utilities for managing Book project
 *
 * @author martin
 *
 */
public class BookUtil {

	private static final String TT = "BookUtil.";

	private BookUtil() {
		// nothing
	}

	/**
	 * compute the Objective chars for Part or Chapter
	 *
	 * @param nature
	 * @param nb
	 * @return
	 */
	public static int computeObjectiveChars(int nature, int nb) {
		if (nature == 0 || nb == 0) {
			return 0;
		}
		String s = I18N.getMsg("book.nature." + nature);
		if (s.startsWith("!")) {
			return 0;
		}
		if (!s.contains(":")) {
			return 0;
		}
		s = s.substring(s.indexOf(":") + 1);
		if (!StringUtil.isNumeric(s)) {
			return 0;
		}
		return Integer.parseInt(s) / nb;
	}

	/**
	 * compute the Book size
	 *
	 * @param mainFrame
	 * @return
	 */
	public static Map<Object, Integer> getBookSize(MainFrame mainFrame) {
		Map<Object, Integer> sizes = new HashMap<>();
		// Get scenes
		sizes.clear();
		List<Part> parts = mainFrame.project.parts.getRoots();
		for (Part part : parts) {
			appendPartSizes(mainFrame, part, sizes);
		}
		return sizes;
	}

	private static int appendPartSizes(MainFrame mainFrame, Part part,
	   Map<Object, Integer> sizes) {
		int ret = 0;
		List<Part> subparts = mainFrame.project.parts.getParts(part);
		@SuppressWarnings("unchecked")
		List<Chapter> chapters = mainFrame.project.chapters.getList();
		for (Part subpart : subparts) {
			ret += appendPartSizes(mainFrame, subpart, sizes);
		}
		for (Chapter chapter : chapters) {
			int chapterSize = appendChapterSizes(mainFrame, chapter, sizes);
			ret += chapterSize;
		}
		sizes.put(part, ret);
		return ret;
	}

	private static int appendChapterSizes(MainFrame mainFrame, Chapter chapter,
	   Map<Object, Integer> sizes) {
		int ret = 0;
		@SuppressWarnings("unchecked")
		List<Scene> scenes = mainFrame.project.scenes.find(chapter);
		for (Scene scene : scenes) {
			int sceneSize = scene.getWords();
			sizes.put(scene, sceneSize);
			ret += sceneSize;
		}
		sizes.put(chapter, ret);
		return ret;
	}

	public static Project chooseProject() {
		//LOG.trace(TT + "chooseProject()");
		final JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(App.preferences.getString(Pref.KEY.LASTOPEN_DIR,
		   EnvUtil.getHomeDir().getAbsolutePath())));
		FileFilter filter = new FileFilter("osbk");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		int ret = fc.showOpenDialog(null);
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (!file.exists()) {
				JOptionPane.showMessageDialog(null,
				   I18N.getMsg("project.not.exist.text", file),
				   I18N.getMsg("project.not.exist.title"),
				   JOptionPane.ERROR_MESSAGE);
				return (Project) null;
			}
			if (Project.isArchive(file)) {
				return Project.loadArchive(file);
			} else if (Project.isOsbk(file)) {
				return new Project(file);
			} else if (Project.isSql(file)) {
				return new Project(file);
			}
			MessageDlg.show((JFrame) null,
			   I18N.getMsg("project.not_ok", file.getAbsolutePath()), "project", true);
		}
		return (Project) null;
	}

	/**
	 * get a new Entityforthe given MainFrame
	 *
	 * @param mainFrame
	 * @param objtype : type of entity
	 * @return
	 */
	public static AbstractEntity getNewEntity(MainFrame mainFrame, Book.TYPE objtype) {
		//LOG.printInfos(TT + ".getNewEntity(mainFrame, type=" + objtype.name() + ")");
		AbstractEntity entity;
		switch (objtype) {
			case ATTRIBUTE:
			case ATTRIBUTES:
				entity = new Attribute();
				break;
			case CATEGORY:
			case CATEGORIES:
				entity = new Category();
				break;
			case CHAPTER:
			case CHAPTERS:
				entity = new Chapter();
				break;
			case ENDNOTE:
			case ENDNOTES:
				entity = new Endnote();
				break;
			case EPISODE:
			case EPISODES:
				entity = new Episode();
				break;
			case EVENT:
			case EVENTS:
				entity = new Event();
				break;
			case GENDER:
			case GENDERS:
				entity = new Gender();
				break;
			case IDEA:
			case IDEAS:
				entity = new Idea();
				break;
			case ITEM:
			case ITEMS:
				entity = new Item();
				break;
			case LOCATION:
			case LOCATIONS:
				entity = new Location();
				break;
			case MEMO:
			case MEMOS:
				entity = new Memo();
				break;
			case PART:
			case PARTS:
				entity = new Part();
				break;
			case PERSON:
			case PERSONS:
				entity = new Person();
				break;
			case PLOT:
			case PLOTS:
				entity = new Plot();
				break;
			case RELATION:
			case RELATIONS:
				entity = new Relation();
				break;
			case SCENE:
			case SCENES:
				entity = new Scene(mainFrame);
				break;
			case STRAND:
			case STRANDS:
				entity = new Strand();
				break;
			case TAG:
			case TAGS:
				entity = new Tag();
				break;
			default:
				return null;
		}
		return entity;
	}

	/**
	 * get number of characters for a list of Chapters
	 *
	 * @param project
	 * @param chapters
	 *
	 * @return
	 */
	public static int getNbChars(Project project, List<Chapter> chapters) {
		int nb = 0;
		for (Chapter chapter : chapters) {
			nb += BookUtil.getNbChars(project, chapter);
		}
		return nb;
	}

	/**
	 * get number of characters for a given Chapter
	 *
	 * @param project
	 * @param chapter
	 *
	 * @return
	 */
	public static Integer getNbChars(Project project, Chapter chapter) {
		@SuppressWarnings("unchecked")
		List<Scene> scenes = (List) project.scenes.find(chapter);
		int nb = 0;
		for (Scene scene : scenes) {
			nb += scene.getChars();
		}
		return nb;
	}

	/**
	 * get number of characters for all scenes
	 *
	 * @param project
	 *
	 * @return
	 */
	public static Integer getNbChars(Project project) {
		@SuppressWarnings("unchecked")
		List<Scene> scenes = (List) project.scenes.getList();
		int nb = 0;
		for (Scene scene : scenes) {
			nb += scene.getChars();
		}
		return nb;
	}

	/**
	 * get number of words in a list of Chapters
	 *
	 * @param project
	 * @param chapters : the list of chapters
	 *
	 * @return
	 */
	public static int getNbWords(Project project, List<Chapter> chapters) {
		int nb = 0;
		for (Chapter chapter : chapters) {
			nb += getNbWords(project, chapter);
		}
		return nb;
	}

	/**
	 * get number of words for the given Chapter
	 *
	 * @param project
	 * @param chapter
	 *
	 * @return
	 */
	public static Integer getNbWords(Project project, Chapter chapter) {
		@SuppressWarnings("unchecked")
		List<Scene> scenes = project.scenes.find(chapter);
		int nb = 0;
		for (Scene s : scenes) {
			nb += s.getWords();
		}
		return nb;
	}

	/**
	 * get number of words in all Scenes
	 *
	 * @param project
	 *
	 * @return
	 */
	public static Integer getNbWords(Project project) {
		@SuppressWarnings("unchecked")
		List<Scene> scenes = (List) project.scenes.getList();
		int nb = 0;
		for (Scene scene : scenes) {
			nb += scene.getWords();
		}
		return nb;
	}

	/**
	 * convert format for summary, description, notes (HTML<>Markdown, Marksown<>HTML
	 *
	 * @param mainFrame
	 * @param b : true for HTML<>Marxkdown
	 */
	public static void convertTo(MainFrame mainFrame, boolean b) {
		// when b is true convert to Markdown else convert to HTML
		for (Book.TYPE t : Book.TYPE.values()) {
			if (t != Book.TYPE.INTERNAL) {
				convertEntity(b, mainFrame.project, t);
			}
		}
	}

	/**
	 * convert format for summary, description, notes (HTML to Markdown, Marksown to HTML)
	 *
	 * @param b : true for HTML<>Marxkdown
	 * @param project
	 * @param type
	 */
	public static void convertEntity(boolean b, Project project, Book.TYPE type) {
		/*LOG.trace(TT + "convertEntity("
		   + "b=" + (b ? "true" : "false")
		   + ", project=" + (project == null ? "null" : "OK")
		   + ", type=" + type.toString() + ")");*/
		List entities = project.getList(type);
		if (entities != null) {
			for (Object obj : entities) {
				boolean c = false;
				AbstractEntity entity = (AbstractEntity) obj;
				if (b && entity.getNotes().contains("</p>")) {
					entity.setNotes(Markdown.toMarkdown(entity.getNotes()));
					c = true;
				}
				if (!b && !entity.getNotes().contains("</p>")) {
					entity.setNotes(Markdown.toHtml(entity.getNotes()));
					c = true;
				}
				if (b && entity.getDescription().contains("</p>")) {
					entity.setDescription(Markdown.toMarkdown(entity.getDescription()));
					c = true;
				}
				if (!b && !entity.getDescription().contains("</p>")) {
					entity.setDescription(Markdown.toHtml(entity.getDescription()));
					c = true;
				}
				if (entity instanceof Scene) {
					if (b && ((Scene) entity).getSummary().contains("</p>")) {
						((Scene) entity).setSummary(Markdown.toMarkdown(((Scene) entity).getSummary()));
						c = true;
					}
					if (!b && !((Scene) entity).getSummary().contains("</p>")) {
						((Scene) entity).setSummary(Markdown.toHtml(((Scene) entity).getSummary()));
						c = true;
					}
				}
				if (c) {
					project.write(entity);
				}
			}
		}
	}

}
