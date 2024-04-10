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

import storybook.exim.doc.LATEX.LatexConverter;

/**
 * Handles events sent from the {@link Parser Parser} class.
 */
public interface IoParserHandler {

	public LatexConverter getConverter();

	/**
	 * Called when a start element is reached in the input document.
	 *
	 * @param element start element reached
	 */
	public void startElement(HtmlElementStart element);

	/**
	 * Called when an end element is reached in the input document.
	 *
	 * @param element end element reached
	 * @param elementStart corresponding start element
	 */
	public void endElement(HtmlElementEnd element, HtmlElementStart elementStart);

	/**
	 * Called when the text content of an element is read.
	 *
	 * @param content ie. &quot;foo&quot; for the &quot;&lt;b&gt;foo&lt;/b&gt;&quot;
	 */
	public void characters(String content);

	/**
	 * Called when the comment is reached in input document.
	 *
	 * @param comment ie. &quot;foo&quot; for the &quot;&lt;!--&gt;foo&lt;/--&gt;&quot;
	 */
	public void comment(String comment);

	/**
	 * Called when the whole input document is read.
	 */
	public void endDocument();

}
