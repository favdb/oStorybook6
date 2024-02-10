/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.tools;

import static api.shef.tools.LOG.ERR;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import api.jsoup.Jsoup;
import api.jsoup.select.Elements;

/**
 * A collection of static convenience methods for working with HTML, HTMLDocuments, AttributeSets
 * and Elements from HTML documents.
 *
 * @author Bob Tantlinger
 *
 */
public class HtmlUtils {

	private HtmlUtils() {
		// nothing
	}

	/**
	 * Tests if an element is an implied paragraph (p-implied)
	 *
	 * @param el The element
	 * @return true if the elements name equals "p-implied", false otherwise
	 */
	public static boolean isImplied(Element el) {
		return el.getName().equals("p-implied");
	}

	/**
	 * Incloses a chunk of HTML text in the specified tag
	 *
	 * @param enclTag the tag to enclose the HTML in
	 * @param innerHTML the HTML to be inclosed
	 * @return
	 */
	public static String createTag(HTML.Tag enclTag, String innerHTML) {
		return createTag(enclTag, new SimpleAttributeSet(), innerHTML);
	}

	/**
	 * Incloses a chunk of HTML text in the specified tag with the specified attribs
	 *
	 * @param enclTag
	 * @param set
	 * @param innerHTML
	 * @return
	 */
	public static String createTag(HTML.Tag enclTag, AttributeSet set, String innerHTML) {
		return tagOpen(enclTag, set) + innerHTML + tagClose(enclTag);
	}

	private static String tagOpen(HTML.Tag enclTag, AttributeSet set) {
		String t = "<" + enclTag;
		for (Enumeration e = set.getAttributeNames(); e.hasMoreElements();) {
			Object name = e.nextElement();
			if (!name.toString().equals("name")) {
				Object val = set.getAttribute(name);
				t += " " + name + "=\"" + val + "\"";
			}
		}

		return t + ">";
	}

	private static String tagClose(HTML.Tag t) {
		return "</" + t + ">";
	}

