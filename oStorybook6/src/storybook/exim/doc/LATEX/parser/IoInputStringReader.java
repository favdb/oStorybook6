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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class IoInputStringReader implements IoReader {

	private final InputStreamReader _reader;

	public IoInputStringReader(String input) {
		InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
		_reader = new InputStreamReader(is);
	}

	@Override
	public int read() throws IOException {
		return _reader.read();
	}

	@Override
	public void close() throws Exception {
		if (_reader != null) {
			try {
				_reader.close();
			} catch (IOException e) {
				throw new Exception("Can't close the input file");
			}
		}
	}
}
