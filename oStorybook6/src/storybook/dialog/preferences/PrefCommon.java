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
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Const;
import storybook.Pref;
import storybook.dialog.chooser.ColorSimpleChooserDlg;
import storybook.tools.LOG;
import storybook.tools.file.EnvUtil;
import static storybook.tools.file.EnvUtil.getHomeDir;
import storybook.tools.swing.ColorIcon;
import storybook.tools.swing.SwingUtil;
import storybook.tools.synonyms.Synonyms;
import storybook.ui.MIG;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public class PrefCommon extends AbstractPanel {

	private static final String TT = "PrefCommon";

	private JLabel lbLanguage;
	private int inLang;
	private JComboBox<Object> cbLanguage;
	private JButton btLanguage;
	private boolean modifLanguage = false;

	private JButton btLanguageReset;
	private JLabel lbDateFormat, lbTimeFormat;
	private JComboBox<Object> cbDateFormat, cbTimeFormat;
	private JLabel lbOnStart;
	private JCheckBox ckOnStart;
	private JLabel lbOnExit;
	private JCheckBox ckOnExit;
	private Pref pref;
	private Locale currentlocale;
	private String initMsgFile;
	private String newMsgFile;
	public PreferencesDlg caller;
	private JPanel pColors;
	private JCheckBox ckOffline;

	public PrefCommon(PreferencesDlg dlg) {
		super(dlg.getMainFrame());
		this.caller = dlg;
		initAll();
	}

	@Override
	public void init() {
		pref = App.preferences;
		currentlocale = Locale.getDefault();
		initMsgFile = App.getInstance().getI18nFile();
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.FILLX, MIG.HIDEMODE3, MIG.WRAP), "[right][]"));
		lbLanguage = new JLabel();
		String currentLangStr = I18N.getCountryLanguage(Locale.getDefault());
		add(lbLanguage);
		cbLanguage = new javax.swing.JComboBox<>();
		inLang = 0;
		int in = 0;
		for (Const.Language lang : Const.Language.values()) {
			cbLanguage.addItem(lang.getI18N());
			if (lang.toString().equals(currentLangStr)) {
				inLang = in;
			}
			in++;
		}
		cbLanguage.setSelectedIndex(inLang);
		cbLanguage.addActionListener((java.awt.event.ActionEvent evt) -> {
			int i = cbLanguage.getSelectedIndex();
			Const.Language lang = Const.Language.values()[i];
			Locale locale = lang.getLocale();
			I18N.initMsgInternal(locale);
			modifLanguage = true;
			caller.refreshUi();
		});
		add(cbLanguage, MIG.SPLIT + " 3");
		btLanguage = new JButton();
		btLanguage.setIcon(IconUtil.getIconSmall(ICONS.K.F_OPEN));
		btLanguage.setToolTipText(I18N.getMsg("language.file_change"));
		SwingUtil.setForcedSize(btLanguage, IconUtil.getDefDim());
		btLanguage.addActionListener((ActionEvent evt) -> changeLanguageFile());
		add(btLanguage, "gap 0");

		btLanguageReset = new JButton();
		btLanguageReset.setIcon(IconUtil.getIconSmall(ICONS.K.CLEAR));
		btLanguageReset.setToolTipText(I18N.getMsg("language.file_reset"));
		SwingUtil.setForcedSize(btLanguageReset, IconUtil.getDefDim());
		btLanguageReset.addActionListener((ActionEvent evt) -> {
			newMsgFile = null;
			modifLanguage = true;
			I18N.setFileMessages(null);
			I18N.initMessages(currentlocale);
			refreshUi();
		});
		add(btLanguageReset, "gap 0");

		lbDateFormat = new JLabel();
		add(lbDateFormat);
		cbDateFormat = new JComboBox<>();
		cbDateFormat.addItem("dd-MM-yyyy");
		cbDateFormat.addItem("MM-dd-yyyy");
		cbDateFormat.addItem("dd/MM/yyyy");
		cbDateFormat.setSelectedItem(pref.getString(Pref.KEY.DATEFORMAT, "MM-dd-yyyy"));
		add(cbDateFormat);
		lbTimeFormat = new JLabel();
		add(lbTimeFormat);
		cbTimeFormat = new JComboBox<>();
		cbTimeFormat.addItem("HH:mm:ss");
		cbTimeFormat.addItem("HH:mm");
		cbTimeFormat.setSelectedItem(pref.getString(Pref.KEY.TIMEFORMAT, "HH:mm:ss"));
		add(cbTimeFormat);
		lbOnStart = new JLabel();
		add(lbOnStart);
		ckOnStart = new JCheckBox();
		ckOnStart.setSelected(pref.getBoolean(Pref.KEY.OPEN_LASTFILE, false));
		add(ckOnStart);
		lbOnExit = new JLabel();
		add(lbOnExit);
		ckOnExit = new JCheckBox();
		ckOnExit.setSelected(pref.getBoolean(Pref.KEY.CONFIRM_EXIT, false));
		add(ckOnExit);

		add(new JLabel(I18N.getColonMsg("intensity.colors")));
		pColors = new JPanel(new MigLayout());
		for (int i = 0; i < 5; i++) {
			JButton btc = new JButton((i + 1) + " ");
			Color color = App.preferences.intensityGet(i);
			btc.setHorizontalTextPosition(SwingConstants.LEFT);
			btc.setIcon(new ColorIcon(color, IconUtil.getDefSize()));
			btc.addActionListener(e -> {
				Color cc = ColorSimpleChooserDlg.show(mainFrame, color);
				btc.setIcon(new ColorIcon(cc, IconUtil.getDefSize()));
			});
			pColors.add(btc);
		}
		add(pColors);
		ckOffline = new JCheckBox();
		ckOffline.setHorizontalTextPosition(SwingConstants.LEFT);
		ckOffline.setSelected(pref.getBoolean(Pref.KEY.WORK_OFFLINE, false));
		add(ckOffline, MIG.SPAN);
		refreshUi();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

	public void apply() {
		//LOG.trace(TT + ".apply()");
		pref.setString(Pref.KEY.DATEFORMAT, cbDateFormat.getSelectedItem().toString());
		pref.setString(Pref.KEY.TIMEFORMAT, cbTimeFormat.getSelectedItem().toString());
		pref.setBoolean(Pref.KEY.OPEN_LASTFILE, ckOnStart.isSelected());
		pref.setBoolean(Pref.KEY.CONFIRM_EXIT, ckOnExit.isSelected());
		// language
		int iLang = cbLanguage.getSelectedIndex();
		Const.Language lang = Const.Language.values()[iLang];
		Locale locale = lang.getLocale();
		pref.setString(Pref.KEY.LANGUAGE, locale.toString());
		if (modifLanguage) {
			if (newMsgFile != null) {
				LOG.log("Messsage set Msgfile to : " + newMsgFile);
			}
			App.getInstance().setI18nFile(newMsgFile);
			if (initMsgFile == null || initMsgFile.isEmpty()) {
				App.preferences.removeString(Pref.KEY.MSGFILE);
			}
			pref.setString(Pref.KEY.MSGFILE, newMsgFile);
			I18N.initMsgInternal(locale);
			pref.setString(Pref.KEY.SPELLING, locale.getLanguage());
			Synonyms.init(getHomeDir().getAbsolutePath()
			   + File.separator + ".storybook5/dicts/", pref.getString(Pref.KEY.SPELLING));
			caller.setToRefresh();
			mainFrame.getMainMenu().refresh();
		}

		if (pColors != null) {
			for (int iIntensity = 0; iIntensity < 5; iIntensity++) {
				JButton btc = (JButton) pColors.getComponent(iIntensity);
				ColorIcon ic = (ColorIcon) btc.getIcon();
				App.preferences.intensitySet(iIntensity, ic.getColor());
			}
		}
		pref.setBoolean(Pref.KEY.WORK_OFFLINE, ckOffline.isSelected());
	}

	public boolean isLanguageChange() {
		return (modifLanguage);
	}

	public void refreshUi() {
		this.setName(I18N.getMsg("pref.general"));
		lbLanguage.setText(I18N.getColonMsg("language"));
		lbDateFormat.setText(I18N.getColonMsg("date.format.label"));
		lbTimeFormat.setText(I18N.getColonMsg("time.format.label"));
		lbOnStart.setText(I18N.getMsg("preferences.start"));
		ckOnStart.setText(I18N.getMsg("preferences.start.openproject"));
		lbOnExit.setText(I18N.getColonMsg("preferences.exit"));
		ckOnExit.setText(I18N.getMsg("preferences.exit.chb"));
		ckOffline.setText(I18N.getMsg("preferences.offline"));
	}

	private void changeLanguageFile() {
		LOG.trace(TT + ".changeLanguageFile()");
		String str = EnvUtil.getHomeDir().getAbsolutePath();
		JFileChooser chooser = new JFileChooser(str);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int i = chooser.showOpenDialog(caller);
		if (i != 0) {
			return;
		}
		File file = chooser.getSelectedFile();
		newMsgFile = file.getAbsolutePath();
		I18N.setFileMessages(newMsgFile);
		I18N.initMessages(currentlocale);
		refreshUi();
		modifLanguage = true;
	}

}
