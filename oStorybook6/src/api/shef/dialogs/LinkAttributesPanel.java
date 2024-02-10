/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import api.shef.actions.TextEditPopupManager;
import i18n.I18N;

public class LinkAttributesPanel extends HTMLAttributeEditorPanel {

	private static final String TITLE = "title", TARGET = "target", SNAME = "name",
			NEW_WIN = "New Window";
	private static final String SAME_WIN = "Same Window";
	private static final String SAME_FRAME = "Same Frame";
	private static final String TARGET_LABELS[] = {NEW_WIN, SAME_WIN, SAME_FRAME};
	private static final String TARGETS[] = {"_blank", "_top", "_self"};
	private JCheckBox nameCB = null;
	private JCheckBox titleCB = null;
	private JCheckBox openInCB = null;
	private JTextField nameField = null;
	private JTextField titleField = null;
	private JComboBox openInCombo = null;
	private JPanel spacerPanel = null;

	/**
	 * This method initializes
	 *
	 */
	public LinkAttributesPanel() {
		super();
		initialize();
		updateComponentsFromAttribs();
	}

	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		nameCB.setEnabled(b);
		titleCB.setEnabled(b);
		openInCB.setEnabled(b);
		nameField.setEditable(nameCB.isSelected() && b);
		titleField.setEditable(titleCB.isSelected() && b);
		openInCombo.setEnabled(openInCB.isSelected() && b);
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		setLayout(new GridBagLayout());
		setSize(new Dimension(320, 118));
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(null, I18N.getMsg("shef.attributes"),
						TitledBorder.DEFAULT_JUSTIFICATION,
						TitledBorder.DEFAULT_POSITION, null, null),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		add(getNameCB(), new GBC("0,0, anchor W, ins 0 0 5 5"));
		add(getTitleCB(), new GBC("1,0,anchor W, ins 0 0 5 5"));
		add(getOpenInCB(), new GBC("2,0,anchor W, ins 0 0 5 5"));
		add(getNameField(), new GBC("0,1,fill H, wx 1.0,ins 0 0 5 0, anchor W"));
		add(getTitleField(), new GBC("1,1,fill H, wx 1.0, ins 0 0 5 0, anchor W"));
		add(getOpenInCombo(), new GBC("2,1, fill N, wx 1.0, ins 0 0 5 0, anchor W"));
		add(getSpacerPanel(), new GBC("3,0, fill H, wy 1.0, wx 0.0, anchor NW, width 2"));

		TextEditPopupManager.getInstance().registerJTextComponent(nameField);
		TextEditPopupManager.getInstance().registerJTextComponent(titleField);

	}

	@Override
	public void updateComponentsFromAttribs() {
		if (attribs.containsKey(SNAME)) {
			nameCB.setSelected(true);
			nameField.setEditable(true);
			nameField.setText(attribs.get(SNAME).toString());
		} else {
			nameCB.setSelected(false);
			nameField.setEditable(false);
		}

		if (attribs.containsKey(TITLE)) {
			titleCB.setSelected(true);
			titleField.setEditable(true);
			titleField.setText(attribs.get(TITLE).toString());
		} else {
			titleCB.setSelected(false);
			titleField.setEditable(false);
		}

		if (attribs.containsKey(TARGET)) {
			openInCB.setSelected(true);
			String val = attribs.get(TARGET).toString();
			openInCombo.setEnabled(true);
			for (int i = 0; i < TARGETS.length; i++) {
				if (val.equals(TARGETS[i])) {
					openInCombo.setSelectedIndex(i);
					break;
				}
			}
		} else {
			openInCB.setSelected(false);
			openInCombo.setEnabled(false);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void updateAttribsFromComponents() {
		if (openInCB.isSelected()) {
			attribs.put(TARGET, TARGETS[openInCombo.getSelectedIndex()]);
		} else {
			attribs.remove(TARGET);
		}

		if (titleCB.isSelected()) {
			attribs.put(TITLE, titleField.getText());
		} else {
			attribs.remove(TITLE);
		}

		if (nameCB.isSelected()) {
			attribs.put(SNAME, nameField.getText());
		} else {
			attribs.remove(SNAME);
		}
	}

	/**
	 * This method initializes nameCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getNameCB() {
		if (nameCB == null) {
			nameCB = new JCheckBox();
			nameCB.setText(I18N.getMsg("shef." + SNAME));
			nameCB.addItemListener((java.awt.event.ItemEvent e) -> {
				nameField.setEditable(nameCB.isSelected());
			});
		}
		return nameCB;
	}

	/**
	 * This method initializes titleCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getTitleCB() {
		if (titleCB == null) {
			titleCB = new JCheckBox();
			titleCB.setText(I18N.getMsg("shef." + TITLE));
			titleCB.addItemListener((java.awt.event.ItemEvent e) -> {
				titleField.setEditable(titleCB.isSelected());
			});
		}
		return titleCB;
	}

	/**
	 * This method initializes openInCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getOpenInCB() {
		if (openInCB == null) {
			openInCB = new JCheckBox();
			openInCB.setText(I18N.getMsg("shef.open_in"));
			openInCB.addItemListener((java.awt.event.ItemEvent e) -> {
				openInCombo.setEnabled(openInCB.isSelected());
			});
		}
		return openInCB;
	}

	/**
	 * This method initializes nameField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getNameField() {
		if (nameField == null) {
			nameField = new JTextField();
		}
		return nameField;
	}

	/**
	 * This method initializes titleField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getTitleField() {
		if (titleField == null) {
			titleField = new JTextField();
		}
		return titleField;
	}

	/**
	 * This method initializes openInCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("unchecked")
	private JComboBox getOpenInCombo() {
		if (openInCombo == null) {
			openInCombo = new JComboBox(TARGET_LABELS);
		}
		return openInCombo;
	}

	/**
	 * This method initializes spacerPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getSpacerPanel() {
		if (spacerPanel == null) {
			spacerPanel = new JPanel();
		}
		return spacerPanel;
	}

}
