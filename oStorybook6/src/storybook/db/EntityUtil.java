/*
 Storybook: Open Source software for novelists and authors.
 Copyright (C) 2008 - 2015 Martin Mustun, Pete Keller

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
package storybook.db;

import i18n.I18N;
import java.awt.Component;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.apache.commons.beanutils.BeanUtils;
import static org.apache.commons.beanutils.BeanUtils.copyProperties;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.*;
import org.w3c.dom.Element;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.ctrl.Ctrl;
import storybook.db.abs.AbstractEntity;
import storybook.db.attribute.Attribute;
import storybook.db.book.Book;
import static storybook.db.book.Book.TYPE.SCENE;
import storybook.db.category.Category;
import storybook.db.chapter.Chapter;
import storybook.db.endnote.Endnote;
import storybook.db.episode.Episode;
import storybook.db.event.Event;
import storybook.db.gender.Gender;
import storybook.db.idea.Idea;
import storybook.db.item.Item;
import storybook.db.item.Items;
import storybook.db.location.Location;
import storybook.db.location.Locations;
import storybook.db.memo.Memo;
import storybook.db.part.Part;
import storybook.db.person.Person;
import storybook.db.person.Persons;
import storybook.db.plot.Plot;
import storybook.db.plot.Plots;
import storybook.db.relation.Relation;
import storybook.db.relation.Relations;
import storybook.db.scene.Scene;
import storybook.db.scene.Scenes;
import storybook.db.status.Status;
import storybook.db.strand.Strand;
import storybook.db.strand.Strands;
import storybook.db.tag.Tag;
import storybook.db.tag.Tags;
import storybook.dialog.ConfirmDeleteDlg;
import storybook.ideabox.IdeaxFrm;
import storybook.model.Model;
import storybook.renderer.EntityLCR;
import storybook.tools.LOG;
import storybook.tools.ListUtil;
import storybook.tools.html.Html;
import storybook.tools.net.Net;
import static storybook.tools.swing.SwingUtil.showModalDialog;
import storybook.ui.MainFrame;
import storybook.ui.MainMenu;

/**
 * utilities for Entity
 *
 * @author favdb
 */
public class EntityUtil {

	private static final String TT = "EntityUtil",
	   HTML_P_START = "<p style=\"padding-top:10px\">";
	public static boolean WITH_CHRONO = true;

	/**
	 * utility class
	 */
	private EntityUtil() {
		// empty
	}

	/**
	 * create an entity in the database
	 *
	 * @param mainFrame
	 * @param entity
	 */
	public static void createEntity(MainFrame mainFrame, AbstractEntity entity) {
		mainFrame.project.write(entity);
		mainFrame.getBookModel().fireAgain();
	}

	/**
	 * remove an entity from the database
	 *
	 * @param mainFrame
	 * @param type
	 * @param id
	 */
	public static void removeEntity(MainFrame mainFrame, Book.TYPE type, long id) {
		AbstractEntity entity = mainFrame.project.get(type, id);
		mainFrame.getBookModel().ENTITY_Delete(entity);
	}

	/**
	 * join entities
	 *
	 * @param mainFrame
	 * @param entities
	 */
	public static void joinEntities(MainFrame mainFrame, List<AbstractEntity> entities) {
		switch (entities.get(0).getObjType()) {
			case CHAPTER:
				joinChapters(mainFrame, entities);
				break;
			case SCENE:
				joinScenes(mainFrame, entities);
				break;
		}
	}

	/**
	 * join Chapters in one and remove joined chapters
	 *
	 * @param mainFrame
	 * @param chapters
	 */
	public static void joinChapters(MainFrame mainFrame, List<AbstractEntity> chapters) {
		//LOG.printInfos(TT + ".joinChapters(mainFrame, chapters nb=" + chapters.size() + ")");
		mainFrame.cursorSetWaiting();
		Chapter master = (Chapter) chapters.get(0);
		for (int i = 1; i < chapters.size(); i++) {
			Chapter chapter = (Chapter) chapters.get(i);
			List<Scene> scenes = mainFrame.project.scenes.find(chapter);
			for (Scene scene : scenes) {
				scene.setChapter(master);
				mainFrame.project.write(scene);
			}
		}
		Model md = mainFrame.getBookModel();
		md.fireAgainScenes();
		md.fireAgainChapters();
		md.fireAgainParts();
		mainFrame.setUpdated();
		mainFrame.cursorSetDefault();
	}

