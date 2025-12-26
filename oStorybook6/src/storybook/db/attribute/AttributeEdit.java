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
package storybook.db.attribute;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.JTextField;
import storybook.Const;
import storybook.db.abs.AbstractEntity;
import storybook.edit.AbstractEditor;
import storybook.edit.Editor;
import static storybook.ui.Ui.*;

/**
 *
 * @author favdb
 */
public class AttributeEdit extends AbstractEditor {

	private JTextField tfKey;
	private JTextField tfValue;

	public AttributeEdit(Editor m, AbstractEntity e) {
		super(m, e, "110");
		initAll();
	}

	@Override
	public void initUpper() {
		Attribute attribute = (Attribute) entity;
		tfName.setEnabled(false);
		tfKey = initStringField(pUpper, "attribute.key", 256, attribute.getKey(), BMANDATORY);
		tfValue = initStringField(pUpper, "attribute.value", 256, attribute.getValue(), BMANDATORY);
	}

	@Override
	public boolean verifier() {
		if (tfName.getText().isEmpty()) {
			tfName.setText("?");
		}
		resetError();
		if (tfKey.getText().isEmpty()) {
			errorMsg(tfKey, Const.ERROR_MISSING);
		}
		if (tfValue.getText().isEmpty()) {
			errorMsg(tfValue, Const.ERROR_MISSING);
		}
		return msgError.isEmpty();
	}

	@Override
	public void apply() {
		Attribute attribute = (Attribute) entity;
		attribute.setKey(tfKey.getText());
		attribute.setValue(tfValue.getText());
		tfName.setText(attribute.getKey() + " > " + attribute.getValue());
		super.apply();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		// empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// empty
	}

}
