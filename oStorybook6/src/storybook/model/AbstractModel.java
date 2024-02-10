package storybook.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import storybook.ctrl.ActKey;
import storybook.ctrl.Ctrl;
import static storybook.ctrl.Ctrl.PROPS.*;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.tools.LOG;
import storybook.ui.MainFrame;

public abstract class AbstractModel {

	private static final String TT = "AbstractModel.";

	protected PropertyChangeSupport pcs;
	MainFrame mainFrame;
	String name;

	public AbstractModel(MainFrame m) {
		mainFrame = m;
		pcs = new PropertyChangeSupport(this);
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	public String getName() {
		return (name);
	}

	public abstract void fireAgain();

	public void testSession(String dbName) {
		//LOG.printInfos(TT+"testSession("+dbName+")");
	}

	public void initDefault() {
		fireAgain();
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		//LOG.printInfos(TT+"addPropertyChangeListener("+l.toString()+")");
		pcs.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void firePropertyChange(Book.TYPE type) {
		String prop = new ActKey(type, INIT).toString();
		firePropertyChange(prop, null, (List) mainFrame.project.getList(type));
	}

	protected void firePropertyChange(Book.TYPE type, Ctrl.PROPS cmd, AbstractEntity old, AbstractEntity value) {
		/*LOG.printInfos(TT + "firePropertyChange("
		   + "type=" + type.name()
		   + ", cmd=" + cmd.name()
		   + ", old=" + (old != null ? old.toString() : "null")
		   + ", value=" + (value != null ? value.toString() : "null") + ")");*/
		String prop = type.name() + "_" + cmd.name();
		try {
			firePropertyChange(prop, old, value);
		} catch (Exception ex) {
			LOG.err(prop, ex);
		}
	}

	protected void firePropertyChange(String prop, Object oldValue, Object newValue) {
		/*LOG.printInfos(TT + "firePropertyChange(" + "prop=" + prop
		   + ", oldValue=" + (oldValue != null ? oldValue.toString() : "null")
		   + ", newValue=" + (newValue != null ? newValue.toString() : "null") + ")");*/
		try {
			pcs.firePropertyChange(prop, oldValue, newValue);
		} catch (Exception ex) {
			LOG.err(prop, ex);
		}
	}

	public void ENTITY_Edit(AbstractEntity entity) {
		//LOG.printInfos(TT+"ENTITY_Edit("+entity.toString()+")");
		mainFrame.showEditorAsDialog(entity);
	}

	public void closeSession() {
	}
}
