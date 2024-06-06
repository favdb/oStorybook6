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
package storybook.ui.panel.tell;

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
 * creation of a new Chapter
 *
 * @author favdb
 */
public class TellingNewChapter extends AbsDialog {

    private final TellingPanel caller;
    private final String typeString;
    private JComboBox combo;
    private JTextField tfName;
    private String msgError;
    private AssistantDlg dlgAssistant;

    public static boolean show(TellingPanel frame) {
	TellingNewChapter dlg = new TellingNewChapter(frame);
	dlg.setVisible(true);
	return !dlg.isCanceled();
    }

    public TellingNewChapter(TellingPanel frame) {
	super(frame.getMainFrame());
	this.caller = frame;
	this.typeString = "story." + caller.type.toString();
	initAll();
    }

    /**
     * initialize
     */
    @Override
    public void init() {

    }

    /**
     * initialize the UI
     */
    @Override
    public void initUi() {
	setLayout(new MigLayout(MIG.WRAP + " 2"));
	setTitle(I18N.getMsg("chapter.new"));
	add(new JLabel(I18N.getColonMsg(typeString)), MIG.RIGHT);
	//setting the combo box to select the rigt element
	dlgAssistant = new AssistantDlg(this, new Chapter());
	combo = (JComboBox) dlgAssistant.findComponentByName(null, caller.type.toString());
	if (combo != null) {
	    add(combo);
	} else {
	    add(new JLabel(I18N.getMsg(typeString + ".empty")));
	}
	// setting the name for the new Chapter
	add(new JLabel(I18N.getColonMsg("name")), MIG.get(MIG.SPAN, MIG.SPLIT2));
	tfName = new JTextField();
	add(tfName, MIG.get(MIG.GROW));
	// add OK and Cancel buttons
	add(getOkButton(), MIG.get(MIG.SPAN, MIG.RIGHT, MIG.SPLIT2));
	add(getCancelButton());
	pack();
	this.setModal(true);
	setLocationRelativeTo(caller);
    }

    /**
     * OKaction
     *
     */
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

    /**
     * verifier for the data
     *
     * @return true if there is no error
     */
    public boolean verifier() {
	resetError();
	if (combo.getSelectedIndex() < 1) {
	    errorMsg(combo, Const.ERROR_MISSING);
	}
	return msgError.isEmpty();
    }

    /**
     * reset the error before any verification
     */
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
	//LOG.trace(TT+"doOk()");
	Chapter chapter = new Chapter();
	chapter.setPart(mainFrame.lastPartGet());
	chapter.setChapterno(mainFrame.project.chapters.getNextNumber());
	chapter.setTitle(tfName.getText());
	chapter.setAssistant(dlgAssistant.getResult());
	mainFrame.getBookModel().ENTITY_New(chapter);
	mainFrame.setUpdated();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	// empty
    }

}
