/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.wys;

import api.shef.dialogs.ImageDialog;
import api.shef.tools.HtmlUtils;
import i18n.I18N;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTML;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.shortcut.Shortcuts;

/**
 * Action which desplays a dialog to insert an image
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLImageAction extends HTMLTextEditAction {

	public HTMLImageAction(WysiwygEditor editor) {
		super(editor, I18N.getMsg("shef.image_"));
		setSmallIcon(IconUtil.getIconSmall(ICONS.K.IMAGE));
		//setMnemonic(I18N.getMnem("shef.image"));
		this.setAccelerator(Shortcuts.getKeyStroke("shef", "image"));
		setShortDescription(Shortcuts.getTooltips("shef", "image"));
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		//LOG.trace("HTMLImageAction.wysiwygEditPerformed(e, editor)");
		ImageDialog d = createDialog(editor);
		d.setLocationRelativeTo(d.getParent());
		d.setVisible(true);
		if (d.hasUserCancelled()) {
			return;
		}
		String tagText = d.getHTML();
		if (editor.getCaretPosition() == editor.getDocument().getLength()) {
			tagText += "&nbsp;";
		}
		editor.replaceSelection("");
		HTML.Tag tag = HTML.Tag.IMG;
		if (tagText.startsWith("<a")) {
			tag = HTML.Tag.A;
		}
		// set the image in the HTML
		HtmlUtils.insertHTML(tagText, tag, editor);
	}

	protected ImageDialog createDialog(JTextComponent ed) {
		Window w = SwingUtilities.getWindowAncestor(ed);
		ImageDialog d = null;
		if (w != null && w instanceof Frame) {
			d = new ImageDialog((Frame) w);
		} else if (w != null && w instanceof Dialog) {
			d = new ImageDialog((Dialog) w);
		}
		return d;
	}

}
