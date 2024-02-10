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
package storybook.tools.swing.js;

import i18n.I18N;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.db.abs.AbstractEntity;

/**
 *
 * @author favdb
 */
public class JSMenuItem extends JMenuItem {

	public JSMenuItem(String name, ICONS.K icon, ActionListener action) {
		super();
		setName(name);
		setText(I18N.getMsg(name));
		if (icon != null && icon != ICONS.K.EMPTY) {
			setIcon(IconUtil.getIconSmall(icon));
		}
		addActionListener(action);
		setFont(App.getInstance().fonts.defGet());
	}

	public JSMenuItem(AbstractEntity entity, ICONS.K icon, ActionListener action) {
		super();
		setName(entity.getName());
		setText(entity.getName());
		setIcon(entity.getIcon());
		addActionListener(action);
		setFont(App.getInstance().fonts.defGet());
	}

}
