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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import storybook.db.category.Category;

/**
 *
 * @author favdb
 */
public class Dataset {

	public String type;
	public List<DatasetItem> items = new ArrayList<>();
	public List<Category> selectedCategories = new ArrayList<>();
	public List<String> idList = new ArrayList<>();
	public Date lastDate, firstDate;
	public int marginT, marginB, marginL, marginR;
	public int intervalX, intervalY, areaWidth, areaHeight;
	public long intervalDate, intervalValue, maxValue;
	public double average;

	public Dataset() {
	}

	public DatasetItem getItem(String id) {
		if (items != null) {
			for (DatasetItem item : items) {
				if (item.id.equals(id)) {
					return item;
				}
				if (item.subItems != null) {
					for (DatasetItem subItem : item.subItems) {
						if (subItem.id.equals(id)) {
							return subItem;
						}
					}
				}
			}
		}
		return null;
	}

	public int getIdIndex(String id) {
		int i = 0;
		for (String x : idList) {
			if (x.equals(id)) {
				return (i);
			}
			i++;
		}
		return (-1);
	}

}
