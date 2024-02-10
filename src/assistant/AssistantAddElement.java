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

import assistant.Assistant.FIELD_TYPE;
import assistant.app.AppAssistant;
import i18n.I18N;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import api.mig.swing.MigLayout;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import storybook.tools.TextUtil;
import storybook.ui.MIG;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class AssistantAddElement extends JDialog implements ItemListener {

	private static final String TT = "AssistantAddElement",
			L_NAME = "name", L_TITLE = "title", L_PROMPT = "prompt";

	/**
	 * creat a new element in existing tab node
	 *
	 * @param section
	 * @param tabNode
	 * @param node
	 * @return
	 */
	public static boolean show(AssistantSection section, Node tabNode, Node node) {
		AssistantAddElement dlg;
		dlg = new AssistantAddElement(section.getPanel().getApp(), tabNode, node);
		dlg.setVisible(true);
		return dlg.cancel;
	}

	private final AssistantXml xml;
	private final Node tabNode;
	private Node node;

	private JComboBox cbType;
	private boolean cancel = false;
	private JButton btOK;
	private JPanel panelText;
	private String typeField = "textfield";
	private JPanel panelArea, panelList, panelCombo;
	private JTextField tfName = new JTextField(""), tfTitle = new JTextField(""), tfPrompt = new JTextField("");
	private JTextField tfLen;
	private JPanel tablePanel;
	private JTable table;
	private DefaultTableModel tableModel;
	private JTextField tfHelp;
	private JTextArea lbMsg;

	public AssistantAddElement(JDialog comp, Node tabNode, Node node) {
		super(comp, true);
		//LOG.trace(TT + "AddElement from JDialog");
		this.xml = Assistant.getXml();
		this.tabNode = tabNode;
		if (node != null) {
			this.node = node;
		} else {
			this.node = null;
		}
		initialize();
	}

	@SuppressWarnings("unchecked")
	public void initialize() {
		//LOG.trace(TT + ".initialize()");
		setLayout(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.WRAP), "[][grow]"));
		setTitle((node != null
				? I18N.getMsg("assistant.item.edit") : I18N.getMsg("assistant.item.add")));
		add(new JLabel(I18N.getColonMsg("assistant.type")));
		cbType = new JComboBox();
		for (FIELD_TYPE t : FIELD_TYPE.values()) {
			cbType.addItem(t);
		}
		add(cbType);
		initFields();
		lbMsg = Ui.MultiLineLabel("");
		lbMsg.setForeground(Color.red);
		lbMsg.setVisible(false);
		add(lbMsg, MIG.get(MIG.SPAN, MIG.GROW));
		btOK = new JButton(I18N.getMsg("ok"));
		btOK.addActionListener(e -> apply());
		add(btOK, MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.RIGHT));
		JButton btCancel = new JButton(I18N.getMsg("cancel"));
		btCancel.addActionListener(e -> {
			cancel = true;
			dispose();
		});
		add(btCancel, MIG.RIGHT);
		setData();
		setFieldVisible();
		pack();
		setLocationRelativeTo(this.getParent());
		cbType.addItemListener(this);
		this.setModal(true);
	}

	public boolean isCanceled() {
		return cancel;
	}

	private JTextField addTextField(JPanel panel, String name, int len) {
		panel.add(new JLabel(I18N.getColonMsg(name)));
		JTextField tf = new JTextField();
		tf.setColumns(len);
		panel.add(tf);
		return tf;
	}

	private void addHeader() {
		JPanel panel = (JPanel) this.getContentPane();
		tfName = addTextField(panel, L_NAME, 8);
		tfTitle = addTextField(panel, L_TITLE, 16);
		tfPrompt = addTextField(panel, L_PROMPT, 32);
	}

	private JPanel addField(String str) {
		JPanel panel = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP), "[][grow]"));
		//panel.setBorder(BorderFactory.createRaisedBevelBorder());
		add(panel, MIG.get(MIG.SPAN, MIG.GROWX));
		return panel;
	}

	private void initFields() {
		addHeader();
		panelText = addField("textfield");
		tfLen = addTextField(panelText, "length", 3);
		panelArea = addField("textarea");
		initTable();
		panelList = addField("listbox");
		panelCombo = addField("combobox");
		tfHelp = addTextField(panelCombo, "assistant.help", 32);
		addTable();
	}

	private void setData() {
		if (node == null) {
			return;
		}
		typeField = node.getNodeName();
		FIELD_TYPE tf = Assistant.getTYPE(typeField);
		cbType.setSelectedItem(tf);
		cbType.setEnabled(false);
		tfName.setText(((Element) node).getAttribute(L_NAME));
		tfName.setCaretPosition(0);
		tfName.setEnabled(false);
		tfTitle.setText(((Element) node).getAttribute(L_TITLE));
		tfTitle.setCaretPosition(0);
		tfPrompt.setText(((Element) node).getAttribute(L_PROMPT));
		tfPrompt.setCaretPosition(0);
		if (tf == FIELD_TYPE.TEXTFIELD && !xml.getAttribute(node, "len").isEmpty()) {
			tfLen.setText(xml.getAttribute(node, "len"));
		}
		if (tf == FIELD_TYPE.COMBOBOX && !xml.getAttribute(node, "help").isEmpty()) {
			tfHelp.setText(xml.getAttribute(node, "help"));
		}
	}

	private void setFieldVisible() {
		panelText.setVisible(false);
		panelArea.setVisible(false);
		panelList.setVisible(false);
		panelCombo.setVisible(false);
		tablePanel.setVisible(false);
		switch ((FIELD_TYPE) cbType.getSelectedItem()) {
			case TEXTFIELD:
				setDataTextfield();
				panelText.setVisible(true);
				break;
			case TEXTAREA:
				setDataTextarea();
				panelArea.setVisible(true);
				break;
			case LISTBOX:
				setDataList();
				panelList.setVisible(true);
				tablePanel.setVisible(true);
				break;
			case COMBOBOX:
				setDataCombo();
				panelCombo.setVisible(true);
				tablePanel.setVisible(true);
				break;
		}
	}

	private void setDataTextfield() {
		// there is no data to add
	}

	private void setDataTextarea() {
		// there is no data to add
	}

	private void initTable() {
		//create table with data
		tableModel = new DefaultTableModel();
		tableModel.addColumn(I18N.getMsg("title"));
		tableModel.addColumn(I18N.getMsg("prompt"));
		table = new JTable(tableModel);
		table.setRowHeight(tfName.getFont().getSize() + 2);
		table.setShowGrid(true);
		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	}

	private void addTable() {
		tablePanel = new JPanel(new MigLayout(MIG.get(MIG.FILLX, MIG.INS0, MIG.WRAP)));
		tablePanel.setBorder(BorderFactory.createEtchedBorder());
		tablePanel.add(new JLabel(I18N.getColonMsg("options")), MIG.SPAN);
		JScrollPane sc = new JScrollPane(table);
		sc.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize()/*new Dimension(2048, 2048)*/);
		tablePanel.add(sc, MIG.get(MIG.SPAN, MIG.GROWX));
		JButton btAdd = new JButton(I18N.getMsg("assistant.table.add"));
		btAdd.addActionListener(e -> optionAdd());
		tablePanel.add(btAdd, MIG.get(MIG.SPLIT2, MIG.RIGHT));
		JButton btRemove = new JButton(I18N.getMsg("assistant.table.remove"));
		btRemove.addActionListener(e -> optionRemove());
		tablePanel.add(btRemove, MIG.RIGHT);
		add(tablePanel, MIG.get(MIG.SPAN, MIG.GROW));
	}

	private void setDataList() {
		//LOG.trace(TT + ".setDataList()");
		this.setTableValues();
	}

	private void setDataCombo() {
		//LOG.trace(TT + ".setDataCombo()");
		this.setTableValues();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		setFieldVisible();
		pack();
		setLocationRelativeTo(this.getParent());
	}

	private void optionAdd() {
		//LOG.trace(TT + ".optionAdd()");
		tableModel.addRow(new Object[]{"", ""});
	}

	private void optionRemove() {
		//LOG.trace(TT + ".optionRemove()");
		int row = table.getSelectedRow();
		if (row < 0) {
			return;
		}
		tableModel.removeRow(row);
	}

	public String getNom() {
		return tfName.getText().trim();
	}

	public String getTitre() {
		return tfTitle.getText().trim();
	}

	public String getAide() {
		return tfPrompt.getText().trim();
	}

	public String getHelp() {
		return tfHelp.getText().trim();
	}

	public String getLenght() {
		return tfLen.getText().trim();
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
		if (!isok.isEmpty()) {
			lbMsg.setText(isok);
			lbMsg.setVisible(true);
			pack();
			return;
		}
		StringBuilder buf = new StringBuilder();
		FIELD_TYPE ty = (FIELD_TYPE) cbType.getSelectedItem();
		String nom = tfName.getText().trim();
		String titre = tfTitle.getText().trim();
		String aide = tfPrompt.getText().trim().replace("\"", "'");
		String help = tfHelp.getText().trim().replace("\"", "'");
		if (node == null) {
			node = xml.createNode(tabNode, ty.toString(), nom, titre, aide);
		} else {
			xml.setAttribute(node, L_TITLE, titre);
			xml.setAttribute(node, L_PROMPT, aide);
		}
		buf.append("<").append(ty.toString());
		buf.append(" name=\"").append(nom).append("\"");
		buf.append(" title=\"").append(titre).append("\"");
		buf.append(" prompt=\"").append(aide).append("\"");
		switch (ty) {
			case TEXTFIELD:
				String len = tfLen.getText();
				if (!len.isEmpty() && len.chars().allMatch(Character::isDigit)) {
					xml.setAttribute(node, "len", len);
					buf.append(" len=").append(len);
				}
				buf.append("/>\n");
				break;
			case TEXTAREA:
				buf.append("/>\n");
				break;
			case LISTBOX:
			case COMBOBOX:
				xml.setAttribute(node, "help", help);
				buf.append(" help=\"").append(help).append("\"");
				xml.removeAllChild(node);
				String attr = getToElement();
				if (hasTablePrompt()) {
					buf.append(">\n");
					buf.append(getElems());
					buf.append("</").append(ty.toString()).append(">\n");
				} else {
					xml.setAttribute(node, "elements", attr);
					buf.append(" elements=\"").append(attr).append(">\n");
				}
				break;
			default:
				break;
		}
		//LOG.trace(buf.toString());
		((AppAssistant) this.getParent()).setModified(true);
		dispose();
	}

	private boolean hasTablePrompt() {
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			if (!((String) tableModel.getValueAt(i, 1)).isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private String getElems() {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			if (((String) tableModel.getValueAt(i, 0)).isEmpty()) {
				continue;
			}
			String name = String.valueOf(i + 1);
			String titre = (String) tableModel.getValueAt(i, 0);
			String prompt = (String) tableModel.getValueAt(i, 1);
			xml.createNode(node, "elem", name, titre, prompt);
			StringBuilder b = new StringBuilder("<elem ");
			b.append("name=\"").append(name).append("\" ");
			b.append("title=\"").append(titre).append("\" ");
			b.append("prompt=\"").append(prompt).append("\"/>\n");
			buf.append(b.toString());
		}
		return buf.toString();
	}

	private String getToElement() {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			if (i > 0) {
				buf.append(";");
			}
			buf.append((String) tableModel.getValueAt(i, 0));
		}
		return buf.toString();
	}

	private void setTableValues() {
		//LOG.trace(TT + ".setTableValues()");
		if (table == null) {
			return;
		}
		if (node != null) {
			String s = ((Element) node).getAttribute("elements");
			if (s != null && !s.isEmpty()) {
				String sc[] = s.split(";");
				for (String v : sc) {
					tableModel.addRow(new Object[]{v, ""});
				}
			} else {
				NodeList childs = ((Element) node).getElementsByTagName("elem");
				for (int i = 0; i < childs.getLength(); i++) {
					Node ne = childs.item(i);
					Element el = (Element) ne;
					String tit = el.getAttribute(L_TITLE);
					if (tit == null) {
						tit = "";
					}
					String prop = el.getAttribute(L_PROMPT);
					if (prop == null) {
						prop = "";
					}
					tableModel.addRow(new Object[]{tit, prop});
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void setCombo(JComboBox cb) {
		cb.removeAllItems();
		cb.addItem("");
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			cb.addItem(tableModel.getValueAt(i, 0));
		}
	}

	@SuppressWarnings("unchecked")
	void setList(JList lsData) {
		lsData.removeAll();
		DefaultListModel listModel = (DefaultListModel) lsData.getModel();
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			listModel.addElement(tableModel.getValueAt(i, 0));
		}
	}

}
