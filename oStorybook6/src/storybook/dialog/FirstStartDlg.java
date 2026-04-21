/*
 * Copyright (C) 2017 favdb
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
import assistant.AssistantWizardDlg;
import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Locale;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Const;
import storybook.Pref;
import storybook.dialog.preferences.PrefLaf;
import storybook.tools.spell.SpellDlg;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;

/**
 *
 * @author favdb
 */
public class FirstStartDlg extends JDialog implements ActionListener {

	private static final String TT = "FirstStartDlg";

	private JComboBox<String> cbLanguage;
	private JCheckBox ckTypist;
	private int phase = 1;
	private PrefLaf lafPanel;
	private JButton btOK;
	private JLabel lbLanguage;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public FirstStartDlg() {
		super((MainFrame) null);
		initialize();
	}

	public void initialize() {
		setIconImage(IconUtil.getIconImage("icon"));
		setLayout(new MigLayout("", "[][]"));
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setModal(true);
		setTitle(I18N.getMsg("first_start.title"));

		JLabel lbLogo = new JLabel();
		lbLogo.setBackground(Color.white);
		lbLogo.setHorizontalAlignment(SwingConstants.CENTER);
		ImageIcon img = IconUtil.getImageIcon("banner");
		lbLogo.setIcon(img);
		lbLogo.setBorder(null);
		add(lbLogo, MIG.get(MIG.SPAN, MIG.CENTER));

		// language selection
		lbLanguage = new JLabel(I18N.getColonMsg("language"));
		add(lbLanguage);
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
		for (Const.Language lang : Const.Language.values()) {
			model.addElement(lang.getI18N());
		}
		Properties props = System.getProperties();
		String k = props.getProperty("user.language") + "_" + props.getProperty("user.country");
		cbLanguage = new JComboBox<>();
		cbLanguage.setName("cbLanguage");
		cbLanguage.setModel(model);
		if (Const.getLanguageIndex(k) < cbLanguage.getItemCount()) {
			cbLanguage.setSelectedIndex(Const.getLanguageIndex(k));
		}
		add(cbLanguage, MIG.get(MIG.GROWX, MIG.SPAN));
		cbLanguage.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					changeLanguage();
				}
			}
		});
		// typist default mode
		ckTypist = new JCheckBox(I18N.getMsg("typist.preference"));
		add(ckTypist, MIG.get(MIG.SPAN));

		getButtonOK();
		add(btOK, MIG.get(MIG.SPAN, MIG.CENTER));
		pack();
		setLocationRelativeTo(null);
	}

	private void changeLanguage() {
		int i = cbLanguage.getSelectedIndex();
		Const.Language lang = Const.Language.values()[i];
		Locale locale = lang.getLocale();
		I18N.initMsgInternal(locale);
		setTitle(I18N.getMsg("first_start.title"));
		lbLanguage.setText(I18N.getColonMsg("language"));
		ckTypist.setText(I18N.getMsg("typist.preference"));
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//LOG.trace(TT + ".actionPerformed(e=" + e.toString() + ")");
		if (e.getSource() instanceof JButton) {
			JButton bt = (JButton) e.getSource();
			if (bt.getName().equals(("ok"))) {
				if (phase == 1) {
					int i = cbLanguage.getSelectedIndex();
					Const.Language lang = Const.Language.values()[i];
					Locale locale = lang.getLocale();
					App.preferences.setString(Pref.KEY.LANGUAGE, I18N.getCountryLanguage(locale));
					I18N.initMsgInternal(locale);
					App.preferences.typistSet(ckTypist.isSelected());
					btOK.removeActionListener(this);
					initPhase2();
					return;
				} else if (phase == 2) {
					lafPanel.apply();
				}
				dispose();
			}
		}
	}

	private void initPhase2() {
		//LOG.trace(TT + ".initPhase2()");
		setTitle(I18N.getMsg("first_start.title"));
		phase++;
		if (messageYesNo("spell")) {
			SpellDlg.install();//install dictionnary
		}
		if (messageYesNo("assistant")) {
			AssistantWizardDlg.install();//install assistant
		}
		if (messageYesNo("layout")) {
			this.getContentPane().removeAll();
			try {
				lafPanel = new PrefLaf(this, true);
			} catch (Exception ex) {
				this.dispose();
				return;
			}
			add(lafPanel, MIG.get(MIG.SPAN, MIG.GROW));
			getButtonOK();
			add(btOK, MIG.get(MIG.SPAN, MIG.CENTER));
			pack();
			setLocationRelativeTo(null);
		} else {
			dispose();
		}
	}

	private void getButtonOK() {
		btOK = new JButton(I18N.getMsg("ok"));
		btOK.setName("ok");
		btOK.setIcon(IconUtil.getIconSmall(ICONS.K.OK));
		btOK.addActionListener(this);
	}

	private boolean messageYesNo(String str) {
		int n = JOptionPane.showConfirmDialog(null,
				I18N.getMsg("first_start.install." + str),
				I18N.getMsg("first_start.title"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		return n == JOptionPane.YES_OPTION;
	}

}
