/*
 * Copyright (C) 2020 favdb
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
package storybook.db;

import storybook.db.abs.AbstractEntity;
import storybook.db.part.Part;
import storybook.db.chapter.Chapter;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.sql.Timestamp;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import storybook.db.book.Book;
import storybook.edit.AbstractEditor;
import storybook.db.DB.DATA;
import storybook.tools.StringUtil;
import storybook.tools.swing.js.JSDateChooser;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class Objective implements CaretListener {

	private Timestamp timeObj, timeDone;
	private Integer sizeObj = 0, sizeDone = 0;
	private JTextField tfSize, tfDoneSize;
	private JSDateChooser dObjective, dDone;

	public Objective(AbstractEntity entity, Integer done) {
		switch (Book.getTYPE(entity)) {
			case CHAPTER: {
				Chapter chapter = (Chapter) entity;
				set(chapter.getObjectiveChars(),
				   chapter.getObjectiveTime(),
				   chapter.getDoneTime(),
				   done);
				break;
			}
			case PART: {
				Part part = (Part) entity;
				set(part.getObjectiveChars(),
				   part.getObjectiveTime(),
				   part.getDoneTime(),
				   done);
				break;
			}
		}
	}

	private void set(Integer sizeObj,
	   Timestamp timeObj,
	   Timestamp timeDone,
	   Integer sizeDone) {
		this.sizeObj = sizeObj;
		this.timeObj = timeObj;
		this.timeDone = timeDone;
		this.sizeDone = sizeDone;
	}

	public Integer getSizeObj() {
		return sizeObj;
	}

	public void setSizeObj(Integer size) {
		this.sizeObj = size;
	}

	public Timestamp getTimeObj() {
		return timeObj;
	}

	public void setTimeObj(Timestamp time) {
		this.timeObj = time;
	}

	public Timestamp getTimeDone() {
		return timeDone;
	}

	public void setTimeDone(Timestamp time) {
		this.timeDone = time;
	}

	public Integer getSizeDone() {
		return sizeDone;
	}

	public void setSizeDone(Integer size) {
		this.sizeDone = size;
	}

	public JPanel getPanel(AbstractEditor src) {
		JPanel p = new JPanel(new MigLayout(MIG.WRAP + " 2", "[][grow]"));
		p.setBorder(BorderFactory.createTitledBorder(I18N.getColonMsg("objective")));
		tfSize = Ui.initIntegerField(p, DATA.OBJECTIVE_SIZE.n(), 5, getSizeObj(), BNONE);
		dObjective = src.initSbDate(p, "objective.date", getTimeObj(), BMANDATORY, 1);
		dDone = src.initSbDate(p, "objective.done", getTimeDone(), BMANDATORY, 1);
		tfDoneSize = Ui.initIntegerField(p, "objective.done_size", 4, getSizeDone(), BINFO);
		if (getSizeObj() < 1) {
			tfSize.setText("");
			dObjective.setEnabled(false);
			dDone.setEnabled(false);
		}
		tfSize.addCaretListener(this);
		refreshDone();
		return p;
	}

	public JPanel getPanel(MainFrame mainFrame) {
		JPanel p = new JPanel(new MigLayout(MIG.WRAP + " 2", "[][grow]"));
		p.setBorder(BorderFactory.createTitledBorder(I18N.getColonMsg("objective")));
		tfSize = Ui.initIntegerField(p, DATA.OBJECTIVE_SIZE.n(), 5, getSizeObj(), BNONE);
		dObjective = initSbDate(mainFrame, p, "objective.date", getTimeObj(), BNONE, 1);
		if (getSizeObj() < 1) {
			tfSize.setText("");
			dObjective.setEnabled(false);
		}
		tfSize.addCaretListener(this);
		refreshDone();
		return p;
	}

	public void check(AbstractEditor src) {
		if (!tfSize.getText().isEmpty()) {
			if (!StringUtil.isNumeric(tfSize.getText())) {
				src.errorMsg(tfSize, "error.notnumeric");
			}
			if (dObjective.getDate() != null && dDone.getDate() != null) {
				if (dObjective.getDate().after(dDone.getDate())) {
					src.errorMsg(dObjective, "error.date_incorrect");
				}
			}
		}
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		Object prop = e.getSource();
		if (prop instanceof JTextField) {
			if (((JTextField) prop).getName().equals("objective.size")) {
				if (tfSize.getText().isEmpty() || Integer.parseInt(tfSize.getText()) < 1) {
					dObjective.setEnabled(false);
					dDone.setEnabled(false);
				} else {
					dObjective.setEnabled(true);
					dDone.setEnabled(true);
				}
				refreshDone();
			}
		}
	}

	public void apply(AbstractEntity entity) {
		switch (Book.getTYPE(entity)) {
			case PART:
				applyToPart((Part) entity);
				break;
			case CHAPTER:
				applyToChapter((Chapter) entity);
				break;
		}
	}

	public void applyToPart(Part part) {
		if (tfSize.getText().isEmpty()) {
			part.setObjectiveChars(0);
			part.setObjectiveTime(null);
			part.setDoneTime(null);
		} else {
			part.setObjectiveChars(Integer.valueOf(tfSize.getText()));
			part.setObjectiveTime(dObjective.getTimestamp());
			part.setDoneTime(dDone.getTimestamp());
		}
	}

	public void applyToChapter(Chapter chapter) {
		if (tfSize.getText().isEmpty()) {
			chapter.setObjectiveChars(0);
			chapter.setObjectiveTime(null);
			chapter.setDoneTime(null);
		} else {
			chapter.setObjectiveChars(Integer.valueOf(tfSize.getText()));
			chapter.setObjectiveTime(dObjective.getTimestamp());
			chapter.setDoneTime(dDone.getTimestamp());
		}
	}

	public static JSDateChooser initSbDate(MainFrame mainFrame,
	   JPanel p,
	   String title,
	   Date date,
	   String opt, int time) {
		JSDateChooser d = new JSDateChooser(mainFrame, time);
		d.setName(title);
		if (date != null) {
			d.setDate(date);
		}
		addField(p, title, "", d, null, opt);
		return (d);
	}

	private void refreshDone() {
		if (tfSize == null || sizeDone == 0 || tfSize.getText().isEmpty()) {
			return;
		}
		int p = (sizeDone * 100) / Integer.parseInt(tfSize.getText());
		if (p < 50) {
			tfDoneSize.setBackground(Color.red);
		} else if (p < 95) {
			tfDoneSize.setBackground(Color.yellow);
		} else {
			tfDoneSize.setBackground(Color.green);
		}
	}

}
