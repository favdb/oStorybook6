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
package storybook.ctrl;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import storybook.db.abs.AbsTable;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.category.Category;
import storybook.db.part.Part;
import storybook.db.plot.Plot;
import storybook.db.scene.SceneStatus;
import storybook.db.strand.Strand;
import storybook.exim.exporter.Exporter;
import storybook.model.BlankModel;
import storybook.model.Model;
import storybook.project.Project;
import storybook.ui.MainFrame;
import storybook.ui.SbView;

/**
 * @author martin
 *
 */
public class Ctrl extends AbstractCtrl {

	private static final String TT = "Ctrl.";

	public static boolean isDelete(PropertyChangeEvent evt) {
		return evt.getPropertyName().toLowerCase().contains("delete");
	}

	public static boolean isUpdate(PropertyChangeEvent evt) {
		return evt.getPropertyName().toLowerCase().contains("update");
	}

	MainFrame mainFrame;

	public Ctrl(MainFrame m) {
		super();
		mainFrame = m;
	}

	public Ctrl(MainFrame m, BlankModel model) {
		this(m);
		attachModel(model);
	}

	public Ctrl(MainFrame m, Model model) {
		this(m);
		attachModel(model);
	}

	public void setTableRow(AbstractEntity e) {
		SbView v = mainFrame.getView(e);
		if (v == null || !v.isLoaded()) {
			return;
		}
		if (v.getComponent() instanceof AbsTable) {
			((AbsTable) v.getComponent()).setCurrentRow(e);
		}
	}

	public enum PROPS {
		EXPORT,
		PRINT,
		REFRESH,
		SHOWOPTIONS,
		SHOWINFO,
		SHOWTASKLIST,
		SHOWINMEMORIA,
		UNLOADEDITOR,
		BOOK_HEIGHTFACTOR,
		BOOK_SHOWENTITY,
		BOOK_ZOOM,
		CHRONO_LAYOUTDIRECTION,
		CHRONO_LAYOUTNODATES,
		CHRONO_SHOWDATEDIFFERENCE,
		CHRONO_SHOWENTITY,
		CHRONO_ZOOM,
		MANAGE_COLUMNS,
		MANAGE_HIDEUNASSIGNED,
		MANAGE_VERTICAL,
		MANAGE_SHOWENTITY,
		MANAGE_ZOOM,
		MEMO_SHOW,
		MEMO_LAYOUTDIRECTION,
		MEMORIA_LAYOUT,
		READING_LAYOUT,
		READING_FONTSIZE,
		STORYBOARD_DIRECTION,
		STORYBOARD_ZOOM,
		TIMELINE_OPTIONS,
		TIMELINE_ZOOM,
		CHANGE,
		DELETE,
		DELETEMULTI,
		EDIT,
		INIT,
		NEW,
		ORDERUP,
		ORDERDOWN,
		UPDATE,
		SCENE_FILTER_STATUS,
		SCENE_FILTER_STRAND,
		SCENE_FILTER_NARRATOR,
		SCENE_FILTER_PERSON,
		SCENE_FILTER_ITEM,
		SCENE_FILTER_LOCATION,
		NONE;

		public boolean check(String prop) {
			return toString().equalsIgnoreCase(prop);
		}
	}

	public static PROPS getPROPS(PropertyChangeEvent evt) {
		return (getPROPS(evt.getPropertyName()));
	}

	public static PROPS getPROPS(String str) {
		for (PROPS p : PROPS.values()) {
			if (p.toString().equals(str)) {
				return (p);
			}
		}
		return (PROPS.NONE);
	}

	public synchronized void updateEntity(AbstractEntity entity) {
		//LOG.trace(TT + "updateEntity(" + entity.getObjType() + ")");
		mainFrame.getBookModel().ENTITY_Update(entity);
	}

	public void deleteEntity(AbstractEntity entity) {
		//LOG.trace("Ctrl.deleteEntity(" + entity.getObjType() + ")");
		mainFrame.getBookModel().ENTITY_Delete(entity);
	}

	public void newEntity(AbstractEntity entity) {
		//LOG.trace("Ctrl.newEntity(" + entity.toString() + ")");
		mainFrame.getBookModel().ENTITY_New(entity);
	}

	public void setEditEntity(AbstractEntity entity) {
		//LOG.trace("Ctrl.setEntityToEdit(" + entity.getObjType() + ")");
		mainFrame.getBookModel().ENTITY_Edit(entity);
	}

