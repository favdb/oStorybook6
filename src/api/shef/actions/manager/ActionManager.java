/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.xml.sax.SAXException;
import resources.icons.IconUtil;

/**
 * Clase de gestion d'une action
 *
 * @author favdb
 */
public class ActionManager implements IconResolver {

	public static final String ID = "ID",
			TOOLBAR_SHOWS_TEXT = "TOOLBAR_SHOWS_TEXT",
			MENU_SHOWS_ICON = "MENU_SHOWS_ICON",
			BUTTON_TYPE = "BUTTON_TYPE",
			BUTTON_TYPE_VALUE_TOGGLE = "toggle",
			BUTTON_TYPE_VALUE_RADIO = "radio",
			BUTTON_TYPE_VALUE_CHECKBOX = "checkbox",
			GROUP = "GROUP",
			LARGE_ICON = "LARGE_ICON",
			SELECTED = "SELECTED",
			ACTION_ROLES = "ROLES",
			ACTION_CLASS = "ACTION_CLASS",
			WEIGHT = "weight";
	private static Class[] ARRAY_OF_ACTION_LISTENER_CLASS,
			ARRAY_OF_ACTION_EVENT_CLASS,
			ARRAY_OF_ITEM_LISTENER_CLASS,
			ARRAY_OF_ITEM_EVENT_CLASS;
	@SuppressWarnings("unchecked")
	private Map s_prototypes = Collections.synchronizedMap(new HashMap()),
			s_actionIdLists = Collections.synchronizedMap(new HashMap()),
			s_globalActions = Collections.synchronizedMap(new HashMap()),
			s_globalActionLists = Collections.synchronizedMap(new HashMap());
	private List roles;
	private IconResolver iconResolver;
	private static ActionManager INSTANCE;
	private static Map NAMED_INSTANCES;
	private static Object INSTANCE_LOCK = new Object();

	@SuppressWarnings("SynchronizeOnNonFinalField")
	public static ActionManager getInstance() {
		synchronized (INSTANCE_LOCK) {
			if (INSTANCE == null) {
				INSTANCE = new ActionManager();
				setInstance(null, INSTANCE);
			}
		}
		return INSTANCE;
	}

	@SuppressWarnings({"SynchronizeOnNonFinalField", "unchecked"})
	public static void setInstance(String name, ActionManager actionManager) {
		synchronized (INSTANCE_LOCK) {
			if (NAMED_INSTANCES == null) {
				NAMED_INSTANCES = Collections.synchronizedMap(new HashMap());
			}
			NAMED_INSTANCES.put(name, actionManager);
			if (name == null) {
				INSTANCE = actionManager;
			}
		}
	}

	public static ActionManager createNamedInstance(String name) {
		ActionManager actionManager = new ActionManager();
		setInstance(name, actionManager);
		return actionManager;
	}

	@SuppressWarnings("SynchronizeOnNonFinalField")
	public static ActionManager getInstance(String name) {
		synchronized (INSTANCE_LOCK) {
			if (NAMED_INSTANCES == null) {
				if (name == null) {
					return getInstance();
				}
				return null;
			}
			return (ActionManager) NAMED_INSTANCES.get(name);
		}
	}

	@SuppressWarnings("unchecked")
	public void reset() {
		this.s_prototypes = Collections.synchronizedMap(new HashMap());
		this.s_actionIdLists = Collections.synchronizedMap(new HashMap());
		this.s_globalActions = Collections.synchronizedMap(new HashMap());
		this.s_globalActionLists = Collections.synchronizedMap(new HashMap());
	}

	public void setRoles(List roles) {
		this.roles = roles;
	}

	@SuppressWarnings("unchecked")
	public List getRoles() {
		if (this.roles == null) {
			return null;
		}
		return Collections.unmodifiableList(this.roles);
	}

	public synchronized void register(File f) throws IOException, SAXException {
		//empty
	}

	public synchronized void register(URL url) throws IOException, SAXException {
		register(url, false);
	}

	public synchronized void register(URL url, boolean debug) throws IOException, SAXException {
		//empty
	}

	public synchronized void register(InputStream inStream) throws IOException, SAXException {
		register(inStream, false);
	}

	public synchronized void register(InputStream inStream, boolean debug) throws IOException, SAXException {
		//empty
	}

