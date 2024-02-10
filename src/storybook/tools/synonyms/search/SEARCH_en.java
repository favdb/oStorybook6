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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import api.jsoup.Jsoup;
import api.jsoup.nodes.Document;
import storybook.tools.LOG;

/**
 * search from Merriam-Webster's dictionary
 *
 * @author favdb
 */
public class SEARCH_en extends SEARCH_Abstract {

	/**
	 * get lines from HTML
	 *
	 * @param html
	 * @return
	 */
	private static List<String> getLines(String html) {
		Document doc = Jsoup.parse(html);
		String text = doc.getElementsByClass("share-link")
				.attr("data-share-description")
				.replace(",", "");
		String ps[] = text.split(";");
		List<String> b = new ArrayList<>();
		b.add("Synonyms " + ps[0].substring(ps[0].indexOf(":") + 1));
		if (ps.length > 1) {
			b.add("Antonyms " + ps[1].substring(ps[1].indexOf(":") + 1));
		}
		return b;
	}

	private static final String TT = "SEARCH_en";
	public static final String URL_SYNONYMS = "https://www.merriam-webster.com/thesaurus/",
			URL_SYNONYMS_PROMPT = "The Merriam-Webster's Dictionnary",
			URL_ANTONYMS = "https://www.merriam-webster.com/thesaurus/",
			URL_ANTONYMS_PROMPT = "The Merriam-Webster's Dictionnary";

	public SEARCH_en(String word) {
		super(word);
		LOG.trace(TT + " word=" + this.word.getMot());
	}

	@Override
	String getUrlContent(URL url, String... iso) {
		LOG.trace(TT + ".getUrlContent(url=" + url.toString() + ")");
		StringBuilder buf = new StringBuilder();
		try {
			URLConnection cnx = url.openConnection();
			BufferedReader br = new BufferedReader(
					new InputStreamReader(cnx.getInputStream()));
			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				buf.append(inputLine);
			}
			return buf.toString();
		} catch (MalformedURLException ex) {
			LOG.err(TT + ".getUrl(...) exception", ex);
			return "";
		} catch (IOException ex) {
			// 404 error
			//LOG.err(TT + ".getUrl(...) exception", ex);
		}
		return "";
	}

	@Override
	public List<String> synonyms() {
		List<String> words = new ArrayList<>();
		int nb = 0;
		String inHtml;
		URL url;
		try {
			url = new URL(URL_SYNONYMS + word.getMot());
			inHtml = getUrlContent(url);
		} catch (MalformedURLException ex) {
			LOG.err(TT + ".synonyms() exception", ex);
			return words;
		}
		LOG.trace("lookforSynonyms=" + url.toString());
		for (String line : getLines(inHtml)) {
			if (line.startsWith("Synonyms")) {
				for (String str : line.split(" ")) {
					if (!str.equals("Synonyms") && !words.contains(str)) {
						words.add(str);
						if (words.size() > 9) {
							break;
						}
					}
				}
			}
		}
		return words;
	}

	@Override
	public List<String> antonyms() {
		List<String> words = new ArrayList<>();
		int nb = 0;
		String inHtml;
		URL url;
		try {
			url = new URL(URL_ANTONYMS + word.getMot());
			inHtml = getUrlContent(url);
		} catch (MalformedURLException ex) {
			LOG.err(TT + ".antonyms() exception", ex);
			return words;
		}
		LOG.trace("lookforAntonyms=" + url.toString());
		for (String line : getLines(inHtml)) {
			if (line.startsWith("Antonyms")) {
				for (String str : line.split(" ")) {
					if (!str.equals("Antonyms") && !words.contains(str)) {
						words.add(str);
						if (words.size() > 9) {
							break;
						}
					}
				}
			}
		}
		return words;
	}

}
