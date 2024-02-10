/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions.manager;

import i18n.I18N;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.event.EventListenerList;
import resources.icons.ICONS;
import resources.icons.IconUtil;

/**
 * Classe type de desciption d'un action
 *
 * adaptation favdb
 */
public class ActionBasic extends AbstractAction
	implements Actionable, ItemAction, DelegatesEnabled, ContextManager {

	public static enum BUTTON_TYPE {
		TOGGLE, RADIO, CHECKBOX, NORMAL
	}

	protected static boolean DEFAULT_ENABLED_STATE = true;
	EventListenerList listenerList = new EventListenerList();
	private List enabledDelegates;
	private Map context;

	public ActionBasic() {
	}

	public ActionBasic(String id) {
		this(id, (Icon) null);
	}

	public ActionBasic(String id, Icon icon) {
		this(id, (String) null, (String) null, (Integer) null, (KeyStroke) null, icon);
	}

	public ActionBasic(String id, String shortDesc, Icon icon, KeyStroke accelerator) {
		this(id, shortDesc, (String) null, (Integer) null, accelerator, icon);
	}

	public ActionBasic(String id,
		Integer mnemonic, KeyStroke accelerator, Icon icon) {
		this(id, (String) null, (String) null, mnemonic, accelerator, icon);
	}

	public ActionBasic(String id,
		String shortDesc, String longDesc,
		Integer mnemonic, KeyStroke accelerator,
		Icon icon) {
		this(id, id, id, shortDesc, longDesc, mnemonic, accelerator, icon);
	}

	public ActionBasic(String id,
		String actionName, String actionCommandName,
		String shortDesc, String longDesc,
		Integer mnemonic, KeyStroke accelerator,
		Icon icon) {
		this(id, id, id, shortDesc, longDesc, mnemonic, accelerator, icon, false, false);
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public ActionBasic(String id, String actionName,
		String actionCommandName,
		String shortDesc, String longDesc,
		Integer mnemonic, KeyStroke accelerator,
		Icon icon, boolean toolbarShowsText, boolean menuShowsIcon) {
		setId(id);
		setActionName(actionName);
		setActionCommandName(actionCommandName);
		setShortDescription(shortDesc);
		setLongDescription(longDesc);
		setMnemonic(mnemonic);
		setAccelerator(accelerator);
		setSmallIcon(icon);
		setToolbarShowsText(toolbarShowsText);
		setMenuShowsIcon(menuShowsIcon);
	}

	@Override
	public void addActionListener(ActionListener l) {
		this.listenerList.add(ActionListener.class, l);
	}

	@Override
	public void removeActionListener(ActionListener l) {
		this.listenerList.remove(ActionListener.class, l);
	}

	@Override
	public final void actionPerformed(ActionEvent evt) {
		actionPerformedTemplate(evt);
	}

	protected void actionPerformedTemplate(ActionEvent evt) {
		try {
			actionPerformedTry();
			execute(evt);
			propogateActionEvent(evt);
		} catch (Exception t) {

		} finally {
			actionPerformedFinally();
		}
	}

	protected void propogateActionEvent(ActionEvent evt) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i].getClass().equals(ActionListener.class)) {
				((ActionListener) listeners[i + 1]).actionPerformed(evt);
			}
		}
	}

	protected void actionPerformedTry() {
		//empty
	}

	protected void execute(ActionEvent evt) throws Exception {
		//empty
	}

	protected void actionPerformedCatch(Throwable t) {
		System.err.println("Exception in action " + this + ".  Exception:" + t);
		t.printStackTrace(System.err);
		throw new RuntimeException(t);
	}

	protected void actionPerformedFinally() {
		//empty
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addShouldBeEnabledDelegate(ShouldBeEnabledDelegate enabledDelegate) {
		if (this.enabledDelegates == null) {
			this.enabledDelegates = new ArrayList(3);
		}
		this.enabledDelegates.add(enabledDelegate);
	}

	@Override
	public void removeShouldBeEnabledDelegate(ShouldBeEnabledDelegate enabledDelegate) {
		if (this.enabledDelegates == null) {
			return;
		}
		this.enabledDelegates.remove(enabledDelegate);
	}

	public void updateEnabledState() {
		boolean shouldBe = shouldBeEnabled();
		setEnabled(shouldBe);
	}

	public boolean shouldBeEnabled() {
		if (this.enabledDelegates == null || this.enabledDelegates.isEmpty()) {
			return DEFAULT_ENABLED_STATE;
		}
		@SuppressWarnings("unchecked")
		List copy = new ArrayList(this.enabledDelegates);
		Iterator iter = copy.iterator();
		while (iter.hasNext()) {
			ShouldBeEnabledDelegate enabledUpdater = (ShouldBeEnabledDelegate) iter.next();
			if (enabledUpdater != null
				&& enabledUpdater.shouldBeEnabled(this) != DEFAULT_ENABLED_STATE) {
				return !DEFAULT_ENABLED_STATE;
			}
		}
		return DEFAULT_ENABLED_STATE;
	}

	@Override
	public void addItemListener(ItemListener l) {
		this.listenerList.add(ItemListener.class, l);
	}

	@Override
	public void removeItemListener(ItemListener l) {
		this.listenerList.remove(ItemListener.class, l);
	}

	protected void propogateItemEvent(ItemEvent evt) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i].getClass().equals(ItemListener.class)) {
				((ItemListener) listeners[i + 1]).itemStateChanged(evt);
			}
		}
	}

	@Override
	public synchronized boolean isSelected() {
		Object actionSelected = getValue("SELECTED");
		return Boolean.TRUE.equals(actionSelected);
	}

	@Override
	public synchronized void setSelected(boolean toSel) {
		boolean old = isSelected();
		if (old != toSel) {
			putValue("SELECTED", toSel);
			firePropertyChange("selected", old, toSel);
		}
	}

	public Object getGroup() {
		return getValue("GROUP");
	}

	@Override
	public void setContext(Map context) {
		this.context = context;
		contextChanged();
	}

	@Override
	public Map getContext() {
		return this.context;
	}

	@Override
	public void clearContext() {
		if (this.context != null) {
			this.context.clear();
		}
		contextChanged();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void putContextValue(Object key, Object contextValue) {
		if (this.context == null) {
			this.context = new HashMap(3);
		}
		this.context.put(key, contextValue);
		contextChanged();
	}

	@Override
	public Object getContextValue(Object key) {
		if (this.context == null) {
			return null;
		}
		return this.context.get(key);
	}

	protected void contextChanged() {
		updateEnabledState();
	}

	private static final String ID = "ID";

	public String getId() {
		return (String) getValue(ID);
	}

	public void setId(String id) {
		putValue(ID, id);
	}

	public String getActionName() {
		return (String) getValue("Name");
	}

	public void setActionName(String actionName) {
		putValue("Name", actionName);
	}

	public String getActionCommandName() {
		return (String) getValue("ActionCommandKey");
	}

	public void setActionCommandName(String actionCommandName) {
		putValue("ActionCommandKey", actionCommandName);
	}

	public String getShortDescription() {
		return (String) getValue("ShortDescription");
	}

	public void setShortDescription(String shortDesc) {
		putValue("ShortDescription", shortDesc);
	}

	public String getLongDescription() {
		return (String) getValue("LongDescription");
	}

	public void setLongDescription(String longDesc) {
		putValue("LongDescription", longDesc);
	}

	public Integer getMnemonic() {
		return (Integer) getValue("MnemonicKey");
	}

	protected void setMnemonic(Integer mnemonic) {
		putValue("MnemonicKey", mnemonic);
	}

	protected void setMnemonic(String mnemonic) {
		putValue("MnemonicKey", I18N.getMnem("shef." + mnemonic));
	}

	public KeyStroke getAccelerator() {
		return (KeyStroke) getValue("AcceleratorKey");
	}

	public void setAccelerator(KeyStroke accelerator) {
		putValue("AcceleratorKey", accelerator);
	}

	public ImageIcon getSmallIcon() {
		return (ImageIcon) getValue("SmallIcon");
	}

	public void setSmallIcon(Icon smallIcon) {
		putValue("SmallIcon", smallIcon);
	}

	public void setSmallIcon(ICONS.K icon) {
		putValue("SmallIcon", IconUtil.getIconSmall(icon));
	}

	public void setButtonType(BUTTON_TYPE type) {
		putValue("ButtonType", type);
	}

	public BUTTON_TYPE getButtonType() {
		return (BUTTON_TYPE) getValue("ButtonType");
	}

	private static final String SHOWS_TEXT = "TOOLBAR_SHOWS_TEXT";

	public void setToolbarShowsText(boolean toolbarShowsText) {
		if (toolbarShowsText) {
			putValue(SHOWS_TEXT, Boolean.TRUE);
		} else {
			putValue(SHOWS_TEXT, Boolean.FALSE);
		}
	}

	public boolean getToolbarShowsText() {
		Object value = getValue(SHOWS_TEXT);
		if (value == null) {
			return false;
		}
		return ((Boolean) value);
	}

	private static final String SHOWS_ICON = "MENU_SHOWS_ICON";

	public void setMenuShowsIcon(boolean menuShowsIcon) {
		if (menuShowsIcon) {
			putValue(SHOWS_ICON, Boolean.TRUE);
		} else {
			putValue(SHOWS_ICON, Boolean.FALSE);
		}
	}

	public boolean getMenuShowsIcon() {
		Object value = getValue(SHOWS_ICON);
		if (value == null) {
			return false;
		}
		return ((Boolean) value);
	}

	private static final String ROLES = "ROLES";

	public List getRoles() {
		return (List) getValue(ROLES);
	}

	public void setRoles(List roles) {
		putValue(ROLES, roles);
	}

	protected ImageIcon createIcon(String resourcePath) {
		return createIcon(getClass().getResource(resourcePath));
	}

	protected ImageIcon createIcon(URL imageURL) {
		if (imageURL == null) {
			return null;
		}
		return new ImageIcon(imageURL);
	}

	@Override
	public void putValue(String key, Object value) {
		if (key != null && value != null) {
			if (value instanceof String) {
				value = ((String) value).intern();
			}
		}
		super.putValue(key, value);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(String.valueOf(super.toString())).append(" [id=");
		buf.append(getId());
		buf.append(", enabled=");
		buf.append(isEnabled());
		buf.append(", values={");
		Object[] keys = getKeys();
		if (keys != null) {
			for (Object key : keys) {
				buf.append(key);
				buf.append("->");
				buf.append(getValue(String.valueOf(key)));
				buf.append(";");
			}
		}
		buf.append("}, context=");
		buf.append(getContext());
		buf.append(", enabled delegates=");
		buf.append(this.enabledDelegates);
		buf.append("]");
		return buf.toString();
	}
}
