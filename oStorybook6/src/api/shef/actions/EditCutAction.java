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
public class EditCutAction extends BasicEditAction {

    public EditCutAction(WysiwygEditor editor) {
	super(editor, "cut");
	putValue(Action.NAME, I18N.getMsg("shef.cut"));
	setSmallIcon(ICONS.K.EDIT_CUT);
	setAccelerator(Shortcuts.getKeyStroke("shef", "cut"));
	setMnemonic("cut");
	addShouldBeEnabledDelegate((Action a) -> {
	    JEditorPane ed = getCurrentEditor();
	    return ed != null && ed.getSelectionStart() != ed.getSelectionEnd();
	});
	putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
    }

    @Override
    protected void doEdit(ActionEvent e, JEditorPane editor) {
	editor.cut();
    }

    @Override
    protected void contextChanged() {
	super.contextChanged();
	this.updateEnabledState();
    }

}
