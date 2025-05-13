package storybook.tools.html;

import api.jsoup.Jsoup;
import api.jsoup.nodes.Document;
import api.jsoup.nodes.Element;
import api.jsoup.select.Elements;
import i18n.I18N;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLWriter;
import storybook.App;
import storybook.Const;
import storybook.Pref;
import storybook.tools.LOG;
import storybook.tools.TextUtil;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.FontUtil;
import storybook.ui.MainFrame;

public class Html {

	private static final String TT = "Html";

	/**
	 * static String, suffixed by: _B for starting the tag _E for ending the tag
	 */
	public static final String TYPE = "text/html",
			NL = "\n", // new ligne
			//DOCTYPE = "",//empty DOCTYPE
			DOCTYPE = "<!DOCTYPE html>" + NL,
			// alignment
			AL_CENTER = "text-align: center;",
			AL_LEFT = "text-align: left;",
			AL_RIGHT = "text-align: right;",
			// bold
			B_BEG = "<b>", B_END = "</b>",
			// html structures
			HTML_B = "<html>" + NL,
			HTML_B_LANG = "<html lang=\"" + Locale.getDefault().getLanguage() + "\">" + NL,
			HTML_E = "</html>" + NL,
			BODY_B = "<body>" + NL, BODY_E = "</body>" + NL,
			BR = "<br>" + NL,
			STYLE_B = "<style type=\"text/css\">\n" + NL, STYLE_E = "</style>" + NL,
			// color
			COLOR = "color",
			COLOR_BG = "background-color",
			DIV_B = "<div>", DIV_E = "</div>",
			EXT = ".html",
			// fonts
			FONT_FAMILY = "font-family",
			FONT_SIZE = "font-size",
			FONT_SIZE_DEFAULT = "html {font-size: "
			+ App.fonts.defGet().getSize() + ";}",
			FONT_STYLE = "font-style",
			FONT_WEIGHT = "font-weight",
			// head tags
			HEAD_B = "<head>" + NL, HEAD_E = "</head>" + NL,
			HR = "<hr>" + NL,
			I_B = "<i>", I_E = "</i>",
			LI_B = "<li>", LI_E = "</li>" + NL,
			//margins
			MARGIN = "margin",
			MARGIN_BOTTOM = "margin-bottom",
			MARGIN_LEFT = "margin-left",
			MARGIN_RIGHT = "margin-right",
			MARGIN_TOP = "margin-top",
			// meta
			META_CONTENT = "<meta name=\"keywords\" content=\"HTML\">" + NL,
			META_UTF8 = "<meta charset=\"utf-8\">" + NL,
			//paddings
			PADDING = "padding",
			PADDING_LEFT = "padding-left",
			PADDING_RIGHT = "padding-right",
			// paragraph
			P_B = "<p>",
			P_CENTER = "<p style=\"" + AL_CENTER + "\">",
			P_EMPTY = "<p></p>" + NL,
			P_E = "</p>" + NL,
			TABLE_B = "<table>" + NL, TABLE_E = "</table>" + NL,
			TABLE_STYLE = STYLE_B
			+ "table,td,th {"
			+ "border: 1px solid black;"
			+ "border-collapse: separate;"
			+ "border-spacing: 0px;"
			+ "}"
			+ STYLE_E,
			TD_B = "<td>", TD_E = "</td>" + NL,
			TEXT_ALIGN = "text-align",
			TR_B = "<tr>" + NL, TR_E = "</tr>" + NL,
			U_B = "<u>", U_E = "</u>",
			UL_B = "<ul>" + NL, UL_E = "</ul>" + NL;

	public static String emptyLines(int n) {
		StringBuilder p = new StringBuilder();
		for (int i = 0; i < n; i++) {
			p.append(BR);
		}
		return p.toString();
	}

	public static boolean isEmpty(String notes) {
		return htmlToText(notes).isEmpty();
	}

	/**
	 * get list of Body fragments max size=25000
	 *
	 * @param text
	 * @return
	 */
	public static List<String> getFragments(String text) {
		int max = 25000;
		List<String> list = new ArrayList<>();
		String[] frags = text.split("<p");
		List<String> l2 = new ArrayList<>();
		if (frags.length > 1) {
			for (String s : frags) {
				if (l2.toString().length() + ("<p" + s).length() > max) {
					list.add(l2.toString());
					l2 = new ArrayList<>();
					l2.add("<p" + s);
				}
			}
			list.add(l2.toString());
		} else {
			list.add(text);
		}
		return list;
	}

