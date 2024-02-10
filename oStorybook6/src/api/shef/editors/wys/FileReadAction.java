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
import storybook.exim.importer.ImportDocument;
import storybook.tools.file.IOUtil;
import storybook.ui.MainFrame;

/**
 * Select all action
 *
 * @author Bob
 */
public class FileReadAction extends HTMLTextEditAction {

	private final MainFrame mainFrame;

	public FileReadAction(MainFrame mainFrame, WysiwygEditor editor) {
		super(editor, I18N.getMsg("shef.read"));
		this.mainFrame = mainFrame;
		setShortDescription(I18N.getMsg("shef.fromfile"));
		setAccelerator(Shortcuts.getKeyStroke("shef", "read"));
		setSmallIcon(IconUtil.getIconSmall(ICONS.K.F_IMPORT));
	}

	@Override
	protected void updateWysiwygContextState(JEditorPane ed) {
		setSelected(true);
	}

	@Override
	protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editorPane) {
		File inFile = IOUtil.fileSelect(editorPane, "", "doc", "", "");
		if (inFile == null) {
			return;
		}
		ImportDocument doc = new ImportDocument(mainFrame, inFile);
		if (doc.openDocument()) {
			String str = doc.getDocument().body().html();
			if (!str.isEmpty()) {
				editorPane.setText(str);
			}
			doc.close();
		}
	}

}
