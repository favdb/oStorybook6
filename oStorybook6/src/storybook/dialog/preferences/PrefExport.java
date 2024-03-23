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
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import storybook.App;
import storybook.Pref;
import storybook.exim.exporter.ExportToPhpBB;
import storybook.tools.swing.FontUtil;
import storybook.ui.MIG;
import storybook.ui.panel.AbstractPanel;

/**
 *
 * @author favdb
 */
public class PrefExport extends AbstractPanel {

	private final PreferencesDlg caller;
	private Pref pref;
	private JRadioButton rbTxt;
	private JRadioButton rbCsv;
	private JRadioButton rbXml;
	private JRadioButton rbHtml;
	private JCheckBox ckPhp;
	private JComboBox cbPhpTheme;
	private JLabel lbTheme;
	private JCheckBox ckPrint;

	public PrefExport(PreferencesDlg dlg) {
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
		setLayout(new MigLayout(MIG.WRAP, "[10%][]"));
		add(new JLabel(I18N.getColonMsg("export.type")));
		JPanel p = new JPanel(new MigLayout());
		rbTxt = new JRadioButton("txt");
		p.add(rbTxt);
		rbCsv = new JRadioButton("csv");
		p.add(rbCsv);
		rbXml = new JRadioButton("xml");
		p.add(rbXml);
		rbHtml = new JRadioButton("html");
		p.add(rbHtml);
		switch (pref.getString(Pref.KEY.EXP_FORMAT)) {
			case "txt":
				rbTxt.setSelected(true);
				break;
			case "csv":
				rbCsv.setSelected(true);
				break;
			case "html":
				rbHtml.setSelected(true);
				break;
			default:
				rbXml.setSelected(true);
				break;
		}
		ButtonGroup bgExport = new ButtonGroup();
		bgExport.add(rbCsv);
		bgExport.add(rbTxt);
		bgExport.add(rbHtml);
		bgExport.add(rbXml);
		add(p);
		add(initPhpBB(), MIG.get(MIG.SPAN, MIG.GROW));
		add(initPrint(), MIG.get(MIG.SPAN, MIG.GROWX));
	}

	@SuppressWarnings("unchecked")
	private JPanel initPhpBB() {
		JPanel pPhpBB = new JPanel(new MigLayout(MIG.WRAP, "[][]"));
		pPhpBB.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("export.phpbb")));
		JLabel lb = new JLabel(I18N.getMsg("export.phpbb_prompt"));
		lb.setFont(FontUtil.getItalic(lb.getFont()));
		pPhpBB.add(lb, MIG.SPAN);
		ckPhp = new JCheckBox(I18N.getMsg("export.phpbb_activate"));
		ckPhp.setSelected(App.preferences.getBoolean(Pref.KEY.EXP_PHPBB));
		ckPhp.addActionListener(e -> changePhpBB());
		pPhpBB.add(ckPhp, MIG.get(MIG.SPAN));
		lbTheme = new JLabel(I18N.getColonMsg("export.phpbb_theme"));
		pPhpBB.add(lbTheme);
		cbPhpTheme = new JComboBox();
		cbPhpTheme.addItem(I18N.getMsg("export.phpbb_blue"));
		cbPhpTheme.addItem(I18N.getMsg("export.phpbb_choco"));
		cbPhpTheme.addItem(I18N.getMsg("export.phpbb_olive"));
		pPhpBB.add(cbPhpTheme, MIG.get(MIG.SPAN, MIG.GROWX));
		String cx = App.preferences.getString(Pref.KEY.EXP_PHPBB_THEME);
		cbPhpTheme.setSelectedIndex(ExportToPhpBB.getColorIndex(cx));
		changePhpBB();
		return pPhpBB;
	}

	private void changePhpBB() {
		lbTheme.setEnabled(ckPhp.isSelected());
		cbPhpTheme.setEnabled(ckPhp.isSelected());
	}

	private JPanel initPrint() {
		JPanel p = new JPanel(new MigLayout());
		p.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("print")));
		ckPrint = new JCheckBox(I18N.getMsg("print.page"));
		ckPrint.setSelected(App.preferences.getPrintPage());
		p.add(ckPrint);
		return p;
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
		// preferred formet
		String export = "xml";
		if (rbTxt.isSelected()) {
			export = "txt";
		} else if (rbCsv.isSelected()) {
			export = "csv";
		} else if (rbHtml.isSelected()) {
			export = "html";
		} else if (rbXml.isSelected()) {
			export = "xml";
		}
		pref.setString(Pref.KEY.EXP_FORMAT, export);
		// copy for PhpBB
		pref.setBoolean(Pref.KEY.EXP_PHPBB, ckPhp.isSelected());
		int i = cbPhpTheme.getSelectedIndex();
		if (i < 0) {
			i = 0;
		}
		if (i >= ExportToPhpBB.COLORS.length) {
			i = ExportToPhpBB.COLORS.length - 1;
		}
		pref.setString(Pref.KEY.EXP_PHPBB_THEME, ExportToPhpBB.COLORS[i][0]);
		pref.setBoolean(Pref.KEY.PRINTPAGE, ckPrint.isSelected());
	}

	public void refreshUi() {
	}

}
