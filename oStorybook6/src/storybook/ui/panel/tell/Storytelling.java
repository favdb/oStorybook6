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

import assistant.AssistantDlg;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.ui.MainFrame;
import storybook.ui.panel.tell.TellingPanel.TELLTYPE;

/**
 * datas for the telling type
 *
 * @author favdb
 */
public class Storytelling implements Comparable {

    private final TELLTYPE tellType;
    private final Book.TYPE bookType;
    private final Long id;
    private final String name;
    private int value = 0;

    public Storytelling(TELLTYPE type, AssistantDlg dlg, AbstractEntity entity) {
	this.tellType = type;
	this.bookType = entity.getObjType();
	this.id = entity.getId();
	this.name = entity.getName();
	AssistantDlg dlgz = new AssistantDlg(dlg.getParent(), entity);
	this.value = dlgz.getCbValue(type.toString());
    }

    /**
     * get the Book TYPE
     *
     * @return
     */
    public Book.TYPE getType() {
	return this.bookType;
    }

    /**
     * get the id
     *
     * @return
     */
    public Long getId() {
	return this.id;
    }

    /**
     * get the name
     *
     * @return
     */
    public String getName() {
	return this.name;
    }

    /**
     * get the value
     *
     * @return
     */
    public int getValue() {
	return this.value;
    }

    /**
     * get the AbstractEntity
     *
     * @param mainFrame
     *
     * @return
     */
    public AbstractEntity getEntity(MainFrame mainFrame) {
	return mainFrame.project.get(bookType, id);
    }

    /**
     * compare two Storytelling
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
	Storytelling fo = (Storytelling) o;
	String si = String.format("%02d.%02d", value, bookType.ordinal());
	String so = String.format("%02d.%02d", fo.value, fo.bookType.ordinal());
	return si.compareTo(so);
    }

    @Override
    public String toString() {
	return "storyType=" + tellType.toString() + ", bookType=" + bookType.toString() + ", id=" + id.toString()
		+ ", name='" + name + "', value=" + value;
    }

}