	public static String removeTag(String html, String tag) {
		Document doc = Jsoup.parse(html);
		for (Element element : doc.select(tag)) {
			element.remove();
		}
		String t = doc.body().outerHtml()
				.replace("<body>", "")
				.replace("</body>", "");
		if (t.startsWith(NL)) {
			t = t.substring(1);
		}
		if (t.endsWith(NL)) {
			t = t.substring(0, t.length() - 1);
		}
		return t.trim();
	}

	public static String removeClass(String html, String cl) {
		Document doc = Jsoup.parse(html);
		Elements elems = doc.getElementsByClass(cl);
		elems.remove();
		return doc.body().html();
	}

	public static String removeComments(String text, List<String> keys) {
		Document doc = Jsoup.parse("<html><body>" + text + "</body></html>");
		for (String key : keys) {
			Elements elems = doc.getElementsByClass("comment");
			for (int i = 0; i < elems.size(); i++) {
				Element el = elems.get(i);
				LOG.trace("el='" + el.toString() + "'");
				String href = el.attr("href");
				if (href.contains(key)) {
					LOG.trace("removing comment " + key + "='" + href + "'");
					el.remove();
					break;
				}
			}
		}
		return doc.body().html();
	}

	public static String removeComment(String text, String key) {
		Document doc = Jsoup.parse("<html><body>" + text + "</body></html>");
		Elements elems = doc.getElementsByAttributeValue("href", "#" + key);
		LOG.trace("search key=" + key);
		for (int i = 0; i < elems.size(); i++) {
			Element el = elems.get(i);
			LOG.trace("el='" + el.toString() + "'");
			String href = el.attr("href");
			if (href.equals("#" + key)) {
				LOG.trace("removing comment=" + href);
				el.remove();
				break;
			}
		}
		return doc.body().html();
	}

	/**
	 * get a clean HTML, with HEAD and default CSS
	 *
	 * @param text
	 * @return
	 */
	public static String toCleanHtml(String text) {
		//LOG.trace(TT + ".toCleanHtml(text=\"" + text + "\")");
		StringBuilder b = new StringBuilder(HTML_B);
		b.append(HEAD_B)
				.append(STYLE_B)
				.append(CSS.getDefault())
				.append(STYLE_E)
				.append(HEAD_E);
		b.append(intoTag("body", text));
		b.append(HTML_E);
		return b.toString();
	}

	public static String removeComments(String html) {
		Document doc = Jsoup.parse(html);
		for (Element element : doc.select("a.comment")) {
			element.remove();
		}
		String t = doc.body().outerHtml()
				.replace("<body>", "")
				.replace("</body>", "");
		if (t.startsWith(NL)) {
			t = t.substring(1);
		}
		if (t.endsWith(NL)) {
			t = t.substring(0, t.length() - 1);
		}
		return t.trim();
	}

	/**
	 * remove all specific tags
	 *
	 * @param html
	 * @param tag
	 * @return
	 */
	public static String removeSpecTag(String html, String tag) {
		String ht = html;
		while (ht.contains("<" + tag)) {
			int start = ht.indexOf("<" + tag), end = ht.indexOf(">", start);
			if (start != -1 && end != -1) {
				String ht1 = "";
				if (start > 0) {
					ht1 = ht.substring(0, start);
				}
				String ht2 = ht.substring(end + 1);
				ht = ht1 + ht2;
			}
		}
		if (ht.contains("</" + tag + ">")) {
			ht = ht.replace("</" + tag + ">", "");
		}
		return ht;
	}

	/**
	 * remove an attribute from all tags
	 *
	 * @param html
	 * @param attrib: the attribute to remove
	 * @return
	 */
	public static String removeAttribute(String html, String attrib) {
		String ht = html;
		Document doc = Jsoup.parse(html);
		Elements elems = doc.getAllElements();
		for (int i = 0; i < elems.size(); i++) {
			Element elem = elems.get(i);
			if (!elem.attr(attrib).isEmpty()) {
				elem.removeAttr(attrib);
			}
		}
		return doc.body().html();
	}

