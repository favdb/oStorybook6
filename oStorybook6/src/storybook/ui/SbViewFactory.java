
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
package storybook.ui;

import api.infonode.docking.View;
import api.infonode.docking.util.StringViewMap;
import i18n.I18N;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JTable;
import resources.icons.IconUtil;
import storybook.db.abs.AbsTable;
import storybook.db.abs.AbstractEntity;
import storybook.db.attribute.AttributeTable;
import storybook.db.attribute.AttributesViewPanel;
import storybook.db.category.CategoryTable;
import storybook.db.chapter.ChapterTable;
import storybook.db.endnote.EndnoteTable;
import storybook.db.episode.panel.EpisodePanel;
import storybook.db.event.EventTable;
import storybook.db.gender.GenderTable;
import storybook.db.idea.IdeaTable;
import storybook.db.item.ItemTable;
import storybook.db.location.LocationTable;
import storybook.db.part.PartTable;
import storybook.db.person.PersonTable;
import storybook.db.plot.PlotTable;
import storybook.db.relation.RelationTable;
import storybook.db.scene.SceneTable;
import storybook.db.strand.StrandTable;
import storybook.db.tag.TagTable;
import storybook.tools.LOG;
import storybook.ui.SbView.VIEWNAME;
import storybook.ui.chart.by.PersonsByDate;
import storybook.ui.chart.by.PersonsByScene;
import storybook.ui.chart.by.StrandsByDate;
import storybook.ui.chart.occurences.OccurrenceOfItems;
import storybook.ui.chart.occurences.OccurrenceOfLocations;
import storybook.ui.chart.occurences.OccurrenceOfPersons;
import storybook.ui.chart.wiww.WiWW;
import storybook.ui.panel.AbstractPanel;
import storybook.ui.panel.book.BookPanel;
import storybook.ui.panel.chrono.Chrono;
import storybook.ui.panel.info.InfoPanel;
import storybook.ui.panel.manage.Manage;
import storybook.ui.panel.memo.MemosPanel;
import storybook.ui.panel.memoria.MemoriaPanel;
import storybook.ui.panel.planning.Planning;
import storybook.ui.panel.reading.ReadingPanel;
import storybook.ui.panel.storyboard.Storyboard;
import storybook.ui.panel.storymap.Storymap;
import storybook.ui.panel.timeline.TimelinePanel;
import storybook.ui.panel.tree.TreePanel;
import storybook.ui.panel.typist.TypistPanel;
import storybook.ui.panel.typist.TypistScenario;

/**
 * @author martin
 *
 */
public class SbViewFactory {

	private static String TT = "SbViewFactory.";

	private final int NONE = 0, EXPORT = 1, OPTIONS = 10;
	private final MainFrame mainFrame;
	private final StringViewMap viewMap;
	private boolean initialisation;

	public SbViewFactory(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		viewMap = new StringViewMap();
	}

	public void initBegin() {
		initialisation = true;
	}

	public void initEnd() {
		initialisation = false;
	}

