/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.wys;

import i18n.I18N;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import resources.icons.ICONS;
import api.shef.actions.CompoundManager;
import api.shef.tools.HtmlUtils;
import storybook.shortcut.Shortcuts;

/**
 * Action which aligns HTML elements
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLAlignAction extends HTMLTextEditAction {

	public static final int LEFT = 0;
	public static final int CENTER = 1;
	public static final int RIGHT = 2;
	public static final int JUSTIFY = 3;
	public static final String ALIGNMENT_NAMES[] = {
		I18N.getMsg("shef.al_left"),
		I18N.getMsg("shef.al_center"),
		I18N.getMsg("shef.al_right"),
		I18N.getMsg("shef.al_justify")
	};
	public static final ICONS.K ALIGNMENT_ICONS[] = {
		ICONS.K.AL_LEFT,
		ICONS.K.AL_CENTER,
		ICONS.K.AL_RIGHT,
		ICONS.K.AL_JUSTIFY
	};
	private static final char[] MNEMS = {
		I18N.getMsg("shef.al_left.mnemonic").charAt(0),
		I18N.getMsg("shef.al_center.mnemonic").charAt(0),
		I18N.getMsg("shef.al_right.mnemonic").charAt(0),
		I18N.getMsg("shef.al_justify.mnemonic").charAt(0)
	};
	public static final String ALIGNMENTS[] = {"left", "center", "right", "justify"};
	private int align;

	/**
	 * Creates a new HTMLAlignAction
	 *
	 * @param editor
	 * @param al LEFT, RIGHT, CENTER, or JUSTIFY
	 * @throws IllegalArgumentException
	 */
	public HTMLAlignAction(WysiwygEditor editor, int al) throws IllegalArgumentException {
		super(editor, ALIGNMENT_NAMES[al]);
		if (al < 0 || al >= ALIGNMENTS.length) {
			throw new IllegalArgumentException("Illegal Argument");
		}
		putValue(NAME, ALIGNMENT_NAMES[al]);
		setShortDescription(Shortcuts.getTooltips("shef", "al_" + ALIGNMENTS[al]));
		setAccelerator(Shortcuts.getKeyStroke("shef", "al_" + ALIGNMENTS[al]));
		setSmallIcon(ALIGNMENT_ICONS[al]);
		align = al;
	}

	@Override
	protected void updateWysiwygContextState(JEditorPane ed) {
		setSelected(shouldBeSelected(ed));
	}

	private boolean shouldBeSelected(JEditorPane ed) {
		HTMLDocument document = (HTMLDocument) ed.getDocument();
		Element elem = document.getParagraphElement(ed.getCaretPosition());
		if (HtmlUtils.isImplied(elem)) {
			elem = elem.getParentElement();
		}
		AttributeSet at = elem.getAttributes();
		return at.containsAttribute(HTML.Attribute.ALIGN, ALIGNMENTS[align]);
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		HTMLDocument doc = (HTMLDocument) editor.getDocument();
		Element curE = doc.getParagraphElement(editor.getSelectionStart());
		Element endE = doc.getParagraphElement(editor.getSelectionEnd());
		CompoundManager.beginCompoundEdit(doc);
		while (true) {
			alignElement(curE);
			if (curE.getEndOffset() >= endE.getEndOffset()
				|| curE.getEndOffset() >= doc.getLength()) {
				break;
			}
			curE = doc.getParagraphElement(curE.getEndOffset() + 1);
		}
		CompoundManager.endCompoundEdit(doc);
	}

	private void alignElement(Element elem) {
		HTMLDocument doc = (HTMLDocument) elem.getDocument();
		if (HtmlUtils.isImplied(elem)) {
			HTML.Tag tag = HTML.getTag(elem.getParentElement().getName());
			//pre tag doesn't support an align attribute
			//http://www.w3.org/TR/REC-html32#pre
			if (tag != null && (!tag.equals(HTML.Tag.BODY))
				&& (!tag.isPreformatted() && !tag.equals(HTML.Tag.DD))) {
				SimpleAttributeSet as = new SimpleAttributeSet(elem.getAttributes());
				as.removeAttribute("align");
				as.addAttribute("align", ALIGNMENTS[align]);
				Element parent = elem.getParentElement();
				String html = HtmlUtils.getElementHTML(elem, false);
				html = HtmlUtils.createTag(tag, as, html);
				String snipet = "";
				for (int i = 0; i < parent.getElementCount(); i++) {
					Element el = parent.getElement(i);
					if (el == elem) {
						snipet += html;
					} else {
						snipet += HtmlUtils.getElementHTML(el, true);
					}
				}
				try {
					doc.setOuterHTML(parent, snipet);
				} catch (IOException | BadLocationException ex) {
					ex.printStackTrace(System.err);
				}
			}
		} else {
			//Set the HTML attribute on the paragraph...
			MutableAttributeSet set = new SimpleAttributeSet(elem.getAttributes());
			set.removeAttribute(HTML.Attribute.ALIGN);
			set.addAttribute(HTML.Attribute.ALIGN, ALIGNMENTS[align]);
			//Set the paragraph attributes...
			int start = elem.getStartOffset();
			int length = elem.getEndOffset() - elem.getStartOffset();
			doc.setParagraphAttributes(start, length - 1, set, true);
		}
	}

}
