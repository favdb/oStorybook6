/*
 * Copyright (C) 2016 favdb
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
package storybook.ui.chart.occurences;

import java.awt.Color;
import java.util.Date;
import java.util.List;
import storybook.tools.DateUtil;
import storybook.tools.swing.ColorUtil;

/**
 *
 * @author favdb
 */
public class DatasetItem {

	private static final String TT = "DatasetItem.";
	String id;
	public Date debut;
	public Date fin;
	long value;
	Color color;
	List<DatasetItem> subItems;

	public DatasetItem(String id, Date debut, Date fin, Color color) {
		this.id = id;
		this.debut = debut;
		this.fin = fin;
		this.color = color;
		this.subItems = null;
		//LOG.trace(TT + "(" + toString() + ")");
	}

	public DatasetItem(String id, long v, Color c) {
		this.id = id;
		this.value = v;
		this.color = c;
		this.subItems = null;
	}

	public void setSubItem(List<DatasetItem> subItems) {
		this.subItems = subItems;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getId() {
		return (id);
	}

	public Iterable<DatasetItem> getSubItems() {
		return (subItems);
	}

	public String toString() {
		return (id
		   + ", " + DateUtil.simpleDateToString(debut)
		   + ", " + DateUtil.simpleDateToString(fin)
		   + ", " + ColorUtil.toHexString(color));
	}

}
