/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.idea;

import api.mig.swing.MigLayout;
import api.shef.ShefEditor;
import i18n.I18N;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import resources.icons.IconUtil;
import storybook.dialog.AbsDialog;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class FoiDlg extends AbsDialog {

	private ShefEditor ideaEditor;

	public FoiDlg(MainFrame mainFrame) {
		super(mainFrame);
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		super.initUi();
		setLayout(new MigLayout(MIG.get(MIG.WRAP, MIG.FILL), "", ""));
		setTitle(I18N.getMsg("foi.title"));
		setIconImage(IconUtil.getIconImageSmall("bulb"));
		JLabel lbTitle = new JLabel(I18N.getColonMsg("foi.new"));
		ideaEditor = new ShefEditor("", "lang_all, disallow, colored");
		// layout
		add(lbTitle);
		add(ideaEditor, MIG.GROW);
		add(getOkButton(), MIG.get(MIG.SPLIT2, MIG.SG, MIG.RIGHT));
		add(getCancelButton(), MIG.SG);
		pack();
		setLocationRelativeTo(mainFrame);
	}

	public String getText() {
		return ideaEditor.getText();
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canceled = false;
				dispose();
				Idea idea = new Idea();
				idea.setStatus(0);
				idea.setCategory(I18N.getMsg("foi.title"));
				idea.setNotes(ideaEditor.getText());
				mainFrame.getBookController().newEntity(idea);
			}
		};
	}

	public static void show(MainFrame m) {
		FoiDlg dlg = new FoiDlg(m);
		dlg.setModal(true);
		dlg.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
