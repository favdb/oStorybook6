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

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import storybook.tools.swing.FontUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class AssistantItemArea extends AssistantItemAbstract {

	private JTextArea taData;

	public AssistantItemArea(AssistantTab tab, Node item, boolean editable, Document assistantDoc) {
		super(tab, item, editable, assistantDoc);
	}

	@Override
	public void initialize() {
		super.initialize();
		lbTitle = new JLabel(((Element) item).getAttribute("title") + ": ");
		panel.add(lbTitle);
		taData = new JTextArea();
		taData.setLineWrap(true);
		taData.setWrapStyleWord(true);
		taData.setRows(3);
		if (((Element) item).hasAttribute("prompt")) {
			String str = "<html><body><p style=\"width: 400px;\">" + ((Element) item).getAttribute("prompt") + "</p></body></html>";
			taData.setToolTipText(str);
		}
		JScrollPane sc = new JScrollPane(taData);
		sc.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		panel.add(sc, MIG.GROWX);
		addEditButton();
		if (((Element) item).hasAttribute("prompt")) {
			JLabel lb = new JLabel();
			taPrompt = Ui.MultiLineLabel(((Element) item).getAttribute("prompt"));
			taPrompt.setFont(FontUtil.getItalic(lb.getFont()));
		}
		setValues();
	}

	@Override
	public void refresh() {
		super.refresh();
		taData.setToolTipText(((Element) item).getAttribute("prompt"));
	}

	@Override
	public void setValues() {
		if (assistantDoc != null) {
			String str = AssistantXml.getText(assistantDoc, ((Element) item).getAttribute("title"));
			taData.setText(str);
		}
	}

	@Override
	public String apply() {
		if (taData.getText().isEmpty()) {
			return null;
		}
		return taData.getText().trim();
	}

	@Override
	public void getResult(StringBuilder b) {
		if (taData.getText().isEmpty()) {
			return;
		}
		String id = AssistantXml.getAttribute(item, "title");
		b.append("<").append(id).append(">");
		String str = taData.getText();
		b.append(str);
		b.append("</").append(id).append(">");
	}

}
