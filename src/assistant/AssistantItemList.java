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

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import storybook.tools.swing.FontUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class AssistantItemList extends AssistantItemAbstract {

	private JList lsData;
	private DefaultListModel model;

	public AssistantItemList(AssistantTab tab, Node item, boolean editable, Document assistantDoc) {
		super(tab, item, editable, assistantDoc);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initialize() {
		super.initialize();
		lbTitle = new JLabel(((Element) item).getAttribute("title") + ": ");
		panel.add(lbTitle);
		initLsData();
		panel.add(lsData);
		addEditButton();
		if (((Element) item).hasAttribute("prompt")) {
			//cbData.setToolTipText(((Element) item).getAttribute("prompt"));
			JLabel lb = new JLabel();
			taPrompt = Ui.MultiLineLabel(((Element) item).getAttribute("prompt"));
			taPrompt.setFont(FontUtil.getItalic(lb.getFont()));
			panel.add(taPrompt, MIG.get(MIG.SKIP, MIG.SPAN, MIG.GROWX));
		}
		setValues();
	}

	@SuppressWarnings("unchecked")
	private void initLsData() {
		//LOG.trace(TT + ".initLsData()");
		if (lsData != null) {
			lsData.removeAll();
		} else {
			lsData = new JList();
		}
		lsData.setVisibleRowCount(5);
		model = (DefaultListModel) lsData.getModel();
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
				model.addElement(lb);
			}
		} else {
			String sx[] = s.split(";");
			for (String ssx : sx) {
				model.addElement(ssx.trim());
			}
		}
	}

	@Override
	public void setValues() {
		if (assistantDoc != null) {
			String str = AssistantXml.getText(assistantDoc, ((Element) item).getAttribute("title"));
			if (str != null && !str.isEmpty()) {
				lsData.setSelectedValue(str, true);
			}
		}
	}

	@Override
	public String apply() {
		return ((String) lsData.getSelectedValue()).trim();
	}

	@Override
	public void refresh() {
		super.refresh();
		dlgItem.setList(lsData);
	}

	@Override
	public void getResult(StringBuilder b) {
		if (lsData.getSelectedIndex() < 0) {
			return;
		}
		String id = AssistantXml.getAttribute(item, "title");
		b.append("<").append(id).append(">");
		String str = (String) lsData.getSelectedValue();
		b.append(str);
		b.append("</").append(id).append(">");
	}

}