	@SuppressWarnings("null")
	public void setViewTitle(View view) {
		//LOG.trace(TT+"setViewTitle(" + view.getName() + ")");
		if (view == null) {
			return;
		}
		String title = "";
		switch (SbView.getVIEW(view)) {
			case ATTRIBUTESLIST:
				title = "attribute.list";
				break;
			case BOOK:
				title = "view.book";
				break;
			case CHRONO:
				title = "view.chrono";
				break;
			case INFO:
				title = "view.info";
				break;
			case MANAGE:
				title = "view.manage";
				break;
			case MEMORIA:
				title = "view.memoria";
				break;
			/*case NAVIGATION:
				title = "navigation";
				break;*/
			case PLAN:
				title = "view.plan";
				break;
			case READING:
				title = "view.reading";
				break;
			case STORYBOARD:
				title = "view.storyboard";
				break;
			case STORYMAP:
				title = "view.storymap";
				break;
			case TIMELINE:
				title = "view.timeline";
				break;
			case TREE:
				title = "tree";
				break;

			case ATTRIBUTES:
				title = "attributes";
				break;
			case CATEGORIES:
				title = "categories";
				break;
			case CHAPTERS:
				title = "chapters";
				break;
			case ENDNOTES:
				title = "endnotes";
				break;
			case EPISODES:
				title = "episodes";
				break;
			case EVENTS:
				title = "events";
				break;
			case GENDERS:
				title = "genders";
				break;
			case IDEAS:
				title = "ideas";
				break;
			case INTERNALS:
				title = "internals";
				break;
			case ITEMS:
				title = "items";
				break;
			case LOCATIONS:
				title = "locations";
				break;
			case MEMOS:
				title = "memos";
				break;
			case PARTS:
				title = "parts";
				break;
			case PERSONS:
				title = "persons";
				break;
			//case PHOTOS: title="photos"; break;
			case PLOTS:
				title = "plots";
				break;
			case RELATIONS:
				title = "relations";
				break;
			case SCENES:
				title = "scenes";
				break;
			case STRANDS:
				title = "strands";
				break;
			case TAGS:
				title = "tags";
				break;

			case CHART_PERSONS_BY_DATE:
				title = "tools.charts.overall.strand.date";
				break;
			case CHART_PERSONS_BY_SCENE:
				title = "tools.charts.part.character.scene";
				break;
			case CHART_WIWW:
				title = "tools.charts.overall.whoIsWhereWhen";
				break;
			case CHART_STRANDS_BY_DATE:
				title = "tools.charts.overall.strand.date";
				break;
			case CHART_OCCURRENCE_OF_PERSONS:
				title = "tools.charts.overall.character.occurrence";
				break;
			case CHART_OCCURRENCE_OF_LOCATIONS:
				title = "tools.charts.overall.location.occurrence";
				break;
			case CHART_OCCURRENCE_OF_ITEMS:
				title = "tools.charts.overall.item.occurrence";
				break;
		}
		title = I18N.getMsg(title);
		view.getViewProperties().setTitle(title);
	}

	public SbView getView(AbstractEntity entity) {
		//LOG.trace(TT+"getView(" + viewName.name() + ")");
		switch (entity.getObjType()) {
			case ATTRIBUTE:
				return getTableView(VIEWNAME.ATTRIBUTES);
			case CATEGORY:
				return getTableView(VIEWNAME.CATEGORIES);
			case CHAPTER:
				return getTableView(VIEWNAME.CHAPTERS);
			case ENDNOTE:
				return getTableView(VIEWNAME.ENDNOTES);
			case EPISODE:
				return getTableView(VIEWNAME.EPISODES);
			case EVENT:
				return getTableView(VIEWNAME.EVENTS);
			case GENDER:
				return getTableView(VIEWNAME.GENDERS);
			case IDEA:
				return getTableView(VIEWNAME.IDEAS);
			case INTERNAL:
				return getTableView(VIEWNAME.INTERNALS);
			case ITEM:
				return getTableView(VIEWNAME.ITEMS);
			/*case ITEMLINK:
				return getTableView(VIEWNAME.ITEMLINKS);*/
			case LOCATION:
				return getTableView(VIEWNAME.LOCATIONS);
			case PART:
				return getTableView(VIEWNAME.PARTS);
			case PERSON:
				return getTableView(VIEWNAME.PERSONS);
			case PLOT:
				return getTableView(VIEWNAME.PLOTS);
			case RELATION:
				return getTableView(VIEWNAME.RELATIONS);
			case SCENE:
				return getTableView(VIEWNAME.SCENES);
			case STRAND:
				return getTableView(VIEWNAME.STRANDS);
			case TAG:
				return getTableView(VIEWNAME.TAGS);
			/*case TAGLINK:
				return getTableView(VIEWNAME.TAGLINKS);*/
			default:
				break;
		}
		return null;
	}

