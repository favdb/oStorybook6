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
import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;
import api.shef.actions.CompoundManager;
import api.shef.tools.HtmlUtils;

/**
 * Action which edits an HTML font
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLFontFamilyAction extends HTMLTextEditAction {

	String family = "";

	public HTMLFontFamilyAction(WysiwygEditor editor, String family) {
		super(editor, I18N.getMsg("shef.font_"));
		this.family = family;
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		HTMLDocument doc = (HTMLDocument) editor.getDocument();
		Element chElem = doc.getCharacterElement(editor.getCaretPosition());
		AttributeSet sas = chElem.getAttributes();
		Object o = sas.getAttribute(StyleConstants.FontFamily);
		MutableAttributeSet tagAttrs = new SimpleAttributeSet();
		tagAttrs.addAttribute(StyleConstants.FontFamily, this.family);
		CompoundManager.beginCompoundEdit(editor.getDocument());
		HtmlUtils.setCharacterAttributes(editor, tagAttrs);
		CompoundManager.endCompoundEdit(editor.getDocument());
	}

}
