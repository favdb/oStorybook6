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
package storybook.dialog.chooser;

import java.awt.Color;
import javax.swing.JComboBox;
import storybook.renderer.ColorLCR;
import storybook.tools.swing.ColorUtil.PALETTE;

/**
 *
 * @author favdb
 */
public class ColorPicker extends JComboBox {

	@SuppressWarnings("unchecked")
	public ColorPicker(Color color) {
		super();
		this.setRenderer(new ColorLCR());
		int sel = -1, i = -1;
		for (PALETTE c : PALETTE.values()) {
			this.addItem(c.getColor());
		}
		setSelectedItem(color);
	}

}
