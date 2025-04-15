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
import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JColorChooser;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledEditorKit;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.tools.swing.ColorUtil;

/**
 * Action which edits HTML font color
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLFontColorAction extends HTMLTextEditAction {

	public HTMLFontColorAction(WysiwygEditor editor) {
		super(editor, I18N.getMsg("shef.color_"));
		String mnem = I18N.getMsg("shef.color_.mnemonic");
		if (!mnem.startsWith("!")) {
			setMnemonic(mnem);
		}
		setSmallIcon(IconUtil.getIconSmall(ICONS.K.COLOR));
		setShortDescription(I18N.getMsg("shef.color_"));
		if (!mnem.startsWith("!")) {
			setShortDescription(getShortDescription()
					+ " (" + I18N.getMsg("shef.alt") + "+" + mnem + ")");
		}
	}

	protected void sourceEditPerformed(ActionEvent e, JEditorPane editor) {
		Color c = getColorFromUser(editor);
		if (c == null) {
			return;
		}

		String prefix = "<font color=\"" + ColorUtil.getHTML(c) + "\">";
		String postfix = "</font>";
		String sel = editor.getSelectedText();
		if (sel == null) {
			editor.replaceSelection(prefix + postfix);
			int pos = editor.getCaretPosition() - postfix.length();
			if (pos >= 0) {
				editor.setCaretPosition(pos);
			}
		} else {
			sel = prefix + sel + postfix;
			editor.replaceSelection(sel);
		}
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
		Color color = getColorFromUser(editor);
		if (color != null) {
			Action a = new StyledEditorKit.ForegroundAction("Color", color);
			a.actionPerformed(e);
		}
	}

	private Color getColorFromUser(Component c) {
		Window win = SwingUtilities.getWindowAncestor(c);
		if (win != null) {
			c = win;
		}
		Color color = JColorChooser.showDialog(c, I18N.getMsg("color.choose"), Color.black);
		return color;
	}

}