	@SuppressWarnings("unchecked")
	public synchronized void registerAction(Object id, Action action) throws IllegalArgumentException {
		if (action == null) {
			throw new IllegalArgumentException("Null action registered to ActionManager.");
		}
		if (this.s_globalActions.containsKey(id)) {
			throw new IllegalArgumentException("An action by the name " + id + "already exists.");
		}
		if (this.s_prototypes.containsKey(id)) {
			throw new IllegalArgumentException("An prototype action by the name " + id + "already exists.");
		}
		this.s_globalActions.put(id, action);
	}

	public synchronized boolean deregisterAction(Object id) {
		Object o = this.s_globalActions.remove(id);
		return (o != null);
	}

	@SuppressWarnings("unchecked")
	public synchronized void registerActionPrototype(Object id, ActionAttributes actionAtts) throws IllegalArgumentException {
		if (actionAtts == null) {
			throw new IllegalArgumentException("Null action registered to ActionManager.");
		}
		if (this.s_prototypes.containsKey(id)) {
			throw new IllegalArgumentException("An action protoype by the name " + id + "already exists.");
		}
		if (this.s_globalActions.containsKey(id)) {
			throw new IllegalArgumentException("An action by the name " + id + "already exists.");
		}
		this.s_prototypes.put(id, actionAtts);
	}

	@SuppressWarnings("unchecked")
	public Set getActionIds() {
		HashSet actionIds = new HashSet();
		actionIds.addAll(this.s_globalActions.keySet());
		actionIds.addAll(this.s_prototypes.keySet());
		return actionIds;
	}

	public Set getGlobalActionIds() {
		return this.s_globalActions.keySet();
	}

	public Set getPrototypeActionIds() {
		return this.s_prototypes.keySet();
	}

	public ActionAttributes getPrototype(Object id) {
		return (ActionAttributes) this.s_prototypes.get(id);
	}

	@SuppressWarnings("unchecked")
	public Action getAction(Object actionId) {
		Action action = (Action) this.s_globalActions.get(actionId);
		if (action == null) {
			action = createAction(actionId);
			if (action != null) {
				this.s_globalActions.put(actionId, action);
			}
		}
		if (action != null) {
			List list = (List) action.getValue(ACTION_ROLES);
			if (list != null && !containsAny(this.roles, list)) {
				return null;
			}
		}
		return action;
	}

	public Action createAction(Object prototypeId) {
		return createAction(prototypeId, null);
	}

	public Action createAction(Object prototypeId, Map context) {
		ActionAttributes attrs = (ActionAttributes) this.s_prototypes.get(prototypeId);
		if (attrs == null) {
			return null;
		}
		Action action = attrs.createAction();
		if (!passesRoleTest(action)) {
			return null;
		}
		if (action instanceof ContextManager) {
			ContextManager aware = (ContextManager) action;
			aware.setContext(context);
		}
		return action;
	}

	protected final boolean passesRoleTest(Action action) {
		if (action == null) {
			return false;
		}
		return passesRoleTest((List) action.getValue(ACTION_ROLES));
	}

	protected final boolean passesRoleTest(ActionList actionList) {
		return passesRoleTest(actionList.getRoles());
	}

	private boolean passesRoleTest(List defRoles) {
		if (defRoles == null) {
			return true;
		}
		return containsAny(this.roles, defRoles);
	}