	public SbView getView(VIEWNAME viewName) {
		//LOG.trace(TT+"getView(" + viewName.name() + ")");
		SbView view = (SbView) viewMap.getView(viewName.toString());
		//LOG.trace("view="+(view!=null?view.toString():"null")+", viewName="+viewName.toString());
		if (view != null) {
			return view;
		}
		switch (viewName) {
			case ATTRIBUTES:
				return getTableView(VIEWNAME.ATTRIBUTES);
			case CATEGORIES:
				return getTableView(VIEWNAME.CATEGORIES);
			case CHAPTERS:
				return getTableView(VIEWNAME.CHAPTERS);
			case ENDNOTES:
				return getTableView(VIEWNAME.ENDNOTES);
			case EPISODES:
				return getEpsiodesView();
			case EVENTS:
				return getTableView(VIEWNAME.EVENTS);
			case GENDERS:
				return getTableView(VIEWNAME.GENDERS);
			case IDEAS:
				return getTableView(VIEWNAME.IDEAS);
			case INTERNALS:
				return getTableView(VIEWNAME.INTERNALS);
			case ITEMS:
				return getTableView(VIEWNAME.ITEMS);
			case ITEMLINKS:
				return getTableView(VIEWNAME.ITEMLINKS);
			case LOCATIONS:
				return getTableView(VIEWNAME.LOCATIONS);
			case MEMOS://not a table
				return getMemosView();
			case PARTS:
				return getTableView(VIEWNAME.PARTS);
			case PERSONS:
				return getTableView(VIEWNAME.PERSONS);
			case PLOTS:
				return getTableView(VIEWNAME.PLOTS);
			case RELATIONS:
				return getTableView(VIEWNAME.RELATIONS);
			case SCENES:
				return getTableView(VIEWNAME.SCENES);
			case STRANDS:
				return getTableView(VIEWNAME.STRANDS);
			case TAGS:
				return getTableView(VIEWNAME.TAGS);
			case TAGLINKS:
				return getTableView(VIEWNAME.TAGLINKS);

			case ATTRIBUTESLIST:
				return getAttributesListView();
			case BOOK:
				return getBookView();
			case CHRONO:
				return getChronoView();
			case INFO:
				return getQuickInfoView();
			case MANAGE:
				return getManageView();
			case MEMORIA:
				return getMemoriaView();
			/*case NAVIGATION:
				return getNavigationView();*/
			case PLAN:
				return getPlanView();
			case READING:
				return getReadingView();
			case SCENARIO:
				return getScenarioView();
			case STORYBOARD:
				return getStoryboardView();
			case STORYMAP:
				return getStorymapView();
			case TIMELINE:
				return getTimelineView();
			case TREE:
				return getTreeView();
			case TYPIST:
				return getTypistView();

			case CHART_PERSONS_BY_DATE:
				return getChartPersonsByDate();
			case CHART_PERSONS_BY_SCENE:
				return getChartPersonsByScene();
			case CHART_WIWW:
				return getChartWiWW();
			case CHART_STRANDS_BY_DATE:
				return getChartStrandsByDate();
			case CHART_OCCURRENCE_OF_PERSONS:
				return getChartOccurrenceOfPersons();
			case CHART_OCCURRENCE_OF_LOCATIONS:
				return getChartOccurrenceOfLocations();
			case CHART_OCCURRENCE_OF_ITEMS:
				return getChartOccurrenceOfItems();
			default:
				break;
		}
		return null;
	}

	public SbView getView(String viewName) {
		//LOG.trace(TT+"getView(string=" + viewName + ")");
		return (SbView) viewMap.getView(viewName);
	}

