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
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import api.shef.actions.CompoundManager;
import api.shef.dialogs.NewTableDialog;
import api.shef.tools.HtmlUtils;
import storybook.shortcut.Shortcuts;

/**
 * Action which shows a dialog to insert an HTML table
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLTableAction extends HTMLTextEditAction {

	public HTMLTableAction(WysiwygEditor editor) {
		super(editor, I18N.getMsg("shef.table_"));
		setSmallIcon(IconUtil.getIconSmall(ICONS.K.TABLE));
		setAccelerator(Shortcuts.getKeyStroke("shef", "table"));
		setShortDescription(Shortcuts.getTooltips("shef", "table"));
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		NewTableDialog dlg = createNewTableDialog(editor);
		if (dlg == null) {
			return;
		}
		dlg.setLocationRelativeTo(dlg.getParent());
		dlg.setVisible(true);
		if (dlg.hasUserCancelled()) {
			return;
		}
		HTMLDocument document = (HTMLDocument) editor.getDocument();
		String html = dlg.getHTML();
		Element elem = document.getParagraphElement(editor.getCaretPosition());
		CompoundManager.beginCompoundEdit(document);
		try {
			if (HtmlUtils.isElementEmpty(elem)) {
				document.setOuterHTML(elem, html);
			} else if (elem.getName().equals("p-implied")) {
				document.insertAfterEnd(elem, html);
			} else {
				HtmlUtils.insertHTML(html, HTML.Tag.TABLE, editor);
			}
		} catch (IOException | BadLocationException ex) {
			ex.printStackTrace(System.err);
		}
		CompoundManager.endCompoundEdit(document);
	}

	/**
	 * Creates the dialog
	 *
	 * @param ed
	 * @return the dialog
	 */
	private NewTableDialog createNewTableDialog(JTextComponent ed) {
		Window w = SwingUtilities.getWindowAncestor(ed);
		NewTableDialog d = null;
		if (w != null && w instanceof Frame) {
			d = new NewTableDialog((Frame) w);
		} else if (w != null && w instanceof Dialog) {
			d = new NewTableDialog((Dialog) w);
		}
		return d;
	}

}
