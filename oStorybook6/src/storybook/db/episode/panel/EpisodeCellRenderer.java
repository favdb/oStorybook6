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
package storybook.db.episode.panel;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import storybook.db.EntityUtil;
import storybook.db.abs.AbstractEntity;
import storybook.db.scene.Scene;
import resources.icons.ICONS;
import resources.icons.IconUtil;

/**
 *
 * @author favdb
 */
public class EpisodeCellRenderer extends DefaultTableCellRenderer {

	private final boolean withTitle;

	public EpisodeCellRenderer(boolean withTitle) {
		this.withTitle = withTitle;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof JLabel) {
			return (JLabel) value;
		}
		JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);
		Dimension dim = new Dimension(table.getRowHeight(),
				table.getColumnModel().getColumn(column).getPreferredWidth());
		label.setPreferredSize(dim);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		if (value instanceof Integer) {
			label.setIcon(null);
			label.setText(value.toString());
		} else if (value instanceof AbstractEntity) {
			AbstractEntity v = (AbstractEntity) value;
			label.setIcon(v.getIcon());
			if (withTitle) {
				label.setText(v.getName());
				if (v instanceof Scene) {
					label.setToolTipText(EntityUtil.getTooltip(v));
				}
			} else {
				label.setText("");
				label.setToolTipText(EntityUtil.getTooltip(v));
			}
		} else {
			label.setText("");
			label.setIcon(IconUtil.getIconSmall(ICONS.K.UNKNOWN));
		}
		if (isSelected) {
			label.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Table.oddForeground")));
		} else {
			label.setBorder(BorderFactory.createEmptyBorder());
		}
		if (row % 2 == 0) {
			label.setBackground(UIManager.getColor("Table.oddBackground"));
			label.setForeground(UIManager.getColor("Table.oddForeground"));
		} else {
			label.setBackground(UIManager.getColor("Table.background"));
			label.setForeground(UIManager.getColor("Table.foreground"));
		}
		return label;
	}

}
