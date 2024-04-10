/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.wys;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import i18n.I18N;

/**
 * Action which inserts a horizontal rule
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLHorizontalRuleAction extends HTMLTextEditAction {

	public HTMLHorizontalRuleAction(WysiwygEditor editor) {
		super(editor, I18N.getMsg("shef.horizontal_rule"));
		putValue(MNEMONIC_KEY, I18N.getMnem("shef.horizontal_rule"));
		putValue(SMALL_ICON, IconUtil.getIconSmall(ICONS.K.HTML_HR));
		putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		HTMLDocument document = (HTMLDocument) editor.getDocument();
		int caret = editor.getCaretPosition();
		Element elem = document.getParagraphElement(caret);

		HTML.Tag tag = HTML.getTag(elem.getName());
		if (elem.getName().equals("p-implied")) {
			tag = HTML.Tag.IMPLIED;
		}
		HTMLEditorKit.InsertHTMLTextAction a
				= new HTMLEditorKit.InsertHTMLTextAction("", "<hr>", tag, HTML.Tag.HR);
		a.actionPerformed(e);
	}

}
