/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions.manager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;

public class ActionAttributes {

	private Map attMap = new HashMap(10);
	private static Class defaultActionClass;

	public static Class getDefaultActionClass() {
		return defaultActionClass;
	}

	public static void setDefaultActionClass(Class defaultActionClass) {
		ActionAttributes.defaultActionClass = defaultActionClass;
	}

	public ActionAttributes() {
	}

	public ActionAttributes(ActionAttributes attrs) {
		Set keys = attrs.getKeys();
		Iterator iter = keys.iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			putValue(key, attrs.getValue((String) key));
		}
	}

	public Object getValue(String key) {
		return this.attMap.get(key);
	}

	public Set getKeys() {
		return this.attMap.keySet();
	}

	@SuppressWarnings("unchecked")
	public void putValue(Object key, Object value) {
		this.attMap.put(key, value);
	}

	@SuppressWarnings("deprecation")
	public Action createAction() {
		String specificClass = (String) this.attMap.get("ACTION_CLASS");
		Action action = null;
		if (specificClass != null)
      try {
			Class actionClass = Class.forName(specificClass);
			action = (Action) actionClass.newInstance();
		} catch (IllegalAccessException | InstantiationException | ClassNotFoundException ex1) {
			return null;
		}
		if (action == null) {
			action = instantiateDefaultAction();
		}
		configureAction(action);
		return action;
	}

	@SuppressWarnings("deprecation")
	protected Action instantiateDefaultAction() {
		if (defaultActionClass != null) {
			try {
				return (Action) defaultActionClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("Could not create action.", e);
			}
		}
		return new ActionBasic();
	}

	public void configureAction(Action action) {
		Set keys = this.attMap.keySet();
		Iterator iter = keys.iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			Object value = this.attMap.get(key);
			if ("enabled".equals(value)) {
				action.setEnabled(Boolean.parseBoolean((String) value));
				continue;
			}
			action.putValue(key, value);
		}
	}

	@Override
	public String toString() {
		return this.attMap.toString();
	}

}
