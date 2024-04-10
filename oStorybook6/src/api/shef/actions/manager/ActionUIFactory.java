/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions.manager;

import java.awt.Dimension;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * Classe de création d'éléments pour les menus, sous-menus, boutons
 *
 * @author favdb
 */
public class ActionUIFactory {

	private static ActionUIFactory instance;
	private static Map namedInstances;
	private static Object instanceLock = new Object();
	private static boolean sToolbarRequestFocusEnabled14 = !System.getProperty("java.vm.version").contains("1.3");
	private static boolean sSetButtonSizeFor13 = (System.getProperty("java.vm.version").contains("1.3"));
	private Dimension toolbarButtonPreferredSize = null;
	private ActionManager actionManager;

	protected ActionUIFactory(ActionManager manager) {
		if (manager == null) {
			manager = ActionManager.getInstance();
		}
		this.actionManager = manager;
	}

	@SuppressWarnings("SynchronizeOnNonFinalField")
	public static ActionUIFactory getInstance() {
		synchronized (instanceLock) {
			if (instance == null) {
				instance = new ActionUIFactory(ActionManager.getInstance());
				setInstance(null, instance);
			}
		}
		return instance;
	}

	public static ActionUIFactory createNamedInstance(String name, ActionManager manager) {
		ActionUIFactory factory = new ActionUIFactory(manager);
		setInstance(name, factory);
		return factory;
	}

	public static void setInstance(String name, ActionUIFactory factory) {
		setInstance(name, factory, (factory == null) ? null : factory.actionManager);
	}

	@SuppressWarnings({"SynchronizeOnNonFinalField", "unchecked"})
	public static void setInstance(String name, ActionUIFactory factory, ActionManager manager) {
		synchronized (instanceLock) {
			if (namedInstances == null) {
				namedInstances = Collections.synchronizedMap(new HashMap());
			}
			if (factory != null) {
				factory.actionManager = manager;
			}
			namedInstances.put(name, factory);
			if (name == null) {
				instance = factory;
			}
		}
	}

	@SuppressWarnings("SynchronizeOnNonFinalField")
	public static ActionUIFactory getInstance(String name) {
		synchronized (instanceLock) {
			if (namedInstances == null) {
				if (name == null) {
					return getInstance();
				}
				return null;
			}
			return (ActionUIFactory) namedInstances.get(name);
		}
	}

	public ActionManager getActionManager() {
		return this.actionManager;
	}

	public void setToolbarButtonPreferredSize(Dimension toolbarButtonPreferredSize) {
		this.toolbarButtonPreferredSize = toolbarButtonPreferredSize;
	}

	public JToolBar createToolBar(Object id) {
		ActionList actions = getActionManager().getActionList(id);
		if (actions == null) {
			return null;
		}
		return createToolBar(actions);
	}

	public JMenuBar createMenuBar(Object id) {
		ActionList list = getActionManager().getActionList(id);
		if (list == null) {
			return null;
		}
		JMenuBar menubar = instantiateJMenuBar();
		loadActions(list, menubar);
		return menubar;
	}

	public JToolBar createToolBar(ActionList actions) {
		JToolBar toolBar = instantiateJToolBar();
		loadActions(actions, toolBar);
		return toolBar;
	}

	public JMenuBar createMenuBar(ActionList actions) {
		JMenuBar menuBar = instantiateJMenuBar();
		loadActions(actions, menuBar);
		return menuBar;
	}

	public JMenu createMenu(Object listId) {
		return createMenu(getActionManager().getActionList(listId));
	}

	public JMenu createMenu(ActionList actions) {
		if (actions == null) {
			return null;
		}
		Action triggerAction = getActionManager().getAction(actions.getTriggerActionId());
		return createMenu(triggerAction, actions);
	}

	public JMenu createMenu(Action menuTriggerAction, ActionList actions) {
		JMenu menu = instantiateJMenu(menuTriggerAction);
		loadActions(menu, actions);
		return menu;
	}

	public JMenu createMenu(String menuName, ActionList actions) {
		JMenu menu = instantiateJMenu(menuName);
		loadActions(menu, actions);
		return menu;
	}

	public JPopupMenu createPopupMenu(Object listId) {
		ActionList actions = getActionManager().getActionList(listId);
		if (actions == null) {
			return null;
		}
		return createPopupMenu(actions);
	}

	public JPopupMenu createPopupMenu(ActionList list) {
		JPopupMenu popup = new JPopupMenu();
		loadActions(popup, list);
		return popup;
	}

