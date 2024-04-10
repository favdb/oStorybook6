/**
 * Project HtmlToLatex from SourceForge
 * https://sourceforge.net/projects/htmltolatex/
 *
 * original Author : Michal Kebrt
 * (contains Word-to-LaTeX (Word-to-XML) convertor, mkrss (RSS reader)
 * and a couple of other programs)
 * original licence is GNU/GPL (unknown version)
 * last modified date of the original project : 2012-09-19
 *
 * adaptation : FaVdB (c) 2023
 *
 */
package storybook.exim.doc.LATEX;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import storybook.exim.doc.LATEX.css.CSS;
import storybook.exim.doc.LATEX.parser.HtmlElementEnd;
import storybook.exim.doc.LATEX.parser.HtmlElementStart;
import storybook.exim.doc.LATEX.parser.IoOutputFileWriter;
import storybook.exim.doc.LATEX.parser.IoOutputStringWriter;
import storybook.exim.doc.LATEX.parser.IoWriter;

/**
 * Class which converts HTML into LaTeX format. Plain HTML elements are converted using
 * {@link LatexConverter#commonElementStart(ElementStart) commonElementStart()} and
 * {@link LatexConverter#commonElementEnd(ElementEnd, ElementStart) commonElementEnd()} methods.
 * Elements requiring special care during the conversion are converted by calling special methods
 * like {@link LatexConverter#tableRowStart(ElementStart) tableRowStart()
 * }.
 */
public class LatexConverter {

	public enum LK {
		FOOTNOTES,
		BIBLIO,
		HYPERTEX,
		IGNORE;

		@Override
		public String toString() {
			return name();
		}
	}
	private final LatexConf _config;
	private final IoWriter _writer;
	private int _countLeaveTextElements = 0;
	private int _countIgnoreContentElements = 0;
	private boolean _firstCell = true;
	private boolean _firstRow = true;
	private boolean _printBorder = false;
	/**
	 * Document's bibliography. <br />
	 * key : bibitem name <br />
	 * value : bibitem description
	 */
	private final HashMap<String, String> _bibliography = new HashMap<>(10);

	public LatexConverter(String cssFile) throws Exception {
		_config = new LatexConf(cssFile);
		_writer = new IoOutputStringWriter();
	}

	/**
	 * Opens the output file.
	 *
	 * @param outputFile output LaTeX file
	 * @param programInput
	 * @throws java.lang.Exception
	 */
	public LatexConverter(File outputFile, Latex programInput) throws Exception {
		_config = new LatexConf(programInput);
		_writer = new IoOutputFileWriter(outputFile);
	}

	public IoWriter getWriter() {
		return _writer;
	}

	/**
	 * Closes the output file.
	 */
	public void destroy() {
		_writer.close();
	}

	/**
	 * Called when HTML start element is reached and special method for the element doesn't exist.
	 *
	 * @param element HTML start tag
	 * @throws IOException output error occurs
	 */
	public void commonElementStart(HtmlElementStart element) throws IOException, Exception {
		LatexConf.ElemConfigItem item = _config.getElement(element.getElementName());
		if (item.leaveText()) {
			++_countLeaveTextElements;
		}
		if (item.ignoreContent()) {
			++_countIgnoreContentElements;
		}
		String str = item.getStart();
		if (str.equals("")) {
			return;
		}
		_writer.write(str);
	}

	/**
	 * Called when HTML end element is reached and special method for the element doesn't exist.
	 *
	 * @param element corresponding end tag
	 * @param es start tag
	 * @throws IOException output error occurs
	 */
	public void commonElementEnd(HtmlElementEnd element, HtmlElementStart es) throws IOException, Exception {
		LatexConf.ElemConfigItem item = _config.getElement(element.getElementName());
		if (item.leaveText()) {
			--_countLeaveTextElements;
		}
		if (item.ignoreContent()) {
			--_countIgnoreContentElements;
		}
		String str = item.getEnd();
		if (str.equals("")) {
			return;
		}
		_writer.write(str);
		processAttributes(es);
	}

