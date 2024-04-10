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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import resources.icons.ICONS;
import api.shef.actions.CompoundManager;
import storybook.shortcut.Shortcuts;
import storybook.tools.html.Html;

public class HTMLEditPasteAction extends HTMLTextEditAction {

	private static final String TT = "HTMLEditPasteAction";

	public HTMLEditPasteAction(WysiwygEditor editor) {
		super(editor, I18N.getMsg("shef.paste"));
		putValue(MNEMONIC_KEY, I18N.getMnem("shef.paste"));
		setSmallIcon(ICONS.K.EDIT_PASTE);
		setAccelerator(Shortcuts.getKeyStroke("shef", "paste"));
		addShouldBeEnabledDelegate((Action a) -> true);
		setShortDescription(Shortcuts.getTooltips("shef", "paste"));
	}

	@Override
	protected void updateWysiwygContextState(JEditorPane wysEditor) {
		//LOG.trace(TT + ".updateWysiwygContextState(editor)");
		this.updateEnabledState();
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		//LOG.trace(TT + ".wysiwygEditPerformed(e=" + e.toString() + ", editor)");
		HTMLEditorKit ekit = (HTMLEditorKit) editor.getEditorKit();
		HTMLDocument document = (HTMLDocument) editor.getDocument();
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		try {
			CompoundManager.beginCompoundEdit(document);
			Transferable content = clip.getContents(this);
			String txt = content.getTransferData(
				new DataFlavor(String.class, "String")).toString();
			txt = Html.htmlToText(txt);
			if (txt == null || txt.isEmpty()) {
				return;
			}
			int start = editor.getSelectionStart(), end = editor.getSelectionEnd();
			if (start < 0) {
				start = 0;
			}
			if (end < 0) {
				end = 0;
			}
			if (start == end) {
				document.insertString(start, txt, null);
			} else {
				document.replace(start, end - start, txt, ekit.getInputAttributes());
			}
		} catch (UnsupportedFlavorException | IOException | BadLocationException ex) {
		} finally {
			CompoundManager.endCompoundEdit(document);
		}
	}

}
