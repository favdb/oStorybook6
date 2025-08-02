/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun, 2015 FaVdB

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
package storybook.model;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import storybook.ctrl.Ctrl;
import static storybook.ctrl.Ctrl.PROPS.*;
import storybook.db.abs.AbstractEntity;
import storybook.db.attribute.Attribute;
import storybook.db.book.Book;
import static storybook.db.book.Book.TYPE.*;
import storybook.db.book.BookUtil;
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
import storybook.db.scene.SceneStatus;
import storybook.db.scene.Scenes;
import storybook.db.strand.Strand;
import storybook.db.tag.Tag;
import storybook.project.Project;
import storybook.tools.DateUtil;
import storybook.tools.LOG;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.FontUtil;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.SbView;
import storybook.ui.SbView.VIEWNAME;
import static storybook.ui.SbView.VIEWNAME.*;
import storybook.ui.panel.book.BookPanel;
import storybook.ui.panel.manage.Manage;
import storybook.ui.panel.reading.ReadingPanel;

/**
 * class for the project Model
 *
 * @author martin
 *
 */
public class Model extends AbstractModel {

	private final static String TT = "Model.";

	public Model(MainFrame m) {
		super(m);
		name = "book";
	}

	/**
	 * initialize the database with basics Entities for Import
	 *
	 * @param title
	 */
	public synchronized void initEntities(String title) {
		//LOG.printInfos(TT+".initEntities()");
		// default strand
		Strand strand = new Strand(1,
				I18N.getMsg("strand.name.init_value"),
				I18N.getMsg("strand.abbr.init_value"),
				ColorUtil.PALETTE.LIGHT_BLUE.getRGB(), "");
		mainFrame.project.write(strand);
		// default genders
		mainFrame.project.write(new Gender(I18N.getMsg("person.gender.male"), 6, 12, 18, 65));
		mainFrame.project.write(new Gender(I18N.getMsg("person.gender.female"), 6, 12, 18, 65));
		// default categories
		mainFrame.project.write(new Category(1, I18N.getMsg("category.central_character"), null));
		mainFrame.project.write(new Category(2, I18N.getMsg("category.minor_character"), null));
	}

	/**
	 * initialize the basics Entities for a new Project
	 *
	 * @param nature
	 * @param nbParts
	 * @param nbChapters
	 * @param objective
	 */
	public synchronized void initEntities(int nature, int nbParts, int nbChapters, Date objective) {
		//LOG.printInfos(TT+".initEntities()");
		// default strand
		Strand strand = new Strand(1,
				I18N.getMsg("strand.name.init_value"),
				I18N.getMsg("strand.abbr.init_value"),
				ColorUtil.PALETTE.LIGHT_BLUE.getRGB(), "");
		mainFrame.project.write(strand);
		// default part
		for (int i = 1; i <= nbParts; i++) {
			Part part = new Part(i, String.format("%s %d", I18N.getMsg("part"), i));
			if (objective != null) {
				part.setObjectiveChars(BookUtil.computeObjectiveChars(nature, nbParts));
				part.setObjectiveTime(DateUtil.addDateTimeToTS(objective, null));
			}
			mainFrame.project.write(part);
		}
		// first chapter
		Part defPart = (Part) mainFrame.project.parts.getList().get(0);
		for (int i = 1; i <= nbChapters; i++) {
			Chapter chapter = new Chapter(defPart, i, String.format("%s %d", I18N.getMsg("chapter"), i));
			if (objective != null) {
				chapter.setObjectiveChars(BookUtil.computeObjectiveChars(nature, nbChapters));
				chapter.setObjectiveTime(DateUtil.addDateTimeToTS(objective, null));
			}
			mainFrame.project.write(chapter);
		}
		Chapter chapter = (Chapter) mainFrame.project.chapters.getList().get(0);
		// first scene
		mainFrame.project.write(Scenes.create(mainFrame.project.scenes.getLast() + 1L, strand, chapter));
		// default genders
		mainFrame.project.write(new Gender(I18N.getMsg("person.gender.male"), 6, 12, 18, 65));
		mainFrame.project.write(new Gender(I18N.getMsg("person.gender.female"), 6, 12, 18, 65));
		// default categories
		mainFrame.project.write(new Category(1, I18N.getMsg("category.central_character"), null));
		mainFrame.project.write(new Category(2, I18N.getMsg("category.minor_character"), null));
	}

	/**
	 * print the view
	 *
	 * @param view
	 */
	public void print(SbView view) {
		firePropertyChange(Ctrl.PROPS.PRINT.toString(), null, view);
	}

