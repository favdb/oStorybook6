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

import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import api.mig.swing.MigLayout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import resources.icons.ICONS;
import resources.icons.IconButton;
import storybook.tools.LOG;
import storybook.tools.swing.FontUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class AssistantItemCombo extends AssistantItemAbstract {

	private static final String TT = "AssistantItemCombo";

	private JComboBox cbData;
	private IconButton btHelp;
	private String helpLink;

	public AssistantItemCombo(AssistantTab tab, Node item, boolean editable, Document assistantDoc) {
		super(tab, item, editable, assistantDoc);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initialize() {
		//LOG.trace(TT + ".initialize() " + xml.getAttribute(item, "title"));
		super.initialize();
		lbTitle = new JLabel(((Element) item).getAttribute("title") + ": ");
		panel.add(lbTitle);
		initCbData();
		JPanel p = new JPanel(new MigLayout(MIG.INS0, "[grow][]"));
		p.add(cbData);
		helpLink = ((Element) item).getAttribute("help");
		btHelp = new IconButton("btHelp", ICONS.K.HELP, e -> openBrowser());
		btHelp.setToolTipText(helpLink);
		if (!helpLink.isEmpty()) {
			p.add(btHelp);
		}
		panel.add(p);
		addEditButton();
		if (((Element) item).hasAttribute("prompt")) {
			//cbData.setToolTipText(((Element) item).getAttribute("prompt"));
			JLabel lb = new JLabel();
			String prompt = ((Element) item).getAttribute("prompt");
			taPrompt = Ui.MultiLineLabel(prompt);
			taPrompt.setFont(FontUtil.getItalic(lb.getFont()));
			if (!prompt.isEmpty()) {
				panel.add(taPrompt, MIG.get(MIG.SKIP, MIG.SPAN, MIG.GROWX));
			}
		}
		setValues();
	}

	@SuppressWarnings("unchecked")
	private void initCbData() {
		//LOG.trace(TT + ".initCbData()");
		if (cbData != null) {
			cbData.removeAllItems();
		} else {
			cbData = new JComboBox();
		}
		cbData.setRenderer(new SpecialRenderer());
		cbData.addItem("");
		String s = ((Element) item).getAttribute("elements");
		if (s.isEmpty()) {
			NodeList elems = item.getChildNodes();
			for (int i = 0; i < elems.getLength(); i++) {
				Node ni = elems.item(i);
				if (!ni.getNodeName().equals("elem")) {
					continue;
				}
				JLabel lb = new JLabel(xml.getAttribute(ni, "title"));
				lb.setToolTipText(xml.getAttribute(ni, "prompt"));
				cbData.addItem(lb);
			}
		} else {
			String sx[] = s.split(";");
			for (String ssx : sx) {
				JLabel lb = new JLabel(ssx.trim());
				cbData.addItem(lb);
			}
		}
	}

	@Override
	public String apply() {
		Object obj = cbData.getSelectedItem();
		if (obj != null && obj instanceof JLabel) {
			return ((JLabel) obj).getText();
		} else if (obj != null && obj instanceof String) {
			if (((String) obj).isEmpty()) {
				return null;
			}
			return (String) obj;
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void refresh() {
		//LOG.trace(TT + ".refresh()");
		super.refresh();
		initCbData();
		helpLink = dlgItem.getHelp();
		if (btHelp != null) {
			btHelp.setToolTipText(helpLink);
		}
	}

	public void openBrowser() {
		//LOG.trace(".openBrowser() for \"" + helpLink + "\"");
		try {
			Desktop.getDesktop().browse(new URI(helpLink));
		} catch (URISyntaxException | IOException e) {
			LOG.err("Net.openBrowser(" + helpLink + ")", e);
		}
	}

	@Override
	public void setValues() {
		if (assistantDoc != null) {
			int sel = -1;
			String str = AssistantXml.getText(assistantDoc, ((Element) item).getAttribute("title"));
			if (str != null && !str.isEmpty()) {
				for (int i = 0; i < cbData.getItemCount(); i++) {
					if (cbData.getItemAt(i) instanceof JLabel) {
						JLabel lb = (JLabel) cbData.getItemAt(i);
						if (lb.getText().equals(str)) {
							sel = i;
							break;
						}
					}
				}
				cbData.setSelectedIndex(sel);
			}
		}
	}

	@Override
	public void getResult(StringBuilder b) {
		if (cbData.getSelectedIndex() == 0) {
			return;
		}
		String id = AssistantXml.getAttribute(item, "title");
		b.append("<").append(id).append(">");
		String str = ((JLabel) cbData.getSelectedItem()).getText();
		b.append(str);
		b.append("</").append(id).append(">");
	}

	public static class SpecialRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean sel, boolean focus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, sel, focus);
			if (value instanceof Node) {
				label.setText(((Element) value).getAttribute("title").trim());
				String prompt = ((Element) value).getAttribute("prompt").trim();
				label.setToolTipText(prompt);
			}
			if (value instanceof JLabel) {
				label.setText(((JLabel) value).getText());
				label.setToolTipText(((JLabel) value).getToolTipText());
			}
			return label;
		}

	}

}
