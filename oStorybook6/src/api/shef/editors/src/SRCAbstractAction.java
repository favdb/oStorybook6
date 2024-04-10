/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.src;

import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import api.shef.actions.ActionDefault;

/**
 *
 * @author favdb
 */
public abstract class SRCAbstractAction extends ActionDefault {

	public static final String EDITOR = "editor";
	public static final int DISABLED = -1;
	public static final int SOURCE = 1;
	public SourceEditor parent;

	public SRCAbstractAction(SourceEditor editor, String name) {
		super(name);
		this.parent = editor;
		updateEnabledState();
	}

	protected JTextComponent getCurrentEditor() {
		try {
			JTextComponent ep = parent.srcEditor;
			return ep;
		} catch (ClassCastException cce) {
		}

		return null;
	}

	@Override
	public void execute(ActionEvent e) throws Exception {
		sourceEditPerformed(e, getCurrentEditor());
	}

	@Override
	protected void actionPerformedCatch(Throwable t) {
		t.printStackTrace(System.out);
	}

	@Override
	protected void contextChanged() {
		updateSourceContextState(getCurrentEditor());
	}

	protected void updateSourceContextState(JTextComponent srcEditor) {
	}

	protected abstract void sourceEditPerformed(ActionEvent e, JTextComponent editor);

}