	/**
	 * fire all
	 */
	@Override
	public void fireAgain() {
		//LOG.trace(TT + "fireAgain()");
		initProgress();
		new Thread(new ExecThread()).start();
	}

	public void fireAgain(SbView view) {
		//LOG.trace(TT + "fireAgain(view=" + view.getName() + ")");
		if (view == null || !view.isLoaded()) {
			return;
		}
		switch (SbView.getVIEW(view.getName())) {
			case BOOK:
				fireAgainScenes();
				break;
			case CHRONO:
				fireAgainScenes();
				break;
			case MANAGE:
				fireAgainChapters();
				break;
			case READING:
				fireAgainChapters();
				break;
			case TIMELINE:
				fireAgainScenes();
				break;

			case ATTRIBUTES:
				fireAgainAttributes();
				break;
			case CATEGORIES:
				fireAgainCategories();
				break;
			case CHAPTERS:
				fireAgainChapters();
				break;
			case ENDNOTES:
				fireAgainEndnotes();
				break;
			case EPISODES:
				fireAgainEpisodes();
				break;
			case EVENTS:
				fireAgainEvents();
				break;
			case GENDERS:
				fireAgainGenders();
				break;
			case IDEAS:
				fireAgainIdeas();
				break;
			case INTERNALS:
				fireAgainInternals();
			case ITEMS:
				fireAgainItems();
				break;
			/*case ITEMLINKS:
				fireAgainItemlinks();
				break;*/
			case LOCATIONS:
				fireAgainLocations();
				break;
			case MEMOS:
				fireAgainMemos();
				break;
			case PARTS:
				fireAgainParts();
				break;
			case PERSONS:
				fireAgainPersons();
				break;
			case PLOTS:
				fireAgainPlots();
				break;
			case RELATIONS:
				fireAgainRelations();
				break;
			case SCENES:
				fireAgainScenes();
				break;
			case STRANDS:
				fireAgainStrands();
				break;
			case TAGS:
				fireAgainTags();
				break;
			/*case TAGLINKS:
				fireAgainTaglinks();
				break;*/
			default:
				break;
		}
	}

	public void fireAgainReading() {
		fireAgainParts();
		fireAgainChapters();
		fireAgainScenes();
	}

	private void fireAgainAttributes() {
		//LOG.printInfos(TT+".fireAgainAttributes()");
		firePropertyChange(ATTRIBUTE);
	}

	private void fireAgainCategories() {
		//LOG.printInfos(TT+".fireAgainCategories()");
		firePropertyChange(CATEGORY);
	}

	public void fireAgainChapters() {
		//LOG.printInfos(TT+".fireAgainChapters()");
		firePropertyChange(CHAPTER);
	}

	private void fireAgainEndnotes() {
		//LOG.printInfos(TT+".fireAgainEndnotes()");
		firePropertyChange(ENDNOTE);
	}

	private void fireAgainEpisodes() {
		//LOG.printInfos(TT+".fireAgainEndnotes()");
		firePropertyChange(EPISODE);
	}

	private void fireAgainEvents() {
		//LOG.printInfos(TT+".fireAgainEvents()");
		firePropertyChange(EVENT);
	}

	private void fireAgainGenders() {
		//LOG.printInfos(TT+".fireAgainGenders()");
		firePropertyChange(GENDER);
	}

	private void fireAgainIdeas() {
		//LOG.printInfos(TT+".fireAgainIdeas()");
		firePropertyChange(IDEA);
	}

	private void fireAgainInternals() {
		//LOG.printInfos(TT+".fireAgainInternals()");
		firePropertyChange(INTERNAL);
	}

	private void fireAgainItems() {
		//LOG.printInfos(TT+".fireAgainItems()");
		firePropertyChange(ITEM);
	}

	/*private void fireAgainItemlinks() {
		//LOG.printInfos(TT+".fireAgainItemlinks()");
		firePropertyChange(ITEMLINK);
	}*/
	private void fireAgainLocations() {
		//LOG.printInfos(TT+".fireAgainLocations()");
		firePropertyChange(LOCATION);
	}

	private void fireAgainMemos() {
		//LOG.printInfos(TT+".fireAgainMemos()");
		firePropertyChange(MEMO);
	}

	public void fireAgainParts() {
		//LOG.printInfos(TT+".fireAgainParts()");
		firePropertyChange(PART);
	}

	private void fireAgainPersons() {
		//LOG.printInfos(TT+".fireAgainPersons()");
		firePropertyChange(PERSON);
	}

