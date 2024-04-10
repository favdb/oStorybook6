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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import api.shef.actions.CompoundManager;
import api.shef.tools.HtmlUtils;

/**
 * @author Bob Tantlinger
 *
 */
public class HTMLIndentAction extends HTMLTextEditAction {

    public static final int INDENT = 0, OUTDENT = 1;
    protected int direction = INDENT;

    /**
     * @param editor
     * @param direction
     */
    public HTMLIndentAction(WysiwygEditor editor, int direction) throws IllegalArgumentException {
	super(editor, (direction == INDENT ? "Indent" : "Outdent"));
	this.direction = direction;
    }

    private void insertHTML(String html, HTML.Tag tag, HTML.Tag root, ActionEvent e) {
	HTMLEditorKit.InsertHTMLTextAction a
	    = new HTMLEditorKit.InsertHTMLTextAction("insertHTML", html, root, tag);
	a.actionPerformed(e);
    }

    private HTML.Tag getRootTag(Element elem) {
	HTML.Tag root = HTML.Tag.BODY;
	if (HtmlUtils.getListParent(elem) != null) {
	    root = HTML.Tag.UL;
	} else if (HtmlUtils.getParent(elem, HTML.Tag.TD) != null) {
	    root = HTML.Tag.TD;
	} else if (HtmlUtils.getParent(elem, HTML.Tag.BLOCKQUOTE) != null) {
	    root = HTML.Tag.BLOCKQUOTE;
	}
	return root;
    }

    @SuppressWarnings("unchecked")
    private Map getListElems(List elems) {
	Map lis = new HashMap();
	for (Iterator it = elems.iterator(); it.hasNext();) {
	    Element li = HtmlUtils.getParent((Element) it.next(), HTML.Tag.LI);
	    if (li != null) {
		Element listEl = HtmlUtils.getListParent(li);
		if (!lis.containsKey(listEl)) {
		    lis.put(listEl, new ArrayList());
		}
		List elList = (List) lis.get(listEl);
		elList.add(li);
	    }
	}
	return lis;
    }

    @SuppressWarnings("unchecked")
    private void unindent(ActionEvent e, JEditorPane editor) {
	List elems = getParagraphElements(editor);
	if (elems.isEmpty()) {
	    return;
	}
	List listElems = getLeadingTralingListElems(elems);
	elems.removeAll(listElems);
	Set elsToIndent = new HashSet();
	Set elsToOutdent = new HashSet();
	Element lastBqParent = null;
	for (int i = 0; i < elems.size(); i++) {
	    Element el = (Element) elems.get(i);
	    Element bqParent = HtmlUtils.getParent(el, HTML.Tag.BLOCKQUOTE);
	    if (bqParent == null) {
		continue;
	    }
	    if (lastBqParent == null || bqParent.getStartOffset() >= lastBqParent.getEndOffset()) {
		elsToOutdent.add(bqParent);
		lastBqParent = bqParent;
	    }
	    if (i == 0 || i == elems.size() - 1) {
		int c = bqParent.getElementCount();
		for (int j = 0; j < c; j++) {
		    Element bqChild = bqParent.getElement(j);
		    int start = bqChild.getStartOffset();
		    int end = bqChild.getEndOffset();
		    if (end < editor.getSelectionStart() || start > editor.getSelectionEnd()) {
			elsToIndent.add(bqChild);
		    }
		}
	    }
	}
	HTMLDocument doc = (HTMLDocument) editor.getDocument();
	adjustListElemsIndent(listElems, doc);
	blockquoteElements(new ArrayList(elsToIndent), doc);
	unblockquoteElements(new ArrayList(elsToOutdent), doc);
    }

    @SuppressWarnings("unchecked")
    private void adjustListElemsIndent(List elems, HTMLDocument doc) {
	Set rootLists = new HashSet();
	Set liElems = new HashSet();
	for (int i = 0; i < elems.size(); i++) {
	    Element liEl = HtmlUtils.getParent((Element) elems.get(i), HTML.Tag.LI);
	    if (liEl == null) {
		continue;
	    }
	    liElems.add(liEl);
	    Element rootList = HtmlUtils.getListParent(liEl);
	    if (rootList != null) {
		while (HtmlUtils.getListParent(rootList.getParentElement()) != null) {
		    rootList = HtmlUtils.getListParent(rootList.getParentElement());
		}
		rootLists.add(rootList);
	    }
	}
	for (Iterator it = rootLists.iterator(); it.hasNext();) {
	    Element rl = (Element) it.next();
	    String newHtml = buildListHTML(rl, new ArrayList(liElems));
	    System.err.println(newHtml);
	    try {
		doc.setInnerHTML(rl, newHtml);
	    } catch (IOException | BadLocationException ex) {
		ex.printStackTrace(System.err);
	    }
	}
    }

    @SuppressWarnings("unchecked")
    private List getItems(Element list, List selLiElems, int level) {
	int c = list.getElementCount();
	List items = new ArrayList();
	for (int i = 0; i < c; i++) {
	    Element e = list.getElement(i);
	    if (e.getName().equals("li")) {
		ListItem item = new ListItem();
		item.listTag = HTML.getTag(list.getName());
		item.level = level;
		if (selLiElems.contains(e)) {
		    if (direction == INDENT) {
			item.level++;
		    } else {
			if (item.level > 0) {
			    item.level--;
			}
		    }
		}
		item.html = HtmlUtils.getElementHTML(e, true);
		items.add(item);
	    } else if (HtmlUtils.getListParent(e) == e) {
		items.addAll(getItems(e, selLiElems, level + 1));
	    }
	}
	return items;
    }

