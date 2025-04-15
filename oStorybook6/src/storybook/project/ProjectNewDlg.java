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
package storybook.project;

import api.jsoup.internal.StringUtil;
import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import storybook.db.DB;
import storybook.dialog.AbsDialog;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.js.JSDateChooser;
import storybook.ui.MIG;
import storybook.ui.MainFrame;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class ProjectNewDlg extends AbsDialog implements ItemListener, ChangeListener {

	private static final String TT = "NewProjectDlg";
	private JTextField tfTitle, nbParts;
	private JComboBox nature;
	private JTextField nbChapters;
	private JSDateChooser objective;
	private JPanel panel;
	private JCheckBox ckObjective;
	private JLabel lbDate;

	public ProjectNewDlg(MainFrame mainFrame) {
		super(mainFrame);
		initAll();
	}

	@Override
	public void init() {
		setLayout(new MigLayout(MIG.get(MIG.WRAP, MIG.HIDEMODE2), "[][]"));
		// title
		JLabel lb = new JLabel(I18N.getColonMsg("book.title"));
		lb.setFont(FontUtil.getBold(lb.getFont()));
		add(lb, MIG.RIGHT);
		tfTitle = Ui.getStringField(DB.DATA.TITLE, 32, "", BNONE);
		add(tfTitle);
		// nature
		add(new JLabel(I18N.getColonMsg("book.nature")), MIG.RIGHT);
		nature = PropertiesDlg.initCbNature();
		nature.addItemListener(this);
		add(nature);

		//objective panel
		panel = new JPanel(new MigLayout(MIG.WRAP, "[][]"));
		//nbparts
		panel.add(new JLabel(I18N.getColonMsg("parts.generate.text")), MIG.get(MIG.SPAN, MIG.SPLIT2));
		nbParts = Ui.getStringField(DB.DATA.NBPARTS, 1, "1", BNONE);
		panel.add(nbParts);
		//nbChapters
		panel.add(new JLabel(I18N.getColonMsg("chapters.generate.text")), MIG.get(MIG.SPAN, MIG.SPLIT2));
		nbChapters = Ui.getStringField(DB.DATA.NBCHAPTERS, 2, "1", BNONE);
		panel.add(nbChapters);
		//objective
		ckObjective = new JCheckBox(I18N.getMsg("objective"));
		ckObjective.addChangeListener(this);
		panel.add(ckObjective, MIG.SPLIT2);
		lbDate = new JLabel(I18N.getColonMsg("date"));
		panel.add(lbDate, MIG.SPLIT2);
		objective = new JSDateChooser(mainFrame, 1);
		objective.setName("date");
		objective.setVisible(false);
		panel.add(objective);
		//objective panel
		add(new JLabel(" "));
		add(panel, MIG.SPAN);
		panel.setVisible(false);
		// ok/cancel
		add(new JLabel(" "));
		add(getCancelButton(), MIG.get(MIG.SPLIT2, MIG.RIGHT));
		add(getOkButton(), MIG.RIGHT);
		pack();
		setLocationRelativeTo(mainFrame);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//empty
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (checkData()) {
					dispose();
				}
			}
		};
	}

	private boolean checkData() {
		String rc = "";
		if (tfTitle.getText().isEmpty()) {
			rc += I18N.getColonMsg("bool.title") + I18N.getMsg("error.missing") + "\n";
		}
		if (!StringUtil.isNumeric(nbParts.getText()) || Integer.parseInt(nbParts.getText()) > 9) {
			rc += "book.nb_parts" + I18N.getMsg("err.value.too.long") + "\n";
		}
		if (!StringUtil.isNumeric(nbChapters.getText()) || Integer.parseInt(nbChapters.getText()) > 99) {
			rc += "book.nb_Chapters" + I18N.getMsg("err.value.too.long") + "\n";
		}
		if (rc.isEmpty()) {
			return (true);
		}
		JOptionPane.showMessageDialog(this, rc, I18N.getMsg("error"), JOptionPane.OK_OPTION);
		return (false);
	}

	@Override
	public String getTitle() {
		return tfTitle.getText();
	}

	public int getNature() {
		return nature.getSelectedIndex();
	}

	public int getNbParts() {
		if (nbParts.getText().isEmpty()) {
			return 1;
		}
		return Math.max(1, Integer.parseInt(nbParts.getText()));
	}

	public int getNbChapters() {
		if (nbChapters.getText().isEmpty()) {
			return 1;
		}
		return Math.max(1, Integer.parseInt(nbChapters.getText()));
	}

	public Date getObjective() {
		return objective.getDate();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (nature.getSelectedIndex() > 0) {
			panel.setVisible(true);
			pack();
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JCheckBox) {
			lbDate.setVisible(ckObjective.isSelected());
			objective.setVisible(ckObjective.isSelected());
		}
	}

}