	/**
	 * Called when text content is reached in the input HTML document.
	 *
	 * @param str text content reached
	 * @throws IOException when output error occurs
	 */
	public void characters(String str) throws IOException {
		if (_countLeaveTextElements == 0) {
			str = str.replace("\n", " ").replace("\t", " ");
		}

		if (str.equals("") || str.trim().equals("")) {
			return;
		}

		if (_countIgnoreContentElements > 0) {
			return;
		}

		if (_countLeaveTextElements == 0) {
			str = convertCharEntitites(convertLaTeXSpecialChars(str));
		} else {
			str = convertCharEntitites(str);
		}
		_writer.write(str);
	}

	/**
	 * Called when comment is reached in the input HTML document.
	 *
	 * @param comment comment (without &lt;!-- and --&gt;)
	 * @throws IOException when output error occurs
	 */
	public void comment(String comment) throws IOException {
		// is it comment for LaTeX
		if (comment.trim().toLowerCase().startsWith("latex:")) {
			comment = comment.trim();
			comment = comment.substring(6, comment.length());
			_writer.write(comment + "\n");
			return;
		}
		comment = "% " + comment;
		comment = "\n" + comment.replace("\n", "\n% ");
		comment += "\n";

		_writer.write(comment);
	}

	/**
	 * Converts LaTeX special characters (ie. '{') to LaTeX commands.
	 *
	 * @param str input string
	 * @return converted string
	 */
	private String convertLaTeXSpecialChars(String str) {
		str = str.replace("\\", "@-DOLLAR-\\backslash@-DOLLAR-")
				.replace("&#", "&@-HASH-")
				.replace("$", "\\$")
				.replace("#", "\\#")
				.replace("%", "\\%")
				.replace("~", "\\textasciitilde")
				.replace("_", "\\_")
				.replace("^", "\\textasciicircum")
				.replace("{", "\\{")
				.replace("}", "\\}")
				.replace("@-DOLLAR-", "$")
				.replace("@-HASH-", "#");

		return str;
	}

	/**
	 * Converts HTML character entities to LaTeX commands.
	 *
	 * @param str input string
	 * @return converted string
	 */
	private String convertCharEntitites(String str) {
		StringBuilder entity = new StringBuilder("");
		int len = str.length();
		boolean addToBuffer = false;
		for (int i = 0; i < len; ++i) {
			// new entity started
			if (str.charAt(i) == '&') {
				addToBuffer = true;
				entity.delete(0, entity.length());
				continue;
			}
			if (addToBuffer && (str.charAt(i) == ';')) {
				// find symbol
				try {
					String repl = "";
					boolean ok = true;
					if (entity.charAt(0) == '#') {
						try {
							Integer entityNum;
							if ((entity.charAt(1) == 'x') || entity.charAt(1) == 'X') {
								entityNum = Integer.valueOf(entity.substring(2, entity.length()), 16);
							} else {
								entityNum = Integer.valueOf(entity.substring(1, entity.length()));
							}
							repl = _config.getChar(entityNum);
						} catch (NumberFormatException ex) {
							System.out.println("Not a number in entity.");
							ok = false;
						}
					} else {
						repl = _config.getChar(entity.toString());
					}
					if (ok) {
						str = str.replace("&" + entity.toString() + ";", repl);
						len = str.length();
						i += repl.length() - (entity.length() + 2);
					}
				} catch (Exception e) {
					System.out.println(e.toString());
				}
				addToBuffer = false;
				entity.delete(0, entity.length());
				continue;
			}
			if (addToBuffer) {
				entity.append(str.charAt(i));
			}
		}

		return str;
	}

