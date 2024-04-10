/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import api.shef.actions.manager.ActionBasic;
import api.shef.actions.manager.EnabledUpdater;

/**
 * Classe par défaut
 *
 * @author favdb
 */
public class ActionDefault extends ActionBasic implements EnabledUpdater {

	public ActionDefault() {
		this(null);
	}

	public ActionDefault(String id) {
		this(id, null);
	}

	public ActionDefault(String id, Icon icon) {
		this(id, null, null, icon);
	}

	public ActionDefault(String id, Integer mnemonic, KeyStroke accelerator, Icon icon) {
		this(id, id, id, mnemonic, accelerator, icon);
	}

	public ActionDefault(String id, String shortDesc, String longDesc, Integer mnemonic, KeyStroke accelerator, Icon icon) {
		this(id, id, id, shortDesc, longDesc, mnemonic, accelerator, icon);
	}

	public ActionDefault(String id, String actionName, String actionCommandName, String shortDesc,
			String longDesc, Integer mnemonic, KeyStroke accelerator, Icon icon) {
		this(id, actionName, actionCommandName, shortDesc, longDesc,
				mnemonic, accelerator, icon, false, true);
	}

	public ActionDefault(String id, String actionName, String actionCommandName,
			String shortDesc, String longDesc, Integer mnemonic, KeyStroke accelerator,
			Icon icon, boolean toolbarShowsText, boolean menuShowsIcon) {
		super(id, actionName, actionCommandName, shortDesc, longDesc, mnemonic,
				accelerator, icon, toolbarShowsText, menuShowsIcon);
	}

	@Override
	public boolean updateEnabled() {
		updateEnabledState();
		return isEnabled();
	}

	@Override
	public boolean shouldBeEnabled(Action action) {
		return shouldBeEnabled();
	}

	@Override
	protected void actionPerformedCatch(Throwable t) {
		t.printStackTrace(System.out);
	}
}
