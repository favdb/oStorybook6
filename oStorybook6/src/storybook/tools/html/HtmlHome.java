/*
 * Copyright (C) 2024 favdb
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
package storybook.tools.html;

import i18n.I18N;
import java.io.File;
import storybook.db.book.BookParamWeb;
import storybook.exim.exporter.ExportBookToHtml;
import storybook.tools.file.IOUtil;
import storybook.tools.swing.ColorUtil;

/**
 *
 * @author favdb
 */
public class HtmlHome {

	private static final String TT = "HtmlHome.";

	private static final String HTML_BEGIN = "<!DOCTYPE html>\n"
		+ "<html lang=\"fr\">\n"
		+ "<head>\n"
		+ "    <meta charset=\"UTF-8\">\n"
		+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
		+ "    <title>{title}</title>\n"
		+ "    <link rel=\"icon\" type=\"image/x-icon\" href=\"Images/favicon.ico\">\n"
		+ "    <style>\n",
		STYLE_BODY = "    body, html {margin: 0; padding: 0; height: 100%;\n"
		+ "        display: flex; flex-direction: column;}\n",
		STYLE_BANNER = "    .banner {width: 100%; height: 115px; {banner} background-size: contain;\n"
		+ "            background-position: center; background-repeat: no-repeat;\n"
		+ "            background-color{bkcolor};color:{fgcolor};}\n",
		STYLE_CONTENT = "    .content { flex: 1; display: flex;}\n",
		STYLE_SIDEBAR = "    .sidebar { width: 20%; border: 3px solid {color}; border-radius: 10px;}\n",
		STYLE_MAIN = "    .main-content { width: 80%; border: 1px solid #FFFFFF;}\n",
		STYLE_FRAMES = "    iframe {width: 100%; height: 100%; border: none;}\n",
		END_STYLE = "    </style>\n"
		+ "</head>\n"
		+ "<body>\n",
		BODY_BANNER = "  <div class=\"banner\">{banner_text}</div>\n",
		BODY_CONTENT = "  <div class=\"content\">\n"
		+ "      <div class=\"sidebar\"><iframe src=\"summary.html\">{msg_err}</iframe></div>\n"
		+ "      <div class=\"main-content\"><iframe name=\"mc\" src=\"cover.html\">{msg_err}</iframe></div>\n"
		+ "  </div>\n";

	public static boolean write(ExportBookToHtml exp) {
		//LOG.trace(TT + "write(exp=" + exp.book.param.getParamWeb().toXml() + ")");
		BookParamWeb web = exp.book.param.getParamWeb();
		String msg_err = I18N.getMsg("export.html_noframe");
		String bkColor = ColorUtil.toHexString(web.getSummary().getColor());
		String fgColor = "#000000";
		if (ColorUtil.isDark(web.getSummary().getColor())) {
			fgColor = "#FFFFFF";
		}
		String banner = "", banner_text = "";
		StringBuilder b = new StringBuilder(HTML_BEGIN.replace("{title}", exp.book.getTitle()));
		b.append(STYLE_BODY);
		if (web.getBanner()) {
			if (!web.getBannerImg().isEmpty()) {
				File f = new File(web.getBannerImg());
				banner = "background-image: url('" + "Images/" + f.getName() + "');\n";
			} else {
				banner_text = Html.intoH(1, exp.book.getTitle(), "margin:0px;text-align: center;");
			}
			b.append(STYLE_BANNER
				.replace("{banner}", banner)
				.replace("{bkcolor}", bkColor)
				.replace("{fgcolor}", fgColor));
		}
		b.append(STYLE_CONTENT);
		b.append(STYLE_SIDEBAR.replace("{color}", bkColor));
		b.append(STYLE_MAIN);
		b.append(STYLE_FRAMES);
		b.append(END_STYLE);
		if (web.getBanner()) {
			b.append(BODY_BANNER.replace("{banner_text}", banner_text));
		}
		b.append(BODY_CONTENT.replace("{msg_err}", msg_err));
		b.append(Html.BODY_E).append(Html.HTML_E);
		IOUtil.fileWriteString(exp.param.getDirectory() + "/" + "home.html", b.toString());
		return true;
	}

}