	/**
	 * Processes HTML elements' attributes. "Title" and "cite" attributes are converted to
	 * footnotes.
	 *
	 * @param element HTML start tag
	 * @throws IOException when output error occurs
	 */
	private void processAttributes(HtmlElementStart element) throws IOException {
		HashMap<String, String> map = element.getAttributes();
		if (element.getElementName().equals("a")) {
			return;
		}
		if (map.get("title") != null) {
			_writer.write("\\footnote{" + map.get("title") + "}");
		}
		if (map.get("cite") != null) {
			_writer.write("\\footnote{" + map.get("cite") + "}");
		}
	}

	/**
	 * Prints CSS style converted to LaTeX command. Called when HTML start element is reached.
	 *
	 * @param e HTML start element
	 * @throws IOException when output error occurs
	 */
	public void cssStyleStart(HtmlElementStart e) throws IOException {
		CSS[] styles = findStyles(e);
		for (int i = 0; i < styles.length; ++i) {
			if (styles[i] == null) {
				continue;
			}
			if (_config.getMakeCmdsFromCSS()) {
				_writer.write(_config.getCmdStyleName(styles[i].getName()) + "{");
			} else {
				_writer.write(styles[i].getStart());
			}
		}
	}

	/**
	 * Prints CSS style converted to LaTeX command. Called when HTML end element is reached.
	 *
	 * @param e corresponding HTML start element
	 * @throws IOException when output error occurs
	 */
	public void cssStyleEnd(HtmlElementStart e) throws IOException {
		CSS[] styles = findStyles(e);
		for (int i = styles.length - 1; i >= 0; --i) {
			if (styles[i] == null) {
				continue;
			}
			if (_config.getMakeCmdsFromCSS()) {
				_writer.write("}");
			} else {
				_writer.write(styles[i].getEnd());
			}
		}
	}

	/**
	 * Finds styles for the specified element.
	 *
	 * @param e HTML element
	 * @return array with styles in this order: element name style, 'class' style, 'id' style (if
	 * style not found null is stored in the array)
	 */
	private CSS[] findStyles(HtmlElementStart e) {
		try {
			if (_config.getElement(e.getElementName()).ignoreStyles()) {
				return null;
			}
		} catch (Exception ex) {
		}
		String[] styleNames = {e.getElementName(), "", ""};
		CSS[] styles = {null, null, null};
		CSS style;
		if (e.getAttributes().get("class") != null) {
			styleNames[1] = e.getAttributes().get("class");
		}
		if (e.getAttributes().get("id") != null) {
			styleNames[2] = e.getAttributes().get("id");
		}
		if ((style = _config.findStyle(styleNames[0])) != null) {
			styles[0] = style;
		}
		if ((style = _config.findStyleClass(styleNames[1], e.getElementName())) != null) {
			styles[1] = style;
		}
		if ((style = _config.findStyleId(styleNames[2], e.getElementName())) != null) {
			styles[2] = style;
		}
		return styles;
	}

	/**
	 * Called when A start element is reached.
	 *
	 * @param e start tag
	 * @throws IOException output error occurs
	 */
	public void anchorStart(HtmlElementStart e) throws IOException, Exception {
		String href = "", name = "", title = "";
		if (e.getAttributes().get("href") != null) {
			href = e.getAttributes().get("href");
		}
		if (e.getAttributes().get("name") != null) {
			name = e.getAttributes().get("name");
		}
		if (e.getAttributes().get("title") != null) {
			title = e.getAttributes().get("title");
		}
		switch (_config.getLinksConversionType()) {
			case FOOTNOTES:
				break;
			case BIBLIO:
				break;
			case HYPERTEX:
				if (href.startsWith("#")) {
					_writer.write("\\hyperlink{" + href.substring(1, href.length()) + "}{");
					break;
				}
				if (!name.equals("")) {
					_writer.write("\\hypertarget{" + name + "}{");
					break;
				}
				if (!href.equals("")) {
					_writer.write("\\href{" + href + "}{");
					break;
				}
				if (!title.equals("")) {
				}
			case IGNORE:
				break;
		}
	}

