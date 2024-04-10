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
import resources.icons.ICONS;
import api.shef.editors.wys.WysiwygEditor;
import storybook.shortcut.Shortcuts;

/**
 * @author Bob Tantlinger
 *
 */
public class EditCopyAction extends BasicEditAction {

	private static final String TT = "EditCopyAction";

	public EditCopyAction(WysiwygEditor editor) {
		super(editor, "");
		putValue(Action.NAME, I18N.getMsg("shef.copy"));
		setSmallIcon(ICONS.K.EDIT_COPY);
		setAccelerator(Shortcuts.getKeyStroke("shef", "copy"));
		setMnemonic("copy");
		addShouldBeEnabledDelegate((Action a) -> {
			JEditorPane ed = getCurrentEditor();
			return ed != null && ed.getSelectionStart() != ed.getSelectionEnd();
		});
		setShortDescription(I18N.getMsg("shef.copy_desc"));
	}

	@Override
	protected void doEdit(ActionEvent e, JEditorPane editor) {
		//LOG.trace(TT + ".doEdit(e=" + e.toString() + ", editor)");
		editor.copy();
	}

	@Override
	protected void contextChanged() {
		super.contextChanged();
		this.updateEnabledState();
	}

}
