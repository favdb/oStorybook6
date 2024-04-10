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
package storybook.tools.spell;

import api.jsoup.Jsoup;
import api.shef.tools.LOG;
import i18n.I18N;
import java.io.File;
import java.io.IOException;
import storybook.Const;
import storybook.dialog.MessageDlg;
import storybook.tools.file.EnvUtil;
import storybook.tools.file.IOUtil;
import storybook.tools.net.Net;
import storybook.tools.zip.ZipUtil;
import storybook.ui.MainFrame;

/**
 * class for using external check with navigator+LanguageTool and CkEditor
 *
 * @author favdb
 */
public class SpellCheck {

	private static final String TT = "Spelling";

	private String htmlText = "";
	private static String CKEDITOR_HEAD = "<!DOCTYPE html>\n"
	   + "<html>\n"
	   + "<head>\n"
	   + "    <meta charset=\"utf-8\">\n"
	   + "    <title>oStorybook test Grammalecte</title>\n"
	   /*+ "    <script class=\"code\" type=\"text/javascript\">\n"
			+ "        var features =  \"\"  \n"
			+ "                        + \"menubar=no,toolbar=no,location=no,personalbar=no\"\n"
			+ "                        + \",status=no,chrome=yes,resizable,centerscreen\";"
			+ "    </script>"*/
	   + "    <script src=\"" + getCkEditorFileName() + "\"></script>\n"
	   + "</head>\n"
	   + "<body>\n"
	   + "    <h2>oStorybook : vérification avec Grammalecte</h2>\n"
	   + "    <form action=\"\" method=\"post\">\n"
	   + "        <!-- (2): textarea will replace by CKEditor -->\n"
	   + "        <textarea id=\"editor1\" name=\"editor1\" cols=\"80\" rows=\"20\" style=\"height:100%\">";
	private static String CKEDITOR_END = "</textarea>\n"
	   + "         <!-- (3): Javascript code to replace textarea with id='editor1' by CKEditor -->\n"
	   + "        <script>\n"
	   + "            CKEDITOR.replace( 'editor1' , {height: 500});\n"
	   + "        </script>\n"
	   + "    </form>\n"
	   + "</body>\n"
	   + "</html>";
	private MainFrame mainFrame = null;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public SpellCheck() {
		setText(htmlText);
	}

	public SpellCheck(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public void setText(String html) {
		//LOG.printInfos(TT + ".setText(html len=" + html.length() + ")");
		this.htmlText = html;
	}

	public static boolean launchExternal(String html) {
		//LOG.printInfos(TT + ".launchExternal(html len=" + html.length() + ")");
		SpellCheck spell = new SpellCheck();
		spell.setText(html);
		return spell.launchExternal();
	}

	public boolean launchExternal() {
		//LOG.printInfos(TT + ".launchExternal()");
		//1 check for existing text
		if (htmlText.isEmpty()) {
			MessageDlg.show(mainFrame,
			   I18N.getMsg("spell.empty"),
			   I18N.getMsg("spell"),
			   true);
			return false;
		}
		//2 get only BODY part of the HTML text
		String text = Jsoup.parse(htmlText).body().outerHtml()
		   .replace("<body>", "").replace("</body>", "");
		//3 create working HTML file replacing any existing file
		String workingFile = EnvUtil.getTempDir() + File.separator + "verif.html";
		if (mainFrame != null) {
			workingFile = mainFrame.getProject().getPath() + File.separator + "verif.html";
		}
		//LOG.printInfos("external verif file=\"" + workingFile + "\"");
		IOUtil.fileWriteString(workingFile, CKEDITOR_HEAD + text + CKEDITOR_END);
		//4 launch Firefox/Chrome
		Net.openBrowser("file://" + workingFile);
		MessageDlg.show(mainFrame,
		   I18N.getMsg("spell.ok"),
		   "spell",
		   true);
		//5 end launcher
		return true;
	}

	public static boolean checkForCkEditor() {
		String fname = getCkEditorFileName();
		File f = new File(fname);
		return f.exists();
	}

	private static String getCkEditorFileName() {
		String f = EnvUtil.getHomeDir().getAbsolutePath()
		   + File.separator + "ckeditor"
		   + File.separator + "ckeditor.js";
		return f;
	}

	/**
	 * install CkEditor
	 *
	 * @param dlg
	 */
	public static void installCkEditor(SpellDlg dlg) {
		//check if navigator is Firefox or Chrome or Opera or Brave
		String title = "spelling",
		   msg = title + ".ckeditor_install_";
		if (checkForCkEditor()) {
			MessageDlg.show(dlg,
			   I18N.getMsg(msg + "allready"),
			   title,
			   true);
			return;
		}
		String src = Const.SpellCheker.CKEDITOR.toString();
		try {
			//unzip CkEditor in user.home.ckeditor
			ZipUtil.deflate(src, EnvUtil.getHomeDir().getAbsolutePath());
		} catch (IOException ex) {
			LOG.err("SpellChecker.installCkEditor() exception", ex);
		}
		if (!checkForCkEditor()) {
			MessageDlg.show(dlg,
			   I18N.getMsg(msg + "ok"),
			   title,
			   true);
		}
	}

}
