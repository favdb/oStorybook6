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
package storybook.exim.doc.LATEX.css;

import java.util.HashMap;
import storybook.exim.doc.LATEX.LatexConf;

/**
 * Handles events sent from the CSS Parser.
 */
public class CSSParserHandler {

	private final LatexConf _config;

	/**
	 * CSS parser handler
	 *
	 * @param config program configuration
	 */
	public CSSParserHandler(LatexConf config) {
		_config = config;
	}

	/**
	 * Called when a new style is reached in the CSS stylesheet. Splits up multiple style names (ie. <code>h1, h2, h3 { ... }
	 * </code>) and sets style properties for each of the style names.
	 *
	 * @param styleName name of the style
	 * @param properties map with all the style's properties
	 */
	public void newStyle(String styleName, HashMap<String, String> properties) {
		// split up multiple names (ie. h1, h2, h3 { ... } )
		String[] split = styleName.toLowerCase().split(",");
		for (int i = 0; i < split.length; ++i) {
			String name = split[i].trim();
			CSS style = _config.getStyle(name);
			// not in the config yet
			if (style == null) {
				style = new CSS(name);
				_config.addStyle(name, style);
			}
			style.setProperties(properties);
		}
	}
}
