/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions;

import i18n.I18N;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import resources.icons.ICONS;
import api.shef.dialogs.TextFinderDialog;
import api.shef.editors.wys.WysiwygEditor;
import storybook.shortcut.Shortcuts;

public class FindReplaceAction extends BasicEditAction {

	private boolean isReplaceTab;
	private TextFinderDialog dialog;

	public FindReplaceAction(WysiwygEditor editor, boolean isReplace) {
		super(editor, null);
		if (isReplace) {
			putValue(NAME, I18N.getMsg("shef.replace_"));
			putValue(ACCELERATOR_KEY, Shortcuts.getKeyStroke("shef", "replace"));
		} else {
			putValue(NAME, I18N.getMsg("shef.find_"));
			setSmallIcon(ICONS.K.FIND);
			setAccelerator(Shortcuts.getKeyStroke("shef", "find"));
			setShortDescription(Shortcuts.getTooltips("shef", "find"));
		}
		this.isReplaceTab = isReplace;
	}

	@Override
	protected void doEdit(ActionEvent e, JEditorPane textComponent) {
		Component c = SwingUtilities.getWindowAncestor(textComponent);
		if (dialog == null) {
			if (c instanceof Frame) {
				if (isReplaceTab) {
					dialog = new TextFinderDialog((Frame) c, textComponent, TextFinderDialog.REPLACE);
				} else {
					dialog = new TextFinderDialog((Frame) c, textComponent, TextFinderDialog.FIND);
				}
			} else if (c instanceof Dialog) {
				if (isReplaceTab) {
					dialog = new TextFinderDialog((Dialog) c, textComponent, TextFinderDialog.REPLACE);
				} else {
					dialog = new TextFinderDialog((Dialog) c, textComponent, TextFinderDialog.FIND);
				}
			} else {
				return;
			}
		}
		if (!dialog.isVisible()) {
			dialog.show((isReplaceTab) ? TextFinderDialog.REPLACE : TextFinderDialog.FIND);
		}
	}

	@Override
	protected void updateContextState(JEditorPane editor) {
		if (dialog != null) {
			dialog.setJTextComponent(editor);
		}
	}

}
