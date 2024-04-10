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
package storybook.project;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTMLEditorKit;
import resources.MainResources;
import resources.icons.IconUtil;
import storybook.Const;
import storybook.dialog.AbsDialog;
import storybook.dialog.chooser.ImageChooserDlg;
import storybook.tools.LOG;
import storybook.tools.file.EnvUtil;
import storybook.tools.html.Html;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSLabel;
import storybook.ui.MIG;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class PropEpubCover extends AbsDialog implements ChangeListener {

	private static final String TT = "EpubCover";

	private final String[] coverName = {"personnal", "antic", "classic", "modern", "contemporary"};
	private String fImage = "";
	private JPanel listCover;
	private JEditorPane edCover;
	private int current = 0;
	private JButton btColor;
	private JPanel right;
	private JCheckBox ckText;
	private JPanel margins;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public PropEpubCover(MainFrame mainFrame) {
		super(mainFrame);
		initAll();
	}

	public PropEpubCover(PropertiesDlg panel) {
		super(panel);
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.WRAP, "[][][]"));
		listCover = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP), "[]"));
		initCoverButton(1);
		initCoverButton(2);
		initCoverButton(3);
		initCoverButton(4);
		JButton btSpec = new JButton(I18N.getMsg("file"));
		btSpec.setName("btSpec");
		btSpec.addActionListener(this);
		listCover.add(btSpec, "center");

		JScrollPane scroller = new JScrollPane(listCover);
		add(scroller, "top");
		JPanel pCover = new JPanel(new MigLayout(MIG.WRAP1));
		edCover = new JEditorPane();
		edCover.setContentType(Html.TYPE);
		edCover.setEditable(false);
		HTMLEditorKit kit = new HTMLEditorKit();
		edCover.setEditorKit(kit);
		Dimension dim = new Dimension(380, 550);
		edCover.setPreferredSize(dim);
		edCover.setMinimumSize(dim);
		edCover.setMaximumSize(dim);
		edCover.setBorder(BorderFactory.createEtchedBorder());
		pCover.add(edCover);
		pCover.add(new JLabel(I18N.getMsg("epub.cover.info")), MIG.CENTER);
		add(pCover);

		right = new JPanel(new MigLayout(MIG.WRAP, "[][]"));
		right.setBorder(BorderFactory.createEtchedBorder());
		right.add(new JSLabel(I18N.getMsg("epub.cover.margin")), "span");

		ckText = new JCheckBox(I18N.getMsg("epub.cover_notext"));
		ckText.setSelected(mainFrame.project.book.param.getParamExport().getEpubCoverNoText());
		ckText.addActionListener(e -> refreshCover());
		right.add(ckText, MIG.get(MIG.SPAN));

		margins = new JPanel(new MigLayout(MIG.WRAP, "[][]"));
		initSpinner(margins, "margin_top", 10, 6, 128);
		initSpinner(margins, "margin_title_before", 64, 6, 128);
		initSpinner(margins, "margin_title_after", 64, 6, 128);
		margins.add(new JSLabel(I18N.getMsg("epub.cover.size")), "span");
		initSpinner(margins, "size_author", 12, 6, 128);
		initSpinner(margins, "size_title", 24, 6, 128);
		initSpinner(margins, "size_footer", 10, 6, 128);
		btColor = initColor(margins, "#C8AB37");
		right.add(margins, MIG.SPAN);

		add(right, "top");

		JPanel p = new JPanel(new MigLayout());
		p.add(getCancelButton());
		p.add(getOkButton());
		add(p, "span, right");
		refreshCover();
		pack();
		this.setLocationRelativeTo(mainFrame);
		this.setModal(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//LOG.printInfos("PropEpubCover.actionPerformed(e=" + e.toString() + ")");
		if (e.getSource() instanceof JButton) {
			JButton bt = (JButton) e.getSource();
			switch (bt.getName()) {
				case "btSpec":
					String path = EnvUtil.getHomeDir().getAbsolutePath();
					ImageChooserDlg chooser = new ImageChooserDlg(true);
					if (!fImage.isEmpty()) {
						chooser.setSelectedFile(new File(fImage));
					} else {
						chooser.setCurrentDirectory(new File(path));
					}
					chooser.setUpper(path);
					int i = chooser.showOpenDialog(this);
					if (i != 0) {
						return;
					}
					fImage = "file://" + chooser.getSelectedFile().getAbsolutePath();
					//LOG.printInfos("selected image=" + fImage);
					break;
				case "btColor":
					Color color = JColorChooser.showDialog(this, I18N.getMsg("color"), btColor.getBackground());
					if (color != null) {
						btColor.setBackground(color);
					}
					break;
				case "antic":
					current = 1;
					break;
				case "classic":
					current = 2;
					break;
				case "modern":
					current = 3;
					break;
				case "contemporary":
					current = 4;
					break;
				default:
					break;
			}
			refreshCover();
		}
	}

	public static BufferedImage toBufferedImage(Image img) {
		if (img == null) {
			LOG.err("toBufferedImage: img is null");
			return (null);
		}
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D gr = bimage.createGraphics();
		gr.drawImage(img, 0, 0, null);
		gr.dispose();
		return bimage;
	}

	private JButton initCoverButton(int n) {
		JButton bt = new JButton();
		bt.setName(coverName[n]);
		bt.setIcon(IconUtil.getJpegIcon("cover", "cover_" + coverName[n]));
		bt.addActionListener(this);
		listCover.add(bt);
		return (bt);
	}

	private JSpinner initSpinner(JPanel p, String name, int init, int min, int max) {
		SpinnerModel spinnerModel = new SpinnerNumberModel(init, min, max, 2);
		JSpinner spinner = new JSpinner(spinnerModel);
		spinner.setName(name);
		spinner.addChangeListener(this);
		p.add(new JSLabel(I18N.getColonMsg("epub.cover." + name)));
		p.add(spinner);
		return (spinner);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		refreshCover();
	}

	private JButton initColor(JPanel p, String color) {
		JButton bt = new JButton(" ");
		bt.setName("btColor");
		bt.setBackground(ColorUtil.fromHexString(color));
		bt.addActionListener(this);
		p.add(new JSLabel(I18N.getColonMsg("color")));
		p.add(bt, "growx");
		return (bt);
	}

	private Integer getSpinnerValue(String name) {
		for (Component c : margins.getComponents()) {
			if (!(c instanceof JSpinner)) {
				continue;
			}
			JSpinner s = (JSpinner) c;
			if (s.getName().equals(name)) {
				return ((int) s.getValue());
			}
		}
		return (0);
	}

	public void save() {
		mainFrame.project.book.param.getParamExport().setEpubCover(ckText.isSelected());
		Rectangle r = new Rectangle();
		r.x = 0;
		r.y = 0;
		r.width = 380;
		r.height = 550;
		int type = BufferedImage.TYPE_INT_RGB;
		BufferedImage image = new BufferedImage(r.width, r.height, type);
		Graphics2D g2 = image.createGraphics();
		g2.translate(-r.x, -r.y);
		edCover.paint(g2);
		g2.dispose();
		String dir = mainFrame.getProject().getPath() + File.separator + "Images";
		String format = "jpg";
		File file = new File(dir);
		if (!file.exists()) {
			file.mkdir();
		}
		file = new File(dir + File.separator + "cover.jpeg");
		if (file.exists()) {
			int n = JOptionPane.showConfirmDialog(mainFrame,
			   I18N.getMsg("epub.cover.replace", file.getAbsoluteFile()),
			   I18N.getMsg("epub.cover"),
			   JOptionPane.YES_NO_OPTION);
			if (n != JOptionPane.YES_OPTION) {
				return;
			}
		}
		try {
			javax.imageio.ImageIO.write(image, format, file);
		} catch (IOException e) {
			LOG.err("write error: " + e.getMessage());
		}
	}

	public static boolean show(MainFrame m) {
		PropEpubCover dlg = new PropEpubCover(m);
		dlg.setVisible(true);
		return !dlg.canceled;
	}

	public static boolean show(PropertiesDlg m) {
		PropEpubCover dlg = new PropEpubCover(m);
		dlg.setVisible(true);
		return !dlg.canceled;
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

	private void refreshCover() {
		String name = "";
		switch (current) {
			case 1:
				name = "_antic";
				break;
			case 2:
				name = "_classic";
				break;
			case 3:
				name = "_modern";
				break;
			case 4:
				name = "_contemporary";
				break;
			case 5:
				name = "_specific";
				break;
			default:
				break;
		}
		URL res = MainResources.class.getResource("cover/cover" + name + ".jpeg");
		if (current < 1) {
			res = null;
		}
		if (!fImage.isEmpty()) {
			try {
				res = new URL(fImage);
			} catch (MalformedURLException ex) {
				LOG.err("unable to acces to " + fImage);
				fImage = "";
				return;
			}
		}
		StringBuilder buf = new StringBuilder();
		buf.append(Html.HTML_B);
		buf.append(Html.HEAD_B);
		buf.append(Html.STYLE_B);
		if (res != null && !res.toString().isEmpty()) {
			//LOG.printInfos("res="+res.toString());
			buf.append("body {\n")
			   .append("background-image: url('").append(res.toString()).append("');\n")
			   .append("background-repeat: no-repeat;\n")
			   .append("background-attachment: fixed;\n")
			   .append("background-size: cover;\n")
			   .append("}\n");
		}
		buf.append("p {")
		   .append(Html.FONT_SIZE + ": 16px;color: ")
		   .append(ColorUtil.toHexString(btColor.getBackground())).append(";")
		   .append(Html.AL_CENTER)
		   .append(Html.MARGIN_RIGHT + ": "
			  + "64"
			  + "px;")
		   .append(Html.MARGIN_LEFT + ": "
			  + "64"
			  + "px;")
		   .append("}");
		buf.append(Html.STYLE_E);
		buf.append(Html.HEAD_E);
		buf.append(Html.BODY_B);
		if (!ckText.isSelected()) {
			buf.append(getInline("margin_top", ""));
			buf.append(getInline("size_author", book.getAuthor()));
			buf.append(getInline("margin_title_before", ""));
			buf.append(getInline("size_title", book.getTitle()));
			buf.append(getInline("margin_title_after", ""));
			buf.append(getInline("size_footer", Const.getFullName()));
		}
		buf.append(Html.BODY_E);
		buf.append(Html.HTML_E);
		edCover.setText(buf.toString());
		SwingUtil.setEnable(margins, !ckText.isSelected());
	}

	private String getInline(String sz, String str) {
		//LOG.printInfos(TT + ".getInline(sz=" + sz + ", str=\"" + str + "\")");
		StringBuilder buf = new StringBuilder();
		buf.append("<p style=\"")
		   .append(Html.FONT_SIZE + ": ")
		   .append(getSpinnerValue(sz).toString())
		   .append("px;\">")
		   .append(str)
		   .append(Html.P_E);
		return buf.toString();
	}

}
