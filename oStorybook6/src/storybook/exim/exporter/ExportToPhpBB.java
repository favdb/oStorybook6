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
package storybook.exim.exporter;

import storybook.App;
import storybook.Pref;
import storybook.db.chapter.Chapter;
import storybook.db.scene.Scene;
import storybook.exim.EXIM;
import storybook.tools.clip.Clip;
import storybook.tools.html.Html;
import storybook.ui.frames.main.MainFrame;

/**
 *
 * @author favdb
 */
public class ExportToPhpBB {

	public static final String COLORS[][] = {
		{"blue", "Navy", "AliceBlue"},
		{"choco", "Chocolate", "BurlyWood"},
		{"olive", "Olive", "LightGreen"}
	};

	private static final String BB_TEXT = "<style type=\"text/css\">"
	   + ".sbkt {"
	   + "  align: center;"
	   + "  border: 5px solid navy;"
	   + "  background-color: aliceblue;"
	   + "  box-shadow: 0px 0px 15px rgba(0, 0, 0, 1);"
	   + "  border-radius: 20px;"
	   + "  text-align: center;"
	   + "  padding: 2%;"
	   + "	 width: 86%;"
	   + "  font-size: 24px;"
	   + "  font-weight: bold;"
	   + "}"
	   + ".sbkx {"
	   + "  align: center;"
	   + "  border: 5px solid navy;"
	   + "  background-color: aliceblue;"
	   + "  box-shadow: 0px 0px 15px rgba(0, 0, 0, 1);"
	   + "  border-radius: 20px;"
	   + "  padding-left: 8%;"
	   + "  padding-right: 8%;"
	   + "  width: 74%;"
	   + "  text-align: justify;"
	   + "  text-indent: 15px;"
	   + "  line-height: 1.1;"
	   + "}"
	   + "</style>";

	private ExportToPhpBB() {
	}

	public static int getColorIndex(String color) {
		for (int i = 0; i < COLORS.length; i++) {
			if (COLORS[i][0].toLowerCase().equals(color.toLowerCase())) {
				return i;
			}
		}
		return 0;
	}

	public static String getStyle() {
		return BB_TEXT;
	}

	public static String getChapter(MainFrame m, Chapter chapter) {
		StringBuilder b = new StringBuilder();
		StringBuilder text = new StringBuilder();
		for (Scene s : m.project.scenes.findBy(chapter)) {
			if (!s.getInformative()) {
				text.append(s.getSummary());
			}
		}
		String title = EXIM.getTitle(chapter.getName());
		if (!title.isEmpty()) {
			b.append(Html.intoTag("div", title, "class=\"sbkt\""));
		}
		b.append(Html.intoTag("div", text.toString(), "class=\"sbkx\""));
		Clip.to(b.toString().replace("\n", ""), "html");
		return b.toString();
	}

	public static String getScene(MainFrame mf, Scene scene) {
		StringBuilder b = new StringBuilder();
		String title = EXIM.getTitle(scene.getName());
		if (!title.isEmpty()) {
			b.append(Html.intoTag("div", title, "class=\"sbkt\""));
		}
		String text = scene.getSummary().replace("\n", "");
		b.append(Html.intoTag("div", text, "class=\"sbkx\""));
		Clip.to(b.toString(), "html");
		return b.toString();
	}

	public static void getInit(String text) {
		String c0 = App.preferences.getString(Pref.KEY.EXP_PHPBB_THEME);
		if (c0 == null || c0.isEmpty()) {
			c0 = COLORS[0][0];
		}
		String c1 = COLORS[0][1], c2 = COLORS[0][2];
		for (String[] COLORS1 : COLORS) {
			if (COLORS1[0].toLowerCase().equals(c0.toLowerCase())) {
				c1 = COLORS1[1].toLowerCase();
				c2 = COLORS1[2].toLowerCase();
				break;
			}
		}
		StringBuilder b = new StringBuilder(BB_TEXT.replace("navy", c1).replace("aliceblue", c2));
		b.append(text);
		Clip.to(b.toString(), "txt");
	}

}
