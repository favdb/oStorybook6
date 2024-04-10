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
import java.awt.Color;
import java.awt.GridBagLayout;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import resources.icons.ICONS;
import api.shef.dialogs.GBC;
import storybook.tools.file.EnvUtil;
import storybook.tools.swing.SwingUtil;
import storybook.ui.Ui;

/**
 *
 * @author favdb
 */
public class JSFileSelector extends JPanel {

	private JTextField tf;
	private final boolean toCheck;
	private final String onlyTitle;
	private final String onlyExt;

	/**
	 * simple file selector
	 *
	 * @param name: internal name for the selector
	 * @param title: prompt label
	 * @param value: initial value
	 * @param toCheck: true=file must exists
	 * @param only: limited allowed extension
	 */
	public JSFileSelector(String name,
			String title,
			String value,
			boolean toCheck,
			String... only) {
		super();
		this.setName(name);
		if (only != null) {
			this.onlyTitle = only[0];
			this.onlyExt = only[1];
		} else {
			this.onlyTitle = "";
			this.onlyExt = "";
		}
		this.toCheck = toCheck;
		initialize(title, value);
	}

	private void initialize(String title, String value) {
		setLayout(new GridBagLayout());
		if (!title.isEmpty()) {
			add(new JLabel(I18N.getColonMsg(title)), new GBC("0,0"));
		}
		tf = new JTextField(value);
		tf.setColumns(24);
		tf.addCaretListener(e -> {
			if (toCheck) {
				tf.setBorder(SwingUtil.getBorderDefault());
				File ftf = getFile();
				if (ftf != null && !ftf.exists()) {
					tf.setBorder(BorderFactory.createLineBorder(Color.red));
				}
			}
		});
		add(tf, new GBC("0,1,growx"));
		JButton bt = Ui.initButton("bt", "", ICONS.K.F_OPEN, "", e -> {
			String str = tf.getText();
			if (str.isEmpty()) {
				str = EnvUtil.getPrefDir().getAbsolutePath();
			}
			JFileChooser chooser = new JFileChooser(str);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (!onlyTitle.isEmpty()) {
				FileFilter filter = new FileNameExtensionFilter(onlyTitle, onlyExt);
				chooser.setFileFilter(filter);
			}
			int i = chooser.showOpenDialog(null);
			if (i != 0) {
				return;
			}
			setFile(chooser.getSelectedFile());
		});
		add(bt, new GBC("0,2"));
	}

	public void setFileName(String fileName) {
		tf.setText(fileName);
		tf.setCaretPosition(0);
	}

	public String getFileName() {
		if (getFile() != null) {
			return getFile().getAbsolutePath();
		}
		return "";
	}

	public void setFile(File file) {
		setFileName(file.getAbsolutePath());
	}

	public File getFile() {
		if (tf.getText().isEmpty()) {
			return null;
		}
		return new File(tf.getText().trim());
	}

}
