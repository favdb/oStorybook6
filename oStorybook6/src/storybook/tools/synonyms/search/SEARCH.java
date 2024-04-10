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
package storybook.tools.synonyms.search;

import java.util.ArrayList;
import java.util.List;
import storybook.tools.synonyms.Synonyms;

/**
 *
 * @author favdb
 */
public class SEARCH {

	private static final String TT = "SEARCH";
	public static final String SYNONYMS = "synonyms", ANTONYMS = "antonyms";

	public static String getUrl(String type) {
		switch (type) {
			case SYNONYMS:
				return getSynonymsUrl();
			case ANTONYMS:
				return getAntonymsUrl();
		}
		return "";
	}

	public static String getSynonymsUrl() {
		switch (Synonyms.lang) {
			case "ca":
				return SEARCH_ca.URL_SYNONYMS;
			case "en":
				return SEARCH_en.URL_SYNONYMS;
			case "fr":
				return SEARCH_fr.URL_SYNONYMS;
			case "hu":
				return SEARCH_hu.URL_SYNONYMS;
			case "pt":
				return SEARCH_pt.URL_SYNONYMS;
			default:
				return "";
		}
	}

	public static String getSynonymsUrlPrompt() {
		switch (Synonyms.lang) {
			case "ca":
				return SEARCH_ca.URL_SYNONYMS_PROMPT;
			case "en":
				return SEARCH_en.URL_SYNONYMS_PROMPT;
			case "fr":
				return SEARCH_fr.URL_SYNONYMS_PROMPT;
			case "hu":
				return SEARCH_hu.URL_SYNONYMS_PROMPT;
			case "pt":
				return SEARCH_pt.URL_SYNONYMS_PROMPT;
			default:
				return "";
		}
	}

	public static String getAntonymsUrl() {
		switch (Synonyms.lang) {
			case "ca":
				return SEARCH_ca.URL_ANTONYMS;
			case "en":
				return SEARCH_en.URL_ANTONYMS;
			case "fr":
				return SEARCH_fr.URL_ANTONYMS;
			case "hu":
				return SEARCH_hu.URL_ANTONYMS;
			case "pt":
				return SEARCH_pt.URL_ANTONYMS;
			default:
				return "";
		}
	}

	public static String getUrlPrompt(String type) {
		switch (type) {
			case SYNONYMS:
				return getSynonymsUrlPrompt();
			case ANTONYMS:
				return getAntonymsUrlPrompt();
		}
		return "";
	}

	public static String getAntonymsUrlPrompt() {
		switch (Synonyms.lang) {
			case "ca":
				return SEARCH_ca.URL_ANTONYMS_PROMPT;
			case "en":
				return SEARCH_en.URL_ANTONYMS_PROMPT;
			case "fr":
				return SEARCH_fr.URL_ANTONYMS_PROMPT;
			case "hu":
				return SEARCH_hu.URL_ANTONYMS_PROMPT;
			case "pt":
				return SEARCH_pt.URL_ANTONYMS_PROMPT;
			default:
				return "";
		}
	}

	private static SEARCH_Abstract getSearch(String word) {
		//LOG.trace(TT + ".getSearch(word=" + word + ")");
		SEARCH_Abstract search;
		switch (Synonyms.lang) {
			case "ca":
				search = new SEARCH_ca(word);
				break;
			case "en":
				search = new SEARCH_en(word);
				break;
			case "fr":
				search = new SEARCH_fr(word);
				break;
			case "hu":
				search = new SEARCH_hu(word);
				break;
			case "pt":
				search = new SEARCH_pt(word);
				break;
			default:
				return null;
		}
		return search;
	}

	public static List<String> get(String type, String word) {
		//LOG.trace(TT + ".get(type=" + type + ", word=" + word + ")");
		switch (type) {
			case SYNONYMS:
				return getSynonyms(word);
			case ANTONYMS:
				return getAntonyms(word);
		}
		return new ArrayList<>();
	}

	public static List<String> getSynonyms(String word) {
		//LOG.trace(TT + ".synonyms(word=" + word + ")");
		return getSearch(word).synonyms();
	}

	public static List<String> getAntonyms(String word) {
		//LOG.trace(TT + ".antonyms(word=" + word + ")");
		return getSearch(word).antonyms();
	}

}
