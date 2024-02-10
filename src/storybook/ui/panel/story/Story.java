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

import assistant.AssistantDlg;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.ui.MainFrame;
import storybook.ui.panel.story.StoryPanel.STORYTYPE;

/**
 * datas for the Freytag pyramid
 *
 * @author favdb
 */
public class Story implements Comparable {

	private final STORYTYPE storyType;
	private final Book.TYPE bookType;
	private final Long id;
	private final String name;
	private final int value;

	public Story(STORYTYPE type, AssistantDlg dlg, AbstractEntity entity) {
		this.storyType = type;
		this.bookType = entity.getObjType();
		this.id = entity.getId();
		this.name = entity.getName();
		AssistantDlg dlgz = new AssistantDlg(dlg.getParent(), entity);
		this.value = dlgz.getComboboxValue(type.toString());
	}

	public Book.TYPE getType() {
		return this.bookType;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public int getValue() {
		return this.value;
	}

	public AbstractEntity getEntity(MainFrame mainFrame) {
		return mainFrame.project.get(bookType, id);
	}

	@Override
	public int compareTo(Object o) {
		Story fo = (Story) o;
		String si = String.format("%02d.%02d", value, bookType.ordinal());
		String so = String.format("%02d.%02d", fo.value, fo.bookType.ordinal());
		return si.compareTo(so);
	}

	@Override
	public String toString() {
		return "storyType=" + storyType.toString() + ", bookType=" + bookType.toString() + ", id=" + id.toString()
		   + ", name='" + name + "', value=" + value;
	}

}
