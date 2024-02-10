/*
 *  JOrtho
 *
 *  Copyright (C) 2005-2012 by i-net software
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 *
 * Created on 31.05.2012
 */
package api.jortho;

import java.util.Locale;

/**
 * Implementation for French
 *
 * @author Volker Berlin adaptation favdb
 *
 */
public class LanguageBundle_fr extends LanguageBundle {

	@Override
	boolean existInDictionary(String word, Dictionary dictionary,
	   SpellCheckerOptions options, boolean isFirstWordInSentence) {
		if (super.existInDictionary(word,
		   dictionary,
		   options,
		   isFirstWordInSentence)) {
			return true;
		}
		if (word.contains("-")) {
			String ws[] = word.split("\\-");
			boolean b = true;
			for (String wsx : ws) {
				if (!wsx.isEmpty()
				   && !super.existInDictionary(wsx,
					  dictionary, options, isFirstWordInSentence)) {
					b = false;
					break;
				}
			}
			if (b) {
				return true;
			}
		}
		if (word.contains("\'") || word.contains("’")) {
			String wsx[] = {""};
			if (word.contains("’")) {
				wsx = word.split("’");
			}
			if (word.contains("\'")) {
				wsx = word.split("\'");
			}
			if (wsx.length > 1) {
				String ref = "l d qu c n m t s j puisqu lorsqu jusqu";
				if (ref.contains(wsx[0])) {
					return super.existInDictionary(wsx[1].toLowerCase(Locale.FRENCH),
					   dictionary, options, false);
				}
				return super.existInDictionary(wsx[0],
				   dictionary, options, isFirstWordInSentence)
				   && super.existInDictionary(wsx[1].toLowerCase(Locale.FRENCH),
					  dictionary, options, false);
			}
		}
		return false;
	}
}
