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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.person.Person;
import storybook.db.plot.Plot;
import storybook.exim.importer.ImportDocument;
import storybook.tools.html.Html;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.SbView;

/**
 *
 * @author favdb
 */
public class ScenesLinks {

	private static final String TT = "ScenesLinks.", LINKS = "links.";

	public static boolean show(MainFrame mainFrame, Book.TYPE type) {
		if (mainFrame.project.getList(type).isEmpty()) {
			JOptionPane.showMessageDialog(mainFrame,
					I18N.getMsg(LINKS + type.toString().toLowerCase() + ".empty"),
					I18N.getMsg(LINKS + type.toString().toLowerCase()),
					JOptionPane.YES_OPTION);
			return false;
		}
		ScenesLinks dlg = new ScenesLinks(mainFrame, type);
		if (!dlg.init()) {
			return false;
		}
		return dlg.exec();
	}

	private final MainFrame mainFrame;
	private boolean xternal;
	private final Book.TYPE type;

	public ScenesLinks(MainFrame mainFrame, Book.TYPE type) {
		this.mainFrame = mainFrame;
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	private boolean init() {
		//LOG.trace(TT + ".init()");
		List list = (List) mainFrame.project.getList(Book.TYPE.PERSON);
		if (list.isEmpty()) {
			JOptionPane.showMessageDialog(mainFrame,
					I18N.getMsg(LINKS + type.toString().toLowerCase() + ".empty"),
					I18N.getMsg(LINKS + type.toString().toLowerCase()),
					JOptionPane.YES_OPTION);
			return false;
		}
		return JOptionPane.showConfirmDialog(mainFrame,
				I18N.getMsg(LINKS + type.toString().toLowerCase() + ".info"),
				I18N.getMsg(LINKS + type.toString().toLowerCase()),
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_NO_OPTION;
	}

	@SuppressWarnings("unchecked")
	private boolean exec() {
		//LOG.trace(TT + ".exec()");
		xternal = mainFrame.getBook().isXeditorUse();
		List<Scene> scenes = (List) mainFrame.project.getList(Book.TYPE.SCENE);
		List entities;
		switch (type) {
			case PERSON:
				entities = (List) mainFrame.project.getList(Book.TYPE.PERSON);
				break;
			case LOCATION:
				entities = (List) mainFrame.project.getList(Book.TYPE.LOCATION);
				break;
			case ITEM:
				entities = (List) mainFrame.project.getList(Book.TYPE.ITEM);
				break;
			default:
				return false;
		}
		for (Scene scene : scenes) {
			update(scene, entities);
		}
		mainFrame.getBookModel().fireAgain(mainFrame.getView(SbView.VIEWNAME.SCENES));
		mainFrame.getBookModel().fireAgain(mainFrame.getView(SbView.VIEWNAME.MEMORIA));
		mainFrame.getBookModel().fireAgain(mainFrame.getView(SbView.VIEWNAME.INFO));
		mainFrame.getBookModel().fireAgain(mainFrame.getView(SbView.VIEWNAME.READING));
		mainFrame.getBookModel().fireAgain(mainFrame.getView(SbView.VIEWNAME.MANAGE));
		mainFrame.getBookModel().fireAgain(mainFrame.getView(SbView.VIEWNAME.BOOK));
		return true;
	}

	/**
	 * update the links
	 *
	 * @param scene
	 * @param entities
	 */
	private void update(Scene scene, List<AbstractEntity> entities) {
		//LOG.trace(TT + ".update(scene="+", entites nb="+entities.size()+")");
		switch (type) {
			case ITEM:
				updateItems(scene, entities);
				break;
			case LOCATION:
				updateLocations(scene, entities);
				break;
			case PERSON:
				updatePersons(scene, entities);
				break;
			case PLOT:
				updatePlots(scene, entities);
				break;
			default:
				break;
		}
	}

	/**
	 * update the items
	 *
	 * @param scene
	 * @param entities
	 */
	private void updateItems(Scene scene, List<AbstractEntity> entities) {
		//LOG.trace(TT + ".updateItems(scene, entities)");
		String text = Html.htmlToText(scene.getSummary());
		if (xternal && !scene.getOdf().isEmpty()) {
			ImportDocument doc = new ImportDocument(mainFrame, new File(scene.getOdf()));
			if (doc.openDocument()) {
				text = doc.getContentAsTxt();
				doc.close();
			}
		}
		List<Item> lp = new ArrayList<>();
		for (Object p : entities) {
			if (text.contains(((Item) p).getName())) {
				lp.add((Item) p);
			}
		}
		scene.setItems(lp);
		mainFrame.project.scenes.save(scene);
	}

	/**
	 * update the locations
	 *
	 * @param scene
	 * @param entities
	 */
	private void updateLocations(Scene scene, List<AbstractEntity> entities) {
		//LOG.trace(TT + ".updateLocations(scene, entities)");
		String text = Html.htmlToText(scene.getSummary());
		if (xternal && !scene.getOdf().isEmpty()) {
			ImportDocument doc = new ImportDocument(mainFrame, new File(scene.getOdf()));
			if (doc.openDocument()) {
				text = doc.getContentAsTxt();
				doc.close();
			}
		}
		List<Location> lp = new ArrayList<>();
		if (entities != null) {
			for (Object p : entities) {
				if (text.contains(((Location) p).getName()) && !lp.contains(p)) {
					lp.add((Location) p);
				}
			}
		}
		scene.setLocations(lp);
		mainFrame.project.scenes.save(scene);
	}

	/**
	 * update the persons
	 *
	 * @param scene
	 * @param entities
	 */
	private void updatePersons(Scene scene, List<AbstractEntity> entities) {
		//LOG.trace(TT + ".updatePersons(scene, entities)");
		String text = Html.htmlToText(scene.getSummary());
		if (xternal && !scene.getOdf().isEmpty()) {
			ImportDocument doc = new ImportDocument(mainFrame, new File(scene.getOdf()));
			if (doc.openDocument()) {
				text = doc.getContentAsTxt();
				doc.close();
			}
		}
		text = text.replaceAll("\\p{Punct}", " ").replace("  ", " ");
		List<String> tl = new ArrayList<>(Arrays.asList(text.split(" ")));
		List<Person> lp = new ArrayList<>();
		for (Object p : entities) {
			String abbr = ((Person) p).getAbbr();
			if (tl.contains(abbr) || tl.contains("[" + abbr + "]")) {
				lp.add((Person) p);
			} else {
				String firstlast = ((Person) p).getFirstname() + " " + ((Person) p).getLastname();
				String lastfirst = ((Person) p).getLastname() + " " + ((Person) p).getFirstname();
				if (text.contains(firstlast) || text.contains(lastfirst)) {
					lp.add((Person) p);
				}
			}
		}
		scene.setPersons(lp);
		mainFrame.project.scenes.save(scene);
	}

	/**
	 * update the plots
	 *
	 * @param scene
	 * @param entities
	 */
	private void updatePlots(Scene scene, List<AbstractEntity> entities) {
		//LOG.trace(TT + ".updatePlots(scene, entities)");
		String text = Html.htmlToText(scene.getSummary());
		if (xternal && !scene.getOdf().isEmpty()) {
			ImportDocument doc = new ImportDocument(mainFrame, new File(scene.getOdf()));
			if (doc.openDocument()) {
				text = doc.getContentAsTxt();
				doc.close();
			}
		}
		List<Plot> lp = new ArrayList<>();
		for (Object p : entities) {
			if (text.contains(((Plot) p).getName())) {
				lp.add((Plot) p);
			}
		}
		scene.setPlots(lp);
		mainFrame.project.scenes.save(scene);
	}

}
