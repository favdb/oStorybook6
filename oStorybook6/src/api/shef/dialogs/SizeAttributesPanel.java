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
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import api.shef.tools.LOG;
import api.shef.tools.SwingUtil;
import i18n.I18N;

/**
 * Panel for editing the size of a table cell
 *
 * @author Bob Tantlinger
 *
 */
public class SizeAttributesPanel extends HTMLAttributeEditorPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final String SWIDTH = "width", SHEIGHT = "height";
	private static final String MEASUREMENTS[] = {"percent", "pixels"};     //$NON-NLS-2$

	private JCheckBox widthCB = null;
	private JCheckBox heightCB = null;
	private JSpinner widthField = null;
	private JSpinner heightField = null;
	private JComboBox wMeasurementCombo = null;
	private JComboBox hMeasurementCombo = null;

	public SizeAttributesPanel() {
		this(new Hashtable());
	}

	public SizeAttributesPanel(Hashtable attr) {
		super(attr);
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
				wMeasurementCombo.setSelectedIndex(1);
			}
			try {
				widthField.getModel().setValue(w);
			} catch (Exception ex) {
				LOG.err("", ex);
			}
			wMeasurementCombo.setEnabled(true);
			widthField.setEnabled(true);
		} else {
			widthCB.setSelected(false);
			widthField.setEnabled(false);
			wMeasurementCombo.setEnabled(false);
		}

		if (attribs.containsKey(SHEIGHT)) {
			heightCB.setSelected(true);
			String h = attribs.get(SHEIGHT).toString();
			if (h.endsWith("%")) {
				h = h.substring(0, h.length() - 1);
			} else {
				hMeasurementCombo.setSelectedIndex(1);
			}
			try {
				heightField.getModel().setValue(h);
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
			hMeasurementCombo.setEnabled(true);
			heightField.setEnabled(true);
		} else {
			heightCB.setSelected(false);
			heightField.setEnabled(false);
			hMeasurementCombo.setEnabled(false);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void updateAttribsFromComponents() {
		if (widthCB.isSelected()) {
			String w = widthField.getModel().getValue().toString();
			if (wMeasurementCombo.getSelectedIndex() == 0) {
				w += "%";
			}
			attribs.put(SWIDTH, w);
		} else {
			attribs.remove(SWIDTH);
		}

		if (heightCB.isSelected()) {
			String h = heightField.getModel().getValue().toString();
			if (hMeasurementCombo.getSelectedIndex() == 0) {
				h += "%";
			}
			attribs.put(SHEIGHT, h);
		} else {
			attribs.remove(SHEIGHT);
		}
	}

	public void setComponentStates(Hashtable attribs) {
		if (attribs.containsKey(SWIDTH)) {
			widthCB.setSelected(true);
			String w = attribs.get(SWIDTH).toString();
			if (w.endsWith("%")) {
				w = w.substring(0, w.length() - 1);
			} else {
				wMeasurementCombo.setSelectedIndex(1);
			}
			try {
				widthField.getModel().setValue(w);
			} catch (Exception ex) {
				LOG.err("", ex);
			}
			wMeasurementCombo.setEnabled(true);
			widthField.setEnabled(true);
		} else {
			widthCB.setSelected(false);
			widthField.setEnabled(false);
			wMeasurementCombo.setEnabled(false);
		}

		if (attribs.containsKey(SHEIGHT)) {
			heightCB.setSelected(true);
			String h = attribs.get(SHEIGHT).toString();
			if (h.endsWith("%")) {
				h = h.substring(0, h.length() - 1);
			} else {
				hMeasurementCombo.setSelectedIndex(1);
			}
			try {
				heightField.getModel().setValue(h);
			} catch (Exception ex) {
				LOG.err("", ex);
			}
			hMeasurementCombo.setEnabled(true);
			heightField.setEnabled(true);
		} else {
			heightCB.setSelected(false);
			heightField.setEnabled(false);
			hMeasurementCombo.setEnabled(false);
		}
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		setLayout(new GridBagLayout());
		setSize(215, 95);
		setPreferredSize(new java.awt.Dimension(215, 95));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(null, I18N.getMsg("shef.size"),
						TitledBorder.DEFAULT_JUSTIFICATION,
						TitledBorder.DEFAULT_POSITION, null, null),
				BorderFactory.createEmptyBorder(2, 5, 2, 5)));

		add(getWidthCB(), new GBC("0,0,anchor W, ins 0 0 5 5"));
		add(getHeightCB(), new GBC("1,0,anchor W, ins 0 0 0 5"));
		add(getWidthField(), new GBC("0,1,fill N, wx 0.0, anchor W, ins 0 0 0 5, ipadx 0"));
		add(getHeightField(), new GBC("1,1,fill N, wx 0.0, anchor W, ins 0 0 0 5,ipadx 0"));
		add(getWMeasurementCombo(), new GBC("0,2,fill N, wx 1.0, anchor W, ins 0 0 5 0"));
		add(getHMeasurementCombo(), new GBC("1,2,fill N, wx 0.0, anchor W, ipadx 0"));
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
				wMeasurementCombo.setEnabled(widthCB.isSelected());
			});
		}
		return widthCB;
	}

	/**
	 * This method initializes heightCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getHeightCB() {
		if (heightCB == null) {
			heightCB = new JCheckBox();
			heightCB.setText(I18N.getMsg("shef." + SHEIGHT));
			heightCB.addItemListener((java.awt.event.ItemEvent e) -> {
				heightField.setEnabled(heightCB.isSelected());
				hMeasurementCombo.setEnabled(heightCB.isSelected());
			});
		}
		return heightCB;
	}

	/**
	 * This method initializes widthField
	 *
	 * @return javax.swing.JSpinner
	 */
	private JSpinner getWidthField() {
		if (widthField == null) {
			widthField = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
			SwingUtil.setColumns(widthField, 3);
		}
		return widthField;
	}

	/**
	 * This method initializes heightField
	 *
	 * @return javax.swing.JSpinner
	 */
	private JSpinner getHeightField() {
		if (heightField == null) {
			heightField = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
			SwingUtil.setColumns(heightField, 3);
		}
		return heightField;
	}

	/**
	 * This method initializes wMeasurementCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("unchecked")
	private JComboBox getWMeasurementCombo() {
		if (wMeasurementCombo == null) {
			wMeasurementCombo = new JComboBox(MEASUREMENTS);
		}
		return wMeasurementCombo;
	}

	/**
	 * This method initializes hMeasurementCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("unchecked")
	private JComboBox getHMeasurementCombo() {
		if (hMeasurementCombo == null) {
			hMeasurementCombo = new JComboBox(MEASUREMENTS);
		}
		return hMeasurementCombo;
	}

}
