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

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import storybook.exim.doc.LATEX.LatexConf;

/**
 * Class representing CSS style definition in program configuration.
 */
public class CSS {

	private final String _name;
	private String _start = "";
	private String _end = "";
	private final HashMap<String, String> _properties;

	/**
	 * Cstr.
	 *
	 * @param name style name
	 */
	public CSS(String name) {
		_properties = new HashMap<>(5);
		_name = name;
	}

	/**
	 * Sets the style properties.
	 *
	 * @param prop style properties
	 */
	public void setProperties(HashMap<String, String> prop) {
		_properties.putAll(prop);
	}

	/**
	 * Sets {@link CSS#_start start} and {@link CSS#_end end} commands for the style on the basis of
	 * style properties and program configuration.
	 *
	 * @param conf program configuration
	 */
	public void makeLaTeXCommands(LatexConf conf) {
		// inheritProperties(conf);
		colorProperty(conf);
		fontFamilyProperty(conf);
		for (Entry<String, String> entry : _properties.entrySet()) {
			String property = (String) entry.getKey();
			String value = (String) entry.getValue();
			// "font-family" already converted
			if (property.equals("font-family")) {
				continue;
			}
			try {
				LatexConf.CSSItem item = conf.getPropertyConf(property + "-" + value);
				_start += item.getStart();
				_end = item.getEnd() + _end;
			} catch (Exception e) {
				// System.out.println(e.getMessage());
			}
		}
	}

	/**
	 * Converts &quot;color&quot; property using &quot;xcolor&quot; LaTeX package. HTML notation
	 * (#xxx or #xxxxxx where &quot;x&quot; is a hexa number) and rgb notation (rgb(20,180,60) or
	 * rgb(20%, 80%, 15%) are supported. Also the 17 named colours defined defined in the CSS
	 * specification are correctly converted.
	 *
	 * @param conf program configuration
	 */
	private void colorProperty(LatexConf conf) {
		String color = _properties.get("color");
		if (color == null) {
			return;
		}
		// #xxx or #xxxxxx
		if (color.startsWith("#")) {
			color = color.replace("#", "");
			// #abc -> #aabbcc
			if (color.length() == 3) {
				StringBuilder buf = new StringBuilder(color);
				buf.insert(1, color.charAt(0));
				buf.insert(3, color.charAt(1));
				buf.insert(5, color.charAt(2));
				color = buf.toString();
			}
			_start += "{\\color[HTML]{" + color + "}";
			_end = "}" + _end;
			// rgb(20,180,60) or rgb(20%, 80%, 15%)
		} else if (color.startsWith("rgb(") && color.endsWith(")")) {
			color = color.substring(4, color.length() - 1);
			String[] numsStr = color.split(",");
			float[] nums = new float[3];
			if (numsStr.length != 3) {
				return;
			}
			// get color parts (from range <0,1>)
			try {
				for (int i = 0; i < numsStr.length; ++i) {
					if (numsStr[i].trim().endsWith("%")) {
						numsStr[i] = numsStr[i].replace("%", "").trim();
						nums[i] = Float.parseFloat(numsStr[i]) / 100;
					} else {
						nums[i] = Float.parseFloat(numsStr[i]) / 255;
					}
				}
			} catch (NumberFormatException e) {
				System.out.println("Wrong color definition in style: " + _name);
				return;
			}
			NumberFormat format = NumberFormat.getInstance(Locale.US);
			format.setMaximumFractionDigits(3);
			_start += "{\\color[rgb]{" + format.format(nums[0]) + "," + format.format(nums[1]) + "," + format.format(nums[2]) + "}";
			_end = "}" + _end;
		}
	}

	/**
	 * Converts &quot;font-family&quot; property. Tries to find first generic font family (ie.
	 * monospace) used in the definition and converts it using the configuration.
	 *
	 * @param conf program configuration
	 */
	public void fontFamilyProperty(LatexConf conf) {
		String family = _properties.get("font-family");
		if (family == null) {
			return;
		}
		// find first generic family (ie. monospace) used in the definition
		String[] fonts = family.split(",");
		for (int i = 0; i < fonts.length; ++i) {
			try {
				LatexConf.CSSItem item = conf.getPropertyConf("font-family" + "-" + fonts[i].trim());
				_start += item.getStart();
				_end = item.getEnd() + _end;
				break;
			} catch (Exception e) {
				// System.out.println(e.getMessage());
			}
		}
	}

	/**
	 * Returns name of the file with configuration.
	 *
	 * @return mapping between the style and LaTeX (start command)
	 */
	public String getStart() {
		return _start;
	}

	/**
	 * Returns mapping between the style and LaTeX (end command).
	 *
	 * @return mapping between the style and LaTeX (end command)
	 */
	public String getEnd() {
		return _end;
	}

	/**
	 * Returns style name.
	 *
	 * @return style name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns style properties.
	 *
	 * @return style properties
	 */
	public HashMap<String, String> getProperties() {
		return _properties;
	}

}
