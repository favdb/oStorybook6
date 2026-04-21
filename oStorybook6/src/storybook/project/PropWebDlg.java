/*
 * Copyright (C) 2024 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package storybook.project;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import resources.icons.ICONS;
import storybook.db.book.BookParamWeb;
import storybook.dialog.AbsDialog;
import storybook.dialog.chooser.ColorPicker;
import storybook.exim.exporter.ExportBookDlg;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSFileSelector;
import storybook.ui.MIG;
import storybook.ui.Ui;
import storybook.ui.frames.main.MainFrame;

/**
 * configuration dialog to export HTML for a website
 *
 * webadanced options:<br>
 * <ul>
 * <li>banner: to use an image banner file</li>
 * <li>author and copyright: to generate these elements</li>
 * </ul>
 *
 * @author favdb
 */
public class PropWebDlg extends AbsDialog {

	private static final String TT = "PropWebDlg";

	public static void show(ExportBookDlg exp) {
		PropWebDlg dlg = new PropWebDlg(exp.getMainFrame());
		dlg.setVisible(true);
	}

	private final BookParamWeb web;
	private JCheckBox ckBanner, ckSummary, summaryChapter;
	private JSFileSelector slBanner;
	private ColorPicker cbColor;
	private List<JLabel> lbCss;
	private final int NB_H = 3;
	private JCheckBox ckAuthor, ckCopyright;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public PropWebDlg(MainFrame mainFrame) {
		super(mainFrame);
		web = mainFrame.getBook().param.getParamWeb();
		initAll();
	}

	@Override
	public void init() {

	}

	/**
	 * initialize user interface
	 */
	@Override
	public void initUi() {
		setTitle(I18N.getMsg("web.advanced"));
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP), "[][][]"));
		initHome();
		initSummary();
		initMain();
		JPanel bts = new JPanel(new MigLayout());
		bts.add(getOkButton(), MIG.get(MIG.RIGHT, MIG.SPLIT2));
		bts.add(getCancelButton());

		add(bts, MIG.get(MIG.SPAN, MIG.RIGHT));
		setModal(true);
		pack();
		setLocationRelativeTo(mainFrame);
	}

	/**
	 * initialize the Home
	 */
	public void initHome() {
		JPanel panel = new JPanel(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.FILL)));
		panel.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("web.home")));
		// banner
		ckBanner = Ui.initCheckBox(panel, "ckBanner", "web.banner", web.getBanner(), null,
				(ActionListener) (ActionEvent e) -> {
					slBanner.setVisible(ckBanner.isSelected());
					pack();
				});
		slBanner = new JSFileSelector("slBanner", "", web.getBannerImg(), true,
				I18N.getMsg("file.type.img") + " (*.jpg, *.jpeg)", "jpg");
		slBanner.setVisible(ckBanner.isSelected());
		panel.add(slBanner, MIG.SPAN);
		// header for author and copyright
		JPanel p2 = new JPanel(new MigLayout(MIG.WRAP, "[]"));
		ckAuthor = Ui.initCheckBox(p2, "ckAuthor", "web.author",
				web.getAuthor(), null);
		ckCopyright = Ui.initCheckBox(p2, "ckCopyright", "web.copyright",
				web.getCopyright(), null);
		panel.add(p2, MIG.SPAN);
		// footer
		add(panel, MIG.get(MIG.GROWX, MIG.SPAN));
	}

	/**
	 * initialize the Summary
	 */
	@SuppressWarnings("unchecked")
	public void initSummary() {
		JPanel panel = new JPanel(new MigLayout(MIG.WRAP + " 2"));
		panel.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("web.summary")));
		// titled
		ckSummary = Ui.initCheckBox(panel,
				"ckSummary", "web.summary_titled", web.getSummary().getTitled(), null);
		// chapters only
		summaryChapter = Ui.initCheckBox(panel,
				"summaryChapter", "web.summary_chapter", web.getSummary().getChapters(), null);
		// color
		panel.add(new JLabel(I18N.getMsg("web.summary_color")), MIG.get(MIG.SPAN, MIG.SPLIT2));
		cbColor = new ColorPicker(web.getSummary().getColor());
		SwingUtil.setCBsize(cbColor);
		panel.add(cbColor, MIG.SPAN);
		// panels for h1 to h6
		JPanel px = new JPanel(new MigLayout(MIG.WRAP, "[][][]"));
		px.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("web.summary_titles")));
		lbCss = new ArrayList<>();
		for (int i = 0; i < NB_H; i++) {
			JLabel v = new JLabel();
			lbCss.add(v);
			String nm = "h" + (i + 1);
			px.add(new JLabel(nm));
			px.add(lbCss.get(i));
			px.add(Ui.initButton("bt" + nm,
					"change", ICONS.K.EMPTY, "", (ActionListener) (ActionEvent e) -> {
						changeH(nm);
					}));
		}
		panel.add(new JLabel("   "), MIG.get(MIG.SPAN, MIG.SPLIT2));
		panel.add(px);
		refreshCss();
		add(panel, MIG.get(MIG.GROWX, MIG.SPAN));
	}

	/**
	 * initialize the Main
	 */
	public void initMain() {

	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canceled = false;
				apply();
				dispose();
			}
		};
	}

	public void apply() {
		web.setBanner(ckBanner.isSelected());
		web.setBannerImg(slBanner.getFileName());
		web.getSummary().setTitled(ckSummary.isSelected());
		web.getSummary().setChapters(summaryChapter.isSelected());
		Color color = Color.LIGHT_GRAY;
		if (cbColor.getSelectedIndex() != -1) {
			color = ((Color) cbColor.getSelectedItem());
		}
		web.getSummary().setColor(ColorUtil.getHTML(color));
		web.setAuthor(ckAuthor.isSelected());
		web.setCopyright(ckCopyright.isSelected());
		mainFrame.setUpdated();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	private void changeH(String nm) {
		//LOG.trace(TT + "changeH(nm=" + nm + ")");
		int i = Integer.parseInt(nm.replace("h", "")) - 1;
		PropWebAdvancedDlg dlg = new PropWebAdvancedDlg(this, web.getSummary().getH(i));
		dlg.setVisible(true);
		if (!dlg.isCanceled()) {
			web.getSummary().setH(i, dlg.getH());
			refreshCss();
		}
	}

	private void refreshCss() {
		for (int i = 0; i < NB_H; i++) {
			lbCss.get(i).setText(web.getSummary().getHtml(i));
		}
	}

}
