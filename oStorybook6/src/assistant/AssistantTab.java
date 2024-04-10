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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import api.mig.swing.MigLayout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.tools.LOG;
import storybook.ui.MIG;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class AssistantTab extends JPanel {

	private static final String TT = "AssistantTab";

	private final AssistantSection section;
	private final Node node;
	private NodeList childs;
	private final boolean editable;
	private JTextArea lbPrompt;
	private final AssistantXml xml;
	private List<AssistantItemAbstract> items = new ArrayList<>();
	private final Document assistantDoc;

	public AssistantTab(AssistantSection section, Node node, boolean editable, Document assistantDoc) {
		this.editable = editable;
		this.assistantDoc = assistantDoc;
		this.section = section;
		if (section == null) {
			LOG.err("section is null");
		}
		this.xml = Assistant.getXml();
		if (xml == null) {
			LOG.err("xml is null");
		}
		this.node = node;
		if (node == null) {
			LOG.err("node is null");
			return;
		}
		this.childs = node.getChildNodes();
		initialize();
	}

	public void initialize() {
		//LOG.trace(TT + ".initialize() for item=" + xml.getAttribute(node, "title"));
		setLayout(new MigLayout(MIG.get(MIG.FILLX, MIG.WRAP, MIG.HIDEMODE3), "[right][grow][]"));
		setMinimumSize(new Dimension(100, 600));
		setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		String prompt = ((Element) node).getAttribute("prompt");
		lbPrompt = Ui.MultiLineLabel(prompt);
		add(lbPrompt, MIG.get(MIG.SPAN, MIG.GROWX));
		if (prompt.isEmpty()) {
			lbPrompt.setVisible(false);
		}
		JButton btAddItem = new JButton(I18N.getMsg("assistant.add.item"));
		btAddItem.setIcon(IconUtil.getIconSmall(ICONS.K.PLUS));
		btAddItem.addActionListener(e -> addItem());
		if (editable) {
			add(btAddItem, MIG.get(MIG.SPAN, MIG.GROWX));
		}
		for (int i = 0; i < childs.getLength(); i++) {
			Node item = (Node) childs.item(i);
			if (item.getNodeName().equals("#text") || item.getNodeType() == Node.COMMENT_NODE) {
				continue;
			}
			switch (Assistant.getTYPE(item.getNodeName().toLowerCase())) {
				case COMBOBOX:
					AssistantItemCombo combo = new AssistantItemCombo(this, item, editable, assistantDoc);
					items.add(combo);
					break;
				case LISTBOX:
					AssistantItemList list = new AssistantItemList(this, item, editable, assistantDoc);
					items.add(list);
					break;
				case TEXTAREA:
					AssistantItemArea area = new AssistantItemArea(this, item, editable, assistantDoc);
					items.add(area);
					break;
				case TEXTFIELD:
					AssistantItemField field = new AssistantItemField(this, item, editable, assistantDoc);
					items.add(field);
					break;
				default:
					break;
			}
		}
	}

	public Node getNode() {
		return node;
	}

	private void addItem() {
		//LOG.trace(TT + ".addItem()");
		AssistantAddElement.show(section, node, null);
	}

	public AssistantXml getXml() {
		//LOG.trace(TT + ".getXml()");
		return xml;
	}

	public void setPrompt(String prompt) {
		lbPrompt.setText(prompt);
		lbPrompt.setVisible(prompt != null && !prompt.isEmpty());
	}

	public JDialog getAppDialog() {
		return (JDialog) section.getPanel().getApp();
	}

	public AppAssistant getAssistantApp() {
		return section.getAssistantApp();
	}

	public void getResult(StringBuilder b) {
		//LOG.trace(TT + ".getResult()");
		StringBuilder bb = new StringBuilder();
		for (AssistantItemAbstract item : items) {
			item.getResult(bb);
		}
		if (!bb.toString().isEmpty()) {
			String id = AssistantXml.getAttribute(node, "title");
			b.append("<").append(id).append(">");
			b.append(bb.toString());
			b.append("</").append(id).append(">");
		}
	}

}