	private void fireAgainPlots() {
		//LOG.printInfos(TT+".fireAgainPlots()");
		firePropertyChange(PLOT);
	}

	private void fireAgainRelations() {
		//LOG.printInfos(TT+".fireAgainRelations()");
		firePropertyChange(RELATION);
	}

	public void fireAgainScenes() {
		//LOG.printInfos(TT+".fireAgainScenes()");
		firePropertyChange(SCENE);
	}

	private void fireAgainStrands() {
		//LOG.printInfos(TT+".fireAgainStrands()");
		firePropertyChange(STRAND);
	}

	private void fireAgainTags() {
		//LOG.printInfos(TT+".fireAgainTags()");
		firePropertyChange(TAG);
	}

	/*private void fireAgainTaglinks() {
		//LOG.printInfos(TT+".fireAgainTaglinks()");
		firePropertyChange(TAGLINK);
	}*/
	// common
	public void setRefresh(SbView view) {
		//LOG.printInfos(TT+".setRefresh(" + view.getName() + ")");
		firePropertyChange(Ctrl.PROPS.REFRESH.toString(), null, view);
		try {
			if (view.getComponentCount() == 0) {
				return;
			}
			Component comp = view.getComponent();
			if (comp instanceof BookPanel
					|| comp instanceof Manage
					|| comp instanceof ReadingPanel) {
				// these views don't need a "fire again"
				return;
			}
			fireAgain(view);
		} catch (ArrayIndexOutOfBoundsException e) {
			// ignore
		}
	}

	/**
	 * show the options of the given view
	 *
	 * @param view
	 */
	public void showOptions(SbView view) {
		firePropertyChange(Ctrl.PROPS.SHOWOPTIONS.toString(), null, view);
	}

	/**
	 * show the given Entity into Info
	 *
	 * @param entity
	 */
	public void InfoShow(AbstractEntity entity) {
		//LOG.trace(TT + "InfoShow(entity=" + LOG.trace(entity));
		firePropertyChange(Ctrl.PROPS.SHOWINFO.toString(), null, entity);
	}

	/**
	 * show the H2db file properties into Info
	 *
	 * @param dbFile
	 */
	public void InfoShow(Project dbFile) {
		firePropertyChange(Ctrl.PROPS.SHOWINFO.toString(), null, dbFile);
	}

	// memoria view
	/**
	 * change layout of the Memoria
	 *
	 * @param val
	 */
	public void MemoriaLayout(Integer val) {
		firePropertyChange(Ctrl.PROPS.MEMORIA_LAYOUT.toString(), null, val);
	}

	/**
	 * show the given Entity into Memoria
	 *
	 * @param entity
	 */
	public void MemoriaShowEntity(AbstractEntity entity) {
		firePropertyChange(Ctrl.PROPS.SHOWINMEMORIA.toString(), null, entity);
	}

	/**
	 * unload the editor
	 *
	 */
	public void setUnloadEditor() {
		firePropertyChange(Ctrl.PROPS.UNLOADEDITOR.toString(), null, null);
	}

	/**
	 * change Scene filter to the given status
	 *
	 * @param state
	 */
	public void setSceneFilter(SceneStatus state) {
		firePropertyChange(Ctrl.PROPS.SCENE_FILTER_STATUS.toString(), null, state);
	}

	/**
	 * change the filter to the given Strand
	 *
	 * @param strand
	 */
	public void setStrandFilter(String strand) {
		firePropertyChange(Ctrl.PROPS.SCENE_FILTER_STRAND.toString(), null, strand);
	}

	/**
	 * print the view
	 *
	 * @param view
	 */
	public void setPrint(SbView view) {
		firePropertyChange(Ctrl.PROPS.PRINT.toString(), null, view);
	}

	/**
	 * export the view
	 *
	 * @param view
	 */
	public void setExport(SbView view) {
		//TODO set export for the given SbView
	}

	/**
	 * change the zoom of the Chrono
	 *
	 * @param val
	 */
	public void ChronoZoom(Integer val) {
		firePropertyChange(Ctrl.PROPS.CHRONO_ZOOM.toString(), null, val);
	}

	/**
	 * change layout direction of Chrono
	 *
	 * @param val
	 */
	public void ChronoLayoutDirection(Boolean val) {
		firePropertyChange(Ctrl.PROPS.CHRONO_LAYOUTDIRECTION.toString(), null, val);
	}

	/**
	 * show date difference in Chrono
	 *
	 * @param val : true=yes, false=no
	 */
	public void ChronoShowDateDifference(Boolean val) {
		firePropertyChange(Ctrl.PROPS.CHRONO_SHOWDATEDIFFERENCE.toString(), null, val);
	}

