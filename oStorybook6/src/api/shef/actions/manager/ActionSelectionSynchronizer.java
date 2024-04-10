/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions.manager;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import javax.swing.AbstractButton;
import javax.swing.Action;

/**
 * Clase de synchronisation
 *
 * @author favdb
 */
public class ActionSelectionSynchronizer implements PropertyChangeListener, ItemListener {

	private static final String SELECTED = "SELECTED";

	private WeakReference buttonRef;
	private WeakReference actionRef;

	@SuppressWarnings("unchecked")
	public ActionSelectionSynchronizer(AbstractButton button, Action action) {
		if (button != null) {
			this.buttonRef = new WeakReference(button);
			button.addItemListener(this);
		}
		if (action != null) {
			this.actionRef = new WeakReference(action);
			action.addPropertyChangeListener(this);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (!checkValidity()) {
			return;
		}
		AbstractButton button = (AbstractButton) this.buttonRef.get();
		Action action = (Action) this.actionRef.get();
		boolean buttonSelected = button.isSelected();
		Object value = action.getValue(SELECTED);
		boolean changeNeeded = true;
		if (value instanceof Boolean) {
			boolean isActionSelected = ((Boolean) value);
			if (e.getStateChange() == 1 && isActionSelected) {
				changeNeeded = false;
			} else if (e.getStateChange() == 2 && !isActionSelected) {
				changeNeeded = false;
			}
		}
		if (changeNeeded) {
			action.putValue(SELECTED, buttonSelected);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!checkValidity()) {
			return;
		}
		String propertyName = evt.getPropertyName();
		if (propertyName.equals(SELECTED)) {
			if (!checkValidity()) {
				return;
			}
			AbstractButton button = (AbstractButton) this.buttonRef.get();
			Action action = (Action) this.actionRef.get();
			boolean buttonSelected = button.isSelected();
			Object value = action.getValue(SELECTED);
			if (value instanceof Boolean) {
				boolean actionSelected = ((Boolean) value);
				if (actionSelected != buttonSelected) {
					button.setSelected(actionSelected);
				}
			}
		}
	}

	private boolean checkValidity() {
		if (this.buttonRef == null || this.actionRef == null
				|| this.buttonRef.get() == null || this.actionRef.get() == null) {
			removeListeners();
			return false;
		}
		return true;
	}

	private void removeListeners() {
		Action action = (Action) this.actionRef.get();
		if (action != null) {
			action.removePropertyChangeListener(this);
		}
		this.actionRef = null;
		AbstractButton button = (AbstractButton) this.buttonRef.get();
		if (button != null) {
			button.removeItemListener(this);
		}
		this.buttonRef = null;
	}
}
