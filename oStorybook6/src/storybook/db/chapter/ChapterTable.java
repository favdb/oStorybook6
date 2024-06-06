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
package storybook.db.chapter;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import storybook.ctrl.ActKey;
import storybook.db.DB;
import static storybook.db.DB.DATA.*;
import storybook.db.abs.AbsColumn;
import static storybook.db.abs.AbsColumn.*;
import storybook.db.abs.AbsTable;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.book.BookUtil;
import storybook.db.scene.Scene;
import storybook.exim.exporter.ExportToPhpBB;
import storybook.tools.LOG;
import storybook.ui.MainFrame;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class ChapterTable extends AbsTable {

    private static final String TT = "ChapterTable";

    public ChapterTable(MainFrame mainFrame) {
	super(mainFrame, Book.TYPE.CHAPTER);
    }

    @Override
    public void init() {
	this.withPart = true;
	allowMultiDelete = true;
    }

    @Override
    public void initUi() {
	super.initUi();
	/*if (Book.getNbChapters(mainFrame) < 2) {
			toolbar.setVisible(false);
		}*/
    }

    @Override
    protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
	ActKey act = new ActKey(evt);
	if ((act.isUpdate() || act.isDelete())
		&& (act.getType() == Book.TYPE.PART
		|| act.getType() == Book.TYPE.SCENE
		|| act.getType() == Book.TYPE.CHAPTER)) {
	    fillTable();
	}
    }

    @Override
    protected AbstractEntity getEntity(Long id) {
	Chapter chapter = (Chapter) mainFrame.project.get(Book.TYPE.CHAPTER, id);
	return chapter;
    }

    @Override
    public void updateRow(AbstractEntity entity) {
	// if scene refresh for nbScenes, words and chars count
	if (Book.getTYPE(entity) == Book.TYPE.SCENE) {
	    fillTable();
	}
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AbstractEntity> getAllEntities() {
	@SuppressWarnings("unchecked")
	List<Chapter> ret = (List) mainFrame.project.getList(getType());
	for (Chapter e : ret) {
	    e.setNbWords(BookUtil.getNbWords(mainFrame.project, e));
	    e.setNbChars(BookUtil.getNbChars(mainFrame.project, e));
	    e.setNbScenes(mainFrame.project.scenes.find(e).size());
	}
	if (getCurrentPart() != null) {
	    List<AbstractEntity> chapters = new ArrayList<>();
	    for (Chapter e : ret) {
		if (e.hasPart() && e.getPart().getId().equals(getCurrentPart().getId())) {
		    chapters.add(e);
		}
	    }
	    return (chapters);
	}
	return (List) ret;
    }

    @Override
    public List<AbsColumn> getColumns(AbstractEntity entity) {
	List<AbsColumn> cols = super.getColumns(entity);
	cols.add(new AbsColumn(mainFrame, cols, PART, TCR_ENTITY));

	cols.add(new AbsColumn(mainFrame, cols, NUMBER, NUMERIC, AL_CENTER));
	cols.add(new AbsColumn(mainFrame, cols, SCENES, NUMERIC, AL_CENTER));
	cols.add(new AbsColumn(mainFrame, cols, INTENSITY, AL_CENTER, TCR_HIDE, TCR_COLOR, AL_CENTER));
	cols.add(new AbsColumn(mainFrame, cols, WORDS, NUMERIC_RENDERER, AL_CENTER, TCR_HIDE));
	cols.add(new AbsColumn(mainFrame, cols, CHARACTERS, NUMERIC_RENDERER, AL_CENTER, TCR_HIDE));
	cols.add(new AbsColumn(mainFrame, cols, OBJECTIVE_DATE, TCR_DAY, AL_CENTER, TCR_HIDE));
	cols.add(new AbsColumn(mainFrame, cols, OBJECTIVE_SIZE, NUMERIC_RENDERER, AL_CENTER, TCR_HIDE));
	cols.add(new AbsColumn(mainFrame, cols, OBJECTIVE_DONE, TCR_DAY, AL_CENTER, TCR_HIDE));
	cols.add(new AbsColumn(mainFrame, cols, OBJECTIVE_REALIZED, AL_CENTER, TCR_HIDE));

	this.getColumnsEnd(cols, entity);

	return (cols);
    }

    @Override
    public List<Object> getRow(AbstractEntity entity) {
	List<Object> cols = super.getRow(entity);
	Chapter e = (Chapter) entity;
	cols.add(e.getPart());
	cols.add(e.getChapterno());
	List<Scene> scenes = mainFrame.project.scenes.findBy(e);
	cols.add(scenes.size());
	cols.add(Scene.getIntensity(scenes));
	cols.add(BookUtil.getNbWords(mainFrame.project, e));
	cols.add(BookUtil.getNbChars(mainFrame.project, e));
	cols.add(e.getObjectiveTime());
	cols.add(e.getObjectiveChars());
	cols.add(e.getDoneTime());
	String realized = "";
	if (e.getObjectiveChars() > 0) {
	    float c = (BookUtil.getNbChars(mainFrame.project, e) * 100), t = e.getObjectiveChars();
	    realized = String.format("%.2f%%", c / t);
	}
	cols.add(realized);
	getRowEnd(cols, entity);
	return (cols);
    }

    /**
     * Table change event (simple change selected row)
     *
     * @param e
     */
    @Override
    public void tableChanged(TableModelEvent e) {
	/*LOG.trace(TT + "tableChanged(e=" + e.toString() + ")"
		+ " for row=" + e.getFirstRow() + ", col=" + e.getColumn());*/
	super.tableChanged(e);
	int row = e.getFirstRow();
	if (row < 0 || row != e.getLastRow()) {
	    return;
	}
	AbstractEntity c = (AbstractEntity) this.getEntityFromRow(row);
	int col = e.getColumn();
	if (c != null && c instanceof Chapter && col != -1) {
	    TableModel model = table.getModel();
	    Chapter chapter = (Chapter) c;
	    DB.DATA data = DB.getData(model.getColumnName(col));
	    Object value = model.getValueAt(row, col);
	    switch (data) {
		case OBJECTIVE_SIZE:
		    if (!(value instanceof Integer)) {
			return;
		    }
		    chapter.setObjectiveChars((Integer) value);
		    break;
		default:
		    return;
	    }
	    //mainFrame.getBookController().updateEntity(chapter);
	}
    }

    @Override
    protected synchronized void copyToPhpBB(int row) {
	LOG.trace(TT + ".copyTo(r=" + row + ")");
	Chapter chapter = (Chapter) getEntityFromRow(row);
	if (chapter != null) {
	    ExportToPhpBB.getChapter(mainFrame, chapter);
	}
    }

}
