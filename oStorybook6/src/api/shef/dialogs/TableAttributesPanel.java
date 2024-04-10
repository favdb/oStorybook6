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
import java.util.Hashtable;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import api.shef.tools.LOG;
import api.shef.tools.SwingUtil;
import i18n.I18N;

public class TableAttributesPanel extends HTMLAttributeEditorPanel {

	private static final String SWIDTH = "width", SALIGN = "align", BORDER = "border",
			CELLSPACING = "cellspacing", CELLPADDING = "cellpadding", BGCOLOR = "bgcolor";

	private static final String ALIGNMENTS[] = {"left", "center", "right"};
	private static final String MEASUREMENTS[] = {"percent", "pixels"};
	private JCheckBox widthCB = null;
	private JSpinner widthField = null;
	private JComboBox widthCombo = null;
	private JCheckBox alignCB = null;
	private JCheckBox cellSpacingCB = null;
	private JSpinner cellSpacingField = null;
	private JCheckBox borderCB = null;
	private JSpinner borderField = null;
	private JCheckBox cellPaddingCB = null;
	private JSpinner cellPaddingField = null;
	private JComboBox alignCombo = null;
	private BGColorPanel bgPanel = null;
	private JPanel expansionPanel = null;

	/**
	 * This is the default constructor
	 */
	public TableAttributesPanel() {
		this(new Hashtable());
	}

	public TableAttributesPanel(Hashtable attribs) {
		super(attribs);
		initialize();
		updateComponentsFromAttribs();
	}