	/**
	 * show the given Entity into Chrono
	 *
	 * @param entity
	 */
	public void ChronoShowEntity(AbstractEntity entity) {
		firePropertyChange(Ctrl.PROPS.CHRONO_SHOWENTITY.toString(), null, entity);
	}

	//*****************************
	//** Book view               **
	//*****************************
	/**
	 * change the zoom for Book
	 *
	 * @param val : zoom level
	 */
	public void BookZoom(Integer val) {
		firePropertyChange(Ctrl.PROPS.BOOK_ZOOM.toString(), null, val);
	}

	/**
	 * show the given Entity into Book
	 *
	 * @param entity
	 */
	public void BookShowEntity(AbstractEntity entity) {
		firePropertyChange(Ctrl.PROPS.BOOK_SHOWENTITY.toString(), null, entity);
	}

	//*****************************
	//** Manage view
	//*****************************
	/**
	 * change the zoom for Manage
	 *
	 * @param val : zoom level
	 */
	public void ManageWidth(Integer val) {
		firePropertyChange(Ctrl.PROPS.MANAGE_ZOOM.toString(), null, val);
	}

	/**
	 * change number of columns in Manage
	 *
	 * @param val
	 */
	public void ManageColumns(Integer val) {
		firePropertyChange(Ctrl.PROPS.MANAGE_COLUMNS.toString(), null, val);
	}

	/**
	 * hide unassigned Scene in Manage
	 */
	public void ManageHideUnassigned() {
		firePropertyChange(Ctrl.PROPS.MANAGE_HIDEUNASSIGNED.toString(), null, null);
	}

	/**
	 * change disposition to vertical in Manage
	 */
	public void ManageVertical() {
		firePropertyChange(Ctrl.PROPS.MANAGE_VERTICAL.toString(), null, null);
	}

	/**
	 * show the given Entity into Manage
	 *
	 * @param entity
	 */
	public void ManageShowEntity(AbstractEntity entity) {
		firePropertyChange(Ctrl.PROPS.MANAGE_SHOWENTITY.toString(), null, entity);
	}

	//*****************************
	//** Reading view
	//*****************************
	/**
	 * change the zoom level in Reading
	 *
	 * @param val
	 */
	public void ReadingZoom(Integer val) {
		firePropertyChange(Ctrl.PROPS.READING_LAYOUT.toString(), null, val);
	}

	/**
	 * change the font size in Reading
	 *
	 * @param val
	 */
	public void ReadingFontSize(Integer val) {
		firePropertyChange(Ctrl.PROPS.READING_FONTSIZE.toString(), null, val);
	}

	//*****************************
	//** Storyboard view
	//*****************************
	/**
	 * change the direction for Storyboard
	 */
	public void StoryboardDirection() {
		firePropertyChange(Ctrl.PROPS.STORYBOARD_DIRECTION.toString(), null, null);
	}

	//*****************************
	//** Timeline view
	//*****************************
	/**
	 * change the zoom for Timeline
	 *
	 * @param val : zoom level
	 */
	public void TimelineZoom(Integer val) {
		firePropertyChange(Ctrl.PROPS.TIMELINE_ZOOM.toString(), null, val);
	}

	/**
	 * change options for Timeline
	 *
	 * @param val
	 */
	public void TimelineOptions(String val) {
		firePropertyChange(Ctrl.PROPS.TIMELINE_OPTIONS.toString(), null, val);
	}

	//*****************************
	//** Entities actions
	//*****************************
	/**
	 * update the given Entity
	 *
	 * @param entity
	 */
	public synchronized void ENTITY_Update(AbstractEntity entity) {
		//LOG.trace(TT + "ENTITY_Update(entity=" + entity.toString() + ")");
		entity.setMaj();
		mainFrame.project.write(entity);
		firePropertyChange(entity.getObjType(), UPDATE, null, entity);
	}

	/**
	 * create a new Entity like the given one
	 *
	 * @param entity
	 */
	public synchronized void ENTITY_New(AbstractEntity entity) {
		entity.setCreation();
		entity.setMaj();
		mainFrame.project.write(entity);
		firePropertyChange(entity.getObjType(), NEW, null, entity);
	}

