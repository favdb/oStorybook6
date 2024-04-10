/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions.manager;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.Action;

/**
 * Classe de définition d'une liste d'actions
 *
 * @author favdb
 */
public class ActionList implements List {

	private Object id;
	private Object triggerActionId;
	private Number weight;
	private List list = new ArrayList();
	private List roles;

	public ActionList(Object id) {
		this.id = id;
	}

	public ActionList(Object id, Object triggerActionId) {
		this(id);
		this.triggerActionId = triggerActionId;
	}

	public Object getId() {
		return this.id;
	}

	public Object getTriggerActionId() {
		return this.triggerActionId;
	}

	@SuppressWarnings("unchecked")
	public List getRoles() {
		if (this.roles == null) {
			return null;
		}
		return Collections.unmodifiableList(this.roles);
	}

	public Number getWeight() {
		return this.weight;
	}

	public void setWeight(Number weight) {
		this.weight = weight;
	}

	public void setRoles(List roles) {
		this.roles = roles;
	}

	public Action getActionById(Object id) {
		return findActionInList(id, this.list);
	}

	public void addActionListenerToAll(ActionListener delegate) {
		ActionBasic action;
		Iterator iter = iterator();
		while (iter.hasNext()) {
			Object item = iter.next();
			if (item == null) {
				continue;
			}
			if (item instanceof ActionBasic) {
				action = (ActionBasic) item;
				action.addActionListener(delegate);
			}
		}
		iter = iterator();
		while (iter.hasNext()) {
			Object subList = iter.next();
			if (subList == null) {
				continue;
			}
			if (subList instanceof ActionList) {
				((ActionList) subList).addActionListenerToAll(delegate);
			}
		}
	}

	private Action findActionInList(Object id, List actionList) {
		Iterator iter = actionList.iterator();
		while (iter.hasNext()) {
			Object item = iter.next();
			if (item == null) {
				continue;
			}
			if (item instanceof Action) {
				Action action = (Action) item;
				if (id.equals(action.getValue("ID"))) {
					return action;
				}
			}
		}
		iter = actionList.iterator();
		while (iter.hasNext()) {
			Object subList = iter.next();
			if (subList == null) {
				continue;
			}
			if (subList instanceof ActionList) {
				Action result = findActionInList(id, (ActionList) subList);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public void setContextForAll(Map context) {
		for (Iterator iterator = iterator(); iterator.hasNext();) {
			Object action = iterator.next();
			if (action instanceof ContextManager) {
				ContextManager aware = (ContextManager) action;
				aware.setContext(context);
			}
		}
	}

	public void putContextValueForAll(Object key, Object contextValue) {
		for (Iterator iterator = iterator(); iterator.hasNext();) {
			Object action = iterator.next();
			if (action instanceof ContextManager) {
				ContextManager aware = (ContextManager) action;
				aware.putContextValue(key, contextValue);
			}
		}
	}

	public void updateEnabledForAll() {
		for (Iterator iterator = iterator(); iterator.hasNext();) {
			Object action = iterator.next();
			if (action instanceof EnabledUpdater) {
				EnabledUpdater updater = (EnabledUpdater) action;
				updater.updateEnabled();
			}
		}
	}

	public void setEnabledForAll(boolean enabled) {
		for (Iterator iterator = iterator(); iterator.hasNext();) {
			Object action = iterator.next();
			if (action instanceof Action) {
				((Action) action).setEnabled(enabled);
			}
		}
	}

	@Override
	public int size() {
		return this.list.size();
	}

	@Override
	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return this.list.contains(o);
	}

	@Override
	public Iterator iterator() {
		return this.list.iterator();
	}

	@Override
	public Object[] toArray() {
		return this.list.toArray();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object[] toArray(Object[] a) {
		return this.list.toArray(a);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean add(Object o) {
		return this.list.add(o);
	}

	@Override
	public boolean remove(Object o) {
		return this.list.remove(o);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean containsAll(Collection c) {
		return this.list.containsAll(c);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean addAll(Collection c) {
		return this.list.addAll(c);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean addAll(int index, Collection c) {
		return this.list.addAll(index, c);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean removeAll(Collection c) {
		return this.list.removeAll(c);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean retainAll(Collection c) {
		return this.list.retainAll(c);
	}

	@Override
	public void clear() {
		this.list.clear();
	}

	@Override
	public Object get(int index) {
		return this.list.get(index);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object set(int index, Object element) {
		return this.list.set(index, element);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void add(int index, Object element) {
		this.list.add(index, element);
	}

	@Override
	public Object remove(int index) {
		return this.list.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return this.list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return this.list.lastIndexOf(o);
	}

	@Override
	public ListIterator listIterator() {
		return this.list.listIterator();
	}

	@Override
	public ListIterator listIterator(int index) {
		return this.list.listIterator(index);
	}

	@Override
	public List subList(int fromIndex, int toIndex) {
		return this.list.subList(fromIndex, toIndex);
	}
}
