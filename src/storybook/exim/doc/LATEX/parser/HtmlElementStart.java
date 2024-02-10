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

import java.util.HashMap;

/**
 * Class representing HTML start element.
 */
public class HtmlElementStart extends HtmlElement {

	private HashMap<String, String> _attributes;

	/**
	 * Cstr.
	 *
	 * @param element element's name
	 * @param attributes element's attributes
	 */
	public HtmlElementStart(String element, HashMap<String, String> attributes) {
		_element = element;
		_attributes = attributes;
	}

	/**
	 * Returns element's attributes.
	 *
	 * @return element's attributes
	 */
	public HashMap<String, String> getAttributes() {
		return _attributes;
	}

}
