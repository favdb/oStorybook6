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
package storybook.tools.swing.js;

import i18n.I18N;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.table.TableModel;
import api.mig.swing.MigLayout;
import resources.icons.ICONS;
import storybook.App;
import storybook.ui.MIG;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class JSTableDialog extends JDialog {

	private final JSTable table;
	public int rc = -1;
	private JPanel panel;

	JSTableDialog(JSTable table) {
		super(/*(JFrame) SwingUtilities.getWindowAncestor(table)*/);
		this.table = table;
		setTitle(I18N.getMsg("table"));
		initUi();
	}

	private void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.WRAP, MIG.FILL)));
		panel = createInternalPanel();
		JScrollPane scroller = new JScrollPane(panel);
		scroller.setMaximumSize(new Dimension(Short.MAX_VALUE, 480));
		add(scroller, MIG.get(MIG.SPAN, MIG.GROWX));
		add(Ui.initButton("pack", "table.pack", ICONS.K.EMPTY, "", e -> {
			rc = 1;
			dispose();
		}));
		add(Ui.initButton("apply", "apply", ICONS.K.EMPTY, "", e -> {
			rc = 2;
			dispose();
		}), MIG.RIGHT);
		add(Ui.initButton("cancel", "cancel", ICONS.K.EMPTY, "", e -> {
			rc = -1;
			dispose();
		}), MIG.RIGHT);
		pack();
		setLocationRelativeTo(table);
		this.setModal(true);
		this.getRootPane().registerKeyboardAction((ActionEvent ae) -> dispose(),
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	/**
	 * Creates the internal panel for visibles.
	 *
	 * @return the JPanel
	 */
	private JPanel createInternalPanel() {
		// create internal dialog panel
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		// Fill panel with checkboxes
		TableModel model = table.getModel();
		for (int i = 0; i < model.getColumnCount(); ++i) {
			String name = table.getColumnName(i);
			Boolean bvisible = table.getVisibles().get(name);
			boolean visible = (bvisible == null) ? true : bvisible;
			JCheckBox box = new JCheckBox(name);
			box.setSelected(visible);
			if (!App.isDev() && name.equalsIgnoreCase("id")) {
				box.setVisible(false);
			}
			panel.add(box);
		}
		return panel;
	}

	public JPanel getPanel() {
		return panel;
	}

}
