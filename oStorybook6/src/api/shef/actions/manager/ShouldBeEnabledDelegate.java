/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions.manager;

import java.util.EventListener;
import javax.swing.Action;

public interface ShouldBeEnabledDelegate extends EventListener {

	boolean shouldBeEnabled(Action paramAction);
}
