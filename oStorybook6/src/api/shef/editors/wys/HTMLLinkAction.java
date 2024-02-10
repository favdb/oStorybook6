/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.wys;

import i18n.I18N;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTML;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import api.shef.dialogs.HyperlinkDialog;
import api.shef.tools.HtmlUtils;
import storybook.shortcut.Shortcuts;

/**
 * Action which displays a dialog to insert a hyperlink
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLLinkAction extends HTMLTextEditAction {

	public HTMLLinkAction(WysiwygEditor editor) {
		super(editor, I18N.getMsg("shef.hyperlink_"));
		setSmallIcon(IconUtil.getIconSmall(ICONS.K.LINK));
		setAccelerator(Shortcuts.getKeyStroke("shef", "hyperlink"));
		setShortDescription(Shortcuts.getTooltips("shef", "hyperlink"));
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		HyperlinkDialog dlg = createDialog(editor);
		if (dlg == null) {
			return;
		}
		if (editor.getSelectedText() != null) {
			dlg.setLinkText(editor.getSelectedText());
		}
		dlg.setLocationRelativeTo(dlg.getParent());
		dlg.setVisible(true);
		if (dlg.hasUserCancelled()) {
			return;
		}
		String tagText = dlg.getHTML();
		if (editor.getSelectedText() == null) {
			tagText += "&nbsp;";
		}
		editor.replaceSelection("");
		HtmlUtils.insertHTML(tagText, HTML.Tag.A, editor);
	}

	protected HyperlinkDialog createDialog(JTextComponent ed) {
		Window w = SwingUtilities.getWindowAncestor(ed);
		HyperlinkDialog d = null;
		if (w != null && w instanceof Frame) {
			d = new HyperlinkDialog((Frame) w);
		} else if (w != null && w instanceof Dialog) {
			d = new HyperlinkDialog((Dialog) w);
		}
		return d;
	}

}
