/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import i18n.I18N;

/**
 * A panel for editing table alignment attributes
 *
 * @author Bob Tantlinger
 *
 */
public class AlignmentAttributesPanel extends HTMLAttributeEditorPanel {

	private static final String ALIGN = "align", VALIGN = "valign";
	private static final String VERT_ALIGNMENTS[] = {"top", "middle", "bottom"};
	private static final String HORIZ_ALIGNMENTS[] = {"left", "center", "right", "justify"};
	private JCheckBox vAlignCB = null;
	private JCheckBox hAlignCB = null;
	private JComboBox vLocCombo = null;
	private JComboBox hLocCombo = null;

	public AlignmentAttributesPanel() {
		this(new Hashtable());
	}

	public AlignmentAttributesPanel(Hashtable attr) {
		super(attr);
		initialize();
		updateComponentsFromAttribs();
	}

	@Override
	public void updateComponentsFromAttribs() {
		if (attribs.containsKey(ALIGN)) {
			hAlignCB.setSelected(true);
			hLocCombo.setEnabled(true);
			hLocCombo.setSelectedItem(attribs.get(ALIGN));
		} else {
			hAlignCB.setSelected(false);
			hLocCombo.setEnabled(false);
		}

		if (attribs.containsKey(VALIGN)) {
			vAlignCB.setSelected(true);
			vLocCombo.setEnabled(true);
			vLocCombo.setSelectedItem(attribs.get(VALIGN));
		} else {
			vAlignCB.setSelected(false);
			vLocCombo.setEnabled(false);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void updateAttribsFromComponents() {
		if (vAlignCB.isSelected()) {
			attribs.put(VALIGN, vLocCombo.getSelectedItem().toString());
		} else {
			attribs.remove(VALIGN);
		}
		if (hAlignCB.isSelected()) {
			attribs.put(ALIGN, hLocCombo.getSelectedItem().toString());
		} else {
			attribs.remove(ALIGN);
		}
	}

	public void setComponentStates(Hashtable attribs) {
		if (attribs.containsKey(ALIGN)) {
			hAlignCB.setSelected(true);
			hLocCombo.setEnabled(true);
			hLocCombo.setSelectedItem(attribs.get(ALIGN));
		} else {
			hAlignCB.setSelected(false);
			hLocCombo.setEnabled(false);
		}

		if (attribs.containsKey(VALIGN)) {
			vAlignCB.setSelected(true);
			vLocCombo.setEnabled(true);
			vLocCombo.setSelectedItem(attribs.get(VALIGN));
		} else {
			vAlignCB.setSelected(false);
			vLocCombo.setEnabled(false);
		}
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		setLayout(new GridBagLayout());
		setSize(185, 95);
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(I18N.getMsg("shef.content_alignment")),
				BorderFactory.createEmptyBorder(2, 5, 2, 5)));
		setPreferredSize(new java.awt.Dimension(185, 95));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());

		add(getVAlignCB(), new GBC("0,0,fill WEST, insets 0 0 5 5"));
		add(getHAlignCB(), new GBC("1,0,fill WEST, insets 0 0 0 5"));
		add(getVLocCombo(), new GBC("0,1,fill NONE, weightx 1.0,anchor WEST, insets 0 0 5 0"));
		add(getHLocCombo(), new GBC("1,1,fill NONE, weightx 1.0, anchor WEST"));
	}

	/**
	 * This method initializes vAlignCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getVAlignCB() {
		if (vAlignCB == null) {
			vAlignCB = new JCheckBox();
			vAlignCB.setText(I18N.getMsg("shef.vertical"));
			vAlignCB.addActionListener((ActionEvent e) -> {
				vLocCombo.setEnabled(vAlignCB.isSelected());
			});
		}
		return vAlignCB;
	}

	/**
	 * This method initializes hAlignCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getHAlignCB() {
		if (hAlignCB == null) {
			hAlignCB = new JCheckBox();
			hAlignCB.setText(I18N.getMsg("shef.horizontal"));
			hAlignCB.addActionListener((ActionEvent e) -> {
				hLocCombo.setEnabled(hAlignCB.isSelected());
			});
		}
		return hAlignCB;
	}

	/**
	 * This method initializes vLocCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("unchecked")
	private JComboBox getVLocCombo() {
		if (vLocCombo == null) {
			vLocCombo = new JComboBox(VERT_ALIGNMENTS);
		}
		return vLocCombo;
	}

	/**
	 * This method initializes hLocCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("unchecked")
	private JComboBox getHLocCombo() {
		if (hLocCombo == null) {
			hLocCombo = new JComboBox(HORIZ_ALIGNMENTS);
		}
		return hLocCombo;
	}

}
