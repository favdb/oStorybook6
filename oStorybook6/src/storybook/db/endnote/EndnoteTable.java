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
package storybook.db.endnote;

import i18n.I18N;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import resources.icons.ICONS;
import storybook.App;
import storybook.ctrl.ActKey;
import storybook.db.DB;
import storybook.db.abs.AbsColumn;
import static storybook.db.abs.AbsColumn.*;
import storybook.db.abs.AbstractEntity;
import storybook.db.abs.AbsTable;
import storybook.db.book.Book;
import storybook.review.Review;
import storybook.ui.MainFrame;
import storybook.ui.Ui;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class EndnoteTable extends AbsTable {

	private static final String TT = "EndnoteTable", BT_ENDNOTES = "BtENDNOTES";
	private JComboBox cbType;

	public EndnoteTable(MainFrame mainFrame) {
		super(mainFrame, Book.TYPE.ENDNOTE);
	}

	@Override
	public void init() {
		//LOG.printInfos(TT + ".init()");
		this.withPart = false;
		allowMultiDelete = false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JToolBar initToolbar() {
		//LOG.printInfos(TT + ".initToolbar()");
		super.initToolbar();
		toolbar.add(new JLabel(I18N.getColonMsg("endnote.type")));
		toolbar.add(initCbType());
		return toolbar;
	}

	/**
	 * initialize the type filter
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JComboBox initCbType() {
		cbType = new JComboBox();
		cbType.setName("cbType");
		cbType.addItem(I18N.getMsg("all"));
		cbType.addItem(I18N.getMsg("endnote"));
		cbType.addItem(I18N.getMsg("comment"));
		cbType.setSelectedIndex(0);
		cbType.addActionListener(e -> fillTable());
		return cbType;
	}

	/**
	 * initialize the footer
	 *
	 * @return
	 */
	@Override
	public JToolBar initFooter() {
		JToolBar footer = super.initFooter();
		btNew.setVisible(false);
		btCopy.setVisible(false);
		btDelete.setVisible(false);
		footer.add(Ui.initButton(BT_ENDNOTES, "", ICONS.K.REFRESH,
		   "endnotes.renumber", e -> {
			   if (Endnote.renumber(mainFrame, 0)) {
				   mainFrame.setUpdated();
				   fillTable();
			   }
		   }));
		return footer;
	}

	@Override
	protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
		ActKey act = new ActKey(evt);
		if (act.isUpdate()
		   && (act.getType() == Book.TYPE.ENDNOTE)) {
			fillTable();
		}
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		return (Endnote) mainFrame.project.get(Book.TYPE.ENDNOTE, id);
	}

	@Override
	public void updateRow(AbstractEntity entity) {
		if (Book.getTYPE(entity) == Book.TYPE.SCENE) {
			fillTable();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<AbstractEntity> getAllEntities() {
		//LOG.printInfos(TT + ".getAllEntities()");
		int ntype = cbType.getSelectedIndex();
		List endnotes;
		switch (ntype) {
			case 1:
				endnotes = Endnote.find(mainFrame, Endnote.TYPE.ENDNOTE);
				break;
			case 2:
				endnotes = Review.find(mainFrame);
				break;
			default:
				endnotes = mainFrame.project.getList(Book.TYPE.ENDNOTE);
				break;
		}
		return endnotes;
	}

	@Override
	public List<AbsColumn> getColumns(AbstractEntity entity) {
		List<AbsColumn> cols = super.getColumns(entity);

		cols.add(new AbsColumn(mainFrame, cols, "endnote.type"));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.NUMBER, NUMERIC, AL_CENTER));
		cols.add(new AbsColumn(mainFrame, cols, DB.DATA.SCENE, TCR_ENTITY));
		if (App.isDev()) {
			cols.add(new AbsColumn(mainFrame, cols, DB.DATA.SORT));
		}

		this.getColumnsEnd(cols, entity);
		return (cols);
	}

	@Override
	public List<Object> getRow(AbstractEntity entity) {
		//LOG.printInfos(TT + ".getRow(entity)");
		List<Object> cols = super.getRow(entity);
		Endnote e = (Endnote) entity;
		cols.add(e.getTypeLib());
		cols.add(e.getNumber());
		cols.add(e.getScene());
		if (App.isDev()) {
			cols.add(e.getSort());
		}
		getRowEnd(cols, entity);
		return (cols);
	}

}
