/*
 * Copyright (C) 2021 favdb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a fileCopy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.exim.exporter;

import api.shef.ShefEditor;
import i18n.I18N;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import storybook.db.book.BookParamExport;
import storybook.db.book.BookParamExport.FORMAT;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.person.Person;
import storybook.db.scene.SceneEdit;
import storybook.exim.doc.DOCX;
import storybook.exim.doc.LATEX.Latex;
import storybook.exim.doc.ODT;
import storybook.tools.file.FileFilter;
import storybook.tools.file.IOUtil;
import storybook.tools.html.Html;
import storybook.ui.MainFrame;

/**
 * export the Book to a DOCXor a ODT
 *
 * @author favdb
 */
public class ExportBookToDoc {

    private static final String TT = "ExporHtml";

    private ExportBookToDoc() {
	// empty
    }

    public static void toHtml(MainFrame mainFrame, ShefEditor editor) {
	//LOG.printInfos(TT + ".toHtml(mainFrame, editor)");
	File f = selectFile(mainFrame);
	if (f == null) {
	    return;
	}
	if (IOUtil.askReplace(editor, f)) {
	    createHtml(f, editor);
	}
    }

    public static void toHtml(MainFrame mainFrame, SceneEdit pscene) {
	//LOG.printInfos(TT + ".toHtml(mainFrame, pscene)");
	File f = selectFile(mainFrame);
	if (f == null) {
	    return;
	}
	if (IOUtil.askReplace(pscene, f)) {
	    createHtml(f, pscene);
	}
    }

    public static File selectFile(MainFrame mainFrame) {
	//LOG.printInfos(TT+".fileSelect(mainFrame)");
	JFileChooser chooser = new JFileChooser(mainFrame.getProject().getPath());
	chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	String str = mainFrame.getBook().param.getParamEditor().getExtension();
	FileFilter f1;
	if (str.isEmpty()) {
	    f1 = new FileFilter("doc");
	} else {
	    f1 = new FileFilter(str);
	}
	chooser.addChoosableFileFilter(f1);
	if (!str.equals("docx")) {
	    chooser.addChoosableFileFilter(new FileFilter("docx"));
	}
	if (!str.equals("odt")) {
	    chooser.addChoosableFileFilter(new FileFilter("odt"));
	}
	if (!str.equals("html")) {
	    chooser.addChoosableFileFilter(new FileFilter("html"));
	}
	if (!str.equals("txt")) {
	    chooser.addChoosableFileFilter(new FileFilter("txt"));
	}
	if (!str.equals("tex")) {
	    chooser.addChoosableFileFilter(new FileFilter("tex"));
	}
	chooser.setFileFilter(f1);
	chooser.setApproveButtonText(I18N.getMsg("file.save"));
	chooser.setDialogTitle(I18N.getMsg("file.save"));
	while (true) {
	    int i = chooser.showOpenDialog(mainFrame);
	    if (i != JFileChooser.APPROVE_OPTION) {
		return null;
	    }
	    if (chooser.getSelectedFile() == null) {
		return null;
	    }
	    String filename = chooser.getSelectedFile().getAbsolutePath();
	    String ext = IOUtil.getExtension(chooser.getSelectedFile());
	    if (filename.endsWith(".html")
		    || filename.endsWith(".docx")
		    || filename.endsWith(".odt")
		    || filename.endsWith(".tex")
		    || filename.endsWith(".txt")) {
		return chooser.getSelectedFile();
	    } else if (ext.isEmpty()) {
		String cx = ((FileFilter) chooser.getFileFilter()).getType();
		if (cx.endsWith("html")
			|| cx.endsWith("docx")
			|| cx.endsWith("odt")
			|| cx.endsWith("tex")
			|| cx.endsWith("txt")) {
		    return new File(filename + "." + cx);
		} else {
		    JOptionPane.showMessageDialog(chooser,
			    I18N.getMsg("file.not.allowed"),
			    I18N.getMsg("file.export"),
			    JOptionPane.ERROR_MESSAGE);
		}
	    }
	}
    }

    private static boolean createHtml(File file, SceneEdit pscene) {
	//LOG.printInfos(TT + ".createHtml(file, pscene)");
	BookParamExport param = pscene.getMainFrame().getBook().param.getParamExport();
	StringBuilder buf = new StringBuilder();
	if (param.getLayout().getSceneTitle()) {
	    String t = pscene.getTfName();
	    if (!t.isEmpty()) {
		buf.append(Html.getHtag("." + t));
	    }
	}
	if (param.getLayout().getSceneDidascalie()) {
	    buf.append(getDidascalie(pscene));
	}
	buf.append(pscene.getText()).append("\n");
	IOUtil.fileWriteString(file, buf.toString());
	return true;
    }

    public static boolean createHtml(File file, ShefEditor editor) {
	//LOG.printInfos(TT + ".creatHtml(file, pscene)");
	StringBuilder buf = new StringBuilder();
	buf.append(editor.getText()).append("\n");
	IOUtil.fileWriteString(file, buf.toString());
	return true;
    }

