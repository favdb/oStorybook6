/*
 STORYBOOK: Scene-based software for novelists and authors.
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

import api.jsoup.internal.StringUtil;
import i18n.I18N;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import javax.swing.SwingUtilities;
import storybook.App;
import storybook.Const;
import storybook.Pref;
import storybook.tools.DateUtil;
import storybook.tools.LOG;

public class Updater {

	private static final String TT = "Updater";

	private Updater() {
		// empty
	}

	/**
	 * check if to update
	 *
	 * @param force
	 * @return
	 */
	public static boolean checkForUpdate(boolean force) {
		//LOG.trace(TT + ".checkForUpdate(".concat(force ? "force" : "no force") + ")");
		String doUpdater = App.preferences.getString(Pref.KEY.UPDATER_DO, "1");
		boolean toDo = false;
		switch (doUpdater) {
			case "0"://initialized value must be tranformed into 1
				App.preferences.setString(Pref.KEY.UPDATER_DO, "1");
				setDateUpdater();
				break;
			case "1"://never check, default value
				break;
			case "2"://check only every 90 days
				toDo = checkDateUpdater();
				break;
			case "3":// check forced
				toDo = true;
				break;
			default:
				break;
		}
		if (toDo || force) {
			try {
				// get version
				URL url = new URL(Net.KEY.VERSIONS.toString());
				String versionStr;
				try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
					String inputLine;
					versionStr = "";
					int c = 0, nc = -1;
					while ((inputLine = in.readLine()) != null) {
						versionStr = inputLine;
						if (inputLine.contains("Versions")) {
							nc = c + 1;
						}
						if (c == nc) {
							break;
						}
						c++;
					}
				}
				// compare version
				int remoteVersion = calculateVersion(versionStr);
				int localVersion = calculateVersion(Const.getVersion());
				//LOG.trace("updater: remoteVersion=" + versionStr + ", localVersion=" + Const.getVersion());
				if (localVersion < remoteVersion) {
					SwingUtilities.invokeLater(() -> {
						String updateUrl = Net.KEY.VERSIONS.toString();
						BrowserDlg.show(updateUrl, I18N.getMsg("update.title"));
					});
					setDateUpdater();
					return false;
				}
			} catch (SocketException | UnknownHostException e) {
				return true;
			} catch (IOException e) {
				LOG.err("Updater.checkForUpdate() Exception:", e);
			}
		}
		return true;
	}

	private static int calculateVersion(String str) {
		//LOG.trace(TT+"calculateVersion("+str+")");
		int ret = 0;
		String[] s = str.split("\\.");
		if (str.contains(":")) {
			String[] x = str.split(":");
			s = x[0].split("\\.");
		}
		if (StringUtil.isNumeric(str)) {
			return Integer.parseInt(str);
		}
		if (s.length == 3
				&& StringUtil.isNumeric(s[0])
				&& StringUtil.isNumeric(s[1])
				&& StringUtil.isNumeric(s[2])) {
			ret += Integer.parseInt(s[0]) * 1000000;
			ret += Integer.parseInt(s[1]) * 1000;
			ret += Integer.parseInt(s[2]);
			return ret;
		}
		if (StringUtil.isNumeric(s[0]) && StringUtil.isNumeric(s[1])) {
			ret += Integer.parseInt(s[0]) * 1000;
			ret += Integer.parseInt(s[1]);
		}
		return ret;
	}

	private static boolean checkDateUpdater() {
		boolean rc = false;
		String dateLast = App.preferences.getString(Pref.KEY.UPDATER_LAST, "");
		//LOG.trace("Updater.checkDateUpdater() last="+dateLast);
		if (!"".equals(dateLast)) {
			Date ddLast = DateUtil.stringToDate(dateLast);
			if (ddLast == null) {
				return false;
			}
			Date ddj = new Date();
			//si la différence entre ddLast et ddj est plus grand que 90 jours alors vérif
			long diff = ddj.getTime() - ddLast.getTime();
			long diffDays = (diff / (1000 * 60 * 60 * 24));
			//LOG.trace("diffDays="+diffDays);
			if (diffDays > 90) {
				App.preferences.setString(Pref.KEY.UPDATER_LAST, DateUtil.dateToString(ddj, true));
				rc = true;
			}
		} else {
			App.preferences.setString(Pref.KEY.UPDATER_LAST, DateUtil.dateToString(new Date(), true));
		}
		return (rc);
	}

	private static void setDateUpdater() {
		Date ddj = new Date();
		App.preferences.setString(Pref.KEY.UPDATER_LAST, DateUtil.dateToString(ddj, true));
	}
}
