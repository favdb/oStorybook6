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

import i18n.I18N;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import storybook.tools.LOG;
import static storybook.tools.file.EnvUtil.getHomeDir;
import storybook.tools.net.Net;
import storybook.tools.synonyms.search.SEARCH;

/**
 * class for searching synonyms, use it as static Synonyms.findWord(...)
 *
 * @author favdb
 */
public class Synonyms {

	private static final String TT = "Synonyms";
	public static String lang,
	   SYNONYMS_URL = "", SYNONYMS_URL_PROMPT = "",
	   ANTONYMS_URL = "", ANTONYMS_URL_PROMPT = "";

	private static BufferedReader openZipReader(String lang) {
		try {
			ZipFile zipFile = new ZipFile(getFilename(lang));
			ZipEntry entry = zipFile.getEntry("thesaurus_" + lang + ".dic");
			InputStream dic = zipFile.getInputStream(entry);
			InputStreamReader isr = new InputStreamReader(dic, "UTF-8");
			return new BufferedReader(isr);
		} catch (IOException ex) {
			LOG.err("Synonyms.openZipReader exception", ex);
		}
		return null;
	}

	public enum APP_SYNONYMS {
		VERSION_MAJOR("0"),
		VERSION_MINOR("01"),
		VERSION_ALPHA(""),
		VERSION(VERSION_MAJOR + "." + VERSION_MINOR + VERSION_ALPHA),
		FULL_NAME("Synonyms " + VERSION),
		RELEASE_DATE("2023-03-01");
		final private String text;

