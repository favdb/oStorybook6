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
package storybook.db.chapter;

import api.mig.swing.MigLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import storybook.Const;
import storybook.db.Objective;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.book.BookUtil;
import storybook.db.part.Part;
import storybook.edit.AbstractEditor;
import storybook.edit.Editor;
import storybook.tools.StringUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class ChapterEdit extends AbstractEditor {

	private static final String TT = "EditChapter";

	private JComboBox cbPart;
	private JTextField tfNumber;
	private Objective objective;

	public ChapterEdit(Editor m, AbstractEntity e) {
		super(m, e, "111");
		setName(TT);
		initAll();
	}

	@Override
	public void initUpper() {
		Chapter chapter = (Chapter) entity;
		if (chapter.getId() == -1) {
			chapter.setPart(mainFrame.lastPartGet());
			chapter.setChapterno(0);
		}
		JPanel pleft = new JPanel(new MigLayout(MIG.get(MIG.WRAP), "[][]"));
		cbPart = Ui.initCbEntities(pleft, this, "part", Book.TYPE.PART, chapter.getPart(),
		   null, BNEW + BEMPTY);
		tfNumber = Ui.initIntegerField(pleft, "number", 5, chapter.getChapterno(), BMANDATORY);
		if (chapter.getChapterno() < 1) {
			tfNumber.setText("+");
		}
		pUpper.add(pleft, MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.TOP, MIG.GROW));
		objective = new Objective(entity, BookUtil.getNbChars(mainFrame.project, chapter));
		pUpper.add(objective.getPanel(this), MIG.SPAN);
	}

	@Override
	public boolean verifier() {
		JTextField tf = new JTextField();
		resetError();
		tfNumber.setBorder(tf.getBorder());
		if (cbPart.getSelectedIndex() < 0) {
			errorMsg(cbPart, Const.ERROR_MISSING);
		}
		List chapters = mainFrame.project.getList(Book.TYPE.CHAPTER);
		if (tfNumber.getText().equals("+")) {
			tfNumber.setText(Chapter.getNextNumber(chapters).toString());
		} else if (tfNumber.getText().isEmpty()) {
			errorMsg(tfNumber, Const.ERROR_MISSING);
		} else if (!StringUtil.isNumeric(tfNumber.getText())) {
			errorMsg(tfNumber, Const.ERROR_NOTNUMERIC);
		} else {
			int n = Integer.parseInt(tfNumber.getText());
			Chapter c = Chapter.findNumber(chapters, n);
			if (c != null && !Objects.equals(c.getId(), entity.getId())) {
				errorMsg(tfNumber, Const.ERROR_EXISTS);
			}
		}
		objective.check(this);
		return (msgError.isEmpty());
	}

	@Override
	public void apply() {
		Chapter chapter = (Chapter) entity;
		chapter.setTitle(tfName.getText());
		if (cbPart.getSelectedIndex() >= 1) {
			chapter.setPart((Part) cbPart.getSelectedItem());
			mainFrame.lastPartSet((Part) cbPart.getSelectedItem());
		} else {
			chapter.setPart((Part) null);
		}
		if (tfNumber.getText().equals("+")) {
			chapter.setChapterno(Chapter.getNextNumber(mainFrame.project.getList(Book.TYPE.CHAPTER)));
		} else {
			chapter.setChapterno(Integer.valueOf(tfNumber.getText()));
		}
		if (chapter.getId() == -1) {
			chapter.setCreationTime(new Timestamp(System.currentTimeMillis()));
		}
		objective.apply(entity);
		mainFrame.lastChapterSet(chapter);
		super.apply();
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