	/**
	 * delete the given Entity
	 *
	 * @param entity
	 */
	public synchronized void ENTITY_Delete(AbstractEntity entity) {
		if (entity == null) {
			return;
		}
		switch (Book.getTYPE(entity)) {
			case ATTRIBUTE:
				ATTRIBUTE_Delete((Attribute) entity);
				break;
			case CATEGORY:
				CATEGORY_Delete((Category) entity);
				break;
			case CHAPTER:
				CHAPTER_Delete((Chapter) entity);
				break;
			case ENDNOTE:
				ENDNOTE_Delete((Endnote) entity);
				break;
			case EPISODE:
				EPISODE_Delete((Episode) entity);
				break;
			case EVENT:
				EVENT_Delete((Event) entity);
				break;
			case GENDER:
				GENDER_Delete((Gender) entity);
				break;
			case IDEA:
				IDEA_Delete((Idea) entity);
				break;
			case ITEM:
				ITEM_Delete((Item) entity);
				break;
			case LOCATION:
				LOCATION_Delete((Location) entity);
				break;
			case MEMO:
				MEMO_Delete((Memo) entity);
				break;
			case PART:
				PART_Delete((Part) entity);
				break;
			case PERSON:
				PERSON_Delete((Person) entity);
				break;
			case PLOT:
				PLOT_Delete((Plot) entity);
				break;
			case RELATION:
				RELATION_Delete((Relation) entity);
				break;
			case SCENE:
				SCENE_Delete((Scene) entity);
				break;
			case STRAND:
				STRAND_Delete((Strand) entity);
				break;
			case TAG:
				TAG_Delete((Tag) entity);
				break;
			default:
				LOG.err("ENTITY_Delete type not found: " + entity.getObjType());
				return;
		}
		mainFrame.setUpdated();
	}

	/**
	 * delete the given Entities list ID
	 *
	 * @param type
	 * @param ids
	 */
	public synchronized void ENTITY_Delete(Book.TYPE type, ArrayList<Long> ids) {
		switch (type) {
			case ATTRIBUTE:
				Model.this.ATTRIBUTE_Delete(ids);
				break;
			case CATEGORY:
				CATEGORY_Delete(ids);
				break;
			case CHAPTER:
				Model.this.CHAPTER_Delete(ids);
				break;
			case ENDNOTE:
				ENDNOTE_Delete(ids);
				break;
			case EVENT:
				EVENT_Delete(ids);
				break;
			case GENDER:
				GENDER_Delete(ids);
				break;
			case IDEA:
				IDEA_Delete(ids);
				break;
			case ITEM:
				ITEM_Delete(ids);
				break;
			case LOCATION:
				LOCATION_Delete(ids);
				break;
			case MEMO:
				MEMO_Delete(ids);
				break;
			case PART:
				PART_Delete(ids);
				break;
			case PERSON:
				PERSON_Delete(ids);
				break;
			case PLOT:
				PLOT_Delete(ids);
				break;
			case RELATION:
				RELATION_Delete(ids);
				break;
			case SCENE:
				SCENE_Delete(ids);
				break;
			case STRAND:
				STRAND_Delete(ids);
				break;
			case TAG:
				TAG_Delete(ids);
				break;
			default:
				LOG.err("ENTITY_Delete type not found: " + type);
				break;
		}
	}

	public synchronized void ATTRIBUTE_Delete(Attribute entity) {
		if (entity.getId() == null) {
			return;
		}
		List<Person> persons = mainFrame.project.persons.findByAttribute(entity);
		for (Person person : persons) {
			if (person.getAttributes().contains(entity)) {
				person.getAttributes().remove(entity);
				ENTITY_Update(person);
			}
		}
		// delete attribute
		mainFrame.project.attributes.delete(entity);
		firePropertyChange(ATTRIBUTE, DELETE, entity, null);
	}

	public synchronized void ATTRIBUTE_Delete(ArrayList<Long> ids) {
		for (Long id : ids) {
			Attribute old = (Attribute) mainFrame.project.attributes.get(id);
			ATTRIBUTE_Delete(old);
		}
	}

	// category
	public synchronized void CATEGORY_Delete(Category category) {
		if (category.getId() == null) {
			return;
		}
		// set category of affected persons to "minor"
		Category minor = (Category) mainFrame.project.categorys.getList().get(0);
		List<Person> persons = mainFrame.project.persons.findByCategory(category);
		for (Person person : persons) {
			person.setCategory(minor);
			ENTITY_Update(person);
		}
		// delete category
		mainFrame.project.categorys.delete(category);
		firePropertyChange(CATEGORY, DELETE, category, null);
	}

	public synchronized void CATEGORY_Delete(ArrayList<Long> ids) {
		for (Long id : ids) {
			Category old = (Category) mainFrame.project.categorys.get(id);
			CATEGORY_Delete(old);
		}
	}

