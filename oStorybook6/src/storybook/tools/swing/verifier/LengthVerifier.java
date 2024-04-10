/*
Storybook: Scene-based software for novelists and authors.
Copyright (C) 2008 - 2011 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.tools.swing.verifier;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import i18n.I18N;
import api.shef.ShefEditor;

public class LengthVerifier extends AbstractInputVerifier {

	private int length;

	public LengthVerifier(int length) {
		super(true);
		this.length = length;
	}

	public LengthVerifier(int length, boolean acceptEmpty) {
		super(acceptEmpty);
		this.length = length;
	}

	@Override
	public boolean verify(JComponent comp) {
		String errorMsg = I18N.getMsg("verifier.too.long", length);
		if (comp instanceof JTextComponent) {
			JTextComponent tc = (JTextComponent) comp;
			if (tc.getText().length() < length) {
				return true;
			}
			setErrorText(errorMsg);
			return false;
		}
		if (comp instanceof JComboBox) {
			JComboBox combo = (JComboBox) comp;
			Object item = combo.getSelectedItem();
			if (item == null || item.toString().length() < length) {
				return true;
			}
			setErrorText(errorMsg);
			return false;
		}
		if (comp instanceof ShefEditor) {
			ShefEditor editor = (ShefEditor) comp;
			if (editor.getText().length() < length) {
				return true;
			}
			setErrorText(errorMsg);
			return false;
		}
		return true;
	}
}
