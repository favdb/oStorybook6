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
package storybook.db.scene;

import i18n.I18N;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import storybook.App;
import storybook.db.abs.AbsEntitys;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import static storybook.db.book.Book.TYPE.*;
import storybook.db.chapter.Chapter;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.part.Part;
import storybook.db.person.Person;
import storybook.db.plot.Plot;
import storybook.db.relation.Relation;
import storybook.db.strand.Strand;
import storybook.db.tag.Tag;
import storybook.dialog.ExceptionDlg;
import storybook.project.Project;
import storybook.tools.DateUtil;
import storybook.tools.LOG;
import storybook.tools.SbDuration;
import storybook.tools.file.XEditorFile;
import storybook.tools.html.Html;
import storybook.ui.MainFrame;

/**
 * Scene utilities
 *
 * @author favdb
 */
public class Scenes extends AbsEntitys {

	private static final String TT = "Scenes.";
	private final List<Scene> scenes = new ArrayList<>();
	private boolean changeDateRel;

	@SuppressWarnings("unchecked")
	public Scenes(Project project) {
		super(project);
	}

	/**
	 * get the last Scene Id
	 *
	 * @return
	 */
	@Override
	public Long getLast() {
		Long n = 0L;
		for (Scene p : scenes) {
			n = Math.max(n, p.getId());
		}
		return n;
	}

	/**
	 * save the given entity as a Scene
	 *
	 * @param entity
	 */
	@Override
	public void save(AbstractEntity entity) {
		if (entity instanceof Scene) {
			if (entity.getId() < 0) {
				entity.setId(getLast() + 1);
				scenes.add((Scene) entity);
			} else {
				try {
					scenes.set(getIdx(entity.getId()), (Scene) entity);
				} catch (Exception ex) {
					LOG.err(TT + ".save(entity) error", ex);
					return;
				}
			}
		}
		// initialize all date relative if changed
		if (changeDateRel) {
			changeDateRel = false;
			relativeDateInit();
		}
	}

	@Override
	public int getIdx(Long id) {
		for (Scene p : scenes) {
			if (p.getId().equals(id)) {
				return scenes.indexOf(p);
			}
		}
		return -1;
	}

