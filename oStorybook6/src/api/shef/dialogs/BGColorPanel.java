/*
 * Copyright (C) oStorybook Team
 *
 * This program is free software, see <http://www.gnu.org/licenses/>.
 *
 * Parts of this code are from the NovaWorx project. Other parts are
 * from the SHEF project developed and published by Bob Tantlinger.
 */
package api.shef.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import api.shef.tools.HtmlUtils;
import i18n.I18N;
import resources.icons.ICONS;
import resources.icons.IconUtil;

/**
 * Classe pour sélectionner une couleur
 *
 * @author favdb
 */
public class BGColorPanel extends JPanel {

	private JCheckBox bgColorCB = null;
	private JPanel colorPanel = null;
	private JButton colorButton = null;
	private Color selColor = Color.WHITE;

	/**
	 * This is the default constructor
	 */
	public BGColorPanel() {
		super();
		initialize();
	}

	public void setSelected(boolean sel) {
		bgColorCB.setSelected(sel);
		colorButton.setEnabled(sel);
	}

	public boolean isSelected() {
		return bgColorCB.isSelected();
	}

	public String getColor() {
		return HtmlUtils.colorToHex(selColor);
	}

	public void setColor(String hexColor) {
		selColor = HtmlUtils.stringToColor(hexColor);
		colorPanel.setBackground(selColor);
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		setLayout(new GridBagLayout());
		setSize(175, 30);
		setPreferredSize(new Dimension(175, 30));
		setMinimumSize(getPreferredSize());
		setMaximumSize(getPreferredSize());

		add(getBgColorCB(), new GBC("0, 0, anchor WEST, insets 0 0 0 5"));
		add(getColorPanel(), new GBC("0, 1, anchor WEST, insets 0 0 0 5"));
		add(getColorButton(), new GBC("0, 2, anchor WEST, weightx 1.0"));

		colorButton.setEnabled(bgColorCB.isSelected());
	}

	/**
	 * This method initializes bgColorCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getBgColorCB() {
		if (bgColorCB == null) {
			bgColorCB = new JCheckBox();
			bgColorCB.setText(I18N.getMsg("shef.background"));
			bgColorCB.addItemListener((ItemEvent e) -> {
				colorButton.setEnabled(bgColorCB.isSelected());
				if (bgColorCB.isSelected()) {
					colorPanel.setBackground(selColor);
				} else {
					colorPanel.setBackground(getBackground());
				}
			});
		}
		return bgColorCB;
	}

	/**
	 * This method initializes colorPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getColorPanel() {
		if (colorPanel == null) {
			colorPanel = new JPanel();
			colorPanel.setPreferredSize(new Dimension(50, 20));
			colorPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		}
		return colorPanel;
	}

	/**
	 * This method initializes colorButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getColorButton() {
		if (colorButton == null) {
			colorButton = new JButton();
			colorButton.setIcon(IconUtil.getIconSmall(ICONS.K.COLOR));
			colorButton.setPreferredSize(new Dimension(20, 20));
			colorButton.addActionListener((ActionEvent e) -> {
				Color c = JColorChooser.showDialog(BGColorPanel.this, I18N.getMsg("shef.color"), selColor);
				if (c != null) {
					selColor = c;
					colorPanel.setBackground(c);
					colorPanel.setToolTipText(HtmlUtils.colorToHex(c));
				}
			});
		}
		return colorButton;
	}

}
