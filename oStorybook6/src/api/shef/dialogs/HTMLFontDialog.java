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
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import resources.icons.ICONS;
import resources.icons.IconUtil;

/**
 * Font selection dialog
 *
 * @author favdb
 */
public class HTMLFontDialog extends HTMLOptionDialog {

	private static Icon icon = IconUtil.getIconSmall(ICONS.K.FONTSIZE);
	private static String title = I18N.getMsg("shef.font");
	private static String desc = I18N.getMsg("shef.font_desc");
	private static final Integer SIZES[] = {8, 10, 12, 14, 18, 24, 36};
	private JPanel jContentPane = null;
	private JLabel fontLabel = null;
	private JComboBox fontCombo = null;
	private JComboBox sizeCombo = null;
	private JPanel stylePanel = null;
	private JCheckBox boldCB = null;
	private JCheckBox italicCB = null;
	private JCheckBox ulCB = null;
	private JPanel previewPanel = null;
	private JLabel previewLabel = null;
	private JPanel spacerPanel = null;
	private String text = "";

	/**
	 * Constructeur si le parent est un Frame
	 *
	 * @param parent: frame parent
	 * @param text : texte sur lequel la font est à appliquer
	 */
	public HTMLFontDialog(Frame parent, String text) {
		super(parent, title, desc, icon);
		initialize(text);
	}

	/**
	 * Constructeur si le parent est un Dialog
	 *
	 * @param parent: dialog parent
	 * @param text : texte sur lequel la font est à appliquer
	 */
	public HTMLFontDialog(Dialog parent, String text) {
		super(parent, title, desc, icon);
		initialize(text);
	}

	/**
	 * le texte est-il en gras?
	 *
	 * @return
	 */
	public boolean isBold() {
		return boldCB.isSelected();
	}

	/**
	 * le texte est-il en italique
	 *
	 * @return : true si oui
	 */
	public boolean isItalic() {
		return italicCB.isSelected();
	}

	/**
	 * le texte est-il en souligné
	 *
	 * @return : true si oui
	 */
	public boolean isUnderline() {
		return ulCB.isSelected();
	}

	/**
	 * valeur pour le gras
	 *
	 * @param b : true si oui
	 */
	public void setBold(boolean b) {
		boldCB.setSelected(b);
		updatePreview();
	}

	/**
	 * valeur pour l'italique
	 *
	 * @param b : true si oui
	 */
	public void setItalic(boolean b) {
		italicCB.setSelected(b);
		updatePreview();
	}

	/**
	 * valeur pour le souligné
	 *
	 * @param b : true si oui
	 */
	public void setUnderline(boolean b) {
		ulCB.setSelected(b);
		updatePreview();
	}

	/**
	 * changer le nom de la font
	 *
	 * @param fn
	 */
	public void setFontName(String fn) {
		fontCombo.setSelectedItem(fn);
		updatePreview();
	}

	/**
	 * obtenir le nom de la font
	 *
	 * @return : le nom de la font
	 */
	public String getFontName() {
		return fontCombo.getSelectedItem().toString();
	}

	/**
	 * obtenir la taille de la font
	 *
	 * @return : la taille
	 */
	public int getFontSize() {
		Integer i = (Integer) sizeCombo.getSelectedItem();
		return i;
	}

	/**
	 * changer la taille de la font
	 */
	public void setFontSize(int size) {
		sizeCombo.setSelectedItem(size);
		updatePreview();
	}

	/**
	 * obtenir les caractéristiques de la font au format HTML
	 *
	 * @return : caractéristiques
	 */
	@Override
	public String getHTML() {
		String html = "<font ";
		html += "name=\"" + fontCombo.getSelectedItem() + "\" ";
		html += "size=\"" + (sizeCombo.getSelectedIndex() + 1) + "\">";
		if (boldCB.isSelected()) {
			html += "<b>";
		}
		if (italicCB.isSelected()) {
			html += "<i>";
		}
		if (ulCB.isSelected()) {
			html += "<u>";
		}
		html += text;
		if (boldCB.isSelected()) {
			html += "</b>";
		}
		if (italicCB.isSelected()) {
			html += "</i>";
		}
		if (ulCB.isSelected()) {
			html += "</u>";
		}
		html += "</font>";
		return html;
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize(String text) {
		setContentPane(getJContentPane());
		pack();
		setSize(480, getHeight());
		//setResizable(false);
		this.text = text;
	}

	/**
	 * mise à jour de l'aperçu
	 *
	 */
	private void updatePreview() {
		int style = Font.PLAIN;
		if (boldCB.isSelected()) {
			style += Font.BOLD;
		}
		if (italicCB.isSelected()) {
			style += Font.ITALIC;
		}
		if (ulCB.isSelected()) {
			previewLabel.setBorder(
					BorderFactory.createMatteBorder(
							0, 0, 1, 0, previewLabel.getForeground()));
		} else {
			previewLabel.setBorder(null);
		}
		String font = fontCombo.getSelectedItem().toString();
		Integer size = SIZES[sizeCombo.getSelectedIndex()];
		Font f = new Font(font, style, size);
		previewLabel.setFont(f);
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			fontLabel = new JLabel();
			fontLabel.setText(I18N.getMsg("shef.font"));
			jContentPane = new JPanel(new GridBagLayout());
			jContentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			jContentPane.add(fontLabel,
					new GBC("0,0, insets 0 0 0 5"));
			jContentPane.add(getFontCombo(),
					new GBC("0,1, fill HORIZONTAL, anchor WEST, insets 0 0 0 5"));
			jContentPane.add(getSizeCombo(),
					new GBC("0,2, fill NONE, anchor WEST, weightx 1.0"));
			jContentPane.add(getStylePanel(),
					new GBC("1,0, fill HORIZONTAL, gridwith 3, anchor WEST, insets 5 0 0 0"));

			sizeCombo.setSelectedItem(previewLabel.getFont().getSize());
		}
		return jContentPane;
	}

