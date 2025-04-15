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
import i18n.I18N;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import resources.icons.ICONS;
import resources.icons.IconButton;
import resources.icons.IconUtil;
import storybook.dialog.MessageDlg;
import storybook.tools.file.IOUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.MIG;
import storybook.ui.interfaces.IRefreshable;

@SuppressWarnings("serial")
public class ImageChooserPanel extends JPanel implements IRefreshable, MouseListener {

	private final Icon startIcon;
	private final String startIconFile;
	private JTextField tfFile;
	private String currentFile;
	private JLabel lbIcon;
	private final String path;
	private final boolean isIcon;

	public ImageChooserPanel(String path, Icon startIcon, String startIconFile, boolean isIcon) {
		//LOG.trace("IconChooserPanel path=" + path);
		this.path = path;
		this.startIcon = startIcon;
		this.startIconFile = startIconFile;
		currentFile = startIconFile;
		this.isIcon = isIcon;
		initUi();
	}

	private void initUi() {
		setLayout(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0)));
		//the icon start
		tfFile = new JTextField();
		tfFile.setText(startIconFile);
		tfFile.setEditable(false);
		tfFile.setColumns(16);
		tfFile.setCaretPosition(0);
		add(tfFile);

		// the icon chooser
		IconButton btChooser = new IconButton("btChooser", ICONS.K.F_OPEN, "file.select", e -> getShowIconChooserAction());
		btChooser.setBounds(0, 0, 0, 0);
		SwingUtil.setForcedSize(btChooser, IconUtil.getDefDim());
		add(btChooser);

		// button to clear the icon and set it to default
		IconButton btClear = new IconButton("btClear", ICONS.K.CLEAR, "file.clear", e -> getClearIconAction());
		btClear.setBounds(0, 0, 0, 0);
		SwingUtil.setForcedSize(btClear, IconUtil.getDefDim());
		add(btClear);
		//set the lbIconFile
		lbIcon = new JLabel(startIcon);
		if (!isIcon) {
			add(lbIcon, MIG.get(MIG.SPAN, MIG.CENTER, MIG.WRAP));
		}

	}

	@Override
	public void refresh() {
		removeAll();
		initUi();
	}

	private void getShowIconChooserAction() {
		Component parent = getThis().getParent();
		if (parent == null) {
			parent = getThis();
		}
		ImageChooserDlg chooser = new ImageChooserDlg(true);
		if (currentFile != null && !currentFile.isEmpty()) {
			chooser.setSelectedFile(new File(currentFile));
		} else {
			chooser.setCurrentDirectory(new File(path + "."));
		}
		chooser.setUpper(path);
		int i = chooser.showOpenDialog(parent);
		if (i != 0) {
			return;
		}
		File file = chooser.getSelectedFile();
		// check allowed images as *.png or *.jpg or *.jpeg or *.bmp or *.gif
		if (!IOUtil.isImage(file)) {
			MessageDlg.show(tfFile, I18N.getMsg("imagechooser.not_image"), "image");
			return;
		}
		tfFile.setText(file.getAbsolutePath());
		tfFile.setCaretPosition(0);
		currentFile = file.getAbsolutePath();
		lbIcon.setIcon(IconUtil.getIconExternal(file.getAbsolutePath(), new Dimension(32, 32)));
	}

	private void getClearIconAction() {
		tfFile.setText("");
		currentFile = "";
		lbIcon.setIcon(startIcon);
	}

	/*
	 * Returns its self for use within anonymous objects that require references
	 * to this object without being able to use <code>this</code> keyword.
	 */
	protected ImageChooserPanel getThis() {
		return this;
	}

	public String getIconFile() {
		if (tfFile.getText().isEmpty()) {
			return null;
		}
		return tfFile.getText();
	}

	public void setIconFile(String str) {
		tfFile.setText(str);
		tfFile.setCaretPosition(0);
		currentFile = str;
		lbIcon.setIcon(new ImageIcon(str));
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
		if (source instanceof JLabel) {
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
