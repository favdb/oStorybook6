/*
 * Copyright (C) 2022 favdb
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
package storybook.dialog.chooser;

import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import api.mig.swing.MigLayout;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.tools.swing.ColorIcon;
import storybook.tools.swing.ColorUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.Ui;
import storybook.dialog.AbsDialog;

/**
 * color chooser dialog for reduced palette
 *
 * @author favdb
 */
public class ColorSimpleChooserDlg extends AbsDialog {

	public static Color show(MainFrame mainFrame, Color color) {
		ColorSimpleChooserDlg dlg = new ColorSimpleChooserDlg(mainFrame, color);
		dlg.setVisible(true);
		return dlg.getSelectedColor();
	}

	private Color defColor;
	private JButton btOther;

	public ColorSimpleChooserDlg(MainFrame mainFrame, Color defColor) {
		super(mainFrame);
		this.defColor = defColor;
		initAll();
	}

	@Override
	public void init() {
		//empty
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout());
		JPanel pp = new JPanel(new MigLayout(MIG.get(MIG.GAP1, MIG.WRAP + " 8")));
		for (ColorUtil.PALETTE p : ColorUtil.PALETTE.values()) {
			JToggleButton bt = new JToggleButton();
			bt.setIcon(new ColorIcon(p.getColor(), IconUtil.getDefSize()));
			if (App.isDev()) {
				bt.setToolTipText(p.name());
			}
			bt.setSelected(p.getColor().equals(defColor));
			bt.addActionListener(e -> {
				if (bt.isSelected()) {
					defColor = ((ColorIcon) bt.getIcon()).getColor();
				}
			});
			pp.add(bt);
		}
		add(pp, MIG.get(MIG.SPAN, MIG.CENTER));
		btOther = Ui.initButton("btother", "other", ICONS.K.COLOR, "", e -> otherColor());
		add(btOther, MIG.get(MIG.SPAN, MIG.GROWX));
		add(getCancelButton(), MIG.get(MIG.NEWLINE, MIG.SPAN, MIG.SPLIT2, MIG.RIGHT));
		add(getOkButton(), MIG.RIGHT);
		pack();
		this.setLocationRelativeTo(getParent());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	public Color getSelectedColor() {
		return defColor;
	}

	private void otherColor() {
		Color color = JColorChooser.showDialog(this, I18N.getMsg("color.choose"), defColor);
		if (color != null) {
			btOther.setBackground(color);
			defColor = color;
		}
	}

}
