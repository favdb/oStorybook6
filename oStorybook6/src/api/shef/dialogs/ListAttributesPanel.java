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
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.util.Hashtable;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import api.shef.tools.SwingUtil;
import storybook.tools.swing.js.JSLabel;

public class ListAttributesPanel extends HTMLAttributeEditorPanel {

	public static final int UL_LIST = 0, OL_LIST = 1;
	private static final String UL = I18N.getMsg("shef.listunordered"),
			OL = I18N.getMsg("shef.listordered");
	private static final String LIST_TYPES[] = {UL, OL},
			OL_TYPES[] = {"1", "a", "A", "i", "I"};
	private static final String OL_TYPE_LABELS[] = {
		"1, 2, 3, ...",
		"a, b, c, ...",
		"A, B, C, ...",
		"i, ii, iii, ...",
		"I, II, III, ..."
	};
	private static final String UL_TYPES[] = {"disc", "square", "circle"};
	private static final String UL_TYPE_LABELS[] = {
		I18N.getMsg("shef.solid_circle"),
		I18N.getMsg("shef.solid_square"), I18N.getMsg("shef.open_circle")
	};
	private JSLabel typeLabel = null;
	private JComboBox typeCombo = null;
	private JComboBox styleCombo = null;
	private JSpinner startAtField = null;
	private JCheckBox styleCB = null;
	private JCheckBox startAtCB = null;

	/**
	 * This method initializes
	 *
	 */
	public ListAttributesPanel() {
		this(new Hashtable());
	}

	public ListAttributesPanel(Hashtable ht) {
		super();
		initialize();
		setAttributes(ht);
		updateComponentsFromAttribs();
	}

	public void setListType(int t) {
		typeCombo.setSelectedIndex(t);
		updateForType();
	}

	public int getListType() {
		return typeCombo.getSelectedIndex();
	}

	@SuppressWarnings("unchecked")
	private void updateForType() {
		styleCombo.removeAllItems();
		if (typeCombo.getSelectedItem().equals(UL)) {
			for (String UL_TYPE_LABELS1 : UL_TYPE_LABELS) {
				styleCombo.addItem(UL_TYPE_LABELS1);
			}
			startAtCB.setEnabled(false);
			startAtField.setEnabled(false);
		} else {
			for (String OL_TYPE_LABELS1 : OL_TYPE_LABELS) {
				styleCombo.addItem(OL_TYPE_LABELS1);
			}
			startAtCB.setEnabled(true);
			startAtField.setEnabled(startAtCB.isSelected());
		}
	}

	private int getIndexForStyle(String s) {
		if (typeCombo.getSelectedIndex() == UL_LIST) {
			for (int i = 0; i < UL_TYPES.length; i++) {
				if (UL_TYPES[i].equals(s)) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < OL_TYPES.length; i++) {
				if (OL_TYPES[i].equals(s)) {
					return i;
				}
			}
		}

		return 0;
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		typeLabel = new JSLabel();
		typeLabel.setText(I18N.getMsg("shef.list_type"));
		setLayout(new GridBagLayout());
		setSize(new java.awt.Dimension(234, 159));
		setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(typeLabel, new GBC("0,0,anchor W, ins 0 0 5 5"));
		add(getTypeCombo(), new GBC("0,1,fill H, wx 1.0,anchor W, ins 0 0 5 0"));
		add(getStyleCombo(), new GBC("1,1,fill H, wx 1.0,anchor W, ins 0 0 5 0"));
		add(getStartAtField(), new GBC("2,1,fill N, wx 1.0, anchor W"));
		add(getStyleCB(), new GBC("1,0,anchor W, ins 0 0 5 5"));
		add(getStartAtCB(), new GBC("2, 0, anchor W"));
	}

	@Override
	public void updateComponentsFromAttribs() {
		//updateForType();
		if (attribs.containsKey("type")) {
			styleCB.setSelected(true);
			styleCombo.setEnabled(true);
			int i = getIndexForStyle(attribs.get("type").toString());
			styleCombo.setSelectedIndex(i);
		} else {
			styleCB.setSelected(false);
			styleCombo.setEnabled(false);
		}
		if (attribs.containsKey("start")) {
			startAtCB.setSelected(true);
			startAtField.setEnabled(true);
			try {
				int n = Integer.parseInt(attribs.get("start").toString());
				startAtField.getModel().setValue(n);
			} catch (NumberFormatException ex) {
			}
		} else {
			startAtCB.setSelected(false);
			startAtField.setEnabled(false);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void updateAttribsFromComponents() {
		if (styleCB.isSelected()) {
			if (typeCombo.getSelectedIndex() == UL_LIST) {
				attribs.put("type", UL_TYPES[styleCombo.getSelectedIndex()]);
			} else {
				attribs.put("type", OL_TYPES[styleCombo.getSelectedIndex()]);
			}
		} else {
			attribs.remove("type");
		}

		if (startAtCB.isSelected()) {
			attribs.put("start", startAtField.getModel().getValue().toString());
		} else {
			attribs.remove("start");
		}
	}

	/**
	 * This method initializes typeCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("unchecked")
	private JComboBox getTypeCombo() {
		if (typeCombo == null) {
			typeCombo = new JComboBox(LIST_TYPES);
			typeCombo.addItemListener((ItemEvent e) -> {
				updateForType();
			});
		}
		return typeCombo;
	}

	/**
	 * This method initializes styleCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("unchecked")
	private JComboBox getStyleCombo() {
		if (styleCombo == null) {
			styleCombo = new JComboBox(UL_TYPE_LABELS);
		}
		return styleCombo;
	}

	/**
	 * This method initializes startAtField
	 *
	 * @return javax.swing.JSpinner
	 */
	private JSpinner getStartAtField() {
		if (startAtField == null) {
			startAtField = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
			SwingUtil.setColumns(startAtField, 3);
		}
		return startAtField;
	}

	/**
	 * This method initializes styleCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getStyleCB() {
		if (styleCB == null) {
			styleCB = new JCheckBox();
			styleCB.setText(I18N.getMsg("shef.style"));
			styleCB.addItemListener((ItemEvent e) -> {
				styleCombo.setEnabled(styleCB.isSelected());
			});
		}
		return styleCB;
	}

	/**
	 * This method initializes startAtCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getStartAtCB() {
		if (startAtCB == null) {
			startAtCB = new JCheckBox();
			startAtCB.setText(I18N.getMsg("shef.start_at"));
			startAtCB.addItemListener((ItemEvent e) -> {
				startAtField.setEnabled(startAtCB.isSelected());
			});
		}
		return startAtCB;
	}

}
