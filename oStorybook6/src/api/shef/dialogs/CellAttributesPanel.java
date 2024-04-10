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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import api.shef.tools.LOG;
import api.shef.tools.SwingUtil;
import i18n.I18N;

/**
 * Panel pour définir/modifier les attributs d'un tableau
 *
 * @author favdb
 */
public class CellAttributesPanel extends HTMLAttributeEditorPanel {

	private static final String NOWRAP = "nowrap", COLSPAN = "colspan", ROWSPAN = "rowspan", BGCOLOR = "bgcolor";

	private AlignmentAttributesPanel alignPanel = null;
	private SizeAttributesPanel sizePanel = null;
	private JCheckBox dontWrapCB = null;
	private BGColorPanel bgColorPanel = null;
	private JPanel spanPanel = null;
	private JCheckBox colSpanCB = null;
	private JCheckBox rowSpanCB = null;
	private JSpinner colSpanField = null;
	private JSpinner rowSpanField = null;
	private JPanel expansionPanel = null;

	/**
	 * This is the default constructor
	 */
	public CellAttributesPanel() {
		this(new Hashtable());
	}

	public CellAttributesPanel(Hashtable attr) {
		super(attr);
		initialize();
		alignPanel.setAttributes(getAttributes());
		sizePanel.setAttributes(getAttributes());
		updateComponentsFromAttribs();
	}

