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
import java.io.File;
import javax.swing.JEditorPane;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.shortcut.Shortcuts;
import storybook.exim.exporter.ExportBookToDoc;
import storybook.tools.file.IOUtil;
import storybook.tools.html.Html;
import storybook.ui.MainFrame;

/**
 * Select all action
 *
 * @author Bob
 */
public class FileSaveAction extends HTMLTextEditAction {

	private final MainFrame mainFrame;

	public FileSaveAction(MainFrame mainFrame, WysiwygEditor editor) {
		super(editor, I18N.getMsg("shef.save"));
		this.mainFrame = mainFrame;
		setShortDescription(Shortcuts.getTooltips("shef", "save"));
		setAccelerator(Shortcuts.getKeyStroke("shef", "save"));
		setSmallIcon(IconUtil.getIconSmall(ICONS.K.F_EXPORT));
	}

	@Override
	protected void updateWysiwygContextState(JEditorPane ed) {
		setSelected(true);
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editorPane) {
		String str = editorPane.getSelectedText();
		File file = IOUtil.fileSelect(editorPane, "", "doc", "", "save");
		if (file == null) {
			return;
		}
		if (str == null || str.isEmpty()) {
			str = editorPane.getText();
		}
		if (!str.startsWith("<p")) {
			str = Html.intoP(str);
		}
		ExportBookToDoc.createScene(mainFrame, str, file);
	}

}
