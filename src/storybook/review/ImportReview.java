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
package storybook.review;

import api.shef.ShefEditor;
import i18n.I18N;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JOptionPane;
import storybook.db.endnote.Endnote;
import storybook.db.scene.Scene;
import storybook.tools.LOG;
import storybook.tools.xml.Xml;
import storybook.ui.MainFrame;

/**
 * import Comments in the current project
 *
 * @author favdb
 */
public class ImportReview {

	private static final String TT = "ImportReview";
	private final MainFrame mainFrame;
	private final File file;
	private Xml xml;

	public static boolean exec(MainFrame mainFrame, File file) {
		//LOG.trace(TT + ".exec(mainFrame, file=" + file.getAbsolutePath() + ")");
		ImportReview imp = new ImportReview(mainFrame, file);
		if (!imp.exec()) {
			JOptionPane.showMessageDialog(null,
			   I18N.getMsg("comments.import.notok", file.getAbsolutePath()),
			   I18N.getMsg("import"),
			   JOptionPane.ERROR_MESSAGE);
		}
		return true;
	}

	public ImportReview(MainFrame mainFrame, File file) {
		this.mainFrame = mainFrame;
		this.file = file;
	}

	public boolean exec() {
		//LOG.trace(TT + ".exec()");
		boolean rc = false;
		try {
			xml = new Xml(file.getAbsolutePath());
		} catch (FileNotFoundException ex) {
			LOG.err(TT + ".exec() error, file " + file.getAbsolutePath() + " not found");
			return false;
		}
		if (xml != null && xml.open()) {
			if (checkUUID()) {
				org.w3c.dom.NodeList nodes = xml.getRoot().getElementsByTagName("endnote");
				if (nodes.getLength() > 0) {
					rc = getComments(nodes);
				} else {
					LOG.err(TT + ".exec() error: No Xml nodes for Comments");
				}
			}
			xml.close();
		} else {
			LOG.err(TT + ".exec() error: xml is null");
		}
		return rc;
	}

	private boolean checkUUID() {
		//LOG.trace(TT + ".checkUUID()");
		String uuid = xml.getRoot().getAttribute("uuid");
		if (!uuid.equals(mainFrame.getBook().getUUID())) {
			if (JOptionPane.showConfirmDialog(null,
			   I18N.getMsg("comments.import.uuid"),
			   I18N.getMsg("import"),
			   JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
				return false;
			}
		}
		return true;
	}

	private boolean getComments(org.w3c.dom.NodeList nodes) {
		//LOG.trace(TT + ".getComments(nodes lenght=" + nodes.getLength() + ")");
		boolean rc = true;
		List<Endnote> ls = new ArrayList<>();
		for (int i = 0; i < nodes.getLength(); i++) {
			Endnote comment = Endnote.importXml(mainFrame, nodes.item(i));
			comment.setId(-1L);
			mainFrame.getBookController().newEntity(comment);
			ls.add(comment);
		}
		Collections.sort(ls, new BySort());
		for (Endnote endnote : ls) {
			String x[] = endnote.getSort().split("[.]");
			if (x.length > 3) {
				Scene scene = mainFrame.project.scenes.get(endnote.getScene().getId());
				ShefEditor ht = new ShefEditor(mainFrame.project.getPath(), "none", scene.getSummary());
				int idx = Integer.parseInt(x[3]);
				ht.setCaretPosition(idx);
				ht.insertText(endnote.getLinkToEndnote(""));
				scene.setSummary(ht.getText());
				mainFrame.getBookController().updateEntity(scene);
			}
		}
		if (!ls.isEmpty()) {
			mainFrame.fileSave(true);
			mainFrame.refresh();
		}
		return rc;
	}

	private static class BySort implements Comparator<Endnote> {

		@Override
		public int compare(Endnote a, Endnote b) {
			return a.getSort().compareTo(b.getSort());
		}
	}

}
