/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import api.shef.tools.HtmlUtils;
import api.shef.tools.LOG;

/**
 * Tab action for tabbing between table cells
 *
 * @author Bob Tantlinger
 *
 */
public class TabAction extends DecoratedTextAction {

	public static final int FORWARD = 0;
	public static final int BACKWARD = 1;
	private int type;

	public TabAction(int type, Action defaultTabAction) {
		super("tabAction", defaultTabAction);
		//delegate = defaultTabAction;
		this.type = type;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JEditorPane editor;
		HTMLDocument document;
		editor = (JEditorPane) getTextComponent(e);
		document = (HTMLDocument) editor.getDocument();
		Element elem = document.getParagraphElement(editor.getCaretPosition());
		Element tdElem = HtmlUtils.getParent(elem, HTML.Tag.TD);
		if (tdElem != null) {
			try {
				if (type == FORWARD) {
					editor.setCaretPosition(tdElem.getEndOffset());
				} else {
					editor.setCaretPosition(tdElem.getStartOffset() - 1);
				}
			} catch (IllegalArgumentException ex) {
				LOG.err("", ex);
			}
		} else {
			delegate.actionPerformed(e);
		}
	}

}
