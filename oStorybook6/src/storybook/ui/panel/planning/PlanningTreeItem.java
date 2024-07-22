/*
 * Copyright (C) 2024 favdb
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
package storybook.ui.panel.planning;

import storybook.db.abs.AbstractEntity;
import storybook.db.chapter.Chapter;
import storybook.db.part.Part;
import storybook.db.scene.Scene;

/**
 *
 * @author favdb
 */
public class PlanningTreeItem {

	public int type = 0, //0=String, 1=Part, 2=Chapter, 3=Scene
		size = 0,// size of the elemnt in nbChars
		max = 0; // maximum size of thes elements
	public String name;
	public AbstractEntity entity;

	public PlanningTreeItem(String name, int max) {
		this.type = 0;
		this.entity = null;
		this.name = name;
		this.max = max;
	}

	public PlanningTreeItem(AbstractEntity entity, int max) {
		int t = 0;
		switch (entity.getObjType()) {
			case PART:
				t = 1;
				break;
			case CHAPTER:
				t = 2;
				break;
			case SCENE:
				t = 3;
				break;
		}
		this.type = t;
		this.entity = entity;
		this.name = entity.getName();
		this.max = max;
	}

	private static final String OPEN_PAR = "    (", CLOSE_PAR = ")";

	private String intoPar(int s) {
		return OPEN_PAR + s + CLOSE_PAR;
	}

	private String intoPar(int s, int max) {
		return OPEN_PAR + s + "/" + max + CLOSE_PAR;
	}

	@Override
	public String toString() {
		String t;
		String notes = "";
		switch (this.type) {
			case 1:
				Part part = (Part) entity;
				if (part.hasNotes()) {
					notes = "*";
				}
				t = part.getName() + notes + intoPar(size, max);
				max = part.getObjectiveChars();
				break;
			case 2:
				Chapter chapter = (Chapter) entity;
				if (chapter.hasNotes()) {
					notes = "*";
				}
				max = chapter.getObjectiveChars();
				t = chapter.getName() + notes + intoPar(size, max);
				break;
			case 3:
				Scene scene = ((Scene) entity);
				if (scene.hasNotes()) {
					notes = "*";
				}
				t = scene.getTitle() + notes + intoPar(size);
				break;
			default:
				t = name + intoPar(size, max);
				break;
		}
		return t;
	}

}
