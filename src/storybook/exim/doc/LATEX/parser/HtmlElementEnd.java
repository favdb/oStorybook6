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

/**
 * Class representing HTML end element.
 */
public class HtmlElementEnd extends HtmlElement {

	/**
	 * Cstr.
	 *
	 * @param element element's name
	 */
	public HtmlElementEnd(String element) {
		_element = element;
	}
}
