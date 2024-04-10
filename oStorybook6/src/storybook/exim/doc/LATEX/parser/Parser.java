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
package storybook.exim.doc.LATEX.parser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

/**
 * HTML parser.
 */
public class Parser {

	private final IoReader _reader;
	private final IoParserHandler _handler;
	private final Stack<HtmlElementStart> _openElements = new Stack<>();

	public Parser(String input, String cssFile) throws Exception {
		_handler = new ParserHandler(cssFile);
		_reader = new IoInputStringReader(input);
	}

	public Parser(File inputFile, IoParserHandler handler) throws Exception {
		_handler = handler;
		_reader = new IoInputFileReader(inputFile);
	}

	public Parser(String inputText, IoParserHandler handler) throws Exception {
		_handler = handler;
		_reader = new IoInputFileReader(inputText);
	}

	/**
	 * Parses the HTML file and converts it using the particular handler.The file is processed char
	 * by char and a couple of events are sent to the handler.The whole process is very similar to
	 * the SAX model used with XML. The list of possible events which are sent to the handler
	 * follows.
	 * <ul>
	 * <li>startElement -- the start element was reached (ie. <code>&lt;p</code>)</li>
	 * <li>endElement -- the end element was reached (ie. <code>&lt;/p&gt;</code>)</li>
	 * <li>endDocument -- the end of the document was reached</li>
	 * <li>characters -- the text content of an element</li>
	 * <li>comment -- the comment was reached</li>
	 * </ul>
	 *
	 * @throws Exception fatal error (ie. input file can't be opened) occurs
	 */
	public void parse() throws Exception {
		try {
			doParsing();
		} catch (IOException e) {
			throw new Exception("Can't read the input file");
		} finally {
			_handler.endDocument();
			destroy();
		}
	}

	public IoParserHandler getParserHandler() {
		return _handler;
	}

	/**
	 * Closes the input input file specified in the
	 * {@link Parser#parse(File inputFile, IParserHandler handler) parse()} method.
	 *
	 * @throws FatalErrorException when input file can't be closed
	 */
	private void destroy() throws Exception {
		_reader.close();
	}

	/**
	 * Reads the input file char by char. When the <code>&quot;&lt;&quot;</code> char is reached
	 * {@link Parser#readElement() readElement()} is called otherwise
	 * {@link Parser#readContent(char) readContent()} is called.
	 *
	 * @throws IOException when input error occurs
	 */
	private void doParsing() throws IOException {
		int c;
		char ch;
		while ((c = _reader.read()) != -1) {
			ch = (char) c;
			if (ch == '<') {
				readElement();
			} else {
				readContent(ch);
			}
		}
	}

	/**
	 * Reads elements (tags). Sends <code>comment</code>, <code>startElement</code> and
	 * <code>endElement</code> events to the handler.
	 *
	 * @throws IOException when input error occurs
	 */
	private void readElement() throws IOException {
		int c;
		char ch;
		StringBuilder strb = new StringBuilder(""); // used while building
		String str; // used once finished building
		while ((c = _reader.read()) != -1) {
			ch = (char) c;
			// i'm at the end of the element
			if (ch == '>') {
				// is it a comment
				if (strb.toString().startsWith("!--")) {
					if (strb.toString().endsWith("--")) {
						// trim the comment's start and end tags
						str = strb.toString().substring(3, strb.length());
						str = str.substring(0, str.length() - 2);
						_handler.comment(str);
						return;
					}
					strb.append(ch);
					continue;
				}
				str = strb.toString();

				// parse the element (get the attributes)
				HtmlElement element = parseElement(str);
				if (element instanceof HtmlElementStart) {
					// non-empty element
					if (!str.endsWith("/")) {
						_openElements.push((HtmlElementStart) element);
					}
					_handler.startElement((HtmlElementStart) element);
					// empty element (ie. "br") -> send also endElement event
					if (str.endsWith("/")) {
						_handler.endElement(new HtmlElementEnd(element.getElementName()), (HtmlElementStart) element);
					}
				} else if (element instanceof HtmlElementEnd) {
					// check validity of the document
					checkValidity((HtmlElementEnd) element);
				}
				return;
			}

			strb.append(ch);
		}
	}

	/**
	 * Parses element. Stores element attributes in {@link HtmlElementStart HtmlElementStart} object if it's
	 * a start element.
	 *
	 * @param elementString string containing the element with its attributes (but without leading
	 * &quot;&lt;&quot; and ending &quot;&gt;&quot;)
	 * @return {@link HtmlElementStart HtmlElementStart} or {@link HtmlElementEnd HtmlElementEnd} object.
	 */
	private HtmlElement parseElement(String elementString) {
		String elementName = "";
		HashMap<String, String> attributes = new HashMap<>(3);
		// remove ending "/" from empty element
		if (elementString.endsWith("/")) {
			elementString = elementString.substring(0, elementString.length() - 1);
		}
		String[] aux = elementString.split("\\s+", 2);
		if (aux.length != 0) {
			elementName = aux[0];
			// it's the end element (starts with "/")
			if ((elementName.length() > 1) && (elementName.charAt(0) == '/')) {
				String name = elementName.substring(1, elementName.length()).toLowerCase();
				return new HtmlElementEnd(name);
			}
			// get all attributes
			if (aux.length == 2) {
				String[] attr = aux[1].split("('\\s+)|(\"\\s+)");
				for (int i = 0; i < attr.length; ++i) {
					attr[i] = attr[i].trim().replace("\"", "").replace("'", "");
					String[] attrInstance = attr[i].split("=", 2);
					if (attrInstance.length == 2) {
						attributes.put(attrInstance[0].toLowerCase(), attrInstance[1]);
					}
				}
			}
		}
		// it's the start element
		return new HtmlElementStart(elementName.toLowerCase(), attributes);
	}

	/**
	 * Reads text content of an element. Sends <code>character</code> event to the handler.
	 *
	 * @param firstChar first char read in {@link Parser#doParsing doParsing()} method
	 * @throws IOException when input error occurs
	 */
	private void readContent(char firstChar) throws IOException {
		int c;
		char ch;
		String str = "";
		str += firstChar;
		while ((c = _reader.read()) != -1) {
			ch = (char) c;
			if (ch == '<') {
				_handler.characters(str);
				readElement();
				return;
			}
			str += ch;
		}
	}

	/**
	 * Checks whether the document is well-formed. If not it sends <code>endElement</code> events
	 * for the elements which were opened but not correctly closed.
	 *
	 * @param element the latest ending element which was reached
	 */
	private void checkValidity(HtmlElementEnd element) {
		// no start element -> ignore close element
		if (_openElements.empty()) {
			return;
		}
		// document well-formed
		if (_openElements.peek().getElementName().equals(element.getElementName())) {
			_handler.endElement(element, _openElements.pop());
			return;
		}
		// document non-well-formed
		// try to find the correspoding start element of the end element in the stack
		// and close all non-closed elements; if not found ignore it
		for (int i = _openElements.size() - 1; i >= 0; --i) {
			if (_openElements.get(i).getElementName().equals(element.getElementName())) {
				for (int j = _openElements.size() - 1; j >= i; --j) {
					HtmlElementStart es = _openElements.get(i);
					HtmlElementEnd e = new HtmlElementEnd(_openElements.pop().getElementName());
					_handler.endElement(e, es);
				}
				return;
			}
		}
	}

}
