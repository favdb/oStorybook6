/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.wys;

import api.shef.tools.HtmlUtils;
import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import javax.swing.text.html.HTML;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.db.endnote.Endnote;
import storybook.db.scene.Scene;
import storybook.shortcut.Shortcuts;
import storybook.ui.MainFrame;

/**
 *
 */
public class HTMLEndnoteAddAction extends HTMLTextEditAction {

	private static final String TT = "HTMLEndnoteAddAction";
	private final MainFrame mainFrame;
	private final Scene scene;

	public HTMLEndnoteAddAction(WysiwygEditor editor, MainFrame mainFrame, Scene scene) {
		super(editor, "endnote_add");
		this.mainFrame = mainFrame;
		this.scene = scene;
		setSmallIcon(IconUtil.getIconSmall(ICONS.K.CHAR_ENDNOTE));
		setAccelerator(Shortcuts.getKeyStroke("shef", "endnote_add"));
		setShortDescription(Shortcuts.getTooltips("shef", "endnote_add"));
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent evt, JEditorPane editorPane) {
		//LOG.trace(TT + ".wysiwygEditPerformed(evt, editor) nbfun=" + nbfun);
		int num = 1;
		for (Endnote en : mainFrame.project.endnotes.find(Endnote.TYPE.ENDNOTE, scene)) {
			num = Math.max(num, ((Endnote) en).getNumber() + 1);
		}
		Endnote en = new Endnote(Endnote.TYPE.ENDNOTE.ordinal(), scene, num);
		en.setSort(editorPane.getCaretPosition());
		if (mainFrame.showEditorAsDialog(en)) {
			return;
		}
		en = mainFrame.project.endnotes.find(Endnote.TYPE.ENDNOTE.ordinal(), scene, num);
		String link = Endnote.linkTo("", en);
		editorPane.replaceSelection("");
		HtmlUtils.insertHTML(link, HTML.Tag.A, editorPane);
		this.wysiwygEditor.btEndnoteShow.setEnabled(true);
	}

}
