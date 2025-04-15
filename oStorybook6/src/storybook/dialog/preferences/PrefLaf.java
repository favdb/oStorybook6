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
package storybook.dialog.preferences;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import resources.icons.ICONS;
import resources.icons.IconButton;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Const;
import storybook.Pref;
import storybook.dialog.chooser.ColorChooserPanel;
import storybook.dialog.chooser.FontChooserDlg;
import storybook.tools.swing.ColorUtil;
import storybook.tools.swing.FontUtil;
import storybook.ui.MIG;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public class PrefLaf extends AbstractPanel {

	private final JDialog caller;
	public boolean changeLaf = false;
	private JComboBox cbLaf;
	private JCheckBox ckColored;
	private ColorChooserPanel btColor;
	private Color originColor;
	private JLabel lbAppearence;
	private JLabel lbFontDefault, lbFontBook, lbFontEditor, lbFontMono;
	private FontPanel fntDefault, fntBook, fntEditor, fntMono;
	private JComboBox iconSize;
	private String origin;
	private boolean first = false;

	public PrefLaf(PreferencesDlg dlg, boolean first) {
		super(dlg.getMainFrame());
		this.caller = dlg;
		this.first = first;
		initAll();
	}

	public PrefLaf(JDialog dlg, boolean first) {
		super();
		this.caller = dlg;
		this.first = first;
		initAll();
	}

	@Override
	public void init() {
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.GAP + " 2", MIG.WRAP + " 3")));
		add(new JLabel(I18N.getColonMsg("preferences.laf")));
		cbLaf = new JComboBox();
		for (Const.LookAndFeel laf : Const.LookAndFeel.values()) {
			cbLaf.addItem(laf.getI18N());
		}
		cbLaf.addActionListener((ActionListener) e -> {
			changeLaf = true;
		});
		add(cbLaf, MIG.SPAN);
		String plaf = App.preferences.getString(Pref.KEY.LAF);
		Const.LookAndFeel laf = Const.LookAndFeel.LIGHT;
		for (Const.LookAndFeel v : Const.LookAndFeel.values()) {
			if (v.name().equalsIgnoreCase(plaf)) {
				laf = v;
			}
		}
		cbLaf.setSelectedIndex(laf.ordinal());
		changeLaf = false;

		ckColored = new JCheckBox(I18N.getMsg("pref.colored"));
		ckColored.setName(("ckColored"));
		ckColored.setSelected(App.preferences.getBoolean(Pref.KEY.LAF_COLORED));
		ckColored.addActionListener(this);
		add(ckColored, MIG.SKIP);
		originColor = Color.decode(App.preferences.getString(Pref.KEY.LAF_COLOR));
		btColor = new ColorChooserPanel(I18N.getMsg("color.choose"), originColor, null, false);
		btColor.setVisible(ckColored.isSelected());
		add(btColor, MIG.SPAN);

		// font
		lbAppearence = new JLabel(I18N.getMsg("preferences.font"));
		lbAppearence.setFont(FontUtil.getBold(App.fonts.defGet()));
		add(lbAppearence, MIG.SPAN);

		add(new JLabel(I18N.getColonMsg("size")), MIG.RIGHT);
		IconButton fontPlus = new IconButton("fontPlus", ICONS.K.PLUS, e -> increaseFont(1));
		add(fontPlus, MIG.get(MIG.SPAN, MIG.SPLIT2));
		IconButton fontMinus = new IconButton("fontPlus", ICONS.K.MINUS, e -> increaseFont(-1));
		add(fontMinus);

		lbFontDefault = new JLabel();
		add(lbFontDefault, MIG.RIGHT);
		fntDefault = new FontPanel(caller, "default", App.fonts.defGet());
		add(fntDefault, MIG.SPAN);

		lbFontBook = new JLabel();
		add(lbFontBook, MIG.RIGHT);
		fntBook = new FontPanel(caller, "book", App.fonts.bookGet());
		add(fntBook, MIG.SPAN);

		lbFontEditor = new JLabel();
		add(lbFontEditor, MIG.RIGHT);
		fntEditor = new FontPanel(caller, "editor", App.fonts.editorGet());
		add(fntEditor, MIG.SPAN);

		lbFontMono = new JLabel();
		add(lbFontMono, MIG.RIGHT);
		fntMono = new FontPanel(caller, "mono", App.fonts.monoGet());
		add(fntMono, MIG.SPAN);

		add(new JLabel(I18N.getColonMsg("icon.size")), MIG.get(MIG.SPAN, MIG.SPLIT2));
		iconSize = new JComboBox();
		iconSize.addItem(I18N.getMsg("icon.size.small"));
		iconSize.addItem(I18N.getMsg("icon.size.medium"));
		iconSize.addItem(I18N.getMsg("icon.size.large"));
		iconSize.addItem(I18N.getMsg("icon.size.tall"));
		iconSize.setSelectedIndex(App.preferences.getInteger(Pref.KEY.ICON_SIZE));
		add(iconSize, MIG.SPAN);
		refreshUi();
		origin = checkValues();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JCheckBox) {
			btColor.setVisible(ckColored.isSelected());
			changeLaf = true;
		}
	}

	public void apply() {
		Pref pref = App.preferences;
		int i = cbLaf.getSelectedIndex();
		Const.LookAndFeel laf = Const.LookAndFeel.values()[i];
		pref.setString(Pref.KEY.LAF, laf.name());
		pref.setBoolean(Pref.KEY.LAF_COLORED, ckColored.isSelected());
		pref.setString(Pref.KEY.LAF_COLOR, ColorUtil.getHTML(btColor.getColor()));
		App.fonts.defSet(fntDefault.getFont());
		App.fonts.bookSet(fntBook.getFont());
		App.fonts.editorSet(fntEditor.getFont());
		App.fonts.monoSet(fntMono.getFont());
		pref.setInteger(Pref.KEY.ICON_SIZE, iconSize.getSelectedIndex());
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		pref.setInteger(Pref.KEY.ICON_SCREEN, screenSize.width);
		if (first && !origin.equalsIgnoreCase(checkValues())) {
			JOptionPane.showMessageDialog(this,
					I18N.getMsg("preferences.laf.first"),
					I18N.getMsg("preferences.laf"),
					JOptionPane.YES_OPTION);
		} else if (!origin.equalsIgnoreCase(checkValues())) {
			JOptionPane.showMessageDialog(this,
					I18N.getMsg("preferences.laf.warning"),
					I18N.getMsg("preferences.laf"),
					JOptionPane.YES_OPTION);
		}
	}

	public void refreshUi() {
		lbAppearence.setText(I18N.getMsg("preferences.font"));
		lbFontDefault.setText(I18N.getMsg("preferences.font.default"));
		lbFontBook.setText(I18N.getMsg("preferences.font.book"));
		lbFontEditor.setText(I18N.getMsg("preferences.font.editor"));
		lbFontMono.setText(I18N.getMsg("preferences.font.mono"));
	}

	private void increaseFont(int inc) {
		fntDefault.increase(inc);
		fntBook.increase(inc);
		fntEditor.increase(inc);
		fntMono.increase(inc);
	}

	private String checkValues() {
		Pref pref = App.preferences;
		StringBuilder b = new StringBuilder();
		b.append(pref.getString(Pref.KEY.LAF)).append("|");
		b.append(pref.getString(Pref.KEY.LAF_COLORED)).append("|");
		b.append(pref.getString(Pref.KEY.LAF_COLOR)).append("|");
		b.append(pref.getString(Pref.KEY.FONT_DEFAULT)).append("|");
		b.append(pref.getString(Pref.KEY.FONT_BOOK)).append("|");
		b.append(pref.getString(Pref.KEY.FONT_EDITOR)).append("|");
		b.append(pref.getString(Pref.KEY.FONT_MONO)).append("|");
		b.append(String.format("%d", iconSize.getSelectedIndex()));
		return b.toString();
	}

	private class FontPanel extends JPanel {

		private final JLabel show;

		public FontPanel(JDialog caller, String name, Font font) {
			setLayout(new MigLayout(MIG.get(MIG.INS1, MIG.GAP + " 2")));
			setName("font." + name);
			setFont(font);
			show = new JLabel(FontUtil.getString(font));
			show.setName("show");
			show.setBorder(BorderFactory.createEtchedBorder());
			show.setFont(font);
			add(show);
			JButton bt = new JButton(IconUtil.getIconSmall(ICONS.K.PREFERENCES));
			bt.setMargin(new Insets(0, 0, 0, 0));
			bt.addActionListener((java.awt.event.ActionEvent evt) -> {
				//FontChooserDlg dlg = new FontChooserDlg(caller, show.getFont());
				FontChooserDlg dlg = new FontChooserDlg(caller, show.getFont());
				dlg.setVisible(true);
				if (dlg.isCanceled() || dlg.getSelectedFont() == null) {
					return;
				}
				show.setFont(dlg.getSelectedFont());
				show.setText(FontUtil.getString(dlg.getSelectedFont()));
				setFont(dlg.getSelectedFont());
			});
			add(bt);
		}

		private void increase(int sz) {
			String str = show.getText();
			String ps[] = str.split(",");
			int nsz = 12;
			if (ps.length > 2) {
				nsz = Integer.parseInt(ps[2].trim());
			}
			nsz += sz;
			show.setText(ps[0] + ", " + ps[1] + ", " + nsz);
			Font fnt = FontUtil.getFont(show.getText());
			show.setFont(fnt);
			setFont(fnt);
		}
	}

}
