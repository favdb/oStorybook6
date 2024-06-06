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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JMenuBar;
import storybook.App;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.scene.SceneStatus;
import storybook.model.AbstractModel;
import storybook.model.Model;
import storybook.project.Project;
import storybook.tools.LOG;
import storybook.ui.SbView;
import storybook.ui.panel.AbstractPanel;

/**
 * gestion par abstract du controller - gestion du model et des vues et des accès - appel du
 * fireAgain générique
 *
 * @author favdb
 */
public abstract class AbstractCtrl implements PropertyChangeListener {

    private static final String TT = "AbstractCtrl.";

    private final List<Component> attachedViews;
    private final List<AbstractModel> attachedModels;

    public AbstractCtrl() {
	//LOG.trace(TT+"new AbstractCtrl()");
	attachedViews = new CopyOnWriteArrayList<>();
	attachedModels = new ArrayList<>();
    }

    public void attachModel(AbstractModel model) {
	//LOG.trace(TT+"AbstractCtrl.attachModel(" + model.toString() + ")");
	attachedModels.add(model);
	model.addPropertyChangeListener(this);
	printAttachedModels();
    }

    public void detachModel(AbstractModel model) {
	//LOG.trace(TT+"AbstractCtrl.detachModel(" + model.toString() + ")");
	attachedModels.remove(model);
	model.removePropertyChangeListener(this);
	printAttachedModels();
    }

    public void attachView(Component view) {
	//LOG.trace(TT+".attachView(" + view.toString() + ")");
	synchronized (attachedViews) {
	    attachedViews.add(view);
	}
	/*if (App.getTrace()) {
			printNumberOfAttachedViews();
			printAttachedViews();
		}*/
    }

    public void detachView(Component view) {
	//LOG.trace(TT+".detachView(" + view.getName() + ")");
	synchronized (attachedViews) {
	    attachedViews.remove(view);
	}
	/*if (App.getTrace()) {
			printAttachedViews();
		}*/
    }

    public void printNumberOfAttachedViews() {
	//LOG.trace(TT+".printNumberOfAttachedViews():" + " attached views size: " + attachedViews.size());
    }

    public void printAttachedViews() {
	//LOG.trace("AbstractCtrl.printAttachedViews()");
    }

    public String getInfoAttachedViews() {
	return getInfoAttachedViews(false);
    }

    public String getInfoAttachedViews(boolean html) {
	StringBuilder buf = new StringBuilder();
	int i = 0;
	int size = attachedViews.size();
	for (Component view : attachedViews) {
	    buf.append("attached view ")
		    .append(i).append("/")
		    .append(size).append(": ")
		    .append(view.getClass().getSimpleName());
	    if (html) {
		buf.append("\n<br>");
	    } else {
		buf.append("\n");
	    }
	    ++i;
	}
	return buf.toString();
    }

    public void printAttachedModels() {
	//AbstractCtrl.printAttachedModels()
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	//LOG.trace(TT+".propertyChange(evt=" + evt.getPropertyName() + ")");
	synchronized (attachedViews) {
	    // must be in synchronized block
	    for (Component comp : attachedViews) {
		if (comp instanceof AbstractPanel) {
		    ((AbstractPanel) comp).modelPropertyChange(evt);
		} else if (comp instanceof JMenuBar) {
		    JMenuBar mb = (JMenuBar) comp;
		    PropertyChangeListener[] pcls = mb.getPropertyChangeListeners();
		    for (PropertyChangeListener pcl : pcls) {
			pcl.propertyChange(evt);
		    }
		} else if (comp instanceof App) {
		    ((App) comp).modelPropertyChange(evt);
		}
	    }
	}
    }

    public synchronized void fireAgain() {
	//LOG.trace(TT+".fireAgain()");
	for (AbstractModel model : attachedModels) {
	    model.fireAgain();
	}
    }

    public void setModelProperty(Book.TYPE type, Ctrl.PROPS cmd, AbstractEntity value) {
	/*LOG.trace(TT + "setModelProperty(type" + type.toString()
		+ "cmd=" + cmd.toString()
		+ "value=" + value.toString() + ")");*/
	for (AbstractModel model : attachedModels) {
	    if (!(model instanceof Model)) {
		return;
	    }
	    Model book = (Model) model;
	    switch (cmd) {
		case EDIT:
		    break;
		case NEW:
		    book.ENTITY_New(value);
		    break;
		case DELETE:
		    book.ENTITY_Delete(value);
		    break;
		case UPDATE:
		    book.ENTITY_Update(value);
		    break;
		default:
		    break;
	    }
	}
    }

