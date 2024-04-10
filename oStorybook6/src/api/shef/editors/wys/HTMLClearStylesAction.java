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
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import storybook.shortcut.Shortcuts;

/**
 * Action which clears inline text styles
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLClearStylesAction extends HTMLTextEditAction {

	public HTMLClearStylesAction(WysiwygEditor editor) {
		super(editor, I18N.getMsg("shef.clear_styles"));
		//setMnemonic("clear_styles");
		setAccelerator(Shortcuts.getKeyStroke("shef", "clear_styles"));
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		HTMLDocument document = (HTMLDocument) editor.getDocument();
		HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
		MutableAttributeSet attrs = new SimpleAttributeSet();
		attrs.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
		int selStart = editor.getSelectionStart();
		int selEnd = editor.getSelectionEnd();
		if (selEnd > selStart) {
			document.setCharacterAttributes(selStart, selEnd - selStart, attrs, true);
		}
		kit.getInputAttributes().removeAttributes(kit.getInputAttributes());
		kit.getInputAttributes().addAttributes(attrs);
	}

}
