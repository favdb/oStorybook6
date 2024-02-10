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
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import api.shef.actions.TextEditPopupManager;
import api.shef.tools.SwingUtil;
import storybook.App;
import storybook.Pref;
import storybook.tools.swing.js.JSLabel;

public class ImageAttributesPanel extends HTMLAttributeEditorPanel {

	private static final String ALT = "alt", SHEIGHT = "height", SWIDTH = "width", SRC = "src",
	   HSPACE = "hspace", VSPACE = "vspace", BORDER = "border", ALIGN = "align";

	private static final String ALIGNMENTS[] = {"top", "middle", "bottom", "left", "right"};

	private JSLabel imgUrlLB = null;
	private JCheckBox altTextCK = null;
	private JCheckBox widthCK = null;
	private JCheckBox heightCK = null;
	private JCheckBox borderCK = null;
	private JSpinner widthSP = null;
	private JSpinner heightSP = null;
	private JSpinner borderSP = null;
	private JCheckBox vSpaceCK = null;
	private JCheckBox hSpaceCK = null;
	private JCheckBox alignCK = null;
	private JSpinner vSpaceSP = null;
	private JSpinner hSpaceSP = null;
	private JComboBox alignCB = null;
	private JTextField imgUrlTF = null;
	private JTextField alternateTF = null;
	private JPanel attribPanel = null;

	private JPanel spacerPanel = null;

