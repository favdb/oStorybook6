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
import javax.swing.JTextField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import storybook.ui.MIG;

/**
 *
 * @author favdb
 */
public class AssistantItemField extends AssistantItemAbstract {

	private JTextField taData;
	private JTextField tfLen;

	public AssistantItemField(AssistantTab tab, Node item, boolean editable, Document assistantDoc) {
		super(tab, item, editable, assistantDoc);
	}

	@Override
	public void initialize() {
		super.initialize();
		String growx = MIG.GROWX;
		lbTitle = new JLabel(((Element) item).getAttribute("title") + ": ");
		panel.add(lbTitle);
		taData = new JTextField();
		if (((Element) item).hasAttribute("len")) {
			int len = Integer.parseInt(((Element) item).getAttribute("len"));
			if (len > 0) {
				taData.setColumns(len);
				growx = "";
			}
		}
		if (((Element) item).hasAttribute("prompt")) {
			taData.setToolTipText(((Element) item).getAttribute("prompt"));
		}
		panel.add(taData, growx);
		addEditButton();
		setValues();
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
			return "";
		}
		return taData.getText().trim();
	}

	@Override
	public void refresh() {
		super.refresh();
		if (((Element) item).hasAttribute("len")) {
			int len = Integer.parseInt(((Element) item).getAttribute("len"));
			if (len > 0) {
				taData.setColumns(len);
			}
		}
		taData.setToolTipText(((Element) item).getAttribute("prompt"));
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
