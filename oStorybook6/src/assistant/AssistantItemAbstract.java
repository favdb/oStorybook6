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

import i18n.I18N;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.tools.LOG;
import storybook.tools.swing.FontUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class AssistantItemAbstract {

	private static final String TT = "AssistantItemAbstract";

	public AssistantTab panel;
	public AssistantXml xml;
	public Node item;
	private final boolean editable;
	private String itemName;
	private String itemTitle;
	private String itemPrompt;
	public JLabel lbTitle;
	private JTextField tfName;
	private JTextField tfTitle;
	public JTextArea taPrompt;
	public AssistantAddElement dlgItem;
	public final Document assistantDoc;

	public AssistantItemAbstract(AssistantTab tab, Node item, boolean editable, Document assistantDoc) {
		this.panel = tab;
		this.xml = Assistant.getXml();
		this.item = item;
		this.editable = editable;
		this.assistantDoc = assistantDoc;
		initialize();
	}

	/**
	 * initialize the item parameters: name, title, prompt
	 *
	 */
	public void initialize() {
		//LOG.trace(TT + ".initialize() for item=" + xml.getAttribute(item, "name"));
		if (item == null) {
			return;
		}
		itemName = ((Element) item).getAttribute("name");
		itemTitle = ((Element) item).getAttribute("title");
		itemPrompt = ((Element) item).getAttribute("prompt");
		taPrompt = Ui.MultiLineLabel(itemPrompt);
		JLabel lb = new JLabel();
		taPrompt.setFont(FontUtil.getItalic(lb.getFont()));
	}

	/**
	 * add the fields in the panel
	 */
	public void initFields() {
		String opt = MIG.get(MIG.GROWX);
		panel.add(new JLabel(I18N.getColonMsg("name")));
		tfName = new JTextField(itemName);
		panel.add(tfName, opt);
		panel.add(new JLabel(I18N.getColonMsg("title")));
		tfTitle = new JTextField(itemTitle);
		panel.add(tfTitle, opt);
		panel.add(new JLabel(I18N.getColonMsg("prompt")));
		taPrompt = new JTextArea(itemPrompt);
		taPrompt.setLineWrap(true);
		taPrompt.setWrapStyleWord(true);
		taPrompt.setRows(3);
		panel.add(taPrompt, opt);
	}

	public void addEditButton() {
		if (editable) {
			JPanel p = new JPanel();
			JButton btEdit = new JButton(IconUtil.getIconSmall(ICONS.K.EDIT));
			btEdit.addActionListener(e -> edit());
			p.add(btEdit);
			JButton btDelete = new JButton(IconUtil.getIconSmall(ICONS.K.MINUS));
			btDelete.addActionListener(e -> delete());
			p.add(btDelete);
			panel.add(p, MIG.TOP);
		} else {
			panel.add(new JLabel());
		}
	}

	public void setValues() {
		LOG.trace(TT + ".setValues()");
	}

	public String apply() {
		return "";
	}

	public void getResult(StringBuilder b) {
		//empty
	}

	public void edit() {
		//LOG.trace(TT + ".edit()");
		dlgItem = new AssistantAddElement(panel.getAppDialog(), item.getParentNode(), item);
		dlgItem.setVisible(true);
		if (!dlgItem.isCanceled()) {
			refresh();
		}
	}

	private void delete() {
		//todo delet an assistant item
	}

	public void refresh() {
		//LOG.trace(TT + ".refresh()");
		if (dlgItem == null) {
			LOG.err(TT + ".refresh() dlgItem is null");
			return;
		}
		lbTitle.setText(dlgItem.getTitre() + ": ");
		taPrompt.setText(dlgItem.getAide());
	}

	public void getResult() {
		//todo get the result
	}

}
