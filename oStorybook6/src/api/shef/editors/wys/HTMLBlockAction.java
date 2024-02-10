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
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import resources.icons.ICONS;
import api.shef.actions.CompoundManager;
import api.shef.actions.manager.ActionList;
import api.shef.tools.ElementWriter;
import api.shef.tools.HtmlUtils;
import storybook.shortcut.Shortcuts;

/**
 * Action which formats HTML block level elements
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLBlockAction extends HTMLTextEditAction {

	private static final String TT = "HTMLBlockAction";
	public static final int DIV = 0,
		P = 1,
		H1 = 2,
		H2 = 3,
		H3 = 4,
		H4 = 5,
		H5 = 6,
		H6 = 7,
		PRE = 8,
		BLOCKQUOTE = 9,
		OL = 10,
		UL = 11;
	private static final int KEYS[] = {
		KeyEvent.VK_D, KeyEvent.VK_ENTER,
		KeyEvent.VK_NUMPAD1, KeyEvent.VK_NUMPAD2,
		KeyEvent.VK_NUMPAD3, KeyEvent.VK_NUMPAD4,
		KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD6,
		KeyEvent.VK_R, KeyEvent.VK_Q, KeyEvent.VK_N, KeyEvent.VK_U
	};
	public static final String[] TYPES = {
		"body_text",
		"paragraph",
		"heading1",
		"heading2",
		"heading3",
		"heading4",
		"heading5",
		"heading6",
		"preformatted",
		"blockquote",
		"listordered",
		"listunordered"
	};
	private int type;

	public static ActionList createBlockElementActionList(WysiwygEditor editor) {
		//LOG.trace(TT + ".createBlockElementActionList(editor)");
		ActionList list = new ActionList("paragraph");
		list.add(new HTMLBlockAction(editor, HTMLBlockAction.P));
		list.add(null);
		list.add(new HTMLBlockAction(editor, HTMLBlockAction.BLOCKQUOTE));
		list.add(new HTMLBlockAction(editor, HTMLBlockAction.PRE));
		list.add(null);
		list.add(new HTMLBlockAction(editor, HTMLBlockAction.H1));
		list.add(new HTMLBlockAction(editor, HTMLBlockAction.H2));
		list.add(new HTMLBlockAction(editor, HTMLBlockAction.H3));
		list.add(new HTMLBlockAction(editor, HTMLBlockAction.H4));
		list.add(new HTMLBlockAction(editor, HTMLBlockAction.H5));
		list.add(new HTMLBlockAction(editor, HTMLBlockAction.H6));
		return list;
	}

	/**
	 * Creates a new HTMLBlockAction
	 *
	 * @param editor
	 * @param type A block type - P, PRE, BLOCKQUOTE, H1, H2, etc
	 *
	 * @throws IllegalArgumentException
	 */
	public HTMLBlockAction(WysiwygEditor editor, int type) throws IllegalArgumentException {
		super(editor, "");
		if (type < 0 || type >= TYPES.length) {
			throw new IllegalArgumentException("Illegal argument");
		}
		this.type = type;
		String typeId = TYPES[type];
		if (typeId.startsWith("heading")) {
			putValue(NAME, I18N.getMsg("shef.heading") + " " + (type - 1));
		} else {
			putValue(NAME, I18N.getMsg("shef." + typeId));
		}
		setShortDescription(Shortcuts.getTooltips("shef", typeId));
		if (typeId.startsWith("heading")) {
			setAccelerator(Shortcuts.getKeyStroke("shef", typeId));
		} else {
			setAccelerator(Shortcuts.getKeyStroke("shef", typeId));
		}
		setMnemonic(I18N.getMnem("shef." + typeId));
		if (type == OL) {
			setSmallIcon(ICONS.K.LIST_ORDERED);
		}
		if (type == UL) {
			setSmallIcon(ICONS.K.LIST_UNORDERED);
		}
	}

	@Override
	protected void updateWysiwygContextState(JEditorPane ed) {
		HTMLDocument document = (HTMLDocument) ed.getDocument();
		Element elem = document.getParagraphElement(ed.getCaretPosition());
		String elemName = elem.getName();
		if (elemName.equals("p-implied")) {
			elemName = elem.getParentElement().getName();
		}
		if (type == DIV && ((elemName.equals("div") || elemName.equals("body") || elemName.equals("td")))) {
			setSelected(true);
		} else if (type == UL) {
			Element listElem = HtmlUtils.getListParent(elem);
			setSelected(listElem != null && (listElem.getName().equals("ul")));
		} else if (type == OL) {
			Element listElem = HtmlUtils.getListParent(elem);
			setSelected(listElem != null && (listElem.getName().equals("ol")));
		} else if (elemName.equals(getTag().toString().toLowerCase())) {
			setSelected(true);
		} else {
			setSelected(false);
		}
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		HTMLDocument document = (HTMLDocument) editor.getDocument();
		int caret = editor.getCaretPosition();
		CompoundManager.beginCompoundEdit(document);
		try {
			if (type == OL || type == UL) {
				insertList(editor, e);
			} else {
				changeBlockType(editor, e);
			}
			editor.setCaretPosition(caret);
		} catch (BadLocationException ex) {
			ex.printStackTrace(System.err);
		}
		CompoundManager.endCompoundEdit(document);
	}

	private HTML.Tag getRootTag(Element elem) {
		HTML.Tag root = HTML.Tag.BODY;
		if (HtmlUtils.getParent(elem, HTML.Tag.TD) != null) {
			root = HTML.Tag.TD;
		}
		return root;
	}

	private void insertHTML(String html, HTML.Tag tag, HTML.Tag root, ActionEvent e) {
		HTMLEditorKit.InsertHTMLTextAction a
			= new HTMLEditorKit.InsertHTMLTextAction("insertHTML", html, root, tag);
		a.actionPerformed(e);
	}

	private void changeListType(Element listParent, HTML.Tag replaceTag, HTMLDocument document) {
		StringWriter out = new StringWriter();
		ElementWriter w = new ElementWriter(out, listParent);
		try {
			w.write();
			String html = out.toString();
			html = html.substring(html.indexOf('>') + 1, html.length());
			html = html.substring(0, html.lastIndexOf('<'));
			html = '<' + replaceTag.toString() + '>' + html + "</" + replaceTag.toString() + '>';
			document.setOuterHTML(listParent, html);
		} catch (IOException | BadLocationException ex) {
		}
	}

	private void insertList(JEditorPane editor, ActionEvent e) throws BadLocationException {
		HTMLDocument document = (HTMLDocument) editor.getDocument();
		int caretPos = editor.getCaretPosition();
		Element elem = document.getParagraphElement(caretPos);
		HTML.Tag parentTag = HTML.getTag(elem.getParentElement().getName());
		//check if we need to change the list from one type to another
		Element listParent = elem.getParentElement().getParentElement();
		HTML.Tag listTag = HTML.getTag(listParent.getName());
		if (listTag.equals(HTML.Tag.UL) || listTag.equals(HTML.Tag.OL)) {
			HTML.Tag t = HTML.getTag(listParent.getName());
			if (type == OL && t.equals(HTML.Tag.UL)) {
				changeListType(listParent, HTML.Tag.OL, document);
				return;
			} else if (type == UL && listTag.equals(HTML.Tag.OL)) {
				changeListType(listParent, HTML.Tag.UL, document);
				return;
			}
		}
		if (!parentTag.equals(HTML.Tag.LI)) {
			//don't allow nested lists
			changeBlockType(editor, e);
		} else {
			//is already a list, so turn off list
			HTML.Tag root = getRootTag(elem);
			String txt = HtmlUtils.getElementHTML(elem, false);
			editor.setCaretPosition(elem.getEndOffset());
			insertHTML("<p>" + txt + "</p>", HTML.Tag.P, root, e);
			HtmlUtils.removeElement(elem);
		}
	}

	@SuppressWarnings("unchecked")
	private void changeBlockType(JEditorPane editor, ActionEvent e) throws BadLocationException {
		HTMLDocument doc = (HTMLDocument) editor.getDocument();
		Element curE = doc.getParagraphElement(editor.getSelectionStart());
		Element endE = doc.getParagraphElement(editor.getSelectionEnd());
		Element curTD = HtmlUtils.getParent(curE, HTML.Tag.TD);
		HTML.Tag tag = getTag();
		HTML.Tag rootTag = getRootTag(curE);
		String html = "";

		if (isListType()) {
			html = "<" + tag + ">";
			tag = HTML.Tag.LI;
		}
		//a list to hold the elements we want to change
		List elToRemove = new ArrayList();
		elToRemove.add(curE);
		while (true) {
			html += HtmlUtils.createTag(tag, curE.getAttributes(), HtmlUtils.getElementHTML(curE, false));
			if (curE.getEndOffset() >= endE.getEndOffset() || curE.getEndOffset() >= doc.getLength()) {
				break;
			}
			curE = doc.getParagraphElement(curE.getEndOffset() + 1);
			elToRemove.add(curE);
			//did we enter a (different) table cell?
			Element ckTD = HtmlUtils.getParent(curE, HTML.Tag.TD);
			if (ckTD != null && !ckTD.equals(curTD)) {
				//stop here so we don't mess up the table
				break;
			}
		}
		if (isListType()) {
			html += "</" + getTag() + ">";
		}
		//set the caret to the start of the last selected block element
		editor.setCaretPosition(curE.getStartOffset());
		//insert our changed block
		//we insert first and then remove, because of a bug in jdk 6.0
		insertHTML(html, getTag(), rootTag, e);
		//now, remove the elements that were changed.
		for (Iterator it = elToRemove.iterator(); it.hasNext();) {
			Element c = (Element) it.next();
			HtmlUtils.removeElement(c);
		}
	}

	private boolean isListType() {
		return type == OL || type == UL;
	}

	/**
	 * Gets the tag
	 *
	 * @return
	 */
	public HTML.Tag getTag() {
		HTML.Tag tag = HTML.Tag.DIV;
		switch (type) {
			case DIV:
				tag = HTML.Tag.DIV;
				break;
			case P:
				tag = HTML.Tag.P;
				break;
			case H1:
				tag = HTML.Tag.H1;
				break;
			case H2:
				tag = HTML.Tag.H2;
				break;
			case H3:
				tag = HTML.Tag.H3;
				break;
			case H4:
				tag = HTML.Tag.H4;
				break;
			case H5:
				tag = HTML.Tag.H5;
				break;
			case H6:
				tag = HTML.Tag.H6;
				break;
			case PRE:
				tag = HTML.Tag.PRE;
				break;
			case UL:
				tag = HTML.Tag.UL;
				break;
			case OL:
				tag = HTML.Tag.OL;
				break;
			case BLOCKQUOTE:
				tag = HTML.Tag.BLOCKQUOTE;
				break;
		}
		return tag;
	}

}