    protected synchronized void setModelProperty(String propName, Object newValue) {
	/*LOG.trace(TT + "setModelProperty("
		+ "prop=" + propName
		+ ", newValue=" + (newValue == null ? "null" : newValue.toString())
		+ ")");*/
	if (newValue != null) {
	    for (AbstractModel model : attachedModels) {
		if (!(model instanceof Model)) {
		    return;
		}
		Model bookModel = (Model) model;
		try {
		    switch (Ctrl.getPROPS(propName)) {
			case READING_FONTSIZE:
			    bookModel.ReadingFontSize((int) newValue);
			    return;
			case SHOWOPTIONS:
			    if (newValue instanceof SbView) {
				bookModel.showOptions((SbView) newValue);
			    }
			    return;
			case SHOWINFO:
			    if (newValue instanceof AbstractEntity) {
				bookModel.InfoShow((AbstractEntity) newValue);
			    } else if (newValue instanceof Project) {
				bookModel.InfoShow((Project) newValue);
			    }
			    return;
			case EXPORT:
			    bookModel.setExport((SbView) newValue);
			    return;
			case PRINT:
			    bookModel.setPrint((SbView) newValue);
			    return;
			case REFRESH:
			    bookModel.setRefresh((SbView) newValue);
			    return;
			case SHOWINMEMORIA:
			    bookModel.MemoriaShowEntity((AbstractEntity) newValue);
			    return;
			case BOOK_SHOWENTITY:
			    bookModel.BookShowEntity((AbstractEntity) newValue);
			    return;
			case BOOK_ZOOM:
			    bookModel.BookZoom((Integer) newValue);
			    return;
			case CHRONO_SHOWDATEDIFFERENCE:
			    return;
			case CHRONO_LAYOUTDIRECTION:
			    bookModel.ChronoLayoutDirection((boolean) newValue);
			    return;
			case CHRONO_SHOWENTITY:
			    bookModel.ChronoShowEntity((AbstractEntity) newValue);
			    return;
			case CHRONO_ZOOM:
			    bookModel.ChronoZoom((Integer) newValue);
			    return;
			case MANAGE_HIDEUNASSIGNED:
			    bookModel.ManageHideUnassigned();
			    return;
			case MANAGE_VERTICAL:
			    bookModel.ManageVertical();
			    return;
			case MANAGE_SHOWENTITY:
			    bookModel.ManageShowEntity((AbstractEntity) newValue);
			    return;
			case MANAGE_COLUMNS:
			    bookModel.ManageColumns((Integer) newValue);
			    return;
			case MANAGE_ZOOM:
			    bookModel.ManageZoom((Integer) newValue);
			    return;
			case SCENE_FILTER_STATUS:
			    bookModel.setSceneFilter((SceneStatus) newValue);
			    return;
			case STORYBOARD_DIRECTION:
			    bookModel.StoryboardDirection();
			    return;
			case TIMELINE_OPTIONS:
			    bookModel.TimelineOptions((String) newValue);
			    return;
			case TIMELINE_ZOOM:
			    bookModel.TimelineZoom((Integer) newValue);
			    return;
			default:
			    LOG.err("unknown PROPS in AbstractCtrl: " + propName);
			    return;
		    }
		} catch (SecurityException | IllegalArgumentException e) {
		    e.printStackTrace(System.err);
		}
	    }
	}
    }

    protected synchronized void setModelProperty(String propertyName) {
	//LOG.trace(TT+"AbstractCtrl.setModelProperty(propertyName=" + propertyName + ")");
	for (AbstractModel model : attachedModels) {
	    try {
		Method method = model.getClass().getMethod("set" + propertyName);
		method.invoke(model);
	    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
		    | IllegalArgumentException | InvocationTargetException ex) {
	    }
	}
    }

    public int getNumberOfAttachedViews() {
	return attachedViews.size();
    }

    public List<Component> getAttachedViews() {
	return attachedViews;
    }

    public List<AbstractModel> getAttachedModels() {
	return attachedModels;
    }
}
