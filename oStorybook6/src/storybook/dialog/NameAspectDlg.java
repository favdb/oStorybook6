/*
 * Copyright (C) 2023 favdb
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
package storybook.dialog;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.tools.swing.ColorUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;
import storybook.dialog.chooser.ColorPicker;

/**
 *
 * @author favdb
 */
public class NameAspectDlg extends AbsDialog {

	private static final String TT = "NameAspectDlg";

	private String aspect = "";
	private JRadioButton rbBold;
	private JRadioButton rbNormal;
	private JRadioButton rbItalic;
	private ColorPicker cbColor;
	private JLabel lbColor;
	private boolean reinit = false;

	public NameAspectDlg(MainFrame mainFrame, String aspect) {
		super(mainFrame);
		initAll();
		setValue(aspect);
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	public void initUi() {
		super.initUi();
		setLayout(new MigLayout(MIG.FILLX, "[][]"));
		setTitle(I18N.getMsg("shef.format"));
		setIconImage(IconUtil.getIconImageSmall("font"));
		JPanel p1 = new JPanel(new MigLayout());
		rbNormal = Ui.initRadioButton(p1, "none", false, BNONE);
		rbBold = Ui.initRadioButton(p1, "shef.bold", false, BNONE);
		rbItalic = Ui.initRadioButton(p1, "shef.italic", false, BNONE);
		ButtonGroup bg = new ButtonGroup();
		bg.add(rbNormal);
		bg.add(rbBold);
		bg.add(rbItalic);
		add(new JLabel(I18N.getColonMsg("shef.format")));
		add(p1, MIG.WRAP);

		lbColor = new JLabel(I18N.getColonMsg("color"));
		add(lbColor);
		cbColor = new ColorPicker(ColorUtil.fromHexString(aspect));
		add(cbColor, MIG.WRAP);

		add(Ui.initButton("btReinit", "clear", ICONS.K.CLEAR, "", e -> {
			reinit = true;
			dispose();
		}), MIG.get(MIG.SPAN, MIG.SPLIT + " 3", MIG.RIGHT));
		add(getCancelButton(), MIG.RIGHT);
		add(getOkButton(), MIG.RIGHT);
		pack();
		this.setLocationRelativeTo(mainFrame);
		setValue(aspect);
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				apply();
				reinit = false;
				dispose();
			}
		};
	}

	protected AbstractAction getReinit() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reinit = true;
				dispose();
			}
		};
	}

	@Override
	protected AbstractAction getCancelAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canceled = true;
				dispose();
			}
		};
	}

	private void apply() {

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	public String getValue() {
		if (reinit) {
			return "";
		}
		StringBuilder v = new StringBuilder();
		if (rbBold.isSelected()) {
			v.append("B");
		} else if (rbItalic.isSelected()) {
			v.append("I");
		} else {
			v.append("N");
		}
		String xcol = "";
		if (cbColor.getSelectedIndex() != -1) {
			xcol = ColorUtil.toHexString((Color) cbColor.getSelectedItem());
		}
		v.append(xcol);
		return v.toString();
	}

	private void setValue(String aspect) {
		//LOG.trace(TT + ".setValue(aspect=" + aspect + ")");
		this.aspect = aspect;
		if (aspect.isEmpty()) {
			reinit = true;
		}
		if (aspect.startsWith("B")) {
			rbBold.setSelected(true);
		} else if (aspect.startsWith("I")) {
			rbItalic.setSelected(true);
		} else {
			rbNormal.setSelected(true);
		}
		if (aspect.length() > 2) {
			cbColor.setSelectedItem((Color) ColorUtil.fromHexString(aspect.substring(1)));
		} else {
			cbColor.setSelectedItem(Color.BLACK);
		}
	}

}