	public synchronized void CATEGORY_OrderDown(Category category) {
		firePropertyChange(CATEGORY, ORDERDOWN, null, category);
	}

	public synchronized void CATEGORY_OrderUp(Category category) {
		firePropertyChange(CATEGORY, ORDERUP, null, category);
	}

	// chapter
	public synchronized void CHAPTER_Delete(Chapter entity) {
		if (entity.getId() == null) {
			return;
		}
		// find scenes, set chapter to null
		List<Scene> scenes = mainFrame.project.scenes.find(entity);
		for (Scene scene : scenes) {
			scene.setChapter();
			ENTITY_Update(scene);
		}
		// delete chapter
		mainFrame.project.chapters.delete(entity);
		firePropertyChange(CHAPTER, DELETE, entity, null);
	}

	public synchronized void CHAPTER_Delete(ArrayList<Long> ids) {
		for (Long id : ids) {
			Chapter old = (Chapter) mainFrame.project.chapters.get(id);
			Model.this.CHAPTER_Delete(old);
		}
	}

	// endnote
	public synchronized void ENDNOTE_Delete(Endnote entity) {
		if (entity.getId() == null) {
			return;
		}
		mainFrame.project.endnotes.delete(entity);
		firePropertyChange(ENDNOTE, DELETE, entity, null);
	}

	public synchronized void ENDNOTE_Delete(List<Long> ids) {
		for (Long id : ids) {
			Endnote old = (Endnote) mainFrame.project.endnotes.get(id);
			Model.this.ENDNOTE_Delete(old);
		}
	}

	// episode
	public synchronized void EPISODE_Delete(Episode entity) {
		//LOG.trace(TT + "setEpisodeDelete(entity=" + App.traceEntity(entity) + ")");
		if (entity.getId() == null) {
			return;
		}
		mainFrame.project.episodes.delete(entity);
		// no fire property
		//firePropertyChange(EPISODE, DELETE, entity, null);
	}

	public synchronized void EPISODE_Delete(List<Long> ids) {
		// no delete multi episodes
		/*for (Long id : ids) {
			Session session = beginTransaction();
			EpisodeDAOImpl dao = new EpisodeDAOImpl(session);
			Episode old = dao.find(id);
			commit();
			setEpisodeDelete(old);
		}*/
	}

	// *****************
	// **** Event     **
	// *****************
	public synchronized void EVENT_Delete(Event entity) {
		if (entity.getId() == null) {
			return;
		}
		mainFrame.project.events.delete(entity);
		firePropertyChange(EVENT, DELETE, entity, null);
	}

	public synchronized void EVENT_Delete(ArrayList<Long> ids) {
		for (Long id : ids) {
			Event old = (Event) mainFrame.project.events.get(id);
			EVENT_Delete(old);
		}
	}

	////// Gender
	public synchronized void GENDER_Delete(Gender gender) {
		if (gender.getId() == null) {
			return;
		}
		// set gender of affected persons to "male"
		Gender male = (Gender) mainFrame.project.genders.get(0L);
		List<Person> persons = mainFrame.project.persons.findByGender(gender);
		for (Person person : persons) {
			person.setGender(male);
			ENTITY_Update(person);
		}
		// delete gender
		mainFrame.project.genders.delete(gender);
		firePropertyChange(GENDER, DELETE, gender, null);
	}

	public synchronized void GENDER_Delete(ArrayList<Long> ids) {
		for (Long id : ids) {
			Gender old = (Gender) mainFrame.project.genders.get(id);
			Model.this.GENDER_Delete(old);
		}
	}

	////// idea
	public void setIDEA_Edit(Idea entity) {
		ENTITY_Edit((AbstractEntity) entity);
	}

	public synchronized void IDEA_Delete(Idea idea) {
		if (idea.getId() == null) {
			return;
		}
		mainFrame.project.ideas.delete(idea);
		firePropertyChange(IDEA, DELETE, idea, null);
	}

	public synchronized void IDEA_Delete(ArrayList<Long> ids) {
		for (Long id : ids) {
			Idea old = (Idea) mainFrame.project.ideas.get(id);
			IDEA_Delete(old);
		}
	}

	////// Item
	public synchronized void ITEM_Delete(Item item) {
		if (item.getId() == null) {
			return;
		}
		// delete item
		mainFrame.project.items.delete(item);
		firePropertyChange(ITEM, DELETE, item, null);
	}

	public synchronized void ITEM_Delete(ArrayList<Long> ids) {
		for (Long id : ids) {
			Item old = (Item) mainFrame.project.items.get(id);
			ITEM_Delete(old);
		}
	}