	@Override
	public Scene get(Long id) {
		for (Scene p : scenes) {
			if (p.getId().equals(id)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * add a scene to the list of scenes
	 *
	 * @param p
	 */
	@Override
	public void add(AbstractEntity p) {
		if (p.getId() == -1L) {
			p.setId(getLast() + 1L);
		}
		scenes.add((Scene) p);
		relativeDateInit();
	}

	/**
	 * update a scene
	 *
	 * @param scene
	 */
	public void update(Scene scene) {
		for (int i = 0; i < scenes.size(); i++) {
			Scene sx = scenes.get(i);
			if (sx.getId().equals(scene.getId())) {
				scenes.set(i, scene);
				break;
			}
		}
	}

	/**
	 * delete a scene
	 *
	 * @param p
	 */
	@Override
	public void delete(AbstractEntity p) {
		int n = getIdx(p.getId());
		if (n != -1) {
			scenes.remove((Scene) p);
		}
	}

	/**
	 * get the first scene of the list of scenes
	 *
	 * @return
	 */
	@Override
	public AbstractEntity getFirst() {
		if (scenes.isEmpty()) {
			return null;
		}
		return scenes.get(0);
	}

	/**
	 * get the list of scenes
	 *
	 * @return
	 */
	@Override
	public List getList() {
		return scenes;
	}

	/**
	 * get count of scenes
	 *
	 * @return
	 */
	@Override
	public int getCount() {
		return scenes.size();
	}

	/**
	 * find a list of scenes sorted by name
	 *
	 * @return
	 */
	public List findByName() {
		List<Scene> ls = new ArrayList<>();
		for (Scene p : scenes) {
			ls.add(p);
		}
		if (ls.size() > 1) {
			Collections.sort(ls, (Scene r1, Scene r2)
					-> r1.getName().compareTo(r2.getName()));
		}
		return ls;
	}

	/**
	 * find a scene which name is the given one
	 *
	 * @param name
	 * @return
	 */
	public Scene findName(String name) {
		for (Scene p : scenes) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * find list of distinct dates of scenes
	 *
	 * @param mainFrame
	 * @param strand
	 * @return
	 */
	public List<Date> findDistinctDatesByStrand(MainFrame mainFrame, Strand strand) {
		List<Date> list = new ArrayList<>();
		for (Scene p : scenes) {
			if (p.hasDate() && !list.contains(p.getDate())) {
				list.add(p.getDate());
			}
		}
		return list;
	}

	/**
	 * get the list of scenes sorted by status
	 *
	 * @param status
	 * @return
	 */
	public List<Scene> findByStatus(int status) {
		List<Scene> ls = new ArrayList<>();
		for (Scene p : scenes) {
			if (p.getStatus() == status) {
				ls.add(p);
			}
		}
		if (ls.size() > 1) {
			Collections.sort(ls, (Scene r1, Scene r2)
					-> r1.getSceneno().compareTo(r2.getSceneno()));
		}
		return ls;
	}

	/**
	 * get list of Scene for the given Chapter
	 *
	 * @param chapter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Scene> find(Chapter chapter) {
		if (chapter == null) {
			return findUnassigned();
		}
		List<Scene> ls = new ArrayList<>();
		for (Scene p : scenes) {
			if (p.hasChapter() && p.getChapter().equals(chapter)) {
				ls.add(p);
			}
		}
		if (ls.size() > 1) {
			Collections.sort(ls, (Scene r1, Scene r2)
					-> r1.getCCSS().compareTo(r2.getCCSS()));
		}
		return ls;
	}

	/**
	 * find the list of unassigned scene
	 *
	 * @return
	 */
	public List<Scene> findUnassigned() {
		List<Scene> ls = new ArrayList<>();
		for (Scene s : scenes) {
			if (!s.hasChapter()) {
				ls.add(s);
			}
		}
		if (ls.size() > 1) {
			Collections.sort(ls, (Scene r1, Scene r2)
					-> r1.getSceneno().compareTo(r2.getSceneno()));
		}
		return ls;
	}

	/**
	 * find scenes which have the given narrator
	 *
	 * @return
	 */
	public List<Person> findNarrators() {
		List<Person> list = new ArrayList<>();
		for (Scene p : scenes) {
			if (p.getNarrator() != null && !list.contains(p.getNarrator())) {
				list.add(p.getNarrator());
			}
		}
		return list;
	}

	/**
	 * create a Scene and initialize the Strand and the Chapter
	 *
	 * @param id
	 * @param strand
	 * @param chapter
	 * @return
	 */
	public static Scene create(Long id, Strand strand, Chapter chapter) {
		Scene scene = new Scene();
		scene.setId(id);
		scene.setStrand(strand);
		scene.setStatus(1);
		scene.setChapter(chapter);
		scene.setSceneno(1);
		scene.setDate(null);
		scene.setTitle(I18N.getMsg("scene") + " 1");
		scene.setSummary("<p></p>");
		scene.setNotes("");
		return scene;
	}

	/**
	 * change the status of a given scene with the new value
	 *
	 * @param mainFrame
	 * @param scene
	 * @param status
	 */
	public void changeStatus(MainFrame mainFrame, Scene scene, int status) {
		scene.setStatus(status);
		mainFrame.getBookController().updateEntity(scene);
	}

	/**
	 * get list of Scenes which have a fixed date, if no fixed date but relative scene then set a
	 * computed date
	 *
	 * @return
	 */
	public List<Scene> getWithDates() {
		//LOG.trace(TT+".getWithDates()");
		List<Scene> list = new ArrayList<>();
		for (Scene p : scenes) {
			if (p.getDateRel() != null) {
				list.add(p);
			}
		}
		return list;
	}

	/**
	 * find all Scenes unsorted
	 *
	 * @param mainFrame
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Scene> find(MainFrame mainFrame) {
		List<Scene> scenes = new ArrayList<>();
		if (mainFrame != null) {
			scenes = mainFrame.project.scenes.getList();
		}
		return scenes;
	}

	/**
	 * find Scenes linked to the given Entity, sorted result by CC.SS
	 *
	 * @param entity
	 * @return
	 */
	public List<Scene> findBy(AbstractEntity entity) {
		List<Scene> list = new ArrayList<>();
		for (Scene p : scenes) {
			switch (Book.getTYPE(entity)) {
				case CHAPTER:
					if (p.hasChapter() && p.getChapter().equals((Chapter) entity)) {
						list.add(p);
					}
					break;
				case SCENE:
					if (p.hasRelativescene() && p.getRelativesceneid().equals(entity.getId())) {
						list.add(p);
					}
					break;
				case ITEM:
					if (p.getItems() != null && p.getItems().contains((Item) entity)) {
						list.add(p);
					}
					break;
				case LOCATION:
					if (p.getLocations() != null && p.getLocations().contains((Location) entity)) {
						list.add(p);
					}
					break;
				case PERSON:
					if (p.getPersons() != null && p.getPersons().contains((Person) entity)) {
						list.add(p);
					}
					break;
				case PLOT:
					if (p.getPlots() != null && p.getPlots().contains((Plot) entity)) {
						list.add(p);
					}
					break;
				case RELATION:
					Relation relation = (Relation) entity;
					if (relation.hasStartScene()) {
						list.add(relation.getStartScene());
					}
					if (relation.hasEndScene()) {
						list.add(relation.getEndScene());
					}
					break;
				case STRAND:
					if (p.getStrand() != null && p.getStrand().equals((Strand) entity)) {
						list.add(p);
					}
					if (p.getStrands() != null && p.getStrands().contains((Strand) entity)) {
						list.add(p);
					}
					break;
				case TAG:
					if (p.getTags() != null && p.getTags().contains((Tag) entity)) {
						list.add(p);
					}
					break;
				default:
					break;
			}
		}
		if (!list.isEmpty()) {
			Collections.sort(list, (Scene r1, Scene r2)
					-> r1.getCCSS().compareTo(r2.getCCSS()));
		}
		return list;
	}

	/**
	 * find Scenes list beetwen starting Scene and ending Scene
	 *
	 * @param start
	 * @param end
	 * @return
	 */
	public List<Scene> find(Scene start, Scene end) {
		List<Scene> ret = new ArrayList<>();
		if (start == null || end == null) {
			return ret;
		}
		int ibegin = scenes.indexOf(start), iend = scenes.indexOf(end);
		if (ibegin <= iend) {
			return scenes.subList(scenes.indexOf(start), scenes.indexOf(end));
		} else {
			return ret;
		}
	}

	/**
	 * find all distinct dates of the given list of scenes
	 *
	 * @param rs
	 * @param periode:Y=year, M=month, D=day, H=hour
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Date> findDistinctDates(List<Scene> rs, char periode) {
		//LOG.trace(TT + ".findDistinctDates(scenes=" + rs.size() + ", periode=" + periode + ")");
		List<Date> dates = new ArrayList<>();
		for (Scene scene : rs) {
			Date ds = scene.getDateRel();
			if (ds != null) {
				if (!dates.contains(ds)) {
					dates.add(ds);
				}
			}
		}
		Collections.sort(dates, (d1, d2) -> d1.compareTo(d2));
		return dates;
	}

	/**
	 * find all distinct dates from list of scenes sorted by date
	 *
	 * @param periode:Y=year, M=month, D=day, H=hour
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Date> findDistinctDates(char periode) {
		//LOG.trace(TT + ".findDistinctDates(periode=" + periode + ")");
		List<Date> dates = new ArrayList<>();
		for (Scene scene : scenes) {
			Date ds = scene.getDateRel();
			//ds for given periode
			switch (periode) {
				case 'Y':
					ds = DateUtil.forYear(ds);
					break;
				case 'M':
					ds = DateUtil.forMonth(ds);
					break;
				case 'D':
					ds = DateUtil.forDay(ds);
					break;
				case 'H':
					ds = DateUtil.forHour(ds);
					break;
				default:
					ds = DateUtil.forMinute(ds);
					break;
			}
			if (ds != null) {
				if (!dates.contains(ds)) {
					dates.add(ds);
				}
			}
		}
		Collections.sort(dates, (d1, d2) -> d1.compareTo(d2));
		return dates;
	}

	/**
	 * find distinct dates for the given Part
	 *
	 * @param part
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Date> findDistinctDates(Part part) {
		//LOG.trace(TT + "findDistinctDates(part=" + LOG.trace(part) + ")");
		if (part != null) {
			return findDistinctDates(findByPart(part), 'H');
		}
		return findDistinctDates(scenes, 'H');
	}

	/**
	 * get the first date of Scenes
	 *
	 * @return
	 */
	public Date findDateFirst() {
		Date date = null;
		for (Scene p : scenes) {
			if (date == null || p.getDate().before(date)) {
				date = p.getDate();
			}
		}
		return date;
	}

	/**
	 * get last date of Scenes
	 *
	 * @return
	 */
	public Date findDateLast() {
		Date date = null;
		for (Scene p : scenes) {
			if (date == null || p.getDate().after(date)) {
				date = p.getDate();
			}
		}
		return date;
	}

	/**
	 * renumber all scenes by Chapter
	 *
	 * @param mainFrame
	 */
	public void renumber(MainFrame mainFrame) {
		//LOG.trace(TT + ".renumber(mainFrame)");
		mainFrame.cursorSetWaiting();
		int start = 1;
		mainFrame.project.chapters.sortByNumber();
		@SuppressWarnings("unchecked")
		List<Chapter> chapters = (List<Chapter>) mainFrame.project.chapters.getList();
		//renumber for each chapter
		for (Chapter chapter : chapters) {
			if (App.preferences.sceneIsRenumAuto()) {
				renumberAuto(chapter);
				continue;
			} else if (App.preferences.sceneIsRenumByChapter()) {
				start = 1;
			}
			start = renumberInc(chapter, start, App.preferences.sceneGetRenumInc());
		}
		//renumber for non assigned Scene
		if (App.preferences.sceneIsRenumAuto()) {
			renumberAuto(null);
		} else {
			renumberInc(null, start, App.preferences.sceneGetRenumInc());
		}
		mainFrame.getBookModel().fireAgainScenes();
		mainFrame.setUpdated();
		mainFrame.cursorSetDefault();
	}

	/**
	 * renumber the scenes of the given chapter
	 *
	 * @param chapter
	 * @return the last number used
	 */
	public int renumber(Chapter chapter) {
		//LOG.trace(TT + "renumber(mainFrame, chapter=" + LOG.trace(chapter) + ")");
		int nb = 0;
		try {
			if (App.preferences.sceneIsRenumAuto()) {
				nb = renumberAuto(chapter);
			} else {
				nb = renumberInc(chapter, 1, App.preferences.sceneGetRenumInc());
			}
		} catch (Exception e) {
			ExceptionDlg.show(TT
					+ "renumber(mainFrame, chapter=" + LOG.trace(chapter) + ") Exception", e);
		}
		return nb;
	}

	/**
	 * automatic renumber scenes of the chapter
	 *
	 * @param chapter
	 * @return the number of renumber scenes
	 */
	public int renumberAuto(Chapter chapter) {
		//LOG.trace(TT + ".renumberAuto(mainFrame, chapter=" + LOG.trace(chapter) + ")");
		int partNumber = 0;
		int chapterNumber = 0;
		if (chapter != null) {
			partNumber = (chapter.hasPart() ? chapter.getPart().getNumber() * 100000 : 0);
			chapterNumber = chapter.getChapterno() * 1000;
		}
		if (chapterNumber > 99000) {
			chapterNumber = 99000;
		}
		int sceneNumber = 1;
		int newNum;
		int inc = 1, nb = 0;
		for (Scene scene : scenes) {
			newNum = partNumber + chapterNumber + (sceneNumber * 10);
			scene.setSceneno(newNum);
			sceneNumber += inc;
			nb++;
		}
		return nb;
	}

	/**
	 * incremental renumber scenes of the given Chapter
	 *
	 * @param chapter
	 * @param start: starting number
	 * @param inc: increment
	 *
	 * @return last number used
	 */
	public int renumberInc(Chapter chapter, int start, int inc) {
		//LOG.trace(TT + ".renumberInc(mainFrame, chapter=" + LOG.trace(chapter)
		//+ ", start=" + start + ", inc=" + inc + ")");
		int sceneNumber = start;
		for (Scene scene : find(chapter)) {
			scene.setSceneno(sceneNumber);
			sceneNumber += inc;
		}
		return sceneNumber;
	}

	/**
	 * get the next number for a scene in the given chapter
	 *
	 * @param mainFrame
	 * @param chapter
	 * @return
	 */
	public int getNextNumber(MainFrame mainFrame, Chapter chapter) {
		//LOG.trace(TT + ".getNextNumber(mainFrame, chapter=" + LOG.trace(chapter) + ")");
		int number = findBy(chapter).size();
		if (App.preferences.sceneIsRenumAuto()) {
			if (chapter != null) {
				@SuppressWarnings("null")
				String snum = String.format("%02d%02d%02d0",
						(chapter.hasPart() ? chapter.getPart().getNumber() : 0),
						chapter.getChapterno(),
						number + 1);
				return Integer.parseInt(snum);
			}
			return (number + 1) * 10;
		} else {
			if (!App.preferences.sceneIsRenumByChapter()) {
				number = find(mainFrame).size();
			}
			return number + App.preferences.sceneGetRenumInc();
		}
	}

	/**
	 * launch external editor
	 *
	 * @param mainFrame
	 * @param scene
	 */
	public static void launchExternal(MainFrame mainFrame, Scene scene) {
		String name = XEditorFile.getFilePath(mainFrame, scene);
		if (name == null || name.isEmpty()) {
			name = XEditorFile.getDefaultFilePath(mainFrame, scene.getName());
		}
		if (name.isEmpty()) {
			return;
		}
		File file = new File(name);
		if (!file.exists()) {
			file = XEditorFile.createFile(mainFrame, name);
			if (file == null) {
				return;
			}
			scene.setOdf(name);
		}
		XEditorFile.launchExternal(file.getAbsolutePath());
	}

	/**
	 * get the last used Scene number
	 *
	 * @return
	 */
	public int getLastNumber() {
		@SuppressWarnings("unchecked")
		int n = 0;
		for (Scene scene : scenes) {
			if ((scene.getSceneno() != null) && (scene.getSceneno() > n)) {
				n = scene.getSceneno();
			}
		}
		return n;
	}

	/**
	 * get the tools tip for the given Scene, contains chapter name and shorted summary
	 *
	 * @param buf
	 * @param scene
	 */
	public static void tooltip(StringBuilder buf, Scene scene) {
		buf.append(Html.P_B).append(Html.intoB(I18N.getColonMsg("chapter") + " "));
		buf.append((scene.getChapter() == null ? "" : scene.getChapter().getName()));
		buf.append(Html.P_E);
		if (scene.getSummary() != null && !scene.getSummary().isEmpty()) {
			buf.append(scene.getSummary(80));
		}
	}

	/**
	 * find first Scene of the given Chapter
	 *
	 * @param chapter
	 * @return
	 */
	public Scene findFirst(Chapter chapter) {
		List<Scene> ls = find(chapter);
		if (!ls.isEmpty()) {
			Collections.sort(ls, (Scene r1, Scene r2)
					-> r1.getCCSS().compareTo(r2.getCCSS()));
			return (ls.get(0));
		}
		return null;
	}

	/**
	 * find distinct dates for the given Strand
	 *
	 * @param strand
	 * @return
	 */
	public List<Date> findDistinctDates(Strand strand) {
		List<Date> ls = new ArrayList<>();
		for (Scene p : scenes) {
			if ((p.getStrand().equals(strand) || p.getStrands().contains(strand))
					&& (p.hasDate() && !ls.contains(p.getDate()))) {
				ls.add(p.getDate());

			}
		}
		return ls;
	}

	/**
	 * find the scenes related to the given Location
	 *
	 * @param entity
	 * @return
	 */
	public List<Scene> findLocation(Location entity) {
		List<Scene> ls = new ArrayList<>();
		for (Scene p : scenes) {
			if (p.getLocations().contains(entity)) {
				ls.add(p);
			}
		}
		return ls;
	}

	/**
	 * find the scenes related to the given Person, without narrator
	 *
	 * @param entity
	 * @return
	 */
	public List<Scene> findPerson(Person entity) {
		List<Scene> ls = new ArrayList<>();
		for (Scene p : scenes) {
			if (p.getPersons().contains(entity)) {
				ls.add(p);
			}
		}
		return ls;
	}

	/**
	 * find the scenes related to the given Item
	 *
	 * @param entity
	 * @return
	 */
	public List<Scene> findItem(Item entity) {
		List<Scene> ls = new ArrayList<>();
		for (Scene p : scenes) {
			if (p.getItems().contains(entity)) {
				ls.add(p);
			}
		}
		return ls;
	}

	/**
	 * find scenes related to the given plot
	 *
	 * @param entity
	 * @return
	 */
	public List<Scene> findPlot(Plot entity) {
		List<Scene> ls = new ArrayList<>();
		for (Scene p : scenes) {
			if (p.getPlots().contains(entity)) {
				ls.add(p);
			}
		}
		return ls;
	}

	/**
	 * find scenes related to the given strand, included secondary strands
	 *
	 * @param entity
	 * @return
	 */
	public List<Scene> findStrand(Strand entity) {
		List<Scene> ls = new ArrayList<>();
		for (Scene p : scenes) {
			if (p.getStrand().equals(entity) || p.getStrands().contains(entity)) {
				ls.add(p);
			}
		}
		return ls;
	}

	/**
	 * find scenes related to the given main strand
	 *
	 * @param strand
	 * @return
	 */
	public List<Scene> find(Strand strand) {
		List<Scene> ls = new ArrayList<>();
		for (Scene p : scenes) {
			if (p.getStrand().equals(strand)) {
				ls.add(p);
			}
		}
		return ls;
	}

	/**
	 * count scenes linked to the given entity
	 *
	 * @param entity
	 * @return
	 */
	public int countBy(AbstractEntity entity) {
		int n = 0;
		for (Scene p : scenes) {
			switch (entity.getObjType()) {
				case ITEM:
					if (p.getItems().contains((Item) entity)) {
						n++;
					}
					break;
				case LOCATION:
					if (p.getLocations().contains((Location) entity)) {
						n++;
					}
					break;
				case PERSON:
					if (p.getPersons().contains((Person) entity)) {
						n++;
					}
					break;
				case PLOT:
					if (p.getPlots().contains((Plot) entity)) {
						n++;
					}
					break;
			}
		}
		return n;
	}

	/**
	 * find scenes of the given Part
	 *
	 * @param part
	 * @return
	 */
	public List<Scene> findByPart(Part part) {
		List<Scene> ls = new ArrayList<>();
		for (Scene p : scenes) {
			if (p.hasChapter() && p.getChapter().getPart().equals(part)) {
				ls.add(p);
			}
		}
		return ls;
	}

	/**
	 * set real links for linked objects
	 */
	@Override
	public void setLinks() {
		for (Scene p : scenes) {
			if (p.getChapterId() != -1L) {
				p.setChapter(project.chapters.get(p.getChapterId()));
			}
			if (p.getStrandId() != -1L) {
				p.setStrand(project.strands.get(p.getStrandId()));
			}
			if (!p.getPersonsId().isEmpty()) {
				for (Long v : p.getPersonsId()) {
					p.getPersons().add(project.persons.get(v));
				}
			}
			if (!p.getItemsId().isEmpty()) {
				for (Long v : p.getItemsId()) {
					p.getItems().add((Item) project.items.get(v));
				}
			}
			if (!p.getLocationsId().isEmpty()) {
				for (Long v : p.getLocationsId()) {
					p.getLocations().add(project.locations.get(v));
				}
			}
			if (!p.getPlotsId().isEmpty()) {
				for (Long v : p.getPlotsId()) {
					p.getPlots().add(project.plots.get(v));
				}
			}
			if (!p.getTagsId().isEmpty()) {
				for (Long v : p.getTagsId()) {
					p.getTags().add((Tag) project.tags.get(v));
				}
			}
			if (!p.getStrandsId().isEmpty()) {
				for (Long v : p.getStrandsId()) {
					p.getStrands().add(project.strands.get(v));
				}
			}
			if (p.getNarratorId() != -1L) {
				p.setNarrator((Person) project.persons.get(p.getNarratorId()));
			}
		}
	}

	/**
	 * get a XML String representing all scenes in XML format
	 *
	 * @return
	 */
	@Override
	public String toXml() {
		StringBuilder b = new StringBuilder();
		for (Scene p : scenes) {
			b.append(p.toXml());
		}
		return b.toString();
	}

	/**
	 * get scenes between two dates
	 *
	 * @param tsStart
	 * @param tsEnd
	 * @return
	 */
	public List<Scene> getBetween(Timestamp tsStart, Timestamp tsEnd) {
		List<Scene> ls = new ArrayList<>();
		for (Scene s : scenes) {
			if (s.getScenets() != null && DateUtil.between(s.getScenets(), tsStart, tsEnd)) {
				ls.add(s);
			}
		}
		return ls;
	}

	/**
	 * set the relative date for all scenes
	 */
	public void relativeDateInit() {
		//LOG.trace(TT + "relativeDateInit()");
		//set all fixed dates
		for (Scene scene : scenes) {
			if (scene.hasScenets()) {
				scene.setDateRel(new Date(scene.getScenets().getTime()));
			} else {
				scene.setDateRel(null);
			}
		}
		//set for relative scene
		for (Scene scene : scenes) {
			if (scene.hasRelativescene()) {
				Scene rs = get(scene.getRelativesceneid());
				if (rs != null && rs.getDateRel() != null) {
					SbDuration rel;
					if (rs.hasDuration()) {
						rel = new SbDuration(rs.getDuration());
					} else {
						rel = rs.getSbDuration();
					}
					if (!scene.getRelativetime().equals("00-00-00_00:00:00")) {
						rel = new SbDuration(scene.getRelativetime());
					}
					scene.setDateRel(DateUtil.add(rs.getDateRel(), rel));
				}
			}
		}
	}

	/**
	 * set the relative date for the given scene
	 *
	 * @param scene
	 */
	public void relativeDateSet(Scene scene) {
		Date date = null;//if no date the date is null
		if (scene.hasScenets()) {
			date = new Date(scene.getScenets().getTime());
		} else if (scene.hasRelativescene()) {
			date = relativeDateGet(scene);
		}
		boolean b = Objects.equals(scene.getDateRel(), date);
		scene.setDateRel(date);
		if (!b) {
			for (Scene s : scenes) {
				if (s.hasRelativescene() && s.getRelativesceneid().equals(s.getId())) {
					s.setDateRel(relativeDateGet(s));
				}
			}
		}
	}

	/**
	 * compute the relative date for a scene with relative scene Id
	 *
	 * @param scene
	 * @return
	 */
	private Date relativeDateGet(Scene scene) {
		Date date = null;
		Scene rs = get(scene.getRelativesceneid());
		if (rs.getDateRel() != null) {
			SbDuration rel;
			if (rs.hasDuration()) {
				rel = new SbDuration(rs.getDuration());
			} else {
				rel = rs.getSbDuration();
			}
			if (!scene.getRelativetime().equals("00-00-00_00:00:00")) {
				rel = new SbDuration(scene.getRelativetime());
			}
			date = DateUtil.add(rs.getDateRel(), rel);
		}
		return date;
	}

	/**
	 * compute the relative date for the given relative scene with somme after delay
	 *
	 * @param rel
	 * @param after
	 * @return
	 */
	public Date relativeDateCompute(Scene rel, String after) {
		return relativeDateCompute(rel, after, new HashSet<>());
	}

	/**
	 * compute the relative date for the given relative scene, with some after delay and check for
	 * visited
	 *
	 * @param rel
	 * @param after
	 * @param visited
	 * @return
	 */
	private Date relativeDateCompute(Scene rel, String after, Set<Long> visited) {
		// protect against circular reference
		Long sceneId = rel.getRelativesceneid();
		if (visited.contains(sceneId)) {
			LOG.err("Circular reference detected for scene: " + sceneId);
			return null;
		}
		Scene sx = (Scene) get(sceneId);
		if (sx == null) {
			//LOG.err("Scene not found: " + sceneId);
			return null;
		}
		visited.add(sceneId);
		Date resultDate = null;
		try {
			// if the referenced scene has a fixed date (scenets)
			if (sx.getDateRel() != null) {
				resultDate = DateUtil.add(sx.getDateRel(), (after.isEmpty() ? sx.getDuration() : after));
			} else if (sx.hasRelativescene()) {
				// compute recursively the begining date of the refercned scene
				Date baseDate = relativeDateCompute(sx, sx.getRelativetime(), visited);
				if (baseDate != null) {
					resultDate = DateUtil.add(baseDate, after);
				}
			}
			// if this is a base date and if the duration after is not empty
			if (resultDate != null && after != null && !SbDuration.isZero(after)) {
				resultDate = DateUtil.add(resultDate, after);
			}
		} finally {
			visited.remove(sceneId);
		}
		return resultDate;
	}

	/**
	 * change all HTML links to the given path
	 *
	 * @param path
	 */
	@Override
	public void changeHtmlLinks(String path) {
		for (Scene p : scenes) {
			p.changeHtmlLinks(path);
		}
	}

	/**
	 * sort scenes by scene number
	 */
	public void sortByNumber() {
		Collections.sort(scenes, (Scene r1, Scene r2) -> r1.getSceneno().compareTo(r2.getSceneno()));
	}

	/**
	 * changing relative date indicator
	 *
	 * @param b
	 */
	public void changeDateRelative(boolean b) {
		changeDateRel = b;
	}

}