	public boolean containsAny(Collection src, Collection target) {
		if (src == null) {
			return false;
		}
		Iterator iter = src.iterator();
		while (iter.hasNext()) {
			if (target.contains(iter.next())) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public synchronized void registerActionList(ActionList actionList) throws IllegalArgumentException {
		if (this.s_globalActionLists.get(actionList.getId()) != null) {
			throw new IllegalArgumentException("Already registered action list named:" + actionList.getId());
		}
		this.s_globalActionLists.put(actionList.getId(), actionList);
	}

	@SuppressWarnings("unchecked")
	public synchronized void registerActionIdList(ActionList actionIds) throws IllegalArgumentException {
		if (this.s_actionIdLists.get(actionIds.getId()) != null) {
			throw new IllegalArgumentException("Already registered action id list named:" + actionIds.getId());
		}
		this.s_actionIdLists.put(actionIds.getId(), actionIds);
	}

	public Set getActionListIds() {
		return this.s_actionIdLists.keySet();
	}

	public synchronized ActionList getActionIdList(Object listId) {
		ActionList idList = (ActionList) this.s_actionIdLists.get(listId);
		if (idList == null || !passesRoleTest(idList)) {
			return null;
		}
		return idList;
	}

	public synchronized boolean deregisterIdList(Object listId) {
		Object o = this.s_actionIdLists.remove(listId);
		this.s_globalActionLists.remove(listId);
		return (o != null);
	}

	@SuppressWarnings("unchecked")
	public synchronized ActionList getActionList(Object listId) {
		ActionList result = (ActionList) this.s_globalActionLists.get(listId);
		if (result == null) {
			result = createActionList(listId, true, null);
			if (result == null) {
				return null;
			}
			this.s_globalActionLists.put(listId, result);
			return result;
		}
		if (!passesRoleTest(result)) {
			return null;
		}
		return result;
	}

	public synchronized ActionList createActionList(Object actionListId) {
		return createActionList(actionListId, true, null);
	}

	public synchronized ActionList createActionList(Object actionListId, Map context) {
		return createActionList(actionListId, true, context);
	}

	public synchronized ActionList createActionList(Object actionListId, boolean useGlobalActions) {
		return createActionList(actionListId, useGlobalActions, null);
	}

	public synchronized ActionList createActionList(Object actionListId, boolean useGlobalActions, Map context) {
		ActionList actionIdList = getActionIdList(actionListId);
		if (actionIdList == null) {
			return null;
		}
		ActionList actionList = new ActionList(actionIdList.getId(),
				actionIdList.getTriggerActionId());
		actionList.setWeight(actionIdList.getWeight());
		fillListWithActions(actionList, actionIdList, useGlobalActions, context);
		return actionList;
	}

	@SuppressWarnings("unchecked")
	private synchronized void fillListWithActions(List toFill, List actionIds, boolean useGlobalActions, Map context) {
		for (int i = 0; i < actionIds.size(); i++) {
			Object elem = actionIds.get(i);
			Number weight = null;
			if (elem == null) {
				toFill.add(null);
				continue;
			}
			if (elem instanceof Separator) {
				weight = ((Separator) elem).getWeight();
				insertWithRespectToWeights(weight, toFill, elem);
				continue;
			}
			if (elem instanceof List) {
				List subIds = (List) elem;
				List subList;
				if (subIds instanceof ActionList) {
					ActionList subAL = (ActionList) subIds;
					if (!passesRoleTest(subAL)) {
						continue;
					}
					subList = new ActionList(subAL.getId(),
							subAL.getTriggerActionId());
					weight = subAL.getWeight();
					((ActionList) subList).setWeight(weight);
				} else {
					subList = new ArrayList(subIds.size());
				}
				insertWithRespectToWeights(weight, toFill, subList);
				fillListWithActions(subList, subIds, useGlobalActions, context);
				continue;
			}
			Action action;
			if (useGlobalActions) {
				action = getAction(actionIds.get(i));
			} else {
				action = createAction(actionIds.get(i), context);
			}
			if (action != null) {
				Object weightVal = action.getValue("weight");
				if (weightVal != null) {
					weight = Double.valueOf((String) weightVal);
				}
				insertWithRespectToWeights(weight, toFill, action);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void insertWithRespectToWeights(Number weight, List toFill, Object toInsert) throws NumberFormatException {
		for (int i = 0; i < toFill.size(); i++) {
			Number childWeight = null;
			Object child = toFill.get(i);
			if (child instanceof ActionList) {
				childWeight = ((ActionList) child).getWeight();
			} else if (child instanceof Action) {
				Object val = ((Action) child).getValue("weight");
				if (val != null) {
					childWeight = Double.valueOf((String) val);
				}
			} else if (child instanceof Separator) {
				Object val = ((Separator) child).getWeight();
				if (val != null) {
					childWeight = Double.valueOf((String) val);
				}
			}
			if (childWeight == null) {
				if (weight != null && weight.doubleValue() < 0.0D) {
					toFill.add(i, toInsert);
					return;
				}
			} else if (weight == null) {
				if (childWeight.doubleValue() > 0.0D) {
					toFill.add(i, toInsert);
					return;
				}
			} else if (weight.doubleValue() < childWeight.doubleValue()) {
				toFill.add(i, toInsert);
				return;
			}
		}
		toFill.add(toInsert);
	}

	public void registerActionCallback(Object actionId, Object callback, String method) throws NoSuchMethodException {
		Action action = getAction(actionId);
		if (action == null) {
			throw new IllegalArgumentException("No action found for action id " + actionId
					+ ", registering method " + method + ", callback "
					+ callback);
		}
		if (action instanceof Actionable) {
			registerActionCallback((Actionable) action, callback, method);
		} else {
			throw new NoSuchMethodException(
					"Action does not implement Actionable, no addActionListener(0 to use: action " + action
					+ ", registering method " + method + ", callback "
					+ callback);
		}
	}

	public void registerActionCallback(Actionable action, Object callback, String method) throws NoSuchMethodException {
		/*if (class$0 == null) {
			try {

			} catch (ClassNotFoundException classNotFoundException) {
				throw new NoClassDefFoundError(null.getMessage());
			}
		}
		ActionListener listenerProxy = (ActionListener) action.registerCallback((Actionable) callback,
				method,
				(String) ARRAY_OF_ACTION_EVENT_CLASS,
				(Class[]) class$0, class$0 = Class.forName("java.awt.event.ActionListener"),
				ARRAY_OF_ACTION_LISTENER_CLASS);
		action.addActionListener(listenerProxy);*/
	}

	public void registerStaticActionCallback(Object id, Class back, String method) throws NoSuchMethodException {
		registerActionCallback(id, back, method);
	}

	public void setEnabledAll(boolean enable) {
		if (this.s_globalActions == null || this.s_globalActions.isEmpty()) {
			return;
		}
		Iterator iter = this.s_globalActions.keySet().iterator();
		while (iter.hasNext()) {
			setEnabled(iter.next(), enable);
		}
	}

	public void setEnabled(Object id, boolean enable) {
		if (this.s_globalActions == null || this.s_globalActions.isEmpty()) {
			return;
		}
		Action action = (Action) this.s_globalActions.get(id);
		if (action == null) {
			return;
		}
		action.setEnabled(enable);
	}

	public void updateEnabledAll() {
		if (this.s_globalActions == null || this.s_globalActions.isEmpty()) {
			return;
		}
		Iterator iter = this.s_globalActions.keySet().iterator();
		while (iter.hasNext()) {
			updateEnabled(iter.next());
		}
	}

	public void putContextValueForAll(Object key, Object contextValue) {
		if (this.s_globalActions == null || this.s_globalActions.isEmpty()) {
			return;
		}
		Iterator iter = this.s_globalActions.keySet().iterator();
		while (iter.hasNext()) {
			Object action = this.s_globalActions.get(iter.next());
			if (action instanceof ContextManager) {
				ContextManager contextAwareAction = (ContextManager) action;
				contextAwareAction.putContextValue(key, contextValue);
			}
		}
	}

	public void updateEnabled(Object id) {
		if (this.s_globalActions == null || this.s_globalActions.isEmpty()) {
			return;
		}
		Action action = (Action) this.s_globalActions.get(id);
		if (action instanceof EnabledUpdater) {
			((EnabledUpdater) action).updateEnabled();
		}
	}

	public static void mapKeystrokeForAction(JComponent comp, Action action, int condition) {
		KeyStroke keystroke = (KeyStroke) action.getValue("AcceleratorKey");
		if (keystroke == null) {
			return;
		}
		comp.getActionMap().put(action.getValue("ActionCommandKey"), action);
		comp.getInputMap(condition).put(keystroke, action.getValue("ActionCommandKey"));
	}

	public static void mapKeystrokeForAction(JComponent comp, Action action) {
		mapKeystrokeForAction(comp, action, 1);
	}

	public IconResolver getIconResolver() {
		return this.iconResolver;
	}

	public void setIconResolver(IconResolver iconResolver) {
		this.iconResolver = iconResolver;
	}

	@Override
	public Icon resolveIcon(String imageURL) {
		if (this.iconResolver != null) {
			return this.iconResolver.resolveIcon(imageURL);
		}
		return IconUtil.getIconSmall(imageURL);
	}
}
