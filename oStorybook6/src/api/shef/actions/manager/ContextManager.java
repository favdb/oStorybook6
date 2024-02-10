/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions.manager;

import java.util.Map;

public interface ContextManager {

	void setContext(Map paramMap);

	Map getContext();

	void clearContext();

	void putContextValue(Object obj1, Object obj2);

	Object getContextValue(Object obj);
}
