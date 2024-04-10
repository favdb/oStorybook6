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
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import storybook.App;
import storybook.Pref;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.js.JSLabel;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public class PrefUpdater extends AbstractPanel {

	private static final String PREF_UPDATER = "preferences.updater.";
	private JSLabel lbUpdater;
	private JRadioButton rbUpdaterNever;
	private JRadioButton rbUpdaterYear;
	private JRadioButton rbUpdaterAuto;
	private final PreferencesDlg caller;
	private Pref pref;

	public PrefUpdater(PreferencesDlg dlg) {
		super(dlg.getMainFrame());
		this.caller = dlg;
		initAll();
	}

	@Override
	public void init() {
		pref = App.preferences;
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout("wrap", "[][]"));
		lbUpdater = new JSLabel(I18N.getMsg("help.update"));
		lbUpdater.setFont(FontUtil.getBold(App.getInstance().fonts.defGet()));
		add(lbUpdater, "span");
		rbUpdaterNever = new JRadioButton(I18N.getMsg(PREF_UPDATER + "never"));
		add(rbUpdaterNever, "skip 1");
		rbUpdaterYear = new JRadioButton(I18N.getMsg(PREF_UPDATER + "year"));
		add(rbUpdaterYear, "skip 1");
		rbUpdaterAuto = new JRadioButton(I18N.getMsg(PREF_UPDATER + "auto"));
		add(rbUpdaterAuto, "skip 1");
		switch (pref.getString(Pref.KEY.UPDATER_DO, "")) {
			case "1":
				rbUpdaterNever.setSelected(true);
				break;
			case "2":
				rbUpdaterYear.setSelected(true);
				break;
			case "3":
				rbUpdaterAuto.setSelected(true);
				break;
			default:
				rbUpdaterNever.setSelected(true);
		}
		ButtonGroup bgUpdater = new ButtonGroup();
		bgUpdater.add(rbUpdaterNever);
		bgUpdater.add(rbUpdaterYear);
		bgUpdater.add(rbUpdaterAuto);
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
		String updater = "1";
		if (rbUpdaterYear.isSelected()) {
			updater = "2";
		} else if (rbUpdaterAuto.isSelected()) {
			updater = "3";
		}
		pref.setString(Pref.KEY.UPDATER_DO, updater);
	}

	public void refreshUi() {
		lbUpdater.setText(I18N.getMsg("help.update"));
		rbUpdaterNever.setText(I18N.getMsg(PREF_UPDATER + "never"));
		rbUpdaterYear.setText(I18N.getMsg(PREF_UPDATER + "year"));
		rbUpdaterAuto.setText(I18N.getMsg(PREF_UPDATER + "auto"));
	}

}
