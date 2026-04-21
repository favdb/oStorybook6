/*
 * Copyright (C) 2021 favdb
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

import i18n.I18N;
import java.awt.Dimension;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import static storybook.db.book.Book.TYPE.*;
import storybook.ui.frames.main.MainFrame;

/**
 *
 * @author favdb
 */
public class EntityCb extends JComboBox {

	private final MainFrame mainFrame;
	private final Long toSel;
	private final Long toHide;
	private final Book.TYPE type;
	private final String options;

	/**
	 * Create a JComboBox for entities
	 *
	 * @param mainFrame
	 * @param type: type of entities
	 * @param toSel: Id to select
	 * @param toHide: entity to hide
	 * @param options : parameters, first for adding empty item, second for adding all item
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public EntityCb(MainFrame mainFrame, Book.TYPE type, Long toSel,
	   AbstractEntity toHide, String options) {
		super();
		setMinimumSize(new Dimension(150, 10));
		this.setMaximumRowCount(10);
		this.mainFrame = mainFrame;
		setName("cbScenes");
		this.type = type;
		this.toSel = toSel;
		if (toHide == null) {
			this.toHide = -1L;
		} else {
			this.toHide = toHide.getId();
		}
		String px = options;
		while (px.length() < 3) {
			px += "0";
		}
		this.options = options;
		reload();
	}

	@SuppressWarnings("unchecked")
	public void reload() {
		int curSel = this.getSelectedIndex();
		DefaultComboBoxModel cbModel = (DefaultComboBoxModel) this.getModel();
		cbModel.removeAllElements();
		if (isAddEmpty(options)) {
			cbModel.addElement((String) "");
		}
		EntityCbItem memo = null;
		List entities;
		switch (type) {
			case ATTRIBUTE:
				entities = (List) mainFrame.project.attributes.getList();
				break;
			case CATEGORY:
				entities = (List) mainFrame.project.categorys.getList();
				break;
			case CHAPTER:
				entities = (List) mainFrame.project.chapters.getList();
				break;
			case ENDNOTES:
				entities = (List) mainFrame.project.endnotes.getList();
				break;
			case EVENT:
				entities = (List) mainFrame.project.events.getList();
				break;
			case GENDER:
				entities = (List) mainFrame.project.genders.getList();
				break;
			case IDEA:
				entities = (List) mainFrame.project.ideas.getList();
				break;
			case ITEM:
				entities = (List) mainFrame.project.items.getList();
				break;
			case LOCATION:
				entities = (List) mainFrame.project.locations.getList();
				break;
			case MEMO:
				entities = (List) mainFrame.project.memos.getList();
				break;
			case PART:
				entities = (List) mainFrame.project.parts.getList();
				break;
			case PERSON:
				entities = (List) mainFrame.project.persons.getList();
				break;
			case PLOT:
				entities = (List) mainFrame.project.plots.getList();
				break;
			case RELATION:
				entities = (List) mainFrame.project.relations.getList();
				break;
			case SCENE:
				entities = (List) mainFrame.project.scenes.getList();
				break;
			case STRAND:
				entities = (List) mainFrame.project.strands.getList();
				break;
			case TAG:
				entities = (List) mainFrame.project.tags.getList();
				break;
			default:
				return;
		}
		for (Object obj : entities) {
			AbstractEntity entity = (AbstractEntity) obj;
			if (!entity.getId().equals(toHide)) {
				EntityCbItem cbEntity = new EntityCbItem(entity);
				cbModel.addElement(cbEntity);
				if (toSel != -1L && toSel.equals(entity.getId())) {
					memo = cbEntity;
				}
			}
		}
		if (isAddAll(options)) {
			this.addItem(I18N.getMsg("all"));
		}
		if (toSel != -1L && memo != null) {
			this.setSelectedItem(memo);
		} else if (curSel != -1) {
			this.setSelectedIndex(curSel);
		}
		this.setModel(cbModel);
	}

	private static boolean isAddEmpty(String x) {
		if (x.length() < 1) {
			return false;
		}
		return x.charAt(0) == '1';
	}

	private static boolean isAddAll(String x) {
		if (x.length() < 2) {
			return false;
		}
		return x.charAt(1) == '1';
	}

}
