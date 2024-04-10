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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

public class IoInputFileReader implements IoReader {

	private BufferedReader _reader;

	public IoInputFileReader(File inputFile) throws Exception {
		try {
			_reader = new BufferedReader(new FileReader(inputFile));
		} catch (IOException e) {
			throw new Exception("Can't open the input file: " + inputFile.getName());
		}
	}

	public IoInputFileReader(String inputText) throws Exception {
		_reader = new BufferedReader(new StringReader(inputText));
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
