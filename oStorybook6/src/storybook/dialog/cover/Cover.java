/*
 * Copyright (C) 2020 favdb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.dialog.cover;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import resources.icons.IconUtil;
import static resources.icons.IconUtil.resizeIcon;
import storybook.dialog.AbsDialog;
import storybook.dialog.chooser.FontChooserDlg;
import storybook.dialog.chooser.ImageChooserDlg;
import storybook.project.PropertiesDlg;
import storybook.tools.LOG;
import storybook.tools.swing.ColorUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class Cover extends AbsDialog {

	private static final String TT = "Cover.";

	// default size of a cover
	public static final int COVER_WIDTH = 380, COVER_HEIGHT = 550;
	public JPanel margins;//margins and gaps panel
	//options for texts
	private JPanel textOptions;//options
	public JCheckBox ckText, ckAuthor, ckTitle, ckSubtitle, ckFooter;
	public Color textColor = Color.decode("#C8AB37");
	private JButton btTextColor;
	private Font titleFont;
	//background image
	private final String[] templates = {// list of available templates, personnal doesn't exist
		"personnal", "antic", "classic", "modern", "contemporary"
	};
	private int currentTemplate;//index of the current selected template, 0 for no template
	private BufferedImage bgImage;//BufferedImage from template or personnal file
	private File fImage;//personnal external cover file
	//preview panel
	private CoverPreview preview;
	private JComboBox alignList;
	private JLabel lbMargin;
	private JSpinner spMargin;
	private int textMargin;

	public enum GAPS {
		TOP, TITLE, SUBTITLE, FOOTER, MARGIN;

		@Override
		public String toString() {
			if (name().equals("MARGIN")) {
				return "cover.margin";
			}
			return "cover.margin_" + name().toLowerCase();
		}
	}

	public static void generate(MainFrame m) {
		Cover dlg = new Cover(m);
		SwingUtilities.invokeLater(() -> {
			dlg.save();
		});
	}

	public static boolean show(MainFrame m) {
		Cover dlg = new Cover(m);
		dlg.setVisible(true);
		return !dlg.canceled;
	}

	public static boolean show(PropertiesDlg m) {
		Cover dlg = new Cover(m);
		dlg.setVisible(true);
		return !dlg.canceled;
	}

	public Cover(PropertiesDlg panel) {
		super(panel);
		initAll();
	}

	public Cover(MainFrame mainFrame) {
		super(mainFrame);
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.WRAP, "[][][]"));
		JPanel left = initLeft();
		JPanel right = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.INS0, MIG.WRAP1, MIG.GAP1)));
		right.add(initRight(), MIG.TOP);
		// panel for OK / Cancel buttons
		JPanel p = new JPanel(new MigLayout());
		JButton btCancel = getCancelButton();
		p.add(btCancel);
		p.add(getOkButton());
		right.add(p, MIG.get(MIG.SPAN, MIG.RIGHT));
		JPanel center = initPreview();// center depends on right

		add(left, MIG.TOP);
		add(center, MIG.TOP);
		add(right, MIG.get(MIG.GROW, MIG.TOP));
		// set Cancel button as default button
		btCancel.requestFocus();
		//this.getRootPane().setDefaultButton(btCancel);
		pack();
		this.setLocationRelativeTo(mainFrame);
		this.setModal(true);
		SwingUtilities.invokeLater(() -> {
			ckText.requestFocusInWindow();
		});
	}

	private JPanel initPreview() {
		preview = new CoverPreview(this, book);
		return preview;
	}

	/**
	 * initialize the left panel
	 *
	 * @return
	 */
	private JPanel initLeft() {
		JPanel panel = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP, MIG.GAP + " 0"), "[center]"));
		panel.setBorder(BorderFactory.createEtchedBorder());
		panel.add(new JLabel(I18N.getColonMsg("cover.background")));
		panel.add(initBgButton(0));
		panel.add(initBgButton(1));
		panel.add(initBgButton(2));
		panel.add(initBgButton(3));
		panel.add(initBgButton(4));
		JButton btSpec = new JButton(I18N.getMsg("file"));
		btSpec.setName("btSpec");
		btSpec.addActionListener(e -> {
			String path = book.project.getPath() + File.separator + "Images";
			ImageChooserDlg chooser = new ImageChooserDlg(true);
			chooser.setCurrentDirectory(new File(path));
			chooser.setUpper(path);
			int i = chooser.showOpenDialog(this);
			if (i != 0) {
				return;
			}
			fImage = chooser.getSelectedFile();
			try {
				bgImage = ImageIO.read(fImage);
			} catch (IOException ex) {
				LOG.err(TT + "createCoverImage()", ex);
			}
			currentTemplate = 0;
			refresh();
		});
		panel.add(btSpec, "center");
		return panel;
	}

	/**
	 * initialize the right panel
	 *
	 * @return
	 */
	private JPanel initRight() {
		JPanel right = new JPanel(new MigLayout(MIG.get(MIG.WRAP, MIG.HIDEMODE3), "[][]"));
		right.setBorder(BorderFactory.createEtchedBorder());
		ckText = new JCheckBox(I18N.getMsg("cover.notext"));
		ckText.setSelected(mainFrame.project.book.param.getParamExport().getEpubCoverNoText());
		ckText.addActionListener(e -> refresh());
		right.add(ckText, MIG.get(MIG.SPAN));
		textOptions = new JPanel(new MigLayout(MIG.WRAP, "[][]"));
		ckAuthor = Ui.initCheckBox(textOptions, "ckAuthor", "author", true, "", e -> refresh());
		ckTitle = Ui.initCheckBox(textOptions, "ckTitle", "title", true, "", e -> refresh());
		ckSubtitle = Ui.initCheckBox(textOptions, "ckSubtitle", "subtitle", true, "", e -> refresh());
		ckFooter = Ui.initCheckBox(textOptions, "ckMake", "cover.by", true, "", e -> refresh());
		right.add(textOptions, MIG.SPAN);
		// Paramètres des marges et tailles des textes
		margins = new JPanel(new MigLayout(MIG.WRAP, "[][]"));
		margins.setBorder(BorderFactory.createTitledBorder(I18N.getColonMsg("cover.margin")));
		initSpinner(margins, GAPS.TOP, 16, 0, COVER_HEIGHT / 3); // Espace au-dessus de l'auteur
		initSpinner(margins, GAPS.TITLE, 190, 0, COVER_HEIGHT - 32); // Espace avant le titre
		initSpinner(margins, GAPS.SUBTITLE, 60, 0, COVER_HEIGHT - 32); // Espace avant le sous-titre
		initSpinner(margins, GAPS.FOOTER, 16, 0, COVER_HEIGHT / 3); // Espace avant le footer
		right.add(margins, MIG.SPAN);
		btTextColor = initColor(right, "#C8AB37");
		JButton bt = new JButton(I18N.getMsg("font"));
		bt.addActionListener((java.awt.event.ActionEvent evt) -> {
			FontChooserDlg dlg = new FontChooserDlg(this, preview.getTitleFont());
			dlg.setLimitedSize(18, 39);
			dlg.setVisible(true);
			if (dlg.isCanceled() || dlg.getSelectedFont() == null) {
				return;
			}
			titleFont = dlg.getSelectedFont();
			preview.setTitleFont(titleFont);
		});
		right.add(bt, MIG.get(MIG.SPAN, MIG.CENTER, MIG.GROWX));
		//alignment
		right.add(new JLabel(I18N.getColonMsg("cover.align")), MIG.RIGHT);
		String aligns[] = {I18N.getMsg("align.left"), I18N.getMsg("align.center"), I18N.getMsg("align.right")};
		alignList = new JComboBox<>(aligns);
		alignList.setSelectedIndex(1);
		alignList.addItemListener(e -> {
			int val = alignList.getSelectedIndex();
			preview.setAlign(val);
			lbMargin.setVisible((val == 1 ? false : true));
			spMargin.setVisible((val == 1 ? false : true));
			pack();
		});
		right.add(alignList);
		//margin
		right.add(lbMargin = new JLabel(I18N.getColonMsg("cover.titlemargin")), MIG.RIGHT);
		SpinnerModel spinnerModel = new SpinnerNumberModel(0, 0, COVER_WIDTH, 2);
		spMargin = new JSpinner(spinnerModel);
		spMargin.setName("margin");
		spMargin.addChangeListener(e -> {
			textMargin = (int) spMargin.getValue();
			preview.setNewMargin(textMargin);
		});
		right.add(spMargin);
		lbMargin.setVisible(false);
		spMargin.setVisible(false);
		return right;
	}

	/**
	 * initialize button for the given background template
	 *
	 * @param n
	 * @return
	 */
	private JButton initBgButton(int n) {
		JButton bt = new JButton();
		if (n != 0) {
			bt.setName(templates[n]);
			ImageIcon imageIcon = new ImageIcon(loadTemplate(templates[n]));
			imageIcon = resizeIcon(imageIcon, IconUtil.getDefSize() * 3);
			bt.setIcon(imageIcon);
		} else {
			bt.setName("none");
			bt.setText(I18N.getMsg("none"));
		}
		bt.addActionListener(e -> changeTemplate(n));
		return bt;
	}

	/**
	 * refresh the cover preview
	 */
	private void refresh() {
		//LOG.trace(TT + "refresh()");
		textOptions.setVisible(!ckText.isSelected());
		bgSet();
		preview.setBgImage(bgImage);
		preview.refresh();
	}

	/**
	 * set the background image (from resource only)
	 */
	private void bgSet() {
		//LOG.trace(TT + "setImageBg()");
		String name = "";
		switch (currentTemplate) {
			case 1:
				name = "antic";
				break;
			case 2:
				name = "classic";
				break;
			case 3:
				name = "modern";
				break;
			case 4:
				name = "contemporary";
				break;
			case 5:
				break;
			default:
				break;
		}
		if (currentTemplate < 1) {
			if (fImage != null) {
				try {
					bgImage = ImageIO.read(fImage);
				} catch (IOException e) {
					LOG.err(TT + "createCoverImage()", e);
				}
			}
		} else {
			bgImage = loadTemplate(name);
		}
	}

	/**
	 * initialize the characters color
	 *
	 * @param p
	 * @param color
	 * @return
	 */
	private JButton initColor(JPanel p, String color) {
		JButton bt = new JButton(" ");
		bt.setName("btColor");
		bt.setBackground(ColorUtil.fromHexString(color));
		bt.addActionListener(e -> {
			Color newColor = JColorChooser.showDialog(this, I18N.getMsg("color.choose"), textColor);
			if (newColor != null) {
				textColor = newColor;
				bt.setBackground(newColor);
				refresh();
			}
		});
		p.add(new JLabel(I18N.getColonMsg("cover.color")));
		p.add(bt);
		return bt;
	}

	/**
	 * initialize a spinner
	 *
	 * @param p
	 * @param name
	 * @param init
	 * @param min
	 * @param max
	 * @return
	 */
	private JSpinner initSpinner(JPanel p, GAPS gap, int init, int min, int max) {
		p.add(new JLabel(I18N.getColonMsg(gap.toString())));
		SpinnerModel spinnerModel = new SpinnerNumberModel(init, min, max, 2);
		JSpinner spinner = new JSpinner(spinnerModel);
		spinner.setName(gap.toString());
		spinner.addChangeListener(e -> preview.setNewLoc(gap));
		p.add(spinner);
		return spinner;
	}

	/**
	 * get a spinner value
	 *
	 * @param gaps
	 * @return
	 */
	public int spinnerGetValue(GAPS gaps) {
		for (Component c : margins.getComponents()) {
			if (c instanceof JSpinner && ((JSpinner) c).getName().equals(gaps.toString())) {
				return (int) ((JSpinner) c).getValue();
			}
		}
		return 0;
	}

	/**
	 * save the cover image
	 */
	public void save() {
		File output = new File(book.project.getPath() + File.separator + "Images/cover.jpeg");
		preview.getCover(output);
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
				canceled = false;
				dispose();
			}
		};
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//LOG.trace(TT + "actionPerformed(e=" + e.toString() + ")");
		if (e.getSource() instanceof JButton) {
			JButton bt = (JButton) e.getSource();
			switch (bt.getName()) {
				case "btSpec":
					String path = book.project.getPath() + File.separator + "Images";
					ImageChooserDlg chooser = new ImageChooserDlg(true);
					chooser.setCurrentDirectory(new File(path));
					chooser.setUpper(path);
					int i = chooser.showOpenDialog(this);
					if (i != 0) {
						return;
					}
					fImage = chooser.getSelectedFile();
					currentTemplate = 5;
					break;
				default:
					break;
			}
			refresh();
		}
	}

	/**
	 * get a BufferedImage for the named embended template
	 *
	 * @param name
	 * @return
	 */
	public BufferedImage loadTemplate(String name) {
		BufferedImage image;
		try (InputStream inputStream = this.getClass().getResourceAsStream("cover_" + name + ".jpeg")) {
			image = ImageIO.read(inputStream);
			return image;
		} catch (Exception ex) {
			LOG.err(TT + "loadTemplate(name=" + name + ") failed", ex);
			return null;
		}
	}

	private void changeTemplate(int n) {
		currentTemplate = n;
		refresh();
	}

	public int titleGetAlign() {
		return alignList.getSelectedIndex();
	}

	public int getMargin() {
		return (int) spMargin.getValue();
	}

}
