/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.wys;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import api.shef.actions.ActionDefault;

/**
 * @author Bob Tantlinger
 *
 */
public abstract class HTMLTextEditAction extends ActionDefault {

	private static final String TT = "HTMLTextEditAction";

	public static final String EDITOR = "editor";
	public static final int DISABLED = -1, WYSIWYG = 0, SOURCE = 1;
	public WysiwygEditor wysiwygEditor;

	public HTMLTextEditAction(WysiwygEditor editor, String name) {
		super(name);
		this.wysiwygEditor = editor;
		addShouldBeEnabledDelegate((Action a) -> getCurrentEditor() != null);
		updateEnabledState();
	}

	@Override
	public void execute(ActionEvent e) throws Exception {
		wysiwygEditPerformed(e, getCurrentEditor());
	}

	protected JEditorPane getCurrentEditor() {
		JEditorPane ep = wysiwygEditor.getWysEditor();
		return ep;
	}

	@Override
	protected void actionPerformedCatch(Throwable t) {
		t.printStackTrace(System.out);
	}

	@Override
	protected void contextChanged() {
		//LOG.trace(TT + ".updateWysiwygContextState(ed)");
		updateWysiwygContextState(getCurrentEditor());
	}

	protected void updateWysiwygContextState(JEditorPane wysEditor) {
	}

	protected abstract void wysiwygEditPerformed(ActionEvent e, JEditorPane editor);

}