	@Override
	public void updateComponentsFromAttribs() {
		if (attribs.containsKey(SWIDTH)) {
			widthCB.setSelected(true);
			String w = attribs.get(SWIDTH).toString();
			if (w.endsWith("%")) {
				w = w.substring(0, w.length() - 1);
			} else {
				widthCombo.setSelectedIndex(1);
			}
			widthField.setEnabled(true);
			try {
				widthField.getModel().setValue(Integer.parseInt(w));
			} catch (NumberFormatException ex) {
				LOG.err("", ex);
			}
		} else {
			widthCB.setSelected(false);
			widthField.setEnabled(false);
			widthCombo.setEnabled(false);
		}
		if (attribs.containsKey(SALIGN)) {
			alignCB.setSelected(true);
			alignCombo.setEnabled(true);
			alignCombo.setSelectedItem(attribs.get(SALIGN));
		} else {
			alignCB.setSelected(false);
			alignCombo.setEnabled(false);
		}
		if (attribs.containsKey(BORDER)) {
			borderCB.setSelected(true);
			borderField.setEnabled(true);
			try {
				borderField.getModel().setValue(Integer.parseInt((String) attribs.get(BORDER)));
			} catch (NumberFormatException ex) {
				LOG.err("", ex);
			}
		} else {
			borderCB.setSelected(false);
			borderField.setEnabled(false);
		}
		if (attribs.containsKey(CELLPADDING)) {
			cellPaddingCB.setSelected(true);
			cellPaddingField.setEnabled(true);
			try {
				cellPaddingField.getModel().setValue(attribs.get(CELLPADDING));
			} catch (Exception ex) {
				LOG.err("", ex);
			}
		} else {
			cellPaddingCB.setSelected(false);
			cellPaddingField.setEnabled(false);
		}
		if (attribs.containsKey(CELLSPACING)) {
			cellSpacingCB.setSelected(true);
			cellSpacingField.setEnabled(true);
			try {
				cellSpacingField.getModel().setValue(attribs.get(CELLSPACING).toString());
			} catch (Exception ex) {
				LOG.err("", ex);
			}
		} else {
			cellSpacingCB.setSelected(false);
			cellSpacingField.setEnabled(false);
		}
		if (attribs.containsKey(BGCOLOR)) {
			bgPanel.setSelected(true);
			bgPanel.setColor(attribs.get(BGCOLOR).toString());
		} else {
			bgPanel.setSelected(false);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void updateAttribsFromComponents() {
		if (widthCB.isSelected()) {
			String w = widthField.getModel().getValue().toString();
			if (widthCombo.getSelectedIndex() == 0) {
				w += "%";
			}
			attribs.put(SWIDTH, w);
		} else {
			attribs.remove(SWIDTH);
		}

		if (alignCB.isSelected()) {
			attribs.put(SALIGN, alignCombo.getSelectedItem().toString());
		} else {
			attribs.remove(SALIGN);
		}

		if (borderCB.isSelected()) {
			attribs.put(BORDER,
					borderField.getModel().getValue().toString());
		} else {
			attribs.remove(BORDER);
		}

		if (cellSpacingCB.isSelected()) {
			attribs.put(CELLSPACING,
					cellSpacingField.getModel().getValue().toString());
		} else {
			attribs.remove(CELLSPACING);
		}

		if (cellPaddingCB.isSelected()) {
			attribs.put(CELLPADDING,
					cellPaddingField.getModel().getValue().toString());
		} else {
			attribs.remove(CELLPADDING);
		}

		if (bgPanel.isSelected()) {
			attribs.put(BGCOLOR, bgPanel.getColor());
		} else {
			attribs.remove(BGCOLOR);
		}
	}

	public void setComponentStates(Hashtable attribs) {
		if (attribs.containsKey(SWIDTH)) {
			widthCB.setSelected(true);
			String w = attribs.get(SWIDTH).toString();
			if (w.endsWith("%")) {
				w = w.substring(0, w.length() - 1);
			} else {
				widthCombo.setSelectedIndex(1);
			}
			try {
				widthField.getModel().setValue(w);
			} catch (Exception ex) {
				LOG.err("", ex);
			}
		} else {
			widthCB.setSelected(false);
			widthField.setEnabled(false);
			widthCombo.setEnabled(false);
		}
		if (attribs.containsKey(SALIGN)) {
			alignCB.setSelected(true);
			alignCombo.setSelectedItem(attribs.get(SALIGN));
		} else {
			alignCB.setSelected(false);
			alignCombo.setEnabled(false);
		}
		if (attribs.containsKey(BORDER)) {
			borderCB.setSelected(true);
			try {
				borderField.getModel().setValue(attribs.get(BORDER).toString());
			} catch (Exception ex) {
				LOG.err("", ex);
			}
		} else {
			borderCB.setSelected(false);
			borderField.setEnabled(false);
		}
		if (attribs.containsKey(CELLPADDING)) {
			cellPaddingCB.setSelected(true);
			try {
				cellPaddingField.getModel().setValue(attribs.get(CELLPADDING));
			} catch (Exception ex) {
				LOG.err("", ex);
			}
		} else {
			cellPaddingCB.setSelected(false);
			cellPaddingField.setEnabled(false);
		}
		if (attribs.containsKey(CELLSPACING)) {
			cellSpacingCB.setSelected(true);
			try {
				cellSpacingField.getModel().setValue(attribs.get(CELLSPACING));
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		} else {
			cellSpacingCB.setSelected(false);
			cellSpacingField.setEnabled(false);
		}
		if (attribs.containsKey(BGCOLOR)) {
			bgPanel.setSelected(true);
			bgPanel.setColor(attribs.get(BGCOLOR).toString());
		} else {
			bgPanel.setSelected(false);
		}
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		setLayout(new GridBagLayout());

		add(getWidthCB(), new GBC("0,0,anchor W, ins 0 0 10 3"));
		add(getWidthField(), new GBC("0,1,fill N, wx 0.0,anchor W, ins 0 0 10 0"));
		add(getWidthCombo(), new GBC("0,2,fill N, wx 0.0, anchor W, width 2, ins 0 0 10 0"));
		add(getAlignCB(), new GBC("1,0,anchor W, ins 0 0 2 3"));
		add(getCellSpacingCB(), new GBC("1,3,anchor W, ins 0 0 2 3"));
		add(getCellSpacingField(), new GBC("1,4,fill N, wx 1.0, anchor W, ins 0 0 2 0"));
		add(getBorderCB(), new GBC("2,0,anchor W, ins 0 0 10 3"));
		add(getBorderField(), new GBC("2,1, fill N, wx 0.0, anchor W, ins 0 0 10 15"));
		add(getCellPaddingCB(), new GBC("2,3,anchor W,ins 0 0 10 3"));
		add(getCellPaddingField(), new GBC("2,4,fill N, wx 1.0,anchor W, ins 0 0 10 0"));
		add(getAlignCombo(), new GBC("1,1,fill N, wx 0.0, width 2,anchor W, ins 0 0 5 15"));
		add(getBGPanel(), new GBC("3,0, anchor W, width 4, wy 0.0"));
		add(getExpansionPanel(), new GBC("4,0,fill H, anchor W,width 4, wx 0.0, wy 1.0"));

	}

	/**
	 * This method initializes widthCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getWidthCB() {
		if (widthCB == null) {
			widthCB = new JCheckBox();
			widthCB.setText(I18N.getMsg("shef." + SWIDTH));
			widthCB.addItemListener((java.awt.event.ItemEvent e) -> {
				widthField.setEnabled(widthCB.isSelected());
				widthCombo.setEnabled(widthCB.isSelected());
			});
		}
		return widthCB;
	}

	/**
	 * This method initializes widthField
	 *
	 * @return javax.swing.JSpinner
	 */
	private JSpinner getWidthField() {
		if (widthField == null) {
			widthField = new JSpinner(new SpinnerNumberModel(100, 1, 999, 1));
			SwingUtil.setColumns(widthField, 3);
		}
		return widthField;
	}

	/**
	 * This method initializes widthCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("unchecked")
	private JComboBox getWidthCombo() {
		if (widthCombo == null) {
			widthCombo = new JComboBox(MEASUREMENTS);
		}
		return widthCombo;
	}

	/**
	 * This method initializes alignCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getAlignCB() {
		if (alignCB == null) {
			alignCB = new JCheckBox();
			alignCB.setText(I18N.getMsg("shef." + SALIGN));
			alignCB.addItemListener((java.awt.event.ItemEvent e) -> {
				alignCombo.setEnabled(alignCB.isSelected());
			});
		}
		return alignCB;
	}

	/**
	 * This method initializes cellSpacingCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getCellSpacingCB() {
		if (cellSpacingCB == null) {
			cellSpacingCB = new JCheckBox();
			cellSpacingCB.setText(I18N.getMsg("shef." + CELLSPACING));
			cellSpacingCB.addItemListener((java.awt.event.ItemEvent e) -> {
				cellSpacingField.setEnabled(cellSpacingCB.isSelected());
			});
		}
		return cellSpacingCB;
	}

	/**
	 * This method initializes cellSpacingField
	 *
	 * @return javax.swing.JSpinner
	 */
	private JSpinner getCellSpacingField() {
		if (cellSpacingField == null) {
			cellSpacingField = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
			SwingUtil.setColumns(cellSpacingField, 3);
		}
		return cellSpacingField;
	}

	/**
	 * This method initializes borderCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getBorderCB() {
		if (borderCB == null) {
			borderCB = new JCheckBox();
			borderCB.setText(I18N.getMsg("shef." + BORDER));
			borderCB.addItemListener((java.awt.event.ItemEvent e) -> {
				borderField.setEnabled(borderCB.isSelected());
			});
		}
		return borderCB;
	}

	/**
	 * This method initializes borderField
	 *
	 * @return javax.swing.JSpinner
	 */
	private JSpinner getBorderField() {
		if (borderField == null) {
			borderField = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
			SwingUtil.setColumns(borderField, 3);
		}
		return borderField;
	}

	/**
	 * This method initializes cellPaddingCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getCellPaddingCB() {
		if (cellPaddingCB == null) {
			cellPaddingCB = new JCheckBox();
			cellPaddingCB.setText(I18N.getMsg("shef." + CELLPADDING));
			cellPaddingCB.addItemListener((java.awt.event.ItemEvent e) -> {
				cellPaddingField.setEnabled(cellPaddingCB.isSelected());
			});
		}
		return cellPaddingCB;
	}

	/**
	 * This method initializes cellPaddingField
	 *
	 * @return javax.swing.JSpinner
	 */
	private JSpinner getCellPaddingField() {
		if (cellPaddingField == null) {
			cellPaddingField = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
			SwingUtil.setColumns(cellPaddingField, 3);
		}
		return cellPaddingField;
	}

	/**
	 * This method initializes alignCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("unchecked")
	private JComboBox getAlignCombo() {
		if (alignCombo == null) {
			alignCombo = new JComboBox(ALIGNMENTS);
		}
		return alignCombo;
	}

	/**
	 * This method initializes tempPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getBGPanel() {
		if (bgPanel == null) {
			bgPanel = new BGColorPanel();

		}
		return bgPanel;
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
