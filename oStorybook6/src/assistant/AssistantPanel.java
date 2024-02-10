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
package assistant;

import assistant.app.AppAssistant;
import i18n.I18N;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import api.mig.swing.MigLayout;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import storybook.tools.LOG;
import storybook.ui.MIG;

/**
 *
 * @author favdb
 */
public class AssistantPanel extends JPanel {

	private static final String TT = "AssistantPanel";

	private final JDialog app;
	private final boolean editable;
	private AssistantXml xml;
	private final Document assistantDoc;

	public AssistantPanel(JDialog app, String values) {
		this(app, false, AssistantXml.getXmlValues(values));
	}

	public AssistantPanel(JDialog app, boolean editable, Document assistantDoc) {
		this.app = app;
		this.editable = editable;
		this.assistantDoc = assistantDoc;
		this.xml = Assistant.getXml();
		initialize();
	}

	public void initialize() {
		//LOG.trace(TT + ".initialize()");
		this.setLayout(new MigLayout(MIG.WRAP + " 4"));
		//setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		if (xml == null) {
			return;
		}
		NodeList sections = xml.getSections();
		if (sections == null) {
			LOG.err("sections is null");
			return;
		}
		JTabbedPane tabbed = new JTabbedPane();
		if (sections.getLength() < 1) {
			return;
		}
		for (int i = 0; i < sections.getLength(); i++) {
			Node node = sections.item(i);
			String nm = node.getNodeName();
			if (nm.equals("#text")) {
				continue;
			}
			AssistantSection section = new AssistantSection(this, node, editable, assistantDoc);
			tabbed.add(I18N.getMsg("assistant.type." + nm), section);
		}
		this.add(tabbed, MIG.GROWX);
	}

	public void reinit() {
		//LOG.trace(TT + ".reinit()");
		this.removeAll();
		this.xml = Assistant.getXml();
		initialize();
	}

	public void setModified() {
		((AppAssistant) app).setModified(true);
	}

	public JDialog getApp() {
		return app;
	}

	public AppAssistant getAssistantApp() {
		return (AppAssistant) app;
	}

}
