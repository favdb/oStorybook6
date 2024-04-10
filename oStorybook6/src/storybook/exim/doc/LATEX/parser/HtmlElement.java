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
 * Abstract class for HTML start and end elements (tags).
 */
public abstract class HtmlElement {

	protected String _element;

	/**
	 * Returns element's name.
	 *
	 * @return element's name
	 */
	public String getElementName() {
		return _element;
	}
}
