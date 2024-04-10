/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import api.shef.tools.ElementWriter;
import api.shef.tools.HtmlUtils;
import api.shef.tools.LOG;

/**
 * Action which properly inserts breaks for an HTMLDocument
 *
 * @author Bob Tantlinger
 *
 */
public class EnterKeyAction extends DecoratedTextAction {

	/**
	 * Creates a new EnterKeyAction.
	 *
	 * @param defaultEnterAction Should be the default action
	 */
	public EnterKeyAction(Action defaultEnterAction) {
		super("EnterAction", defaultEnterAction);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//LOG.trace("EnterKeyAction.actionPerformed(e=" + e.toString() + ")");
		JEditorPane editor;
		HTMLDocument document;
		try {
			editor = (JEditorPane) getTextComponent(e);
			document = (HTMLDocument) editor.getDocument();
		} catch (ClassCastException ex) {
			// don't know what to do with this type
			// so pass off the event to the delegate
			delegate.actionPerformed(e);
			return;
		}
		Element elem = document.getParagraphElement(editor.getCaretPosition());
		Element parentElem = elem.getParentElement();
		HTML.Tag tag = HTML.getTag(elem.getName());
		HTML.Tag parentTag = HTML.getTag(parentElem.getName());
		int caret = editor.getCaretPosition();
		CompoundManager.beginCompoundEdit(document);
		try {
			if (HtmlUtils.isImplied(elem)) {
				//are we inside a list item?
				if (parentTag.equals(HTML.Tag.LI)) {
					//does the list item have any contents
					if (parentElem.getEndOffset() - parentElem.getStartOffset() > 1) {
						String txt = "";
						//caret at start of listitem
						if (caret == parentElem.getStartOffset()) {
							document.insertBeforeStart(parentElem, toListItem(txt));
						}//caret in the middle of list item content
						else if (caret < parentElem.getEndOffset() - 1 && caret > parentElem.getStartOffset()) {
							int len = parentElem.getEndOffset() - caret;
							txt = document.getText(caret, len);
							caret--;// hmmm
							document.insertAfterEnd(parentElem, toListItem(txt));
							document.remove(caret, len);
						} else//caret at end of list item
						{
							document.insertAfterEnd(parentElem, toListItem(txt));
						}

						editor.setCaretPosition(caret + 1);
					} else {
						// empty list item
						Element listParentElem = HtmlUtils.getListParent(parentElem).getParentElement();
						if (isListItem(HTML.getTag(listParentElem.getName()))) {
							//nested list
							String ml = HtmlUtils.getElementHTML(listParentElem, true);
							ml = ml.replaceFirst("\\<li\\>\\s*\\<\\/li\\>\\s*\\<\\/ul\\>", "</ul>");
							ml = ml.replaceFirst("\\<ul\\>\\s*\\<\\/ul\\>", "");
							document.setOuterHTML(listParentElem, ml);
						}//are we directly under a table cell?
						else if (listParentElem.getName().equals("td")) {
							//reset the table cell contents nested in a <div>
							//we do this because otherwise the next table cell would
							//get deleted!! Perhaps this is a bug in swing's html implemenation?
							encloseInDIV(listParentElem, document);
							editor.setCaretPosition(caret + 1);
						} else {
							//end the list
							if (isInList(listParentElem)) {
								HTML.Tag listParentTag = HTML.getTag(HtmlUtils.getListParent(listParentElem).toString());
								HTMLEditorKit.InsertHTMLTextAction a
										= new HTMLEditorKit.InsertHTMLTextAction("insert",
												"<li></li>", listParentTag, HTML.Tag.LI);
								a.actionPerformed(e);
							} else {
								HTML.Tag root = HTML.Tag.BODY;
								if (HtmlUtils.getParent(elem, HTML.Tag.TD) != null) {
									root = HTML.Tag.TD;
								}
								HTMLEditorKit.InsertHTMLTextAction a
										= new HTMLEditorKit.InsertHTMLTextAction("insert",
												"<p></p>", root, HTML.Tag.P);
								a.actionPerformed(e);
							}
							HtmlUtils.removeElement(parentElem);
						}
					}
				} else {
					//not a list
					if (parentTag.isPreformatted()) {
						insertImpliedBR(e);
					} else if (parentTag.equals(HTML.Tag.TD)) {
						encloseInDIV(parentElem, document);
						editor.setCaretPosition(caret + 1);
					} else if (parentTag.equals(HTML.Tag.BODY) || isInList(elem)) {
						insertParagraphAfter(elem, editor);
					} else {
						insertParagraphAfter(parentElem, editor);
					}
				}
			} else {
				//not implied
				//we need to check for this here in case any straggling li's
				//or dd's exist
				if (isListItem(tag)) {
					if ((elem.getEndOffset() - editor.getCaretPosition()) == 1) {
						//caret at end of para
						editor.replaceSelection("\n ");
						editor.setCaretPosition(editor.getCaretPosition() - 1);
					} else {
						delegate.actionPerformed(e);
					}
				} else {
					// if Maj+Enter then it's a <br>
					if (e.getModifiers() == ActionEvent.SHIFT_MASK) {
					}
					insertParagraphAfter(elem, editor);
				}
			}
		} catch (Exception ex) {
			LOG.err("", ex);
		}
		CompoundManager.endCompoundEdit(document);
	}