	@SuppressWarnings("unchecked")
	public static List getParagraphElements(JEditorPane editor) {
		List elems = new LinkedList();
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

	private static Element getParaElement(HTMLDocument doc, int pos) {
		Element curE = doc.getParagraphElement(pos);
		while (isImplied(curE)) {
			curE = curE.getParentElement();
		}

		Element lp = getListParent(curE);
		if (lp != null) {
			curE = lp;
		}

		return curE;
	}

	/**
	 * Searches upward for the specified parent for the element.
	 *
	 * @param curElem
	 * @param parentTag
	 * @return The parent element, or null if the parent wasnt found
	 */
	public static Element getParent(Element curElem, HTML.Tag parentTag) {
		Element parent = curElem;
		while (parent != null) {
			if (parent.getName().equals(parentTag.toString())) {
				return parent;
			}
			parent = parent.getParentElement();
		}

		return null;
	}

	/**
	 * Tests if the element is empty
	 *
	 * @param el
	 * @return
	 */
	public static boolean isElementEmpty(Element el) {
		String s = getElementHTML(el, false).trim();
		return s.length() == 0;
	}

	/**
	 * Searches for a list {@link Element} that is the parent of the specified {@link Element}.
	 *
	 * @param elem
	 * @return A list element (UL, OL, DIR, MENU, or DL) if found, null otherwise
	 */
	public static Element getListParent(Element elem) {
		Element parent = elem;
		while (parent != null) {
			if (parent.getName().equalsIgnoreCase("UL")
				|| parent.getName().equalsIgnoreCase("OL")
				|| parent.getName().equals("dl") || parent.getName().equals("menu")
				|| parent.getName().equals("dir")) {
				return parent;
			}
			parent = parent.getParentElement();
		}
		return null;
	}

	/**
	 * Gets the element one position less than the start of the specified element
	 *
	 * @param doc
	 * @param el
	 * @return
	 */
	public static Element getPreviousElement(HTMLDocument doc, Element el) {
		if (el.getStartOffset() > 0) {
			return doc.getParagraphElement(el.getStartOffset() - 1);
		}
		return el;
	}

	/**
	 * Gets the element one position greater than the end of the specified element
	 *
	 * @param doc
	 * @param el
	 * @return
	 */
	public static Element getNextElement(HTMLDocument doc, Element el) {
		if (el.getEndOffset() < doc.getLength()) {
			return doc.getParagraphElement(el.getEndOffset() + 1);
		}
		return el;
	}

	/**
	 * Removes the enclosing tags from a chunk of HTML text
	 *
	 * @param elem
	 * @param txt
	 * @return
	 */
	public static String removeEnclosingTags(Element elem, String txt) {
		HTML.Tag t = HTML.getTag(elem.getName());
		return removeEnclosingTags(t, txt);
	}

	/**
	 * Removes the enclosing tags from a chunk of HTML text
	 *
	 * @param t
	 * @param txt
	 * @return
	 */
	public static String removeEnclosingTags(HTML.Tag t, String txt) {
		String openStart = "<" + t;
		String closeTag = "</" + t + ">";
		txt = txt.trim();
		if (txt.startsWith(openStart)) {
			int n = txt.indexOf(">");
			if (n != -1) {
				txt = txt.substring(n + 1, txt.length());
			}
		}
		if (txt.endsWith(closeTag)) {
			txt = txt.substring(0, txt.length() - closeTag.length());
		}
		return txt;
	}

	/**
	 * Gets the html of the specified {@link Element}
	 *
	 * @param el
	 * @param includeEnclosingTags true, if the enclosing tags should be included
	 * @return
	 */
	public static String getElementHTML(Element el, boolean includeEnclosingTags) {
		String txt = "";
		try {
			StringWriter out = new StringWriter();
			ElementWriter w = new ElementWriter(out, el);
			w.write();
			txt = out.toString();
		} catch (IOException | BadLocationException ex) {
			LOG.write(ERR, ex);
		}
		if (includeEnclosingTags) {
			return txt;
		}
		return removeEnclosingTags(el, txt);
	}

	/**
	 * Removes an element from the document that contains it
	 *
	 * @param el
	 * @throws BadLocationException
	 */
	public static void removeElement(Element el) throws BadLocationException {
		HTMLDocument document = (HTMLDocument) el.getDocument();
		int start = el.getStartOffset();
		int len = el.getEndOffset() - start;

		Element tdEle = HtmlUtils.getParent(el, HTML.Tag.TD);
		if (tdEle != null && el.getEndOffset() == tdEle.getEndOffset()) {
			document.remove(start, len - 1);
		} else {
			if (el.getEndOffset() > document.getLength()) {
				len = document.getLength() - start;
			}

			document.remove(start, len);
		}
	}

	public static HTML.Tag getStartTag(String text) {
		String html = text.trim();
		int s = html.indexOf('<');
		if (s != 0) {
			//doesn't start with a tag.
			return null;
		}
		int e = html.indexOf('>');
		if (e == -1) {
			//not any kind of tag
			return null;
		}
		String tagName = html.substring(1, e).trim();
		if (tagName.indexOf(' ') != -1) {
			tagName = tagName.split("\\s")[0];
		}

		return HTML.getTag(tagName);
	}

	private static int depthFromRoot(Element curElem) {
		Element parent = curElem;
		int depth = 0;
		while (parent != null) {
			if (parent.getName().equals("body") || parent.getName().equals("td")) {
				break;
			}
			parent = parent.getParentElement();
			depth++;
		}

		return depth;
	}

	/**
	 * Inserts an arbitrary chunk of HTML into the JEditorPane at the current caret position.
	 *
	 * @param rawHtml
	 * @param editor
	 */
	public static void insertArbitraryHTML(String rawHtml, JEditorPane editor) {

		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(rawHtml.getBytes("UTF-8"));
			api.jsoup.nodes.Document doc = Jsoup.parse(bin.toString());
			api.jsoup.nodes.Element body = doc.body();

			if (body != null) {
				Elements bodyChildren = body.getAllElements();

				//for(int i = bodyChildren.getLength() - 1; i >= 0; i--)
				int len = bodyChildren.size();
				for (int i = 0; i < len; i++) {
					String ml = bodyChildren.get(i).html();
					if (ml != null) {
						//System.out.println(ml);
						HTML.Tag tag = getStartTag(ml);
						if (tag == null) {
							tag = HTML.Tag.SPAN;
							ml = "<span>" + ml + "</span>";
						}
						insertHTML(ml, tag, editor);
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			LOG.write(ERR, e);
		}
	}

	/**
	 * Inserts a string of html into the {@link JEditorPane}'s {@link HTMLDocument} at the current
	 * caret position.
	 *
	 * @param html
	 * @param tag
	 * @param editor
	 */
	public static void insertHTML(String html, HTML.Tag tag, JEditorPane editor) {
		HTMLEditorKit editorKit;
		HTMLDocument document;
		try {
			editorKit = (HTMLEditorKit) editor.getEditorKit();
			document = (HTMLDocument) editor.getDocument();
		} catch (ClassCastException ex) {
			return;
		}

		int caret = editor.getCaretPosition();
		Element pElem = document.getParagraphElement(caret);

		boolean breakParagraph = tag.breaksFlow() || tag.isBlock();
		boolean beginParagraph = caret == pElem.getStartOffset();
		html = jEditorPaneizeHTML(html);

		try {
			if (breakParagraph && beginParagraph) {
				document.insertBeforeStart(pElem, "<p></p>");
				Element nextEl = document.getParagraphElement(caret + 1);
				editorKit.insertHTML(document, caret + 1, html, depthFromRoot(nextEl), 0, tag);
				document.remove(caret, 1);
			} else if (breakParagraph && !beginParagraph) {
				editorKit.insertHTML(document, caret, html, depthFromRoot(pElem), 0, tag);
			} else if (!breakParagraph && beginParagraph) {
				document.insertAfterStart(pElem, "&nbsp;");
				editorKit.insertHTML(document, caret + 1, html, 0, 0, tag);
				document.remove(caret, 1);
			} else if (!breakParagraph && !beginParagraph) {
				//System.out.println("!breakParagraph && !beginParagraph");
				editorKit.insertHTML(document, caret, html, 0, 0, tag);
			}
		} catch (IOException | BadLocationException ex) {
			LOG.write(ERR, ex);
		}
	}

	/**
	 * Gets the character attributes at the {@link JEditorPane}'s caret position
	 * <p>
	 * If there is no selection, the character attributes at caretPos - 1 are retuned. If there is a
	 * selection, the attributes at selectionEnd - 1 are returned
	 * </p>
	 *
	 * @param editor
	 * @return An {@link AttributeSet} or null, if the editor doesn't have a {@link StyledDocument}
	 */
	public static AttributeSet getCharacterAttributes(JEditorPane editor) {
		int p;
		if (editor.getSelectedText() != null) {
			p = editor.getSelectionEnd() - 1;
		} else {
			p = (editor.getCaretPosition() > 0) ? (editor.getCaretPosition() - 1) : 0;
		}

		try {
			StyledDocument doc = (StyledDocument) editor.getDocument();
			return (doc.getCharacterElement(p).getAttributes());
		} catch (ClassCastException cce) {
		}

		return null;
	}

	/**
	 * Gets the font family name at the {@link JEditorPane}'s current caret position
	 *
	 * @param editor
	 * @return The font family name, or null if no font is set
	 */
	public static String getFontFamily(JEditorPane editor) {
		AttributeSet attr = getCharacterAttributes(editor);
		if (attr != null) {
			Object val = attr.getAttribute(StyleConstants.FontFamily);
			if (val != null) {
				return val.toString();
			}
			val = attr.getAttribute(CSS.Attribute.FONT_FAMILY);
			if (val != null) {
				return val.toString();
			}
			val = attr.getAttribute(HTML.Tag.FONT);
			if (val != null && val instanceof AttributeSet) {
				AttributeSet set = (AttributeSet) val;
				val = set.getAttribute(HTML.Attribute.FACE);
				if (val != null) {
					return val.toString();
				}
			}
		}
		//no font family was defined
		return null;
	}

	/**
	 * Set's the font family at the {@link JEditorPane}'s current caret positon, or for the current
	 * selection (if there is one).
	 * <p>
	 * If the fontName parameter is null, any currently set font family is removed.
	 * </p>
	 *
	 * @param editor
	 * @param fontName
	 */
	public static void setFontFamily(JEditorPane editor, String fontName) {
		AttributeSet attr = getCharacterAttributes(editor);
		if (attr == null) {
			return;
		}

		printAttribs(attr);
		if (fontName == null) {
			//we're removing the font
			//the font might be defined as a font tag
			Object val = attr.getAttribute(HTML.Tag.FONT);
			if (val instanceof AttributeSet) {
				MutableAttributeSet set = new SimpleAttributeSet((AttributeSet) val);
				val = set.getAttribute(HTML.Attribute.FACE); //does it have a FACE attrib?
				if (val != null) {
					set.removeAttribute(HTML.Attribute.FACE);
					removeCharacterAttribute(editor, HTML.Tag.FONT); //remove the current font tag
					if (set.getAttributeCount() > 0) {
						//it's not empty so replace the other font attribs
						SimpleAttributeSet fontSet = new SimpleAttributeSet();
						fontSet.addAttribute(HTML.Tag.FONT, set);
						setCharacterAttributes(editor, set);
					}
				}
			}
			//also remove these for good measure
			removeCharacterAttribute(editor, StyleConstants.FontFamily);
			removeCharacterAttribute(editor, CSS.Attribute.FONT_FAMILY);
		} else {
			//adding the font family
			MutableAttributeSet tagAttrs = new SimpleAttributeSet();
			tagAttrs.addAttribute(StyleConstants.FontFamily, fontName);
			setCharacterAttributes(editor, tagAttrs);
		}
		printAttribs(attr);
	}

	/**
	 * Removes a CSS character attribute that has the specified value from the {@link JEditorPane}'s
	 * current caret position or selection.
	 * <p>
	 * The val parameter is a {@link String} even though the actual attribute value is not. This is
	 * because the actual attribute values are not public. Thus, this method checks the value via
	 * the toString() method</p>
	 *
	 * @param editor
	 * @param atr
	 * @param val
	 */
	public static void removeCharacterAttribute(JEditorPane editor, CSS.Attribute atr, String val) {
		HTMLDocument doc;
		MutableAttributeSet attr;
		try {
			doc = (HTMLDocument) editor.getDocument();
			attr = ((HTMLEditorKit) editor.getEditorKit()).getInputAttributes();
		} catch (ClassCastException cce) {
			return;
		}

		List tokens = tokenizeCharAttribs(doc, editor.getSelectionStart(), editor.getSelectionEnd());
		for (Iterator it = tokens.iterator(); it.hasNext();) {
			CharStyleToken t = (CharStyleToken) it.next();
			if (t.attrs.isDefined(atr) && t.attrs.getAttribute(atr).toString().equals(val)) {
				SimpleAttributeSet sas = new SimpleAttributeSet();
				sas.addAttributes(t.attrs);
				sas.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
				sas.removeAttribute(atr);
				doc.setCharacterAttributes(t.offs, t.len, sas, true);
			}
		}
		int pos = editor.getCaretPosition();
		attr.addAttributes(doc.getCharacterElement(pos).getAttributes());
		attr.removeAttribute(atr);
	}

	/**
	 * Removes a single character attribute from the editor's current position/selection.
	 *
	 * <p>
	 * Removes from the editor kit's input attribtues and/or document at the caret position. If
	 * there is a selction the attribute is removed from the selected text</p>
	 *
	 * @param editor
	 * @param atr
	 */
	public static void removeCharacterAttribute(JEditorPane editor, Object atr) {
		HTMLDocument doc;
		MutableAttributeSet attr;
		try {
			doc = (HTMLDocument) editor.getDocument();
			attr = ((HTMLEditorKit) editor.getEditorKit()).getInputAttributes();
		} catch (ClassCastException cce) {
			return;
		}

		List tokens = tokenizeCharAttribs(doc, editor.getSelectionStart(), editor.getSelectionEnd());
		for (Iterator it = tokens.iterator(); it.hasNext();) {
			CharStyleToken t = (CharStyleToken) it.next();
			if (t.attrs.isDefined(atr)) {
				SimpleAttributeSet sas = new SimpleAttributeSet();
				sas.addAttributes(t.attrs);
				sas.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
				sas.removeAttribute(atr);
				doc.setCharacterAttributes(t.offs, t.len, sas, true);
			}
		}
		int pos = editor.getCaretPosition();
		attr.addAttributes(doc.getCharacterElement(pos).getAttributes());
		attr.removeAttribute(atr);
	}

	/**
	 * Tokenizes character attrbutes.
	 *
	 * @param doc
	 * @param s
	 * @param e
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static List tokenizeCharAttribs(HTMLDocument doc, int s, int e) {
		LinkedList tokens = new LinkedList();
		CharStyleToken tok = new CharStyleToken();
		for (; s <= e; s++) {
			AttributeSet as = doc.getCharacterElement(s).getAttributes();
			if (tok.attrs == null || (s + 1 <= e && !as.isEqual(tok.attrs))) {
				tok = new CharStyleToken();
				tok.offs = s;
				tokens.add(tok);
				tok.attrs = as;
			}
			if (s + 1 <= e) {
				tok.len++;
			}
		}
		return tokens;
	}

	/**
	 * Sets the character attributes for selection of the specified editor
	 *
	 * @param editor
	 * @param attrs
	 * @param replace if true, replaces the attributes
	 */
	public static void setCharacterAttributes(JEditorPane editor, AttributeSet attr, boolean replace) {
		HTMLDocument doc;
		StyledEditorKit k;
		try {
			doc = (HTMLDocument) editor.getDocument();
			k = (StyledEditorKit) editor.getEditorKit();
		} catch (ClassCastException ex) {
			return;
		}

		MutableAttributeSet inputAttributes = k.getInputAttributes();
		SimpleAttributeSet sas = new SimpleAttributeSet(attr);
		sas.removeAttribute("CR");
		attr = sas;

		int p0 = editor.getSelectionStart();
		int p1 = editor.getSelectionEnd();
		if (p0 != p1) {
			doc.setCharacterAttributes(p0, p1 - p0, attr, replace);
		} else {
			//No selection, so we have to update the input attributes
			//otherwise they apparently get reread from the document...
			//not sure if this is a bug or what, but the following works
			//so just go with it.
			if (replace) {
				attr = attr.copyAttributes();
				inputAttributes.removeAttributes(inputAttributes);
				inputAttributes.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
			}
			inputAttributes.addAttributes(attr);
			//System.err.println("inputAttr: " + inputAttributes);
		}
	}

	/**
	 * Sets the character attributes for selection of the specified editor
	 *
	 * @param editor
	 * @param attrs
	 */
	public static void setCharacterAttributes(JEditorPane editor, AttributeSet attrs) {
		setCharacterAttributes(editor, attrs, false);
	}

	/**
	 * Converts an html tag attribute list to a {@link Map}. For example, the String
	 * 'href="http://blah.com" target="_self"' becomes name-value pairs:<br>
	 * href > http://blah.com<br>
	 * target > _self
	 *
	 * @param atts
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map tagAttribsToMap(String atts) {
		Map attribs = new HashMap();
		StringTokenizer st = new StringTokenizer(atts.trim(), " ");
		String lastAtt = null;
		while (st.hasMoreTokens()) {
			String atVal = st.nextToken().trim();
			int equalPos = atVal.indexOf('=');
			if (equalPos == -1) {
				if (lastAtt == null) {
					//no equals char in this string
					break;
				}
				String lastVal = attribs.get(lastAtt).toString();
				attribs.put(lastAtt, lastVal + " " + atVal);
				continue;
			}
			String at = atVal.substring(0, equalPos);
			String val = atVal.substring(atVal.indexOf('=') + 1, atVal.length());
			if (val.startsWith("\"")) {
				val = val.substring(1, val.length());
			}
			if (val.endsWith("\"")) {
				val = val.substring(0, val.length() - 1);
			}
			attribs.put(at, val);
			lastAtt = at;
		}
		return attribs;
	}

	/**
	 * Converts a Color to a hex string in the format "#RRGGBB"
	 *
	 * @param color
	 * @return
	 */
	public static String colorToHex(Color color) {
		String colorstr = "#";
		// Red
		String str = Integer.toHexString(color.getRed());
		if (str.length() > 2) {
			str = str.substring(0, 2);
		}
		if (str.length() < 2) {
			colorstr += "0" + str;
		} else {
			colorstr += str;
		}
		// Green
		str = Integer.toHexString(color.getGreen());
		if (str.length() > 2) {
			str = str.substring(0, 2);
		}
		if (str.length() < 2) {
			colorstr += "0" + str;
		} else {
			colorstr += str;
		}
		// Blue
		str = Integer.toHexString(color.getBlue());
		if (str.length() > 2) {
			str = str.substring(0, 2);
		}
		if (str.length() < 2) {
			colorstr += "0" + str;
		} else {
			colorstr += str;
		}
		return colorstr;
	}

	/**
	 * Convert a "#FFFFFF" hex string to a Color. If the color specification is bad, an attempt will
	 * be made to fix it up.
	 */
	public static Color hexToColor(String value) {
		String digits;
		if (value.startsWith("#")) {
			digits = value.substring(1, Math.min(value.length(), 7));
		} else {
			digits = value;
		}
		String hstr = "0x" + digits;
		Color c;
		try {
			c = Color.decode(hstr);
		} catch (NumberFormatException nfe) {
			c = Color.BLACK; // just return black
		}
		return c;
	}

	/**
	 * Convert a color string such as "RED" or "#NNNNNN" or "rgb(r, g, b)" to a Color.
	 *
	 * @param str
	 * @return
	 */
	public static Color stringToColor(String str) {
		Color color;

		if (str.length() == 0) {
			color = Color.black;
		} else if (str.charAt(0) == '#') {
			color = hexToColor(str);
		} else if (str.equalsIgnoreCase("Black")) {
			color = hexToColor("#000000");
		} else if (str.equalsIgnoreCase("Silver")) {
			color = hexToColor("#C0C0C0");
		} else if (str.equalsIgnoreCase("Gray")) {
			color = hexToColor("#808080");
		} else if (str.equalsIgnoreCase("White")) {
			color = hexToColor("#FFFFFF");
		} else if (str.equalsIgnoreCase("Maroon")) {
			color = hexToColor("#800000");
		} else if (str.equalsIgnoreCase("Red")) {
			color = hexToColor("#FF0000");
		} else if (str.equalsIgnoreCase("Purple")) {
			color = hexToColor("#800080");
		} else if (str.equalsIgnoreCase("Fuchsia")) {
			color = hexToColor("#FF00FF");
		} else if (str.equalsIgnoreCase("Green")) {
			color = hexToColor("#008000");
		} else if (str.equalsIgnoreCase("Lime")) {
			color = hexToColor("#00FF00");
		} else if (str.equalsIgnoreCase("Olive")) {
			color = hexToColor("#808000");
		} else if (str.equalsIgnoreCase("Yellow")) {
			color = hexToColor("#FFFF00");
		} else if (str.equalsIgnoreCase("Navy")) {
			color = hexToColor("#000080");
		} else if (str.equalsIgnoreCase("Blue")) {
			color = hexToColor("#0000FF");
		} else if (str.equalsIgnoreCase("Teal")) {
			color = hexToColor("#008080");
		} else if (str.equalsIgnoreCase("Aqua")) {
			color = hexToColor("#00FFFF");
		} else {
			// sometimes get specified without leading #
			color = hexToColor(str);
		}
		return color;
	}

	/**
	 * Removes self-closing tags from xhtml for the benifit of {@link JEditorPane}
	 *
	 * <p>
	 * JEditorpane can't handle empty xhtml containers like &lt;br /&gt; or &lt;img /&gt;, so this
	 * method replaces them without the "/" as in &lt;br&gt;</p>
	 *
	 * @param html
	 * @return JEditorpane friendly html
	 */
	public static String jEditorPaneizeHTML(String html) {
		return html.replaceAll("(<\\s*\\w+\\b[^>]*)/(\\s*>)", "$1$2");
	}

	/**
	 * Helper method that prints out the contents of an {@link AttributeSet} to System.err for
	 * debugging
	 *
	 * @param attr
	 */
	public static void printAttribs(AttributeSet attr) {
		LOG.write(ERR, "----------------------------------------------------------------");
		LOG.write(ERR, attr.toString());
		Enumeration ee = attr.getAttributeNames();
		while (ee.hasMoreElements()) {
			Object name = ee.nextElement();
			Object atr = attr.getAttribute(name);
			LOG.write(ERR, name + " " + name.getClass().getName() + " | " + atr + " " + atr.getClass().getName());
		}
		LOG.write(ERR, "----------------------------------------------------------------");
	}

	private static class CharStyleToken {

		int offs;
		int len;
		AttributeSet attrs;
	}
}
