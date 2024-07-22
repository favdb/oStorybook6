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
import storybook.tools.file.IOUtil;

/**
 *
 * @author favdb
 */
public class HtmlHome {

    private static final String TT = "HtmlHome.";

    private static String HOME_SRC = "<html>\n<head>\n"
	    + "  <title>{title}</title>\n"
	    + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\">\n"
	    + "  <style>\n"
	    + "   .menu {\n"
	    + "		border: 5px solid green;\n"
	    + "		border-radius: 5px;\n"
	    + "		float:left;\n"
	    + "		height:100%;\n"
	    + "		width:20%;\n"
	    + "      }\n"
	    + "    .mainContent {\n"
	    + "	     border: 0;\n"
	    + "      float:left;\n"
	    + "      height:100%;\n"
	    + "      width:79%;\n"
	    + "      }"
	    + "  </style>\n"
	    + "</head>\n"
	    + "<body>\n"
	    + "	<iframe name=\"menu\" class=\"menu\" src=\"summary.html\">{msg_err}</iframe>\n"
	    + "	<iframe name=\"mc\" class=\"mainContent\" src=\"index.html\">{msg_err}</iframe>\n"
	    + "</body>\n</html>";

    public static boolean write(String title, String path) {
	//LOG.trace(TT + "write(title=" + title + ", path=" + path);
	String msg_err = I18N.getMsg("export.html_noframe");
	String content = HOME_SRC.replace("{title}", title).replace("{msg_err}", msg_err);
	IOUtil.fileWriteString(path + "/" + "home.html", content);
	return true;
    }

}
