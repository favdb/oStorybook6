/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.editors.src;

import javax.swing.text.JTextComponent;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**
 *
 * @author favdb
 */
public class ExtSrcEditor {

	public static JTextComponent getRsyntax() {
		RSyntaxTextArea edClass = new RSyntaxTextArea();
		edClass.setSyntaxEditingStyle(org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_HTML);
		return (JTextComponent) edClass;
	}

}