	/**
	 * Called when A end element is reached.
	 *
	 * @param element corresponding end tag
	 * @param es start tag
	 * @throws IOException output error occurs
	 */
	public void anchorEnd(HtmlElementEnd element, HtmlElementStart es) throws IOException, Exception {
		String href = "", name = "", title = "";
		if (es.getAttributes().get("href") != null) {
			href = es.getAttributes().get("href");
		}
		if (es.getAttributes().get("name") != null) {
			name = es.getAttributes().get("name");
		}
		if (es.getAttributes().get("title") != null) {
			title = es.getAttributes().get("title");
		}
		switch (_config.getLinksConversionType()) {
			case FOOTNOTES:
				if (href.equals("")) {
					return;
				}
				if (href.startsWith("#")) {
					return;
				}
				_writer.write("\\footnote{" + es.getAttributes().get("href") + "}");
				break;
			case BIBLIO:
				if (href.equals("")) {
					return;
				}
				if (href.startsWith("#")) {
					return;
				}
				String key,
				 value;
				if (es.getAttributes().get("name") != null) {
					key = es.getAttributes().get("name");
				} else {
					key = es.getAttributes().get("href");
				}
				value = "\\verb|" + es.getAttributes().get("href") + "|.";
				if (es.getAttributes().get("title") != null) {
					value += " " + es.getAttributes().get("title");
				}
				_bibliography.put(key, value);
				_writer.write("\\cite{" + key + "}");
				break;
			case HYPERTEX:
				if (!name.equals("")) {
					_writer.write("}");
					break;
				}
				if (href.startsWith("#")) {
					_writer.write("}");
					break;
				}
				if (!href.equals("")) {
					_writer.write("}");
					break;
				}
				if (!title.equals("")) {
				}
				break;
			case IGNORE:
				break;
		}
	}

	/**
	 * Called when TR start element is reached.
	 *
	 * @param e start tag
	 * @throws IOException output error occurs
	 */
	public void tableRowStart(HtmlElementStart e) throws IOException, Exception {
		if (!_firstRow && !_printBorder) {
			_writer.write(" \\\\ \n");
		} else {
			_firstRow = false;
		}
	}

	/**
	 * Called when TR end element is reached.
	 *
	 * @param e corresponding end tag
	 * @param es start tag
	 * @throws IOException output error occurs
	 */
	public void tableRowEnd(HtmlElementEnd e, HtmlElementStart es) throws IOException {
		if (_printBorder) {
			_writer.write(" \\\\ \n\\hline\n");
		}
		_firstCell = true;
	}

	/**
	 * Called when TD start element is reached.
	 *
	 * @param e start tag
	 * @throws IOException output error occurs
	 */
	public void tableCellStart(HtmlElementStart e) throws IOException, Exception {
		if (!_firstCell) {
			_writer.write(" & ");
		} else {
			_firstCell = false;
		}
		_writer.write(_config.getElement(e.getElementName()).getStart());
	}

	/**
	 * Called when TD end element is reached.
	 *
	 * @param element corresponding end tag
	 * @param e start tag
	 * @throws IOException output error occurs
	 */
	public void tableCellEnd(HtmlElementEnd element, HtmlElementStart e) throws IOException, Exception {
		_writer.write(_config.getElement(e.getElementName()).getEnd());
	}

	/**
	 * Called when TABLE start element is reached.
	 *
	 * @param e start tag
	 * @throws IOException output error occurs
	 */
	public void tableStart(HtmlElementStart e) throws IOException, Exception {
		_writer.write(_config.getElement(e.getElementName()).getStart());
		String str;
		if ((str = e.getAttributes().get("latexcols")) != null) {
			_writer.write("{" + str + "}\n");
		}
		if ((str = e.getAttributes().get("border")) != null) {
			if (!str.equals("0")) {
				_printBorder = true;
			}
		}
		if (_printBorder) {
			_writer.write("\\hline \n");
		}
	}

	/**
	 * Called when TABLE end element is reached.
	 *
	 * @param e corresponding end tag
	 * @param es start tag
	 * @throws IOException output error occurs
	 */
	public void tableEnd(HtmlElementEnd e, HtmlElementStart es) throws IOException, Exception {
		_writer.write(_config.getElement(e.getElementName()).getEnd());
		_firstRow = true;
		_printBorder = false;
	}