	private boolean isListItem(HTML.Tag t) {
		return (t.equals(HTML.Tag.LI) || t.equals(HTML.Tag.DT) || t.equals(HTML.Tag.DD));
	}

	private String toListItem(String txt) {
		return "<li>" + txt + "</li>";
	}

	private boolean isInList(Element el) {
		return HtmlUtils.getListParent(el) != null;
	}

	private void insertImpliedBR(ActionEvent e) {
		HTMLEditorKit.InsertHTMLTextAction hta
				= new HTMLEditorKit.InsertHTMLTextAction("insertBR",
						"<br>", HTML.Tag.IMPLIED, HTML.Tag.BR);
		hta.actionPerformed(e);
	}

	private void encloseInDIV(Element elem, HTMLDocument document) throws Exception {
		HTML.Tag tag = HTML.getTag(elem.getName());
		String html = HtmlUtils.getElementHTML(elem, false);
		html = HtmlUtils.createTag(tag,
				elem.getAttributes(), "<div>" + html + "</div><div></div>");
		document.setOuterHTML(elem, html);
	}

	/**
	 * Inserts a paragraph after the current paragraph of the same type
	 *
	 * @param elem
	 * @param editor
	 * @throws BadLocationException
	 * @throws java.io.IOException
	 */
	private void insertParagraphAfter(Element elem, JEditorPane editor) throws BadLocationException, IOException {
		int cr = editor.getCaretPosition();
		HTMLDocument document = (HTMLDocument) elem.getDocument();
		HTML.Tag t = HTML.getTag(elem.getName());
		int endOffs = elem.getEndOffset();
		int startOffs = elem.getStartOffset();
		//if this is an implied para, make the new para a div
		if (t == null || elem.getName().equals("p-implied")) {
			t = HTML.Tag.DIV;
		}
		String html;
		//got to test for this here, otherwise <hr> and <br>
		//get duplicated
		if (cr == startOffs) {
			html = createBlock(t, elem, "");
		} else {
			//split the current para at the cursor position
			StringWriter out = new StringWriter();
			ElementWriter w = new ElementWriter(out, elem, startOffs, cr);
			w.write();
			html = createBlock(t, elem, out.toString());
		}
		if (cr == endOffs - 1) {
			html += createBlock(t, elem, "");
		} else {
			StringWriter out = new StringWriter();
			ElementWriter w = new ElementWriter(out, elem, cr, endOffs);
			w.write();
			html += createBlock(t, elem, out.toString());
		}
		//copy the current para's character attributes
		AttributeSet chAttribs;
		if (endOffs > startOffs && cr == endOffs - 1) {
			chAttribs = new SimpleAttributeSet(document.getCharacterElement(cr - 1).getAttributes());
		} else {
			chAttribs = new SimpleAttributeSet(document.getCharacterElement(cr).getAttributes());
		}
		document.setOuterHTML(elem, html);
		cr++;
		Element p = document.getParagraphElement(cr);
		if (cr == endOffs) {
			//update the character attributes for the added paragraph
			//F I X M E If the added paragraph is at the start/end
			//of the document, the char attrs dont get set
			setCharAttribs(p, chAttribs);
		}
		editor.setCaretPosition(p.getStartOffset());
	}

	private String createBlock(HTML.Tag t, Element elem, String html) {
		AttributeSet attribs = elem.getAttributes();
		return HtmlUtils.createTag(t, attribs,
				HtmlUtils.removeEnclosingTags(elem, html));
	}

	private void setCharAttribs(Element p, AttributeSet chAttribs) {
		HTMLDocument document = (HTMLDocument) p.getDocument();
		int start = p.getStartOffset();
		int end = p.getEndOffset();
		SimpleAttributeSet sas = new SimpleAttributeSet(chAttribs);
		sas.removeAttribute(HTML.Attribute.SRC);
		//if the charattribs contains a br, hr, or img attribute, it'll erase
		//any content in the paragraph
		boolean skipAttribs = false;
		for (Enumeration ee = sas.getAttributeNames(); ee.hasMoreElements();) {
			Object n = ee.nextElement();
			String val = chAttribs.getAttribute(n).toString();
			skipAttribs = val.equals("br") || val.equals("hr") || val.equals("img");
		}
		if (!skipAttribs) {
			document.setCharacterAttributes(start, end - start, sas, true);
		}
	}

}
