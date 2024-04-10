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

import java.io.IOException;

public class IoOutputStringWriter implements IoWriter {

	private final StringBuilder output;

	public IoOutputStringWriter() {
		output = new StringBuilder();
	}

	@Override
	public void write(String str) throws IOException {
		output.append(str);
	}

	@Override
	public void close() {

	}

	public String getOutput() {
		return output.toString();
	}
}
