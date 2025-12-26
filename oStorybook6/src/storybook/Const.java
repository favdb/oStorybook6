/*
 STORYBOOK: Open Source software for novelists and authors.
 Copyright (C) 2008 - 2012 Martin Mustun, 2013-2024 FaVdB

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
package storybook;

import i18n.I18N;
import java.util.Locale;
import storybook.tools.html.Html;
import storybook.tools.net.Net;

/**
 * Constants usable anywhere
 *
 * @author martin
 * @update favdb
 *
 */
public class Const {

	/* default global values */
	public enum STORYBOOK {
		NAME("oStorybook"),
		VERSION_MAJOR("6"),
		VERSION_MINOR("05"),
		VERSION(VERSION_MAJOR + "." + VERSION_MINOR),
		//replace alpha by empty string for a stable release
		VERSION_ALPHA(""),
		RELEASE_DATE("2025-05-13"),
		COPYRIGHT_YEAR("2012-2025"),
		COPYRIGHT_COMPANY("The oStorybook Team"),
		DB_VERSION("5.0"),
		FILE_EXT_H2DB(".h2.db"),
		FILE_EXT_MVDB(".mv.db"),
		FILE_EXT_OSBK(".osbk"),
		FILE_VERSION("6"),
		USER_HOME_DIR("storybook5");
		final private String text;

