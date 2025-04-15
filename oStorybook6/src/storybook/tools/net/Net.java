/*
Storybook: Scene-based software for novelists and authors.
Copyright (C) 2008 - 2011 Martin Mustun

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
package storybook.tools.net;

import i18n.I18N;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import storybook.db.location.Location;
import storybook.tools.LOG;
import storybook.tools.ListUtil;

public class Net {

	private static final String TT = "Net";

	public enum KEY {
		ROOT("http://ostorybook.eu/"),
		HOME(ROOT + "v6/"),
		CONTACT("mailto:admin@ostorybook.eu"),
		DOC("documentation.html"),
		FAQ("faq.html"),
		DOWNLOAD(ROOT + "download/"),
		DOWNLOAD_CURRENT(DOWNLOAD + "current/"),
		VERSION(DOWNLOAD_CURRENT + "Versions.txt"),
		UPDATE(DOWNLOAD_CURRENT + "Update.zip"),
		DICTIONARIES(ROOT + "dictionary/"),
		ASSISTANT(ROOT + "assistant/"),
		TUXFAMILY("http://ostorybook.tuxfamily.org/"),
		BUG(TUXFAMILY + "support"),
		HELP_ONLINE(TUXFAMILY + "forum"),
		OSM("http://www.openstreetmap.org/"),
		DO_UPDATE("false");
		private final String text;

		private KEY(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public static String getI18nUrl(KEY url) {
		String languages[] = {// allowed languages, default is fr
			"fr", "en"
		};
		String loc = Locale.getDefault().getLanguage(), lng = languages[0];
		// select the language
		for (String l : languages) {
			if (l.equals(loc)) {
				lng = l;
			}
		}
		return url.toString() + lng + "/";
	}

	public static void openBrowser(String path) {
		//LOG.trace(TT + ".openBrowser(" + path + ")");
		try {
			Desktop.getDesktop().browse(new URI(path));
		} catch (URISyntaxException | IOException e) {
			LOG.err(TT + ".openBrowser(" + path + ")", e);
		}
	}

	public static void openOSM(Location location) {
		//LOG.trace(TT+".openOSM(location=" + location.toString() + ")");
		List<String> list = new ArrayList<>();
		if (!location.getCity().isEmpty()) {
			list.add(location.getCity());
			if (!location.getAddress().isEmpty()) {
				list.add(location.getAddress());
			}
			if (!location.getCountry().isEmpty()) {
				list.add(location.getCountry());
			}
		}
		String b = ListUtil.join(list);
		String str = "";
		try {
			str = "search?query=" + URLEncoder.encode(b, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			LOG.err(TT + "openOSM(location=" + LOG.trace(location) + ") URLEncoder\n", ex);
		}
		if (location.getGps() != null && !location.getGps().isEmpty()) {
			str = "#map=15/" + location.getGps();
		} else if (str.isEmpty()) {
			JOptionPane.showMessageDialog(null,
					I18N.getMsg("location.gps.no_position"),
					I18N.getMsg("location.gps.show"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		openBrowser(KEY.OSM.toString() + str);
	}

	/**
	 * open an URL, may be a local file
	 *
	 * @param evt
	 */
	public static void openUrl(HyperlinkEvent evt) {
		//LOG.trace(TT + ".openUrl(evt description=\"" + evt.getDescription() + "\")");
		String url = evt.getDescription();
		try {
			if (url.toLowerCase().startsWith("http")) {
				//App.trace("open browser");
				Desktop.getDesktop().browse(new URI(url));
			} else if (url.startsWith("#")) {
				if (evt.getSource() instanceof JEditorPane) {
					JEditorPane ep = (JEditorPane) evt.getSource();
					ep.scrollToReference(url.substring(1));
				}
			} else if (url.startsWith("file")) {
				openFile(url);
			} else {
				// trying to open any other link by default application
				Desktop.getDesktop().browse(new URI(url));
			}
		} catch (URISyntaxException ex) {
			LOG.err(TT + ".openUrl URI exception ", ex);
		} catch (IOException ex) {
			//LOG.err(TT + ".openUrl IOException for " + url, ex);
		}
	}

	private static void openFile(String url) {
		//LOG.trace(TT+".openFile(url=\"" + url + "\")");
		File file = new File(url.replace("file://", ""));
		if (file.exists()) {
			try {
				Desktop.getDesktop().edit(file);
			} catch (UnsupportedOperationException ex) {
				try {
					Desktop.getDesktop().open(file);
				} catch (IOException ex1) {
					LOG.err(TT + "file exception for " + url);
				}
			} catch (IOException ex) {
				LOG.err(TT + "file exception for " + url);
			}
		} else {
			LOG.err(TT + "warning file not exists: " + file.getAbsolutePath());
		}
	}

}