    private String buildListHTML(Element list, List liItems) {
	List items = getItems(list, liItems, 0);
	ListItem lastItem = null;
	StringBuilder html = new StringBuilder();
	for (int i = 0; i < items.size(); i++) {
	    ListItem item = (ListItem) items.get(i);
	    if (lastItem != null && (lastItem.level != item.level || !lastItem.listTag.equals(item.listTag))) {
		if (lastItem.level > item.level) {
		    html.append(openOrCloseList(lastItem.listTag, -1 * (lastItem.level - item.level)));
		    html.append(item.html);
		} else if (item.level > lastItem.level) {
		    html.append(openOrCloseList(item.listTag, (item.level - lastItem.level)));
		    html.append(item.html);
		} else {
		    html.append(item.html);
		}
	    } else {
		if (lastItem == null) {
		    html.append(openOrCloseList(item.listTag, item.level));
		}
		html.append(item.html);
	    }
	    lastItem = item;
	}
	if (lastItem != null) {
	    html.append(openOrCloseList(lastItem.listTag, -1 * lastItem.level));
	}
	return html.toString();
    }

    private String openOrCloseList(HTML.Tag ltag, int level) {
	String tag;
	if (level < 0) {
	    tag = "</" + ltag + ">\n";
	} else {
	    tag = "<" + ltag + ">\n";
	}
	int c = Math.abs(level);
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < c; i++) {
	    sb.append(tag);
	}
	return sb.toString();
    }

    private class ListItem {

	String html;
	int level;
	HTML.Tag listTag;
    }

    @SuppressWarnings("unchecked")
    private List getLeadingTralingListElems(List elems) {
	Set listElems = new HashSet();
	for (int i = 0; i < elems.size(); i++) {
	    Element el = (Element) elems.get(i);
	    if (HtmlUtils.getListParent(el) != null) {
		listElems.add(el);
	    } else {
		break;
	    }
	}
	for (int i = elems.size() - 1; i >= 0; i--) {
	    Element el = (Element) elems.get(i);
	    if (HtmlUtils.getListParent(el) != null) {
		listElems.add(el);
	    } else {
		break;
	    }
	}
	return new ArrayList(listElems);
    }

    @SuppressWarnings("unchecked")
    private void indent(ActionEvent e, JEditorPane editor) {
	List elems = getParagraphElements(editor);
	if (elems.isEmpty()) {
	    return;
	}
	List listElems = this.getLeadingTralingListElems(elems);
	elems.removeAll(listElems);
	HTMLDocument doc = (HTMLDocument) editor.getDocument();
	blockquoteElements(elems, doc);
	adjustListElemsIndent(listElems, doc);
    }

    private void unblockquoteElements(List elems, HTMLDocument doc) {
	for (Iterator it = elems.iterator(); it.hasNext();) {
	    Element curE = (Element) it.next();
	    if (!curE.getName().equals("blockquote")) {
		continue;
	    }
	    String eleHtml = HtmlUtils.getElementHTML(curE, false);
	    HTML.Tag t = HtmlUtils.getStartTag(eleHtml);
	    if (t == null || !t.breaksFlow()) {
		eleHtml = "<p>\n" + eleHtml + "</p>\n";
	    }
	    try {
		doc.setOuterHTML(curE, eleHtml);
	    } catch (IOException | BadLocationException ex) {
		ex.printStackTrace(System.err);
	    }
	}
    }

    private void blockquoteElements(List elems, HTMLDocument doc) {
	for (Iterator it = elems.iterator(); it.hasNext();) {
	    Element curE = (Element) it.next();
	    String eleHtml = HtmlUtils.getElementHTML(curE, true);
	    StringBuilder sb = new StringBuilder();
	    sb.append("<blockquote>\n");
	    sb.append(eleHtml);
	    sb.append("</blockquote>\n");
	    try {
		doc.setOuterHTML(curE, sb.toString());
	    } catch (IOException | BadLocationException ex) {
		ex.printStackTrace(System.err);
	    }
	}
    }

    @SuppressWarnings("unchecked")
    public List getParagraphElements(JEditorPane editor) {
	List elems = new ArrayList();
	try {
	    HTMLDocument doc = (HTMLDocument) editor.getDocument();
	    Element curE = getParaElement(doc, editor.getSelectionStart());
	    Element endE = getParaElement(doc, editor.getSelectionEnd());
	    while (curE.getEndOffset() <= endE.getEndOffset()) {
		elems.add(curE);
		curE = getParaElement(doc, curE.getEndOffset() + 1);
		if (curE.getEndOffset() >= doc.getLength()) {
		    break;
		}
	    }
	} catch (ClassCastException cce) {
	}
	return elems;
    }

    private Element getParaElement(HTMLDocument doc, int pos) {
	Element curE = doc.getParagraphElement(pos);
	return curE;
    }

    @Override
    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
	int cp = editor.getCaretPosition();
	CompoundManager.beginCompoundEdit(editor.getDocument());
	if (direction == INDENT) {
	    indent(e, editor);
	} else {
	    unindent(e, editor);
	}
	CompoundManager.endCompoundEdit(editor.getDocument());
	editor.setCaretPosition(cp);
    }

}
