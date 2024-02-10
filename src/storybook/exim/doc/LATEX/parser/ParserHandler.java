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
import storybook.exim.doc.LATEX.Latex;
import storybook.exim.doc.LATEX.LatexConverter;

/**
 * Handles events sent from the {@link Parser Parser} class. Calls appropriate methods from the
 * {@link LatexConverter Convertor} class.
 */
public class ParserHandler implements IoParserHandler {

	/**
	 * Convertor.
	 */
	private final LatexConverter _converter;

	public ParserHandler(String cssFile) throws Exception {
		_converter = new LatexConverter(cssFile);
	}

	/**
	 * Cstr.
	 *
	 * @param outputFile output LaTeX file
	 * @param latex
	 * @throws FatalErrorException fatal error (ie. output file can't be closed) occurs
	 */
	public ParserHandler(File outputFile, Latex latex) throws Exception {
		_converter = new LatexConverter(outputFile, latex);
	}

	@Override
	public LatexConverter getConverter() {
		return _converter;
	}

	/**
	 * Called when a start element is reached in the input document. Calls
	 * {@link LatexConverter#commonElementStart(ElementStart) commonElementStart()} for non-special
	 * elements and specials methods for the elements requiring special care (ie.
	 * {@link LatexConverter#tableRowStart(ElementStart) tableRowStart()} for
	 * <code>&lt;table&gt;</code>)
	 *
	 * @param element start element reached
	 */
	@Override
	public void startElement(HtmlElementStart element) {
		try {
			String name = element.getElementName();
			switch (name) {
				case "a":
					_converter.anchorStart(element);
					break;
				case "tr":
					_converter.tableRowStart(element);
					break;
				case "td":
					_converter.tableCellStart(element);
					break;
				case "th":
					_converter.tableCellStart(element);
					break;
				case "meta":
					_converter.metaStart(element);
					break;
				case "body":
					_converter.bodyStart(element);
					break;
				case "font":
					_converter.fontStart(element);
					break;
				case "img":
					_converter.imgStart(element);
					break;
				case "table":
					_converter.tableStart(element);
					break;
				default:
					_converter.commonElementStart(element);
					break;
			}

			_converter.cssStyleStart(element);
		} catch (IOException e) {
			System.err.println("Can't write into output file");
		} catch (Exception e) {
			System.out.println(e);
			// e.printStackTrace();
		}
	}

	/**
	 * Called when an end element is reached in the input document. Calls
	 * {@link LatexConverter#commonElementEnd(ElementEnd, ElementStart) commonElementEnd()} for
	 * non-special elements and specials methods for the elements requiring special care (ie. {@link LatexConverter#tableRowEnd(ElementEnd, ElementStart)
	 * tableRowEnd()} for <code>&lt;/table&gt;</code>)
	 *
	 * @param element end element reached
	 * @param elementStart corresponding start element
	 */
	@Override
	public void endElement(HtmlElementEnd element, HtmlElementStart elementStart) {
		try {
			String name = element.getElementName();
			_converter.cssStyleEnd(elementStart);
			switch (name) {
				case "a":
					_converter.anchorEnd(element, elementStart);
					break;
				case "tr":
					_converter.tableRowEnd(element, elementStart);
					break;
				case "th":
					_converter.tableCellEnd(element, elementStart);
					break;
				case "td":
					_converter.tableCellEnd(element, elementStart);
					break;
				case "table":
					_converter.tableEnd(element, elementStart);
					break;
				case "body":
					_converter.bodyEnd(element, elementStart);
					break;
				case "font":
					_converter.fontEnd(element, elementStart);
					break;
				default:
					_converter.commonElementEnd(element, elementStart);
					break;
			}
		} catch (IOException e) {
			System.err.println("Can't write into output file.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Called when the text content of an element is read. Calls
	 * {@link LatexConverter#characters(String) characters()} method of the
	 * {@link LatexConverter Convertor} class.
	 *
	 * @param content ie. &quot;foo&quot; for the &quot;&lt;b&gt;foo&lt;/b&gt;&quot;
	 */
	@Override
	public void characters(String content) {
		try {
			_converter.characters(content);
		} catch (IOException e) {
			System.err.println("Can't write into output file.");
		}
	}

	/**
	 * Called when the comment is reached in input document. Calls
	 * {@link LatexConverter#comment(String) comment()} method of the
	 * {@link LatexConverter Convertor} class.
	 *
	 * @param comment ie. &quot;foo&quot; for the &quot;&lt;!--&gt;foo&lt;/--&gt;&quot;
	 */
	@Override
	public void comment(String comment) {
		try {
			_converter.comment(comment);
		} catch (IOException e) {
			System.err.println("Can't write into output file.");
		}
	}

	/**
	 * Called when the whole input document is read. Calls
	 * {@link LatexConverter#destroy() destroy()} method of the {@link LatexConverter Convertor}
	 * class.
	 */
	@Override
	public void endDocument() {
		_converter.destroy();
	}
}
