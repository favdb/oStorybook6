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
package storybook.tools.comparator;

import java.util.Comparator;
import java.util.Date;
import storybook.db.abs.AbstractEntity;

/**
 * @author favdb
 */
public class ObjectComparator implements Comparator<Object> {

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	@SuppressWarnings("null")
	public int compare(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return 0;
		} else if (o1 == null && o2 != null) {
			return -1;
		} else if (o1 != null && o2 == null) {
			return 1;
		}
		if (o1 instanceof AbstractEntity || o2 instanceof AbstractEntity) {
			return compareEntity(o1, o2);
		} else if (o1 instanceof String || o2 instanceof String) {
			return compareString(o1, o2);
		} else if (o1 instanceof Integer || o2 instanceof Integer) {
			return compareInteger(o1, o2);
		} else if (o1 instanceof Long || o2 instanceof Long) {
			return compareLong(o1, o2);
		} else if (o1 instanceof Date || o2 instanceof Date) {
			return compareDate(o1, 02);
		} else {
			return o1.toString().compareTo(o2.toString());
		}
	}

	/**
	 * compare String objects
	 *
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareString(Object o1, Object o2) {
		if (!(o1 instanceof String)) {
			return -1;
		}
		if (!(o2 instanceof String)) {
			return 1;
		}
		return o1.toString().compareTo(o2.toString());
	}

	/**
	 * compare Integer objects
	 *
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareInteger(Object o1, Object o2) {
		if (!(o1 instanceof Integer)) {
			return -1;
		}
		if (!(o2 instanceof Integer)) {
			return 1;
		}
		return ((Integer) o1).compareTo((Integer) o2);
	}

	/**
	 * compare Long objects
	 *
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareLong(Object o1, Object o2) {
		if (!(o1 instanceof Long)) {
			return -1;
		}
		if (!(o2 instanceof Long)) {
			return 1;
		}
		return ((Long) o1).compareTo((Long) o2);
	}

	/**
	 * compare Date objects
	 *
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareDate(Object o1, Object o2) {
		if (!(o1 instanceof Date)) {
			return -1;
		}
		if (!(o2 instanceof Date)) {
			return 1;
		}
		return ((Date) o1).compareTo((Date) o2);
	}

	/**
	 * compare Entity objects
	 *
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareEntity(Object o1, Object o2) {
		if (!(o1 instanceof AbstractEntity)) {
			return -1;
		}
		if (!(o2 instanceof AbstractEntity)) {
			return 1;
		}
		AbstractEntity e1 = (AbstractEntity) o1;
		AbstractEntity e2 = (AbstractEntity) o2;
		return e1.getName().compareToIgnoreCase(e2.getName());
	}

}