	public void deletemultiEntity(Book.TYPE type, ArrayList<Long> ids) {
		//LOG.trace("Ctrl.deleteMultiEdit(type="+type.toString()+",ids number="+ids.size()+")");
		mainFrame.getBookModel().ENTITY_Delete(type, ids);
	}

	// common
	public void refresh(SbView view) {
		//LOG.trace(TT + "refresh(view=" + view.getName() + ")");
		setModelProperty(PROPS.REFRESH.toString(), view);
	}

	public void showOptions(SbView view) {
		setModelProperty(PROPS.SHOWOPTIONS.toString(), view);
	}

	public void print(SbView view) {
		setModelProperty(PROPS.PRINT.toString(), view);
	}

	public void export(SbView view) {
		//LOG.trace("Ctrl.export(view=" + view.getName() + ")");
		Exporter xp = new Exporter(mainFrame);
		xp.exec(view);
		//setModelProperty(PROPS.EXPORT.toString(), view);
	}

	/**
	 * update Info view without showing it and focus for the given Entity
	 *
	 * @param entity
	 */
	public void infoSetTo(AbstractEntity entity) {
		// si suppression de l'automatisme mettre la ligne en commentaire
		setModelProperty(PROPS.SHOWINFO.toString(), entity);
	}

	/**
	 * update Info view with showing it and focus for the given Entity
	 *
	 * @param entity
	 */
	public void infoShow(AbstractEntity entity) {
		mainFrame.showView(SbView.VIEWNAME.INFO);
		setModelProperty(PROPS.SHOWINFO.toString(), entity);
	}

	/**
	 * update Info view with showing it and focus for the given Entity
	 *
	 * @param entity
	 */
	public void infoShow(Project entity) {
		mainFrame.showView(SbView.VIEWNAME.INFO);
		setModelProperty(PROPS.SHOWINFO.toString(), entity);
	}

	/**
	 * show and focus the Memoria view for the given Entity
	 *
	 * @param entity
	 */
	public void memoriaShow(AbstractEntity entity) {
		mainFrame.showView(SbView.VIEWNAME.MEMORIA);
		setModelProperty(PROPS.SHOWINMEMORIA.toString(), entity);
	}

	public void unloadEditor() {
		setModelProperty(PROPS.UNLOADEDITOR.toString(), null);
	}

	//*******************************
	//** chrono view
	//*******************************
	/**
	 * set the zoom level for the Chrono view
	 *
	 * @param val
	 */
	public void chronoSetZoom(Integer val) {
		setModelProperty(PROPS.CHRONO_ZOOM.toString(), val);
	}

	/**
	 * set the direction layout for the Chrono view
	 *
	 * @param val
	 */
	public void chronoSetLayoutDirection(Boolean val) {
		setModelProperty(PROPS.CHRONO_LAYOUTDIRECTION.toString(), val);
	}

	/**
	 * set the show nodates for the Chrono view
	 *
	 * @param val
	 */
	public void chronoSetLayoutNodates(Boolean val) {
		setModelProperty(PROPS.CHRONO_LAYOUTNODATES.toString(), val);
	}

	/**
	 * showing date difference in the Chrono view
	 *
	 * @param val
	 */
	public void chronoSetShowDateDifference(Boolean val) {
		setModelProperty(PROPS.CHRONO_SHOWDATEDIFFERENCE.toString(), val);
	}

	/**
	 * show and focus the Chrono view for the given Enetity
	 *
	 * @param entity
	 */
	public void chronoShow(AbstractEntity entity) {
		mainFrame.showView(SbView.VIEWNAME.CHRONO);
		setModelProperty(PROPS.CHRONO_SHOWENTITY.toString(), entity);
	}

	//*******************************
	//** book view
	//*******************************
	/**
	 * set the zoom level for the Book view
	 *
	 * @param val
	 */
	public void bookSetZoom(Integer val) {
		setModelProperty(PROPS.BOOK_ZOOM.toString(), val);
	}

	/**
	 * set the height factor for the Book view
	 *
	 * @param val
	 */
	public void bookSetHeightFactor(Integer val) {
		setModelProperty(PROPS.BOOK_HEIGHTFACTOR.toString(), val);
	}

	/**
	 * show and focus the Book view for the given Entity
	 *
	 * @param entity
	 */
	public void bookShow(AbstractEntity entity) {
		mainFrame.showView(SbView.VIEWNAME.BOOK);
		setModelProperty(PROPS.BOOK_SHOWENTITY.toString(), entity);
	}

	//*******************************
	//** manage view
	//*******************************
	/**
	 * set the zoom level for the Manage view
	 *
	 * @param val
	 */
	public void manageSetZoom(Integer val) {
		setModelProperty(PROPS.MANAGE_ZOOM.toString(), val);
	}

