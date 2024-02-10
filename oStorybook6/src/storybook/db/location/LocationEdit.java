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
package storybook.db.location;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import storybook.db.DB.DATA;
import storybook.db.abs.AbstractEntity;
import storybook.db.book.Book;
import storybook.edit.AbstractEditor;
import storybook.edit.Editor;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSCheckList;
import storybook.ui.MIG;
import storybook.ui.Ui;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class LocationEdit extends AbstractEditor {

	private JTextField tfAddress;
	private JTextField tfAltitude;
	private JComboBox cbSup;
	private JComboBox cbCity;
	private JComboBox cbCountry;
	private JTextField tfGps;
	private JSCheckList lRelations;

	public LocationEdit(Editor m, AbstractEntity e) {
		super(m, e, "111");
		initAll();
	}

	@Override
	public void initUpper() {
		btDeploy.setEnabled(false);
		Location location = (Location) entity;
		JPanel panel1 = new JPanel(new MigLayout(
		   MIG.get(MIG.WRAP, MIG.HIDEMODE2),
		   "[][grow]"));
		cbSup = Ui.initAutoCombo(panel1, mainFrame, DATA.LOCATION_SITE, Book.TYPE.LOCATION,
		   location.getSite(), location, "011");
		tfAddress = Ui.initStringField(panel1, DATA.LOCATION_ADDRESS, 0,
		   location.getAddress(), BNONE);
		cbCity = Ui.initAutoCombo(panel1, DATA.LOCATION_CITY,
		   mainFrame.project.locations.findCities(), location.getCity(), "010");
		cbCountry = Ui.initAutoCombo(panel1, DATA.LOCATION_COUNTRY,
		   mainFrame.project.locations.findCountries(), location.getCountry(), "010");
		tfAltitude = Ui.initIntegerField(panel1, DATA.LOCATION_ALTITUDE, 5,
		   location.getAltitude(), BNONE);
		tfGps = Ui.initStringField(panel1, DATA.LOCATION_GPS, 10,
		   location.getGps(), BNONE);
		JScrollPane sc = new JScrollPane(panel1);
		sc.setPreferredSize(SwingUtil.getScreenSize());
		sc.setBorder(BorderFactory.createEmptyBorder());
		tab1.add(sc, I18N.getMsg("editor.main_part"));
		lRelations = Ui.initCkList(pUpper, mainFrame, Book.TYPE.RELATION,
		   mainFrame.project.relations.find(entity), tab1, BNONE);
	}

	@Override
	public boolean verifier() {
		resetError();
		return (msgError.isEmpty());
	}

	@Override
	public void apply() {
		//App.trace("LocationEdit.apply()");
		Location location = (Location) entity;
		if (cbSup.getSelectedItem() == null || cbSup.getSelectedIndex() < 1) {
			location.setSite(null);
		} else {
			location.setSite((Location) cbSup.getSelectedItem());
		}
		location.setAddress(tfAddress.getText());
		String str = (String) cbCity.getSelectedItem();
		if (str == null) {
			str = "";
		}
		location.setCity(str);
		str = (String) cbCountry.getSelectedItem();
		if (str == null) {
			str = "";
		}
		location.setCountry(str);
		location.setAltitude(Integer.getInteger(tfAltitude.getText()));
		location.setGps(tfGps.getText());
		super.apply();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton bt = (JButton) e.getSource();
			switch (bt.getName()) {
				default:
					break;
			}
		}
	}

}
