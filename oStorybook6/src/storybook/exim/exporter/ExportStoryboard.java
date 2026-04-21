/*
 * Copyright (C) 2021 favdb
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

import i18n.I18N;
import javax.swing.JOptionPane;
import storybook.db.scene.Scene;
import storybook.db.scene.Scenes;
import storybook.tools.StringUtil;
import storybook.ui.frames.main.MainFrame;

/**
 * export to a Storyboard format
 *
 * @author favdb
 */
public class ExportStoryboard extends AbstractExport {

	public static void execute(MainFrame mainFrame) {
		ExportStoryboard exp = new ExportStoryboard(mainFrame);
		exp.create();
	}

	public ExportStoryboard(MainFrame m) {
		super(m, "osbd");
	}

	private void create() {
		String v = StringUtil.capitalize(book.getTitle());
		if (this.askFileExists(v) && JOptionPane.showConfirmDialog(mainFrame,
			I18N.getMsg("export.replace", param.getFileName()),
			I18N.getMsg("export"),
			JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
			return;
		}
		if (!openFile("storyboard_" + v, false)) {
			return;
		}
		writeShots();
		closeFile(true);
	}

	private void writeShots() {
		for (Scene scene : Scenes.find(mainFrame)) {
			if (scene.getInformative()) {
				continue;
			}
			writeText(scene.toStoryboard());
		}
	}

}
