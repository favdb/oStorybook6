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

import java.net.URL;
import java.util.List;
import storybook.tools.synonyms.Word;

/**
 *
 * @author favdb
 */
public abstract class SEARCH_Abstract {

	public Word word;

	public SEARCH_Abstract(String word) {
		this.word = new Word(word);
	}

	abstract String getUrlContent(URL url, String... iso);

	public abstract List<String> synonyms();

	public abstract List<String> antonyms();

}
