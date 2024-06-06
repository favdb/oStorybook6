/*
Copyright 2018 by FaVdB
Modifications ???? by ????

This file is part of oStorybook.

    oStorybook is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License.

    oStorybook is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with oStorybook.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.book;

import java.util.ArrayList;
import java.util.List;
import static storybook.exim.exporter.AbstractExport.stringAttribute;
import storybook.tools.ListUtil;
import storybook.tools.StringUtil;
import storybook.tools.file.IOUtil;
import storybook.tools.html.Html;
import static storybook.tools.xml.XmlUtil.*;

/**
 *
 * @author FaVdB
 */
public class BookParamExport extends BookParamAbstract {

    private static final String TT = "BookParamExport.";

    public enum KW {
	DIRECTORY, FORMAT, HTML, HIGHLIGHT, CSS, CSV, TXT;

	@Override
	public String toString() {
	    return name().toLowerCase();
	}
    }

    public enum FORMAT {
	XML, HTML, OSBD, CSV, TXT, NONE;

	@Override
	public String toString() {
	    return this.name().toLowerCase();
	}

	public boolean isHtml() {
	    return toString().equals("html");
	}

	public boolean isXml() {
	    return toString().equals("xml");
	}

	public boolean isOsbd() {
	    return toString().equals("osbd");
	}

	public boolean compare(String str) {
	    return this.name().toLowerCase().equals(str.toLowerCase());
	}
    }

    public static FORMAT getFormat(String str) {
	if (str != null && !str.isEmpty()) {
	    for (FORMAT id : FORMAT.values()) {
		if (id.toString().equals(str)) {
		    return (id);
		}
	    }
	}
	return FORMAT.NONE;
    }

    private String directory = "",
	    format = "",
	    htmlCss = "",
	    fileName = "",
	    csvQuote = "\"",
	    csvComma = ";",
	    txtSeparator = "";
    private boolean epubCover,
	    epubCoverNoText,
	    chapterBookTitle,
	    chapterBreakPage,
	    htmlNav,
	    htmlNavImage,
	    htmlMultiChapter,
	    htmlMultiScene,
	    txtTab;
    private int highlight = Html.EM_LEFTASIS;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public BookParamExport(BookParam param) {
	super(param, "export");
	if (param.book.project.rootNode != null) {
	    node = getNodeElement("export");
	}
	init();
    }

    public String getDirectory() {
	return directory;
    }

    public void setDirectory(String d) {
	directory = d;
    }

    public String getFormat() {
	return format;
    }

    public void setFormat(String d) {
	format = d;
    }

    public boolean getEpubCoverNoText() {
	return this.epubCoverNoText;
    }

    public boolean getEpubCover() {
	return this.epubCover;
    }

    public void setEpubCover(boolean b) {
	this.epubCover = b;
    }

    public void setEpubCoverNoText(boolean b) {
	this.epubCoverNoText = b;
    }

    public boolean getHtmlChapterTitle() {
	return chapterBookTitle;
    }

    public void setHtmlChapterTitle(boolean d) {
	chapterBookTitle = d;
    }

    public boolean getHtmlChapterBreakPage() {
	return chapterBreakPage;
    }

    public void setHtmlChapterBreakPage(boolean d) {
	chapterBreakPage = d;
    }

    public void setHighlight(int value) {
	this.highlight = value;
    }

    public int getHighlight() {
	return this.highlight;
    }

    public String getHtmlCss() {
	return htmlCss;
    }

    public void setHtmlCss(String d) {
	htmlCss = d;
    }

    public boolean isMulti() {
	return htmlMultiChapter || htmlMultiScene;
    }

    public boolean getHtmlMultiChapter() {
	return htmlMultiChapter;
    }

    public void setHtmlMultiChapter(boolean d) {
	htmlMultiChapter = d;
    }

    public boolean getHtmlMultiScene() {
	return htmlMultiScene;
    }

    public void setHtmlMultiScene(boolean d) {
	htmlMultiScene = d;
    }

    public boolean getHtmlNav() {
	return htmlNav;
    }

    public void setHtmlNav(boolean d) {
	htmlNav = d;
    }

    public boolean getHtmlNavImage() {
	return htmlNavImage;
    }

    public void setHtmlNavImage(boolean d) {
	htmlNavImage = d;
    }

    @Override
    protected void init() {
	if (node != null) {
	    String str = getString(node, KW.DIRECTORY.toString());
	    if (IOUtil.dirExists(str)) {
		setDirectory(str);
	    } else {
		setDirectory("");
	    }
	    setFormat(getString(node, KW.FORMAT.toString()));
	    String x = getString(node, KW.HTML.toString());
	    while (x.length() < 6) {
		x += "0";
	    }
	    setHtmlChapterTitle(x.charAt(0) == '1');
	    setHtmlChapterBreakPage(x.charAt(1) == '1');
	    setHtmlMultiChapter(x.charAt(2) == '1');
	    setHtmlMultiScene(x.charAt(3) == '1');
	    setHtmlNav(x.charAt(4) == '1');
	    setHtmlNavImage(x.charAt(5) == '1');
	    setHtmlCss(getString(node, KW.CSS.toString()));
	    setHighlight(getInteger(node, KW.HIGHLIGHT.toString()));
	    setCsv(getString(node, KW.CSV.toString()));
	    setTxt(getString(node, KW.TXT.toString()));
	}
    }

    @Override
    public void refresh() {
	init();
    }

