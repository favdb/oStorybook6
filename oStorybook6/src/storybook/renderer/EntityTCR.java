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
package storybook.renderer;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import storybook.App;
import storybook.db.abs.AbstractEntity;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.person.Person;
import storybook.db.strand.Strand;
import storybook.tools.ListUtil;
import storybook.tools.TextUtil;
import storybook.tools.swing.ColorIcon;
import storybook.ui.MainFrame;

/**
 * table cell renderer for an entity or a list of entities
 *
 * @author favdb
 */
public class EntityTCR extends DefaultTableCellRenderer {

	MainFrame mainFrame;

	public EntityTCR(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
	   boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
		   "", isSelected, hasFocus, row, column);
		label.setToolTipText("");
		if (value == null) {
			label.setText("");
		} else {
			if (value instanceof List) {
				List list = (List) value;
				if (list.isEmpty()) {
					label.setIcon(IconUtil.getIconSmall(ICONS.K.EMPTY));
					label.setText("");
					label.setToolTipText("");
				} else {
					setList(label, list);
				}
			} else if (value instanceof Icon) {
				setIcon(label, value);
			} else if (value instanceof String) {
				setString(label, value);
			} else if (value instanceof AbstractEntity) {
				setEntity(label, (AbstractEntity) value);
			}
		}
		return label;
	}

	private void setStrand(JLabel label, Object value) {
		label.setHorizontalAlignment(CENTER);
		int sz = App.fonts.defGet().getSize();
		label.setIcon(new ColorIcon(Strand.getJColor((Strand) value), sz, sz * 2));
		label.setText("");
		label.setToolTipText(((Strand) value).getName());
	}

	private void setIcon(JLabel label, Object value) {
		label.setText("");
		label.setIcon((Icon) value);
	}

	private void setString(JLabel label, Object value) {
		String str = (String) value;
		if (str.isEmpty() || str.trim().equals("[]")) {
			label.setText("");
		} else {
			label.setText(str);
		}
	}

	private void setEntity(JLabel label, AbstractEntity entity) {
		if (entity == null) {
			return;
		}
		if (entity instanceof Strand) {
			setStrand(label, entity);
		} else {
			try {
				/*if (!(entity instanceof Attribute)) {
					label.setIcon(entity.getIcon());
				}*/
				label.setText(entity.getName());
			} catch (Exception ex) {

			}
		}
	}

	private void setList(JLabel label, List value) {
		@SuppressWarnings("unchecked")
		List<AbstractEntity> list = (List<AbstractEntity>) value;
		if (list.size() < 2) {
			setEntity(label, list.get(0));
			AbstractEntity e = list.get(0);
			if (e instanceof Person) {
				label.setIcon(((Person) e).getIcon());
			}
			label.setToolTipText(e.getName());
		} else {
			List<String> txt = new ArrayList<>();
			List<String> tips = new ArrayList<>();
			for (AbstractEntity entity : list) {
				if (entity instanceof Person) {
					label.setIcon(IconUtil.getIconSmall(ICONS.K.ENT_PERSON));
					txt.add(((Person) entity).getAbbreviation());
				} else {
					txt.add(TextUtil.trunc(entity.getName(), 6));
				}
				tips.add(entity.getName());
			}
			label.setText(ListUtil.join(txt));
			label.setToolTipText(ListUtil.join(tips, ", "));
		}
		if (list.get(0) instanceof Location || list.get(0) instanceof Item) {
			label.setIcon(list.get(0).getIcon());
		}
	}

}
