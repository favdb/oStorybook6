/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.wys;

import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.shortcut.Shortcuts;
import storybook.db.endnote.Endnote;
import storybook.db.scene.Scene;
import storybook.ui.MainFrame;
import storybook.db.endnote.EndnotesListDlg;

/**
 *
 */
public class HTMLEndnoteShowAction extends HTMLTextEditAction {

	private static final String TT = "HTMLEndnoteShowAction";
	private final MainFrame mainFrame;
	private final Scene scene;

	public HTMLEndnoteShowAction(WysiwygEditor editor, MainFrame mainFrame, Scene scene) {
		super(editor, "endnote_show");
		this.mainFrame = mainFrame;
		this.scene = scene;
		setSmallIcon(IconUtil.getIconSmall(ICONS.K.SORT));
		setAccelerator(Shortcuts.getKeyStroke("shef", "endnote_show"));
		setShortDescription(Shortcuts.getTooltips("shef", "endnote_show"));
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent evt, JEditorPane editor) {
		//LOG.trace(TT + ".wysiwygEditPerformed(evt, editor) nbfun=" + nbfun);
		EndnotesListDlg.showDlg(mainFrame, Endnote.TYPE.ENDNOTE.ordinal(), scene);
	}

}
