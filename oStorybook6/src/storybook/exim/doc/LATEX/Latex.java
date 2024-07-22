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
package storybook.exim.doc.LATEX;

import java.io.*;
import storybook.exim.doc.LATEX.parser.Parser;
import storybook.exim.doc.LATEX.parser.ParserHandler;
import storybook.tools.LOG;
import storybook.ui.MainFrame;

/**
 * Program main class.
 */
public class Latex {

    private static final String TT = "Latex";

    /**
     * Creates Latex instance and runs its
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
	Latex latex = new Latex();
	latex.processCmdLineArgs(args);
	if (latex.getInputFile().isEmpty()) {
	    System.err.println("Input file not specified.");
	    return;
	}
	File f = new File(latex.getInputFile());
	if (!f.exists()) {
	    System.err.println("Input file not exists.");
	    return;
	}
	if (latex.getOutputFile().isEmpty()) {
	    System.err.println("Output file not specified.");
	    return;
	}
	f = new File(latex.getOutputFile());
	if (f.exists()) {
	    System.out.println("Output file exists, replacing it.");
	    f.delete();
	}
	latex.execMain();
    }

    /**
     * create a Latex document file for the whole book
     *
     * @param mainFrame
     * @param file
     * @param text
     * @return
     */
    public static boolean createDoc(MainFrame mainFrame, File file, String text) {
	try {
	    ParserHandler handler = new ParserHandler(file, null);
	    new Parser(text
		    .replace("«", "&laquo;").replace("»", "&raquo;")
		    .replace("“", "&ldquo;").replace("”", "&rdquo;"), handler).parse();
	    return true;
	} catch (Exception ex) {
	    LOG.err(TT + ".createDoc error", ex);
	}
	return false;
    }

    /**
     * Latex class
     */
    public Latex() {
    }

    /**
     * execute the main conversion
     */
    public void execMain() {
	try {
	    File inf = new File(getInputFile());
	    File ouf = new File(getOutputFile());
	    // TODO: check files exist & have write permissions
	    ParserHandler handler = new ParserHandler(ouf, this);
	    new Parser(inf, handler).parse();
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	    System.exit(-1);
	}
    }

    private String inputFile = "",
	    outputFile = "",
	    configFile = "config.xml",
	    cssFile = "";

    /**
     * Processes command line arguments.
     * <ul>
     * <li>-input &lt;fileName&gt;</li>
     * <li>-output &lt;fileName&gt;</li>
     * <li>-config &lt;fileName&gt;</li>
     * <li>-css &lt;fileName&gt;</li>
     * </ul>
     *
     * @param args command line arguments
     */
    public void processCmdLineArgs(String[] args) {
	for (int i = 0; i < args.length; ++i) {
	    switch (args[i]) {
		case "--input":
		case "-i":
		    if (i < (args.length - 1)) {
			inputFile = args[i + 1];
			++i;
		    }
		    break;
		case "--output":
		case "-o":
		    if (i < (args.length - 1)) {
			outputFile = args[i + 1];
			++i;
		    }
		    break;
		case "--config":
		case "-c":
		    if (i < (args.length - 1)) {
			configFile = args[i + 1];
			++i;
		    }
		    break;
		case "--css":
		    if (i < (args.length - 1)) {
			cssFile = args[i + 1];
			++i;
		    }
		    break;
	    }
	}
    }

    /**
     * get the input file name
     *
     * @return
     */
    public String getInputFile() {
	return inputFile;
    }

    /**
     * set the input file name
     *
     * @param inputFile
     */
    public void setInputFile(String inputFile) {
	this.inputFile = inputFile;
    }

    /**
     * get the output file name
     *
     * @return
     */
    public String getOutputFile() {
	return outputFile;
    }

    /**
     * set the output file name
     *
     * @param outputFile
     */
    public void setOutputFile(String outputFile) {
	this.outputFile = outputFile;
    }

    /**
     * get the configuration file name
     *
     * @return
     */
    public String getConfigFile() {
	return configFile;
    }

    /**
     * set the configuration file name
     *
     * @param configFile
     */
    public void setConfigFile(String configFile) {
	this.configFile = configFile;
    }

    /**
     * get the CSS file name
     *
     * @return
     */
    public String getCssFile() {
	return cssFile;
    }

    /**
     * set the CSS file name
     *
     * @param cssFile
     */
    public void setCssFile(String cssFile) {
	this.cssFile = cssFile;
    }

    /**
     * check if configuration file (config.xml) exists
     *
     * @return
     */
    public boolean configExists() {
	if (configFile.isEmpty()) {
	    return false;
	}
	File f = new File(configFile);
	return f.exists();
    }

}
