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
import storybook.tools.LOG;
import storybook.tools.StringUtil;

/**
 * search from Institut d'Estudis Catalans
 * https://sinonims.iec.cat/sinonims_cerca.asp?limit=2&pclau=
 *
 * @author favdb
 */
public class SEARCH_ca extends SEARCH_Abstract {

	private static final String TT = "SEARCH_ca";
	public static final String URL_SYNONYMS = "https://sinonims.iec.cat/sinonims_cerca.asp?limit=2&pclau=",
			URL_SYNONYMS_PROMPT = "Institut d'Estudis Catalans",
			URL_ANTONYMS = "",
			URL_ANTONYMS_PROMPT = "Institut d'Estudis Catalans";

	public SEARCH_ca(String word) {
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
			LOG.err(TT + ".getUrl(...) exception", ex);
			return "";
		} catch (IOException ex) {
			LOG.err(TT + ".getUrl(...) exception", ex);
		}
		return "";
	}

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
		if (inHtml != null && !inHtml.isEmpty() && inHtml.contains("sinonims")) {
			String html = inHtml;
			html = StringUtil.subText(html,
					"<span class=\"sinonims\">",
					"</span>");
			html = html.replace(" ", "")
					.replace("<br/>", "")
					.replace('.', ',');
			if (!html.isEmpty()) {
				try {
					String str[] = html.split(",");
					for (String s : str) {
						if (!StringUtil.isNumeric(s)) {
							words.add(s.trim());
						}
						if (words.size() > 9) {
							break;
						}
					}
				} catch (Exception ex) {
					LOG.err("SEARCH_ca.synonyms(...) exception", ex);
				}
			}
		}
		return words;
	}

	@Override
	public List<String> antonyms() {
		List<String> words = new ArrayList<>();
		// nothing
		return words;
	}

}