    private static String getDidascalie(SceneEdit pscene) {
	//LOG.printInfos("ExportBookHtml.getDidascalie("+scene.getTitle()+")");
	String rc = "";
	if (!pscene.getPersons().isEmpty()) {
	    rc += "<i>" + Html.intoB(I18N.getMsg("persons")) + " : ";
	    for (Person p : pscene.getPersons()) {
		rc += p.getFullName() + ", ";
	    }
	    rc = rc.substring(0, rc.length() - 2);
	    rc += "</i>" + Html.BR;
	}
	if (!pscene.getLocations().isEmpty()) {
	    rc += "<i>" + Html.intoB(I18N.getMsg("locations")) + " : ";
	    for (Location p : pscene.getLocations()) {
		rc += p.getFullName() + ", ";
	    }
	    rc = rc.substring(0, rc.length() - 2);
	    rc += "</i>" + Html.BR;
	}
	if (!pscene.getItems().isEmpty()) {
	    rc += "<i>" + Html.intoB(I18N.getMsg("items")) + " : ";
	    for (Item p : pscene.getItems()) {
		rc += p.getName() + ", ";
	    }
	    rc = rc.substring(0, rc.length() - 2);
	    rc += "</i>" + Html.BR;
	}
	if (!pscene.getScenario_pitch().isEmpty()) {
	    rc += "<i>" + Html.intoB(I18N.getMsg("scenario.pitch")) + " : ";
	    rc += pscene.getScenario_pitch();
	    rc += "</i>" + Html.BR;
	}
	if (!rc.isEmpty()) {
	    rc = "<p style=\"text-align:right\">" + rc + "</p>";
	}
	return (rc);
    }

    /**
     * create a docx file
     *
     * @param mainFrame
     * @return
     */
    public static boolean createDocx(MainFrame mainFrame) {
	return createDoc(mainFrame, "docx");
    }

    /**
     * create an odt file
     *
     * @param mainFrame
     * @return
     */
    public static boolean createOdt(MainFrame mainFrame) {
	return createDoc(mainFrame, "odt");
    }

    /**
     * create a Latex file
     *
     * @param mainFrame
     * @return
     */
    public static boolean createLatex(MainFrame mainFrame) {
	return createDoc(mainFrame, "tex");
    }

    /**
     * create a document file for the whole book
     *
     * @param mainFrame
     * @param ext
     * @return
     */
    public static boolean createDoc(MainFrame mainFrame, String ext) {
	//compute a HTML String containig the Book
	BookParamExport paramBook = mainFrame.getBook().getParam().getParamExport();
	ExportBookToHtml exp = new ExportBookToHtml(mainFrame);
	exp.param.setFormat(FORMAT.HTML.toString());
	StringBuilder buf = new StringBuilder();
	buf.append(exp.getString());
	File file;
	if (ext == null || ext.isEmpty()) {
	    // select a file, this one may exist or not
	    file = IOUtil.fileSelect(mainFrame, "", "doc", "", "");
	} else {
	    file = new File(paramBook.getDirectory() + File.separator + mainFrame.getProject().getName());
	}
	file = new File(IOUtil.changeExt(file.getAbsolutePath(), ext));
	if (IOUtil.askReplace(mainFrame, file)) {
	    if (file.exists()) {
		file.delete();
	    }
	    boolean rc = false;
	    if (file.getAbsolutePath().endsWith("docx")) {
		DOCX doc = new DOCX(file);
		rc = doc.createDoc(mainFrame, file, buf.toString());
	    } else if (file.getAbsolutePath().endsWith("odt")) {
		ODT doc = new ODT(file);
		rc = doc.createDoc(mainFrame, file, buf.toString());
	    } else if (file.getAbsolutePath().endsWith("tex")) {
		rc = Latex.createDoc(mainFrame, file, buf.toString());
	    }
	    if (rc) {
		JOptionPane.showMessageDialog(mainFrame,
			I18N.getMsg("export.success", file.getAbsolutePath()),
			I18N.getMsg("export"), JOptionPane.INFORMATION_MESSAGE);
	    }
	    return rc;
	}
	return false;
    }

    /**
     * create a document file for a Scene
     *
     * @param mainFrame
     * @param text
     * @return
     */
    public static boolean createScene(MainFrame mainFrame, String text) {
	// select a file, this one may exist or not
	File file = selectFile(mainFrame);
	if (file == null) {
	    return false;
	}
	return createScene(mainFrame, text, file);
    }

    public static boolean createScene(MainFrame mainFrame, String text, File file) {
	if (IOUtil.askReplace(mainFrame, file)) {
	    // convert the text to a document file depending on extension
	    if (file.exists()) {
		file.delete();
	    }
	    StringBuilder buf = new StringBuilder();
	    buf.append(Html.DOCTYPE).append(Html.HTML_B).append(Html.BODY_B);
	    buf.append(text
		    .replace("«", "&laquo;").replace("»", "&raquo;")
		    .replace("“", "&ldquo;").replace("”", "&rdquo;"));
	    buf.append(Html.BODY_E).append(Html.HTML_E);
	    boolean rc = false;
	    if (file.getAbsolutePath().endsWith(".docx")) {
		DOCX doc = new DOCX(file);
		rc = doc.createDoc(mainFrame, file, buf.toString());
	    } else if (file.getAbsolutePath().endsWith(".odt")) {
		ODT doc = new ODT(file);
		rc = doc.createDoc(mainFrame, file, buf.toString());
	    } else if (file.getAbsolutePath().endsWith(".html")) {
		rc = IOUtil.fileWriteString(file, buf.toString());
	    } else if (file.getAbsolutePath().endsWith(".txt")) {
		rc = IOUtil.fileWriteString(file, Html.htmlToText(text, true));
	    } else if (file.getAbsolutePath().endsWith(".tex")) {
		rc = Latex.createDoc(mainFrame, file, buf.toString());
	    }
	    if (rc) {
		JOptionPane.showMessageDialog(mainFrame,
			I18N.getMsg("export.success", file.getAbsolutePath()),
			I18N.getMsg("export"), JOptionPane.INFORMATION_MESSAGE);
	    }
	    return true;
	}
	return false;
    }

}