	private JMenuItem createMenuItem(Object id) {
		Action action = getActionManager().getAction(id);
		if (action == null) {
			return null;
		}
		JMenuItem menuItem = createMenuItem(action);
		return menuItem;
	}

	public JMenuItem createMenuItem(Action action) {
		JMenuItem menuItem;
		Object object = action.getValue("BUTTON_TYPE");
		if (action.getValue("GROUP") != null) {
			menuItem = instantiateCheckBoxMenuItem(action);
		} else if ("toggle".equals(object)) {
			menuItem = instantiateCheckBoxMenuItem(action);
		} else if ("radio".equals(object)) {
			menuItem = instantiateRadioButtonMenuItem(action);
		} else if ("checkbox".equals(object)) {
			menuItem = instantiateCheckBoxMenuItem(action);
		} else {
			menuItem = instantiateJMenuItem(action);
		}
		configureMenuItem(menuItem);
		return menuItem;
	}

	public AbstractButton createButton(Object id) {
		Action action = getActionManager().getAction(id);
		if (action == null) {
			return null;
		}
		return createButton(action);
	}

	public AbstractButton createButton(Action action) {
		AbstractButton button;
		Object buttonType = action.getValue("BUTTON_TYPE");
		if (action.getValue("GROUP") != null) {
			button = instantiateToggleButton(action);
		} else if ("toggle".equals(buttonType)) {
			button = instantiateToggleButton(action);
		} else if ("radio".equals(buttonType)) {
			button = instantiateRadioButton(action);
		} else if ("checkbox".equals(buttonType)) {
			button = instantiateJCheckBox(action);
		} else {
			button = instantiateJButton(action);
		}
		configureToolBarButton(button);
		return button;
	}

	protected void configureToolBarButton(AbstractButton button) {
		commonConfig(button);
		button.setText("");
		Action action = button.getAction();
		if (action != null) {
			if (action.getValue("ShortDescription") == null) {
				button.setToolTipText((String) action.getValue("Name"));
			}
			if (action instanceof ActionBasic
					&& !((ActionBasic) action).getToolbarShowsText()) {
				button.setText("");
			}
		}
		if (this.toolbarButtonPreferredSize != null) {
			button.setPreferredSize(this.toolbarButtonPreferredSize);
			button.setMaximumSize(this.toolbarButtonPreferredSize);
		} else if (sSetButtonSizeFor13) {
			button.setPreferredSize(new Dimension(32, 32));
			button.setMaximumSize(new Dimension(32, 32));
		}
		if (sToolbarRequestFocusEnabled14) {
			button.setRequestFocusEnabled(false);
		}
	}

	protected void configureMenuItem(JMenuItem menuItem) {
		commonConfig(menuItem);
		menuItem.setToolTipText((String) null);
		Action action = menuItem.getAction();
		if (action == null) {
			return;
		}
		try {
			Boolean shows = (Boolean) action.getValue("MENU_SHOWS_ICON");
			if (shows != null && !shows) {
				menuItem.setIcon((Icon) null);
			}
		} catch (ClassCastException classCastException) {
		}
	}

	private void commonConfig(AbstractButton buttonOrMenuItem) {
		Action action = buttonOrMenuItem.getAction();
		if (action == null) {
			return;
		}
		buttonOrMenuItem.setActionCommand((String) action.getValue("ActionCommandKey"));
		if (isShouldSyncSelectedProperty(buttonOrMenuItem, action)) {
			ActionSelectionSynchronizer ass = new ActionSelectionSynchronizer(buttonOrMenuItem, action);
		}
		Boolean selected = (Boolean) action.getValue("SELECTED");
		if (selected != null) {
			buttonOrMenuItem.setSelected(selected);
		}
	}

	protected boolean isShouldSyncSelectedProperty(AbstractButton buttonOrMenuItem, Action action) {
		return true;
	}

	protected JToolBar instantiateJToolBar() {
		JToolBar toolBar = new JToolBar();
		return toolBar;
	}

	protected JMenuBar instantiateJMenuBar() {
		JMenuBar menubar = new JMenuBar();
		return menubar;
	}

	protected JMenu instantiateJMenu(Action menuAction) {
		JMenu menu = new JMenu(menuAction);
		return menu;
	}

	protected JMenu instantiateJMenu(String menuName) {
		JMenu menu = new JMenu(menuName);
		return menu;
	}

	protected JMenuItem instantiateJMenuItem(Action itemAction) {
		return new JMenuItem(itemAction);
	}

	protected JRadioButtonMenuItem instantiateRadioButtonMenuItem(Action itemAction) {
		return new JRadioButtonMenuItem(itemAction);
	}