	/**
	 * join summary of Scenes and delete joined scenes, joined text limited to 32700 chars max
	 *
	 * @param mainFrame
	 * @param scenes
	 */
	public static void joinScenes(MainFrame mainFrame, List<AbstractEntity> scenes) {
		//LOG.printInfos(TT + ".joinScenes(mainFrame, scenes nb=" + scenes.size() + ")");
		mainFrame.cursorSetWaiting();
		Scene master = (Scene) scenes.get(0);
		StringBuilder text = new StringBuilder(master.getSummary());
		int len = master.getSummary().length();
		for (int i = 1; i < scenes.size(); i++) {
			Scene scene = (Scene) scenes.get(i);
			if (len + scene.getSummary().length() < 32700) {
				text.append(scene.getSummary());
				addEntities(master.getPersons(), scene.getPersons());
				addEntities(master.getLocations(), scene.getLocations());
				addEntities(master.getItems(), scene.getItems());
				addEntities(master.getPlots(), scene.getPlots());
				if (!master.getStrands().contains(scene.getStrand())) {
					master.getStrands().add(scene.getStrand());
				}
				addEntities(master.getStrands(), scene.getStrands());
				mainFrame.project.delete(scene);
			} else {
				master.setSummary(text.toString());
				mainFrame.project.write(master);
				master = scene;
				text = new StringBuilder(master.getSummary());
				len = master.getSummary().length();
			}
		}
		master.setSummary(text.toString());
		mainFrame.project.scenes.save(master);
		Model md = mainFrame.getBookModel();
		md.fireAgainScenes();
		md.fireAgainChapters();
		md.fireAgainParts();
		mainFrame.setUpdated();
		mainFrame.cursorSetDefault();
	}

	/**
	 * add entities list to the given master list, only if object isn't in master
	 *
	 * @param master
	 * @param toadd
	 */
	@SuppressWarnings("unchecked")
	public static void addEntities(List master, List toadd) {
		if (!toadd.isEmpty()) {
			for (Object p : toadd) {
				if (!master.contains(p)) {
					master.add(p);
				}
			}
		}
	}

	/**
	 * create an entity from a given XML node
	 *
	 * @param mainFrame
	 * @param node
	 * @return
	 */
	public static boolean fromXml(MainFrame mainFrame, Element node) {
		boolean rc = false;
		Book.TYPE type = Book.getTYPE(node.getNodeName());
		AbstractEntity entity = null;
		switch (type) {
			case ATTRIBUTE:
				// forbidden
				break;
			case CATEGORY:
				entity = Category.fromXml(node);
				break;
			case CHAPTER:
				entity = Chapter.fromXml(node);
				break;
			case EVENT:
				entity = Event.fromXml(node);
				break;
			case GENDER:
				// forbidden
				break;
			case ITEM:
				entity = Item.fromXml(node);
				break;
			/*case ITEMLINK:
				// forbidden
				break;*/
			case LOCATION:
				entity = Location.fromXml(node);
				break;
			case PART:
				entity = Scene.fromXml(node);
				break;
			case PERSON:
				entity = Scene.fromXml(node);
				break;
			case PLOT:
				entity = Scene.fromXml(node);
				break;
			case RELATION:
				// forbidden
				break;
			case SCENE:
				entity = Scene.fromXml(node);
				break;
			case STRAND:
				entity = Strand.fromXml(node);
				break;
			case TAG:
				entity = Tag.fromXml(node);
				break;
			/*case TAGLINK:
				// forbidden
				break;*/
		}
		if (entity != null) {
			mainFrame.getBookController().newEntity(entity);
			rc = true;
		}
		return rc;
	}

	/**
	 * get list of ID which cannot be modified/removed
	 *
	 * @param entity
	 * @return
	 */
	public static List<Long> getReadOnlyIds(AbstractEntity entity) {
		ArrayList<Long> ret = new ArrayList<>();
		if (entity instanceof Category || entity instanceof Gender) {
			ret.add(1L);
			ret.add(2L);
		} else if (entity instanceof Part || entity instanceof Strand) {
			ret.add(1L);
		}
		return ret;
	}

