/**
 * code from https://github.com/furstenheim/copy-down
 *
 * The MIT License (MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package api.copydown;

import api.jsoup.nodes.Element;
import api.jsoup.nodes.Node;
import api.jsoup.nodes.TextNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * from https://github.com/furstenheim/copy-down/tree/master
 *
 * Main class of the package
 */
public class CopyDown {

	public enum BlockStyle {
		INDENTED,
		FENCED;
	}

	public CopyDown() {
		this.options = OptionsBuilder.anOptions().build();
		setUp();
	}

	public CopyDown(Options options) {
		this.options = options;
		setUp();
	}

	/**
	 * Accepts a HTML string and converts it to markdown
	 *
	 * Note, if LinkStyle is chosen to be REFERENCED the method is not thread safe.
	 *
	 * @param input html to be converted
	 * @return markdown text
	 */
	public String convert(String input) {
		references = new ArrayList<>();
		CopyNode copyRootNode = new CopyNode(input);
		String result = process(copyRootNode);
		return postProcess(result);
	}

	private Rules rules;
	private final Options options;
	private List<String> references = null;

	private void setUp() {
		rules = new Rules();
	}

	private static class Escape {

		String pattern;
		String replace;

		public Escape(String pattern, String replace) {
			this.pattern = pattern;
			this.replace = replace;
		}
	}
	private final List<Escape> escapes = Arrays.asList(
			new Escape("\\\\", "\\\\\\\\"),
			new Escape("\\*", "\\\\*"),
			new Escape("^-", "\\\\-"),
			new Escape("^\\+ ", "\\\\+ "),
			new Escape("^(=+)", "\\\\$1"),
			new Escape("^(#{1,6}) ", "\\\\$1 "),
			new Escape("`", "\\\\`"),
			new Escape("^~~~", "\\\\~~~"),
			new Escape("\\[", "\\\\["),
			new Escape("\\]", "\\\\]"),
			new Escape("^>", "\\\\>"),
			new Escape("_", "\\\\_"),
			new Escape("^(\\d+)\\. ", "$1\\\\. ")
	);

	private String postProcess(String output) {
		for (Rule rule : rules.rules) {
			if (rule.getAppend() != null) {
				output = join(output, rule.getAppend().get());
			}
		}
		return output.replaceAll("^[\\t\\n\\r]+", "").replaceAll("[\\t\\r\\n\\s]+$", "");
	}

	private String process(CopyNode node) {
		String result = "";
		for (Node child : node.element.childNodes()) {
			CopyNode copyNodeChild = new CopyNode(child, node);
			String rep = "";
			if (NodeUtils.isNodeType3(child)) {
				rep = copyNodeChild.isCode() ? ((TextNode) child).text() : escape(((TextNode) child).text());
			} else if (NodeUtils.isNodeType1(child)) {
				rep = replacementForNode(copyNodeChild);
			}
			result = join(result, rep);
		}
		return result;
	}

	private String replacementForNode(CopyNode node) {
		Rule rule = rules.findRule(node.element);
		String content = process(node);
		CopyNode.FlankingWhiteSpaces flankingWhiteSpaces = node.flankingWhitespace();
		if (flankingWhiteSpaces.getLeading().length() > 0 || flankingWhiteSpaces.getTrailing().length() > 0) {
			content = content.trim();
		}
		return flankingWhiteSpaces.getLeading() + rule.getReplacement().apply(content, node.element)
				+ flankingWhiteSpaces.getTrailing();
	}
	private static final Pattern LEADINGNEWLINEPATTERN = Pattern.compile("^(\n*)");
	private static final Pattern TRAILINGNEWLINEPATTERN = Pattern.compile("(\n*)$");

	private String join(String string1, String string2) {
		Matcher matcher = TRAILINGNEWLINEPATTERN.matcher(string1);
		matcher.find();
		Matcher leading = LEADINGNEWLINEPATTERN.matcher(string2);
		leading.find();
		int nl = Integer.min(2, Integer.max(leading.group().length(), matcher.group().length()));
		String nlj = String.join("", Collections.nCopies(nl, "\n"));
		return matcher.replaceAll("") + nlj + leading.replaceAll("");
	}

	private String escape(String string) {
		for (Escape escape : escapes) {
			string = string.replaceAll(escape.pattern, escape.replace);
		}
		return string;
	}

	class Rules {

		private List<Rule> rules;

