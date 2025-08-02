/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions;

import api.shef.editors.wys.HTMLTextEditAction;
import api.shef.editors.wys.WysiwygEditor;
import api.shef.tools.LOG;
import i18n.I18N;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.shortcut.Shortcuts;
import storybook.tools.html.Html;

/**
 * @author Bob Tantlinger
 *
 */
public class EditPasteFormattedAction extends HTMLTextEditAction {

	private static final String TT = "EditPasteFormattedAction.";

	/**
	 * @param editor
	 */
	public EditPasteFormattedAction(WysiwygEditor editor) {
		super(editor, I18N.getMsg("shef.paste_formatted"));
		putValue(MNEMONIC_KEY, I18N.getMnem("shef.paste_formatted"));
		putValue(SMALL_ICON, IconUtil.getIconSmall(ICONS.K.EDIT_PASTE));
		putValue(ACCELERATOR_KEY, Shortcuts.getKeyStroke("shef", "paste_formatted"));
		addShouldBeEnabledDelegate((Action a) -> {
			if (getCurrentEditor() == null) {
				return false;
			}
			Transferable content
					= Toolkit.getDefaultToolkit().getSystemClipboard()
							.getContents(EditPasteFormattedAction.this);
			if (content == null) {
				return false;
			}
			DataFlavor flv = DataFlavor.selectBestTextFlavor(content.getTransferDataFlavors());
			return flv != null && (flv.getMimeType().contains("text/html")
					|| flv.getMimeType().contains("String"));
		});
		putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
		enabled = true;
	}

	@Override
	protected void updateWysiwygContextState(JEditorPane wysEditor) {
		this.updateEnabledState();
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		//LOG.trace(TT + "wysiwygEditPerformed(e=" + e.toString() + ", editor)");
		String html = getHTMLFragment(editor);
		if (html != null && !html.isEmpty()) {
			CompoundManager.beginCompoundEdit(editor.getDocument());
			HTMLDocument doc = (HTMLDocument) editor.getDocument();
			HTMLEditorKit ekit = (HTMLEditorKit) editor.getEditorKit();
			try {
				ekit.insertHTML(doc, editor.getSelectionStart(), html, 0, 0, null);
			} catch (BadLocationException | IOException ex) {
				LOG.err("EditPasteFormattedAction exception", ex);
			}
			CompoundManager.endCompoundEdit(editor.getDocument());
		}
	}

	/**
	 * Get the HTML text from the content if any
	 *
	 * @return returns the html fragment, or null if this content isn't HTML
	 * @throws UnsupportedFlavorException
	 * @throws IOException
	 */
	private String getHTMLFragment(JEditorPane editor) {
		//LOG.trace(TT+"getFragment(editor)");
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable trans = clip.getContents(this);
		if (trans == null) {
			return null;
		}
		DataFlavor flavor = DataFlavor.selectBestTextFlavor(trans.getTransferDataFlavors());
		String html = null;
		if (flavor != null) {
			String flvType = flavor.getMimeType();
			if (flvType.contains("text/html") || flvType.contains("String")) {
				html = getHtml(trans, flavor);
				DataFlavor[] flavors = trans.getTransferDataFlavors();
				if (flavors.length > 1) {
					html = selectFlavor(editor, html, flavors);
				}
			}
		}
		return html;
	}

	private String getHtml(Transferable transferable, DataFlavor flv) {
		StringBuilder b = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(flv.getReaderForText(transferable))) {
			int ch;
			while ((ch = reader.read()) != -1) {
				b.append((char) ch);
			}
		} catch (IOException | UnsupportedFlavorException ex) {
		}
		return b.toString();
	}

	/**
	 * select the paste mode
	 *
	 * @param html
	 * @param flavors
	 * @return
	 */
	private String selectFlavor(JEditorPane editor, String html, DataFlavor[] flavors) {
		//LOG.trace(TT+"selectFlavor(...)");
		String choices[] = {
			I18N.getMsg("paste.as_text"),
			I18N.getMsg("paste.as_html")
		};
		int n = JOptionPane.showOptionDialog(
				SwingUtilities.getWindowAncestor(editor),
				I18N.getMsg("paste.as_prompt"),
				I18N.getMsg("paste.as_title"),
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				choices,
				choices[1]);
		switch (n) {
			case 0:
				return Html.htmlToText(html, enabled);
			case 1:
				if (!html.matches(".*\\<[^>]+>.*")) {
					html = Html.intoP(html.replace("\n\n", "</p><p>")).replace("<p></p>", "");
				}
				return html;
			default:
				return "";
		}
	}
}
