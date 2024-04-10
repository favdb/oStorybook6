/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.wys;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import api.shef.dialogs.UnicodeDialog;
import i18n.I18N;

public class HTMLUnicodeAction extends HTMLTextEditAction {

	UnicodeDialog dialog;

	public HTMLUnicodeAction(WysiwygEditor editor) {
		super(editor, I18N.getMsg("shef.unicode"));
		setSmallIcon(IconUtil.getIconSmall(ICONS.K.CHAR_UNICODE));
		setShortDescription(I18N.getMsg("shef.unicode_desc"));
	}

	protected void doEdit(ActionEvent e, JEditorPane ed) {
		Component c = SwingUtilities.getWindowAncestor(ed);
		if (dialog == null) {
			if (c instanceof Frame) {
				dialog = new UnicodeDialog((Frame) c, ed);
			} else if (c instanceof Dialog) {
				dialog = new UnicodeDialog((Dialog) c, ed);
			} else {
				return;
			}
		}
		dialog.setInsertEntity(false);
		if (!dialog.isVisible()) {
			dialog.setLocationRelativeTo(c);
			dialog.setVisible(true);
		}
	}

	protected void updateContextState(JEditorPane editor) {
		if (dialog != null) {
			dialog.setInsertEntity(false);
			dialog.setJTextComponent(editor);
		}
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		doEdit(e, editor);
	}

	@Override
	protected final void updateWysiwygContextState(JEditorPane wysEditor) {
		updateContextState(wysEditor);
	}

}
