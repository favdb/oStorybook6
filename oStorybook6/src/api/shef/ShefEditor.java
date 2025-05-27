/**
 * Copyright (C) oStorybook Team
 *
 * This program is libre software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project.
 *
 * Other parts are from the SHEF project developed and published by Bob Tantlinger at
 * https://sourceforge.net/projects/shef/
 *
 */
package api.shef;

import api.jsoup.Jsoup;
import api.jsoup.nodes.Document;
import api.shef.editors.AbstractPanel;
import api.shef.editors.src.SourceEditor;
import api.shef.editors.wys.WysiwygEditor;
import api.shef.tools.LOG;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.FocusListener;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import storybook.tools.html.Html;

/**
 * main SHEF class
 *
 * @author favdb
 */
public class ShefEditor extends JPanel {

	public static final String TT = "ShefEditor", VERSION = "1.0";
	public static final int DLG_NONE = 0,
			DLG_EN = 1,
			DLG_FR = 2,
			DLG_ALL = 3;
	public static final int NONE = 0, REDUCED = 1, FULL = 2;
	public static final String WYSIWYG = "wysiwyg",
			SOURCE = "source",
			HTML = "html",
			NORMAL = "normal";
	private static String spelling = "none";

	public static String getSpelling() {
		return spelling;
	}

	public static void setSpelling(String spelling) {
		ShefEditor.spelling = spelling;
	}

	private WysiwygEditor wysEditor;// the Wysiwig panel
	private SourceEditor srcEditor;// the HTMLsource panel
	private AbstractPanel current;// current view mode
	private int dlgLang = DLG_ALL;// dialog marks type 0=none, 1=en, 2=fr
	private int reducedMode = FULL;// tool bar type 0=none, 1=reduced, 2=full
	private boolean colored = false,//if true then use RSyantaxArea, else use JTeaxtArea
			showStatus = true;// if false status bar must not be shown
	private String param = "";// param memorized
	private String curMod = NORMAL;// current mode
	private int maxLen = 32768;// max length of text

	/**
	 * main class
	 *
	 * @param baseDir : the base URL
	 * @param param : see documentation
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public ShefEditor(String baseDir, String param) {
		super();
		this.param = param;
		String[] p = param.split(",");
		for (String pp : p) {
			if (pp.startsWith("len ")) {
				this.maxLen = Integer.parseInt(pp.substring(pp.indexOf(" ") + 1));
			} else {
				switch (pp.toLowerCase().trim()) {
					case "colored":
						colored = true;
						break;
					case "reduced":// reduce the tool bar
						reducedMode = REDUCED;
						break;
					case "full":// full tool bar, is default
						reducedMode = FULL;
						break;
					case "none":// no tool bar
						reducedMode = NONE;
						break;
					case "nothing":// no tool bar, no status bar
						reducedMode = NONE;
						showStatus = false;
						break;
					case "lang_en":// english dialog marks
						dlgLang = DLG_EN;
						break;
					case "lang_fr":// french dialog marks
						dlgLang = DLG_FR;
						break;
					case "lang_all":// all dialog marks
						dlgLang = DLG_ALL;
						break;
					case "lang_none":// no language for dialog marks
						dlgLang = DLG_NONE;
						break;
					default:
						break;
				}
			}
		}
		initUI();
		wysEditorGet().setBase(baseDir);
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public ShefEditor(String baseDir, String param, String text) {
		this(baseDir, param);
		if (!text.startsWith("<p")) {
			setText(Html.intoP(text));
		} else {
			setText(text);
		}
	}

	/**
	 * initialize user interface
	 */
	private void initUI() {
		setLayout(new BorderLayout());
		wysEditor = new WysiwygEditor(this, param);
		current = wysEditor;
		add(wysEditor);
		srcEditor = new SourceEditor(this);
	}

	public void setBase(String base) {
		wysEditor.setBase(base);
	}

	/**
	 * set text
	 *
	 * @param text
	 */
	public void setText(String text) {
		//LOG.trace(TT+".setText(text=\""+text+"\")");
		String html = text;
		if (!text.contains("<html")) {
			html = Html.toCleanHtml(text);
		}
		if (current instanceof WysiwygEditor) {
			wysEditor.setText(html);
		} else {
			srcEditor.setText(html);
		}
	}