	public boolean loadView(SbView view) {
		//LOG.trace(TT+"loadView(view=" + (view==null?"null":view.getName()) + ")");
		if (view == null) {
			return (false);
		}
		AbstractPanel comp;
		switch (SbView.getVIEW(view)) {
			case ATTRIBUTESLIST:
				comp = new AttributesViewPanel(mainFrame);
				break;
			case BOOK:
				comp = new BookPanel(mainFrame);
				break;
			case CHRONO:
				comp = new Chrono(mainFrame);
				break;
			case INFO:
				comp = new InfoPanel(mainFrame);
				break;
			case MANAGE:
				comp = new Manage(mainFrame);
				break;
			case MEMORIA:
				comp = new MemoriaPanel(mainFrame);
				break;
			case MEMOS:
				comp = new MemosPanel(mainFrame);
				break;
			/*case NAVIGATION:
				comp = new Navigation(mainFrame);
				break;*/
			case PLAN:
				comp = new Planning(mainFrame);
				break;
			case READING:
				comp = new ReadingPanel(mainFrame);
				break;
			case SCENARIO:
				comp = new TypistScenario(mainFrame);
				break;
			case STORYBOARD:
				comp = new Storyboard(mainFrame);
				break;
			case STORYMAP:
				comp = new Storymap(mainFrame);
				break;
			case TIMELINE:
				comp = new TimelinePanel(mainFrame);
				break;
			case TREE:
				comp = new TreePanel(mainFrame);
				break;
			case TYPIST:
				comp = new TypistPanel(mainFrame);
				break;

			case CHART_OCCURRENCE_OF_ITEMS:
				comp = new OccurrenceOfItems(mainFrame);
				break;
			case CHART_OCCURRENCE_OF_LOCATIONS:
				comp = new OccurrenceOfLocations(mainFrame);
				break;
			case CHART_OCCURRENCE_OF_PERSONS:
				comp = new OccurrenceOfPersons(mainFrame);
				break;
			case CHART_PERSONS_BY_DATE:
				comp = new PersonsByDate(mainFrame);
				break;
			case CHART_PERSONS_BY_SCENE:
				comp = new PersonsByScene(mainFrame);
				break;
			case CHART_STRANDS_BY_DATE:
				comp = new StrandsByDate(mainFrame);
				break;
			case CHART_WIWW:
				comp = new WiWW(mainFrame);
				break;

			case ATTRIBUTES:
				comp = new AttributeTable(mainFrame);
				break;
			case CATEGORIES:
				comp = new CategoryTable(mainFrame);
				break;
			case CHAPTERS:
				comp = new ChapterTable(mainFrame);
				break;
			case ENDNOTES:
				comp = new EndnoteTable(mainFrame);
				break;
			case EPISODES:
				comp = new EpisodePanel(mainFrame);
				break;
			case EVENTS:
				comp = new EventTable(mainFrame);
				break;
			case GENDERS:
				comp = new GenderTable(mainFrame);
				break;
			case IDEAS:
				comp = new IdeaTable(mainFrame);
				break;
			case ITEMS:
				comp = new ItemTable(mainFrame);
				break;
			case LOCATIONS:
				comp = new LocationTable(mainFrame);
				break;
			case PARTS:
				comp = new PartTable(mainFrame);
				break;
			case PERSONS:
				comp = new PersonTable(mainFrame);
				break;
			case PLOTS:
				comp = new PlotTable(mainFrame);
				break;
			case RELATIONS:
				comp = new RelationTable(mainFrame);
				break;
			case SCENES:
				comp = new SceneTable(mainFrame);
				break;
			case STRANDS:
				comp = new StrandTable(mainFrame);
				break;
			case TAGS:
				comp = new TagTable(mainFrame);
				break;
			default:
				LOG.err(TT + ".loadView error: " + view.toString() + " not found");
				/*for (int i = 0; i < getViewMap().getViewCount(); ++i) {
					View v = getViewMap().getViewAtIndex(i);
					LOG.err("view number " + i + "=" + v.getName());
				}*/
				return (false);
		}
		comp.initAll();
		view.load(comp);
		if (isTable(view) && !initialisation) {
			loadTableDesign(view);
		}
		return (true);
	}

