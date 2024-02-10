/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import java.awt.Dialog;
import java.awt.Frame;
import javax.swing.Icon;

/**
 * An abstract OptionDialog for HTML editor dialog boxes.
 *
 * Subclasses should implement dialogs for inserting HTML elements such as tables, links, images, etc.
 *
 * @author Bob Tantlinger
 *
 */
public abstract class HTMLOptionDialog extends OptionDialog {

	HTMLOptionDialog(Frame parent, String title, String desc, Icon ico) {
		super(parent, title, desc, ico);
	}

	HTMLOptionDialog(Dialog parent, String title, String desc, Icon ico) {
		super(parent, title, desc, ico);
	}

	/**
	 * Gets the generated HTML from the dialog
	 *
	 * @return the HTML
	 */
	public abstract String getHTML();
}
