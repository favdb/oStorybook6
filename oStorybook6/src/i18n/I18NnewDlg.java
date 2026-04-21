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
package i18n;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import api.mig.swing.MigLayout;
import storybook.tools.spell.SpellUtil;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.dialog.AbsDialog;

/**
 *
 * @author favdb
 */
public class I18NnewDlg extends AbsDialog {

	public String language;
	private final JDialog parentDlg;
	private final List<String> languages;
	private JList<String> lsLanguage;
	private final boolean bFile = false;

	public I18NnewDlg(JDialog p, MainFrame m, List<String> l) {
		super(m);
		languages = l;
		parentDlg = p;
		initAll();
	}

	@Override
	public void init() {
		// empty
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initUi() {
		setLayout(new MigLayout(MIG.WRAP + " 1"));
		setTitle(I18N.getMsg("language.select"));
		add(new JLabel(I18N.getMsg("language.select")));
		lsLanguage = new javax.swing.JList<>();
		DefaultListModel model = new DefaultListModel();
		for (SpellUtil.Language lang : SpellUtil.getLanguages()) {
			if (!languages.contains(lang.getCode())) {
				model.addElement(lang.getCode() + "=" + lang.getName());
			}
		}
		lsLanguage.setModel(model);
		JScrollPane jScrollPane1 = new JScrollPane(lsLanguage);
		add(jScrollPane1);

		add(getCancelButton(), MIG.get(MIG.SPLIT2, MIG.SG, MIG.RIGHT));
		add(getOkButton(), MIG.get(MIG.SG, MIG.RIGHT));
		pack();
		setLocationRelativeTo(mainFrame);
		setModal(true);
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (lsLanguage.getSelectedIndex() == -1) {
					return;
				}
				language = (String) lsLanguage.getSelectedValue();
				dispose();
			}
		};
	}

	@Override
	protected AbstractAction getCancelAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				language = "";
				dispose();
			}
		};
	}

	public String getLanguage() {
		return (language);
	}

	public boolean isFile() {
		return (bFile);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
