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
package storybook.project;

import assistant.Assistant;
import assistant.AssistantPanel;
import assistant.AssistantSection;
import assistant.AssistantXml;
import i18n.I18N;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import api.mig.swing.MigLayout;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import storybook.App;
import storybook.Pref;
import storybook.db.book.Book;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;

/**
 *
 * @author favdb
 */
public class PropAssistantPanel extends JPanel {

	private static final String TT = "AssistantBookPanel";

	private final MainFrame mainFrame;
	private Pref pref;
	private final JDialog dlg;
	private final Book book;
	private final AssistantXml xml;
	private AssistantSection bookSection;

	public PropAssistantPanel(JDialog dlg, MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		this.book = mainFrame.getBook();
		this.dlg = dlg;
		this.xml = Assistant.getXml();
		initialize();
	}

	private void initialize() {
		this.setLayout(new MigLayout(MIG.get(MIG.FILLX, MIG.WRAP, MIG.INS1), "[grow]"));
		pref = App.preferences;
		String values = book.info.assistantGet();
		try {
			Node node = xml.getSection("book").item(0);
			Document doc = AssistantXml.getXmlValues(values);
			AssistantPanel panel = new AssistantPanel(this.dlg, false, doc);
			bookSection = new AssistantSection(panel, node, false, doc);
			add(bookSection, MIG.GROW);
		} catch (Exception ex) {
			add(new JLabel(I18N.getMsg("assistant.empty")), MIG.GROW);
			bookSection = null;
		}
	}

	/**
	 * apply the values
	 *
	 */
	public void apply() {
		if (bookSection == null) {
			return;
		}
		StringBuilder b = new StringBuilder();
		String values = bookSection.getResult(b);
		book.info.assistantSet(values);
	}

}
