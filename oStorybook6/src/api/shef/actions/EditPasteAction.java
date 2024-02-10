/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions;

import i18n.I18N;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLDocument;
import resources.icons.ICONS;
import api.shef.editors.wys.HTMLTextEditAction;
import api.shef.editors.wys.WysiwygEditor;
import storybook.shortcut.Shortcuts;
import storybook.tools.html.Html;

public class EditPasteAction extends HTMLTextEditAction {

	private static final String TT = "EditPasteAction";

	public EditPasteAction(WysiwygEditor editor) {
		super(editor, I18N.getMsg("shef.paste"));
		putValue(MNEMONIC_KEY, I18N.getMnem("shef.paste"));
		setSmallIcon(ICONS.K.EDIT_PASTE);
		setAccelerator(Shortcuts.getKeyStroke("shef", "paste"));
		addShouldBeEnabledDelegate((Action a) -> true);
		setShortDescription(I18N.getMsg("shef.paste"));
		enabled = true;
	}

	@Override
	protected void updateWysiwygContextState(JEditorPane wysEditor) {
		//LOG.trace(TT + ".updateWysiwygContextState(editor)");
		this.updateEnabledState();
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		//LOG.trace(TT + ".wysiwygEditPerformed(e=" + e.toString() + ", editor)");
		HTMLDocument document = (HTMLDocument) editor.getDocument();
		CompoundManager.beginCompoundEdit(document);
		try {
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable content = clip.getContents(this);
			String txt = content.getTransferData(
				new DataFlavor(String.class, "String")).toString();
			txt = Html.htmlToText(txt);
			if (txt == null || txt.isEmpty()) {
				return;
			}
			int caret = editor.getCaretPosition();
			if (caret < 1) {
				return;
			}
			String x = editor.getSelectedText();
			if (x == null || x.isEmpty()) {
				editor.setSelectionStart(caret);
				editor.setSelectionEnd(caret);
			}
			editor.replaceSelection(txt);
		} catch (HeadlessException | UnsupportedFlavorException | IOException ex) {
			//ex.printStackTrace(System.err);
		}
		CompoundManager.endCompoundEdit(document);
	}

}
