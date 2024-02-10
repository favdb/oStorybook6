/*
 * Copyright (C) 2021 favdb
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
package storybook.ui;

import java.awt.Color;
import i18n.I18N;
import storybook.tools.swing.js.JSLabel;

/**
 *
 * @author favdb
 */
public class MessageLabel extends JSLabel {

	public MessageLabel(String key, int level) {
		setText(I18N.getMsg(key));
		// level 0 simple message
		if (level == 1) {
			// ok level, set foreground to green
			setForeground(Color.GREEN);
		}
		if (level == 2) {
			//warning level, set background to orange
			setBackground(Color.ORANGE);
		}
		if (level == 3) {
			//error level, set foreground to white and background to red
			setBackground(Color.RED);
			setForeground(Color.WHITE);
		}
		if (level > 0) {
			setOpaque(true);
		}
	}

}