	/**
	 * This is the default constructor
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public ImageAttributesPanel() {
		super();
		initialize();
		updateComponentsFromAttribs();
	}

	@Override
	public void updateComponentsFromAttribs() {
		if (attribs.containsKey(SRC)) {
			imgUrlTF.setText(attribs.get(SRC).toString());
			if (attribs.get(SRC).toString().startsWith("file:")) {
				File file = new File(imgUrlTF.getText().replace("file://", ""));
				Dimension d = IconUtil.getImageDimension(file);
				if (d != null) {
					heightSP.setValue(d.height);
					widthSP.setValue(d.width);
				}
			}
		}

		if (attribs.containsKey(ALT)) {
			altTextCK.setSelected(true);
			alternateTF.setEditable(true);
			alternateTF.setText(attribs.get(ALT).toString());
		} else {
			altTextCK.setSelected(false);
			alternateTF.setEditable(false);
		}

		if (attribs.containsKey(SWIDTH)) {
			widthCK.setSelected(true);
			widthSP.setEnabled(true);
			try {
				widthSP.getModel().setValue(Integer.parseInt((String) attribs.get(SWIDTH)));
			} catch (NumberFormatException ex) {
				//LOG.err("error", ex);
			}
		} else {
			widthCK.setSelected(false);
			widthSP.setEnabled(false);
		}

		if (attribs.containsKey(SHEIGHT)) {
			heightCK.setSelected(true);
			heightSP.setEnabled(true);
			try {
				heightSP.getModel().setValue(Integer.parseInt((String) attribs.get(SHEIGHT)));
			} catch (NumberFormatException ex) {
				//LOG.err("error", ex);
			}
		} else {
			heightCK.setSelected(false);
			heightSP.setEnabled(false);
		}

		if (attribs.containsKey(HSPACE)) {
			hSpaceCK.setSelected(true);
			hSpaceSP.setEnabled(true);
			try {
				hSpaceSP.getModel().setValue(Integer.parseInt((String) attribs.get(HSPACE)));
			} catch (NumberFormatException ex) {
				//LOG.err("", ex);
			}
		} else {
			hSpaceCK.setSelected(false);
			hSpaceSP.setEnabled(false);
		}

		if (attribs.containsKey(VSPACE)) {
			vSpaceCK.setSelected(true);
			vSpaceSP.setEnabled(true);
			try {
				vSpaceSP.getModel().setValue(Integer.parseInt((String) attribs.get(VSPACE)));
			} catch (NumberFormatException ex) {
				//LOG.err("", ex);
			}
		} else {
			vSpaceCK.setSelected(false);
			vSpaceSP.setEnabled(false);
		}

		if (attribs.containsKey(BORDER)) {
			borderCK.setSelected(true);
			borderSP.setEnabled(true);
			try {
				borderSP.getModel().setValue(Integer.parseInt((String) attribs.get(BORDER)));
			} catch (NumberFormatException ex) {
				//LOG.err("", ex);
			}
		} else {
			borderCK.setSelected(false);
			borderSP.setEnabled(false);
		}

		if (attribs.containsKey(ALIGN)) {
			alignCK.setSelected(true);
			alignCB.setEnabled(true);
			alignCB.setSelectedItem(attribs.get(ALIGN));
		} else {
			alignCK.setSelected(false);
			alignCB.setEnabled(false);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void updateAttribsFromComponents() {
		attribs.put(SRC, imgUrlTF.getText());

		if (altTextCK.isSelected()) {
			attribs.put(ALT, alternateTF.getText());
		} else {
			attribs.remove(ALT);
		}

		if (widthCK.isSelected()) {
			attribs.put(SWIDTH, widthSP.getModel().getValue().toString());
		} else {
			attribs.remove(SWIDTH);
		}

		if (heightCK.isSelected()) {
			attribs.put(SHEIGHT, heightSP.getModel().getValue().toString());
		} else {
			attribs.remove(SHEIGHT);
		}

		if (vSpaceCK.isSelected()) {
			attribs.put(VSPACE, vSpaceSP.getModel().getValue().toString());
		} else {
			attribs.remove(VSPACE);
		}

		if (hSpaceCK.isSelected()) {
			attribs.put(HSPACE, hSpaceSP.getModel().getValue().toString());
		} else {
			attribs.remove(HSPACE);
		}

		if (borderCK.isSelected()) {
			attribs.put(BORDER, borderSP.getModel().getValue().toString());
		} else {
			attribs.remove(BORDER);
		}

		if (alignCK.isSelected()) {
			attribs.put(ALIGN, alignCB.getSelectedItem().toString());
		} else {
			attribs.remove(ALIGN);
		}
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		setLayout(new GridBagLayout());
		imgUrlLB = new JSLabel(I18N.getMsg("shef.image_url"));
		add(imgUrlLB, new GBC("0,0, anchor W, ins 0 0 5 5"));
		add(getImgUrlTF(), new GBC("0,1,wx 1.0, width 1, anchor W, ins 0 0 5 0"));
		JButton btFile = new JButton(IconUtil.getIconSmall(ICONS.K.F_OPEN));
		btFile.addActionListener((ActionEvent e) -> {
			String str = App.preferences.get(Pref.KEY.IMAGE_LATSDIR, ".");
			JFileChooser chooser = new JFileChooser(str);
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File file = chooser.getSelectedFile();
			if (file.exists()) {
				imgUrlTF.setText("file://" + file.getAbsolutePath());
				Dimension d = IconUtil.getImageDimension(file);
				if (d != null) {
					heightSP.setValue(d.height);
					widthSP.setValue(d.width);
				}
				App.preferences.setString(Pref.KEY.IMAGE_LATSDIR, file.getAbsolutePath());
			}
		});
		btFile.setPreferredSize(new Dimension(20, 20));
		add(btFile, new GBC("0, 2, baseline, weightx 1.0"));
		add(getAlternateCK(), new GBC("1,0,anchor W, ins 0 0 10 5"));
		add(getAlternateTF(), new GBC("1,1,fill H, wx 1.0, ins 0 0 10 0, width 1, anchor W"));
		add(getAttribPanel(), new GBC("2,0, width 2, anchor W"));
		add(getSpacerPanel(), new GBC("3,0,fill H, with 2, anchor W, wy 1.0"));

		TextEditPopupManager popupMan = TextEditPopupManager.getInstance();
		popupMan.registerJTextComponent(imgUrlTF);
		popupMan.registerJTextComponent(alternateTF);
	}

	/**
	 * This method initializes altTextCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getAlternateCK() {
		if (altTextCK == null) {
			altTextCK = new JCheckBox();
			altTextCK.setText(I18N.getMsg("shef.alt_text"));
			altTextCK.addItemListener((ItemEvent e) -> {
				alternateTF.setEditable(altTextCK.isSelected());
			});
		}
		return altTextCK;
	}

	/**
	 * This method initializes widthCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getWidthCK() {
		if (widthCK == null) {
			widthCK = new JCheckBox();
			widthCK.setText(I18N.getMsg("shef." + SWIDTH));
			widthCK.addItemListener((ItemEvent e) -> {
				widthSP.setEnabled(widthCK.isSelected());
			});
		}
		return widthCK;
	}

	/**
	 * This method initializes heightCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getHeightCK() {
		if (heightCK == null) {
			heightCK = new JCheckBox();
			heightCK.setText(I18N.getMsg("shef." + SHEIGHT));
			heightCK.addItemListener((ItemEvent e) -> {
				heightSP.setEnabled(heightCK.isSelected());
			});
		}
		return heightCK;
	}

	/**
	 * This method initializes borderCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getBorderCK() {
		if (borderCK == null) {
			borderCK = new JCheckBox();
			borderCK.setText(I18N.getMsg("shef." + BORDER));
			borderCK.addItemListener((ItemEvent e) -> {
				borderSP.setEnabled(borderCK.isSelected());
			});
		}
		return borderCK;
	}

	/**
	 * This method initializes widthField
	 *
	 * @return javax.swing.JSpinner
	 */
	private JSpinner getWidthSP() {
		if (widthSP == null) {
			widthSP = new JSpinner(new SpinnerNumberModel(1, 1, 1920, 5));
			SwingUtil.setColumns(widthSP, 3);
		}
		return widthSP;
	}