    @Override
    public String toXml() {
	//LOG.trace(TT + ".toXml(b)");
	StringBuilder b = new StringBuilder("        <export ");
	b.append(stringAttribute(0, KW.DIRECTORY.toString(), getDirectory()));
	b.append(stringAttribute(0, KW.FORMAT.toString(), getFormat()));
	b.append(stringAttribute(13, KW.HTML.toString(), getHtmlChapterTitle(), getHtmlChapterBreakPage(),
		getHtmlMultiChapter(), getHtmlMultiScene(), getHtmlNav(), getHtmlNavImage())
	);
	b.append(stringAttribute(0, KW.CSS.toString(), getHtmlCss()));
	b.append(stringAttribute(13, KW.HIGHLIGHT.toString(), getHighlight()));
	b.append(stringAttribute(13, KW.CSV.toString(), getCsv()));
	b.append(stringAttribute(13, KW.TXT.toString(), getTxt()));
	b.append(" />\n");
	return b.toString();
    }

    @Override
    public int hash() {
	return toString().hashCode();
    }

    @Override
    public String toString() {
	StringBuilder b = new StringBuilder();
	b.append("export dir=").append(getDirectory()).append("\n");
	List<String> str = new ArrayList<>();
	if (getHtmlChapterTitle()) {
	    str.add("chapterBookTitle");
	}
	if (getHtmlMultiChapter()) {
	    str.add("files chapters");
	}
	if (getHtmlMultiScene()) {
	    str.add("files scenes");
	}
	if (getHighlight() == 0) {
	    str.add("highlighter 0");
	}
	if (getHighlight() == 1) {
	    str.add("highlighter 1");
	}
	if (getHighlight() == 2) {
	    str.add("highlighter 2");
	}
	if (getHtmlNav()) {
	    str.add("navbar");
	    if (getHtmlNavImage()) {
		str.add("navbar images");
	    }
	}
	if (getHtmlChapterBreakPage()) {
	    str.add("chapter break page");
	}
	b.append("parameters=").append(ListUtil.join(str, ", "));
	b.append("\ncss=").append(htmlCss);
	b.append("\nformat=").append(format);
	return b.toString();
    }

    public void setFileName(String fileName) {
	this.fileName = fileName;
    }

    public String getFileName() {
	return fileName;
    }

    public boolean isHtml() {
	return format.equals("html");
    }

    public boolean isCsv() {
	return format.equals("csv");
    }

    public boolean isTxt() {
	return format.equals("txt");
    }

    public boolean isXml() {
	return format.equals("xml");
    }

    public boolean isOsbk() {
	return format.equals("osbk");
    }

    public void setTxt(String v) {
	if (!v.isEmpty()) {
	    String s[] = v.split(",");
	    if (s.length > 0) {
		setTxtTab(s[0].equals("1"));
	    }
	    if (s.length > 1) {
		setTxtSeparator(s[1]);
	    }
	}
    }

    public String getTxt() {
	if ((txtSeparator == null || txtSeparator.isEmpty())
		&& (!txtTab)) {
	    return "";
	}
	StringBuilder b = new StringBuilder();
	b.append((txtTab ? "1" : "0"));
	b.append(",");
	if (txtSeparator != null && !txtSeparator.isEmpty()) {
	    b.append(txtSeparator);
	}
	return b.toString();
    }

    public String getTxtSeparator() {
	return this.txtSeparator;
    }

    public void setTxtSeparator(String value) {
	this.txtSeparator = value;
    }

    public boolean getTxtTab() {
	return txtTab;
    }

    public void setTxtTab(boolean sel) {
	txtTab = sel;
    }

    public String getCsv() {
	if ((csvQuote == null || csvQuote.isEmpty())
		&& (csvComma == null || csvComma.isEmpty())) {
	    return "";
	}
	StringBuilder b = new StringBuilder();
	b.append(getCsvQuoteXml());
	b.append(",");
	b.append(getCsvCommaXml());
	return b.toString();
    }

    public void setCsvQuote(String value) {
	this.csvQuote = value;
    }

    public String getCsvQuote() {
	return this.csvQuote;
    }

    public String getCsvQuoteXml() {
	if (csvQuote == null || csvQuote.isEmpty()) {
	    csvQuote = "\"";
	}
	int n = csvQuote.charAt(0);
	return "" + n;
    }

    public void setCsv(String v) {
	if (!v.isEmpty()) {
	    String s[] = v.split(",");
	    if (s.length > 0 && StringUtil.isNumeric(s[0])) {
		int n = Integer.parseInt(s[0]);
		setCsvComma(Character.toString((char) n));
	    }
	    if (s.length > 1 && StringUtil.isNumeric(s[1])) {
		int n = Integer.parseInt(s[1]);
		setCsvQuote(Character.toString((char) n));
	    }
	}
    }

    public void setCsvComma(String value) {
	this.csvComma = value;
    }

    public String getCsvComma() {
	return this.csvComma;
    }

    public String getCsvCommaXml() {
	if (csvComma == null || csvComma.isEmpty()) {
	    csvComma = ";";
	}
	int n = csvComma.charAt(0);
	return "" + n;
    }

    public BookParamLayout getLayout() {
	return this.param.getParamLayout();
    }

    public String toCheck() {
	StringBuilder b = new StringBuilder();
	b.append("format=").append(format).append("|");
	b.append("csvComma=").append(csvComma).append("|");
	b.append("csvQuote=").append(csvQuote).append("|");
	b.append("dir=").append(directory).append("|");
	b.append("fileName=").append(fileName).append("|");
	b.append("txtSeparator=").append(txtSeparator).append("|");
	b.append("txtTab=").append(txtTab ? "1" : "0");
	return b.toString();
    }

}