	/**
	 * set the editor editable or not
	 *
	 * @param editable
	 */
	public void setEditable(boolean editable) {
		wysEditor.getWysEditor().setEditable(editable);
		srcEditor.getEditor().setEditable(editable);
	}

	public void setMaxLen(int maxLen) {
		this.maxLen = maxLen;
		wysEditor.setMaxLen(maxLen);
	}

	/**
	 * set the caret position
	 *
	 * @param pos
	 */
	public void setCaretPosition(int pos) {
		//LOG.trace(TT + ".setCaretPosition(pos=\"" + pos + "\")");
		if (current instanceof WysiwygEditor) {
			wysEditor.setCaretPosition(pos);
		} else if (current instanceof SourceEditor) {
			srcEditor.setCaretPosition(pos);
		}
	}

	/**
	 * get the text
	 *
	 * @return
	 */
	public String getText() {
		String text = wysEditor.getText();
		if (current instanceof SourceEditor) {
			text = srcEditor.getText();
		}
		Document doc = Jsoup.parse(text);
		return doc.body().html();
	}

	/**
	 * change to Wysiwig or to source view
	 *
	 * @param type: may be "wysiwig" or "source"
	 */
	public void changeTo(String type) {
		String text = getText();
		switch (type) {
			case WYSIWYG:
				current = wysEditor;
				break;
			case SOURCE:
				current = srcEditor;
				break;
			case HTML:
				curMod = curMod.equals(HTML) ? NORMAL : HTML;
				wysEditor.setEditable(curMod.equals(NORMAL));
				break;
			default:
				return;
		}
		current.setText(text);
		current.setCaretPosition(0);
		this.removeAll();
		this.add(current);
		this.revalidate();
		this.repaint();
	}

	/**
	 * get the wysiwig editor
	 *
	 * @return
	 */
	public WysiwygEditor wysEditorGet() {
		return wysEditor;
	}

	/**
	 * check if the text is changed
	 *
	 * @return true if text is changed
	 */
	public boolean isTextChanged() {
		return wysEditor.isTextChanged();
	}

	/**
	 * set the wysiwig editor font
	 *
	 * @param font
	 */
	public void wysEditorSetFont(Font font) {
		this.wysEditor.setFont(font);
	}

	public void wysEditorAddFocusListener(FocusListener l) {
		this.wysEditor.addFocusListener(l);
	}

	/**
	 * set the source editor font
	 *
	 * @param font
	 */
	public void sourceSetFont(Font font) {
		this.srcEditor.setFont(font);
	}

	/**
	 * check if RSyntaxArea may be used or JTextArea
	 *
	 * @return true if RSyntaxArea else JTextArea
	 */
	public boolean isColored() {
		return this.colored;
	}

	public boolean isStatus() {
		return this.showStatus;
	}

	/**
	 * print out the version
	 *
	 */
	public void printVersion() {
		LOG.message("SHEF editor version " + VERSION);
	}

	public void insertText(String link) {
		HTMLEditorKit editorKit;
		HTMLDocument document;
		JEditorPane editorPane;
		try {
			editorPane = wysEditor.getWysEditor();
			editorKit = (HTMLEditorKit) editorPane.getEditorKit();
			document = (HTMLDocument) editorPane.getDocument();
			int caret = editorPane.getCaretPosition();
			editorKit.insertHTML(document, caret, link, 0, 0, javax.swing.text.html.HTML.Tag.A);
			wysEditor.setChange();
		} catch (ClassCastException | BadLocationException | IOException ex) {
			storybook.tools.LOG.log(TT + ".createEndnote exception");
		}

	}

	public void removeTag(String str) {
		wysEditor.removeTag(str);
	}

	public WysiwygEditor getWysiwyg() {
		return wysEditor;
	}

	public JTextComponent getWysEditor() {
		return wysEditor.getWysEditor();
	}

	public void showHideTB() {
		wysEditor.showHideTB();
	}

}
