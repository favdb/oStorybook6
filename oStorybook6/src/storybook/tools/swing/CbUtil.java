/*
 * SbApp: Open Source software for novelists and authors.
 * Original idea 2008 - 2012 Martin Mustun
 * Copyrigth (C) Favdb
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
 */
package storybook.tools.swing;

import assistant.Assistant;
import i18n.I18N;
import java.util.Collections;
import java.util.List;
import javax.swing.JComboBox;
import storybook.renderer.EntityLCR;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.db.category.Category;
import storybook.db.status.Status;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class CbUtil {

	private CbUtil() {
		// empty
	}

	public static boolean NEW = true, EMPTY = true, ALL = true;

	@SuppressWarnings("unchecked")
	public static void fillCbStage(MainFrame mainFrame, JComboBox cb, String toSel) {
		cb.removeAllItems();
		cb.addItem(" ");//empty item
		List<String> list = Assistant.getListOf("scene", "stage");
		if (list == null || list.isEmpty()) {
			return;
		}
		for (String elem : list) {
			cb.addItem(elem);
		}
		if (toSel != null && !toSel.isEmpty()) {
			cb.setSelectedItem(toSel);
		}
	}

	@SuppressWarnings("unchecked")
	public static void fillCbStatus(JComboBox cb, int sel) {
		cb.removeAllItems();
		if (sel == -1) {
			cb.addItem(I18N.getMsg("change.no"));
		}
		for (int i = 0; i < Status.STATUS.values().length; i++) {
			cb.addItem(i);
		}
		if (sel != -1 && sel < cb.getItemCount()) {
			cb.setSelectedIndex(sel);
		}
	}

	@SuppressWarnings("unchecked")
	public static void fillEntity(MainFrame mainFrame, JComboBox cb, Book.TYPE type,
	   AbstractEntity toSel, AbstractEntity toHide, String options) {
		List<AbstractEntity> entities = (List) mainFrame.project.getList(type);
		if (type == Book.TYPE.CATEGORY) {
			Collections.sort(entities, (AbstractEntity e1, AbstractEntity e2)
			   -> ((Category) e1).getSort().compareTo(((Category) e2).getSort()));
		} else {
			Collections.sort(entities, (AbstractEntity e1, AbstractEntity e2)
			   -> e1.getFullName().compareTo(e2.getFullName()));
		}
		fillEntity(cb, entities, toSel, toHide, options);
	}

	public static void fillEntity(MainFrame mainFrame, JComboBox cb, Book.TYPE type,
	   Long toSel, AbstractEntity toHide, String options) {
		@SuppressWarnings("unchecked")
		List<AbstractEntity> entities = (List) mainFrame.project.getList(type);
		if (type == Book.TYPE.CATEGORY) {
			Collections.sort(entities, (AbstractEntity e1, AbstractEntity e2)
			   -> ((Category) e1).getSort().compareTo(((Category) e2).getSort()));
		} else {
			Collections.sort(entities, (AbstractEntity e1, AbstractEntity e2)
			   -> e1.getName().compareTo(e2.getName()));
		}
		fillCbEntity(cb, entities, toSel, toHide, options);
		//cb.revalidate();
	}

	@SuppressWarnings("unchecked")
	public static void fillEntity(JComboBox cb,
	   List<AbstractEntity> entities, AbstractEntity toSel, AbstractEntity toHide, String options) {
		cb.removeAllItems();
		cb.setRenderer(new EntityLCR());
		if (isAddEmpty(options)) {
			cb.addItem(" ");
		}
		if (isAddAll(options)) {
			cb.addItem(I18N.getMsg("all"));
		}
		entities.forEach((entity) -> {
			if (toHide == null || !entity.equals(toHide)) {
				cb.addItem(entity);
			}
		});
		if (toSel != null) {
			cb.setSelectedItem(toSel);
		}
		cb.revalidate();
	}

	/**
	 * fill a JComboBox with given Entities
	 *
	 * @param cb
	 * @param entities
	 * @param toSel Entity to select
	 * @param toHide Entity to hide
	 * @param options Empty allowed, All allowed
	 */
	@SuppressWarnings("unchecked")
	public static void fillCbEntity(JComboBox cb, List<AbstractEntity> entities,
	   Long toSel, AbstractEntity toHide,
	   String options) {
		cb.removeAllItems();
		cb.setRenderer(new EntityLCR());
		if (isAddEmpty(options)) {
			cb.addItem(" ");
		}
		if (isAddAll(options)) {
			cb.addItem(I18N.getMsg("all"));
		}
		int s = -1, i = 0;
		for (AbstractEntity entity : entities) {
			if (toHide == null || !entity.equals(toHide)) {
				cb.addItem(entity);
				if (entity.getId().equals(toSel)) {
					s = i;
				}
				i++;
			}
		}
		if (s != -1) {
			cb.setSelectedItem(s);
		}
		cb.revalidate();
	}

	private static boolean isAddEmpty(String x) {
		return x.charAt(1) == '1';
	}

	private static boolean isAddAll(String x) {
		return x.charAt(2) == '1';
	}

}