	/**
	 * This method initializes fontCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("unchecked")
	private JComboBox getFontCombo() {
		if (fontCombo == null) {
			GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
			String envfonts[] = gEnv.getAvailableFontFamilyNames();
			List<String> fonts = new ArrayList<>();
			fonts.add("Default");
			fonts.add("serif");
			fonts.add("sans-serif");
			fonts.add("monospaced");
			fonts.addAll(Arrays.asList(envfonts));
			fontCombo = new JComboBox(fonts.toArray());
			fontCombo.addItemListener((ItemEvent e) -> {
				updatePreview();
			});
		}
		return fontCombo;
	}

	/**
	 * This method initializes sizeCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("unchecked")
	private JComboBox getSizeCombo() {
		if (sizeCombo == null) {
			sizeCombo = new JComboBox(SIZES);
			sizeCombo.setSelectedItem(12);
			sizeCombo.addItemListener((ItemEvent e) -> {
				updatePreview();
			});
		}
		return sizeCombo;
	}

	/**
	 * This method initializes stylePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getStylePanel() {
		if (stylePanel == null) {
			stylePanel = new JPanel();
			stylePanel.setLayout(new GridBagLayout());
			stylePanel.add(getCkBold(),
					new GBC("0, 0, anchor W, ins 5 0 0 5"));
			stylePanel.add(getCkItalic(),
					new GBC("1, 0, anchor W, ins 0 0 0 5"));
			stylePanel.add(getPreviewPanel(),
					new GBC("1, 1, fill BOTH, width 1, height 4, wx 1.0, wy 1.0, anchor NW"));
			stylePanel.add(getCkUl(),
					new GBC("2,0, anchor W, ins 0 0 0 5, wy 0.0"));
			stylePanel.add(getSpacerPanel(),
					new GBC("3,0, fill B, anchor NW, wy 1.0"));
		}
		return stylePanel;
	}

	/**
	 * This method initializes boldCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getCkBold() {
		if (boldCB == null) {
			boldCB = new JCheckBox();
			boldCB.setText(I18N.getMsg("shef.bold"));
			boldCB.addItemListener((ItemEvent e) -> {
				updatePreview();
			});
		}
		return boldCB;
	}

	/**
	 * This method initializes italicCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getCkItalic() {
		if (italicCB == null) {
			italicCB = new JCheckBox();
			italicCB.setText(I18N.getMsg("shef.italic"));
			italicCB.addItemListener((ItemEvent e) -> {
				updatePreview();
			});
		}
		return italicCB;
	}

	/**
	 * This method initializes ulCB
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getCkUl() {
		if (ulCB == null) {
			ulCB = new JCheckBox();
			ulCB.setText(I18N.getMsg("shef.underline"));
			ulCB.addItemListener((ItemEvent e) -> {
				updatePreview();
			});
		}
		return ulCB;
	}

	/**
	 * This method initializes previewPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPreviewPanel() {
		if (previewPanel == null) {
			previewLabel = new JLabel();
			previewLabel.setText("AaBbYyZz");
			JPanel spacer = new JPanel(new FlowLayout(FlowLayout.LEFT));
			spacer.setBackground(Color.WHITE);
			spacer.add(previewLabel);
			previewPanel = new JPanel();
			previewPanel.setLayout(new BorderLayout());
			previewPanel.setBorder(BorderFactory.createCompoundBorder(
					null, BorderFactory.createCompoundBorder(
							BorderFactory.createTitledBorder(
									null, I18N.getMsg("shef.preview"),
									TitledBorder.DEFAULT_JUSTIFICATION,
									TitledBorder.DEFAULT_POSITION, null, null),
							BorderFactory.createCompoundBorder(
									BorderFactory.createEmptyBorder(5, 5, 5, 5),
									BorderFactory.createBevelBorder(BevelBorder.LOWERED))
					)));
			previewPanel.setPreferredSize(new Dimension(90, 100));
			previewPanel.setMaximumSize(previewPanel.getPreferredSize());
			previewPanel.setMinimumSize(previewPanel.getPreferredSize());
			previewPanel.add(spacer, null);
		}
		return previewPanel;
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
