/*
 * Copyright (C) 2018 FaVdB
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package api.shef.editors.wys;

import i18n.I18N;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTML;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import api.shef.dialogs.HTMLLinkInternalDialog;
import api.shef.tools.HtmlUtils;
import storybook.ui.MainFrame;

/**
 *
 * @author FaVdB
 */
public class HTMLLinkInternalAction extends HTMLTextEditAction {

	private final MainFrame mainFrame;

	public HTMLLinkInternalAction(MainFrame m, WysiwygEditor editor) {
		super(editor, I18N.getMsg("link.internal"));
		mainFrame = m;
		putValue(SMALL_ICON, IconUtil.getIconSmall(ICONS.K.LINKINTERNAL));
		putValue(Action.SHORT_DESCRIPTION, I18N.getMsg("link.internal_desc"));
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		HTMLLinkInternalDialog dlg = createDialog(editor);
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

	protected void sourceEditPerformed(ActionEvent e, JEditorPane editor) {
		// empty
	}

	protected HTMLLinkInternalDialog createDialog(JTextComponent ed) {
		Window w = SwingUtilities.getWindowAncestor(ed);
		HTMLLinkInternalDialog d = null;
		if (w != null) {
			if (w instanceof Frame) {
				d = new HTMLLinkInternalDialog(mainFrame, (Frame) w);
			} else if (w instanceof Dialog) {
				d = new HTMLLinkInternalDialog(mainFrame, (Dialog) w);
			}
		}
		return d;
	}

}