	@Override
	public void updateComponentsFromAttribs() {
		alignPanel.updateComponentsFromAttribs();
		sizePanel.updateComponentsFromAttribs();

		if (attribs.containsKey(COLSPAN)) {
			colSpanCB.setSelected(true);
			colSpanField.setEnabled(true);
			try {
				colSpanField.getModel().setValue(attribs.get(COLSPAN));
			} catch (Exception ex) {
				LOG.err("", ex);
			}
		} else {
			colSpanCB.setSelected(false);
			colSpanField.setEnabled(false);
		}

		if (attribs.containsKey(ROWSPAN)) {
			rowSpanCB.setSelected(true);
			rowSpanField.setEnabled(true);
			try {
				rowSpanField.getModel().setValue(attribs.get(ROWSPAN));
			} catch (Exception ex) {
				LOG.err("", ex);
			}
		} else {
			rowSpanCB.setSelected(false);
			rowSpanField.setEnabled(false);
		}

		if (attribs.containsKey(BGCOLOR)) {
			bgColorPanel.setSelected(true);
			bgColorPanel.setColor(attribs.get(BGCOLOR).toString());
		} else {
			bgColorPanel.setSelected(false);
		}

		dontWrapCB.setSelected(attribs.containsKey(NOWRAP));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void updateAttribsFromComponents() {
		alignPanel.updateAttribsFromComponents();
		sizePanel.updateAttribsFromComponents();
		if (dontWrapCB.isSelected()) {
			attribs.put(NOWRAP, NOWRAP);
		} else {
			attribs.remove(NOWRAP);
		}

		if (bgColorPanel.isSelected()) {
			attribs.put(BGCOLOR, bgColorPanel.getColor());
		} else {
			attribs.remove(BGCOLOR);
		}

		if (colSpanCB.isSelected()) {
			attribs.put(COLSPAN, colSpanField.getModel().getValue().toString());
		} else {
			attribs.remove(COLSPAN);
		}

		if (rowSpanCB.isSelected()) {
			attribs.put(ROWSPAN, rowSpanField.getModel().getValue().toString());
		} else {
			attribs.remove(ROWSPAN);
		}
	}

	@Override
	public void setAttributes(Map attr) {
		alignPanel.setAttributes(attr);
		sizePanel.setAttributes(attr);
		super.setAttributes(attr);
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		setLayout(new GridBagLayout());
		setSize(420, 200);
		setPreferredSize(new Dimension(410, 200));
		GBC gbc00 = new GBC(0, 0);
		gbc00.anchor = GridBagConstraints.WEST;
		gbc00.insets = new Insets(0, 0, 5, 5);
		gbc00.gridwidth = 1;
		add(getAlignPanel(), gbc00);
		GBC gbc01 = new GBC(0, 1);
		gbc01.anchor = GridBagConstraints.WEST;
		gbc01.weightx = 1.0;
		gbc01.insets = new Insets(0, 0, 5, 0);
		add(getSizePanel(), gbc01);
		GBC gbc11 = new GBC(1, 1);
		gbc11.anchor = GridBagConstraints.SOUTHWEST;
		gbc11.insets = new java.awt.Insets(0, 0, 5, 0);
		gbc11.gridwidth = 2;
		add(getDontWrapCB(), gbc11);
		GBC gbc21 = new GBC(2, 1);
		gbc21.gridwidth = 2;
		gbc21.anchor = GridBagConstraints.NORTHWEST;
		add(getBgColorPanel(), gbc21);
		GBC gbc10 = new GBC(1, 0, GridBagConstraints.HORIZONTAL);
		gbc10.gridheight = 2;
		gbc10.anchor = GridBagConstraints.WEST;
		gbc10.insets = new Insets(0, 0, 0, 5);
		add(getSpanPanel(), gbc10);
		GBC gbc30 = new GBC(3, 0, GridBagConstraints.HORIZONTAL);
		gbc30.gridwidth = 3;
		gbc30.anchor = GridBagConstraints.WEST;
		gbc30.weighty = 1.0;
		add(getExpansionPanel(), gbc30);

	}

	/**
	 * This method initializes alignPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private AlignmentAttributesPanel getAlignPanel() {
		if (alignPanel == null) {
			alignPanel = new AlignmentAttributesPanel();
			alignPanel.setPreferredSize(new Dimension(180, 95));

		}
		return alignPanel;
	}

	/**
	 * This method initializes sizePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getSizePanel() {
		if (sizePanel == null) {
			sizePanel = new SizeAttributesPanel();
		}
		return sizePanel;
	}

	/**
	 * This method initializes dontWrapCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getDontWrapCB() {
		if (dontWrapCB == null) {
			dontWrapCB = new JCheckBox();
			dontWrapCB.setText(I18N.getMsg("shef.dont_wrap_text"));
		}
		return dontWrapCB;
	}

	/**
	 * This method initializes bgColorPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private BGColorPanel getBgColorPanel() {
		if (bgColorPanel == null) {
			bgColorPanel = new BGColorPanel();
		}
		return bgColorPanel;
	}

	/**
	 * This method initializes spanPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getSpanPanel() {
		if (spanPanel == null) {
			spanPanel = new JPanel();
			spanPanel.setLayout(new GridBagLayout());
			spanPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(null, I18N.getMsg("shef.span"),
							TitledBorder.DEFAULT_JUSTIFICATION,
							TitledBorder.DEFAULT_POSITION, null, null),
					BorderFactory.createEmptyBorder(2, 2, 2, 2)));
			GBC gbc00 = new GBC(0, 0);
			gbc00.anchor = java.awt.GridBagConstraints.WEST;
			gbc00.insets = new java.awt.Insets(0, 0, 5, 0);
			spanPanel.add(getColSpanCB(), gbc00);
			GBC gbc10 = new GBC(1, 0);
			gbc10.anchor = GridBagConstraints.WEST;
			gbc10.gridheight = 1;
			gbc10.insets = new Insets(0, 0, 0, 0);
			spanPanel.add(getRowSpanCB(), gbc10);
			GBC gbc01 = new GBC(0, 1, GridBagConstraints.NONE);
			gbc01.weightx = 0.0;
			gbc01.anchor = GridBagConstraints.WEST;
			spanPanel.add(getColSpanField(), gbc01);
			GBC gbc11 = new GBC(1, 1, GridBagConstraints.NONE);
			gbc11.weightx = 1.0;
			gbc11.anchor = GridBagConstraints.WEST;
			spanPanel.add(getRowSpanField(), gbc11);
		}
		return spanPanel;
	}

	/**
	 * This method initializes colSpanCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getColSpanCB() {
		if (colSpanCB == null) {
			colSpanCB = new JCheckBox();
			colSpanCB.setText(I18N.getMsg("shef." + COLSPAN));
			colSpanCB.setPreferredSize(new java.awt.Dimension(85, 25));
			colSpanCB.addItemListener((java.awt.event.ItemEvent e) -> {
				colSpanField.setEnabled(colSpanCB.isSelected());
			});
		}
		return colSpanCB;
	}

	/**
	 * This method initializes rowSpanCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getRowSpanCB() {
		if (rowSpanCB == null) {
			rowSpanCB = new JCheckBox();
			rowSpanCB.setText(I18N.getMsg("shef." + ROWSPAN));
			rowSpanCB.addItemListener((ItemEvent e) -> {
				rowSpanField.setEnabled(rowSpanCB.isSelected());
			});
		}
		return rowSpanCB;
	}

	/**
	 * This method initializes colSpanField
	 *
	 * @return javax.swing.JSpinner
	 */
	private JSpinner getColSpanField() {
		if (colSpanField == null) {
			colSpanField = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
			SwingUtil.setColumns(colSpanField, 3);
		}
		return colSpanField;
	}

	/**
	 * This method initializes rowSpanField
	 *
	 * @return javax.swing.JSpinner
	 */
	private JSpinner getRowSpanField() {
		if (rowSpanField == null) {
			rowSpanField = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
			SwingUtil.setColumns(rowSpanField, 3);
		}
		return rowSpanField;
	}

	/**
	 * This method initializes expansionPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getExpansionPanel() {
		if (expansionPanel == null) {
			expansionPanel = new JPanel();
		}
		return expansionPanel;
	}

}
