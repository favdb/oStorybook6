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
package storybook.tools.swing.table;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import resources.icons.ICONS;
import storybook.db.scene.Scene;
import storybook.ui.MainFrame;
import storybook.ui.MainMenu;

/**
 *
 * @author favdb
 */
public class TableHeaderMouseListener extends MouseAdapter {

	private final JTable table;
	private final MainFrame mainFrame;

	public TableHeaderMouseListener(MainFrame m, JTable t) {
		mainFrame = m;
		table = t;
	}

	@Override
	public void mouseClicked(MouseEvent evt) {
		if (evt.getButton() == MouseEvent.BUTTON3) {
			Point point = evt.getPoint();
			int column = table.columnAtPoint(point);
			String colval = (String) table.getColumnModel().getColumn(column).getHeaderValue();
			//System.out.println("Column header #" + column + " is clicked, value="+colval);
			JComponent comp = (JComponent) evt.getSource();
			JPopupMenu menu = new JPopupMenu();
			Scene scene = mainFrame.project.scenes.get(Long.valueOf(colval));
			if (scene == null) {
				return;
			}
			menu.add(MainMenu.initMenuItem(ICONS.K.EDIT, "edit", "", ' ', "edit",
			   e -> mainFrame.showEditorAsDialog(scene)));
			menu.show(comp, evt.getX(), evt.getY());
		}
	}

}