	/**
	 * set the number of columns for the Manage view
	 *
	 * @param val
	 */
	public void manageSetColumns(Integer val) {
		setModelProperty(PROPS.MANAGE_COLUMNS.toString(), val);
	}

	/**
	 * show and focus the Manage view
	 *
	 * @param entity
	 */
	public void manageShow(AbstractEntity entity) {
		mainFrame.showView(SbView.VIEWNAME.MANAGE);
		setModelProperty(PROPS.MANAGE_SHOWENTITY.toString(), entity);
	}

	/**
	 * hide/show the unassigned scenes in the Manage view
	 *
	 * @param val
	 */
	public void manageSetHideUnassigned(Boolean val) {
		setModelProperty(PROPS.MANAGE_HIDEUNASSIGNED.toString(), val);
	}

	/**
	 * set the layout to be vertical for the Manage view
	 *
	 * @param val
	 */
	public void manageSetVertical(Boolean val) {
		setModelProperty(PROPS.MANAGE_VERTICAL.toString(), val);
	}

	//*******************************
	//** reading view
	//*******************************
	/**
	 * set the zoom level for Reading view
	 *
	 * @param val
	 */
	public void readingSetZoom(Integer val) {
		setModelProperty(PROPS.READING_LAYOUT.toString(), val);
	}

	/**
	 * set the font size for the Reading view
	 *
	 * @param val
	 */
	public void readingSetFontSize(Integer val) {
		setModelProperty(PROPS.READING_FONTSIZE.toString(), val);
	}

	// memoria view
	public void memoriaSetLayout(int val) {
		setModelProperty(PROPS.MEMORIA_LAYOUT.toString(), val);
	}

	// timeline view
	public void timelineSetZoom(Integer val) {
		setModelProperty(PROPS.TIMELINE_ZOOM.toString(), val);
	}

	public void timelineSetOptions(String val) {
		setModelProperty(PROPS.TIMELINE_OPTIONS.toString(), val);
	}

	// part
	public void changePart(Part part) {
		String prop = new ActKey(Book.TYPE.PART, Ctrl.PROPS.CHANGE).toString();
		setModelProperty(prop, part);
	}

	// plot
	public void orderUpPlot(Plot entity) {
		String prop = new ActKey(Book.TYPE.PLOT, Ctrl.PROPS.ORDERUP).toString();
		setModelProperty(prop, entity);
	}

	public void orderDownPlot(Plot entity) {
		String prop = new ActKey(Book.TYPE.PLOT, Ctrl.PROPS.ORDERDOWN).toString();
		setModelProperty(prop, entity);
	}

	// category
	public void orderUpCategory(Category entity) {
		if (entity == null) {
			return;
		}
		String prop = new ActKey(Book.TYPE.CATEGORY, Ctrl.PROPS.ORDERUP).toString();
		setModelProperty(prop, entity);
	}

	public void orderDownCategory(Category entity) {
		if (entity == null) {
			return;
		}
		String prop = new ActKey(Book.TYPE.CATEGORY, Ctrl.PROPS.ORDERDOWN).toString();
		setModelProperty(prop, entity);
	}

	// strands
	public void orderUpStrand(Strand entity) {
		String prop = new ActKey(Book.TYPE.STRAND, Ctrl.PROPS.ORDERUP).toString();
		setModelProperty(prop, entity);
	}

	public void orderDownStrand(Strand entity) {
		String prop = new ActKey(Book.TYPE.STRAND, Ctrl.PROPS.ORDERDOWN).toString();
		setModelProperty(prop, entity);
	}

	// scenes
	public void filterScenes(SceneStatus state) {
		setModelProperty(PROPS.SCENE_FILTER_STATUS.toString(), state);
	}

	public void filterScenesStrand(String strand) {
		setModelProperty(PROPS.SCENE_FILTER_STRAND.toString(), strand);
	}

	public void filterScenesNarrator(String person) {
		setModelProperty(PROPS.SCENE_FILTER_NARRATOR.toString(), person);
	}

	public void filterScenesPerson(String person) {
		setModelProperty(PROPS.SCENE_FILTER_PERSON.toString(), person);
	}

	public void filterScenesLocation(String location) {
		setModelProperty(PROPS.SCENE_FILTER_LOCATION.toString(), location);
	}

	public void filterScenesItem(String item) {
		setModelProperty(PROPS.SCENE_FILTER_ITEM.toString(), item);
	}

}