	/**
	 * This method initializes heightSP
	 *
	 * @return javax.swing.JSpinner
	 */
	private JSpinner getHeightSP() {
		if (heightSP == null) {
			heightSP = new JSpinner(new SpinnerNumberModel(1, 1, 1920, 1));
			SwingUtil.setColumns(heightSP, 3);
		}
		return heightSP;
	}

	/**
	 * This method initializes borderSP
	 *
	 * @return javax.swing.JSpinner
	 */
	private JSpinner getBorderSP() {
		if (borderSP == null) {
			borderSP = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
			SwingUtil.setColumns(borderSP, 3);
		}
		return borderSP;
	}

	/**
	 * This method initializes vSpaceCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getVSpaceCK() {
		if (vSpaceCK == null) {
			vSpaceCK = new JCheckBox();
			vSpaceCK.setText(I18N.getMsg("shef." + VSPACE));
			vSpaceCK.addItemListener((ItemEvent e) -> {
				vSpaceSP.setEnabled(vSpaceCK.isSelected());
			});
		}
		return vSpaceCK;
	}

	/**
	 * This method initializes hSpaceCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getHSpaceCK() {
		if (hSpaceCK == null) {
			hSpaceCK = new JCheckBox();
			hSpaceCK.setText(I18N.getMsg("shef." + HSPACE));
			hSpaceCK.addItemListener((ItemEvent e) -> {
				hSpaceSP.setEnabled(hSpaceCK.isSelected());
			});
		}
		return hSpaceCK;
	}

	/**
	 * This method initializes alignCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getAlignCK() {
		if (alignCK == null) {
			alignCK = new JCheckBox();
			alignCK.setText(I18N.getMsg("shef." + ALIGN));
			alignCK.addItemListener((ItemEvent e) -> {
				alignCB.setEnabled(alignCK.isSelected());
			});
		}
		return alignCK;
	}

	/**
	 * This method initializes vSpaceField
	 *
	 * @return javax.swing.JSpinner
	 */
	private JSpinner getVSpaceField() {
		if (vSpaceSP == null) {
			vSpaceSP = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
			SwingUtil.setColumns(vSpaceSP, 3);
		}
		return vSpaceSP;
	}

	/**
	 * This method initializes hSpaceField
	 *
	 * @return javax.swing.JSpinner
	 */
	private JSpinner getHSpaceField() {
		if (hSpaceSP == null) {
			hSpaceSP = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
			SwingUtil.setColumns(hSpaceSP, 3);
		}
		return hSpaceSP;
	}

	/**
	 * This method initializes alignCB
	 *
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("unchecked")
	private JComboBox getAlignCombo() {
		if (alignCB == null) {
			alignCB = new JComboBox(ALIGNMENTS);
		}
		return alignCB;
	}

	/**
	 * This method initializes imgUrlField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getImgUrlTF() {
		if (imgUrlTF == null) {
			imgUrlTF = new JTextField();
		}
		imgUrlTF.setColumns(32);
		return imgUrlTF;
	}

	/**
	 * This method initializes altTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getAlternateTF() {
		if (alternateTF == null) {
			alternateTF = new JTextField();
		}
		return alternateTF;
	}

	/**
	 * This method initializes attribPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getAttribPanel() {
		if (attribPanel == null) {
			attribPanel = new JPanel();
			attribPanel.setLayout(new GridBagLayout());

			attribPanel.add(getWidthCK(), new GBC("0,0,anchor W,insets 0 0 5 5"));
			attribPanel.add(getHeightCK(), new GBC("1,0,anchor W,ins 0 0 10 5"));
			attribPanel.add(getBorderCK(), new GBC("2,0,anchor W,ins 0 0 10 5"));
			attribPanel.add(getWidthSP(), new GBC("0,1,anchor W, ins 0 0 5 10, wx 0.0"));
			attribPanel.add(getHeightSP(), new GBC("1,1, anchor W, ins 0 0 10 10, wx 0.0"));
			attribPanel.add(getBorderSP(), new GBC("2,1,anchor W,ins 0 0 10 10,wx 0.0"));
			attribPanel.add(getVSpaceCK(), new GBC("0,2,anchor W, ins 0 0 5 5"));
			attribPanel.add(getHSpaceCK(), new GBC("1,2,anchor W,ins 0 0 10 5"));
			attribPanel.add(getAlignCK(), new GBC("2,2,anchor W, ins 0 0 10 5"));
			attribPanel.add(getVSpaceField(), new GBC("0,3,anchor W, ins 0 0 5 0,wx 0.0"));
			attribPanel.add(getHSpaceField(), new GBC("1,3,anchor W,ins 0 0 10 0, wx 0.0"));
			attribPanel.add(getAlignCombo(), new GBC("2,3,anchor W,ins 0 0 10 0, wx 1.0"));
		}
		return attribPanel;
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
