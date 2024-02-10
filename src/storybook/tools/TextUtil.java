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
package storybook.tools;

import storybook.tools.html.Html;

/**
 * @author martin mod by favdb
 *
 */
public class TextUtil {

	public static String trunc(String str, int i) {
		if (str.length() <= i) {
			return str;
		}
		return str.substring(0, i);
	}

	private TextUtil() {
		//nothing
	}

	public static String ELLIPSIS = "\u2026";

	/**
	 * count the number of words in a text
	 *
	 * @param text
	 * @return
	 */
	public static int countWords(String text) {
		if (text == null) {
			return 0;
		}
		String txt = Html.htmlToText(text, true).replaceAll("[^ a-z A-Z 0-9 ]+", " ");
		while (txt.contains("  ")) {
			txt = txt.replace("  ", " ");
		}
		if (text.trim().isEmpty()) {
			return 0;
		}
		String[] words = txt.trim().split("\\s+");
		int count = words.length;
		for (String word : words) {
			String w = word.trim();
			if (w.length() == 0) {
				count--;
			}
		}
		/*String punc = "#`’~!#$%^，";
		for (String word : words) {
			String w = word.trim();
			if (w.length() == 0 || punc.contains(w)) {
				count--;
			}
		}
		if (count > 0) {
			count--;
		}*/
		return count;
	}

	/**
	 * count the number of characters in text
	 *
	 * @param text
	 * @return
	 */
	public static int countChars(String text) {
		if (text == null || text.isEmpty()) {
			return (0);
		}
		if (text.contains("<") && text.contains(">")) {
			return Html.htmlToText(text, true).trim().length();
		}
		return text.trim().length();
	}

	/**
	 * convert a String in array of lines
	 *
	 * @param str
	 * @return
	 */
	public static String[] getTextLines(String str) {
		return str.split("\\r?\\n");
	}

	/**
	 * trim the text line by line
	 *
	 * @param str
	 * @return
	 */
	public static String trimText(String str) {
		StringBuilder buf = new StringBuilder();
		String[] lines = getTextLines(str);
		for (String line : lines) {
			buf.append(line.trim()).append("\n");
		}
		return buf.toString();
	}

	/**
	 * truncate the String to 200 characters and add an ellipsis character
	 *
	 * @param text
	 * @return
	 */
	public static String ellipsize(String text) {
		return TextUtil.ellipsize(text, 200);
	}

	/**
	 * truncate the String to max length and add an ellipsis character
	 *
	 * @param str
	 * @param max
	 * @return
	 */
	public static String ellipsize(String str, int max) {
		if (str == null || str.isEmpty()) {
			return "";
		}
		if (max < 1) {//ellipsize at first space
			if (str.contains(" ")) {
				return str.substring(0, str.indexOf(" ")) + ELLIPSIS;
			}
		} else if (str.length() > max) {
			for (int j = max; j > 0; j--) {
				if (str.charAt(j) == ' ') {
					return str.substring(0, j) + ELLIPSIS;
				}
			}
			return str.substring(0, max) + ELLIPSIS;
		}
		return str;
	}

	/**
	 * check if String contains only digits
	 *
	 * @param str
	 * @return
	 */
	public static boolean isNumber(String str) {
		char[] t = str.toCharArray();
		for (char c : t) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * check if the String contains only alpha characters
	 *
	 * @param str
	 * @return
	 */
	public static boolean checkAlpha(String str) {
		return str.matches("[a-zA-Z]+");
	}

	/**
	 * replace punctuation spaced characters by a normal puntuation in the String
	 *
	 * @param str
	 * @return
	 */
	public static String normalPunctuation(String str) {
		String r = str;
		String punc = ",.;:-_\t?!%";
		for (int i = 0; i < punc.length(); i++) {
			r = r.replace(" " + punc.charAt(i) + " ", punc.charAt(i) + " ");
		}
		return r;
	}

}
