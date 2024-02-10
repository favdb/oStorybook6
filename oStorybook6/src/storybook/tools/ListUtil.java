/*
 * Copyright (C) 2017 FaVdB
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
package storybook.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import storybook.db.abs.AbstractEntity;

/**
 *
 * @author FaVdB
 */
public class ListUtil {

	public static String find(List<String> list, String val) {
		for (String s : list) {
			if (s.contains(val)) {
				return s;
			}
		}
		return "";
	}

	private ListUtil() {
		// nothing
	}

	public static String join(List array) {
		return (join(array, ","));
	}

	public static String join(List array, String separator) {
		//App.trace("ListUtil.join(array="+array.toString()+", separator)");
		String sep = separator;
		if (separator == null) {
			sep = "";
		}
		final StringBuilder buf = new StringBuilder();
		if (array != null) {
			for (int i = 0; i < array.size(); i++) {
				if (i > 0) {
					buf.append(sep);
				}
				if (array.get(i) != null) {
					Object obj = (Object) array.get(i);
					if (obj instanceof String) {
						buf.append(array.get(i));
					} else if (obj instanceof AbstractEntity) {
						buf.append(((AbstractEntity) obj).getName());
					} else {
						buf.append(obj.toString());
					}
				}
			}
		}
		return buf.toString();
	}

	public static String join(String[] str) {
		StringBuilder b = new StringBuilder();
		for (String s : str) {
			if (!b.toString().isEmpty()) {
				b.append(", ");
			}
			b.append(str);
		}
		return b.toString();
	}

	@SuppressWarnings("unchecked")
	public static List setUnique(List array) {
		List list = new ArrayList();
		if (array != null) {
			for (Object o : array.toArray()) {
				if (o != null && !list.contains(o)) {
					list.add(o);
				}
			}
		}
		return (list);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static List removeNullAndDuplicates(List list) {
		list.removeAll(Collections.singletonList(null));
		Set set = new LinkedHashSet(list);
		return new ArrayList(set);
	}
}