	protected JCheckBoxMenuItem instantiateCheckBoxMenuItem(Action itemAction) {
		return new JCheckBoxMenuItem(itemAction);
	}

	protected JButton instantiateJButton(Action action) {
		return new JButton(action);
	}

	protected JCheckBox instantiateJCheckBox(Action action) {
		JCheckBox menu = new JCheckBox(action);
		return menu;
	}

	protected JToggleButton instantiateToggleButton(Action action) {
		return new JToggleButton(action);
	}

	protected JRadioButton instantiateRadioButton(Action action) {
		return new JRadioButton(action);
	}

	protected JSeparator instantiateSeparator() {
		return new JToolBar.Separator(null);
	}

	protected JSeparator instantiateInvisibleSeparator() {
		JToolBar.Separator sep = new JToolBar.Separator();
		sep.setVisible(false);
		return (sep);
	}

	@SuppressWarnings("unchecked")
	public void loadActions(ActionList actions, JToolBar toolBar) {
		if (actions == null) {
			return;
		}
		HashMap buttonGroupsByGroupID = new HashMap();
		Iterator iter = actions.iterator();
		while (iter.hasNext()) {
			Object elem = iter.next();
			if (elem == null || elem instanceof Separator) {
				if (elem instanceof Separator && !((Separator) elem).isLineVisible()) {
					toolBar.add(instantiateSeparator());
					continue;
				}
				toolBar.add(instantiateInvisibleSeparator());
				continue;
			}
			Action action = (Action) elem;
			AbstractButton button = createButton(action);
			toolBar.add(button);
			if (action.getValue("GROUP") != null) {
				ButtonGroup buttonGroup = (ButtonGroup) buttonGroupsByGroupID.get(action.getValue("GROUP"));
				if (buttonGroup == null) {
					buttonGroup = new ButtonGroup();
					buttonGroupsByGroupID.put(action.getValue("GROUP"), buttonGroup);
				}
				buttonGroup.add(button);
			}
		}
		if (sToolbarRequestFocusEnabled14) {
			toolBar.setRequestFocusEnabled(false);
		}
	}

	private void loadActions(ActionList list, JMenuBar menubar) {
		JMenu menu = null;
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			Object element = iter.next();
			if (element == null || element instanceof Separator) {
				if (menu != null) {
					menu.addSeparator();
				}
				continue;
			}
			if (element instanceof ActionList) {
				menu = createMenu((ActionList) element);
				if (menu != null) {
					menubar.add(menu);
				}
				continue;
			}
			if (menu != null) {
				if (element instanceof Action) {
					menu.add(createMenuItem((Action) element));
					continue;
				}
				menu.add(createMenuItem(element));
			}
		}
	}

	private void loadActions(JMenu menu, ActionList actions) {
		for (Object element : actions) {
			if (element == null || element instanceof Separator) {
				menu.addSeparator();
			} else if (element instanceof ActionList) {
				JMenu newMenu = createMenu((ActionList) element);
				if (newMenu != null) {
					menu.add(newMenu);
				}
			} else if (element instanceof Action) {
				menu.add(createMenuItem((Action) element));
			} else {
				menu.add(createMenuItem(element));
			}
		}
	}

	private void loadActions(JPopupMenu popup, ActionList actions) {
		HashMap buttonGroups = new HashMap();
		for (Object element : actions) {
			if (element == null || element instanceof Separator) {
				popup.addSeparator();
				continue;
			}
			if (element instanceof ActionList) {
				JMenu newMenu = createMenu((ActionList) element);
				if (newMenu != null) {
					popup.add(newMenu);
				}
				continue;
			}
			if (element instanceof Action) {
				JMenuItem jMenuItem = createMenuItem((Action) element);
				handleButtonGroups(jMenuItem, buttonGroups);
				popup.add(jMenuItem);
				continue;
			}
			JMenuItem mi = createMenuItem(element);
			handleButtonGroups(mi, buttonGroups);
			popup.add(mi);
		}
	}

	@SuppressWarnings("unchecked")
	private void handleButtonGroups(AbstractButton buttonOrMenu, Map buttonGroups) {
		if (buttonOrMenu == null) {
			return;
		}
		if (buttonOrMenu.getAction() != null) {
			Object group = buttonOrMenu.getAction().getValue("GROUP");
			if (group != null) {
				ButtonGroup bg = (ButtonGroup) buttonGroups.get(group);
				if (bg == null) {
					bg = new ButtonGroup();
					buttonGroups.put(group, bg);
				}
				bg.add(buttonOrMenu);
			}
		}
	}
}
