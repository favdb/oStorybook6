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
import java.awt.Color;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import api.mig.swing.MigLayout;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import storybook.tools.TextUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class AssistantAddTab extends JDialog {

	private static final String TT = "AssistantAddTab";
	private JTextField tfName;
	private JTextField tfTitle;
	private JTextField tfPrompt;
	private boolean cancel;
	private boolean editable;
	private AssistantXml xml;
	private Node node;
	private JTextArea lbMsg;

	public AssistantAddTab(JDialog panel, AssistantXml xml, Node node, boolean editable) {
		super(panel, true);
		this.xml = xml;
		this.node = node;
		this.editable = editable;
		initialize();
		if (node != null) {
			setNode();
		}
	}

	public AssistantAddTab(AssistantPanel panel, AssistantXml xml, Node node, boolean editable) {
		this(panel.getApp(), xml, node, editable);
	}

	private void initialize() {
		//LOG.trace(TT+".initialize()");
		setLayout(new MigLayout(MIG.WRAP, "[][]"));
		setTitle((node != null ? I18N.getMsg("assistant.edit.tab") : I18N.getMsg("assistant.add.tab")));
		setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		add(new JLabel(I18N.getColonMsg("name")));
		tfName = new JTextField();
		tfName.setColumns(8);
		tfName.setEnabled(node == null);
		add(tfName);
		add(new JLabel(I18N.getColonMsg("title")));
		tfTitle = new JTextField();
		tfTitle.setColumns(16);
		add(tfTitle);
		add(new JLabel(I18N.getColonMsg("assistant.prompt")));
		tfPrompt = new JTextField();
		//tfPrompt.setColumns(16);
		add(tfPrompt, MIG.GROWX);
		lbMsg = Ui.MultiLineLabel("");
		lbMsg.setForeground(Color.red);
		lbMsg.setVisible(false);
		add(lbMsg, MIG.get(MIG.SPAN, MIG.GROW));
		JButton btOK = new JButton(I18N.getMsg("ok"));
		btOK.addActionListener(e -> apply());
		add(btOK, MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.RIGHT));
		JButton btCancel = new JButton(I18N.getMsg("cancel"));
		btCancel.addActionListener(e -> {
			cancel = true;
			dispose();
		});
		add(btCancel, MIG.RIGHT);
		pack();
		setLocationRelativeTo(this.getParent());
	}

	private void apply() {
		//LOG.trace(TT + ".apply()");
		String isok = "";
		if (tfName.getText().isEmpty()) {
			tfName.setBorder(BorderFactory.createLineBorder(Color.red));
			isok += I18N.getMsg("missing") + ": " + I18N.getMsg("name") + "\n";
		} else if (!TextUtil.checkAlpha(tfName.getText())) {
			tfName.setBorder(BorderFactory.createLineBorder(Color.red));
			isok += I18N.getMsg("error") + ": " + I18N.getMsg("name") + "\n";
		}
		if (tfTitle.getText().isEmpty()) {
			tfTitle.setBorder(BorderFactory.createLineBorder(Color.red));
			isok += I18N.getMsg("missing") + ": " + I18N.getMsg("title") + "\n";
		}
		if (isok.isEmpty()) {
			dispose();
		} else {
			lbMsg.setText(isok);
			lbMsg.setVisible(true);
			pack();
		}
	}

	public void setData(String name, String title, String prompt) {
		tfName.setText(name);
		tfName.setCaretPosition(0);
		tfTitle.setText(title);
		tfTitle.setCaretPosition(0);
		if (prompt != null && !prompt.isEmpty()) {
			tfPrompt.setText(prompt);
			tfPrompt.setCaretPosition(0);
		}
	}

	public String getTfName() {
		return tfName.getText();
	}

	public String getTfTitle() {
		return tfTitle.getText();
	}

	public String getTfPrompt() {
		return tfPrompt.getText();
	}

	boolean isCanceled() {
		return cancel;
	}

	public void setNode() {
		if (node != null) {
			setData(((Element) node).getAttribute("name"),
					((Element) node).getAttribute("title"),
					((Element) node).getAttribute("prompt"));
		}
	}

}
