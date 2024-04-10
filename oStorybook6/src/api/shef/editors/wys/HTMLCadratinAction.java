/**
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.wys;

import i18n.I18N;
import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument;
import resources.icons.IconUtil;
import api.shef.actions.CompoundManager;
import storybook.shortcut.Shortcuts;

/**
 * Action which inserts special characters like cadratin
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLCadratinAction extends HTMLTextEditAction {

	private static final String TT = "HTMLCadratinAction";

	public static final int CADRATIN = 0,
		DLG_OPEN_EN = 1,
		DLG_CLOSE_EN = 2,
		DLG_OPEN_FR = 3,
		DLG_CLOSE_FR = 4,
		UNBREAK = 5;
	public static final String TYPES[][] = {
		{"char_cadratin", "—\u00A0"},
		{"dlg_open_en", "“\u00A0"},
		{"dlg_close_en", "\u00A0”"},
		{"dlg_open_fr", "«\u00A0"},
		{"dlg_close_fr", "\u00A0»"},
		{"char_unbreak", "\u00A0"}
	};
	private final int type;

	public HTMLCadratinAction(WysiwygEditor editor, int c) {
		super(editor, TYPES[c][0]);
		type = c;
		setShortDescription(I18N.getMsg("shef." + TYPES[type][0]));
		setSmallIcon(IconUtil.getIconSmall(TYPES[type][0]));
		if (type == UNBREAK) {
			setAccelerator(Shortcuts.getKeyStroke("shef", "char_unbreak"));
		}
	}

	int nbfun = 0;

	@Override
	protected void wysiwygEditPerformed(ActionEvent evt, JEditorPane editor) {
		//LOG.trace(TT + "wysiwygEditPerformed(evt, editor) nbfun=" + nbfun);
		if (nbfun > 0) {
			nbfun = 0;
			return;
		}
		if (evt.getSource() instanceof JMenuItem) {
			nbfun++;
		}
		try {
			HTMLDocument document = (HTMLDocument) editor.getDocument();
			CompoundManager.beginCompoundEdit(document);
			int caret = editor.getCaretPosition();
			if (caret < 1) {
				return;
			}
			String c = TYPES[type][1];
			String x = editor.getSelectedText();
			if (x == null || x.isEmpty()) {
				editor.setSelectionStart(caret);
				editor.setSelectionEnd(caret);
			}
			editor.replaceSelection(c);
			SwingUtilities.invokeLater(() -> {
				nettoyage(editor);
			});
			CompoundManager.endCompoundEdit(document);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

	public static void nettoyage(JEditorPane editor) {
		//LOG.trace(TT + ".nettoyage(editor)");
		int caret = editor.getCaretPosition();
		String txt = editor.getText();
		int z = txt.contains("&#160; ") ? 1 : 0;
		txt = txt.replace("\u00A0 ", "\u00A0");
		txt = txt.replace("&nbsp; ", "&nbsp;");
		txt = txt.replace("&#160; ", "&#160;");
		editor.setText(txt);
		editor.setCaretPosition(caret - z);
	}
}