	/**
	 * clean the HTML by removing specific tags
	 *
	 * @param src
	 * @return
	 */
	public static String getClean(String src) {
		//LOG.trace(TT + ".getClean(src=" + src + ")");
		Document doc = Jsoup.parse("<p>" + src + "</p>");
		Elements elems = doc.body().getElementsByTag("form");
		for (int i = 0; i < elems.size(); i++) {
			elems.remove(elems.get(i));
		}
		String html = doc.body().html();
		// remove specific tags
		String tags[] = {
			"meta",
			"div",
			"embed",
			"span",
			"form",
			"button",
			"input",
			"label",
			"font"
		};
		for (String tag : tags) {
			html = Html.removeSpecTag(html, tag);
		}
		// remove specific attributes
		String attrs[] = {"onclick", "style", "name", "id"};
		for (String attr : attrs) {
			html = Html.removeAttribute(html, attr);
		}
		html = html.replace("<p><p>", "<p>");
		html = html.replace("</p></p>", "</p>");
		return html;
	}

	public static String forPrint(String s) {
		return "<html><head>"
				+ STYLE_B + "h1, h2, h3 {text-aligne: center}" + STYLE_E
				+ "</head>"
				+ intoTag("body", s, "style=\"font-size:12px;") + HTML_E;
	}

	private Html() {
		throw new IllegalStateException("Utility class Html");
	}

	/**
	 * return a cleaning HTML string
	 *
	 * @param html
	 * @return
	 */
	public static String getCleanHtml(String html) {
		StringBuilder buf = new StringBuilder();
		appendCleanHtml(buf, html);
		return buf.toString();
	}

	/**
	 * return a cleaning HTML string
	 *
	 * @param buf
	 * @param html
	 */
	public static void appendCleanHtml(StringBuilder buf, String html) {
		// remove new lines
		html = html.replace(NL, "");
		// replace empty div tags with paragraphs: "<div>\s*</div>"
		html = html.replaceAll(DIV_B + "\\s*" + DIV_E, P_EMPTY);
		// body>(.*)</body
		Pattern p = Pattern.compile("body>(.*)</body");
		Matcher m = p.matcher(html);
		if (m.find()) {
			html = m.group(1);
		}
		html = html.trim();
		// not in any tag at all, so build one
		boolean addPara = false;
		if (!html.startsWith("<")) {
			addPara = true;
			buf.append(P_B);
		}
		buf.append(html);
		if (addPara) {
			buf.append(P_E);
		}
	}

	/**
	 * return HTML content from a HTMLDocument
	 *
	 * @param doc
	 * @return
	 */
	public static String getContent(HTMLDocument doc) {
		try {
			StringWriter writer = new StringWriter();
			HTMLWriter htmlWriter = new HtmlBodyWriter(writer, doc);
			htmlWriter.write();
			return writer.toString();
		} catch (IOException | BadLocationException e) {
		}
		return "";
	}

	/**
	 * return href of a link
	 *
	 * @param html
	 * @return
	 */
	public static String findHref(String html) {
		Pattern pattern = Pattern
				.compile("(((file|http|https)://)|(www\\.))+(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(/[a-zA-Z0-9\\&amp;%_\\./-~-]*)?");
		Matcher matcher = pattern.matcher(html);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}

	public static String getAttribute(String key, String value) {
		return " " + key + "=\"" + value + "\"";
	}

	/**
	 * return a BODY with some attribute
	 *
	 * @param complement
	 * @return
	 */
	public static String getBody(String complement) {
		return "<body " + complement + ">";
	}

	/**
	 * return STYLE definition
	 *
	 * @param style
	 * @return
	 */
	public static String getStyle(String style) {
		return " style=\"" + style + "\"";
	}