	public static boolean isTable(SbView view) {
		VIEWNAME t[] = {
			VIEWNAME.ATTRIBUTES,
			VIEWNAME.CHAPTERS,
			VIEWNAME.CATEGORIES,
			VIEWNAME.ENDNOTES,
			VIEWNAME.EVENTS,
			VIEWNAME.GENDERS,
			VIEWNAME.IDEAS,
			VIEWNAME.INTERNALS,
			VIEWNAME.ITEMS, VIEWNAME.ITEMLINKS,
			VIEWNAME.LOCATIONS,
			// not a table VIEWNAME.MEMOS,
			VIEWNAME.PARTS,
			VIEWNAME.PERSONS,
			VIEWNAME.PLOTS,
			VIEWNAME.RELATIONS,
			VIEWNAME.SCENES,
			VIEWNAME.STRANDS,
			VIEWNAME.TAGS, VIEWNAME.TAGLINKS
		};
		for (VIEWNAME v : t) {
			if (v.compare(view)) {
				return (true);
			}
		}
		return (false);
	}

	public void unloadView(SbView view) {
		//LOG.trace(TT+"unloadView(" + view.getName() + ")");
		if (isTable(view)) {
			saveTableDesign(view);
		}
		view.unload();
	}

	private String getChartName(String i18nKey) {
		//LOG.trace(TT+"getChartName(" + i18nKey + ")");
		return I18N.getMsg("chart") + ": " + I18N.getMsg(i18nKey);
	}

	public SbView getChartPersonsByDate() {
		//LOG.trace(TT+"getChartPersonsByDate()");
		if (isViewInitialized(VIEWNAME.CHART_PERSONS_BY_DATE)) {
			SbView view = new SbView(getChartName("tools.charts.overall.character.date"));
			view.setName(VIEWNAME.CHART_PERSONS_BY_DATE.toString());
			addRefreshButton(view);
			addExportButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.CHART_PERSONS_BY_DATE.toString());
	}

	public SbView getChartPersonsByScene() {
		//LOG.trace(TT+"getChartPersonsByScene()");
		if (isViewInitialized(VIEWNAME.CHART_PERSONS_BY_SCENE)) {
			SbView view = new SbView(getChartName("tools.charts.part.character.scene"));
			view.setName(VIEWNAME.CHART_PERSONS_BY_SCENE.toString());
			addRefreshButton(view);
			addExportButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.CHART_PERSONS_BY_SCENE.toString());
	}

	public SbView getChartWiWW() {
		//LOG.trace(TT+"getChartWiWW()");
		if (isViewInitialized(VIEWNAME.CHART_WIWW)) {
			SbView view = new SbView(getChartName("tools.charts.overall.whoIsWhereWhen"));
			view.setName(VIEWNAME.CHART_WIWW.toString());
			addRefreshButton(view);
			addExportButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.CHART_WIWW.toString());
	}

	public SbView getChartStrandsByDate() {
		//LOG.trace(TT+"getChartStrandsByDate()");
		if (isViewInitialized(VIEWNAME.CHART_STRANDS_BY_DATE)) {
			SbView view = new SbView(getChartName("tools.charts.overall.strand.date"));
			view.setName(VIEWNAME.CHART_STRANDS_BY_DATE.toString());
			addRefreshButton(view);
			addExportButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.CHART_STRANDS_BY_DATE.toString());
	}

	public SbView getChartOccurrenceOfPersons() {
		//LOG.trace(TT+"getChartOccurrenceOfPersons()");
		if (isViewInitialized(VIEWNAME.CHART_OCCURRENCE_OF_PERSONS)) {
			SbView view = new SbView(getChartName("tools.charts.overall.character.occurrence"));
			view.setName(VIEWNAME.CHART_OCCURRENCE_OF_PERSONS.toString());
			addRefreshButton(view);
			addExportButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.CHART_OCCURRENCE_OF_PERSONS.toString());
	}

