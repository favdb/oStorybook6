/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions;

import javax.swing.Action;
import javax.swing.text.TextAction;

/**
 * @author Bob Tantlinger
 *
 */
public abstract class DecoratedTextAction extends TextAction {

	Action delegate;

	public DecoratedTextAction(String name, Action delegate) {
		super(name);
		this.delegate = delegate;
	}

	public Action getDelegate() {
		return delegate;
	}

}