		private APP_SYNONYMS(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	private static Synonyms instance;

	public static Synonyms getInstance() {
		if (instance == null) {
			instance = new Synonyms();
		}
		return instance;
	}

	/**
	 * main process
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		String vargs = String.join(" ", args);
		if (vargs.length() == 0) {
			vargs = "no option";
		}
		for (String arg : args) {
			switch (arg) {
				case "--trace":
					setTrace(true);
					break;
			}
		}
		LOG.log(APP_SYNONYMS.FULL_NAME.toString() + " starting with: " + vargs);
		SwingUtilities.invokeLater(() -> {
			Synonyms app = Synonyms.getInstance();
			app.initForTest();
		});
	}

	public Synonyms() {
		//empty for test only, don't use this class
	}

	public static void setTrace(boolean btrace) {
		LOG.setTrace(btrace);
	}

	public void initForTest() {
		LOG.log(TT + ".initForTest()");
		LOG.setTrace();
		lang = "fr";
		init(getHomeDir().getAbsolutePath()
		   + File.separator + ".storybook5/dicts/", lang);
		if (!exists(lang)) {
			LOG.err("no synonyms file to use");
			return;
		}
		LOG.trace("search for 'abaca'=" + findSynonyms("abaca"));
		LOG.trace("search for 'humus'=" + findSynonyms("humus"));
		LOG.trace("search for 'zythum'=" + findSynonyms("zythum"));
		LOG.log("ending tests");
	}

	public static List<String> findSynonyms(String word) {
		//LOG.trace(TT + ".find(word=\"" + word + "\")");
		Word mot = findWord(word);
		if (mot == null) {
			return new ArrayList<>();
		}
		return mot.getSynonyms();
	}

	public static List<String> findAntonyms(String word) {
		//LOG.trace(TT + ".find(word=\"" + word + "\")");
		Word mot = findWord(word);
		if (mot == null) {
			return new ArrayList<>();
		}
		return mot.getSynonyms();
	}

	/**
	 * search Word for a given word in a zipped thesaurus file
	 *
	 * @param search: word to search
	 * @return
	 */
	@SuppressWarnings("NonPublicExported")
	public static Word findWord(String search) {
		//LOG.trace(TT + ".find(word=\"" + word + "\") lang=" + lang);
		String word = search.toLowerCase();
		Word mot = null;
		File f = new File(getFilename(lang));
		if (!f.exists()) {
			return mot;
		}
		try {
			BufferedReader br = openZipReader(lang);
			String line = br.readLine().trim();
			while (line != null) {
				if (line.contains("=")) {
					String data[] = line.replace('=', ',').split(",");
					if (data[0].equals(word)) {
						// change word to the lemme
						word = data[1];
						// restart searching with the lemme
						br.close();
						br = openZipReader(lang);
						//return mot;
					}
				} else {
					if (!line.startsWith("#")) {
						mot = getWord(line, word);
						if (mot != null) {
							break;
						}
					}
				}
				line = br.readLine();
				if (line == null) {
					break;
				}
				line = line.trim();
			}
			br.close();
		} catch (IOException ex) {
			LOG.err(TT + ".findWord(word=\"" + word + "\") exception", ex);
		}
		return mot;
	}

	/**
	 * get a word and synonyms from a line text
	 *
	 * @param line
	 * @param word
	 * @return
	 */
	private static Word getWord(String line, String word) {
		if (line.contains("@")) {
			//if (!line.contains(word)) return null;
			String x[] = line.split("@");
			if (x[0].equals(word)) {
				Word mot = new Word(x[0].trim());
				String xx[] = {};
				String y[];
				if (x[1].contains("#")) {
					xx = x[1].split("#");
					y = xx[0].split("\\|");
				} else {
					y = x[1].split("\\|");
				}
				List<String> synonymes = new ArrayList<>();
				for (String z : y) {
					String zz = z.trim();
					if (zz.startsWith("(")) {
						mot.setType(zz);
					} else if (z.trim().startsWith("=")) {
						mot.setLemme(zz);
					} else {
						synonymes.add(zz);
					}
				}
				mot.setSynonyms(synonymes);
				List<String> antonymes = new ArrayList<>();
				if (x[1].contains("#")) {
					y = xx[1].split("\\|");
					for (String z : y) {
						if (z.trim().startsWith("(")) {
							mot.setType(z.trim());
						} else {
							antonymes.add(z.trim());
						}
					}
				}
				mot.setAntonyms(antonymes);
				return mot;
			}
		}
		return null;
	}
	private static String dicopath = "";

	/**
	 * initialize for static use
	 *
	 * @param path: folder where the thesaurus_lang.ocid is located
	 * @param lang: language to initialize
	 */
	public static void init(String path, String lang) {
		//LOG.trace(TT + ".init(path=\"" + path + "\", lang=" + lang + ")");
		Synonyms.dicopath = path;
		Synonyms.lang = lang;
		Synonyms.SYNONYMS_URL = SEARCH.getUrl(SEARCH.SYNONYMS);
		Synonyms.SYNONYMS_URL_PROMPT = SEARCH.getUrlPrompt(SEARCH.SYNONYMS);
		Synonyms.ANTONYMS_URL = SEARCH.getUrl(SEARCH.ANTONYMS);
		Synonyms.ANTONYMS_URL_PROMPT = SEARCH.getUrlPrompt(SEARCH.ANTONYMS);
		if (!lang.isEmpty()) {
			LOG.log("Initialize Synonyms for '" + lang + "'");
		}
	}

	public static String getFilename(String lang) {
		return dicopath + "thesaurus_" + lang + ".ocid";
	}

	/**
	 * check if the given synonyms file exists
	 *
	 * @return
	 */
	public static boolean exists() {
		//LOG.trace(TT + ".exists()");
		return exists(lang);
	}

	/**
	 * check if the given synonyms file exists
	 *
	 * @param lang
	 * @return
	 */
	public static boolean exists(String lang) {
		//LOG.trace(TT + ".exists(lang=" + lang + ") for " + getFilename(lang));
		File f = new File(getFilename(lang));
		return f.exists();
	}

	public static List<String> lookFor(JComponent comp, String word, String type) {
		String call = TT + ".lookFor(comp, word=" + word + ", type=" + type + ")";
		if (type.equals("synonyms")) {
			return lookForSynonyms(comp, word);
		} else {
			return lookForAntonyms(comp, word);
		}
	}

	public static List<String> lookForSynonyms(JComponent comp, String word) {
		String call = TT + ".lookForSynonyms(comp, word=" + word + ")";
		//LOG.trace(call);
		List<String> words = SEARCH.get(SEARCH.SYNONYMS, word);
		if (!words.isEmpty()) {
			return words;
		}
		int r = JOptionPane.showConfirmDialog(comp,
		   I18N.getMsg("word.synonyms.look_none") + "\n"
		   + I18N.getMsg("word.synonyms.url_show") + " " + SYNONYMS_URL_PROMPT,
		   I18N.getMsg("word.synonyms"),
		   JOptionPane.YES_NO_OPTION);
		if (r == JOptionPane.YES_OPTION) {
			showForSynonyms(comp, word);
		}
		return words;
	}

	public static void showFor(JComponent comp, String word, String type) {
		if (type.equals("synonyms")) {
			showForSynonyms(comp, word);
		} else {
			showForAntonyms(comp, word);
		}
	}

	public static void showForSynonyms(JComponent wysEditor, String word) {
		String CT = (TT + ".showForSynonyms(comp, word=\"" + word + "\")");
		//LOG.trace(CT);
		String url = SYNONYMS_URL;
		if (!url.startsWith("!")) {
			Net.openBrowser(url + word);
		} else {
			LOG.err(CT + "\ninvalid URL, starts with ! \"" + url + "\"");
		}
	}

	public static List<String> lookForAntonyms(JComponent comp, String word) {
		String CT = TT + ".lookForAntonyms(comp, word=" + word + ")";
		//LOG.trace(call);
		List<String> words = SEARCH.get(SEARCH.ANTONYMS, word);
		if (!words.isEmpty()) {
			return words;
		}
		int r = JOptionPane.showConfirmDialog(comp,
		   I18N.getMsg("word.antonyms.look_none") + "\n"
		   + I18N.getMsg("word.antonyms.url_show") + " " + ANTONYMS_URL_PROMPT,
		   I18N.getMsg("antonyms"),
		   JOptionPane.YES_NO_OPTION);
		if (r == JOptionPane.YES_OPTION) {
			showForAntonyms(comp, word);
		}
		return words;
	}

	public static void showForAntonyms(JComponent wysEditor, String word) {
		String CT = (TT + ".showForAntonyms(comp, word=\"" + word + "\")");
		//LOG.trace(CT);
		String url = ANTONYMS_URL;
		if (!url.startsWith("!")) {
			Net.openBrowser(url + word);
		} else {
			LOG.err(CT + "\ninvalid URL, starts with ! \"" + url + "\"");
		}
	}

}
