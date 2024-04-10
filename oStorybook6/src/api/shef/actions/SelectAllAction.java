/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions;

import i18n.I18N;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import api.shef.editors.wys.WysiwygEditor;
import storybook.shortcut.Shortcuts;

/**
 * Select all action
 *
 * @author Bob
 */
public class SelectAllAction extends BasicEditAction {

	private static final long serialVersionUID = 1L;

	public SelectAllAction(WysiwygEditor editor) {
		super(editor, I18N.getMsg("shef.select_all"));
		//putValue(MNEMONIC_KEY, I18N.getMnem("shef.select_all"));
		putValue(ACCELERATOR_KEY, Shortcuts.getKeyStroke("shef", "select_all"));
		putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
	}

	@Override
	protected void doEdit(ActionEvent e, JEditorPane editor) {
		editor.selectAll();
	}
}
