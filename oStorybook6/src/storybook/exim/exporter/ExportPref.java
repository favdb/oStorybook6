/*
 * Copyright (C) 2020 favdb
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
package storybook.exim.exporter;

/**
 * export the preferences (not used)
 *
 * @author favdb
 */
public class ExportPref {

	public enum PREF {
		CHAPTER_BOOKTITLE("ExportChapterBooktitle", "1"),
		CHAPTER_BREAKPAGE("ExportChapterBreakPage", "0"),
		CHAPTER_DATES_LOCATIONS("ExportChapterDatesLocations", "0"),
		CHAPTER_NUMBERS("ExportChapterNumbers", "1"),
		CHAPTER_TITLES("ExportChapterTitles", "1"),
		CSV_COMMA("ExportCsvComma", ";"),
		CSV_QUOTE("ExportCsvQUOTE", "'"),
		DEFAULT_FORMAT("xml", "0"),
		DIRECTORY("ExportDirectory", ""),
		EPUB_ISBN("EpubIsbn", ""),
		EPUB_UUID("EpubUuid", ""),
		HTML_BOOK_MULTI("HTMLBookMultifile", "0"),
		HTML_BOOK_MULTISCENE("HTMLBookMultiScene", "0"),
		HTML_CSS_FILE("HTMLCssFile", ""),
		HTML_NAV("HTMLNav", "0"),
		HTML_NAV_IMAGE("HTMLNavImage", "0"),
		HTML_USE_CSS("HTMLUseCSS", "0"),
		PART_TITLES("ExportPartTitles", "1"),
		PDF_LANDSCAPE("PDFLandscape", "0"),
		PDF_PAGE_SIZE("PDFPageSize", "A4"),
		PREF("ExportPref", "010"),
		ROMAN_NUMERALS("ExportRomanNumerals", "0"),
		SCENE_DIDASCALIE("ExportSceneDidascalie", "0"),
		SCENE_SEPARATOR("ExportSceneSeparator", "0"),
		SCENE_TITLES("ExportSceneTitles", "1"),
		TXT_SEPARATOR("ExportTxtSeparator", "|"),
		TXT_TAB("ExportTxtTab", "true"),
		NONE("none", "none");
		final private String text, value;

		private PREF(String text, String value) {
			this.text = text;
			this.value = value;
		}

		public String getValue() {
			return (value);
		}

		public Integer getInteger() {
			return (Integer.parseInt(value));
		}

		public boolean getBoolean() {
			if (value.equals("1") || value.equals("true")) {
				return (true);
			} else {
				return (false);
			}
		}

		@Override
		public String toString() {
			return text;
		}
	}

}
