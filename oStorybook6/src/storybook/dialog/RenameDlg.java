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
package storybook.dialog;

import api.mig.swing.MigLayout;
import i18n.I18N;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import storybook.ctrl.Ctrl;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.tag.Tag;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class RenameDlg extends AbsDialog {

	private final String which;
	JComboBox<String> combo;
	JTextField tfNewName;

	public RenameDlg(MainFrame parent, String which) {
		super(parent);
		this.which = which;
		initAll();
	}

	@Override
	public void init() {
		//empty
	}

	@Override
	public void initUi() {
		JLabel lbRename = new JLabel(I18N.getMsg("rename.rename"));
		combo = new JComboBox<>();
		JLabel lbTo = new JLabel(I18N.getMsg("rename.to"));
		tfNewName = new JTextField(20);
		switch (which) {
			case "country":
				this.setTitle(I18N.getMsg("location.rename.country"));
				createListCombo(countryGetList());
				break;
			case "city":
				this.setTitle(I18N.getMsg("location.rename.city"));
				createListCombo(cityGetList());
				break;
			case "item":
				this.setTitle(I18N.getMsg("item.rename.category"));
				createListCombo(itemCategoryGetList());
				break;
			case "tag":
				this.setTitle(I18N.getMsg("tag.rename.category"));
				createListCombo(tagCategoryGetList());
				break;
			default:
				break;
		}
		//layout
		setLayout(new MigLayout("wrap 4", "[]", "[]20[]"));
		setTitle(I18N.getMsg("rename"));
		add(lbRename);
		add(combo);
		add(lbTo);
		add(tfNewName);
		add(getOkButton(), "sg,span,split 2,right");
		add(getCancelButton(), "sg");
		pack();
		setLocationRelativeTo(mainFrame);
		this.setModal(true);
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String oldValue = (String) combo.getSelectedItem();
				String newValue = tfNewName.getText();
				if (!newValue.isEmpty()) {
					rename(oldValue, newValue);
					dispose();
				}
			}
		};
	}

	@SuppressWarnings("unchecked")
	private void createListCombo(List<String> list) {
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		for (String category : list) {
			model.addElement(category);
		}
		combo.setModel(model);
	}

	private void rename(String oldValue, String newValue) {
		switch (which) {
			case "country":
				countryRename(oldValue, newValue);
				break;
			case "city":
				cityRename(oldValue, newValue);
				break;
			case "item":
				itemCategoryRename(oldValue, newValue);
				break;
			case "tag":
				tagCategoryRename(oldValue, newValue);
				break;
			default:
				break;
		}
	}

	protected List<String> countryGetList() {
		return mainFrame.project.locations.findCountries();
	}

	protected void countryRename(String oldValue, String newValue) {
		Ctrl ctrl = mainFrame.getBookController();
		List<Location> locations = mainFrame.project.locations.findByCountry(oldValue);
		for (Location location : locations) {
			location.setCountry(newValue);
			ctrl.updateEntity(location);
		}
	}

	protected List<String> cityGetList() {
		return mainFrame.project.locations.findCities();
	}

	protected void cityRename(String oldValue, String newValue) {
		Ctrl ctrl = mainFrame.getBookController();
		List<Location> locations = mainFrame.project.locations.findByCity(oldValue);
		for (Location location : locations) {
			location.setCity(newValue);
			ctrl.updateEntity(location);
		}
	}

	protected List<String> itemCategoryGetList() {
		List<String> ret = mainFrame.project.items.findCategories();
		return ret;
	}

	protected void itemCategoryRename(String oldValue, String newValue) {
		Ctrl ctrl = mainFrame.getBookController();
		@SuppressWarnings("unchecked")
		List<Item> items = (List) mainFrame.project.items.findCategory(oldValue);
		for (Item item : items) {
			item.setCategory(newValue);
			ctrl.updateEntity(item);
		}
	}

	protected List<String> tagCategoryGetList() {
		List<String> ret = mainFrame.project.tags.findCategories();
		return ret;
	}

	protected void tagCategoryRename(String oldValue, String newValue) {
		Ctrl ctrl = mainFrame.getBookController();
		@SuppressWarnings("unchecked")
		List<Tag> tags = (List) mainFrame.project.tags.findCategory(oldValue);
		for (Tag tag : tags) {
			tag.setCategory(newValue);
			ctrl.updateEntity(tag);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
