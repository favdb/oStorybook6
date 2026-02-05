/*
 * Copyright (C) 2022 favdb
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
package storybook.tools.html;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import resources.MainResources;
import storybook.App;
import storybook.tools.LOG;
import storybook.tools.file.IOUtil;
import static storybook.tools.html.Html.*;
import storybook.tools.swing.LaF;

/**
 *
 * @author favdb
 */
public class CSS {

	private static final String TT = "CSS";

	private CSS() {
		//empty
	}

	/**
	 * return a CSS param
	 *
	 * @param key
	 * @param value
	 * @param unit
	 * @return
	 */
	private static String getParam(String key, String value) {
		return getParam(0, key, value);
	}

	/**
	 * return a CSS param
	 *
	 * @param tab
	 * @param key
	 * @param value
	 * @param unit
	 * @return
	 */
	private static String getParam(int tab, String key, int value, String unit) {
		return getParam(tab, key, Integer.toString(value) + unit);
	}

	private static String getParam(int tab, String key, Double value, String unit) {
		return getParam(tab, key, value.toString(), unit);
	}

	public static String getParam(int tab, String key, String... value) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < tab; i++) {
			b.append("    ");
		}
		if (key.isEmpty()) {
			b.append("}\n");
			return (b.toString());
		}
		b.append(key);
		if (value != null && value.length > 0 && !value[0].isEmpty()) {
			b.append(": ");
			b.append(value[0]);
			b.append(";");
		} else if (tab == 1) {
			b.append(" {");
		}
		b.append("\n");
		return (b.toString());
	}

	/**
	 * return a default CSS style with editor Font and no margin
	 *
	 * @return
	 */
	public static String getDefault() {
		return getDefault(App.fonts.editorGet(), 0, false);
	}

	/**
	 * return a default CSS style with a given Font and a given Margin
	 *
	 * @param font
	 * @param margin
	 * @param isPanel
	 * @return
	 */
	public static String getDefault(Font font, int margin, boolean isPanel) {
		StringBuilder buf = new StringBuilder();
		String unit = "pt";
		// body
		buf.append(getParam(1, "body"));
		if (font != null) {
			buf.append(getParam(2, FONT_FAMILY, font.getFamily()));
			buf.append(CSS.getParam(2, FONT_SIZE, font.getSize(), unit));
		}
		buf.append(CSS.getParam(2, PADDING_LEFT, margin, unit))
				.append(CSS.getParam(2, PADDING_RIGHT, margin, unit));
		buf.append(getParam(1, ""));
		//h1
		buf.append(getParam(1, "h1"))
				.append(CSS.getParam(2, FONT_SIZE, 140, "%"))
				//.append(getParam(2, TEXT_ALIGN, "center"))
				.append(CSS.getParam(2, MARGIN_TOP, 10, unit))
				.append(CSS.getParam(2, MARGIN_BOTTOM, 1, unit));
		buf.append(getParam(1, ""));
		//h2
		buf.append(getParam(1, "h2"))
				.append(CSS.getParam(2, FONT_SIZE, 130, "%"))
				.append(CSS.getParam(2, MARGIN_TOP, 10, unit))
				.append(CSS.getParam(2, MARGIN_BOTTOM, 1, unit));
		buf.append(getParam(1, ""));
		//h3
		buf.append(getParam(1, "h3"))
				.append(CSS.getParam(2, FONT_SIZE, 120, "%"))
				.append(CSS.getParam(2, MARGIN_TOP, 6, unit))
				.append(CSS.getParam(2, MARGIN_BOTTOM, 1, unit));
		buf.append(getParam(1, ""));
		//h4
		buf.append(getParam(1, "h4"))
				.append(CSS.getParam(2, FONT_SIZE, 110, "%"))
				.append(CSS.getParam(2, MARGIN_TOP, 6, unit))
				.append(CSS.getParam(2, MARGIN_BOTTOM, 1, unit));
		buf.append(getParam(1, ""));
		//h5
		buf.append(getParam(1, "h5"))
				.append(CSS.getParam(2, FONT_SIZE, 100, "%"))
				.append(CSS.getParam(2, MARGIN_TOP, 6, unit))
				.append(CSS.getParam(2, MARGIN_BOTTOM, 1, unit));
		buf.append(getParam(1, ""));
		//h6
		buf.append(getParam(1, "h6"))
				.append(CSS.getParam(2, FONT_SIZE, 100, "%"))
				.append(CSS.getParam(2, MARGIN_TOP, 6, unit))
				.append(CSS.getParam(2, MARGIN_BOTTOM, 1, unit));
		buf.append(getParam(1, ""));
		// links
		if (isPanel && LaF.isDark()) {
			buf.append(getParam(1, "a"))
					.append(getParam(2, "color", "#e0ffff"));
			buf.append(getParam(1, ""));
		}
		// paragraphe
		buf.append(getParam(1, "p"))
				.append(CSS.getParam(2, MARGIN_TOP, 0, unit))
				.append(CSS.getParam(2, MARGIN_BOTTOM, 6, unit))
				.append(getParam(2, TEXT_ALIGN, "justify"));
		buf.append(getParam(1, ""));
		// blockquote
		buf.append(getParam(1, "blockquote"))
				.append(CSS.getParam(2, MARGIN_TOP, 0, unit))
				.append(CSS.getParam(2, MARGIN_BOTTOM, 6, unit))
				.append(CSS.getParam(2, MARGIN_RIGHT, 40, unit))
				.append(CSS.getParam(2, MARGIN_LEFT, 40, unit))
				.append(getParam(2, TEXT_ALIGN, "justify"))
				.append(getParam(2, FONT_STYLE, "italic"));
		buf.append(getParam(1, ""));
		//nav bar
		buf.append(getParam(1, "#navbar"))
				.append(getParam(2, COLOR_BG, "lightgrey"))
				.append(getParam(2, TEXT_ALIGN, "right"));
		buf.append(getParam(1, ""));
		//ul
		buf.append(getParam(1, "ul"))
				.append(CSS.getParam(2, MARGIN_TOP, 2, unit))
				.append(CSS.getParam(2, MARGIN_BOTTOM, 2, unit))
				.append(CSS.getParam(2, MARGIN_LEFT, 15, unit));
		buf.append(getParam(1, ""));
		//ol
		buf.append(getParam(1, "ol"))
				.append(CSS.getParam(2, MARGIN_TOP, 2, unit))
				.append(CSS.getParam(2, MARGIN_BOTTOM, 2, unit))
				.append(CSS.getParam(2, MARGIN_LEFT, 15, unit));
		buf.append(getParam(1, ""));
		//table
		buf.append(getParam(1, "table tr"))
				.append(CSS.getParam(2, MARGIN, 0, unit))
				.append(CSS.getParam(2, PADDING, 0, unit));
		buf.append(getParam(1, ""));
		buf.append(getParam(1, "td"))
				.append(CSS.getParam(2, MARGIN_RIGHT, 1, unit))
				.append(CSS.getParam(2, PADDING, 1, unit));
		buf.append(getParam(1, ""));
		buf.append(getParam(1, ".endnote"))
				.append(getParam(2, FONT_SIZE, "0.5em"))
				.append(getParam(2, "vertical-align: super;"));
		buf.append(getParam(1, ""));
		buf.append(getParam(1, ".comment"))
				.append(getParam(2, FONT_SIZE, "0.5em"))
				.append(getParam(2, FONT_WEIGHT, "bold"))
				.append(getParam(2, COLOR, "white"))
				.append(getParam(2, COLOR_BG, "Red"))
				.append(getParam(2, "vertical-align: super;"));
		buf.append(getParam(1, ""));
		return buf.toString();
	}

	/**
	 * get CSS for scenario
	 *
	 * @param force: get standard CSS and force font-family, size and margin-top and bottom left and
	 * right margin will be 1cm. If not forced, then get the CSS ressource and force font-family and
	 * font size to the default font mono
	 *
	 * @return String containing the CSS code
	 */
	public static String forScenario(boolean force) {
		String buf = "", unit = "pt";
		Font font = App.fonts.monoGet();
		if (force) {
			buf = "body {"
					+ FONT_FAMILY + ": " + font.getFontName() + ";"
					+ FONT_SIZE + ": " + font.getSize() + unit
					+ "}\n"
					+ "p {"
					+ MARGIN_LEFT + ": 0.5cm;"
					+ MARGIN_RIGHT + ": 0.5cm;"
					+ MARGIN_TOP + ": " + font.getSize() / 2 + unit + ";"
					+ MARGIN_BOTTOM + ": " + font.getSize() / 2 + unit + ";"
					+ "}\n";
			return (buf);
		}
		File file = new File(MainResources.class.getResource("css/scenario.css").getFile());
		try {
			buf = new String(Files.readAllBytes(file.toPath()));
		} catch (IOException ex) {
		}
		String str = buf.replace("monospace", font.getName());
		str = str.replace(FONT_SIZE + ": 12pt", FONT_SIZE + ": " + Integer.toString(font.getSize()) + unit);
		str = str.replace("8pt", Integer.toString(font.getSize() / 2) + unit);
		return (str);
	}

	public static String forEditor() {
		StringBuilder b = new StringBuilder();
		b.append("body {\n")
				.append(getFontSize(App.fonts.bookGet()))
				.append("}\n");
		b.append("a.endnote {\n")
				.append(getParam("vertical-align", "super"))
				.append(getParam(FONT_SIZE, "smaller"))
				.append("}\n");
		b.append("a.comment {\n")
				.append("vertical-align: super;\n")
				.append(getParam(FONT_SIZE, "smaller"))
				.append("font-weight: bold;\n")
				.append(getParam(COLOR, "white"))
				.append(getParam(COLOR_BG, "red"))
				.append("}\n");
		b.append("em {\n")
				.append(getParam(COLOR_BG, "yellow"))
				.append(getParam("font-style", "normal;"))
				.append("}");
		b.append(IOUtil.resourceRead("css/review.css", MainResources.class));
		return b.toString();
	}

	public static String getFontFamily(Font font) {
		return FONT_FAMILY + ": \"" + font.getFontName() + "\";\n";
	}

	public static String getFontSize(Font font) {
		return FONT_SIZE + ": " + font.getSize() + "pt;\n";
	}

	/**
	 * get the Scenario CSS from a file
	 *
	 * @param cssFile
	 * @return
	 */
	public static String fromFile(String cssFile) {
		String buf = "";
		File file = new File(cssFile);
		try {
			buf = new String(Files.readAllBytes(file.toPath()));
		} catch (IOException ex) {
		}
		return (buf);
	}

	/**
	 * get a CSS String using a CSS file content,
	 *
	 * @param nf
	 * @param font
	 * @return if CSS file not exists then return default CSS with the given Font
	 */
	public static String fromFile(String nf, Font font) {
		StringBuilder b = new StringBuilder();
		b.append(STYLE_B);
		if (nf != null && !nf.isEmpty()) {
			File f = new File(nf);
			if (f.exists()) {
				try (BufferedReader br = new BufferedReader(new FileReader(f))) {
					String ligne;
					while ((ligne = br.readLine()) != null) {
						b.append(ligne).append("\n");
					}
					br.close();
				} catch (IOException e) {
					LOG.err(TT + ".fromFile(nf, font) I/O error reading CSS");
				} finally {

				}
			}
		} else {
			b.append(getDefault(font, 0, false));
		}
		b.append(STYLE_E);
		return (b.toString());
	}

	/**
	 * return a HEAD with CSS STYLE for a given Font
	 *
	 * @param font
	 * @return
	 */
	public static String getHeadWithCss(Font font) {
		String buf = Html.HEAD_B
				+ Html.STYLE_B
				+ CSS.getDefault(font, 0, false)
				+ Html.STYLE_E
				+ Html.HEAD_E;
		return buf;
	}

	/**
	 * return a HEAD with default CSS STYLE
	 *
	 * @return
	 */
	public static String getHeadWithCss() {
		return getHeadWithCss(App.fonts.defGet());
	}

}
