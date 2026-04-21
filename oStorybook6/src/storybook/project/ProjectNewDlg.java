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
import storybook.tools.LOG;
import storybook.tools.swing.FontUtil;
import storybook.tools.swing.js.JSDateChooser;
import storybook.ui.MIG;
import storybook.ui.frames.main.MainFrame;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class ProjectNewDlg extends AbsDialog implements ItemListener, ChangeListener {

	private static final String TT = "ProjectNewDlg.";

	private JPanel objectivePanel;
	private JTextField tfTitle, nbParts, nbChapters, nbScenes;
	private JComboBox cbNature;
	private JSDateChooser objective;
	private JCheckBox ckObjective;
	private JLabel lbDate;
	int[][] minmax = {//min and max allowed values for number of parts, chapters, scenes to create
		{1, 9},//parts
		{1, 10},//chapters
		{1, 10}//scenes
	};
	private JLabel lpParts;
	private JLabel lbChapters;
	private JLabel lbScenes;
	private JPanel pNature;

	//class for allowed values for number of parts, chapters, scenes to create
	public class Nature {

		public int id;
		public int[] parts, chapters, scenes;

		public Nature(int id, int[] p, int[] c, int[] s) {
			this.id = id;
			this.parts = p;
			this.chapters = c;
			this.scenes = s;
		}
	}

	public Nature[] natures = {
		// Nature(id, {pMin, pMax}, {cMin, cMax}, {sMin, sMax})
		new Nature(0, new int[]{1, 20}, new int[]{1, 100}, new int[]{1, 20}), // Autre
		new Nature(1, new int[]{1, 9}, new int[]{1, 10}, new int[]{1, 10}), // Roman Long
		new Nature(2, new int[]{1, 3}, new int[]{1, 10}, new int[]{1, 10}), // Roman
		new Nature(3, new int[]{1, 1}, new int[]{1, 5}, new int[]{1, 8}), // Roman Court
		new Nature(4, new int[]{1, 1}, new int[]{1, 3}, new int[]{1, 6}), // Nouvelle
		new Nature(5, new int[]{1, 1}, new int[]{1, 1}, new int[]{1, 6}) // Histoire courte
	};

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
		add(tfTitle = Ui.getStringField(DB.DATA.TITLE, 32, "", BNONE));

		// nature
		add(new JLabel(I18N.getColonMsg("book.nature")), MIG.RIGHT);
		cbNature = PropertiesDlg.initCbNature();
		cbNature.addItemListener(this);
		add(cbNature);

		//nature panel
		pNature = new JPanel(new MigLayout(MIG.WRAP, "[][]"));
		//nbparts
		pNature.add(lpParts = new JLabel(getLabel("parts", natures[0].parts)), MIG.get(MIG.SPAN, MIG.SPLIT2));
		pNature.add(nbParts = Ui.getStringField(DB.DATA.NBPARTS, 1, minmax[0][0], BNONE));
		//nbChapters
		pNature.add(lbChapters = new JLabel(getLabel("chapters", natures[0].chapters)), MIG.get(MIG.SPAN, MIG.SPLIT2));
		pNature.add(nbChapters = Ui.getStringField(DB.DATA.NBCHAPTERS, 2, minmax[1][0], BNONE));
		//nbScenes
		pNature.add(lbScenes = new JLabel(getLabel("scenes", natures[0].scenes)), MIG.get(MIG.SPAN, MIG.SPLIT2));
		pNature.add(nbScenes = Ui.getStringField(DB.DATA.NBSCENES, 2, minmax[2][0], BNONE));
		add(pNature, MIG.SPAN);

		//objective panel
		objectivePanel = new JPanel(new MigLayout(MIG.WRAP, "[][]"));
		ckObjective = new JCheckBox(I18N.getMsg("objective"));
		ckObjective.addChangeListener(this);
		objectivePanel.add(ckObjective, MIG.SPLIT2);
		lbDate = new JLabel(I18N.getColonMsg("date"));
		objectivePanel.add(lbDate, MIG.SPLIT2);
		objective = new JSDateChooser(mainFrame, 1);
		objective.setName("date");
		objective.setVisible(false);
		objectivePanel.add(objective);
		//objective panel
		add(new JLabel(" "));
		add(objectivePanel, MIG.SPAN);
		objectivePanel.setVisible(false);

		// ok/cancel
		add(new JLabel(" "));
		add(getCancelButton(), MIG.get(MIG.SPLIT2, MIG.RIGHT));
		add(getOkButton(), MIG.RIGHT);
		refreshNature();
		pack();
		setLocationRelativeTo(mainFrame);
	}

	private String getLabel(String key, int nat[]) {
		String rc = I18N.getMsg(key + ".generate.text");
		return String.format("%s (%d, %d)", rc, nat[0], nat[1]);
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
		StringBuilder rc = new StringBuilder();
		if (tfTitle.getText().isEmpty()) {
			rc.append(I18N.getColonMsg("book.title")).append(I18N.getMsg("error.missing")).append("\n");
		}
		Nature n = natures[cbNature.getSelectedIndex()];
		if (cbNature.getSelectedIndex() == 0) {
			nbParts.setText("1");
			nbChapters.setText("1");
			nbScenes.setText("1");
		} else {
			rc.append(checkValue("parts", nbParts, n.parts));
			rc.append(checkValue("chapters", nbChapters, n.chapters));
			rc.append(checkValue("scenes", nbScenes, n.scenes));
		}
		if (rc.toString().isEmpty()) {
			return true;
		}
		JOptionPane.showMessageDialog(this, rc.toString(), I18N.getMsg("error"), JOptionPane.OK_OPTION);
		return false;
	}

	/**
	 * check if given JtextField is numeric and value is less than minv and greater than maxv
	 *
	 * @param tf
	 * @param val: values to check
	 * @return
	 */
	private String checkValue(String key, JTextField tf, int[] val) {
		if (StringUtil.isNumeric(tf.getText())) {
			int v = Integer.parseInt(tf.getText());
			if (v >= val[0] && v <= val[1]) {
				return "";
			}
			return I18N.getColonMsg(key) + " " + I18N.getMsg("error.wrong") + "\n";
		}
		return I18N.getColonMsg(key) + " " + I18N.getMsg("error.not_numeric") + "\n";
	}

	@Override
	public String getTitle() {
		return tfTitle.getText();
	}

	public int getNature() {
		return cbNature.getSelectedIndex();
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

	public int getNbScenes() {
		if (nbScenes.getText().isEmpty()) {
			return 1;
		}
		return Math.max(0, Integer.parseInt(nbScenes.getText()));
	}

	public Date getObjective() {
		return objective.getDate();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		LOG.trace(TT + "itemStateChanged(e=" + e.toString() + ")");
		refreshNature();
		pack();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		//LOG.trace(TT + "stateChanged(e=" + e.toString() + ")");
		if (e.getSource() instanceof JCheckBox) {
			lbDate.setVisible(ckObjective.isSelected());
			objective.setVisible(ckObjective.isSelected());
		}
	}

	private void refreshNature() {
		int n = cbNature.getSelectedIndex();
		LOG.trace((TT + "refreshNature() " + n));
		pNature.setVisible(n > 0);
		objective.setVisible(n > 0);
	}

}
