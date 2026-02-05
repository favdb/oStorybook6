/*
 * Copyright (C) 2019 favdb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.db.endnote;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.JTextField;
import storybook.Const;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.edit.AbstractEditor;
import storybook.edit.Editor;
import storybook.tools.html.Html;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class EndnoteEdit extends AbstractEditor {

	private static final String TT = "EditEndnote";

	private JTextField tfNumber;
	private Endnote endnote;

	public EndnoteEdit(Editor m, AbstractEntity e) {
		super(m, e, "010");
		initAll();
	}

	@Override
	public void initUpper() {
		// the name must not be editable
		tfName.setEditable(false);
		endnote = (Endnote) entity;
		tfNumber = Ui.initIntegerField(pUpper, "number", 3, endnote.getNumber(), BINFO);
		if (endnote.getNumber() < 1) {
			List endnotes = mainFrame.project.getList(Book.TYPE.ENDNOTE);
			endnote.setNumber(Endnote.getNextNumber(endnotes));
			tfNumber.setText(endnote.getNumber().toString());
		}
	}

	@Override
	public boolean verifier() {
		resetError();
		if (hNotes.getText().isEmpty()) {
			errorMsg(hNotes, Const.ERROR_MISSING);
		} else if (hNotes.getText().length() > 32768) {
			errorMsg(hNotes, Const.ERROR_EXCEED);
		}
		return msgError.isEmpty();
	}

	@Override
	public void apply() {
		//LOG.trace(TT + ".apply()");
		tfName.setText(String.format("%03d", endnote.getNumber()));
		entity.setNotes(hNotes != null ? Html.checkImages(mainFrame, hNotes.getText()) : "");
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
