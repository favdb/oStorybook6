/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import api.shef.actions.TextEditPopupManager;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class LinkAttributesPanel extends HTMLAttributeEditorPanel {

	private static final String TITLE = "title",
			SNAME = "name",
			DOWNLOAD = "download",
			//target options
			TARGET = "target",
			NEW_WIN = "New Window",
			SAME_WIN = "Same Window",
			SAME_FRAME = "Same Frame";
	private static final String TARGET_LABELS[] = {NEW_WIN, SAME_WIN, SAME_FRAME};
	private static final String TARGETS[] = {"_blank", "_top", "_self"};
	private JCheckBox nameCB = null;
	private JTextField nameField = null;
	private JCheckBox titleCB = null;
	private JTextField titleField = null;
	private JCheckBox targetCB = null;
	private JComboBox targetCombo = null;
	private JCheckBox downloadCB = null;
	private JTextField downloadField = null;
	private JPanel spacerPanel = null;

	/**
	 * This method initializes
	 *
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public LinkAttributesPanel() {
		super();
		initialize();
		updateComponentsFromAttribs();
	}

	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		//enable name
		nameCB.setEnabled(b);
		nameField.setEditable(nameCB.isSelected() && b);
		//enable title
		titleCB.setEnabled(b);
		titleField.setEditable(titleCB.isSelected() && b);
		//enable openin
		targetCombo.setEnabled(targetCB.isSelected() && b);
		targetCB.setEnabled(b);
		//enable download
		downloadCB.setEnabled(targetCB.isSelected() && b);
		downloadField.setEnabled(targetCB.isSelected() && b);
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
		// name of the link
		add(getNameCB(), new GBC("0,0, anchor W, ins 0 0 5 5"));
		add(getNameField(), new GBC("0,1,fill H, wx 1.0,ins 0 0 5 0, anchor W"));
		//title of the link
		add(getTitleCB(), new GBC("1,0,anchor W, ins 0 0 5 5"));
		add(getTitleField(), new GBC("1,1,fill H, wx 1.0, ins 0 0 5 0, anchor W"));
		//target option
		add(getTargetCB(), new GBC("2,0,anchor W, ins 0 0 5 5"));
		add(getTargetCombo(), new GBC("2,1, fill N, wx 1.0, ins 0 0 5 0, anchor W"));
		//download option
		add(getDownloadCB(), new GBC("3,0,anchor W, ins 0 0 5 5"));
		add(getDownloadField(), new GBC("3,1, fill H, wx 1.0,ins 0 0 5 0, anchor W"));
		//spacer
		add(getSpacerPanel(), new GBC("4,0, fill H, wy 1.0, wx 0.0, anchor NW, width 2"));
		//register text field components
		TextEditPopupManager.getInstance().registerJTextComponent(nameField);
		TextEditPopupManager.getInstance().registerJTextComponent(titleField);
		TextEditPopupManager.getInstance().registerJTextComponent(downloadField);
	}

	/**
	 * update components from attributes
	 */
	@Override
	public void updateComponentsFromAttribs() {
		//update name
		if (attribs.containsKey(SNAME)) {
			nameCB.setSelected(true);
			nameField.setEditable(true);
			nameField.setText(attribs.get(SNAME).toString());
		} else {
			nameCB.setSelected(false);
			nameField.setEditable(false);
		}
		//update title
		if (attribs.containsKey(TITLE)) {
			titleCB.setSelected(true);
			titleField.setEditable(true);
			titleField.setText(attribs.get(TITLE).toString());
		} else {
			titleCB.setSelected(false);
			titleField.setEditable(false);
		}
		// update target otpion
		if (attribs.containsKey(TARGET)) {
			targetCB.setSelected(true);
			String val = attribs.get(TARGET).toString();
			targetCombo.setEnabled(true);
			for (int i = 0; i < TARGETS.length; i++) {
				if (val.equals(TARGETS[i])) {
					targetCombo.setSelectedIndex(i);
					break;
				}
			}
		} else {
			targetCB.setSelected(false);
			targetCombo.setEnabled(false);
		}
		//update download
		if (attribs.containsKey(DOWNLOAD)) {
			downloadCB.setSelected(true);
			downloadField.setEditable(true);
			downloadField.setText(attribs.get(DOWNLOAD).toString());
		} else {
			downloadCB.setSelected(false);
			downloadField.setText("");
			downloadField.setEditable(false);
		}

	}

	/**
	 * *
	 * update attributes from the components
	 */
	@Override
	@SuppressWarnings({"unchecked", "unchecked"})
	public void updateAttribsFromComponents() {
		//target update
		if (targetCB.isSelected()) {
			attribs.put(TARGET, TARGETS[targetCombo.getSelectedIndex()]);
		} else {
			attribs.remove(TARGET);
		}
		//title update
		if (titleCB.isSelected()) {
			attribs.put(TITLE, titleField.getText());
		} else {
			attribs.remove(TITLE);
		}
		//name update
		if (nameCB.isSelected()) {
			attribs.put(SNAME, nameField.getText());
		} else {
			attribs.remove(SNAME);
		}
		//download update
		if (downloadCB.isSelected()) {
			attribs.put(DOWNLOAD, downloadField.getText());
		} else {
			attribs.remove(DOWNLOAD);
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
	 * This method initializes targetCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getTargetCB() {
		if (targetCB == null) {
			targetCB = new JCheckBox();
			targetCB.setText(I18N.getMsg("shef.open_in"));
			targetCB.addItemListener((java.awt.event.ItemEvent e) -> {
				targetCombo.setEnabled(targetCB.isSelected());
			});
		}
		return targetCB;
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
	 * This method initializes targetCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("unchecked")
	private JComboBox getTargetCombo() {
		if (targetCombo == null) {
			targetCombo = new JComboBox(TARGET_LABELS);
		}
		return targetCombo;
	}

	/**
	 * This method initializes downloadCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("unchecked")
	private JCheckBox getDownloadCB() {
		if (downloadCB == null) {
			downloadCB = new JCheckBox();
		}
		downloadCB.setText(I18N.getMsg("shef." + DOWNLOAD));
		downloadCB.addItemListener((java.awt.event.ItemEvent e) -> {
			downloadField.setEditable(downloadCB.isSelected());
		});
		return downloadCB;
	}

	/**
	 * This method initializes downloadField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getDownloadField() {
		if (downloadField == null) {
			downloadField = new JTextField();
		}
		return downloadField;
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