	/**
	 * delete an Entity
	 *
	 * @param mainFrame
	 * @param entity to delete
	 */
	public static void delete(MainFrame mainFrame, AbstractEntity entity) {
		// check if read only
		boolean allowed = true;
		List<Long> readOnlyIds = EntityUtil.getReadOnlyIds(entity);
		if (readOnlyIds.contains(entity.getId())) {
			allowed = false;
		}
		if (entity.getObjType() == Book.TYPE.STRAND
		   && Book.getNbStrands(mainFrame) == 1) {
			allowed = false;
		}
		if (entity.getObjType() == Book.TYPE.PART
		   && Book.getNbParts(mainFrame) == 1) {
			allowed = false;
		}
		if (entity.getObjType() == Book.TYPE.CHAPTER
		   && Book.getNbChapters(mainFrame) == 1) {
			allowed = false;
		}
		if (!allowed) {
			JOptionPane.showMessageDialog(mainFrame,
			   I18N.getMsg("no.delete"),
			   I18N.getMsg("warning"),
			   JOptionPane.ERROR_MESSAGE);
			return;
		}
		ConfirmDeleteDlg dlg = new ConfirmDeleteDlg(mainFrame, entity);
		showModalDialog(dlg, mainFrame, true);
		if (dlg.isCanceled()) {
			return;
		}
		Ctrl ctrl = mainFrame.getBookController();
		Part lastUsed = mainFrame.lastPartGet();
		if (entity instanceof Part
		   && (lastUsed != null && lastUsed.getId().equals(entity.getId()))) {
			// current part will be delete, change to first part
			Part firstPart = (Part) mainFrame.project.parts.getFirst();
			mainFrame.lastPartSet(firstPart);
		}
		ctrl.deleteEntity(entity);
	}

