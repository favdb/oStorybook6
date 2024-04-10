/*
 * Copyright (C) 2023 favdb
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
package storybook.exim.importer;

import java.sql.Timestamp;
import java.util.List;
import storybook.db.abs.AbstractEntity;
import storybook.tools.LOG;
import storybook.tools.StringUtil;

/**
 *
 * @author favdb
 */
public class SqlUtil {

	public SqlUtil() {
		//empty
	}

	private static int findIndex(List<String> ls, String name) {
		for (int i = 0; i < ls.size(); i++) {
			if (ls.get(i).equalsIgnoreCase(name)) {
				return i;
			}
		}
		return -1;
	}

	public static void getCommon(List<String> ls, AbstractEntity e, List<String> s) {
		e.setId(getLong(ls, "ID", s));
		e.setCreation(getString(ls, "CREATION", s));
		e.setMaj(getString(ls, "MAJ", s));
		e.setName(getString(ls, "NAME", s));
		e.setNotes(getString(ls, "NOTES", s));
		e.setDescription(getString(ls, "DESCRIPTION", s));
		e.setAssistant(getString(ls, "ASSISTANT", s));
	}

	public static boolean getBoolean(String s) {
		if (!s.isEmpty()) {
			return s.equals("1") || s.equalsIgnoreCase("true");
		}
		return false;
	}

	public static boolean getBoolean(List<String> ls, String name, List<String> s) {
		return (ls.isEmpty() ? false : getBoolean(getString(ls, name, s)));
	}

	public static Integer getInteger(String s) {
		if (!s.isEmpty()) try {
			return Integer.valueOf(s);
		} catch (NumberFormatException ex) {

		}
		return 0;
	}

	public static Integer getInteger(List<String> ls, String name, List<String> s) {
		return (ls == null || ls.isEmpty() ? 0 : getInteger(getString(ls, name, s)));
	}

	public static Long getLong(String s) {
		if (!s.isEmpty()) try {
			return Long.valueOf(s);
		} catch (NumberFormatException ex) {

		}
		return -1L;
	}

	public static Long getLong(List<String> ls, String name, List<String> s) {
		return (ls == null || ls.isEmpty() ? -1L : getLong(getString(ls, name, s)));
	}

	public static String getString(String s) {
		if (s.isEmpty() || s.equals("NULL") || s.equals("''")) {
			return "";
		}
		String sx = s.trim();
		if (s.startsWith("STRINGDECODE")) {
			sx = s.substring(s.indexOf('(') + 1, s.lastIndexOf(')'));
		}
		sx = sx.replace("''", "&apos;");
		int st = sx.indexOf("\'") + 1, en = sx.lastIndexOf("\'");
		if (en - st > 0) {
			try {
				sx = sx.substring(st, en).trim();
			} catch (Exception ex) {
				LOG.err(s + "\n" + ex.getLocalizedMessage() + " st=" + st + ", e=" + en);
			}
		} else if (en - 1 == st) {
			return "";
		}
		return StringUtil.javaDecode(sx).replace("&apos;", "'");
	}

	public static String getString(List<String> ls, String name, List<String> s) {
		//LOG.trace(s.toString());
		if (!ls.isEmpty()) {
			int i = findIndex(ls, name);
			if (i != -1 && i < s.size()) {
				return getString(s.get(i).trim());
			}
		}
		return "";
	}

	public static Timestamp getTimestamp(String s) {
		//LOG.trace("getTimestamp(s=\"" + s + "\")");
		if (!s.isEmpty() && !s.equalsIgnoreCase("NULL")) {
			String sx = s;
			if (sx.contains("TIMESTAMP ")) {
				sx = sx.replace("TIMESTAMP ", "");
			}
			if (sx.endsWith("'")) {
				sx = sx.replace("'", "");
			}
			try {
				return Timestamp.valueOf(sx.trim());
			} catch (Exception ex) {
				//LOG.err(ex.getLocalizedMessage() + " from=\"" + sx + "\"");
			}
		}
		return null;
	}

	public static Timestamp getTimestamp(List<String> ls, String name, List<String> s) {
		return (ls == null || ls.isEmpty() ? null : getTimestamp(getString(ls, name, s)));
	}

}
