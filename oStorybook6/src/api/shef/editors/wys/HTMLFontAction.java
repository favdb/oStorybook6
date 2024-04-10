/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.wys;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;
import api.shef.actions.CompoundManager;
import api.shef.dialogs.HTMLFontDialog;
import api.shef.tools.HtmlUtils;
import i18n.I18N;

/**
 * Action which edits an HTML font
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLFontAction extends HTMLTextEditAction {

	public HTMLFontAction(WysiwygEditor editor) {
		super(editor, I18N.getMsg("shef.font_"));
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		HTMLDocument doc = (HTMLDocument) editor.getDocument();
		Element chElem = doc.getCharacterElement(editor.getCaretPosition());
		AttributeSet sas = chElem.getAttributes();
		HTMLFontDialog d = createDialog(editor);
		d.setBold(sas.containsAttribute(StyleConstants.Bold, Boolean.TRUE));
		d.setItalic(sas.containsAttribute(StyleConstants.Italic, Boolean.TRUE));
		d.setUnderline(sas.containsAttribute(StyleConstants.Underline, Boolean.TRUE));
		Object o = sas.getAttribute(StyleConstants.FontFamily);
		if (o != null) {
			d.setFontName(o.toString());
		}
		o = sas.getAttribute(StyleConstants.FontSize);
		if (o != null) {
			try {
				d.setFontSize(Integer.parseInt(o.toString()));
			} catch (NumberFormatException ex) {
				ex.printStackTrace(System.err);
			}
		}
		d.setLocationRelativeTo(d.getParent());
		d.setVisible(true);
		if (!d.hasUserCancelled()) {
			MutableAttributeSet tagAttrs = new SimpleAttributeSet();
			tagAttrs.addAttribute(StyleConstants.FontFamily, d.getFontName());
			tagAttrs.addAttribute(StyleConstants.FontSize, d.getFontSize());
			tagAttrs.addAttribute(StyleConstants.Bold, d.isBold());
			tagAttrs.addAttribute(StyleConstants.Italic, d.isItalic());
			tagAttrs.addAttribute(StyleConstants.Underline, d.isUnderline());
			CompoundManager.beginCompoundEdit(editor.getDocument());
			HtmlUtils.setCharacterAttributes(editor, tagAttrs);
			CompoundManager.endCompoundEdit(editor.getDocument());
		}
	}

	private HTMLFontDialog createDialog(JTextComponent ed) {
		Window w = SwingUtilities.getWindowAncestor(ed);
		String t = "";
		if (ed.getSelectedText() != null) {
			t = ed.getSelectedText();
		}
		HTMLFontDialog d = null;
		if (w != null && w instanceof Frame) {
			d = new HTMLFontDialog((Frame) w, t);
		} else if (w != null && w instanceof Dialog) {
			d = new HTMLFontDialog((Dialog) w, t);
		}
		return d;
	}

}
