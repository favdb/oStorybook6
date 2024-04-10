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
import javax.swing.JMenuItem;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import api.shef.actions.CompoundManager;
import storybook.shortcut.Shortcuts;

/**
 *
 */
public class HTMLLineBreakAction extends HTMLTextEditAction {

	private static final String TT = "HTMLLineBreakAction";

	public HTMLLineBreakAction(WysiwygEditor editor) {
		super(editor, I18N.getMsg("shef.line_break"));
		setSmallIcon(IconUtil.getIconSmall(ICONS.K.HTML_BR));
		setAccelerator(Shortcuts.getKeyStroke("shef", "linebreak"));
		setShortDescription(I18N.getMsg("shef.line_break"));
	}

	int nbfun = 0;

	@Override
	protected void wysiwygEditPerformed(ActionEvent evt, JEditorPane editor) {
		//LOG.trace(TT + ".wysiwygEditPerformed(evt, editor) nbfun=" + nbfun);
		if (nbfun > 0) {
			nbfun = 0;
			return;
		}
		if (evt.getSource() instanceof JMenuItem) {
			nbfun++;
		}
		HTMLDocument document = (HTMLDocument) editor.getDocument();
		CompoundManager.beginCompoundEdit(document);
		int pos = editor.getCaretPosition();
		String elName = document.getParagraphElement(pos).getName();
		HTML.Tag tag = HTML.getTag(elName);
		if (elName.toUpperCase().equals("P-IMPLIED")) {
			tag = HTML.Tag.IMPLIED;
		}
		HTMLEditorKit.InsertHTMLTextAction hta
			= new HTMLEditorKit.InsertHTMLTextAction("insertBR", "<br>", tag, HTML.Tag.BR);
		hta.actionPerformed(evt);
		CompoundManager.endCompoundEdit(document);
	}

}
