/*
 * Copyright (C) 2019 favdb
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
package storybook.db.person;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import resources.icons.ICONS;
import storybook.Const;
import storybook.db.abs.AbstractEntity;
import storybook.db.attribute.Attribute;
import storybook.db.book.Book;
import storybook.db.category.Category;
import storybook.db.gender.Gender;
import storybook.dialog.chooser.ColorPicker;
import storybook.edit.AbstractEditor;
import storybook.edit.Editor;
import storybook.tools.LOG;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSCheckList;
import storybook.tools.swing.js.JSDateChooser;
import storybook.ui.MIG;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class PersonEdit extends AbstractEditor implements ActionListener {

	private JTextField tfFirstName;
	private JTextField tfLastName;
	private JTextField tfAbbr;
	private JComboBox cbGender;
	private JComboBox cbCategory;
	private JSDateChooser dBirth;
	private JSDateChooser dDeath;
	private JTextField tfOccupation;
	private boolean autoAbbr = false;
	private JLabel tfCategories;
	private JButton btCategories;
	private JSCheckList lAttributes;
	private ColorPicker cbColor;
	private JButton btColor;
	private JSCheckList lRelations;

	public PersonEdit(Editor m, AbstractEntity e) {
		super(m, e, "111");
		initAll();
	}

	@Override
	public void initUpper() {
		btDeploy.setEnabled(false);
		JPanel panel1 = new JPanel(new MigLayout(MIG.get(MIG.INS1, MIG.WRAP, MIG.HIDEMODE2), "[][grow]"));
		Person person = (Person) entity;
		tfName.setEditable(false);
		tfFirstName = Ui.initStringField(panel1, "firstname", 0, person.getFirstname(), BMANDATORY);
		tfLastName = Ui.initStringField(panel1, "lastname", 0, person.getLastname(), BNONE);
		tfAbbr = Ui.initStringField(panel1, "abbreviation", 8, person.getAbbreviation(), BMANDATORY);
		if (tfAbbr.getText().isEmpty()) {
			autoAbbr = true;
			tfFirstName.addCaretListener(e -> doAutoAbbr());
			tfLastName.addCaretListener(e -> doAutoAbbr());
			tfAbbr.addCaretListener(e -> {
				if (tfAbbr.hasFocus()) {
					autoAbbr = false;
				}
			});
		}
		// choix de la couleur
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0)));
		cbColor = new ColorPicker(person.getJColor());
		SwingUtil.setCBsize(cbColor);
		p.add(cbColor);
		btColor = Ui.initButton("btColor", "", ICONS.K.COLOR, "color.other", e -> changeColor());
		btColor.setHorizontalAlignment(SwingConstants.LEFT);
		if (cbColor.getSelectedIndex() < 1) {
			btColor.setBackground(person.getJColor());
		}
		p.add(btColor);
		Ui.addField(panel1, "color", "", p, null, BMANDATORY);
		cbGender = Ui.initCbEntities(panel1, this, "gender", Book.TYPE.GENDER,
				person.getGender(), null, BMANDATORY + BNEW);
		cbCategory = Ui.initCbEntities(panel1, this, "category", Book.TYPE.CATEGORY,
				person.getCategory(), null, BMANDATORY + BNEW);
		// preparation pour le changement de gestion des catégories multiples
		/*JPanel pc = new JPanel(new MigLayout());
		tfCategories = new JLabel(Category.listToString(person.getCategories()));
		pc.add(tfCategories);
		btCategories = Ui.initButton("btCategories", "", ICONS.K.ENT_CATEGORY, "list.modify", this);
		pc.add(btCategories);*/
		dBirth = initSbDate(panel1, "person.birthday", person.getBirthday(), BNONE, 1);
		dDeath = initSbDate(panel1, "person.death", person.getDayofdeath(), BNONE, 1);
		tfOccupation = Ui.initStringField(panel1, "person.occupation", 20, person.getOccupation(), BNONE);
		// scroller pour panel1
		JScrollPane sc = new JScrollPane(panel1);
		sc.setPreferredSize(SwingUtil.getScreenSize());
		sc.setBorder(BorderFactory.createEmptyBorder());
		//panel.add(sc, MIG.get(MIG.GROWX, MIG.SPAN));
		tab1.add(sc, I18N.getMsg("editor.main_part"));
		// relations
		/*lRelations = Ui.initCkList(pUpper, mainFrame, Book.TYPE.RELATION,
		   mainFrame.project.relations.find(entity), tab1, BNONE);*/
		// attributs
		lAttributes = Ui.initCkList(this, mainFrame, Book.TYPE.ATTRIBUTE,
				person.getAttributes(), tab1, BNONE);
	}

	private void doAutoAbbr() {
		if (!autoAbbr) {
			return;
		}
		String str1 = tfFirstName.getText();
		if (str1.length() > 2) {
			str1 = str1.substring(0, 2);
		}
		String str2 = tfLastName.getText();
		if (str2.length() > 2) {
			str2 = str2.substring(0, 3);
		}
		tfAbbr.setText(str1 + str2);
		tfName.setText(tfFirstName.getText() + " " + tfLastName.getText());
	}

	@Override
	public boolean verifier() {
		resetError();
		Person person = (Person) entity;
		if (tfFirstName.getText().isEmpty()) {
			errorMsg(tfFirstName, Const.ERROR_MISSING);
		}
		//check if this name is allready use
		String s = tfFirstName.getText() + " " + tfLastName.getText();
		Person p = (Person) mainFrame.project.findByName(Book.TYPE.PERSON, s);
		if (p != null && !Objects.equals(p.getId(), person.getId())) {
			errorMsg(tfName, Const.ERROR_EXISTS);
		}
		tfName.setText(s);
		if (tfAbbr.getText().isEmpty()) {
			errorMsg(tfAbbr, Const.ERROR_MISSING);
		}
		Date birth = dBirth.getDate();
		if (birth != null) {
			Date death = dDeath.getDate();
			if (death != null && birth.after(death)) {
				errorMsg(dDeath, "error.date_before_birth");
			}
		}
		return (msgError.isEmpty());
	}

	@Override
	public void apply() {
		Person person = (Person) entity;
		person.setFirstname(tfFirstName.getText());
		person.setLastname(tfLastName.getText());
		person.setAbbreviation(tfAbbr.getText());
		person.setGender((Gender) cbGender.getSelectedItem());
		if (cbCategory.getSelectedIndex() != -1) {
			person.setCategory(
					(Category) cbCategory.getSelectedItem());
		} else {
			person.setCategory(
					(Category) mainFrame.project.getList(Book.TYPE.CATEGORY).get(0));
		}
		person.setBirthday(dBirth.getDate());
		person.setDayofdeath(dDeath.getDate());
		person.setOccupation(tfOccupation.getText());
		if (cbColor.getSelectedIndex() == -1) {
			person.setJColor(btColor.getBackground());
		} else {
			person.setJColor((Color) cbColor.getSelectedItem());
		}
		if (lAttributes != null) {
			person.setAttributes(getAttributes());
		}
		super.apply();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		//App.printInfos("PersonEdit.actionPerformed(e="+e.toString()+")");
		Object prop = e.getSource();
		if (prop instanceof JButton) {
			JButton bt = (JButton) prop;
			switch (bt.getName()) {
				case "btCategories":
				/*CheckBoxPanel cbp = new CheckBoxPanel(mainFrame, Book.TYPE.CATEGORY, (List) lsCategories);
					JDialog dlg = new JDialog();
					dlg.setLayout(new MigLayout());
					dlg.add(cbp);
					dlg.setModal(false);
					dlg.setLocationRelativeTo(this);
					dlg.setVisible(true);
					lsCategories = new ArrayList<>();
					for (AbstractEntity c : cbp.getSelectedEntities()) {
						lsCategories.add((Category) c);
					}
					tfCategories.setText(Category.listToString(lsCategories));*/
				default:
					break;
			}
		}
	}

	private void changeColor() {
		LOG.trace("changeColor()");
		Color color = JColorChooser.showDialog(this, I18N.getMsg("color.choose"),
				((Person) entity).getJColor());
		btColor.setBackground(color);
	}

	/**
	 * get the selected Attributes
	 *
	 * @return
	 */
	public List<Attribute> getAttributes() {
		List<Attribute> ls = new ArrayList<>();
		lAttributes.getSelectedEntities().forEach((e) -> {
			ls.add((Attribute) e);
		});
		return ls;
	}

}