	////// Location
	public synchronized void LOCATION_Delete(Location entity) {
		if (entity.getId() == null) {
			return;
		}
		// delete scene links
		List<Scene> scenes = mainFrame.project.scenes.findLocation(entity);
		for (Scene scene : scenes) {
			scene.getLocations().remove(entity);
			ENTITY_Update(scene);
		}
		// delete location
		mainFrame.project.locations.delete(entity);
		firePropertyChange(LOCATION, DELETE, entity, null);
	}

	public synchronized void LOCATION_Delete(ArrayList<Long> ids) {
		for (Long id : ids) {
			Location old = (Location) mainFrame.project.locations.get(id);
			LOCATION_Delete(old);
		}
	}

	////// Memo
	public synchronized void MEMO_Delete(Memo entity) {
		if (entity.getId() == null) {
			return;
		}
		mainFrame.project.memos.delete(entity);
		firePropertyChange(MEMO, DELETE, entity, null);
	}

	public synchronized void MEMO_Delete(ArrayList<Long> ids) {
		for (Long id : ids) {
			Memo old = (Memo) mainFrame.project.memos.get(id);
			MEMO_Delete(old);
		}
	}

	////// Part
	public synchronized void PART_Delete(Part entity) {
		if (entity.getId() == null) {
			return;
		}
		// delete chapters
		@SuppressWarnings("unchecked")
		List<Chapter> chapters = mainFrame.project.chapters.getList();
		for (Chapter chapter : chapters) {
			CHAPTER_Delete(chapter);
		}
		// delete part
		mainFrame.project.parts.delete(entity);
		firePropertyChange(PART, DELETE, entity, null);
	}

	public synchronized void PART_Delete(ArrayList<Long> ids) {
		for (Long id : ids) {
			Part old = (Part) mainFrame.project.parts.get(id);
			PART_Delete(old);
		}
	}

	public synchronized void PART_Change(Part entity) {
		firePropertyChange(PART, CHANGE, null, entity);
	}

	////// Person
	public synchronized void PERSON_Delete(Person entity) {
		if (entity.getId() == null) {
			return;
		}
		// delete scene links
		List<Scene> scenes = mainFrame.project.scenes.findPerson(entity);
		for (Scene scene : scenes) {
			scene.getPersons().remove(entity);
			ENTITY_Update(scene);
		}
		// delete person
		mainFrame.project.persons.delete(entity);
		firePropertyChange(PERSON, DELETE, entity, null);
	}

	public synchronized void PERSON_Delete(ArrayList<Long> ids) {
		for (Long id : ids) {
			Person old = (Person) mainFrame.project.persons.get(id);
			PERSON_Delete(old);
		}
	}

	////// Plot
	public synchronized void PLOT_Delete(Plot entity) {
		if (entity.getId() == null) {
			return;
		}
		// delete plot assignments
		List<Scene> scenes = mainFrame.project.scenes.findPlot(entity);
		for (Scene scene : scenes) {
			scene.getPlots().remove(entity);
			ENTITY_Update(scene);
		}
		// delete plot
		mainFrame.project.plots.delete(entity);
		firePropertyChange(PLOT, DELETE, entity, null);
	}

	public synchronized void PLOT_Delete(ArrayList<Long> ids) {
		for (Long id : ids) {
			Plot old = (Plot) mainFrame.project.plots.get(id);
			PLOT_Delete(old);
		}
	}

	////// Relation
	public synchronized void RELATION_Delete(Relation entity) {
		if (entity.getId() == null) {
			return;
		}
		mainFrame.project.relations.delete(entity);
		firePropertyChange(RELATION, DELETE, entity, null);
	}

	public synchronized void RELATION_Delete(ArrayList<Long> ids) {
		for (Long id : ids) {
			Relation old = (Relation) mainFrame.project.relations.get(id);
			RELATION_Delete(old);
		}
	}

	////// Scene
	public synchronized void SCENE_Delete(Scene entity) {
		if (entity.getId() == null) {
			return;
		}
		// remove relative scene of affected scenes
		List<Scene> scenes = mainFrame.project.scenes.findBy(entity);
		for (Scene scene2 : scenes) {
			scene2.removeRelativescene();
			ENTITY_Update(scene2);
		}
		// delete scene
		mainFrame.project.scenes.delete(entity);
		firePropertyChange(SCENE, DELETE, entity, null);
	}

	public synchronized void SCENE_Delete(List<Long> ids) {
		for (Long id : ids) {
			Scene old = (Scene) mainFrame.project.scenes.get(id);
			SCENE_Delete(old);
		}
	}

