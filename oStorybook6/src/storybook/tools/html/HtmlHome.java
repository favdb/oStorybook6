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
			STYLE_BANNER = "    .banner {\n"
			+ "       width: 100%;\n"
			+ "       height: 115px;\n"
			+ "       {banner} background-size: contain;\n"
			+ "       background-position: center;\n"
			+ "       background-repeat: no-repeat;\n"
			+ "       background-color:{bkcolor};\n"
			+ "       color:{fgcolor};\n"
			+ "       }\n",
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
				banner_text = Html.intoH(1, exp.book.getTitle(),
						"margin:0px; text-align:center;padding-top:35px;");
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

	private static final String HOME = "<!DOCTYPE html>\n"
			+ "<html lang=\"fr\">\n"
			+ "<head>\n"
			+ "    <meta charset=\"UTF-8\">\n"
			+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
			+ "    <title>{title}</title>\n"
			+ "    <link rel=\"icon\" type=\"image/x-icon\" href=\"Images/favicon.ico\">\n"
			+ "    <style>\n"
			+ "        body, html {\n"
			+ "            margin: 0; padding: 0; height: 100%;\n"
			+ "            display: flex; flex-direction: column;\n"
			+ "        }\n"
			+ "        .banner {\n"
			+ "            width: 100%;\n"
			+ "            height: 115px;\n"
			+ "            background-image: url('Images/banner.jpeg');\n"
			+ "            {banner} background-size: contain;\n"
			+ "            background-position: center;\n"
			+ "            background-repeat: no-repeat;\n"
			+ "            background-color: {bkcolor};\n"
			+ "            color: {fgcolor};\n"
			+ "        }\n"
			+ "        .content { flex: 1; display: flex; }\n"
			+ "        .sidebar {\n"
			+ "            width: 25%;\n"
			+ "            border: 3px solid {fgcolor};\n"
			+ "            border-radius: 10px;\n"
			+ "        }\n"
			+ "        .main-content {\n"
			+ "            width: 80%;\n"
			+ "            border: 1px solid #FFFFFF;\n"
			+ "            transition: width 0.3s ease;\n"
			+ "        }\n"
			+ "        iframe { width: 100%; height: 100%; border: none; }\n"
			+ "        .hidden {display: none;}\n"
			+ "        .full-width {width: 100% !important;}\n"
			+ "        .toggle-btn {\n"
			+ "            position: absolute;\n"
			+ "            top: 10px;\n"
			+ "            right: 10px;\n"
			+ "            z-index: 1000;\n"
			+ "            padding: 10px;\n"
			+ "            background-color: {bkcolor};\n"
			+ "            color: white;\n"
			+ "            border: none;\n"
			+ "            cursor: pointer;\n"
			+ "            font-size: 20px;\n"
			+ "            opacity: 0.5;\n"
			+ "            transition: opacity 0.3s ease;\n"
			+ "        }\n"
			+ "        .toggle-btn:hover {opacity: 1;}\n"
			+ "    </style>\n"
			+ "</head>\n"
			+ "<body>\n"
			+ "    <div class=\"banner\">{banner_text}</div>\n"
			+ "    <div class=\"content\">\n"
			+ "        <div class=\"sidebar\">\n"
			+ "            <iframe src=\"summary.html\">{msg_err}</iframe>\n"
			+ "        </div>\n"
			+ "        <div class=\"main-content\">\n"
			+ "            <iframe name=\"mc\" src=\"cover.html\">{msg_err}</iframe>\n"
			+ "        </div>\n"
			+ "    </div>\n"
			+ "    <button class=\"toggle-btn\" onclick=\"toggleLayout()\">● ● ●</button>\n"
			+ "    <script>\n"
			+ "        function toggleLayout() {\n"
			+ "            const banner = document.querySelector('.banner');\n"
			+ "            const sidebar = document.querySelector('.sidebar');\n"
			+ "            const mainContent = document.querySelector('.main-content');\n"
			+ "            banner.classList.toggle('hidden');\n"
			+ "            sidebar.classList.toggle('hidden');\n"
			+ "            if (banner.classList.contains('hidden') && sidebar.classList.contains('hidden')) {\n"
			+ "                mainContent.classList.add('full-width');\n"
			+ "            } else {\n"
			+ "                mainContent.classList.remove('full-width');\n"
			+ "            }\n"
			+ "        }\n"
			+ "    </script>\n"
			+ "</body>\n"
			+ "</html>";

	public static boolean write2(ExportBookToHtml exp) {
		//LOG.trace(TT + "write(exp=" + exp.book.param.getParamWeb().toXml() + ")");
		BookParamWeb web = exp.book.param.getParamWeb();
		String msg_err = I18N.getMsg("export.html_noframe");
		String bkColor = ColorUtil.toHexString(web.getSummary().getColor());
		String fgColor = "#000000";
		if (ColorUtil.isDark(web.getSummary().getColor())) {
			fgColor = "#FFFFFF";
		}
		String banner = "";
		StringBuilder banner_text = new StringBuilder();
		if (web.getBanner() && !web.getBannerImg().isEmpty()) {
			File f = new File(web.getBannerImg());
			banner = "background-image: url('" + "Images/" + f.getName() + "');\n";
		} else {
			banner_text.append("<div class=\"banner\" style=\"text-align: center;\n "
					+ "    font-family: sans-serif;\n "
					+ "    font-weight: bold;\n "
					+ "    font-size: 2rem;\n "
					+ "    padding-top: 10px; "
					+ "\">");
			banner_text.append(exp.book.getTitle());
			if (!exp.book.getSubtitle().isEmpty()) {
				banner_text.append("<div style=\""
						+ "        font-size: 1.2rem; "
						+ "        font-style: italic; "
						+ "        margin-top: -5px; "
						+ "\">")
						.append(exp.book.getSubtitle())
						.append("</div>");
			}
			banner_text.append("</div>");
		}
		String b = HOME.replace("{banner}", banner)
				.replace("{banner_text}", banner_text.toString())
				.replace("{bkcolor}", bkColor)
				.replace("{fgcolor}", fgColor)
				.replace("{msg_err}", msg_err)
				.replace("{title}", exp.book.getTitle());
		IOUtil.fileWriteString(exp.param.getDirectory() + "/" + "home.html", b);
		return true;
	}

}
