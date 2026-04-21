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
import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import storybook.db.book.WEB_SUMMARY.WEB_SUMMARY_H;
import storybook.dialog.AbsDialog;
import storybook.dialog.chooser.ColorPicker;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;

/**
 *
 * @author favdb
 */
public class PropWebAdvancedDlg extends AbsDialog {

	private static final String TT = "PropWebAdvancedDlg.";

	private final PropWebDlg propWeb;
	private WEB_SUMMARY_H webH;
	private JComboBox cbFamily, cbSize, cbStyle;
	private ColorPicker cbColor, cbBkColor;

	public PropWebAdvancedDlg(PropWebDlg propWeb, WEB_SUMMARY_H webH) {
		this.propWeb = propWeb;
		this.webH = webH;
		initAll();
	}

	@Override
	public void init() {
		//empty
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP), "[][]"));
		add(new JLabel("font-family: "));
		String[] fonts = {"serif", "sans-serif", "monospace", "cursive"};
		cbFamily = new JComboBox(fonts);
		cbFamily.setSelectedItem(webH.getFamily());
		add(cbFamily);

		add(new JLabel("font-size: "));
		String[] sizes = {"110%", "100%", "90%"};
		cbSize = new JComboBox(sizes);
		cbSize.setSelectedItem(webH.getSize() + "%");
		add(cbSize);

		add(new JLabel("font-style: "));
		String[] styles = {"normal", "bold", "italic"};
		cbStyle = new JComboBox(styles);
		cbStyle.setSelectedItem(webH.getStyle());
		add(cbStyle);

		add(new JLabel("color: "));
		cbColor = new ColorPicker(ColorUtil.fromHexString(webH.getColor()));
		SwingUtil.setCBsize(cbColor);
		add(cbColor);

		add(new JLabel("background: "));
		cbBkColor = new ColorPicker(ColorUtil.fromHexString(webH.getBkColor()));
		SwingUtil.setCBsize(cbBkColor);
		add(cbBkColor);

		JPanel bts = new JPanel(new MigLayout());
		bts.add(getOkButton(), MIG.get(MIG.RIGHT, MIG.SPLIT2));
		bts.add(getCancelButton());

		add(bts, MIG.get(MIG.SPAN, MIG.RIGHT));

		pack();
		setLocationRelativeTo(propWeb);
	}

	public WEB_SUMMARY_H getH() {
		WEB_SUMMARY_H nv = new WEB_SUMMARY_H();
		nv.setFamily((String) cbFamily.getSelectedItem());
		nv.setSize(((String) cbSize.getSelectedItem()).replace("%", ""));
		nv.setStyle((String) cbStyle.getSelectedItem());
		Color clr = Color.BLACK;
		if (cbColor.getSelectedIndex() != -1) {
			clr = ((Color) cbColor.getSelectedItem());
		}
		nv.setColor(ColorUtil.getHTML(clr));
		clr = Color.WHITE;
		if (cbBkColor.getSelectedIndex() != -1) {
			clr = ((Color) cbBkColor.getSelectedItem());
		}
		nv.setBkColor(ColorUtil.getHTML(clr));
		return nv;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

}