	////// Strand
	public synchronized void STRAND_Delete(Strand strand) {
		if (strand.getId() == null) {
			return;
		}
		@SuppressWarnings("unchecked")
		List<Strand> list = (List) mainFrame.project.getList(STRAND);
		Strand def = null;
		for (Strand s : list) {
			if (!s.equals(strand)) {
				def = s;
				break;
			}
		}
		// delete episode strand
		List<Episode> episodes = mainFrame.project.episodes.find(strand);
		for (Episode episode : episodes) {
			EPISODE_Delete(episode);
		}
		// delete scene strands
		List<Scene> scenes = mainFrame.project.scenes.findBy(strand);
		for (Scene scene : scenes) {
			if (scene.getStrands().contains(strand)) {
				scene.getStrands().remove(strand);
				ENTITY_Update(scene);
			}
		}
		// delete for scenes strand
		scenes = mainFrame.project.scenes.find(strand);
		for (Scene scene : scenes) {
			scene.setStrand(def);
			ENTITY_Update(scene);
		}
		// delete strand
		mainFrame.project.strands.delete(strand);
		firePropertyChange(STRAND, DELETE, strand, null);
	}

	public synchronized void STRAND_Delete(ArrayList<Long> ids) {
		for (Long id : ids) {
			Strand old = (Strand) mainFrame.project.strands.get(id);
			STRAND_Delete(old);
		}
	}

	public synchronized void STRAND_OrderUp(Strand strand) {
		firePropertyChange(STRAND, ORDERUP, null, strand);
	}

	public synchronized void STRAND_OrderDown(Strand strand) {
		firePropertyChange(STRAND, ORDERDOWN, null, strand);
	}

	////// Tag
	public synchronized void TAG_Delete(Tag tag) {
		if (tag.getId() == null) {
			return;
		}
		// delete tag
		mainFrame.project.tags.delete(tag);
		firePropertyChange(TAG, DELETE, tag, null);
	}

	public synchronized void TAG_Delete(ArrayList<Long> ids) {
		for (Long id : ids) {
			Tag old = (Tag) mainFrame.project.tags.get(id);
			TAG_Delete(old);
		}
	}

	/**
	 * refresh the Parts view
	 */
	public void refreshParts() {
		SbView v = mainFrame.getView(VIEWNAME.PARTS);
		if (v != null) {
			fireAgain(v);
		}
	}

	/**
	 * refresh the Chapters view
	 */
	public void refreshChapters() {
		SbView v = mainFrame.getView(VIEWNAME.CHAPTERS);
		if (v != null) {
			fireAgain(v);
		}
	}

	/**
	 * refresh the Scenes view
	 */
	public void refreshScenes() {
		SbView v = mainFrame.getView(VIEWNAME.SCENES);
		if (v != null) {
			fireAgain(v);
		}
	}

	private JFrame progressFrame;
	private JProgressBar progressBar;
	Book.TYPE toRefresh[] = {
		ATTRIBUTE,
		CATEGORY,
		CHAPTER,
		ENDNOTE,
		ENDNOTE,
		EPISODE,
		EVENT,
		GENDER,
		IDEA,
		ITEM,//ITEMLINK,
		LOCATION,
		MEMO,
		PART,
		PERSON,
		PLOT,
		RELATION,
		SCENE,
		STRAND,
		TAG//,TAGLINK
	};

	/**
	 * initialize the progress Frame
	 */
	private void initProgress() {
		progressFrame = new JFrame();
		progressFrame.setUndecorated(true);
		progressFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel pane = (JPanel) progressFrame.getContentPane();
		pane.setLayout(new MigLayout(MIG.WRAP));
		pane.add(new JLabel(I18N.getMsg("refresh")), MIG.CENTER);
		progressBar = new JProgressBar(0, toRefresh.length);
		progressBar.setMinimumSize(new Dimension(410, FontUtil.getHeight()));
		progressBar.setStringPainted(true);
		pane.add(progressBar, MIG.GROWX);
		progressFrame.pack();
		progressFrame.setLocationRelativeTo(mainFrame);
		progressFrame.setVisible(true);
	}

	public class ExecThread implements Runnable {

		public ExecThread() {
		}

		@Override
		public void run() {
			int i = 0, total = toRefresh.length;
			for (Book.TYPE type : toRefresh) {
				progressBar.setValue(i);
				firePropertyChange(type);
				i++;
				progressBar.setString(i + "/" + total
						+ " (" + I18N.getMsg(type.toString()) + ")");
			}
			progressFrame.dispose();
		}
	}

}
