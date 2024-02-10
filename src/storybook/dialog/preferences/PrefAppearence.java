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
import javax.swing.JCheckBox;
import storybook.App;
import storybook.Pref;
import storybook.ui.MIG;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public class PrefAppearence extends AbstractPanel {

	private final PreferencesDlg caller;
	private Pref pref;
	private JCheckBox ckTypist, ckDescription, ckNotes;

	public PrefAppearence(PreferencesDlg dlg) {
		super(dlg.getMainFrame());
		this.caller = dlg;
		initAll();
	}

	@Override
	public void init() {
		pref = App.preferences;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		setLayout(new MigLayout(MIG.WRAP, "[][][]"));
		ckTypist = new JCheckBox();
		ckTypist.setSelected(pref.typistGet());
		add(ckTypist, MIG.SPAN);

		ckDescription = Ui.initCheckBox(this, "ckDescription", "tabbed.description",
		   App.preferences.getBoolean(Pref.KEY.EDITOR_TABBED_DESCRIPTION),
		   BNONE + "!span, wrap");
		ckNotes = Ui.initCheckBox(this, "ckNotes", "tabbed.notes",
		   App.preferences.getBoolean(Pref.KEY.EDITOR_TABBED_NOTES),
		   BNONE + "!span, wrap");
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
		Pref preferences = App.preferences;
		preferences.typistSet(ckTypist.isSelected());
		preferences.setBoolean(Pref.KEY.EDITOR_TABBED_DESCRIPTION, ckDescription.isSelected());
		preferences.setBoolean(Pref.KEY.EDITOR_TABBED_NOTES, ckNotes.isSelected());
	}

	public void refreshUi() {
		ckTypist.setText(I18N.getMsg("typist.preference"));
	}

}
