/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.wys;

import i18n.I18N;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.JEditorPane;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import api.shef.actions.CompoundManager;
import api.shef.tools.HtmlUtils;
import storybook.shortcut.Shortcuts;

/**
 * @author Bob Tantlinger
 *
 */
public class HTMLEditPasteFormattedAction extends HTMLTextEditAction {

    private static final String TITLE = "shortcut.paste_formatted";

    /**
     * @param editor
     */
    public HTMLEditPasteFormattedAction(WysiwygEditor editor) {
	super(editor, I18N.getMsg("shef.paste_formatted"));
	putValue(MNEMONIC_KEY, I18N.getMnem("shef.paste_formatted"));
	putValue(SMALL_ICON, IconUtil.getIconSmall(ICONS.K.EDIT_PASTE));
	putValue(ACCELERATOR_KEY, Shortcuts.getKeyStroke("shef", "paste_formatted"));
	addShouldBeEnabledDelegate((Action a) -> {
	    if (getCurrentEditor() == null) {
		return false;
	    }
	    Transferable content
		= Toolkit.getDefaultToolkit().getSystemClipboard().getContents(HTMLEditPasteFormattedAction.this);

	    if (content == null) {
		return false;
	    }
	    DataFlavor flv = DataFlavor.selectBestTextFlavor(content.getTransferDataFlavors());
	    return flv != null && flv.getMimeType().startsWith("text/html");
	});
	putValue(Action.SHORT_DESCRIPTION, Shortcuts.getTooltips("shef", "paste_formatted"));
    }

    @Override
    protected void updateWysiwygContextState(JEditorPane wysEditor) {
	this.updateEnabledState();
    }

    @Override
    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
	String htmlFragment = null;
	try {
	    htmlFragment = getHTMLFragment();
	} catch (UnsupportedFlavorException | IOException ex) {
	    ex.printStackTrace(System.err);
	}
	if (htmlFragment != null) {
	    CompoundManager.beginCompoundEdit(editor.getDocument());
	    HtmlUtils.insertArbitraryHTML(htmlFragment, editor);
	    CompoundManager.endCompoundEdit(editor.getDocument());
	}
    }

    /**
     * Get the HTML text from the content if any
     *
     * @return returns the html fragment, or null if this content isn't HTML
     * @throws UnsupportedFlavorException
     * @throws IOException
     */
    private String getHTMLFragment() throws IOException, UnsupportedFlavorException {
	Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
	Transferable c = clip.getContents(this);
	if (c == null) {
	    return null;
	}
	DataFlavor flv = DataFlavor.selectBestTextFlavor(c.getTransferDataFlavors());
	if (!flv.getMimeType().startsWith("text/html")) {
	    return null;
	}
	String text = read((flv.getReaderForText(c)));
	//when html content is retrieved from the transferable, the copied part
	//is enclosed in a <body> tag, so only get the contents we want...
	int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
	Pattern p = Pattern.compile(("<\\s*body\\b([^<>]*)>"), flags);
	Matcher m = p.matcher(text);
	if (m.find()) {
	    text = text.substring(m.end(), text.length());
	}
	p = Pattern.compile("<\\s*/\\s*body\\s*>", flags);
	m = p.matcher(text);
	if (m.find()) {
	    text = text.substring(0, m.start());
	}
	//when html content is retrieved from the transferable, the copied part
	//is surrounded with the comments <!--StartFragment--> and <!--EndFragment--> on windows
	text = text.replaceAll("<\\!\\-\\-StartFragment\\-\\->", "");
	text = text.replaceAll("<\\!\\-\\-EndFragment\\-\\->", "");
	//gets rid of 'class' and 'id' attributes in the tags.
	//It really doesn't make much sense to include these attribs in HTML
	//pasted in from the wild.
	String r = "<([^>]*)(?:class|id)\\s*=\\s*(?:'[^']*'|\"\"[^\"\"]*\"\"|[^\\s>]+)([^>]*)>";
	p = Pattern.compile(r, flags);
	//run it twice for each attrib
	m = p.matcher(text);
	text = m.replaceAll("<$1$2>");
	m = p.matcher(text);
	text = m.replaceAll("<$1$2>");
	return text;
    }

    public String read(Reader input) throws IOException {
	BufferedReader reader = new BufferedReader(input);
	StringBuilder b = new StringBuilder();
	int ch;
	try {
	    while ((ch = reader.read()) != -1) {
		b.append((char) ch);
	    }
	} catch (IOException ex) {
	    throw ex;
	} finally {
	    try {
		reader.close();
	    } catch (IOException ex) {
	    }
	}
	return b.toString();
    }

}
