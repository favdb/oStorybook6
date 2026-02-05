/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import api.shef.ShefEditor;
import i18n.I18N;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.Icon;
import resources.icons.ICONS;
import resources.icons.IconUtil;

/**
 * Dialog to enter an end note
 *
 * @author favdb
 */
public class HTMLEndnoteDialog extends HTMLOptionDialog {

	private static final String ENDNOTE = "endnote";

	private static Icon icon = IconUtil.getIconSmall(ICONS.K.CHAR_ENDNOTE);
	private static String title = I18N.getMsg("shef." + ENDNOTE);
	private static String desc = I18N.getMsg("shef." + ENDNOTE);
	private ShefEditor eNote;

	/**
	 * Constructor if parent is a Frame
	 *
	 * @param parent: parent frame
	 * @param text	: text to initialize
	 */
	public HTMLEndnoteDialog(Frame parent, String text) {
		super(parent, title, desc, icon);
		initialize(text);
	}

	/**
	 * Constructor if the parent is a Dialog
	 *
	 * @param parent: dialog parent
	 * @param text	: text to initialize
	 */
	public HTMLEndnoteDialog(Dialog parent, String text) {
		super(parent, title, desc, icon);
		initialize(text);
	}

	/**
	 * return the link to the endnote in HTML format
	 *
	 * @return : the formatted text
	 */
	@Override
	public String getHTML() {
		String html = "<a ";
		html += "name=\"" + ENDNOTE + "\" ";
		html += "\">";
		html += eNote.getText();
		html += "</a>";
		return html;
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize(String text) {
		eNote = new ShefEditor("", "reduced, disallow");
		eNote.setText(text);
		this.setContentPane(eNote);
		this.pack();
		this.setMinimumSize(new Dimension(640, 480));
		this.setLocationRelativeTo(this.getParent());
		eNote.wysEditorGet().getWysEditor().requestFocus();
		this.setModal(true);
		this.pack();
	}

	/**
	 * return the text of the endnote
	 *
	 * @return : the text
	 */
	public String getText() {
		return eNote.getText();
	}

}