	public SbView getChartOccurrenceOfLocations() {
		//LOG.trace(TT+"getChartOccurrenceOfLocations()");
		if (isViewInitialized(VIEWNAME.CHART_OCCURRENCE_OF_LOCATIONS)) {
			SbView view = new SbView(getChartName("tools.charts.overall.location.occurrence"));
			view.setName(VIEWNAME.CHART_OCCURRENCE_OF_LOCATIONS.toString());
			addRefreshButton(view);
			addExportButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.CHART_OCCURRENCE_OF_LOCATIONS.toString());
	}

	public SbView getChartOccurrenceOfItems() {
		//LOG.trace(TT+"getChartOccurrenceOfItems()");
		if (isViewInitialized(VIEWNAME.CHART_OCCURRENCE_OF_ITEMS)) {
			SbView view = new SbView(getChartName("tools.charts.overall.item.occurrence"));
			view.setName(VIEWNAME.CHART_OCCURRENCE_OF_ITEMS.toString());
			addRefreshButton(view);
			addExportButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.CHART_OCCURRENCE_OF_ITEMS.toString());
	}

	// table views
	public SbView getTableView(VIEWNAME vname) {
		SbView view = new SbView(I18N.getMsg(vname.toString().toLowerCase()));
		if (isViewInitialized(vname)) {
			view.setName(vname.toString());
			addRefreshButton(view);
			addExportButton(view);
			addPrintButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (view);
	}

	public SbView getBookView() {
		//LOG.trace(TT+"getBookView()");
		if (isViewInitialized(VIEWNAME.BOOK)) {
			SbView view = new SbView(I18N.getMsg("view.book"));
			view.setName(VIEWNAME.BOOK.toString());
			addRefreshButton(view);
			//addOptionsButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.BOOK.toString());
	}

	public SbView getChronoView() {
		//LOG.trace(TT+"getChronoView()");
		if (isViewInitialized(VIEWNAME.CHRONO)) {
			SbView view = new SbView(I18N.getMsg("view.chrono"));
			view.setName(VIEWNAME.CHRONO.toString());
			addRefreshButton(view);
			//addPrintButton(view);
			addExportButton(view);
			//addOptionsButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.CHRONO.toString());
	}

	public SbView getManageView() {
		//LOG.trace(TT+"getManageView()");
		if (isViewInitialized(VIEWNAME.MANAGE)) {
			SbView view = new SbView(I18N.getMsg("view.manage"));
			view.setName(VIEWNAME.MANAGE.toString());
			addRefreshButton(view);
			//addOptionsButton(view);
			//addButtons(view, OPTIONS);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.MANAGE.toString());
	}

	public SbView getMemoriaView() {
		//LOG.trace(TT+"getMemoriaView()");
		if (isViewInitialized(VIEWNAME.MEMORIA)) {
			SbView view = new SbView(I18N.getMsg("view.memoria"));
			view.setName(VIEWNAME.MEMORIA.toString());
			addRefreshButton(view);
			addExportButton(view);
			addPrintButton(view);
			//addOptionsButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.MEMORIA.toString());
	}

	public SbView getMemosView() {
		//LOG.trace(TT+"getMemoriaView()");
		if (isViewInitialized(VIEWNAME.MEMOS)) {
			SbView view = new SbView(I18N.getMsg("memos"));
			view.setName(VIEWNAME.MEMOS.toString());
			addRefreshButton(view);
			addExportButton(view);
			//addOptionsButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.MEMOS.toString());
	}

	public SbView getPlanView() {
		//LOG.trace(TT+"getPlanView()");
		if (isViewInitialized(VIEWNAME.PLAN)) {
			SbView view = new SbView(I18N.getMsg("view.plan"));
			view.setName(VIEWNAME.PLAN.toString());
			addRefreshButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.PLAN.toString());
	}

	public SbView getReadingView() {
		//LOG.trace(TT+"getReadingView()");
		if (isViewInitialized(VIEWNAME.READING)) {
			SbView view = new SbView(I18N.getMsg("view.reading"));
			view.setName(VIEWNAME.READING.toString());
			addRefreshButton(view);
			addPrintButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.READING.toString());
	}

	public SbView getStoryboardView() {
		//LOG.trace(TT+"getStoryboardView()");
		if (isViewInitialized(VIEWNAME.STORYBOARD)) {
			SbView view = new SbView(I18N.getMsg("view.storyboard"));
			view.setName(VIEWNAME.STORYBOARD.toString());
			addRefreshButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.STORYBOARD.toString());
	}

	public SbView getStorymapView() {
		//LOG.trace(TT+"getStoryboardView()");
		if (isViewInitialized(VIEWNAME.STORYMAP)) {
			SbView view = new SbView(I18N.getMsg("view.storymap"));
			view.setName(VIEWNAME.STORYMAP.toString());
			addRefreshButton(view);
			addPrintButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.STORYMAP.toString());
	}

	public SbView getTimelineView() {
		//LOG.trace(TT+"getTimelineView()");
		if (isViewInitialized(VIEWNAME.TIMELINE)) {
			SbView view = new SbView(I18N.getMsg("view.timeline"));
			view.setName(VIEWNAME.TIMELINE.toString());
			addRefreshButton(view);
			addPrintButton(view);
			//addExportButton(view);
			//addOptionsButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.TIMELINE.toString());
	}

	public SbView getTreeView() {
		//LOG.trace(TT+"getTreeView()");
		if (isViewInitialized(VIEWNAME.TREE)) {
			SbView view = new SbView(I18N.getMsg("tree"));
			view.setName(VIEWNAME.TREE.toString());
			addRefreshButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.TREE.toString());
	}

	public SbView getQuickInfoView() {
		//LOG.trace(TT+"getQuickInfoView()");
		if (isViewInitialized(VIEWNAME.INFO)) {
			SbView view = new SbView(I18N.getMsg("view.info"));
			view.setName(VIEWNAME.INFO.toString());
			addRefreshButton(view);
			addExportButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.INFO.toString());
	}

	public SbView getAttributesListView() {
		//LOG.trace(TT+"getAttributesListView()");
		if (isViewInitialized(VIEWNAME.ATTRIBUTESLIST)) {
			SbView view = new SbView(I18N.getMsg("attribute.list"));
			view.setName(VIEWNAME.ATTRIBUTESLIST.toString());
			addRefreshButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.ATTRIBUTESLIST.toString());
	}

	public SbView getEpsiodesView() {
		//LOG.trace(TT+"getEpisodesView()");
		if (isViewInitialized(VIEWNAME.EPISODES)) {
			SbView view = new SbView(I18N.getMsg("episodes"));
			view.setName(VIEWNAME.EPISODES.toString());
			addRefreshButton(view);
			addExportButton(view);
			addPrintButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.EPISODES.toString());
	}

	public SbView getScenarioView() {
		//LOG.trace(TT+"getScenarioView()");
		if (isViewInitialized(VIEWNAME.SCENARIO)) {
			SbView view = new SbView(I18N.getMsg("scenario"));
			view.setName(VIEWNAME.SCENARIO.toString());
			addRefreshButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.SCENARIO.toString());
	}

	public SbView getTypistView() {
		//LOG.trace(TT+"getTypistView()");
		if (isViewInitialized(VIEWNAME.TYPIST)) {
			SbView view = new SbView(I18N.getMsg("typist"));
			view.setName(VIEWNAME.TYPIST.toString());
			addRefreshButton(view);
			viewMap.addView(view.getName(), view);
		}
		return (SbView) viewMap.getView(VIEWNAME.TYPIST.toString());
	}

	@SuppressWarnings("unchecked")
	private void addRefreshButton(SbView view) {
		//LOG.trace(TT+"addRefreshButton("+view.getName()+")");
		JButton bt = createMiniButton("refresh");
		bt.addActionListener((ActionEvent e) -> {
			mainFrame.cursorSetWaiting();
			mainFrame.getBookModel().setRefresh(view);
			mainFrame.cursorSetDefault();
		});
		view.getCustomTabComponents().add(bt);
	}

	@SuppressWarnings("unchecked")
	private void addOptionsButton(final SbView view) {
		//LOG.trace(TT+"addOptionsButton("+view.getName()+")");
		JButton bt = createMiniButton("options");
		bt.addActionListener((ActionEvent e) -> {
			mainFrame.getBookController().showOptions(view);
		});
		view.getCustomTabComponents().add(bt);
	}

	@SuppressWarnings("unchecked")
	private void addExportButton(final SbView view) {
		//LOG.trace(TT+"addExportButton("+view.getName()+")");
		JButton bt = createMiniButton("export");
		bt.addActionListener((ActionEvent e) -> {
			mainFrame.getBookController().export(view);
		});
		view.getCustomTabComponents().add(bt);
	}

	@SuppressWarnings({"unchecked", "unused"})
	private void addPrintButton(final SbView view) {
		//LOG.trace(TT+"addPrintButton("+view.getName()+")");
		JButton bt = createMiniButton("print");
		bt.addActionListener((ActionEvent e) -> {
			mainFrame.getBookModel().print(view);
		});
		view.getCustomTabComponents().add(bt);
	}

	private JButton createMiniButton(String iconKey) {
		//LOG.trace(TT+"createMiniButton("+iconKey+","+toolTipKey+")");
		final JButton bt = new JButton(IconUtil.getIconMini(iconKey));
		bt.setOpaque(false);
		bt.setBorder(null);
		bt.setBorderPainted(false);
		bt.setContentAreaFilled(false);
		bt.setToolTipText(I18N.getMsg(iconKey));
		return bt;
	}

	public StringViewMap getViewMap() {
		//LOG.trace(TT+"getViewMap()");
		return viewMap;
	}

	private boolean isViewInitialized(VIEWNAME viewName) {
		return viewMap.getView(viewName.toString()) == null;
	}

	public void saveAllTableDesign() {
		if (viewMap.getViewCount() == 0) {
			return;
		}
		for (int i = 0; i < viewMap.getViewCount(); i++) {
			SbView view = (SbView) viewMap.getViewAtIndex(i);
			if (isTable(view) && view.isLoaded()) {
				AbsTable comp = (AbsTable) view.getComponent();
				comp.saveTableDesign();
			}
		}
		saveEpisodeDesign();
	}

	private void saveTableDesign(SbView view) {
		//LOG.trace(TT + ".saveTableDesign(view=" + view.getName() + ")");
		if (!isTable(view)) {
			return;
		}
		if (!view.isLoaded()) {
			return;
		}
		try {
			AbsTable comp = (AbsTable) view.getComponent();
			if (comp == null) {
				return;
			}
			JTable table = comp.getTable();
			if (table == null) {
				return;
			}
			comp.saveTableDesign();
		} catch (Exception e) {
			LOG.err("SbViewFactory.saveTableDesign(view=" + view.getName() + ")", e);
		}
	}

	private void loadTableDesign(SbView view) {
		if (null == view) {
			return;
		}
		((AbsTable) view.getComponent()).loadTableDesign();
	}

	public void saveEpisodeDesign() {
		if (viewMap.getViewCount() == 0) {
			return;
		}
		if (getView(VIEWNAME.EPISODES).getComponent() != null) {
			EpisodePanel p = (EpisodePanel) getView(VIEWNAME.EPISODES).getComponent();
			p.designSave();
		}
	}

}