	public static String nbsp(int nb) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < nb; i++) {
			b.append("&nbsp;");
		}
		return b.toString();
	}

	public static String intoHTML(String str) {
		return HTML_B + str + HTML_E;
	}

	public static String indent(int n) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < n; i++) {
			b.append("\t");
		}
		return b.toString();
	}

	/**
	 * return title/subtitle (H)
	 *
	 * @param level
	 * @param text
	 * @param style
	 * @return
	 */
	public static String intoH(Integer level, String text, String... style) {
		String h = "h" + level.toString();
		StringBuilder b = new StringBuilder("<").append(h);
		if (style != null && style.length > 0) {
			b.append(getStyle(style[0]));
		}
		b.append(">")
				.append(text)
				.append("</").append(h).append(">\n");
		return b.toString();
	}

	/**
	 * return a link (A) with optional name, link and text
	 *
	 * @param name: maybe empty
	 * @param href: maybe empty
	 * @param text
	 * @return
	 */
	public static String intoA(String name, String href, String text) {
		return intoA(name, href, text, "");
	}

	/**
	 * return a link (A) with optional name, link and text
	 *
	 * @param name: maybe empty
	 * @param href: maybe empty
	 * @param text
	 * @param cpl : complement like "class=...", or "style=..."
	 * @return
	 */
	public static String intoA(String name, String href, String text, String cpl) {
		StringBuilder b = new StringBuilder("<a");
		if (!name.isEmpty()) {
			b.append(getAttribute("name", name));
		}
		if (!href.isEmpty()) {
			b.append(getAttribute("href", href));
		}
		if (!cpl.isEmpty()) {
			b.append(cpl);
		}
		if (!text.isEmpty()) {
			b.append(">").append(text).append("</a>");
		} else {
			b.append("/>");
		}
		return b.toString();
	}

	/**
	 * return a LI object
	 *
	 * @param value
	 * @return
	 */
	public static String intoLI(String value) {
		return intoTag("li", value);
	}

	/**
	 * return a paragraph (P) object
	 *
	 * @param text
	 * @param style
	 * @return
	 */
	public static String intoP(String text, String... style) {
		StringBuilder b = new StringBuilder("<p");
		if (style != null && style.length > 0) {
			b.append(" style=\"");
			for (String st : style) {
				b.append(st);
				if (!st.endsWith(";")) {
					b.append(";");
				}
			}
			b.append("\"");
		}
		b.append(">").append(text).append(P_E);
		return b.toString();
	}

	/**
	 * return a paragraph (P) object
	 *
	 * @param text
	 * @param style
	 * @return
	 */
	public static String intoPcenter(String text) {
		return intoP(text, AL_CENTER);
	}

	/**
	 * return an italic (I) object
	 *
	 * @param text
	 * @return
	 */
	public static String intoI(String text) {
		return intoTag("i", text);
	}

	/**
	 * return an uderlined (U) object
	 *
	 * @param text
	 * @return
	 */
	public static String intoU(String text) {
		return intoTag("u", text);
	}

	/**
	 * return a bold (B) object
	 *
	 * @param text
	 * @return
	 */
	public static String intoB(String text) {
		return intoTag("b", text);
	}

	/**
	 * return an image tag
	 *
	 * @param src
	 * @param title
	 * @param alt
	 * @param height
	 * @return
	 */
	public static String intoIMG(String src, String title, String alt, Integer... height) {
		StringBuilder b = new StringBuilder("<img");
		b.append(" src=\"").append(src).append("\"");
		if (!title.isEmpty()) {
			b.append(" title=\"").append(title).append("\"");
		}
		if (!alt.isEmpty()) {
			b.append(" alt=\"").append(alt).append("\"");
		} else {
			b.append(" alt=\"image\"");
		}
		if (height != null && height.length > 0) {
			b.append(" width=\"").append(height[0].toString()).append("\"");
		}
		b.append(">");
		return b.toString();
	}

	/**
	 * return a fonted text with a given size
	 *
	 * @param size
	 * @param text
	 * @return
	 */
	public static String intoFont(int size, String text) {
		return "<font size=\"" + size + "\">" + text + "</font>";
	}

	/**
	 * return a fonted text with a given color
	 *
	 * @param color
	 * @param text
	 * @return
	 */
	public static String intoFontColor(String color, String text) {
		return "<font color=\"" + color + "\">" + text + "</font>";
	}

	/**
	 * return a small text
	 *
	 * @param text
	 * @return
	 */
	public static String intoSmall(String text) {
		return intoTag("small", text);
	}

	/**
	 * return a row (TR) for a TABLE
	 *
	 * @param text
	 * @param complement
	 * @return
	 */
	public static String intoTR(String text, String... complement) {
		return intoTag("tr", text, complement);
	}

	/**
	 * return a cell (TD) for a TABLE
	 *
	 * @param text
	 * @param complement
	 * @return
	 */
	public static String intoTD(String text, String... complement) {
		return intoTag("td", text, complement);
	}

	/**
	 * return a tagged text
	 *
	 * @param tag
	 * @param value
	 * @param opt: complement
	 * @return
	 */
	public static String intoTag(String tag, String value, String... opt) {
		StringBuilder r = new StringBuilder("<" + tag);
		if (opt != null && opt.length > 0) {
			for (String s : opt) {
				r.append(" ").append(s);
			}
		}
		r.append(">")
				.append(value)
				.append("</").append(tag).append(">\n");
		return r.toString();
	}

	/**
	 * return a text enclose into a TABLE
	 *
	 * @param text
	 * @return
	 */
	public static String wrapIntoTable(String text) {
		return wrapIntoTable(text, 200);
	}

	/**
	 * return a text enclose into a limited width TABLE
	 *
	 * @param text
	 * @param width
	 * @return
	 */
	public static String wrapIntoTable(String text, int width) {
		return HTML_B
				+ "<table width=\"" + width + "\">"
				+ intoTR(intoTD(text))
				+ TABLE_E;
	}

	/**
	 * compare two HTML text strings
	 *
	 * @param html1
	 * @param html2
	 * @return
	 */
	public static boolean equalsHtml(String html1, String html2) {
		String s1 = Jsoup.parse(html1).text();
		String s2 = Jsoup.parse(html2).text();
		return (s1.equals(s2));
	}

	/**
	 * return text len without tags and UTF8 codes
	 *
	 * @param html
	 * @return the len
	 */
	public static int getTextSize(String html) {
		if (html == null) {
			return (0);
		}
		String x = htmlToText(html).replaceAll("&#.*;", "?");
		return (x.length());
	}

	public static String htmlToText(String html) {
		return htmlToText(html, false);
	}

	/**
	 * convert a HTML String into truncated plain text
	 *
	 * @param html
	 * @param len
	 * @return
	 */
	public static String htmlToText(String html, int len) {
		if (html == null) {
			return "";
		}
		String txt = html.replaceAll("<[/]?img[^>]*>", "[" + I18N.getMsg("image") + "]");
		return TextUtil.ellipsize(htmlToText(txt, true), len);
	}

	/**
	 * remove all tags from the HTML
	 *
	 * @param html
	 * @return
	 */
	public static String htmlToCleanText(String html) {
		if (html == null || html.isEmpty()) {
			return "";
		}
		return html.replaceAll("<[^>]*>", "").replace("◘", "");
	}

	/**
	 * convert HTML to plain text
	 *
	 * @param src String to convert
	 * @param preserveNewLines true for preserving new lines char
	 * @return
	 */
	public static String htmlToText(String src, boolean preserveNewLines) {
		if (src == null) {
			return ("");
		}
		String html = src;
		if (!preserveNewLines) {
			html = Jsoup.parse(html).text();
		} else {
			html = Jsoup.parse(html).wholeText();
		}
		html = html.replace("\n\n", NL);
		while (html.startsWith(NL)) {
			html = html.substring(1);
		}
		html = TextUtil.trimText(html);
		while (html.endsWith(NL)) {
			html = html.substring(0, html.length() - 1);
		}
		return html;
	}

	/**
	 * convert any special chars to hexadecimal codes
	 *
	 * @param text
	 * @return
	 */
	public static String textToHtmlCode(String text) {
		String out = "";
		for (char ch : text.toCharArray()) {
			out += (ch < 128 ? ch : "&#" + (int) ch + ";");
		}
		return (out);
	}

	/**
	 * convert plain text to Html, inserting tags like P or BR
	 *
	 * @param text
	 * @return
	 */
	public static String textToHTML(String text) {
		if (text == null) {
			return P_EMPTY;
		}
		int length = text.length();
		boolean prevSlashR = false;
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < length; i++) {
			char ch = text.charAt(i);
			switch (ch) {
				case '\r':
					if (prevSlashR) {
						out.append(BR);
					}
					prevSlashR = true;
					break;
				case '\n':
					prevSlashR = false;
					out.append(BR);
					break;
				case '"':
					if (prevSlashR) {
						out.append(BR);
						prevSlashR = false;
					}
					out.append("&quot;");
					break;
				case '<':
					if (prevSlashR) {
						out.append(BR);
						prevSlashR = false;
					}
					out.append("&lt;");
					break;
				case '>':
					if (prevSlashR) {
						out.append(BR);
						prevSlashR = false;
					}
					out.append("&gt;");
					break;
				case '&':
					if (prevSlashR) {
						out.append(BR);
						prevSlashR = false;
					}
					out.append("&amp;");
					break;
				default:
					if (prevSlashR) {
						out.append(BR);
						prevSlashR = false;
					}
					out.append(ch);
					break;
			}
		}
		return (out.toString());
	}

	/**
	 * return a HTML string into a row (TR) with 2 columns (TD)
	 *
	 * @param key
	 * @param val
	 * @return
	 */
	public static String getRow2Cols(String key, String val) {
		return TR_B + TD_B + I18N.getMsg(key) + TD_E + TD_B + val + TD_E + TR_E;
	}

	public static String getRow2Cols(String key, Integer val) {
		return TR_B + TD_B + I18N.getMsg(key) + TD_E + TD_B + String.format("%,d", val) + TD_E + TR_E;
	}

	/**
	 * return a SPAN color (a 4 colored spaces)
	 *
	 * @param clr
	 * @return
	 */
	public static String getColorSpan(Color clr, int nb) {
		String htmlClr = (clr == null ? "white" : "#" + ColorUtil.getHexName(clr));
		StringBuilder buf = new StringBuilder("<span style=\"");
		if (clr != null) {
			buf.append("background-color:").append(htmlClr).append(";");
		}
		buf.append("\">");
		for (int i = 0; i < nb; i++) {
			buf.append("&nbsp;");
		}
		buf.append("</span>");
		return buf.toString();
	}

	/**
	 * return a SPAN color (a 4 colored spaces)
	 *
	 * @param clr
	 * @return
	 */
	public static String getColorSpan(Color clr) {
		return getColorSpan(clr, 4);
	}

	/**
	 * return a colored bold title
	 *
	 * @param clr: the color
	 * @param title: the text title to colorize
	 * @return
	 */
	public static String getColoredTitle(Color clr, String title) {
		String htmlClr = (clr == null ? "white" : ColorUtil.getHexName(clr));
		String buf = "<div style=\"padding-top:2pt;padding-bottom:2pt;"
				+ "padding-left:4pt;padding-right:4pt;margin-bottom:2pt;";
		if (clr != null) {
			buf += "background-color:" + htmlClr + ";";
		}
		if (clr != null && ColorUtil.isDark(clr)) {
			buf += "color:white;";
		}
		buf += "\">" + intoB(title) + DIV_E;
		return (buf);
	}

	/**
	 * return the HEAD TITLE
	 *
	 * @param title
	 * @return
	 */
	public static String getHeadTitle(String title) {
		return intoTag("title", title) + NL;
	}

	/**
	 * return a RED colored string
	 *
	 * @param str
	 * @return
	 */
	public static String getWarning(String str) {
		return intoFontColor("red", intoB(str));
	}

	/**
	 * return a horizontal rule (HR)
	 *
	 * @return
	 */
	public static String getHr() {
		return "<hr style=\"margin:10pt\" />";
	}

	private static final String REG_CRLF = "(\r\n|\r|\n|\n\r)";

	/**
	 * return a formatted warning text colored in red
	 *
	 * @param buf
	 * @param warning
	 * @return
	 */
	public static String appendFormatedWarning(String buf, String warning) {
		String str = warning.replaceAll(REG_CRLF, "<br>");
		if (str.isEmpty()) {
			return (buf);
		}
		return ("<div style=\"color:red\">" + str + DIV_E);
	}

	/**
	 * return a formatted description
	 *
	 * @param buf
	 * @param descr
	 * @param shorten
	 * @return
	 */
	public static String appendFormatedDescr(String buf, String descr, boolean shorten) {
		String str = descr.replaceAll(REG_CRLF, "<br>");
		if (str.isEmpty()) {
			return (buf);
		}
		String ret = buf;
		if (shorten) {
			ret += "<div style=\"width:300pt\">" + TextUtil.ellipsize(str, 300);
		} else {
			ret += DIV_B + str;
		}
		return (ret + DIV_E);
	}

	/**
	 * return a formatted Notes
	 *
	 * @param buf
	 * @param notes
	 * @param shorten
	 * @return
	 */
	public static String appendFormatedNotes(String buf, String notes, boolean shorten) {
		String str = notes.replaceAll(REG_CRLF, "<br>");
		if (str.isEmpty()) {
			return (buf);
		}
		String ret = "<hr style=\"margin:5pt\"/>";
		if (shorten) {
			ret += "<div style=\"width:300pt\">" + TextUtil.ellipsize(str, 300);
		} else {
			ret += DIV_B + str;
		}
		return (ret + DIV_E);
	}

	/**
	 * return a formatted informations
	 *
	 * @param buf
	 * @param metaInfos
	 * @return
	 */
	public static String appendFormatedMetaInfos(String buf, String metaInfos) {
		String str = metaInfos.replaceAll(REG_CRLF, "<br>");
		if (str.isEmpty()) {
			return (buf);
		}
		return ("<hr style=\"margin:5pt;\"/>" + DIV_B + str + DIV_E);
	}

	/**
	 * return a subtitle text string, level depends upon nb of points
	 *
	 * @param lib
	 * @return
	 */
	public static String getHtag(String lib) {
		String[] x = lib.split("\\.");
		int n = 2 + x.length;
		if (n > 6) {
			n = 6;
		}
		String t = lib;
		if (t.startsWith(".")) {
			t = lib.substring(1);
		}
		if (t.contains(". ")) {
			t = t.substring(t.indexOf(". ") + 2);
		}
		return ("<h" + n + ">" + t + "</h" + n + ">\n");
	}

	/**
	 * return an IMG object
	 *
	 * @param src
	 * @param alt
	 * @param width
	 * @param height
	 * @return
	 */
	public static String getImg(String src, String alt, String width, String height) {
		StringBuilder b = new StringBuilder("<img");
		b.append(" src=\"");
		b.append(src);
		b.append("\"");
		if (!alt.isEmpty()) {
			b.append(" alt=\"").append(alt).append("\"");
		}
		if (!width.isEmpty()) {
			b.append(" width=\"").append(width).append("\"");
		}
		if (!height.isEmpty()) {
			b.append(" height=\"").append(height).append("\"");
		}
		b.append(">");
		return b.toString();
	}

	/**
	 * return an indentation
	 *
	 * @param lev
	 * @return
	 */
	public static String getIndent(int lev) {
		String r = "";
		for (int i = 0; i < lev; i++) {
			r += "&nbsp;&nbsp;&nbsp;&nbsp;";
		}
		return (r);
	}

	/**
	 * return HTML Font definition
	 *
	 * @param font
	 * @return
	 */
	public static String fontToHtml(Font font) {
		String r = "style=\""
				+ FONT_FAMILY + ": " + font.getFontName() + ";"
				+ FONT_SIZE + ": " + font.getSize() + "pt;"
				+ "\"";
		return (r);
	}

	/**
	 * return a formatted key,value into one row (TR) of two columns (TD)
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public static String keyToHtml(String key, Integer value) {
		return keyToHtml(key, value.toString());
	}

	/**
	 * return a formatted key,value into one row (TR) of two columns (TD)
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public static String keyToHtml(String key, boolean value) {
		return keyToHtml(key, (I18N.getMsg(value ? "yes" : "no")));
	}

	/**
	 * return a formatted key,value into one row (TR) of two columns (TD)
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public static String keyToHtml(String key, String value) {
		StringBuilder b = new StringBuilder();
		b.append(TR_B);
		b.append("<td valign=\"top\">")
				.append(intoB(I18N.getMsg(key)))
				.append(TD_E).append(TD_B);
		if (value == null) {
			b.append("null");
		} else if (value.isEmpty()) {
			b.append(I18N.getMsg("empty"));
		} else {
			b.append(value);
		}
		b.append(TD_E).append(TR_E);
		return (b.toString());
	}

	/**
	 * clean HTML for XHTML
	 *
	 * @param html: string to check/transform
	 * @return checked String
	 */
	public static String toXhtml(final String html) {
		final Document document = Jsoup.parse(html);
		document.outputSettings().syntax(api.jsoup.nodes.Document.OutputSettings.Syntax.xml);
		return document.body().html();
	}

	/**
	 * clean HTML for a XHTML body
	 *
	 * @param html: string to check/transform
	 * @return checked String
	 */
	public static String toXhtmlBody(final String html) {
		final Document document = Jsoup.parse(html);
		document.outputSettings().syntax(api.jsoup.nodes.Document.OutputSettings.Syntax.xml);
		return document.body().html();
	}

	public static final int EM_REMOVETAGS = 0, EM_REMOVEALL = 1, EM_LEFTASIS = 2;

	/**
	 * remove or set color EM tag
	 *
	 * @param text
	 * @param hi:<ul>
	 * <li>0=simply remove EM tags</li>
	 * <li>1=remove EM (tag+text)</li>
	 * <li>2=set EM tags to yellow one</li>
	 * </ul>
	 * @return
	 */
	public static String emTag(String text, int hi) {
		switch (hi) {
			case 0:
				return (text.replaceAll("<em.*>", "").replace("</em>", ""));
			case 1:
				return (text.replaceAll("<em.*>.*</em>", ""));
			case 2:
				return text.replace("<em>", "<em style=\"background: yellow;font-style: normal;\">");
			default:
				break;
		}
		return (text);
	}

	/**
	 * return a replacing tag
	 *
	 * @param text : HTML code to search
	 * @param tag : tag to replace
	 * @param by : replacing tag
	 * @return : new HTML code
	 */
	public static String replaceTags(String text, String tag, String by) {
		if (text == null || text.isEmpty()) {
			return text;
		}
		String s = text.replaceAll("<" + tag.toUpperCase(), "<" + by.toUpperCase());
		s = s.replaceAll("</" + tag.toUpperCase(), "</" + by.toUpperCase());
		s = s.replaceAll("<" + tag.toLowerCase(), "<" + by.toLowerCase());
		return s.replace("</" + tag.toLowerCase(), "</" + by.toLowerCase());
	}

	/**
	 * tranform each fixed "file://" by a relative path
	 *
	 * @param html
	 * @param path
	 * @return
	 */
	public static String fileToRel(String html, String path) {
		if (!html.contains("file:")) {
			return html;
		}
		String r = "file://" + path;
		String s = html.replace(r, "file:..");
		return s;
	}

	/**
	 * tranform each relative path by a fixed "file://"
	 *
	 * @param html
	 * @param path
	 * @return
	 */
	public static String fileToPath(String html, String path) {
		if (!html.contains("file:")) {
			return html;
		}
		String r = "file:..";
		return html.replace(r, "file://" + path);
	}

	/**
	 * get HTML style for Font mono
	 *
	 * @param mainFrame
	 * @return
	 */
	public static String getFontMono(MainFrame mainFrame) {
		Font f = FontUtil.getFont(App.preferences.getString(Pref.KEY.FONT_MONO, Const.FONT_MONO));
		return Html.getStyle(Html.FONT_FAMILY + ": " + f.getFamily() + "; "
				+ Html.FONT_SIZE + ": " + f.getSize() + "pt;");
	}

	/**
	 * changing the images links for the local directory
	 *
	 * @param path : the local directory to set
	 * @param html : the HTML source String
	 * @return
	 */
	public static String changeLinks(String path, String html) {
		if (!html.contains("src=\"file:")) {
			return html;
		}
		//LOG.trace(TT+"changingLinks(path="+path+", html="+html);
		Document doc = Jsoup.parse(html);
		Elements links = doc.select("img[src]");
		for (Element link : links) {
			String oldSrc = link.attr("src");
			if (oldSrc.startsWith("file:")) {
				try {
					URL urlo = new URL(oldSrc);
					File f = Paths.get(urlo.toURI()).toFile();
					if (!f.exists()) {
						String newSrc = path + File.separator + "Images" + File.separator + f.getName();
						File fx = new File(newSrc);
						if (fx.exists()) {
							URL url = Paths.get(fx.getAbsolutePath()).toUri().toURL();
							link.attr("src", url.toString());
						}
					}
				} catch (MalformedURLException | URISyntaxException ex) {
					LOG.err(TT + "changeLinks(path=" + path + ", html) error", ex);
				}
			}
		}
		return doc.body().html();
	}

	/**
	 * restore for linked file like images
	 *
	 * @param text
	 *
	 * @return the html String replacing local image links by workingDir+"Images"
	 */
	public static String checkImages(MainFrame mainFrame, String text) {
		//LOG.trace(TT + ".checkLinks(text=\"" + text + "\")");
		if (!text.contains("<img")) {
			return text;
		}
		String html = text;
		api.jsoup.nodes.Document jsoup = Jsoup.parse(html);
		for (api.jsoup.nodes.Element el : jsoup.body().children()) {
			if (el.tagName().equals("img")) {
				String imgDir = mainFrame.getImageDir();
				File dir = new File(imgDir);
				if (!dir.exists()) {
					dir.mkdir();
				}
				String src = el.attr("src");
				if (src.startsWith("file://")) {
					src = src.replace("file://", "");
					File f = new File(src);
					String dest = imgDir + File.separator + f.getName();
					File fd = new File(dest);
					if (!src.startsWith(imgDir)) {
						if (!fd.exists()) {
							try {
								// copy file from source to dest image
								Files.copy(f.toPath(), fd.toPath());
							} catch (IOException ex) {
								// abnormal copy, dont report it
							}
						}
						el.attr("src", "file://" + fd.getAbsolutePath());
					}
				}
			} else if (!el.getAllElements().isEmpty()) {
				checkImages(mainFrame, el.html());
			}
		}
		return jsoup.body().html();
	}

}
