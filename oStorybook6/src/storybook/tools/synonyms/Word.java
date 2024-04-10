/*
 * oDico: Open Source software for synonyms Dictonnaries.
 * initial design FaVdB, inspired by the work of Olivier R. (Dicollecte project)

 * Copyright (C) 2023 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.tools.synonyms;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author favdb
 */
public class Word {

	private Long id = -1L;
	private String word;
	private String lemme = "";
	private String type = "?";
	private String description = "";
	private List<String> synonyms;
	private List<String> antonyms;

	public Word() {
	}

	public Word(String mot) {
		//LOG.trace("Word(mot=" + mot + ")");
		this.word = mot;
	}

	public Word(Long id, String mot) {
		this.word = mot;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMot() {
		return word;
	}

	public void setMot(String value) {
		this.word = value;
	}

	public String getLemme() {
		return lemme;
	}

	public void setLemme(String value) {
		this.lemme = value.replace("=", "");
	}

	public String getType() {
		return type;
	}

	public void setType(String value) {
		this.type = value.replace("(", "").replace(")", "");
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(List<String> values) {
		this.synonyms = values;
	}

	public List<String> getAntonyms() {
		return antonyms;
	}

	public void setAntonyms(List<String> values) {
		this.antonyms = values;
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		Word test = (Word) obj;
		return id.equals(test.getId());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = hash * 31 + word.hashCode();
		hash = hash * 31 + type.hashCode();
		return hash;
	}

	public String trace() {
		StringBuilder b = new StringBuilder(word);
		b.append(": ").append(type).append("=");
		List<String> bs = new ArrayList<>();
		for (String s : synonyms) {
			bs.add(s);
		}
		b.append(bs.toString());
		return b.toString();
	}

}
