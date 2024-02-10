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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class IoOutputFileWriter implements IoWriter {

	private BufferedWriter _writer;

	public IoOutputFileWriter(File outputFile) throws Exception {
		try {
			_writer = new BufferedWriter(new FileWriter(outputFile));
		} catch (IOException e) {
			throw new Exception("Can't open the output file: " + outputFile.getName());
		}
	}

	public void write(String str) throws IOException {
		_writer.write(str);
	}

	public void close() {
		try {
			_writer.close();
		} catch (IOException e) {
			System.err.println("Can't close the output file");
		}
	}

	@Override
	public String getOutput() {
		return null;
	}
}
