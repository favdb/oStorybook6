/*
PanelIconChooser: Clever Icon Chooser

This class needs MigLayout to compile:
http://www.miglayout.com/

Copyright (C) 2016 FaVdB

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
package storybook.dialog.chooser;

import api.mig.swing.MigLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import resources.icons.ICONS;
import resources.icons.IconButton;
import resources.icons.IconUtil;
import storybook.tools.swing.SwingUtil;
import storybook.tools.swing.js.JSLabel;
import storybook.ui.MainFrame;
import storybook.ui.interfaces.IRefreshable;

@SuppressWarnings("serial")
public class IconChooserPanel extends JPanel implements IRefreshable, MouseListener {

	private MainFrame mainFrame;
	private IconButton btChooser;
	private IconButton btClearIcon;
	private ImageIcon startIcon;
	private String startIconFile;
	private JTextField tfIconFile;
	private String currentFile;
	private JSLabel lbIconFile;

	public IconChooserPanel(MainFrame m) {
		this(m, IconUtil.getImageIcon("small.unlink"), "");
	}

	public IconChooserPanel(MainFrame m, ImageIcon startIcon, String startIconFile) {
		mainFrame = m;
		this.startIcon = startIcon;
		this.startIconFile = startIconFile;
		currentFile = startIconFile;
		initGUI();
	}

	private void initGUI() {
		MigLayout layout = new MigLayout("insets 0");
		setLayout(layout);
		//set the lbIconFile
		lbIconFile = new JSLabel(startIcon);
		//the icon start
		tfIconFile = new JTextField();
		tfIconFile.setText(startIconFile);
		tfIconFile.setEditable(false);
		SwingUtil.setForcedSize(tfIconFile, new Dimension(300, 20));
		tfIconFile.setCaretPosition(0);
		// the icon chooser
		btChooser = new IconButton("btChooser", ICONS.K.F_OPEN, getShowIconChooserAction());
		// button to clear the icon and set it to default
		btClearIcon = new IconButton("btClearIcon", ICONS.K.CLEAR, getClearIconAction());
		btClearIcon.set20x20();
		add(lbIconFile);
		add(tfIconFile);
		add(btChooser);
		add(btClearIcon);
	}

	@Override
	public void refresh() {
		removeAll();
		initGUI();
	}

	private AbstractAction getShowIconChooserAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Component parent = getThis().getParent();
				if (parent == null) {
					parent = getThis();
				}
				IconChooserDlg chooser = new IconChooserDlg();
				if (!currentFile.isEmpty()) {
					chooser.setSelectedFile(new File(currentFile));
				} else {
					chooser.setCurrentDirectory(new File(mainFrame.getProject().getPath()));
				}
				int i = chooser.showOpenDialog(parent);
				if (i != 0) {
					return;
				}
				File file = chooser.getSelectedFile();
				tfIconFile.setText(file.getAbsolutePath());
				currentFile = file.getAbsolutePath();
				lbIconFile.setIcon(new ImageIcon(file.getAbsolutePath()));
			}
		};
	}

	private AbstractAction getClearIconAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				tfIconFile.setText("");
				currentFile = "";
				lbIconFile.setIcon(startIcon);
			}
		};
	}

	/*
	 * Returns its self for use within anonymous objects that require references
	 * to this object without being able to use <code>this</code> keyword.
	 */
	protected IconChooserPanel getThis() {
		return this;
	}

	public String getIconFile() {
		if (tfIconFile.getText().isEmpty()) {
			return null;
		}
		return tfIconFile.getText();
	}

	public void setIconFile(String str) {
		tfIconFile.setText(str);
		tfIconFile.setCaretPosition(0);
		currentFile = str;
		lbIconFile.setIcon(new ImageIcon(str));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// empty
	}

	@Override
	public void mousePressed(MouseEvent evt) {
		Object source = evt.getSource();
		if (source instanceof JSLabel) {
			JComponent comp = (JComponent) source;
			JComponent parent1 = (JComponent) comp.getParent();
			JComponent parent2 = (JComponent) parent1.getParent();
			JPopupMenu menu = (JPopupMenu) parent2;
			menu.setVisible(false);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// empty
	}

}