		private STORYBOOK(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public static int getVersionMajor() {
		return Integer.parseInt(STORYBOOK.VERSION_MAJOR.toString());
	}

	public static int getVersionMinor() {
		return Integer.parseInt(STORYBOOK.VERSION_MINOR.toString());
	}

	public static String getVersion() {
		return STORYBOOK.VERSION.toString();
	}

	public static String getVersionFull() {
		return STORYBOOK.VERSION.toString() + STORYBOOK.VERSION_ALPHA.toString();
	}

	public static String getName() {
		return STORYBOOK.NAME.toString();
	}

	public static String getNameVersion() {
		return STORYBOOK.NAME.toString() + " " + STORYBOOK.VERSION_MAJOR;
	}

	public static String getFullName() {
		return getName() + " " + getVersionFull();
	}

	/* generic values */
	public static final boolean IGNORE = true;
	public static final String FONT_DEFAULT = "Dialog,plain,12",
			FONT_BOOK = "Sans,plain,12",
			FONT_EDITOR = "Serif,plain,12",
			FONT_MONO = "Monospace,plain,12";
	public static final String ERROR_MISSING = "error.missing",
			ERROR_EXCEED = "error.exceed",
			ERROR_EXISTS = "error.exists",
			ERROR_NOTNUMERIC = "error.notnumeric",
			ERROR_WRONG = "error.wrong";
	public static final String SCENE_SEPARATOR = ".oOo.";

	public static enum Language {
		ar_, //Arabe
		bg_BG, //Bulgarian
		ca, //Català
		cs_CZ, //Czech
		da_DK, //Danish
		de_DE, //German
		el_GR, //Greek
		en_US, //USA english
		eo_EO, //Esperanto
		es_ES, //Spanish
		fi_FI, //Finnish
		fr_FR, //French
		hu_HU, //Hungarian
		it_IT, //Italian
		iw_IL, //Hebrew
		ja_JP, //Japanese
		ko_KR, // South Korean
		nl_NL, //Ducth
		pl_PL, //Polish
		pt_PT, //Potuguese
		pt_BR, //Brazilian Portuguese
		ru_RU, //Russian
		sv_SE, //Swedish
		tr_TR, //Turkish
		uk_UA, //Ukrainian
		zh_CN, //Simplified Chinese
		zh_HK, //Traditional Chinese (Hong Kong)
		;

		public String getI18N() {
			return I18N.getLanguage("language." + name());
		}

		public Locale getLocale() {
			Locale locale;
			switch (this) {
				case ar_:
					locale = new Locale("ar");
					break;
				case bg_BG:
					locale = new Locale("bg", "BG");
					break;
				case ca:
					locale = new Locale("ca", "ES");
					break;
				case cs_CZ:
					locale = new Locale("cs", "CZ");
					break;
				case da_DK:
					locale = new Locale("da", "DK");
					break;
				case de_DE:
					locale = Locale.GERMANY;
					break;
				case el_GR:
					locale = new Locale("el", "GR");
					break;
				case en_US:
					locale = Locale.US;
					break;
				case eo_EO:
					locale = new Locale("eo", "EO");
					break;
				case es_ES:
					locale = new Locale("es", "ES");
					break;
				case fi_FI:
					locale = new Locale("fi", "FI");
					break;
				case fr_FR:
					locale = new Locale("fr", "FR");
					break;
				case hu_HU:
					locale = new Locale("hu", "HU");
					break;
				case it_IT:
					locale = new Locale("it", "IT");
					break;
				case iw_IL:
					locale = new Locale("iw", "IL");
					break;
				case ja_JP:
					locale = new Locale("ja", "JP");
					break;
				case ko_KR:
					locale = new Locale("ko", "KR");
					break;
				case nl_NL:
					locale = new Locale("nl", "NL");
					break;
				case pl_PL:
					locale = new Locale("pl", "PL");
					break;
				case pt_PT:
					locale = new Locale("pt", "PT");
					break;
				case pt_BR:
					locale = new Locale("pt", "BR");
					break;
				case ru_RU:
					locale = new Locale("ru", "RU");
					break;
				case sv_SE:
					locale = new Locale("sv", "SE");
					break;
				case tr_TR:
					locale = new Locale("tr", "TR");
					break;
				case uk_UA:
					locale = new Locale("uk", "UA");
					break;
				case zh_HK:
					locale = new Locale("zh", "HK");
					break;
				case zh_CN:
					locale = new Locale("zh", "CN");
					break;
				default:
					locale = Locale.US;
			}
			return locale;
		}
	};

	public static int getLanguageIndex(String lang) {
		int i = 0;
		for (Language l : Language.values()) {
			if (l.name().equals(lang)) {
				return (i);
			}
			i++;
		}
		return (Language.en_US.ordinal());
	}

	/**
	 * Specific values for the spell checker
	 */
	public enum SpellCheker {
		DICTIONARIES("dicts/"),
		USER_DICTS("dicts"),
		CKEDITOR("https://download.cksource.com/CKEditor/CKEditor/CKEditor%204.20.2/ckeditor_4.20.2_standard.zip");
		private final String text;

		private SpellCheker(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum Spelling {
		none, en_US, de_DE, es_ES, it_IT, fr_FR, ru_RU, nl_NL, pl_PL;

		public String getI18N() {
			if (this == none) {
				return I18N.getMsg("preferences.spelling.no");
			}
			return I18N.getMsg("language." + name());
		}
	}

	/**
	 * Look and Feel values
	 */
	public enum LookAndFeel {
		LIGHT, ADVANCED, DARK, DRACULA;

		public String getI18N() {
			return I18N.getMsg("preferences.laf." + name().toLowerCase());
		}
	}

	/**
	 * GPL licence text
	 *
	 * @return
	 */
	public static String getGpl() {
		String url = Net.KEY.ROOT.toString();
		StringBuilder b = new StringBuilder(Html.HTML_B);
		b.append("<body ").append(Html.fontToHtml(App.fonts.defGet())).append(">")
				.append(Html.intoP(I18N.getMsg("about.gpl.intro")))
				.append(Html.intoP(I18N.getMsg("about.gpl.copyright") + Const.STORYBOOK.COPYRIGHT_YEAR))
				.append(Html.intoP(I18N.getMsg("about.gpl.homepage")))
				.append(Html.intoP(Html.intoA("homeSite", url, url)))
				.append(Html.intoP(I18N.getMsg("about.gpl.distribution")))
				.append(Html.intoP(I18N.getMsg("about.gpl.gpl")))
				.append(Html.intoP(I18N.getMsg("about.gpl.license")))
				.append(Html.BODY_E)
				.append(Html.HTML_E);
		return b.toString();
	}

}