		public Rules() {
			this.rules = new ArrayList<>();

			addRule("blankReplacement", new Rule((el) -> CopyNode.isBlank(el), (content, el)
					-> CopyNode.isBlock(el) ? "\n\n" : ""));
			addRule("paragraph", new Rule("p", (content, el) -> {
				return "\n\n" + content + "\n\n";
			}));
			addRule("br", new Rule("br", (content, el) -> {
				return options.br + "\n";
			}));
			addRule("heading", new Rule(new String[]{"h1", "h2", "h3", "h4", "h5", "h6"}, (content, element) -> {
				Integer hLevel = Integer.valueOf(element.nodeName().substring(1, 2));
				if (options.headingStyle == HeadingStyle.SETEXT && hLevel < 3) {
					String underline = String.join("", Collections.nCopies(content.length(), hLevel == 1 ? "=" : "-"));
					return "\n\n" + content + "\n" + underline + "\n\n";
				} else {
					return "\n\n" + String.join("", Collections.nCopies(hLevel, "#")) + " " + content + "\n\n";
				}
			}));
			addRule("blockquote", new Rule("blockquote", (content, element) -> {
				content = content.replaceAll("^\n+|\n+$", "");
				content = content.replaceAll("(?m)^", "> ");
				return "\n\n" + content + "\n\n";
			}));
			addRule("list", new Rule(new String[]{"ul", "ol"}, (content, element) -> {
				Element parent = (Element) element.parentNode();
				if (parent.nodeName().equals("li") && parent.child(parent.childrenSize() - 1) == element) {
					return "\n" + content;
				} else {
					return "\n\n" + content + "\n\n";
				}
			}));
			addRule("listItem", new Rule("li", (content, el) -> {
				content = content.replaceAll("^\n+", "") // remove leading new lines
						.replaceAll("\n+$", "\n") // remove trailing new lines with just a single one
						.replaceAll("(?m)\n", "\n    "); // indent
				String prefix = options.bulletListMaker + "   ";
				Element parent = (Element) el.parentNode();
				if (parent.nodeName().equals("ol")) {
					String start = parent.attr("start");
					int index = parent.children().indexOf(el);
					int parsedStart = 1;
					if (start.length() != 0) {
						try {
							parsedStart = Integer.parseInt(start);
						} catch (NumberFormatException e) {
							e.printStackTrace(System.err);
						}
					}
					prefix = String.valueOf(parsedStart + index) + ".  ";
				}
				return prefix + content
						+ (el.nextSibling() != null
						&& !Pattern.compile("\n$").matcher(content).find() ? "\n" : "");
			}));
			addRule("indentedCodeBlock", new Rule((el) -> {
				return options.codeBlockStyle == BlockStyle.INDENTED
						&& el.nodeName().equals("pre")
						&& el.childNodeSize() > 0
						&& el.childNode(0).nodeName().equals("code");
			}, (content, el) -> {
				return "\n\n    " + ((Element) el.childNode(0)).wholeText().replaceAll("\n", "\n    ");
			}));
			addRule("fencedCodeBock", new Rule((element) -> {
				return options.codeBlockStyle == BlockStyle.FENCED
						&& element.nodeName().equals("pre")
						&& element.childNodeSize() > 0
						&& element.childNode(0).nodeName().equals("code");
			}, (content, el) -> {
				String childClass = el.childNode(0).attr("class");
				if (childClass == null) {
					childClass = "";
				}
				Matcher languageMatcher = Pattern.compile("language-(\\S+)").matcher(childClass);
				String language = "";
				if (languageMatcher.find()) {
					language = languageMatcher.group(1);
				}

				String code;
				if (el.childNode(0) instanceof Element) {
					code = ((Element) el.childNode(0)).wholeText();
				} else {
					code = el.childNode(0).outerHtml();
				}

				String fenceChar = options.fence.substring(0, 1);
				int fenceSize = 3;
				Matcher fenceMatcher = Pattern.compile("(?m)^(" + fenceChar + "{3,})").matcher(content);
				while (fenceMatcher.find()) {
					String group = fenceMatcher.group(1);
					fenceSize = Math.max(group.length() + 1, fenceSize);
				}
				String fence = String.join("", Collections.nCopies(fenceSize, fenceChar));
				if (code.length() > 0 && code.charAt(code.length() - 1) == '\n') {
					code = code.substring(0, code.length() - 1);
				}
				return ("\n\n" + fence + language + "\n" + code
						+ "\n" + fence + "\n\n");
			}));

			addRule("horizontalRule", new Rule("hr", (content, el) -> {
				return "\n\n" + options.hr + "\n\n";
			}));
			addRule("inlineLink", new Rule((element) -> {
				return options.linkStyle == LinkStyle.INLINED
						&& element.nodeName().equals("a")
						&& element.attr("href").length() != 0;
			}, (content, element) -> {
				String href = element.attr("href");
				String title = cleanAttribute(element.attr("title"));
				if (title.length() != 0) {
					title = " \"" + title + "\"";
				}
				return "[" + content + "](" + href + title + ")";
			}));
			addRule("referenceLink", new Rule((element) -> {
				return options.linkStyle == LinkStyle.REFERENCED
						&& element.nodeName().equals("a")
						&& element.attr("href").length() != 0;
			}, (content, el) -> {
				String href = el.attr("href");
				String title = cleanAttribute(el.attr("title"));
				if (title.length() != 0) {
					title = " \"" + title + "\"";
				}
				String rep;
				String ref;
				switch (options.linkReferenceStyle) {
					case COLLAPSED:
						rep = "[" + content + "][]";
						ref = "[" + content + "]: " + href + title;
						break;
					case SHORTCUT:
						rep = "[" + content + "]";
						ref = "[" + content + "]: " + href + title;
						break;
					case DEFAULT:
					default:
						int id = references.size() + 1;
						rep = "[" + content + "][" + id + "]";
						ref = "[" + id + "]: " + href + title;
				}
				references.add(ref);
				return rep;
			}, () -> {
				String referenceString = "";
				if (!references.isEmpty()) {
					referenceString = "\n\n" + String.join("\n", references) + "\n\n";
				}
				return referenceString;
			}));
			addRule("emphasis", new Rule(new String[]{"em", "i"}, (content, element) -> {
				if (content.trim().length() == 0) {
					return "";
				}
				return options.emDelimiter + content + options.emDelimiter;
			}));
			addRule("strong", new Rule(new String[]{"strong", "b"}, (content, element) -> {
				if (content.trim().length() == 0) {
					return "";
				}
				return options.strongDelimiter + content + options.strongDelimiter;
			}));
			addRule("code", new Rule((el) -> {
				boolean hasSiblings = el.previousSibling() != null || el.nextSibling() != null;
				boolean isCodeBlock = el.parentNode().nodeName().equals("pre") && !hasSiblings;
				return el.nodeName().equals("code") && !isCodeBlock;
			}, (content, el) -> {
				if (content.trim().length() == 0) {
					return "";
				}
				String delimiter = "`";
				String leadingSpace = "";
				String trailingSpace = "";
				Pattern pattern = Pattern.compile("(?m)(`)+");
				Matcher matcher = pattern.matcher(content);
				if (matcher.find()) {
					if (Pattern.compile("^`").matcher(content).find()) {
						leadingSpace = " ";
					}
					if (Pattern.compile("`$").matcher(content).find()) {
						trailingSpace = " ";
					}
					int counter = 1;
					if (delimiter.equals(matcher.group())) {
						counter++;
					}
					while (matcher.find()) {
						if (delimiter.equals(matcher.group())) {
							counter++;
						}
					}
					delimiter = String.join("", Collections.nCopies(counter, "`"));
				}
				return delimiter + leadingSpace + content + trailingSpace + delimiter;
			}));
			addRule("img", new Rule("img", (content, element) -> {
				String alt = cleanAttribute(element.attr("alt"));
				String src = element.attr("src");
				if (src.length() == 0) {
					return "";
				}
				String title = cleanAttribute(element.attr("title"));
				String titlePart = "";
				if (title.length() != 0) {
					titlePart = " \"" + title + "\"";
				}
				return "![" + alt + "]" + "(" + src + titlePart + ")";
			}));
			addRule("default", new Rule((element -> true), (content, element)
					-> CopyNode.isBlock(element) ? "\n\n" + content + "\n\n" : content));
		}

		public Rule findRule(Node node) {
			for (Rule rule : rules) {
				if (rule.getFilter().test(node)) {
					return rule;
				}
			}
			return null;
		}

		private void addRule(String name, Rule rule) {
			rule.setName(name);
			rules.add(rule);
		}

		private String cleanAttribute(String attribute) {
			return attribute.replaceAll("(\n+\\s*)+", "\n");
		}
	}
}
