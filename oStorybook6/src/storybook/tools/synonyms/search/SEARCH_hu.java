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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import api.jsoup.Jsoup;
import api.jsoup.nodes.Document;
import api.jsoup.select.Elements;
import storybook.tools.LOG;

/**
 * search from Midnet Digital Kft.
 *
 * synonyms: https://topszotar.hu/szinonima-szotar/
 *
 * antonyms: https://topszotar.hu/ellentet-szotar/
 *
 * @author favdb
 */
public class SEARCH_hu extends SEARCH_Abstract {

	private static final String TT = "SEARCH_hu";

	public static final String URL_SYNONYMS_PROMPT = "szinonimaszotar.hu",
			URL_SYNONYMS = "https://szinonimaszotar.hu/keres/",
			URL_ANTONYMS_PROMPT = "ellentetszotar.hu",
			URL_ANTONYMS = "https://ellentetszotar.hu/";

	public SEARCH_hu(String word) {
		super(word);
	}

	@Override
	String getUrlContent(URL url, String... iso) {
		//LOG.trace(TT + ".getUrlContent(url=" + url.toString() + ")");
		StringBuilder buf = new StringBuilder();
		try {
			URLConnection cnx = url.openConnection();
			BufferedReader br;
			if (iso == null || iso.length < 1) {
				br = new BufferedReader(
						new InputStreamReader(cnx.getInputStream()));
			} else {
				br = new BufferedReader(
						new InputStreamReader(cnx.getInputStream(), Charset.forName(iso[0])));
			}
			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				buf.append(inputLine);
			}
			return buf.toString();
		} catch (MalformedURLException ex) {
			LOG.err(TT + ".getUrl(...) URL malformed");
		} catch (IOException ex) {
			LOG.err(TT + ".getUrl(...) missing file");
		}
		return "";
	}

	/**
	 * search synonyms
	 *
	 * @return
	 */
	@Override
	public List<String> synonyms() {
		List<String> words = new ArrayList<>();
		String inHtml;
		URL url;
		try {
			url = new URL(URL_SYNONYMS + word.getMot());
			inHtml = getUrlContent(url);
		} catch (MalformedURLException ex) {
			LOG.err(TT + ".synonyms() exception", ex);
			return words;
		}
		if (inHtml != null && !inHtml.isEmpty() && inHtml.contains("class=\"szavak\"")) {
			Document doc = Jsoup.parse(inHtml);
			Elements elems = doc.getElementsByClass("szavak");
			if (!elems.isEmpty()) {
				String text = elems.get(0).text().replace(" ", "");
				String texts[] = text.split(",");
				for (String s : texts) {
					words.add(s);
					if (words.size() > 9) {
						break;
					}
				}
			}
		}
		return words;
	}

	/**
	 * search antonyms
	 *
	 * @return
	 */
	@Override
	public List<String> antonyms() {
		List<String> words = new ArrayList<>();
		String inHtml;
		URL url;
		try {
			url = new URL(URL_ANTONYMS + word.getMot());
			inHtml = getUrlContent(url, "iso-8859-2");
		} catch (MalformedURLException ex) {
			LOG.err(TT + ".antonyms() exception", ex);
			return words;
		}
		if (inHtml != null
				&& !inHtml.isEmpty()
				&& !inHtml.contains("<p>nem " + word.getMot() + "</p>")) {
			Document doc = Jsoup.parse(inHtml);
			Elements elems = doc.getElementsByAttributeValue("id", "content");
			if (!elems.isEmpty()) {
				Elements selems = elems.get(0).getElementsByTag("p");
				if (!selems.isEmpty()) {
					String text[] = selems.get(0).text().replace(" ", "").split(",");
					for (String s : text) {
						words.add(s);
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
