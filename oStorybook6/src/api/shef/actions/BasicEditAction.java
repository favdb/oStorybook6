/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions;

import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import api.shef.editors.wys.HTMLTextEditAction;
import api.shef.editors.wys.WysiwygEditor;

/**
 * Action suitable for when wysiwyg or source context does not matter.
 *
 * @author Bob Tantlinger
 *
 */
public abstract class BasicEditAction extends HTMLTextEditAction {

	/**
	 * @param editor
	 * @param name
	 */
	public BasicEditAction(WysiwygEditor editor, String name) {
		super(editor, name);
	}

	@Override
	protected final void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		doEdit(e, editor);
	}

	protected abstract void doEdit(ActionEvent e, JEditorPane editor);

	protected void updateContextState(JEditorPane editor) {

	}

	@Override
	protected final void updateWysiwygContextState(JEditorPane editor) {
		updateContextState(editor);
	}

}
