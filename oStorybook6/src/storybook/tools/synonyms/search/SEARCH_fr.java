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
import api.jsoup.nodes.Element;
import api.jsoup.select.Elements;
import storybook.tools.LOG;
import storybook.tools.StringUtil;

/**
 * search from CRISCO-DES https://crisco4.unicaen.fr/des/
 *
 * @author favdb
 */
public class SEARCH_fr extends SEARCH_Abstract {

	private static final String TT = "SEARCH_fr";
	public static final String URL_SYNONYMS = "https://crisco4.unicaen.fr/des/synonymes/",
			URL_SYNONYMS_PROMPT = "CRISCO-DES",
			URL_ANTONYMS = URL_SYNONYMS,
			URL_ANTONYMS_PROMPT = URL_SYNONYMS_PROMPT;

	public SEARCH_fr(String word) {
		super(word);
	}

	@Override
	String getUrlContent(URL url, String... iso) {
		//LOG.trace(TT + ".getUrlContent(url=" + url.toString() + ")");
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
			//LOG.err(TT + ".getUrl(...) exception", ex);
		}
		return "";
	}

	@Override
	public List<String> synonyms() {
		//LOG.trace(TT + ".synonyms()");
		List<String> words = new ArrayList<>();
		try {
			int nb = 0;
			String inHtml = getUrlContent(new URL(URL_SYNONYMS + word.getMot()));
			if (inHtml != null && !inHtml.isEmpty()) {
				String html = StringUtil.subText(inHtml,
						"<!-- Fin titre (vedette + nb de synonymes)-->",
						"<!--Fin liste des synonymes-->");
				if (!html.isEmpty()) {
					Document doc = Jsoup.parse("<html>" + html + "</html>");
					Elements elems = doc.getElementsByTag("a");
					for (int i = 0; i < elems.size(); i++) {
						Element elem = elems.get(i);
						words.add(elem.text());
						if (nb++ > 9) {
							break;
						}
					}
				}
			}
		} catch (MalformedURLException ex) {
			LOG.err(TT + ".synonyms() exception", ex);
		}
		return words;
	}

	@Override
	public List<String> antonyms() {
		//LOG.trace(TT + ".antonyms()");
		List<String> words = new ArrayList<>();
		try {
			int nb = 0;
			String inHtml = getUrlContent(new URL(URL_ANTONYMS + word.getMot()));
			if (inHtml != null && !inHtml.isEmpty()) {
				String html = StringUtil.subText(inHtml,
						"<!-- Fin titre (nb d'antonymes)-->",
						"<!-- Fin liste des antonymes -->");
				if (!html.isEmpty()) {
					Document doc = Jsoup.parse("<html>" + html + "</html>");
					Elements elems = doc.getElementsByTag("a");
					for (int i = 0; i < elems.size(); i++) {
						Element elem = elems.get(i);
						words.add(elem.text());
						if (nb++ > 9) {
							break;
						}
					}
				}
			}
		} catch (MalformedURLException ex) {
			LOG.err(TT + ".antonyms() exception", ex);
		}
		return words;
	}

}
