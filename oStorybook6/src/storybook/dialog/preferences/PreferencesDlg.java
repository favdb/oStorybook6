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
package storybook.dialog.preferences;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import resources.icons.IconUtil;
import storybook.App;
import storybook.Pref;
import storybook.dialog.AbsDialog;
import storybook.project.PropBookLayoutPanel;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;

/**
 *
 * @author favdb
 */
public class PreferencesDlg extends AbsDialog implements ActionListener {

	Font font;
	private JCheckBox cbMemory;
	private PrefCommon tabCommon;
	private PrefAppearence tabAppearence;
	private JTabbedPane tbPane;
	private PrefUpdater tabUpdater;
	private PrefExport tabExport;
	private PrefBackup tabBackup;
	private PropBookLayoutPanel tabLayout;
	private PrefLaf tabPlaf;
	private boolean toRefresh;
	private String curLang;

	public static void show(MainFrame mainFrame) {
		PreferencesDlg dlg = new PreferencesDlg(mainFrame);
		dlg.setVisible(true);
	}

	public PreferencesDlg(MainFrame m) {
		super(m);
		initAll();
	}

	@Override
	public void init() {
		curLang = App.preferences.getString(Pref.KEY.LANGUAGE);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		super.initUi();
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP)));
		setTitle(I18N.getMsg("preferences.title"));
		setIconImage(IconUtil.getIconImage("icon"));
		tbPane = new JTabbedPane();
		tabCommon = new PrefCommon(this);
		tbPane.addTab(I18N.getMsg("pref.common"), tabCommon);
		tabUpdater = new PrefUpdater(this);
		tbPane.addTab(I18N.getMsg("pref.updater"), tabUpdater);
		tabAppearence = new PrefAppearence(this);
		tbPane.addTab(I18N.getMsg("pref.appearence"), tabAppearence);
		tabExport = new PrefExport(this);
		tbPane.addTab(I18N.getMsg("pref.export"), tabExport);
		tabBackup = new PrefBackup(this);
		tbPane.addTab(I18N.getMsg("pref.backup"), tabBackup);
		tabLayout = new PropBookLayoutPanel(this, false);
		tbPane.add(I18N.getMsg("book.layout"), tabLayout);
		tabPlaf = new PrefLaf(this, false);
		tbPane.addTab(I18N.getMsg("preferences.laf"), tabPlaf);
		add(tbPane, MIG.get(MIG.SPAN, MIG.GROW));
		cbMemory = new JCheckBox(I18N.getMsg("preferences.memory"));
		cbMemory.setName("cbMemory");
		cbMemory.setSelected(App.preferences.getBoolean(Pref.KEY.MEMORY, false));
		cbMemory.addActionListener(this);
		add(cbMemory, MIG.get(MIG.SPAN, MIG.RIGHT));
		JButton btDumpPreferences = new JButton("Dump Preferences");
		btDumpPreferences.setFont((App.fonts.defGet()));
		btDumpPreferences.addActionListener(evt -> App.preferences.dump());
		add(btDumpPreferences, "split 4, left");
		JLabel lbEmpty = new JLabel(" ");
		add(lbEmpty, MIG.GROWX);
		add(getCancelButton(), MIG.RIGHT);
		add(getOkButton(), MIG.RIGHT);
		pack();
		this.setLocationRelativeTo(mainFrame);
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				apply();
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
		SwingUtil.setWaitingCursor(this);
		tabCommon.apply();
		tabAppearence.apply();
		tabUpdater.apply();
		tabExport.apply();
		tabBackup.apply();
		tabPlaf.apply();
		tabLayout.apply();
		App.preferences.setString(Pref.KEY.SCENE_SEPARATOR, tabLayout.getSeparator());
		App.preferences.setBoolean(Pref.KEY.MEMORY, cbMemory.isSelected());
		App.preferences.save();
		if (toRefresh) {
			App.getInstance().refresh();
		}
		if (tabCommon.isModifLanguage()) {
			App.getInstance().refreshLanguage();
		}
		SwingUtil.setDefaultCursor(this);
	}

	public void refreshUi() {
		setTitle(I18N.getMsg("preferences.title"));
		cbMemory.setText(I18N.getMsg("preferences.memory"));
		tbPane.setTitleAt(0, I18N.getMsg("pref.common"));
		tbPane.setTitleAt(1, I18N.getMsg("pref.updater"));
		tbPane.setTitleAt(2, I18N.getMsg("pref.appearence"));
		tbPane.setTitleAt(3, I18N.getMsg("pref.export"));
		tbPane.setTitleAt(4, I18N.getMsg("pref.backup"));
		tabAppearence.refreshUi();
		tabBackup.refreshUi();
		tabCommon.refreshUi();
		tabUpdater.refreshUi();
		tabExport.refreshUi();
		tabPlaf.refreshUi();
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//LOG.trace("PreferencesDlg.actionPerformed(e=" + e.toString());
		if (e.getSource() instanceof JCheckBox) {
			JCheckBox cb = (JCheckBox) e.getSource();
			if (cb.getName().equals("cbMemory")) {
				App.preferences.setBoolean(Pref.KEY.MEMORY, cbMemory.isSelected());
				App.getInstance().refreshStatusBar();
			}
		}
	}

	void setToRefresh() {
		toRefresh = true;
	}

}