	/**
	 * Called when BODY start element is reached.
	 *
	 * @param es start tag
	 * @throws IOException output error occurs
	 */
	public void bodyStart(HtmlElementStart es) throws IOException, Exception {
		if (_config.getLinksConversionType() == LatexConverter.LK.HYPERTEX) {
			_writer.write("\n\\usepackage{hyperref}");
		}
		if (_config.getMakeCmdsFromCSS()) {
			_writer.write(_config.makeCmdsFromCSS());
		}
		_writer.write(_config.getElement(es.getElementName()).getStart());
	}

	/**
	 * Called when IMG start element is reached.
	 *
	 * @param es start tag
	 * @throws IOException output error occurs
	 */
	public void imgStart(HtmlElementStart es) throws IOException, Exception {
		_writer.write("\n\\includegraphics{" + es.getAttributes().get("src") + "}");
	}

	/**
	 * Called when META start element is reached.Recognizes basic charsets (cp1250, utf8, latin2)
	 *
	 * @param es start tag
	 * @throws IOException output error occurs
	 */
	public void metaStart(HtmlElementStart es) throws IOException, Exception {
		String str, str2 = "";
		if ((str = es.getAttributes().get("http-equiv")) != null) {
			if ((str.compareToIgnoreCase("content-type") == 0) && ((str2 = es.getAttributes().get("content")) != null)) {
				str2 = str2.toLowerCase();

				if (str2.contains("windows-1250")) {
					_writer.write("\n\\usepackage[cp1250]{inputenc}");
				} else if (str2.contains("iso-8859-2")) {
					_writer.write("\n\\usepackage[latin2]{inputenc}");
				} else if (str2.contains("utf-8")) {
					_writer.write("\n\\usepackage[utf8]{inputenc}");
				}
			}
		}
	}

	/**
	 * Called when FONT start element is reached.
	 *
	 * @param es start tag
	 * @throws IOException output error occurs
	 */
	public void fontStart(HtmlElementStart es) throws IOException, Exception {
		if (es.getAttributes().get("size") != null) {
			String command;
			try {
				Integer size = Integer.valueOf(es.getAttributes().get("size"));
				switch (size) {
					case 1:
						command = "{\\tiny";
						break;
					case 2:
						command = "{\\footnotesize";
						break;
					case 3:
						command = "{\\normalsize";
						break;
					case 4:
						command = "{\\large";
						break;
					case 5:
						command = "{\\Large";
						break;
					case 6:
						command = "{\\LARGE";
						break;
					case 7:
						command = "{\\Huge";
						break;
					default:
						command = "{\\normalsize";
						break;
				}
			} catch (NumberFormatException ex) {
				command = "{\\normalsize";
			}

			_writer.write(command + " ");
		}
	}

	/**
	 * Called when FONT end element is reached.
	 *
	 * @param e corresponding end tag
	 * @param es start tag
	 * @throws IOException output error occurs
	 */
	public void fontEnd(HtmlElementEnd e, HtmlElementStart es) throws IOException, Exception {
		if (es.getAttributes().get("size") != null) {
			_writer.write("}");
		}
	}

	/**
	 * Called when end element is reached.
	 *
	 * @param element corresponding end tag
	 * @param es start tag
	 * @throws IOException output error occurs
	 */
	public void bodyEnd(HtmlElementEnd element, HtmlElementStart es) throws IOException, Exception {
		if (!_bibliography.isEmpty()) {
			_writer.write("\n\n\\begin{thebibliography}{" + _bibliography.size() + "}\n");
			for (Entry<String, String> entry : _bibliography.entrySet()) {
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				_writer.write("\t\\bibitem{" + key + "}" + value + "\n");
			}
			_writer.write("\\end{thebibliography}");
		}
		commonElementEnd(element, es);
	}
}
