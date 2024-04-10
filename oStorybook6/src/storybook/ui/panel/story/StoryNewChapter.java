/*
 * Copyright (C) 2023 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.ui.panel.story;

import api.mig.swing.MigLayout;
import assistant.AssistantDlg;
import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import storybook.Const;
import storybook.db.chapter.Chapter;
import storybook.dialog.AbsDialog;
import storybook.ui.MIG;

/**
 *
 * @author favdb
 */
public class StoryNewChapter extends AbsDialog {

	private final StoryPanel caller;
	private final String typeString;
	private JComboBox combo;
	private JTextField tfName;
	private String msgError;
	private AssistantDlg dlgAssistant;

	public static boolean show(StoryPanel frame) {
		StoryNewChapter dlg = new StoryNewChapter(frame);
		dlg.setVisible(true);
		return !dlg.isCanceled();
	}

	public StoryNewChapter(StoryPanel frame) {
		super(frame.getMainFrame());
		this.caller = frame;
		this.typeString = caller.type.toString();
		initAll();
	}

	@Override
	public void init() {

	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.WRAP + " 2"));
		// select the right Freytag value
		add(new JLabel(I18N.getColonMsg("story." + typeString)), MIG.RIGHT);
		dlgAssistant = new AssistantDlg(this, new Chapter());
		combo = (JComboBox) dlgAssistant.findComponentByName(null, typeString);
		if (combo != null) {
			add(combo);
		} else {
			add(new JLabel(I18N.getMsg("story.no")));
		}
		// setting the name for the new Chapter
		add(new JLabel(I18N.getColonMsg("name")), MIG.RIGHT);
		tfName = new JTextField();
		add(tfName, MIG.get(MIG.GROW));
		// add OK and Cancel buttons
		add(getOkButton(), "span, right,split 2");
		add(getCancelButton());
		pack();
		this.setModal(true);
		setLocationRelativeTo(caller);
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (verifier()) {
					doOK();
					canceled = false;
					dispose();
				}
			}
		};
	}

	public boolean verifier() {
		resetError();
		if (combo.getSelectedIndex() < 1) {
			errorMsg(combo, Const.ERROR_MISSING);
		}
		return msgError.isEmpty();
	}

	public void resetError() {
		msgError = "";
		if (tfName.getText().isEmpty()) {
			errorMsg(tfName, Const.ERROR_MISSING);
		}
	}

	/**
	 * add to error message field
	 *
	 * @param comp
	 * @param msg
	 */
	public void errorMsg(JComponent comp, String msg) {
		msgError += I18N.getColonMsg(comp.getName()) + " " + I18N.getMsg(msg) + "\n";
		comp.setBorder(new LineBorder(Color.red, 1));
	}

	private void doOK() {
		Chapter chapter = new Chapter();
		chapter.setPart(mainFrame.lastPartGet());
		chapter.setChapterno(mainFrame.project.chapters.getNextNumber());
		chapter.setTitle(tfName.getText());
		chapter.setAssistant(dlgAssistant.getResult());
		mainFrame.getBookModel().ENTITY_New(chapter);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
