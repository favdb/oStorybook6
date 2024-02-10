/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import i18n.I18N;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import api.shef.actions.TextEditPopupManager;

public class LinkPanel extends HTMLAttributeEditorPanel {

	private JPanel hlinkPanel = null;
	private JLabel urlLabel = null;
	private JLabel textLabel = null;
	private JTextField urlField = null;
	private JTextField textField = null;
	private HTMLAttributeEditorPanel linkAttrPanel = null;
	private boolean urlFieldEnabled;
	private JButton btFile;

	/**
	 * This is the default constructor
	 */
	public LinkPanel() {
		this(true);
	}

	/**
	 * @param urlFieldEnabled
	 */
	public LinkPanel(boolean urlFieldEnabled) {
		this(new Hashtable(), urlFieldEnabled);
	}

	public LinkPanel(Hashtable attr, boolean urlFieldEnabled) {
		super();
		this.urlFieldEnabled = urlFieldEnabled;
		initialize();
		setAttributes(attr);
		updateComponentsFromAttribs();
	}

	@Override
	public void updateComponentsFromAttribs() {
		linkAttrPanel.updateComponentsFromAttribs();
		if (attribs.containsKey("href")) {
			urlField.setText(attribs.get("href").toString());
		} else {
			urlField.setText("");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void updateAttribsFromComponents() {
		linkAttrPanel.updateAttribsFromComponents();
		attribs.put("href", urlField.getText());
	}

	@Override
	public void setAttributes(Map at) {
		super.setAttributes(at);
		linkAttrPanel.setAttributes(attribs);
	}

	public void setLinkText(String text) {
		textField.setText(text);
	}

	public String getLinkText() {
		return textField.getText();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new BorderLayout(5, 5));
		this.add(getHlinkPanel(), BorderLayout.NORTH);
		this.add(getLinkAttrPanel(), BorderLayout.CENTER);
		TextEditPopupManager popupMan = TextEditPopupManager.getInstance();
		popupMan.registerJTextComponent(urlField);
		popupMan.registerJTextComponent(textField);
	}

	/**
	 * This method initializes hlinkPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getHlinkPanel() {
		if (hlinkPanel == null) {
			hlinkPanel = new JPanel();
			hlinkPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(
							null, I18N.getMsg("shef.link"),
							TitledBorder.DEFAULT_JUSTIFICATION,
							TitledBorder.DEFAULT_POSITION, null, null),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			hlinkPanel.setLayout(new GridBagLayout());
			urlLabel = new JLabel(I18N.getMsg("shef.url"));
			hlinkPanel.add(urlLabel, new GBC("0,0,anchor WEST, insets 0 0 0 5"));
			hlinkPanel.add(getUrlField(), new GBC("0,1,fill HORIZONTAL, weightx 1.0"));
			btFile = new JButton(IconUtil.getIconSmall(ICONS.K.F_OPEN));
			btFile.addActionListener((ActionEvent e) -> {
				String str = "";
				JFileChooser chooser = new JFileChooser(str);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int i = chooser.showOpenDialog(null);
				if (i != JFileChooser.APPROVE_OPTION) {
					return;
				}
				File file = chooser.getSelectedFile();
				urlField.setText("file://" + file.getAbsolutePath());
			});
			hlinkPanel.add(btFile, new GBC("0,2, insets 0 0 0 0"));
			textLabel = new JLabel(I18N.getMsg("shef.text"));
			hlinkPanel.add(textLabel, new GBC("1,0,anchor WEST, insets 0 0 0 5"));
			hlinkPanel.add(getTextField(), new GBC("1,1,fill HORIZONTAL,weightx 1.0"));
		}
		return hlinkPanel;
	}

	/**
	 * This method initializes urlField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getUrlField() {
		if (urlField == null) {
			urlField = new JTextField();
			urlField.setColumns(32);
			urlField.setEditable(urlFieldEnabled);
			//urlField.setEditable(true);
		}
		return urlField;
	}

	/**
	 * This method initializes textField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getTextField() {
		if (textField == null) {
			textField = new JTextField();
		}
		return textField;
	}

	private JPanel getLinkAttrPanel() {
		if (linkAttrPanel == null) {
			linkAttrPanel = new LinkAttributesPanel();
		}

		return linkAttrPanel;
	}

}
