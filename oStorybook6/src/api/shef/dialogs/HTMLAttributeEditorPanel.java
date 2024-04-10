/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.JPanel;

/**
 * This abstract class should be subclassed to create any sort of panel that is used to modify html element attributes..
 * e.g an HTML table dialog
 *
 * @author Bob Tantlinger
 *
 */
public abstract class HTMLAttributeEditorPanel extends JPanel {

	protected Map attribs = new HashMap();

	public HTMLAttributeEditorPanel() {
		super();
	}

	public HTMLAttributeEditorPanel(Hashtable attribs) {
		super();
		this.attribs = attribs;
	}

	public void setAttributes(Map attribs) {
		this.attribs = attribs;
		updateComponentsFromAttribs();
	}

	public Map getAttributes() {
		updateAttribsFromComponents();
		return attribs;
	}

	/**
	 * Subclasses should implement this method to set component values to the values in the attribs hashtable.
	 */
	public abstract void updateComponentsFromAttribs();

	/**
	 * Subclasses should implement this method to set values in the attribs hashtable from the states of any components
	 * on the panel.
	 */
	public abstract void updateAttribsFromComponents();
}