	/**
	 * copy properties from a given entity to a given entity
	 *
	 * @param mainFrame
	 * @param inEntity
	 * @param toEntity
	 */
	public static void copyEntityProperties(MainFrame mainFrame,
	   AbstractEntity inEntity,
	   AbstractEntity toEntity) {
		try {
			if (inEntity == null || toEntity == null) {
				LOG.err("EntityUtils.copyEntityProperties inEntity or toEntity is null");
				return;
			}
			ConvertUtils.register(new DateConverter(null), Date.class);
			ConvertUtils.register(new SqlTimestampConverter(null), Timestamp.class);
			ConvertUtils.register(new IntegerConverter(), Integer.class);
			ConvertUtils.register(new LongConverter(), Long.class);
			ConvertUtils.register(new StringConverter(), String.class);
			copyProperties(toEntity, inEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			if (e.getMessage() == null) {
				return;
			}
			LOG.err(e.getMessage(), e);
		}
	}

	/**
	 * clone a given Entity
	 *
	 * @param mainFrame
	 * @param entity
	 * @return
	 */
	public static AbstractEntity cloneEntity(MainFrame mainFrame, AbstractEntity entity) {
		if (entity != null) try {
			ConvertUtils.register(new DateConverter(null), Date.class);
			ConvertUtils.register(new SqlTimestampConverter(null), Timestamp.class);
			ConvertUtils.register(new IntegerConverter(), Integer.class);
			ConvertUtils.register(new StringConverter(), String.class);
			return (AbstractEntity) BeanUtils.cloneBean(entity);
		} catch (IllegalAccessException
		   | InstantiationException
		   | InvocationTargetException
		   | NoSuchMethodException e) {
			LOG.err("EntityUtil.cloneEntityProperties() Exception : ", e);
		}
		return null;
	}

	/**
	 * copy a given Entity into a new Entity
	 *
	 * @param mainFrame
	 * @param inEntity
	 */
	public static void copyEntity(MainFrame mainFrame, AbstractEntity inEntity) {
		if (inEntity == null) {
			return;
		}
		AbstractEntity toEntity = createNewEntity(inEntity);
		copyEntityProperties(mainFrame, inEntity, toEntity);
		markCopiedEntity(toEntity);
		List<Plot> plots = new ArrayList<>();
		List<Item> items = new ArrayList<>();
		List<Location> locations = new ArrayList<>();
		List<Person> persons = new ArrayList<>();
		List<Strand> strands = new ArrayList<>();
		switch (Book.getTYPE(inEntity)) {
			case SCENE:
				//empty all links to others
				Scene scene = (Scene) inEntity;
				Scene newScene = (Scene) toEntity;
				if (newScene == null) {
					break;
				}
				if (!scene.hasRelativescene()) {
					newScene.removeRelativescene();
				}
				newScene.setPersons(persons);
				newScene.setLocations(locations);
				newScene.setItems(items);
				newScene.setPlots(plots);
				newScene.setStrands(strands);
				if (!scene.hasRelativescene()) {
					newScene.removeRelativescene();
				}
				break;
			default:
				break;
		}
		Ctrl ctrl = mainFrame.getBookController();
		ctrl.newEntity(toEntity);
	}

	/**
	 * change the Entity name to add (copy) for Chapter, Scene, Strand, Tag, Idea
	 *
	 * @param mainFrame
	 * @param entity
	 */
	private static void markCopiedEntity(AbstractEntity entity) {
		String copyStr = "(" + I18N.getMsg("copy") + ") ";
		switch (Book.getTYPE(entity)) {
			case SCENE:
				Scene s = (Scene) entity;
				s.setTitle(copyStr + s.getTitle());
				break;
			case CHAPTER:
				Chapter c = (Chapter) entity;
				c.setTitle(copyStr + c.getName());
				break;
			case ATTRIBUTE:
			case CATEGORY:
			case GENDER:
			case ITEM:
			case LOCATION:
			case PART:
			case PLOT:
			case RELATION:
			case STRAND:
			case TAG:
				Tag t = (Tag) entity;
				t.setName(copyStr + t.getName());
				break;
			case IDEA:
				Idea i = (Idea) entity;
				i.setName(copyStr + i.getName());
				i.setNotes(copyStr + i.getNotes());
				break;
			default:
				break;
		}
	}

	/**
	 * create a JPopupMenu for the given Entity
	 *
	 * @param mainFrame
	 * @param entity
	 * @param withChrono : boolean true i view chrono is required
	 *
	 * @return
	 */
	public static JPopupMenu createPopupMenu(MainFrame mainFrame,
	   AbstractEntity entity, boolean withChrono) {
		JPopupMenu menu = new JPopupMenu();
		if (entity == null) {
			return null;
		}
		if (entity.isTransient()) {
			return null;
		}
		Ctrl ctrl = mainFrame.getBookController();
		menu.add(MainMenu.initMenuItem(ICONS.K.EDIT,
		   "edit", "", ' ',
		   "edit",
		   e -> mainFrame.showEditorAsDialog(entity)));
		if (Book.getTYPE(entity) == Book.TYPE.ENDNOTE) {
			return menu;
		}
		if (entity instanceof Scene && mainFrame.getBook().isXeditorUse()) {
			JMenuItem item = MainMenu.initMenuItem(ICONS.K.EMPTY,
			   "xeditor", "", ' ',
			   "xeditor",
			   e -> mainFrame.showXeditor(entity));
			item.setEnabled(new File(((Scene) entity).getOdf()).exists());
			menu.add(item);
		}
		menu.add(new JPopupMenu.Separator());
		// show in view...
		List<JMenuItem> lj = new ArrayList<>();
		if (entity instanceof Scene || entity instanceof Chapter) {
			if (withChrono) {
				lj.add(MainMenu.initMenuItem("view.chrono", "",
				   e -> ctrl.chronoShow(entity)));
			}
			lj.add(MainMenu.initMenuItem("view.book", "",
			   e -> ctrl.bookShow(entity)));
			lj.add(MainMenu.initMenuItem("view.manage", "",
			   e -> ctrl.manageShow(entity)));
		}
		lj.add(MainMenu.initMenuItem("view.info", "",
		   e -> ctrl.infoShow(entity)));
		if (isAvailableInMemoria(entity)) {
			lj.add(MainMenu.initMenuItem("view.memoria", "",
			   e -> ctrl.memoriaShow(entity)));
		}
		if (lj.size() > 1) {
			JMenu mj = new JMenu(I18N.getMsg("show.in"));
			mj.setIcon(IconUtil.getIconSmall(ICONS.K.SEARCH));
			for (JMenuItem lmj : lj) {
				mj.add(lmj);
			}
			menu.add(mj);
		} else {
			menu.add(lj.get(0));
		}
		menu.add(new JPopupMenu.Separator());
		if (entity instanceof Chapter) {
			menu.add(MainMenu.initMenuItem(ICONS.K.SORT, "order.time", "", ' ', "order.time",
			   e -> mainFrame.project.scenes.renumberByTime(mainFrame, (Chapter) entity)));
			menu.add(MainMenu.initMenuItem(ICONS.K.SORT, "re_sort", "", ' ', "re_sort",
			   e -> mainFrame.project.scenes.renumber(mainFrame, (Chapter) entity)));
			menu.add(new JPopupMenu.Separator());
		}

		if (entity instanceof Scene) {
			menu.add(Status.getMenu(mainFrame, (Scene) entity));
		}

		menu.add(MainMenu.initMenuItem(ICONS.K.NEW,
		   "entity.new", "", ' ', "new." + entity.getObjType().toString(),
		   e -> {
			   AbstractEntity n = EntityUtil.createNewEntity(entity);
			   if (n != null) {
				   ctrl.setEditEntity(n);
			   }
		   }));
		menu.add(MainMenu.initMenuItem(ICONS.K.DELETE, "delete", "", ' ', "delete",
		   e -> EntityUtil.delete(mainFrame, entity)));

		if (entity instanceof Idea) {
			JMenuItem item = new JMenuItem(I18N.getMsg("ideabox.copy_to"));
			item.setIcon(IconUtil.getIconSmall(ICONS.K.IDEABOX));
			item.addActionListener(e -> {
				Idea idea = (Idea) entity;
				if (IdeaxFrm.ideaboxAdd(mainFrame, idea) != null) {
					mainFrame.getBookModel().ENTITY_Update(idea);
				}
			});
			menu.add(item);
		}
		if (entity instanceof Location) {
			menu.add(new JPopupMenu.Separator());
			menu.add(MainMenu.initMenuItem(ICONS.K.WORLD, "openStreetMap", "", ' ', "!OpenStreetMap",
			   e -> Net.openOSM((Location) entity)));
		}
		if (menu.getComponents().length == 0) {
			return null;
		}
		for (Component comp : menu.getComponents()) {
			if (comp instanceof JMenuItem) {
				((JMenuItem) comp).setFont(App.fonts.defGet());
			}
		}
		return menu;
	}

	/**
	 * check if the given Entity is available in Memoria
	 *
	 * @param entity
	 * @return
	 */
	public static boolean isAvailableInMemoria(AbstractEntity entity) {
		return entity instanceof Item
		   || entity instanceof Location
		   || entity instanceof Person
		   || entity instanceof Plot
		   || entity instanceof Scene
		   || entity instanceof Tag;
	}

	/**
	 * get the tool tip for the given entity
	 *
	 * @param entity
	 * @return
	 */
	public static String getTooltip(AbstractEntity entity) {
		StringBuilder buf = new StringBuilder();
		buf.append(Html.HTML_B);
		buf.append(Html.HEAD_B)
		   .append(Html.STYLE_B).append(Html.FONT_SIZE_DEFAULT).append(Html.STYLE_E)
		   .append(Html.HEAD_E);
		buf.append(Html.BODY_B);
		if (entity.getName().trim().isEmpty()) {
			buf.append(Html.intoH(2, I18N.getMsg(entity.getObjType().toString())));
		} else {
			buf.append("<table width=\"300\">");
			buf.append(Html.TR_B).append(Html.TD_B);
			buf.append(Html.intoB(I18N.getColonMsg(entity.getObjType().toString())))
			   .append(" ")
			   .append(entity.getName());
			buf.append(Html.BR);
			switch (Book.getTYPE(entity)) {
				case LOCATION:
					Locations.tooltip(buf, (Location) entity);
					break;
				case PERSON:
					Persons.tooltip(buf, (Person) entity);
					break;
				case PLOT:
					Plots.tooltip(buf, (Plot) entity);
					break;
				case RELATION:
					Relations.tooltip(buf, (Relation) entity);
					break;
				case SCENE:
					Scenes.tooltip(buf, (Scene) entity);
					break;
				case ITEM:
					Items.tooltip(buf, (Item) entity);
					break;
				case TAG:
					Tags.tooltip(buf, (Tag) entity);
					break;
				case STRAND:
					Strands.tooltip(buf, (Strand) entity);
					break;
				default:
					break;
			}
			buf.append(Html.TD_E)
			   .append(Html.TR_E)
			   .append(Html.TABLE_E);
		}
		buf.append(Html.BODY_E);
		buf.append(Html.HTML_E);
		return buf.toString();
	}

	/**
	 * get info for the given Entity to delete, if no warning then delete
	 *
	 * @param mainFrame
	 * @param entity
	 * @return
	 */
	public static String getDeleteInfo(MainFrame mainFrame, AbstractEntity entity) {
		StringBuilder buf = new StringBuilder();
		boolean warnings = addDeletionInfo(mainFrame, entity, buf);
		if (warnings) {
			buf.append(Html.getHr());
		}
		buf.append(entity.toDetail(0));
		return buf.toString();
	}

	/**
	 * collect delete info for the given Entity
	 *
	 * @param mainFrame
	 * @param entity
	 * @param buf
	 * @return
	 */
	private static boolean addDeletionInfo(MainFrame mainFrame,
	   AbstractEntity entity, StringBuilder buf) {
		boolean warnings = false;
		switch (entity.getObjType()) {
			case CATEGORY: {
				Category category = (Category) entity;
				List<Person> persons = mainFrame.project.persons.findByCategory(category);
				if (!persons.isEmpty()) {
					buf.append(HTML_P_START);
					buf.append(Html.getWarning(I18N.getMsg("category.delete.warning")));
					buf.append(Html.P_E);
					buf.append(Html.BR);
					buf.append(I18N.getColonMsg("persons"));
					buf.append(Html.BR);
					buf.append(Html.UL_B);
					for (Person person : persons) {
						buf.append(Html.intoLI(person.toString()));
					}
					buf.append(Html.UL_E);
					warnings = true;
				}
				break;
			}
			case CHAPTER: {
				Chapter chapter = (Chapter) entity;
				List<Scene> scenes = mainFrame.project.scenes.find(chapter);
				if (!scenes.isEmpty()) {
					buf.append(HTML_P_START);
					buf.append(Html.getWarning(I18N
					   .getMsg("warning.chapter.has.assigned.scenes")));
					buf.append(Html.P_E);
					buf.append(Html.BR);
					buf.append(I18N.getColonMsg("scenes"));
					buf.append(Html.BR);
					buf.append(Html.UL_B);
					for (Scene scene : scenes) {
						buf.append(Html.intoLI(scene.toString()));
					}
					buf.append(Html.UL_E);
					warnings = true;
				}
				break;
			}
			case GENDER: {
				Gender gender = (Gender) entity;
				List<Person> persons = mainFrame.project.persons.findByGender(gender);
				if (!persons.isEmpty()) {
					buf.append(HTML_P_START);
					buf.append(Html.getWarning(I18N.getMsg("gender.delete.warning")));
					buf.append(Html.P_E);
					buf.append(Html.BR);
					buf.append(I18N.getColonMsg("persons"));
					buf.append(Html.BR);
					buf.append(Html.UL_B);
					for (Person person : persons) {
						buf.append(Html.intoLI(person.toString()));
					}
					buf.append(Html.UL_E);
					warnings = true;
				}
				break;
			}
			case ITEM: {
				Item item = (Item) entity;
				List<Scene> links = mainFrame.project.scenes.findBy(item);
				if (!links.isEmpty()) {
					buf.append(HTML_P_START);
					buf.append(Html.getWarning(I18N.getMsg("itemlink.warning.delete.all")));
					buf.append(Html.P_E);
					buf.append(Html.BR);
					buf.append(I18N.getColonMsg("itemlink"));
					buf.append(Html.BR);
					buf.append(Html.UL_B);
					for (Scene link : links) {
						buf.append(Html.intoLI(link.toString()));
					}
					buf.append(Html.UL_E);
					warnings = true;
				}
				break;
			}
			case PART: {
				Part part = (Part) entity;
				List<Chapter> chapters = mainFrame.project.chapters.find(part);
				if (!chapters.isEmpty()) {
					buf.append(HTML_P_START);
					buf.append(Html.getWarning(I18N.getMsg("manage.parts.delete.warning")));
					buf.append(Html.P_E);
					buf.append(Html.BR);
					buf.append(I18N.getColonMsg("chapters"));
					buf.append(Html.BR);
					buf.append(Html.UL_B);
					for (Chapter chapter : chapters) {
						buf.append(Html.intoLI(chapter.toString()));
					}
					buf.append(Html.UL_E);
					warnings = true;
				}
				break;
			}
			case SCENE: {
				Scene scene = (Scene) entity;
				List<Scene> scenes
				   = mainFrame.project.scenes.findBy(scene);
				if (!scenes.isEmpty()) {
					buf.append(HTML_P_START);
					buf.append(Html.getWarning(I18N.getMsg("scene.relativedate.delete.warning")));
					buf.append(Html.P_E);
					buf.append(Html.BR);
					buf.append(I18N.getColonMsg("scenes"));
					buf.append(Html.BR);
					buf.append(Html.UL_B);
					for (Scene scene2 : scenes) {
						buf.append(Html.intoLI(scene2.toString()));
					}
					buf.append(Html.UL_E);
					warnings = true;
				}
				break;
			}
			case STRAND: {
				Strand strand = (Strand) entity;
				List<Episode> episodes
				   = mainFrame.project.episodes.find(strand);
				if (!episodes.isEmpty()) {
					buf.append(HTML_P_START);
					buf.append(Html.getWarning(I18N.getMsg("manage.episodes.delete.warning")));
					buf.append(Html.P_E);
					warnings = true;
				}
				List<Scene> scenes = mainFrame.project.scenes.findBy(strand);
				if (!scenes.isEmpty()) {
					buf.append(HTML_P_START);
					buf.append(Html.getWarning(I18N.getMsg("manage.strands.delete.warning")));
					buf.append(Html.P_E);
					buf.append(Html.BR);
					buf.append(I18N.getColonMsg("scenes"));
					buf.append(Html.BR);
					buf.append(Html.UL_B);
					for (Scene scene : scenes) {
						buf.append(Html.intoLI(scene.toString()));
					}
					buf.append(Html.UL_E);
					warnings = true;
				}
				break;
			}
			case TAG: {
				Tag tag = (Tag) entity;
				List<Scene> links = mainFrame.project.scenes.findBy(tag);
				if (!links.isEmpty()) {
					buf.append(HTML_P_START);
					buf.append(Html.getWarning(I18N.getMsg("taglink.warning.delete.all")));
					buf.append(Html.P_E);
					buf.append(Html.BR);
					buf.append(I18N.getColonMsg("taglink"));
					buf.append(Html.BR);
					buf.append(Html.UL_B);
					for (Scene link : links) {
						buf.append(Html.intoLI(link.toString()));
					}
					buf.append(Html.UL_E);
					warnings = true;
				}
				break;
			}
			default:
				break;
		}
		return warnings;
	}

	/**
	 * refresh the given Entity
	 *
	 * @param mainFrame
	 * @param entity
	 */
	@SuppressWarnings("UnusedAssignment")
	public static void refresh(MainFrame mainFrame, AbstractEntity entity) {
		if (entity == null) {
			return;
		}
		entity = mainFrame.project.get(entity.getObjType(), entity.getId());
	}

	/**
	 * fill a JComboBox for the given Entity type
	 *
	 * @param mainFrame
	 * @param cb
	 * @param type
	 * @param toSel
	 * @param isNew
	 * @param addEmptyItem
	 */
	@SuppressWarnings("unchecked")
	public static void cbFill(MainFrame mainFrame, JComboBox cb,
	   Book.TYPE type, AbstractEntity toSel,
	   boolean isNew, boolean addEmptyItem) {
		cb.removeAllItems();
		cb.setRenderer(new EntityLCR());
		if (addEmptyItem) {
			cb.addItem("");
		}
		List<AbstractEntity> entities = (List) mainFrame.project.getList(type);
		entities.forEach(entity -> cb.addItem(entity));
		if (toSel != null) {
			cb.setSelectedItem(toSel);
		}
	}

	/**
	 * get the simple name of the class for the given Entity
	 *
	 * @param entity
	 * @return
	 */
	public static String getClassName(AbstractEntity entity) {
		if (entity == null) {
			return "null";
		}
		return entity.getClass().getSimpleName().toLowerCase();
	}

	/**
	 * get the list of ID as a String for the given Entities
	 *
	 * @param entities
	 * @return
	 */
	public static String getIds(List<?> entities) {
		if (entities == null) {
			return ("");
		}
		List<String> list = new ArrayList<>();
		for (Object a : entities) {
			if (a != null) {
				list.add(((AbstractEntity) a).getId().toString());
			}
		}
		return ListUtil.join(list, ",");
	}

	/**
	 * get next Entity for the given Entity
	 *
	 * @param mainFrame
	 * @param entity
	 * @return
	 */
	public static AbstractEntity getNext(MainFrame mainFrame, AbstractEntity entity) {
		List entities = (List) mainFrame.project.getList(entity.getObjType());
		if (entities != null) {
			for (int i = 0; i < entities.size(); i++) {
				AbstractEntity e = (AbstractEntity) entities.get(i);
				if (entity.getId().equals(e.getId())) {
					if (i > entities.size() - 2) {
						return (null);
					}
					return ((AbstractEntity) entities.get(i + 1));
				}
			}
		}
		return (null);
	}

	/**
	 * get the prior Entity for the given Entity
	 *
	 * @param mainFrame
	 * @param entity
	 * @return
	 */
	public static AbstractEntity getPrior(MainFrame mainFrame, AbstractEntity entity) {
		if (entity == null) {
			return (null);
		}
		List entities = (List) mainFrame.project.getList(entity.getObjType());
		if (entities != null) {
			for (int i = 0; i < entities.size(); i++) {
				AbstractEntity e = (AbstractEntity) entities.get(i);
				if (entity.getId().equals(e.getId())) {
					if (i < 1) {
						return (null);
					}
					return ((AbstractEntity) entities.get(i - 1));
				}
			}
		}
		return (null);
	}

	/**
	 * create a new Entity from the given Entity template
	 *
	 * @param entity
	 * @return
	 */
	public static AbstractEntity createNewEntity(AbstractEntity entity) {
		return (entity == null ? null : createNewEntity(entity.getObjType()));
	}

	/**
	 * create a new Entity for the given TYPE
	 *
	 * @param type
	 * @return
	 */
	public static AbstractEntity createNewEntity(Book.TYPE type) {
		AbstractEntity n = null;
		switch (type) {
			case ATTRIBUTE:
				n = new Attribute();
				break;
			case CATEGORY:
				n = new Category();
				break;
			case CHAPTER:
				n = new Chapter();
				break;
			case ENDNOTE:
				n = new Endnote();
				break;
			case EVENT:
				n = new Event();
				break;
			case GENDER:
				n = new Gender();
				break;
			case IDEA:
				n = new Idea();
				break;
			case ITEM:
				n = new Item();
				break;
			case LOCATION:
				n = new Location();
				break;
			case MEMO:
				n = new Memo();
				break;
			case PART:
				n = new Part();
				break;
			case PERSON:
				n = new Person();
				break;
			case PLOT:
				n = new Plot();
				break;
			case RELATION:
				n = new Relation();
				break;
			case SCENE:
				n = new Scene();
				break;
			case STRAND:
				n = new Strand();
				break;
			case STATUS:
				n = new Status();
				break;
			case TAG:
				n = new Tag();
				break;
			default:
				break;
		}
		return n;
	}

	/**
	 * get a String containing the list of names of the given Entities
	 *
	 * @param entities
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String getNames(List<?> entities) {
		//LOG.printInfos(TT+".getListName(entities nb="+(entities==null?"null":entities.size())+")");
		if (entities == null || entities.isEmpty()) {
			return ("");
		}
		List<String> list = new ArrayList<>();
		for (AbstractEntity e : (List<AbstractEntity>) entities) {
			list.add(e.getName());
		}
		return ListUtil.join(list);
	}

}
